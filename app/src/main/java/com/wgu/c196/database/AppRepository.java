package com.wgu.c196.database;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import com.wgu.c196.database.entities.*;
import com.wgu.c196.utilities.SampleData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppRepository {
    private static AppRepository ourInstance;

    public LiveData<List<TermEntity>> mTerms;
    public LiveData<List<CourseEntity>> mCourses;
    public LiveData<List<AssessmentEntity>> mAssessments;
    public LiveData<List<MentorEntity>> mMentors;
    private AppDatabase mDb;
    private Executor executor = Executors.newSingleThreadExecutor();

    public static AppRepository getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new AppRepository(context);
        }
        return ourInstance;
    }

    private AppRepository(Context context) {
        mDb = AppDatabase.getInstance(context);
        mTerms = getAllTerms();
        mMentors = getAllMentors();
    }

    private LiveData<List<MentorEntity>> getAllMentors() {
        return mDb.mentorDao().getAll();
    }

    public void refreshMentors() {
        mMentors = this.getAllMentors();
    }

    public void addSampleData() {
        executor.execute(new Runnable() {
            @Override
            public void run() {

                mDb.termDao().insertAll(SampleData.getTerms());

                for (TermEntity term : SampleData.getTerms()) {
                    insertCoursesForTerm(term);
                }
            }
        });
    }

    private void insertCoursesForTerm(TermEntity term) {
        List<CourseEntity> courses = term.getCourses();
        if (courses != null) {
            for (CourseEntity course : courses) {
                TermEntity selectedTerm = mDb.termDao().getIdByTitle(term.getTitle());
                course.setTermId(selectedTerm.getId());
                mDb.courseDao().insertCourse(course);
                insertAssessmentsForCourse(course);
            }

        }
    }

    private void insertAssessmentsForCourse(CourseEntity course) {
        List<AssessmentEntity> assessments = course.getAssessments();
        if (assessments != null) {
            for (AssessmentEntity assessment : assessments) {
                CourseEntity selectedCourse = mDb.courseDao().getIdByTitle(course.getTitle());
                assessment.setCourseId(selectedCourse.getId());
            }
            mDb.assessmentDao().insertAll(assessments);
        }
    }

    private LiveData<List<TermEntity>> getAllTerms() {
        return mDb.termDao().getAll();
    }

    public void deleteAllData() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mDb.assessmentDao().deleteAll();
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mDb.courseDao().deleteAll();
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mDb.termDao().deleteAll();
            }
        });
    }

    public TermEntity getTermById(int termId) {
        return mDb.termDao().getTermById(termId);
    }

    public TermWithCourses getTermWithCoursesById(int termId) {
        return mDb.termDao().getTermWithCourses(termId);
    }

    public CourseWithAssessments getCourseWithAssessmentsById(int courseId) {
        return mDb.courseDao().getCoursesWithAssessments(courseId);
    }

    public void insertTerm(final TermEntity term) {
        insertCoursesForTerm(term);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mDb.termDao().insertTerm(term);
            }
        });
    }

    public void insertCourse(final CourseEntity course) {
        insertAssessmentsForCourse(course);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mDb.courseDao().insertCourse(course);
            }
        });
    }

    public void deleteTerm(final TermWithCourses term) {
        final Boolean[] foreignKeyConstraint = {false};
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mDb.termDao().deleteTerm(term.term);
                } catch (Exception e) {
                    String msg = e.getMessage();
                    assert msg != null;
                    if (msg.equals("foreign key constraint failed (code 19)")) {
                        foreignKeyConstraint[0] = true;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        });
        if (foreignKeyConstraint[0]) {
            throw new SQLiteConstraintException();
        }
    }

    public void deleteCourse(final CourseWithAssessments course) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mDb.courseDao().deleteCourse(course.course);
            }
        });
    }

    public LiveData<List<CourseEntity>> getCoursesByTermId(final int termId) {
        return mDb.courseDao().getCoursesByTermId(termId);
    }

    public LiveData<List<AssessmentEntity>> getAssessmentsByCourseId(int courseId) {
        return mDb.assessmentDao().getAssessmentsByCourseId(courseId);
    }

    public MentorEntity getMentorById(int mentorId) {
        return mDb.mentorDao().getMentorById(mentorId);
    }

    public AssessmentEntity getAssessmentById(int assessmentId) {
        return mDb.assessmentDao().getAssessmentById(assessmentId);
    }

    public void insertAssessment(AssessmentEntity assessment) {
        mDb.assessmentDao().insertAssessment(assessment);
    }

    public void deleteAssessment(AssessmentEntity assessment) {
        mDb.assessmentDao().deleteAssessment(assessment);
    }

    public MentorEntity getMentorByName(String mentorName) {
        return mDb.mentorDao().getMentorByName(mentorName);
    }

    public MentorEntity getFirstMentor() {
        return mDb.mentorDao().getFirstMentor();
    }

    public void addMentors() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (mDb.mentorDao().getMentorById(1) == null) {
                    mDb.mentorDao().insertMentor(new MentorEntity(1, "Becky Stovall", "555-222-4444", "bstovall@wgu.edu"));
                    mDb.mentorDao().insertMentor(new MentorEntity(2, "John Doe", "444-333-2222", "jdoe@wgu.edu"));
                    mDb.mentorDao().insertMentor(new MentorEntity(3, "Jane Doe", "777-333-7654", "jane_doe@wgu.edu"));
                }
            }
        });
    }

    public void insertMentor(MentorEntity newMentor) {
        mDb.mentorDao().insertMentor(newMentor);
    }
}
