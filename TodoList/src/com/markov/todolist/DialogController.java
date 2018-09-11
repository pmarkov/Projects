package com.markov.todolist;

import com.markov.todolist.datamodel.TodoData;
import com.markov.todolist.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField descriptionField;

    @FXML
    private TextArea detailsArea;

    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label datePickerLabel;

    public TodoItem processResults() {
        boolean correct = true;
        descriptionLabel.setVisible(false);
        datePickerLabel.setVisible(false);

        String shortDescription = descriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadline = deadlinePicker.getValue();

        if(shortDescription.isEmpty()) {
            correct = false;
            descriptionLabel.setText("Please enter description!");
            descriptionLabel.setVisible(true);
        }

        if(deadline == null) {
            correct = false;
            datePickerLabel.setText("Please choose deadline!");
            datePickerLabel.setVisible(true);
        }

        if(correct) {
            TodoItem item = new TodoItem(shortDescription, details, deadline);
            TodoData.getInstance().addTodoItem(item);
            return item;
        }

        return null;
    }

    public void populateFields(String shortDescription, String details, LocalDate deadline) {
        descriptionField.setText(shortDescription);
        detailsArea.setText(details);
        deadlinePicker.setValue(deadline);
    }

}
