package com.durini.solucionlab10.datasource.local_source

import androidx.room.*
import com.durini.solucionlab10.datasource.model.LoadCharacter

@Dao
interface LoadCharacterDao {
    @Query("SELECT * FROM LoadCharacter")
    suspend fun getAllLoadCharacters():List<LoadCharacter>

    @Query("SELECT * FROM LoadCharacter WHERE id = :id")
    suspend fun getLoadCharacter(id: Int): LoadCharacter

    @Query("DELETE FROM LoadCharacter WHERE id = :id")
    suspend fun deleteCharacter(id: Int)

    @Insert
    suspend fun createLoadCharacter(NewCharacter: LoadCharacter)

    @Update
    suspend fun update(UpdtChar: LoadCharacter)

    @Query("DELETE FROM LoadCharacter")
    suspend fun deleteAll():Int
}