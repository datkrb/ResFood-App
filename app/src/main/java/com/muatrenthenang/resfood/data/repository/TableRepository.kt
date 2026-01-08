package com.muatrenthenang.resfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Table
import kotlinx.coroutines.tasks.await

class TableRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tablesRef = db.collection("tables")

    suspend fun createTable(table: Table): Result<Boolean> {
        return try {
            val docRef = tablesRef.document()
            val finalTable = table.copy(id = docRef.id)
            docRef.set(finalTable).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTables(): Result<List<Table>> {
        return try {
            val snapshot = tablesRef.orderBy("name").get().await()
            val tables = snapshot.toObjects(Table::class.java)
            Result.success(tables)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTableStatus(tableId: String, newStatus: String): Result<Boolean> {
        return try {
            tablesRef.document(tableId).update("status", newStatus).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTable(tableId: String): Result<Boolean> {
        return try {
            tablesRef.document(tableId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
