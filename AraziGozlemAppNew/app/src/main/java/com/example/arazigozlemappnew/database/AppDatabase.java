package com.example.arazigozlemappnew.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Bu anotasyon ile Room'a hangi Entity'lerin kullanılacağını ve veritabanı versiyonunu belirtiriz
@Database(entities = {Observation.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    // Uygulama boyunca tek bir veritabanı nesnesi olsun diye singleton olarak tanımlarız
    private static volatile AppDatabase INSTANCE;

    // Veritabanındaki DAO'ya erişmek için abstract bir metod tanımlarız
    public abstract ObservationDao observationDao();

    // Singleton örneğini almak için kullanılan metod
    public static AppDatabase getInstance(Context context) {
        // Eğer INSTANCE henüz oluşturulmamışsa
        if (INSTANCE == null) {
            // Çoklu iş parçacığına karşı önlem (thread-safe) için synchronized kullanılır
            synchronized (AppDatabase.class) {
                // INSTANCE hala null ise veritabanı oluşturulur
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),  // Context
                            AppDatabase.class,               // Database class'ı
                            "observation_database"           // Veritabanı dosya adı
                    ).build();
                }
            }
        }
        // Var olan veya yeni oluşturulan INSTANCE döndürülür
        return INSTANCE;
    }
}
