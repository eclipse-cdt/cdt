package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.source.ISourceViewer;



public class PrintMarginPainter  implements IPainter, PaintListener {
	

	private StyledText fTextWidget;
	
	private int fMarginWidth= 80;
	private Color fColor;
	private int fLineStyle= SWT.LINE_SOLID;
	private int fLineWidth= 1;
	
	private int fCachedWidgetX= -1;
	private boolean fIsActive= false;
	
	public PrintMarginPainter(ISourceViewer sourceViewer) {
		fTextWidget= sourceViewer.getTextWidget();
	}
	
	public void setMarginRulerColumn(int width) {
		fMarginWidth= width;
		intialize();
	}
	
	public void setMarginRulerStyle(int lineStyle) {
		fLineStyle= lineStyle;
	}
	
	public void setMarginRulerWidth(int lineWidth) {
		fLineWidth= lineWidth;
	}
	
	/**
	 * Must be called before <code>paint</code> is called the first time.
	 */
	public void setMarginRulerColor(Color color) {
		fColor= color;
	}
	
	/**
	 * Must be called explicitly when font of text widget changes.
	 */
	public void intialize() {
		computeWidgetX();
		fTextWidget.redraw();
	}
	
	private void computeWidgetX() {
		GC gc= new GC(fTextWidget);
		int pixels= gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();
		
		fCachedWidgetX= pixels * fMarginWidth;
	}
	
	/*
	 * @see IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			fTextWidget.removePaintListener(this);
			if (redraw)
				fTextWidget.redraw();
		}	
	}

	/*
	 * @see IPainter#dispose()
	 */
	public void dispose() {
		fTextWidget= null;
	}

	/*
	 * @see IPainter#paint(int)
	 */
	public void paint(int reason) {
		if (!fIsActive) {
			fIsActive= true;
			fTextWidget.addPaintListener(this);
			if (fCachedWidgetX == -1)
				computeWidgetX();
			fTextWidget.redraw();
		}
	}

	/*
	 * @see IPainter#setPositionManager(IPositionManager)
	 */
	public void setPositionManager(IPositionManager manager) {
	}
	
	/*
	 * @see PaintListener#paintControl(PaintEvent)
	 */
	public void paintControl(PaintEvent e) {
		if (fTextWidget != null) {
			int x= fCachedWidgetX - fTextWidget.getHorizontalPixel();
			if (x >= 0) {
				Rectangle area= fTextWidget.getClientArea();
				e.gc.setForeground(fColor);
				e.gc.setLineStyle(fLineStyle);
				e.gc.setLineWidth(fLineWidth);
				e.gc.drawLine(x, 0, x, area.height);
			}
		}
	}
}


