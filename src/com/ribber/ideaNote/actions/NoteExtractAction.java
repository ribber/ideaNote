package com.ribber.ideaNote.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.charset.StandardCharsets;

public class NoteExtractAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
            try {
                Editor editor = e.getData(CommonDataKeys.EDITOR);
                if(editor == null) return;
                SelectionModel selectionModel = editor.getSelectionModel();
                String selectedText = selectionModel.getSelectedText();
                String firstLine = selectedText.substring(0, selectedText.indexOf("\n"));
                String filename = firstLine
                        .replaceAll("#", "")
                        .replaceAll("\\*", "")
                        .trim()
                        .replaceAll(" ", "_");
                editor.getDocument().replaceString(
                        selectionModel.getSelectionStart(),
                        selectionModel.getSelectionEnd(),
                        "[" + filename + "](" + filename + ".md)");
                VirtualFile currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
                VirtualFile newFile = currentFile.getParent().createChildData(new Object(), filename + ".md");
                newFile.setBinaryContent(selectedText.getBytes(StandardCharsets.UTF_8));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });


    }
}
