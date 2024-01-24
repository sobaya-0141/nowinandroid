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

package com.google.samples.apps.nowinandroid.core.testing.repository

import com.google.samples.apps.nowinandroid.core.data.repository.SearchContentsRepository
import sobaya.app.sharemodel.NewsResource
import sobaya.app.sharemodel.SearchResult
import sobaya.app.sharemodel.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class TestSearchContentsRepository : SearchContentsRepository {

    private val cachedTopics: MutableList<sobaya.app.sharemodel.Topic> = mutableListOf()
    private val cachedNewsResources: MutableList<sobaya.app.sharemodel.NewsResource> = mutableListOf()

    override suspend fun populateFtsData() { /* no-op */ }

    override fun searchContents(searchQuery: String): Flow<sobaya.app.sharemodel.SearchResult> = flowOf(
        sobaya.app.sharemodel.SearchResult(
            topics = cachedTopics.filter {
                it.name.contains(searchQuery) ||
                    it.shortDescription.contains(searchQuery) ||
                    it.longDescription.contains(searchQuery)
            },
            newsResources = cachedNewsResources.filter {
                it.content.contains(searchQuery) ||
                    it.title.contains(searchQuery)
            },
        ),
    )

    override fun getSearchContentsCount(): Flow<Int> = flow {
        emit(cachedTopics.size + cachedNewsResources.size)
    }

    /**
     * Test only method to add the topics to the stored list in memory
     */
    fun addTopics(topics: List<sobaya.app.sharemodel.Topic>) {
        cachedTopics.addAll(topics)
    }

    /**
     * Test only method to add the news resources to the stored list in memory
     */
    fun addNewsResources(newsResources: List<sobaya.app.sharemodel.NewsResource>) {
        cachedNewsResources.addAll(newsResources)
    }
}
