package rahulstech.android.recipe_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeEntity::class, RecipeMediaEntity::class],
    version = RecipeDatabase.DB_VERSION,
    exportSchema = true,
)
abstract class RecipeDatabase: RoomDatabase() {

    companion object {

        const val DB_NAME = "recipes.db3"
        const val DB_VERSION = 1

        fun getInstance(context: Context): RecipeDatabase {
            return Room.databaseBuilder(context, RecipeDatabase::class.java, DB_NAME)
                .build()
        }
    }

    abstract val recipeDao: RecipeDao
    abstract val recipeMediaDao: RecipeMediaDao
}