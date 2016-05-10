package org.openintents.filemanager.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class BaseTestFileManager {
    public static final String TEST_DIRECTORY = "oi-filemanager-tests";
    protected static String sdcardPath;

    protected static void cleanDirectory(File file) {
        if(!file.exists()) return;
        for(String name:file.list()) {
            if(!name.startsWith("oi-") && !name.startsWith(".oi-")) {
                throw new RuntimeException(file + " contains unexpected file");
            }
            File child = new File(file, name);
            if(child.isDirectory())
                cleanDirectory(child);
            else
                child.delete();
        }
        file.delete();
        if(file.exists()) {
            throw new RuntimeException("Deletion of " + file + " failed");
        }
    }

    protected void createFile(String path, String content) throws IOException {
        File file = new File(path);
        OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        wr.write(content);
        wr.close();
    }

    protected static void createDirectory(String path) throws IOException {
        File file = new File(path);
        file.mkdir();
        if(!file.exists())
            throw new IOException("Creation of " + path + " failed");
    }

    protected void deleteDirectory(String path) {
        File file = new File(path);
        if(file.exists())
            if(file.isDirectory())
                cleanDirectory(file);
            file.delete();
    }
}
