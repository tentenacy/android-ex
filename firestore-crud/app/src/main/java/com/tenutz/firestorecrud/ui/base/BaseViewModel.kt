package com.tenutz.firestorecrud.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tenutz.firestorecrud.util.Event
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class BaseViewModel : ViewModel() {

    protected val compositeDisposable = CompositeDisposable()

    private val _viewEvent = MutableLiveData<Event<Pair<Int, Any?>>>()
    val viewEvent: LiveData<Event<Pair<Int, Any?>>>
        get() = _viewEvent

    fun viewEvent(content: Pair<Int, Any?>) {
        _viewEvent.postValue(Event(content))
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}