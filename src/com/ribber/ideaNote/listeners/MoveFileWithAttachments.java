package com.ribber.ideaNote.listeners;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveFileWithAttachments implements BulkFileListener {
    Pattern attachmentRefPattern = Pattern.compile("\\[.*\\]\\((.*)\\)");

    @Override
    public void before(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent vFileEvent : events) {
            if (!(vFileEvent instanceof VFileMoveEvent)) continue;
            VFileMoveEvent vFileMoveEvent = (VFileMoveEvent) vFileEvent;
            VirtualFile virtualFile = vFileMoveEvent.getFile();
            if (virtualFile == null) continue;
            String newPath = vFileMoveEvent.getNewPath();
            if (!newPath.endsWith(".md")) continue;
            try {
                String fileContent = new String(virtualFile.contentsToByteArray());
                Matcher m = attachmentRefPattern.matcher(fileContent);
                while (m.find()) {
                    String referencePath = m.group(1);
                    if (referencePath.endsWith(".md")
                            || referencePath.indexOf("://") > 0
                            || referencePath.startsWith("/")
                            || referencePath.startsWith("../")) continue;
                    String[] pathFragment = referencePath.split("/");
                    if (pathFragment.length != 2) continue;
                    referencePath = "../" + referencePath;
                    VirtualFile attachmentFile = virtualFile.findFileByRelativePath(referencePath);
                    if (attachmentFile == null) {
                        throw new IOException("can't find attachment");
                    }
                    VirtualFile newParent = vFileMoveEvent.getNewParent();
                    VirtualFile newAttachDir = newParent.findChild(pathFragment[0]);
                    if (newAttachDir == null || !newAttachDir.exists()) {
                        newAttachDir = newParent.createChildDirectory(vFileEvent.getRequestor(), pathFragment[0]);
                    }

                    attachmentFile.move(vFileEvent.getRequestor(), newAttachDir);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
