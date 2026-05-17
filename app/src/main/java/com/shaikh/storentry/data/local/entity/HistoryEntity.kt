package com.shaikh.storentry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shaikh.storentry.domain.model.HistoryActionType
import com.shaikh.storentry.domain.model.HistoryRecord

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: String,
    val productName: String,
    val actionType: String, // Stored as String for easy DB viewing
    val description: String,
    val timestamp: Long,
    val metadata: String? = null
) {
    fun toDomain() = HistoryRecord(
        id = id,
        productId = productId,
        productName = productName,
        actionType = HistoryActionType.valueOf(actionType),
        description = description,
        timestamp = timestamp,
        metadata = metadata
    )

    companion object {
        fun fromDomain(record: HistoryRecord) = HistoryEntity(
            id = record.id,
            productId = record.productId,
            productName = record.productName,
            actionType = record.actionType.name,
            description = record.description,
            timestamp = record.timestamp,
            metadata = record.metadata
        )
    }
}
