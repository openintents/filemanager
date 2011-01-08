package org.openintents.filemanager;

import java.util.ArrayList;
import java.util.List;

class DirectoryContents {
    List<IconifiedText> listDir;
    List<IconifiedText> listFile;
    List<IconifiedText> listSdCard;
    
    // If true, there's a ".nomedia" file in this directory.
    boolean noMedia;
}
