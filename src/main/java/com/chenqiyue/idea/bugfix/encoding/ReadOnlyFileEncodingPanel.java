package com.chenqiyue.idea.bugfix.encoding;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.encoding.*;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EncodingPanel;
import com.intellij.util.ObjectUtils;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author cqy
 * @date 2020/01/18
 */
public class ReadOnlyFileEncodingPanel extends EncodingPanel {

    public ReadOnlyFileEncodingPanel(@NotNull Project project, @NotNull CoroutineScope scope) {
        super(project, scope);
    }

    @NotNull
    @Override
    protected WidgetState getWidgetState(@Nullable VirtualFile file) {
        if (file == null) {
            return WidgetState.HIDDEN;
        }

        Pair<Charset, String> check = EncodingUtil.getCharsetAndTheReasonTooltip(file);
        String failReason = Pair.getSecond(check);
        Charset charset = ObjectUtils.notNull(Pair.getFirst(check), file.getCharset());
        String charsetName = ObjectUtils.notNull(charset.displayName(), "n/a");
        String toolTipText = failReason == null ? "ReadOnly File Encoding: " + charsetName : StringUtil.capitalize(failReason) + ".";
        return new WidgetState(toolTipText, charsetName, failReason == null);
    }

    @Nullable
    @Override
    protected ListPopup createPopup(DataContext context) {
        ChangeFileEncodingAction action = new ChangeFileEncodingAction() {
            @Override
            protected boolean chosen(Document document, Editor editor,
                                     @Nullable VirtualFile virtualFile,
                                     byte[] bytes, @NotNull Charset charset) {
                if (virtualFile == null) { return false; }

                Project project = getProject();
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    virtualFile.setCharset(charset);
                    CharSequence content = LoadTextUtil.getTextByBinaryPresentation(bytes, charset);
                    document.setReadOnly(false);
                    ((DocumentEx)document).replaceText(content, virtualFile.getModificationStamp());

                    forceUpdateProjectEncodingHistory(project, virtualFile, charset);
                });
                return true;
            }
        };
        action.getTemplatePresentation().setText("ReadOnly File Encoding");
        return action.createPopup(context, null);
    }

    @SuppressWarnings("unchecked")
    private void forceUpdateProjectEncodingHistory(Project project, @NotNull VirtualFile virtualFile,
                                                   @NotNull Charset charset) {
        try {
            EncodingProjectManagerImpl epm = (EncodingProjectManagerImpl)EncodingProjectManager.getInstance(project);
            Field myMappingField = EncodingProjectManagerImpl.class.getDeclaredField("myMapping");
            myMappingField.setAccessible(true);
            Map<VirtualFilePointer, Charset> myMapping = (Map<VirtualFilePointer, Charset>)myMappingField.get(epm);
            VirtualFilePointer pointer = VirtualFilePointerManager.getInstance().create(virtualFile, epm, null);
            myMapping.put(pointer, charset);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    @NotNull
    @Override
    protected StatusBarWidget createInstance(@NotNull Project project) {
        return new ReadOnlyFileEncodingPanel(project, getScope());
    }

    @Override
    @NotNull
    public String ID() {
        return "ReadOnly_" + StatusBar.StandardWidgets.ENCODING_PANEL;
    }

    @Override
    protected boolean isEnabledForFile(@Nullable VirtualFile file) {
        return file != null && !file.isWritable();
    }
}
