package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import org.eclipse.cdt.internal.ui.text.CPairMatcher;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;




public final class BracketPainter implements IPainter, PaintListener {
			
	private CPairMatcher fMatcher= new CPairMatcher(new char[] { '{', '}', '(', ')', '[', ']' });
	private Position fBracketPosition= new Position(0, 0);
	private int fAnchor;
	
	private boolean fIsActive= false;
	private ISourceViewer fSourceViewer;
	private StyledText fTextWidget;
	private Color fColor;
	private boolean fNoBox;
	
	private IPositionManager fPositionManager;
	
	
	public BracketPainter(ISourceViewer sourceViewer) {
		fSourceViewer= sourceViewer;
		fTextWidget= sourceViewer.getTextWidget();
	}
	
	public void setHighlightColor(Color color) {
		fColor= color;
	}

	public void setHighlightStyle(boolean nobox) {
		fNoBox = nobox;
	}
					
	public void dispose() {
		if (fMatcher != null) {
			fMatcher.dispose();
			fMatcher= null;
		}
		
		fColor= null;
		fTextWidget= null;
	}
				
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			fTextWidget.removePaintListener(this);
			if (fPositionManager != null)
				fPositionManager.removeManagedPosition(fBracketPosition);
			if (redraw)
				handleDrawRequest(null);
		}
	}
		
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null)
			handleDrawRequest(event.gc);
	}
	
	private void handleDrawRequest(GC gc) {
		
		if (fBracketPosition.isDeleted)
			return;
			
		int length= fBracketPosition.getLength();
		if (length < 1)
			return;
			
		int offset= fBracketPosition.getOffset();
		IRegion region= fSourceViewer.getVisibleRegion();
		
		if (region.getOffset() <= offset && region.getOffset() + region.getLength() >= offset + length) {
			offset -= region.getOffset();
			if (CPairMatcher.RIGHT == fAnchor)
				draw(gc, offset, 1);
			else 
				draw(gc, offset + length -1, 1);					
		}
	}
	
	private void draw(GC gc, int offset, int length) {
		if (gc != null) {
			Point left= fTextWidget.getLocationAtOffset(offset);
			Point right= fTextWidget.getLocationAtOffset(offset + length);
			
			gc.setForeground(fColor);
			if(fNoBox) {
				gc.drawString(fTextWidget.getTextRange(offset, 1), left.x, left.y);
			} else {
				gc.drawRectangle(left.x, left.y, right.x - left.x - 1, gc.getFontMetrics().getHeight() - 1);
			}				
		} else {
			fTextWidget.redrawRange(offset, length, true);
		}
	}
	
	/*
	 * @see IPainter#paint(int)
	 */
	public void paint(int reason) {
		Point selection= fSourceViewer.getSelectedRange();
		if (selection.y > 0) {
			deactivate(true);
			return;
		}
			
		IRegion pair= fMatcher.match(fSourceViewer.getDocument(), selection.x);
		if (pair == null) {
			deactivate(true);
			return;
		}
		
		if (fIsActive) {
			// only if different
			if (pair.getOffset() != fBracketPosition.getOffset() || 
					pair.getLength() != fBracketPosition.getLength() || 
					fMatcher.getAnchor() != fAnchor) {
				
				// remove old highlighting
				handleDrawRequest(null);
				// update position
				fBracketPosition.isDeleted= false;
				fBracketPosition.offset= pair.getOffset();
				fBracketPosition.length= pair.getLength();
				fAnchor= fMatcher.getAnchor();
				// apply new highlighting
				handleDrawRequest(null);
			
			}
		} else {
			
			fIsActive= true;
			
			fBracketPosition.isDeleted= false;
			fBracketPosition.offset= pair.getOffset();
			fBracketPosition.length= pair.getLength();
			fAnchor= fMatcher.getAnchor();
			
			fTextWidget.addPaintListener(this);
			fPositionManager.addManagedPosition(fBracketPosition);
			handleDrawRequest(null);
		}
	}
	
	/*
	 * @see IPainter#setPositionManager(IPositionManager)
	 */
	public void setPositionManager(IPositionManager manager) {
		fPositionManager= manager;
	}
}

