package rahulstech.android.recipe_database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "recipe_medias",
    foreignKeys = [
        ForeignKey(entity = RecipeEntity::class, childColumns = ["recipeId"], parentColumns = ["id"], onDelete = CASCADE)
    ],
    indices = [
        Index(name = "index__recipe_medias__recipeId", value = ["recipeId"])
    ]
)
data class RecipeMediaEntity(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val data: String,
    val caption: String? = null,
)

@Dao
interface RecipeMediaDao {

    @Insert
    fun insertMultiple(medias: List<RecipeMediaEntity>)

    @Query("SELECT * FROM `recipe_medias` WHERE `id` = :id")
    fun observeMediaById(id: String): Flow<RecipeMediaEntity?>

    @Query("SELECT * FROM `recipe_medias` WHERE `recipeId` = :recipeId")
    fun observeMediasForRecipe(recipeId: String): Flow<List<RecipeMediaEntity>>

    @Update
    fun updateMultiple(medias: List<RecipeMediaEntity>)

    @Query("DELETE FROM `recipe_medias` WHERE `id` IN(:mediaIds)")
    fun deleteMultiple(mediaIds: List<String>)
}