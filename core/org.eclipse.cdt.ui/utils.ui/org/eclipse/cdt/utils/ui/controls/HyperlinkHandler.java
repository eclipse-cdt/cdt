package org.eclipse.cdt.utils.ui.controls;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.util.*;
import org.eclipse.swt.*;

public class HyperlinkHandler implements MouseListener, MouseTrackListener, PaintListener {
	public static final int UNDERLINE_NEVER = 1;
	public static final int UNDERLINE_ROLLOVER = 2;
	public static final int UNDERLINE_ALWAYS = 3;

	private Cursor hyperlinkCursor;
	private Cursor busyCursor;
	private boolean hyperlinkCursorUsed=true;
	private int hyperlinkUnderlineMode=UNDERLINE_ALWAYS;
	private Color background;
	private Color foreground;
	private Color activeBackground;
	private Color activeForeground;
	private Hashtable hyperlinkListeners;
	private Control lastLink;

public HyperlinkHandler() {
	hyperlinkListeners = new Hashtable();
	hyperlinkCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
	busyCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT);
}
public void dispose() {
	hyperlinkCursor.dispose();
	busyCursor.dispose();
}
public org.eclipse.swt.graphics.Color getActiveBackground() {
	return activeBackground;
}
public org.eclipse.swt.graphics.Color getActiveForeground() {
	return activeForeground;
}
public org.eclipse.swt.graphics.Color getBackground() {
	return background;
}
public org.eclipse.swt.graphics.Cursor getBusyCursor() {
	return busyCursor;
}
public org.eclipse.swt.graphics.Color getForeground() {
	return foreground;
}
public org.eclipse.swt.graphics.Cursor getHyperlinkCursor() {
	return hyperlinkCursor;
}
public int getHyperlinkUnderlineMode() {
	return hyperlinkUnderlineMode;
}
public org.eclipse.swt.widgets.Control getLastLink() {
	return lastLink;
}
public boolean isHyperlinkCursorUsed() {
	return hyperlinkCursorUsed;
}
		public void mouseDoubleClick(MouseEvent e) {
		}
public void mouseDown(MouseEvent e) {
	if (e.button == 1)
		return;
	lastLink = (Control)e.widget;
}
public void mouseEnter(MouseEvent e) {
	Control control = (Control) e.widget;
	if (isHyperlinkCursorUsed())
		control.setCursor(hyperlinkCursor);
	if (activeBackground != null)
		control.setBackground(activeBackground);
	if (activeForeground != null)
		control.setForeground(activeForeground);
	if (hyperlinkUnderlineMode==UNDERLINE_ROLLOVER) underline(control, true);    

	IHyperlinkListener action =
		(IHyperlinkListener) hyperlinkListeners.get(control);
	if (action != null)
		action.linkEntered(control);
}
public void mouseExit(MouseEvent e) {
	Control control = (Control) e.widget;
	if (isHyperlinkCursorUsed())
		control.setCursor(null);
	if (hyperlinkUnderlineMode==UNDERLINE_ROLLOVER)
		underline(control, false);
	if (background != null)
		control.setBackground(background);
	if (foreground != null)
		control.setForeground(foreground);
	IHyperlinkListener action =
		(IHyperlinkListener) hyperlinkListeners.get(control);
	if (action != null)
		action.linkExited(control);
}
		public void mouseHover(MouseEvent e) {
		}
public void mouseUp(MouseEvent e) {
	if (e.button != 1)
		return;
	IHyperlinkListener action =
		(IHyperlinkListener) hyperlinkListeners.get(e.widget);
	if (action != null) {
		Control c = (Control) e.widget;
		c.setCursor(busyCursor);
		action.linkActivated(c);
		if (!c.isDisposed()) 
		   c.setCursor(isHyperlinkCursorUsed()?hyperlinkCursor:null);
	}
}
public void paintControl(PaintEvent e) {
	Control label = (Control) e.widget;
	if (hyperlinkUnderlineMode == UNDERLINE_ALWAYS)
		HyperlinkHandler.underline(label, true);
}
public void registerHyperlink(Control control, IHyperlinkListener listener) {
	if (background != null)
		control.setBackground(background);
	if (foreground != null)
		control.setForeground(foreground);
	control.addMouseListener(this);
	control.addMouseTrackListener(this);
	if (hyperlinkUnderlineMode == UNDERLINE_ALWAYS)
		control.addPaintListener(this);
	hyperlinkListeners.put(control, listener);
	removeDisposedLinks();
}
private void removeDisposedLinks() {
	for (Enumeration keys = hyperlinkListeners.keys(); keys.hasMoreElements();) {
		Control control = (Control)keys.nextElement();
		if (control.isDisposed()) {
			hyperlinkListeners.remove(control);
		}
	}
}
public void reset() {
	hyperlinkListeners.clear();
}
public void setActiveBackground(org.eclipse.swt.graphics.Color newActiveBackground) {
	activeBackground = newActiveBackground;
}
public void setActiveForeground(org.eclipse.swt.graphics.Color newActiveForeground) {
	activeForeground = newActiveForeground;
}
public void setBackground(org.eclipse.swt.graphics.Color newBackground) {
	background = newBackground;
}
public void setForeground(org.eclipse.swt.graphics.Color newForeground) {
	foreground = newForeground;
}
public void setHyperlinkCursorUsed(boolean newHyperlinkCursorUsed) {
	hyperlinkCursorUsed = newHyperlinkCursorUsed;
}
public void setHyperlinkUnderlineMode(int newHyperlinkUnderlineMode) {
	hyperlinkUnderlineMode = newHyperlinkUnderlineMode;
}
public static void underline(Control control, boolean inside) {
	if (!(control instanceof Label))
		return;
	Composite parent = control.getParent();
	Rectangle bounds = control.getBounds();
	GC gc = new GC(parent);
	Color color = inside? control.getForeground() : control.getBackground();
	gc.setForeground(color);
	int y = bounds.y + bounds.height;
	gc.drawLine(bounds.x, y, bounds.x+bounds.width, y);
	gc.dispose();
}
}
