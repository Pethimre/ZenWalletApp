package com.aestroon.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A generic and performant way to add an expandable list section to a LazyColumn.
 *
 * @param T The type of items in the list.
 * @param sectionId A unique identifier for this section, used for generating stable keys
 * for the title and button items within the LazyColumn.
 * @param title The title of the section.
 * @param allItems The complete list of items for this section.
 * @param isExpanded Whether the section is currently expanded to show all items.
 * @param onToggleExpand Lambda to be invoked when the "Show All/Less" button is clicked.
 * @param maxVisibleItemsInitially The number of items to show when the section is collapsed.
 * @param itemKeyProvider An optional lambda to provide a stable and unique key for each item in `allItems`.
 * Crucial for performance and state preservation in LazyColumn.
 * It receives the item itself. If your items have a unique ID, use it here.
 * @param titleContent A composable lambda to render the title. Allows for custom title styling.
 * @param noItemsContent A composable lambda to render when `allItems` is empty or
 * the filtered list is empty.
 * @param buttonTexts A pair of strings for the "expand" and "collapse" button text.
 * @param buttonIcons A pair of ImageVectors for the expand and collapse icons.
 * @param itemContent The composable lambda responsible for rendering a single item of type T.
 */
fun <T> LazyListScope.expandableListSection(
    sectionId: String,
    title: String,
    allItems: List<T>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    maxVisibleItemsInitially: Int = 3,
    itemKeyProvider: ((item: T) -> Any)? = null,
    titleContent: @Composable (String) -> Unit = { sectionTitle ->
        Text(
            text = sectionTitle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    },
    noItemsContent: @Composable () -> Unit = {
        Text(
            text = "No items to display.",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    },
    buttonTexts: Pair<String, String> = "See All" to "Show Less",
    buttonIcons: Pair<ImageVector, ImageVector> = Icons.Default.ExpandMore to Icons.Default.ExpandLess,
    itemContent: @Composable (item: T) -> Unit
) {
    item(key = "${sectionId}_title") { // Unique key for the title item
        titleContent(title)
    }

    val displayedItems = if (isExpanded) allItems else allItems.take(maxVisibleItemsInitially)

    if (displayedItems.isEmpty()) {
        item(key = "${sectionId}_no_items") { // Unique key for the no items message
            noItemsContent()
        }
    } else {
        itemsIndexed(
            items = displayedItems,
            // Provide a key if itemKeyProvider is available, otherwise let LazyColumn handle it (less optimal)
            key = if (itemKeyProvider != null) { index, item -> itemKeyProvider(item) } else null
        ) { _, item -> // Index is available if needed, otherwise use items(displayedItems, key = ...)
            itemContent(item)
        }
    }

    // 4. "Show All" / "Show Less" Button
    if (allItems.size > maxVisibleItemsInitially) {
        item(key = "${sectionId}_toggle_button") { // Unique key for the button item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                TextButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) buttonIcons.second else buttonIcons.first,
                        contentDescription = if (isExpanded) buttonTexts.second else buttonTexts.first,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) buttonTexts.second else buttonTexts.first,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}