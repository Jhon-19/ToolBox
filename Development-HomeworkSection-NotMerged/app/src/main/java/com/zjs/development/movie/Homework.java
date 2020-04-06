package com.zjs.development.movie;

import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.GregorianCalendar;

/**
 * Contains the information of a piece of homework.
 *
 * @author Zhou Jingsen
 * */
public class Homework implements Serializable {
    public String subjectName;
    public GregorianCalendar deadline;
    public String description;
    public boolean completed;
    public GregorianCalendar completeTime;
    @Override
    @NonNull
    public String toString() {
        return "["+subjectName+"][deadline:"+deadline.toString()+"]"+description;
    }
}
