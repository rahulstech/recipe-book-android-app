package rahulstech.android.recipe_database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "recipes"
)
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val note: String,
    val ingredients: String? = null,
    val steps: String? = null,
    val coverPhoto: String? = null,
)

data class RecipeItem(
    val id: String,
    val title: String,
    val note: String,
    val coverPhoto: String,
)

@Dao
interface RecipeDao {

    @Insert
    fun insert(recipe: RecipeEntity)

    @Query("SELECT * FROM `recipes` WHERE `id` = :id")
    fun findById(id: String): Flow<RecipeEntity?>

    @Query("SELECT `id`, `title`, `note`, `coverPhoto` FROM `recipes`")
    fun getAllRecipes(): Flow<List<RecipeItem>>

    @Update
    fun update(recipe: RecipeEntity)

    @Delete
    fun delete(recipe: RecipeEntity)
}