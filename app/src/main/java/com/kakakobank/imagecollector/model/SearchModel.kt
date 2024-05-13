package com.kakakobank.imagecollector.model

import com.kakakobank.imagecollector.model.data.Contents
import com.kakakobank.imagecollector.model.data.NetworkResult
import com.kakakobank.imagecollector.model.data.ServerException
import com.kakakobank.imagecollector.model.datastore.SearchDataStore
import com.kakakobank.imagecollector.model.network.search.SearchApi
import com.kakakobank.imagecollector.model.network.search.SortType
import com.kakakobank.imagecollector.ui.search.SearchViewModel
import com.kakakobank.imagecollector.util.Log
import kotlinx.coroutines.flow.Flow
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * [SearchViewModel]
 */
class SearchModel @Inject constructor(
    private val dataStore: SearchDataStore
) {
    companion object {
        private const val PAGE_SIZE = 10
        private val SORT_TYPE = SortType.RECENCY
    }

    suspend fun searchImage(keyword: String, page: Int): NetworkResult<Pair<Boolean, List<Contents>>> {
        val result = SearchApi.searchImage(
            keyword,
            page,
            PAGE_SIZE,
            SORT_TYPE
        )
        Log.d(result)

        if (!result.isSuccess) {
            return NetworkResult.failure(result)
        }

        val documents = result.response?.documents
            ?: return NetworkResult.failure(result, ServerException("[${result.response?.errorType}] ${result.response?.message}"))

        val isEnd = result.response.meta?.isEnd ?: true
        val contentsList = documents
            .map {
                Contents(
                    Contents.Type.IMAGE,
                    it.thumbnailUrl,
                    it.displaySiteName,
                    it.collection,
                    it.docUrl,
                    toTimeMillis(it.datetime)
                )
            }
        return NetworkResult.success(result, isEnd to contentsList)
    }

    suspend fun searchVideo(keyword: String, page: Int): NetworkResult<Pair<Boolean, List<Contents>>> {
        val result = SearchApi.searchVideo(
            keyword,
            page,
            PAGE_SIZE,
            SORT_TYPE
        )
        Log.d(result)

        if (!result.isSuccess) {
            return NetworkResult.failure(result)
        }

        val documents = result.response?.documents
            ?: return NetworkResult.failure(result, ServerException("[${result.response?.errorType}] ${result.response?.message}"))

        val isEnd = result.response.meta?.isEnd ?: true
        val contentsList = documents
            .map {
                Contents(
                    Contents.Type.VIDEO,
                    it.thumbnail,
                    it.title,
                    "",
                    it.url,
                    toTimeMillis(it.datetime)
                )
            }
        return NetworkResult.success(result, isEnd to contentsList)
    }

    fun getFavoriteFlow(): Flow<Set<Contents>> {
        return dataStore.getFavoriteFlow()
    }

    suspend fun toggleFavorite(contents: Contents) {
        val favoriteSet = dataStore.getFavorite()
            .toMutableSet()
        if (favoriteSet.contains(contents)) {
            favoriteSet.remove(contents)
        } else {
            favoriteSet.add(contents)
        }
        dataStore.updateFavorite(favoriteSet)
    }

    private fun toTimeMillis(iso8601: String): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                .parse(iso8601)
                ?.time ?: 0L
        } catch (e: ParseException) {
            e.printStackTrace()
            0L
        }
    }
}