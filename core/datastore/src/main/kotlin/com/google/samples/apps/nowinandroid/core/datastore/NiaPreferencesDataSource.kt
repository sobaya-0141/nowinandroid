/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.google.samples.apps.nowinandroid.core.datastore.DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
import com.google.samples.apps.nowinandroid.core.datastore.DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
import com.google.samples.apps.nowinandroid.core.datastore.DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
import com.google.samples.apps.nowinandroid.core.datastore.DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED
import com.google.samples.apps.nowinandroid.core.datastore.ThemeBrandProto.THEME_BRAND_ANDROID
import com.google.samples.apps.nowinandroid.core.datastore.ThemeBrandProto.THEME_BRAND_DEFAULT
import com.google.samples.apps.nowinandroid.core.datastore.ThemeBrandProto.THEME_BRAND_UNSPECIFIED
import com.google.samples.apps.nowinandroid.core.datastore.ThemeBrandProto.UNRECOGNIZED
import sobaya.app.sharemodel.DarkThemeConfig
import sobaya.app.sharemodel.ThemeBrand
import sobaya.app.sharemodel.UserData
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class NiaPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val userData = userPreferences.data
        .map {
            sobaya.app.sharemodel.UserData(
                bookmarkedNewsResources = it.bookmarkedNewsResourceIdsMap.keys,
                viewedNewsResources = it.viewedNewsResourceIdsMap.keys,
                followedTopics = it.followedTopicIdsMap.keys,
                themeBrand = when (it.themeBrand) {
                    null,
                    THEME_BRAND_UNSPECIFIED,
                    UNRECOGNIZED,
                    THEME_BRAND_DEFAULT,
                    -> sobaya.app.sharemodel.ThemeBrand.DEFAULT

                    THEME_BRAND_ANDROID -> sobaya.app.sharemodel.ThemeBrand.ANDROID
                },
                darkThemeConfig = when (it.darkThemeConfig) {
                    null,
                    DARK_THEME_CONFIG_UNSPECIFIED,
                    DarkThemeConfigProto.UNRECOGNIZED,
                    DARK_THEME_CONFIG_FOLLOW_SYSTEM,
                    ->
                        sobaya.app.sharemodel.DarkThemeConfig.FOLLOW_SYSTEM

                    DARK_THEME_CONFIG_LIGHT ->
                        sobaya.app.sharemodel.DarkThemeConfig.LIGHT

                    DARK_THEME_CONFIG_DARK -> sobaya.app.sharemodel.DarkThemeConfig.DARK
                },
                useDynamicColor = it.useDynamicColor,
                shouldHideOnboarding = it.shouldHideOnboarding,
            )
        }

    suspend fun setFollowedTopicIds(topicIds: Set<String>) {
        try {
            userPreferences.updateData {
                it.copy {
                    followedTopicIds.clear()
                    followedTopicIds.putAll(topicIds.associateWith { true })
                    updateShouldHideOnboardingIfNecessary()
                }
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update user preferences", ioException)
        }
    }

    suspend fun setTopicIdFollowed(topicId: String, followed: Boolean) {
        try {
            userPreferences.updateData {
                it.copy {
                    if (followed) {
                        followedTopicIds.put(topicId, true)
                    } else {
                        followedTopicIds.remove(topicId)
                    }
                    updateShouldHideOnboardingIfNecessary()
                }
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update user preferences", ioException)
        }
    }

    suspend fun setThemeBrand(themeBrand: sobaya.app.sharemodel.ThemeBrand) {
        userPreferences.updateData {
            it.copy {
                this.themeBrand = when (themeBrand) {
                    sobaya.app.sharemodel.ThemeBrand.DEFAULT -> ThemeBrandProto.THEME_BRAND_DEFAULT
                    sobaya.app.sharemodel.ThemeBrand.ANDROID -> ThemeBrandProto.THEME_BRAND_ANDROID
                }
            }
        }
    }

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferences.updateData {
            it.copy {
                this.useDynamicColor = useDynamicColor
            }
        }
    }

    suspend fun setDarkThemeConfig(darkThemeConfig: sobaya.app.sharemodel.DarkThemeConfig) {
        userPreferences.updateData {
            it.copy {
                this.darkThemeConfig = when (darkThemeConfig) {
                    sobaya.app.sharemodel.DarkThemeConfig.FOLLOW_SYSTEM ->
                        DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                    sobaya.app.sharemodel.DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                    sobaya.app.sharemodel.DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                }
            }
        }
    }

    suspend fun setNewsResourceBookmarked(newsResourceId: String, bookmarked: Boolean) {
        try {
            userPreferences.updateData {
                it.copy {
                    if (bookmarked) {
                        bookmarkedNewsResourceIds.put(newsResourceId, true)
                    } else {
                        bookmarkedNewsResourceIds.remove(newsResourceId)
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update user preferences", ioException)
        }
    }

    suspend fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        setNewsResourcesViewed(listOf(newsResourceId), viewed)
    }

    suspend fun setNewsResourcesViewed(newsResourceIds: List<String>, viewed: Boolean) {
        userPreferences.updateData { prefs ->
            prefs.copy {
                newsResourceIds.forEach { id ->
                    if (viewed) {
                        viewedNewsResourceIds.put(id, true)
                    } else {
                        viewedNewsResourceIds.remove(id)
                    }
                }
            }
        }
    }

    suspend fun getChangeListVersions() = userPreferences.data
        .map {
            ChangeListVersions(
                topicVersion = it.topicChangeListVersion,
                newsResourceVersion = it.newsResourceChangeListVersion,
            )
        }
        .firstOrNull() ?: ChangeListVersions()

    /**
     * Update the [ChangeListVersions] using [update].
     */
    suspend fun updateChangeListVersion(update: ChangeListVersions.() -> ChangeListVersions) {
        try {
            userPreferences.updateData { currentPreferences ->
                val updatedChangeListVersions = update(
                    ChangeListVersions(
                        topicVersion = currentPreferences.topicChangeListVersion,
                        newsResourceVersion = currentPreferences.newsResourceChangeListVersion,
                    ),
                )

                currentPreferences.copy {
                    topicChangeListVersion = updatedChangeListVersions.topicVersion
                    newsResourceChangeListVersion = updatedChangeListVersions.newsResourceVersion
                }
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update user preferences", ioException)
        }
    }

    suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
        userPreferences.updateData {
            it.copy {
                this.shouldHideOnboarding = shouldHideOnboarding
            }
        }
    }
}

private fun UserPreferencesKt.Dsl.updateShouldHideOnboardingIfNecessary() {
    if (followedTopicIds.isEmpty() && followedAuthorIds.isEmpty()) {
        shouldHideOnboarding = false
    }
}
