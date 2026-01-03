package rahulstech.android.recipebook

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RecipeBookApp: Application() {

    override fun onCreate() {
        super.onCreate()

        // Global Coil ImageLoader
        Coil.setImageLoader {
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        // 25% of available memory for memory cache
                        .maxSizePercent(.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        // 2% - 5% of available disk space for disk cache
                        .maxSizePercent(.2)
                        // put image cache inside app private cache directory
                        .directory(cacheDir.resolve("image_cache"))
                        .build()
                }
                .build()
        }
    }
}