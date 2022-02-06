package com.example.pmfstevidencija;

import java.util.Date;
import java.util.List;

public class Schedule {

    private List<String> attendees;
    private Date startTime;
    private Date endTime;

    public Schedule(){}

    public Schedule(List<String> attendees, Date startTime,Date endTime) {
        this.attendees = attendees;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public Date getStartTime() {
        return startTime;
    }


    public Date getEndTime() {
        return endTime;
    }

    public List<String> getAttendees() {
        return attendees;
    }
}
