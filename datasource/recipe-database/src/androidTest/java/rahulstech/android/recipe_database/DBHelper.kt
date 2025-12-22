package rahulstech.android.recipe_database

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider

fun createInMemoryRecipeDatabase(vararg callback: RoomDatabase.Callback): RecipeDatabase {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val builder = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
        .allowMainThreadQueries()
    callback.forEach { builder.addCallback(it) }
    return builder.build()
}

val FAKE_DATA_1 = object : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO `recipes` (`id`,`title`,`note`) VALUES ('recipe-1','title 1','note 1')")
        db.execSQL("INSERT INTO `recipes` (`id`,`title`,`note`, `ingredients`, `steps`, `coverPhoto`)" +
                " VALUES ('recipe-2','title 2','note 2','1. ingredient 1', '1. step1', 'cover-photo-1')")
    }
}