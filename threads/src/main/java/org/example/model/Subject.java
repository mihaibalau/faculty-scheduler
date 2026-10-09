package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Model class representing a subject/course.
 */
public class Subject {
    private String name;

    @JsonProperty("MainTeacher")
    private String mainTeacher;

    @JsonProperty("Language")
    private String language;

    @JsonProperty("CoursesPerWeek")
    private int coursesPerWeek;

    @JsonProperty("CourseLenght")
    private int courseLength;

    @JsonProperty("SeminarsPerWeek")
    private int seminarsPerWeek;

    @JsonProperty("SeminarLenght")
    private int seminarLength;

    @JsonProperty("LaboratoriesPerWeek")
    private double laboratoriesPerWeek;

    @JsonProperty("LaboratoriesLenght")
    private int laboratoriesLength;

    public Subject() {}

    public Subject(String name, String mainTeacher, String language,
                   int coursesPerWeek, int courseLength,
                   int seminarsPerWeek, int seminarLength,
                   double laboratoriesPerWeek, int laboratoriesLength) {
        this.name = name;
        this.mainTeacher = mainTeacher;
        this.language = language;
        this.coursesPerWeek = coursesPerWeek;
        this.courseLength = courseLength;
        this.seminarsPerWeek = seminarsPerWeek;
        this.seminarLength = seminarLength;
        this.laboratoriesPerWeek = laboratoriesPerWeek;
        this.laboratoriesLength = laboratoriesLength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainTeacher() {
        return mainTeacher;
    }

    public void setMainTeacher(String mainTeacher) {
        this.mainTeacher = mainTeacher;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getCoursesPerWeek() {
        return coursesPerWeek;
    }

    public void setCoursesPerWeek(int coursesPerWeek) {
        this.coursesPerWeek = coursesPerWeek;
    }

    public int getCourseLength() {
        return courseLength;
    }

    public void setCourseLength(int courseLength) {
        this.courseLength = courseLength;
    }

    public int getSeminarsPerWeek() {
        return seminarsPerWeek;
    }

    public void setSeminarsPerWeek(int seminarsPerWeek) {
        this.seminarsPerWeek = seminarsPerWeek;
    }

    public int getSeminarLength() {
        return seminarLength;
    }

    public void setSeminarLength(int seminarLength) {
        this.seminarLength = seminarLength;
    }

    public double getLaboratoriesPerWeek() {
        return laboratoriesPerWeek;
    }

    public void setLaboratoriesPerWeek(double laboratoriesPerWeek) {
        this.laboratoriesPerWeek = laboratoriesPerWeek;
    }

    public int getLaboratoriesLength() {
        return laboratoriesLength;
    }

    public void setLaboratoriesLength(int laboratoriesLength) {
        this.laboratoriesLength = laboratoriesLength;
    }

    public int getTotalHoursPerWeek() {
        return (coursesPerWeek * courseLength) +
               (seminarsPerWeek * seminarLength) +
               (int) (laboratoriesPerWeek * laboratoriesLength);
    }

    public int getCourseHours() {
        return coursesPerWeek * courseLength;
    }

    public int getSeminarHours() {
        return seminarsPerWeek * seminarLength;
    }

    public int getLaboratoryHours() {
        return (int) Math.ceil(laboratoriesPerWeek * laboratoriesLength);
    }

    public int getTotalHours() {
        return getTotalHoursPerWeek();
    }

    @Override
    public String toString() {
        return "Subject{" +
                "name='" + name + '\'' +
                ", mainTeacher='" + mainTeacher + '\'' +
                ", language='" + language + '\'' +
                ", coursesPerWeek=" + coursesPerWeek +
                ", courseLength=" + courseLength +
                ", seminarsPerWeek=" + seminarsPerWeek +
                ", seminarLength=" + seminarLength +
                ", laboratoriesPerWeek=" + laboratoriesPerWeek +
                ", laboratoriesLength=" + laboratoriesLength +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return Objects.equals(name, subject.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}