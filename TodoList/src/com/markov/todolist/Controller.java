package com.markov.todolist;

import com.markov.todolist.datamodel.TodoData;
import com.markov.todolist.datamodel.TodoItem;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private TextArea itemDetailsTextArea;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodaysItems;

    public void initialize() {

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(event -> {
            TodoItem item = todoListView.getSelectionModel().getSelectedItem();
            deleteItem(item);
        });

        MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(event -> handleUpdate());

        listContextMenu = new ContextMenu();
        listContextMenu.getItems().addAll(updateMenuItem, deleteMenuItem);

        todoListView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) -> {
                    if(newValue != null) {
                        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                        itemDetailsTextArea.setText(item.getDetails());
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM, yyyy");
                        deadlineLabel.setText(dtf.format(item.getDeadLine()));
                    }
        });

        wantAllItems = item -> true;
        wantTodaysItems = item -> item.getDeadLine().equals(LocalDate.now());

        filteredList = new FilteredList<>(TodoData.getInstance().getTodoItems(), wantAllItems);

        SortedList<TodoItem> sortedList = new SortedList<>(filteredList, Comparator.comparing(TodoItem::getDeadLine));

        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> param) {
                ListCell<TodoItem> cell = new ListCell<TodoItem>() {
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty) {
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if(item.getDeadLine().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if(item.getDeadLine().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.BROWN);
                            } else {
                                setTextFill(Color.BLACK);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(((observable, wasEmpty, isNowEmpty) -> {
                    if(isNowEmpty) {
                        cell.setContextMenu(null);
                    } else {
                        cell.setContextMenu(listContextMenu);
                    }
                }));

                return cell;
            }
        });
    }

    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Todo Item");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        while(true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if(result.isPresent()) {
                if(result.get() == ButtonType.OK) {
                    DialogController controller = loader.getController();
                    TodoItem newItem = controller.processResults();
                    if(newItem != null) {
                        todoListView.getSelectionModel().select(newItem);
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    @FXML
    public void handleDeleteKey(KeyEvent keyEvent) {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();

        if(item != null && keyEvent.getCode().equals(KeyCode.DELETE)) {
            deleteItem(item);
        }
    }

    @FXML
    public void handleFilterButton() {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();

        if(filterToggleButton.isSelected()) {
            filteredList.setPredicate(wantTodaysItems);
            if(filteredList.isEmpty()) {
                itemDetailsTextArea.clear();
                deadlineLabel.setText("");
            } else if(filteredList.contains(item)) {
                todoListView.getSelectionModel().select(item);
            } else {
                todoListView.getSelectionModel().selectFirst();
            }
        } else {
            if(filteredList.isEmpty()) {
                filteredList.setPredicate(wantAllItems);
                todoListView.getSelectionModel().selectFirst();
            } else {
                filteredList.setPredicate(wantAllItems);
                todoListView.getSelectionModel().select(item);
            }
        }
    }

    @FXML
    public void handleUpdate() {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();

        if(item != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(mainBorderPane.getScene().getWindow());
            dialog.setTitle("Update Todo Item");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("todoItemDialog.fxml"));
            try {
                dialog.getDialogPane().setContent(loader.load());
            } catch (IOException e) {
                System.out.println("Couldn't load the dialog");
                e.printStackTrace();
            }

            DialogController controller = loader.getController();
            controller.populateFields(item.getShortDescription(), item.getDetails(), item.getDeadLine());

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            while(true) {
                Optional<ButtonType> result = dialog.showAndWait();
                if(result.isPresent()) {
                    if(result.get() == ButtonType.OK) {
                        TodoItem updatedItem = controller.processResults();
                        if(updatedItem != null) {
                            TodoData.getInstance().deleteTodoItem(item);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }

        }
    }

    @FXML
    public void handleDelete() {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        if(item != null) {
            deleteItem(item);
        }
    }

    private void deleteItem(TodoItem item) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm or Cancel to back out");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            TodoData.getInstance().deleteTodoItem(item);
        }

        if(todoListView.getItems().isEmpty()) {
            itemDetailsTextArea.clear();
            deadlineLabel.setText("");
        }
    }

}
