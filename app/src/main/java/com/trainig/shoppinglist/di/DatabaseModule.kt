package com.trainig.shoppinglist.di

import android.content.Context
import androidx.room.Room
import com.trainig.shoppinglist.data.ProductDao
import com.trainig.shoppinglist.data.ShoppingListDatabase
import com.trainig.shoppinglist.data.ShoppingListRepositoryImpl
import com.trainig.shoppinglist.domain.ShoppingListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ShoppingListDatabase {
        return Room.databaseBuilder(
            context,
            ShoppingListDatabase::class.java,
            "shopping_list.db"
        )
            .addMigrations(com.trainig.shoppinglist.data.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: ShoppingListDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideShoppingListRepository(
        repository: ShoppingListRepositoryImpl
    ): ShoppingListRepository = repository
}
