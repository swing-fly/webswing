package org.webswing.toolkit.extra;

import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.webswing.dispatch.WebPaintDispatcher;
import org.webswing.toolkit.WebToolkit;
import org.webswing.toolkit.WebWindowPeer;
import org.webswing.util.Util;

public class WindowManager {

    private static WindowManager singleton = null;

    LinkedList<Window> zorder = new LinkedList<Window>();
    Window activeWindow = null;

    private WindowManager() {
    }

    public static WindowManager getInstance() {
        if (singleton == null) {
            singleton = new WindowManager();
        }
        return singleton;
    }

    public void bringToFront(Window w) {
        synchronized (WebPaintDispatcher.webPaintLock) {
            zorder.remove(w);
            if (w.isAlwaysOnTop()) {
                zorder.addFirst(w);
            } else {
                Window firstNotAlwaysOnTopWindow = null;
                for (Window z : zorder) {
                    if (!z.isAlwaysOnTop()) {
                        firstNotAlwaysOnTopWindow = z;
                    }
                }
                if (firstNotAlwaysOnTopWindow != null) {
                    zorder.add(zorder.indexOf(firstNotAlwaysOnTopWindow), w);
                } else {
                    zorder.addFirst(w);
                }
            }
            w.repaint();
        }
    }

    public void removeWindow(Window target) {
        synchronized (WebPaintDispatcher.webPaintLock) {
            if (zorder.contains(target)) {
                int index = zorder.indexOf(target);
                Rectangle bounds = target.getBounds();
                zorder.remove(target);
                requestRepaintUnderlyingAfterWindowClosed(index, bounds);
            }
        }
    }

    public void bringToBack(Window w) {
        synchronized (WebPaintDispatcher.webPaintLock) {
            w.setAlwaysOnTop(false);
            removeWindow(w);
            zorder.add(w);
            w.repaint();
        }
    }

    public Window getActiveWindow() {
        return activeWindow;
    }

    public boolean isWindowActive(Window w) {
        if (activeWindow == w) {
            return true;
        } else {
            return false;
        }
    }

    public void activateWindow(Window w) {
        bringToFront(w);
        this.activeWindow = w;
    }

    public Window getVisibleWindowOnPosition(int x, int y) {
        for (Window w : zorder) {
            if (SwingUtilities.isRectangleContainingRectangle(w.getBounds(), new Rectangle(x, y, 0, 0))) {
                return w;
            }
        }
        return null;
    }

    public void extractNonVisibleAreas(Map<String, List<Rectangle>> map) {
        if (zorder.size() > 0) {
            Rectangle previous = zorder.get(0).getBounds();
            SwingUtilities.convertPointToScreen(previous.getLocation(),zorder.get(0));
            List<Rectangle> previousDifferences = new ArrayList<Rectangle>();
            for (int i = 1; i < zorder.size(); i++) {
                String id = ((WebWindowPeer) WebToolkit.targetToPeer(zorder.get(i))).getGuid();
                Rectangle current = zorder.get(i).getBounds();
                SwingUtilities.convertPointToScreen(current.getLocation(),zorder.get(i));
                List<Rectangle> currentDifferences = new ArrayList<Rectangle>();
                for (Rectangle diff : previousDifferences) {
                    Rectangle intersect = SwingUtilities.computeIntersection(current.x, current.y, current.width, current.height, diff);
                    if (!intersect.isEmpty()) {
                        currentDifferences.add(intersect);
                    }
                }
                Rectangle intersect = SwingUtilities.computeIntersection(current.x, current.y, current.width, current.height, previous);
                if (!intersect.isEmpty()) {
                    currentDifferences.add(intersect);
                }

                previous = current;
                previousDifferences = currentDifferences;
                if (previousDifferences.size() != 0) {
                    map.put(id, previousDifferences);
                }
            }
        }
    }

    private void requestRepaintUnderlyingAfterWindowClosed(int index, Rectangle bounds) {
        for (int i = index; i < zorder.size(); i++) {
            Window underlying = zorder.get(i);
            Rectangle uBounds = underlying.getBounds();
            SwingUtilities.computeIntersection(uBounds.x, uBounds.y, uBounds.width, uBounds.height, bounds);
            underlying.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
        }

    }

}
