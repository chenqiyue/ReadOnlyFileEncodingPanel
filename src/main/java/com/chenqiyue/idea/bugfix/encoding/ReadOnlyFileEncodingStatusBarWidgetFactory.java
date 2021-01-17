package com.chenqiyue.idea.bugfix.encoding;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author cqy
 * @date 2020/01/19
 */
public class ReadOnlyFileEncodingStatusBarWidgetFactory extends StatusBarEditorBasedWidgetFactory {

    @Override
    public @NotNull
    String getId() {
        return "ReadOnly_" + StatusBar.StandardWidgets.ENCODING_PANEL;
    }

    @Override
    public @Nls
    @NotNull
    String getDisplayName() {
        return getId();
    }

    @Override
    public @NotNull
    StatusBarWidget createWidget(@NotNull Project project) {
        return new ReadOnlyFileEncodingPanel(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }
}
