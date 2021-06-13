package com.ribber.ideaNote.extensions;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.vcsUtil.VcsFileUtil;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class LinkCompletionContributor extends CompletionContributor {
    public LinkCompletionContributor() {
        extend(CompletionType.BASIC, psiElement().afterLeaf("[").beforeLeaf("]"),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        VirtualFile currentFile = parameters.getOriginalFile().getVirtualFile();
                        String key = parameters.getPosition().getText().replace("IntellijIdeaRulezzz","");
                        Project current = parameters.getPosition().getProject();
                        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(MarkdownFileType.INSTANCE, GlobalSearchScope.projectScope(current));
                        LinkInsertHandler<LookupElement> linkInsertHandler = new LinkInsertHandler<>();
                        for(VirtualFile virtualFile : virtualFiles) {
                            if(virtualFile.getName().toLowerCase().contains(key.toLowerCase())) {
                                String ext = virtualFile.getExtension();
                                String name = virtualFile.getName().replace("." + ext, "");
                                String relativePath = VcsFileUtil.relativePath(currentFile, virtualFile).replaceFirst("../", "");
                                resultSet.addElement(LookupElementBuilder.create(name, relativePath).withInsertHandler(linkInsertHandler).withCaseSensitivity(false));
                            }

                        }

                    }
                }
        );
    }
}

class LinkInsertHandler<T> implements InsertHandler {

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        context.getDocument().replaceString(
                context.getStartOffset() - 1,
                context.getTailOffset() + 1,
                "[" + item.getObject().toString() + "](" + item.getLookupString() + ")");
    }
}
