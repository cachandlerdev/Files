package com.candle.fileexplorer.model.data;

import com.candle.fileexplorer.model.helpers.FileOperations;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;

/**
 * Information about a directory item such as a drive, folder, or file.
 */
public class DefaultFileItem implements FileItem {
    //region Private Members

    /**
     * The type of item contained in this directory.
     */
    private final FileType fileType;

    /**
     * The file object stored for internal use.
     */
    private final File file;

    //endregion

    //region Constructor

    /**
     * Creates a new file item with the specified data values.
     *
     * @param fileType The type of file item being created, be it a drive,
     *                 folder, or file.
     * @param path     The path to the file.
     */
    public DefaultFileItem(FileType fileType, String path) {
        file = new File(FileOperations.sanitizePath(path));
        this.fileType = fileType;
    }

    /**
     * Creates a new file item and automatically determines if the given item
     * returns a folder or a file.
     * NOTE: This function should not be used for drives, and will automatically
     * set the type to a file if the item does not currently exist on disk.
     *
     * @param path The path to the file.
     */
    public DefaultFileItem(String path) {
        String cleanPath = FileOperations.sanitizePath(path);
        file = new File(cleanPath);
        fileType = FileOperations.determineType(cleanPath);
    }

    //endregion

    //region Public Methods

    @Override
    public void moveTo(String targetPath) throws FileSystemException {
        File targetDestination = new File(targetPath + "/" + file.getName());
        if (file.getAbsolutePath().equals(targetDestination.getAbsolutePath())) {
            throw new IllegalStateException();
        }

        switch (fileType) {
            case File -> {
                try {
                    FileUtils.moveFile(file, targetDestination);
                }catch (FileSystemException e) {
                    throw e;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            case Folder -> {
                try {
                    FileUtils.moveDirectory(file, targetDestination);
                }catch (FileSystemException e) {
                    throw e;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public void copyTo(String targetPath) throws FileSystemException {
        File targetDestination = new File(targetPath + "/" + file.getName());
        if (file.getAbsolutePath().equals(targetDestination.getAbsolutePath())) {
            throw new IllegalStateException();
        }

        switch (fileType) {
            case File -> {
                try {
                    FileUtils.copyFile(file, targetDestination);
                } catch (FileSystemException e) {
                    throw e;
                } catch (FileNotFoundException ex) {
                    // Gets called if the user tries to copy a file from the
                    // clipboard that has been deleted
                    // (i.e. file was deleted, but it's still sitting on the
                    // clipboard).
                    // It doesn't exist anymore, so there's no point in doing
                    // anything.
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }
            }
            case Folder -> {
                try {
                    FileUtils.copyDirectory(file, targetDestination);
                } catch (FileSystemException e) {
                    throw e;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public boolean writeToDisk() {
        switch (fileType) {
            case File -> {
                try {
                    return file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case Folder -> {
                return file.mkdir();
            }
        }
        return false;
    }

    @Override
    public boolean rename(String name) {
        if (fileType != FileType.Drive) {
            try {
                Files.move(file.toPath(), file.toPath().resolveSibling(name));
                return true;
            } catch (FileAlreadyExistsException e) {
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else
            return false;
    }

    @Override
    public boolean sendToTrash() {
        if (fileType != FileType.Drive) {
            return FileOperations.sendItemToTrash(file.getAbsolutePath());
        } else
            return false;
    }

    @Override
    public String getFileName() {
        // File.getName() doesn't work for drives.
        if (fileType.equals(FileType.Drive)) {
            return file.getPath();
        }

        return file.getName();
    }

    @Override
    public String getItemDirectory() {
        return file.getAbsolutePath();
    }

    @Override
    public FileType getFileType() {
        return fileType;
    }

    @Override
    public boolean getIsHiddenFile() {
        return file.isHidden();
    }

    @Override
    public long getLastModifiedTime() {
        return file.lastModified();
    }

    /**
     * The objects should be equal if the file and type match.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultFileItem otherItem) {
            boolean fileMatch = file.equals(otherItem.file);
            boolean typeMatch = fileType.equals(otherItem.fileType);
            return (fileMatch && typeMatch);
        }
        return false;
    }

    //endregion
}
