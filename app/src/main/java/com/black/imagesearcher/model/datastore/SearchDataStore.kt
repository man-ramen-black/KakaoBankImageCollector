package com.black.imagesearcher.model.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.black.imagesearcher.base.datastore.BaseDataStore
import com.black.imagesearcher.model.SearchModel
import com.black.imagesearcher.model.data.Content
import com.black.imagesearcher.util.JsonUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [SearchModel]
 */
class SearchDataStore @Inject constructor(
    @ApplicationContext context: Context
): BaseDataStore(context) {
    companion object {
        private val Context.searchDataStore: DataStore<Preferences> by preferencesDataStore(name = "ImageSearcher.Search")

        private val KEY_FAVORITE = stringPreferencesKey("favorite")
    }

    override fun getDataStore(context: Context): DataStore<Preferences> {
        return context.searchDataStore
    }

    suspend fun getFavorite(): Set<Content> {
        return get(KEY_FAVORITE, "[]")
            .let { JsonUtil.from(it, true) ?: emptySet() }
    }

    fun getFavoriteFlow(): Flow<Set<Content>> {
        return flow(KEY_FAVORITE, "[]")
            .map { JsonUtil.from(it, true) ?: emptySet() }
    }

    suspend fun updateFavorite(favoriteSet: Set<Content>) {
        val updateData = favoriteSet
            .let { JsonUtil.to(it) ?: "" }
        update(KEY_FAVORITE, updateData)
    }
}