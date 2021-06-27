package com.ribber.ideaNote.actions;

import com.intellij.codeInsight.editorActions.TextBlockTransferable;
import com.intellij.ide.PasteProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.usages.impl.GroupNode;
import com.intellij.usages.impl.rules.FileGroupingRule;
import com.intellij.vcsUtil.VcsFileUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class AddMarkdownLinkAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        if(project == null) return;
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if(editor == null) return;
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if(currentFile == null || !currentFile.getExtension().equals("md")) return;
        int currentCaret = editor.getCaretModel().getCurrentCaret().getOffset();

        JTree tree = (JTree)anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if(tree == null) return;
        StringBuffer insertContent = new StringBuffer();
        TreePath[] paths = tree.getSelectionPaths();
        if(paths == null || paths.length < 1) return;
        for(TreePath path : paths) {
            TreeNode node = (TreeNode)path.getLastPathComponent();
            if(! (node instanceof GroupNode)) continue;
            GroupNode groupNode = (GroupNode)node;
            if(! (groupNode.getUserObject() instanceof FileGroupingRule.FileUsageGroup)) continue;
            FileGroupingRule.FileUsageGroup fileGroupingRule = (FileGroupingRule.FileUsageGroup) groupNode.getUserObject();
            VirtualFile selectedFile = fileGroupingRule.getPsiFile().getVirtualFile();
            String relativePath = VcsFileUtil.relativePath(currentFile, selectedFile)
                    .replaceFirst("../", "").replaceAll(" ", "%20");
            insertContent.append("[" + selectedFile.getNameWithoutExtension() + "](" + relativePath + ")\n");
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().insertString(currentCaret, insertContent.toString());
        });

    }
}
