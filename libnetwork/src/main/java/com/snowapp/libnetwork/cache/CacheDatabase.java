package com.snowapp.libnetwork.cache;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.snowapp.libcommon.global.AppGlobals;

/**
 * @date 2020-08-20
 * @author snow
 * @description 缓存数据库
 */
@Database(entities = {Cache.class}, version = 1, exportSchema = true)
public abstract class CacheDatabase extends RoomDatabase {
    private static CacheDatabase database = null;
    private static final String DATABASE_NAME = "jjfunny_cache";

    static {
        // 内存数据库
        // 但该数据库只存在与内存中，换句话说就是进程被杀之后，数据随之丢失，所以很少用到
        // Room.inMemoryDatabaseBuilder();
        database = Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase.class, DATABASE_NAME)
                //是否允许在主线程进行查询
                .allowMainThreadQueries()
                //数据库创建和打开后的回调
                //.addCallback()
                //设置查询的线程池
                //.setQueryExecutor()
                //.openHelperFactory()
                //room的日志模式
                //.setJournalMode()
                //数据库升级异常之后的回滚
                //.fallbackToDestructiveMigration()
                //数据库升级异常后根据指定版本进行回滚
                //.fallbackToDestructiveMigrationFrom()
                // 数据库升级操作入口
                // .addMigrations(CacheDatabase.sMigration)
                .build();
    }

    public abstract CacheDao getCache();

    public static CacheDatabase get() {
        return database;
    }

//    static Migration sMigration = new Migration(1, 3) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("alter table teacher rename to student");
//            database.execSQL("alter table teacher add column teacher_age INTEGER NOT NULL default 0");
//        }
//    };

}
