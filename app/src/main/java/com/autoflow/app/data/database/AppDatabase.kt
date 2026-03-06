package com.autoflow.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.autoflow.app.data.database.entities.Action
import com.autoflow.app.data.database.entities.Condition
import com.autoflow.app.data.database.entities.ExecutionLog
import com.autoflow.app.data.database.entities.Routine
import com.autoflow.app.data.database.entities.Trigger

@Database(
    entities = [
        Routine::class,
        Trigger::class,
        Condition::class,
        Action::class,
        ExecutionLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autoflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
