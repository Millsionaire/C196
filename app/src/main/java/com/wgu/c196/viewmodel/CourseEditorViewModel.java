package com.wgu.c196.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.wgu.c196.database.*;
import com.wgu.c196.database.entities.AssessmentEntity;
import com.wgu.c196.database.entities.CourseEntity;
import com.wgu.c196.database.entities.CourseWithAssessments;
import com.wgu.c196.database.entities.MentorEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CourseEditorViewModel extends AndroidViewModel {

    public MutableLiveData<CourseWithAssessments> mLiveCourse = new MutableLiveData<>();
    public LiveData<List<AssessmentEntity>> mAssessments;
    public LiveData<List<MentorEntity>> mMentors;
    private AppRepository mRepository;
    private Executor executor = Executors.newSingleThreadExecutor();

    public CourseEditorViewModel(@NonNull Application application) {
        super(application);
        mRepository = AppRepository.getInstance(getApplication());
        mAssessments = mRepository.mAssessments;
        mMentors = mRepository.mMentors;
    }

    public void loadData(final int courseId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                CourseWithAssessments course = mRepository.getCourseWithAssessmentsById(courseId);
                if (course.assessments != null) {
                    mAssessments = mRepository.getAssessmentsByCourseId(courseId);
                }
                course.course.setMentor(mRepository.getMentorById(course.course.getMentorId()));
                mLiveCourse.postValue(course);
            }
        });
    }

    public void saveCourse(final CourseEntity newCourse) {
        CourseWithAssessments courseWithAssessments = mLiveCourse.getValue();
        if (courseWithAssessments == null) {
            if (TextUtils.isEmpty(newCourse.getTitle().trim())) {
                return;
            }
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    CourseEntity course;
                    MentorEntity mentor = mRepository.getFirstMentor();
                    course = new CourseEntity(newCourse.getTitle(), newCourse.getTermId(), newCourse.getStartDate(), newCourse.getEndDate(), CourseEntity.Status.PLAN_TO_TAKE, mentor.getId(), newCourse.getNotes());
                    mRepository.insertCourse(course);
                }
            });

        } else {
            CourseEntity course;
            course = courseWithAssessments.course;
            course.setTitle(newCourse.getTitle());
            course.setStartDate(newCourse.getStartDate());
            course.setEndDate(newCourse.getEndDate());
            course.setNotes(newCourse.getNotes());
            mRepository.insertCourse(course);
        }

    }

    public void deleteCourse() {
        try {
            mRepository.deleteCourse(mLiveCourse.getValue());
        } catch (SQLiteConstraintException e) {
            throw new SQLiteConstraintException(e.getMessage());
        }
    }

    public void updateMentor(String mentorName) {
        final String finalMentorName = mentorName;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MentorEntity mentor = mRepository.getMentorByName(finalMentorName);
                CourseWithAssessments courseWithAssessments = mLiveCourse.getValue();
                courseWithAssessments.course.setMentorId(mentor.getId());
                courseWithAssessments.course.setMentor(mentor);
                mRepository.insertCourse(courseWithAssessments.course);
                mLiveCourse.postValue(courseWithAssessments);
            }
        });
    }

    public void updateStatus(CourseEntity.Status status) {
        CourseWithAssessments courseWithAssessments = mLiveCourse.getValue();
        courseWithAssessments.course.setStatus(status);
        mRepository.insertCourse(courseWithAssessments.course);
        mLiveCourse.postValue(courseWithAssessments);
    }

    public void saveMentor(MentorEntity newMentor) {
        final MentorEntity finalNewMentor = newMentor;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mRepository.insertMentor(finalNewMentor);
                mRepository.refreshMentors();
            }
        });

    }
}
