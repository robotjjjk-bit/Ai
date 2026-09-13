package com.nexchat.app.data.local

import androidx.room.*
import com.nexchat.app.data.model.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Dao
interface NexDao {
    @Query("SELECT * FROM providers ORDER BY name") suspend fun providers(): List<ProviderEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertProvider(p: ProviderEntity)
    @Query("DELETE FROM providers WHERE id=:id") suspend fun deleteProvider(id: String)
    @Query("SELECT * FROM conversations ORDER BY createdAt DESC") suspend fun conversations(): List<ConversationEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertConversation(c: ConversationEntity)
    @Query("DELETE FROM conversations WHERE id=:id") suspend fun deleteConversation(id: String)
    @Query("SELECT * FROM messages WHERE conversationId=:cid ORDER BY createdAt") suspend fun messages(cid: String): List<MessageEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertMessage(m: MessageEntity)
    @Query("DELETE FROM messages WHERE conversationId=:cid") suspend fun clearMessages(cid: String)
    @Query("SELECT * FROM messages WHERE parentId=:pid ORDER BY branchIndex") suspend fun branches(pid: String): List<MessageEntity>
}

@Database(entities = [ProviderEntity::class, ConversationEntity::class, MessageEntity::class], version = 1, exportSchema = true)
abstract class AppDb : RoomDatabase() { abstract fun dao(): NexDao }

@Module @InstallIn(SingletonComponent::class)
object DbModule {
    @Provides @Singleton
    fun db(@dagger.hilt.android.qualifiers.ApplicationContext ctx: android.content.Context): AppDb =
        Room.databaseBuilder(ctx, AppDb::class.java, "nexchat.db").fallbackToDestructiveMigration().build()
    @Provides fun dao(db: AppDb): NexDao = db.dao()
}
