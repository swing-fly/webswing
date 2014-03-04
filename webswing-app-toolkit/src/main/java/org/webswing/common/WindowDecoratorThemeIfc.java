package org.webswing.common;

import java.awt.AWTEvent;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;

public interface WindowDecoratorThemeIfc {

    
    public static final String DECORATION_THEME_IMPL_PROP = "org.webswing.window.theme.impl";

    public static final String DECORATION_THEME_IMPL_DEFAULT = "org.webswing.theme.DefaultWindowDecoratorTheme";

    Insets getInsets();

    Image getWindowDecoration(Object window, int w, int h);

    Image repaintBackground(Rectangle toRepaint);

    WindowActionType getAction(Window w, MouseEvent e);

}
