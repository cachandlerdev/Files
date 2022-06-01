package com.candle.fileexplorer.viewmodel;

import com.candle.fileexplorer.model.FilesModel;
import com.candle.fileexplorer.model.data.FileItem;
import com.candle.fileexplorer.model.helpers.DirectoryStructure;
import com.candle.fileexplorer.model.observer.DataListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.util.ArrayList;

public class FileGridViewModel implements DataListener
{
    //region Private Members

    /**
     * The data model containing information about the explorer's current directory.
     */
    FilesModel dataModel;

    /**
     * A list of file item view models to be exposed to the UI.
     */
    private ObservableList<FileItemViewModel> items;

    /**
     * A boolean variable that is used to determine whether hidden files/folders should be displayed.
     */
    private boolean showHiddenItems = false;

    //endregion

    //region Constructor

    /**
     * Initializes the view model and sets the current directory to the user's home folder.
     */
    public FileGridViewModel(FilesModel dataModel)
    {
        this.dataModel = dataModel;
        dataModel.addListener(this);
        currentDirectoryChanged();

        updateContents();
    }

    //endregion

    //region Public Methods

    /**
     * Updates the file items in the view model using the new current directory value.
     */
    public void updateContents()
    {
        // Lazy instantiation
        if (items == null)
            items = new SimpleListProperty<>();
        else
            items.removeAll();

        // Get the files/folders.
        ArrayList<FileItem> children = DirectoryStructure.getDirectoryContents(dataModel.getCurrentDirectory(), showHiddenItems);

        // Create the view models from the data.
        ArrayList<FileItemViewModel> models = new ArrayList<>();
        if (children != null)
            children.forEach((fileItem) -> models.add(new FileItemViewModel(fileItem)));

        // Turn the data into an observable array list for the view to access.

        items = FXCollections.observableArrayList(models);

    }

    public ObservableList<FileItemViewModel> getItems()
    {
        return items;
    }

    /**
     * Updates the data model's current directory with the new path.
     */
    public void setCurrentDirectory(String newDirectory)
    {
        dataModel.setCurrentDirectory(newDirectory);
    }

    public boolean getShowHiddenItems()
    {
        return showHiddenItems;
    }

    public void setShowHiddenItems(boolean value)
    {
        this.showHiddenItems = value;
    }

    @Override
    public void currentDirectoryChanged()
    {
        updateContents();
    }

    //endregion
}