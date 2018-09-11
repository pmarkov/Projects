package com.markov.todolist.datamodel;

import java.io.Serializable;
import java.time.LocalDate;

public class TodoItem implements Serializable {

    private String shortDescription;
    private String details;
    private LocalDate deadLine;

    private long serialVersionUID = 1;

    public TodoItem(String shortDescription, String details, LocalDate deadLine) {
        this.shortDescription = shortDescription;
        this.details = details;
        this.deadLine = deadLine;
    }

    public String getShortDescription() {
        return shortDescription;
    }


    public String getDetails() {
        return details;
    }

    public LocalDate getDeadLine() {
        return deadLine;
    }

}
