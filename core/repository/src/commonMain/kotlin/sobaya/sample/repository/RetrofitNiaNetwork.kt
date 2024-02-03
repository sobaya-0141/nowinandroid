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

package sobaya.sample.repository

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.serialization.Serializable
import sobaya.sample.repository.model.NetworkChangeList
import sobaya.sample.repository.model.NetworkNewsResource
import sobaya.sample.repository.model.NetworkTopic

/**
 * Retrofit API declaration for NIA Network API
 */
internal interface RetrofitNiaNetworkApi {
    @GET(value = "topics")
    suspend fun getTopics(
        @Query("id") ids: List<String>?,
    ): NetworkResponse<List<NetworkTopic>>

    @GET(value = "newsresources")
    suspend fun getNewsResources(
        @Query("id") ids: List<String>?,
    ): NetworkResponse<List<NetworkNewsResource>>

    @GET(value = "changelists/topics")
    suspend fun getTopicChangeList(
        @Query("after") after: Int?,
    ): List<NetworkChangeList>

    @GET(value = "changelists/newsresources")
    suspend fun getNewsResourcesChangeList(
        @Query("after") after: Int?,
    ): List<NetworkChangeList>
}

private const val NIA_BASE_URL = "http://example.com"

/**
 * Wrapper for data provided from the [NIA_BASE_URL]
 */
@Serializable
data class NetworkResponse<T>(
    val data: T,
)

/**
 * [Retrofit] backed [NiaNetworkDataSource]
 */
class RetrofitNiaNetwork constructor(
//    networkJson: Json,
) : NiaNetworkDataSource {

    private val networkApi = Ktorfit.Builder()
        .baseUrl(NIA_BASE_URL)
//        .addConverterFactory(
//            networkJson.asConverterFactory("application/json".toMediaType()),
//        )
        .build()
        .create<RetrofitNiaNetworkApi>()

    override suspend fun getTopics(ids: List<String>?): List<NetworkTopic> =
        networkApi.getTopics(ids = ids).data

    override suspend fun getNewsResources(ids: List<String>?): List<NetworkNewsResource> =
        networkApi.getNewsResources(ids = ids).data

    override suspend fun getTopicChangeList(after: Int?): List<NetworkChangeList> =
        networkApi.getTopicChangeList(after = after)

    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> =
        networkApi.getNewsResourcesChangeList(after = after)
}
