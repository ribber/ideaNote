package com.ribber.ideaNote.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.vcsUtil.VcsFileUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FindUnlinkFileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getData(CommonDataKeys.PROJECT);
        VirtualFile root = ProjectRootManager.getInstance(currentProject).getContentRoots()[0];
        List<VirtualFile> unlinkFileList = new ArrayList<>();
        VfsUtilCore.iterateChildrenRecursively(root, file -> {
            if(file.getName().startsWith(".")) return false;
            return true;
        }, file -> {
            if(file.isDirectory()) return true;
            PsiFile psiFile = PsiManager.getInstance(currentProject).findFile(file);
            if(psiFile == null) return true;
            Collection<PsiReference> fileReferences = ReferencesSearch.search(psiFile, GlobalSearchScope.projectScope(currentProject)).findAll();
            if(fileReferences.size() == 0) {
                unlinkFileList.add(file);
            } else if(fileReferences.size() == 1 && fileReferences.stream().findFirst().get().getElement().getContainingFile().getName().equals("unlinkFileReport.md")) {
                unlinkFileList.add(file);
            }
            return true;
        });
        WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
            try {
                VirtualFile reportFile = root.createChildData(new Object(), "unlinkFileReport.md");
                StringBuffer sb = new StringBuffer();
                for(VirtualFile unlinkFile : unlinkFileList) {
                    String relativePath = VcsFileUtil.relativePath(reportFile, unlinkFile).replaceFirst("../", "");
                    sb.append("[" + unlinkFile.getName() + "](" + relativePath + ")\n");
                }
                reportFile.setBinaryContent(sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
