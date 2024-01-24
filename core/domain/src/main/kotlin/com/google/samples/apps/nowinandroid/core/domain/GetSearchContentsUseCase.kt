/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.core.domain

import com.google.samples.apps.nowinandroid.core.data.repository.SearchContentsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import sobaya.app.sharemodel.FollowableTopic
import sobaya.app.sharemodel.SearchResult
import sobaya.app.sharemodel.UserData
import sobaya.app.sharemodel.UserNewsResource
import sobaya.app.sharemodel.UserSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * A use case which returns the searched contents matched with the search query.
 */
class GetSearchContentsUseCase @Inject constructor(
    private val searchContentsRepository: SearchContentsRepository,
    private val userDataRepository: UserDataRepository,
) {

    operator fun invoke(
        searchQuery: String,
    ): Flow<sobaya.app.sharemodel.UserSearchResult> =
        searchContentsRepository.searchContents(searchQuery)
            .mapToUserSearchResult(userDataRepository.userData)
}

private fun Flow<sobaya.app.sharemodel.SearchResult>.mapToUserSearchResult(userDataStream: Flow<sobaya.app.sharemodel.UserData>): Flow<sobaya.app.sharemodel.UserSearchResult> =
    combine(userDataStream) { searchResult, userData ->
        sobaya.app.sharemodel.UserSearchResult(
            topics = searchResult.topics.map { topic ->
                sobaya.app.sharemodel.FollowableTopic(
                    topic = topic,
                    isFollowed = topic.id in userData.followedTopics,
                )
            },
            newsResources = searchResult.newsResources.map { news ->
                sobaya.app.sharemodel.UserNewsResource(
                    newsResource = news,
                    userData = userData,
                )
            },
        )
    }
