package org.brightmindenrichment.street_care.util

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// https://medium.com/android-news/implementing-search-on-type-in-android-with-coroutines-ab117c8f13a4
class DebouncingQueryTextListener(
    lifecycle: Lifecycle,
    private val onDebouncingQueryTextChange: (String?) -> Unit
) : SearchView.OnQueryTextListener {
    private var debouncePeriod: Long = 1000L

    private val coroutineScope = lifecycle.coroutineScope

    private var searchJob: Job? = null

    override fun onQueryTextSubmit(inputText: String?): Boolean {
        /*
        inputText?.let {
            onDebouncingQueryTextChange(inputText)
        }

         */
        return false
    }

    override fun onQueryTextChange(inputText: String?): Boolean {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            inputText?.let {
                delay(debouncePeriod)
                onDebouncingQueryTextChange(inputText)
            }
        }
        return false
    }
}