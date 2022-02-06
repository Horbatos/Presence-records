package com.example.pmfstevidencija;

import java.io.Serializable;
import java.util.List;

public class UserInfo implements Serializable {

    private String name;
    private String email;
    private boolean isProfessor;
    private List<String> courses;

    public UserInfo(){}

    public UserInfo(List<String> courses, String email, boolean isProfessor, String name) {
        this.name = name;
        this.email = email;
        this.isProfessor = isProfessor;
        this.courses = courses;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean getIsProfessor() {
        return isProfessor;
    }


    public List<String> getCourses() {
        return courses;
    }

}
