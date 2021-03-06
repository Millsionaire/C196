package com.wgu.c196.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;
import com.wgu.c196.database.entities.TermWithCourses;
import com.wgu.c196.database.entities.TermEntity;

import java.util.List;

@Dao
public interface TermDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTerm(TermEntity termEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TermEntity> terms);

    @Delete
    void deleteTerm(TermEntity termEntity);

    @Query("SELECT * FROM terms WHERE id = :id")
    TermEntity getTermById(int id);

    @Query("SELECT * FROM terms WHERE title = :title")
    TermEntity getIdByTitle(String title);

    @Query("SELECT * FROM terms ORDER BY startDate DESC")
    LiveData<List<TermEntity>> getAll();

    @Query("DELETE FROM terms")
    int deleteAll();

    @Query("SELECT COUNT(*) FROM terms")
    int getCount();

    @Transaction
    @Query("SELECT * FROM terms WHERE id = :id")
    TermWithCourses getTermWithCourses(int id);
}
