package com.isax.launcher.system

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class NotifItem(
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long
)

/** Bus partagé entre le NotificationListenerService et l'UI Compose. */
object NotificationBus {
    private val _items = MutableStateFlow<List<NotifItem>>(emptyList())
    val items: StateFlow<List<NotifItem>> = _items

    fun push(item: NotifItem) {
        _items.value = (listOf(item) + _items.value.filterNot { it.packageName == item.packageName && it.text == item.text })
            .take(40)
    }

    fun clear() { _items.value = emptyList() }
}
