package com.wgu.c196.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "terms")
public class TermEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;

    private Date startDate;

    private Date endDate;

    /**
     * Constructor for creating a new note with an auto-generated id
     *
     * @param title String
     * @param startDate Date
     * @param endDate Date
     */
    @Ignore
    public TermEntity(String title, Date startDate, Date endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Constructor for when you want to create a new term and then assign values
     * separately
     */
    @Ignore
    public TermEntity() { }

    /**
     * Constructor for editing an existing term
     *
     * @param id int
     * @param title String
     * @param startDate Date
     * @param endDate Date
     */
    public TermEntity(int id, String title, Date startDate, Date endDate) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "TermEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}