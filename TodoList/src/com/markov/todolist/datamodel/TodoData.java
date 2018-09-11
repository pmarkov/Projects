package com.markov.todolist.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;

public class TodoData {

    private static TodoData instance = new TodoData();
    private static String filename = "TodoItems.dat";

    private ObservableList<TodoItem> todoItems;

    public static TodoData getInstance() {
        return instance;
    }

    private TodoData() {

    }

    public ObservableList<TodoItem> getTodoItems() {
        return todoItems;
    }

    public void addTodoItem(TodoItem item) {
        todoItems.add(item);
    }

    public void loadTodoItems() throws IOException {

        todoItems = FXCollections.observableArrayList();

        try (ObjectInputStream inputStream = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(filename)))) {

            while(true) {

                try {
                    TodoItem item = (TodoItem) inputStream.readObject();
                    todoItems.add(item);

                } catch (EOFException e) {
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());
        }
    }

    public void saveTodoItems() throws IOException {

        try (ObjectOutputStream outputStream = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filename)))) {

            for(TodoItem item : todoItems) {
                outputStream.writeObject(item);
            }
        }
    }

    public void deleteTodoItem(TodoItem item) {
        todoItems.remove(item);
    }

}
