package com.durini.solucionlab10.datasource.local_source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.durini.solucionlab10.datasource.model.LoadCharacter

@Database(
    entities = [LoadCharacter::class],
    version = 1
)
abstract class DataBase:RoomDatabase() {
    abstract fun characterDao():LoadCharacterDao
}