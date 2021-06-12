package com.ribber.ideaNote.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.intellij.plugins.markdown.lang.MarkdownFileType;

import java.util.Collection;
import java.util.Random;

public class RandomOpenNoteAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getData(CommonDataKeys.PROJECT);
        Collection<VirtualFile> virtualFileCollection = FileTypeIndex.getFiles(MarkdownFileType.INSTANCE, GlobalSearchScope.projectScope(currentProject));
        VirtualFile[] virtualFiles = new VirtualFile[virtualFileCollection.size()];
        virtualFileCollection.toArray(virtualFiles);
        int i = new Random().nextInt(virtualFiles.length);
        FileEditorManager.getInstance(currentProject).openFile(virtualFiles[i], true);
    }
}
