/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ca.footeware.javafx.squeaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Footeware.ca
 */
class PlayList {

    private String name;
    private ObservableList<Audio> items;

    public PlayList(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObservableList<Audio> getItems() {
        if (items == null) {
            items = FXCollections.observableArrayList();
        }
        return items;
    }

    public void setItems(ObservableList<Audio> files) {
        this.items = files;
    }
}
