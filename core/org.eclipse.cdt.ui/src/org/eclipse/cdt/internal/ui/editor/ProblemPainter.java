package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

//import org.eclipse.jdt.core.compiler.IProblem;


/**
 * Highlights the temporary problems.
 */
public class ProblemPainter implements IPainter, PaintListener, IAnnotationModelListener {
	
	private boolean fIsActive= false;
	private boolean fIsPainting= false;
	protected boolean fIsModelChanging= false;
	
	private Color fColor;
	private ITextEditor fTextEditor;
	private ISourceViewer fSourceViewer;
	private StyledText fTextWidget;
	private IAnnotationModel fModel;
	private List fProblemPositions= new ArrayList();
	
	
	
	public ProblemPainter(ITextEditor textEditor, ISourceViewer sourceViewer) {
		fTextEditor= textEditor;
		fSourceViewer= sourceViewer;
		fTextWidget= sourceViewer.getTextWidget();
	}
	
	private boolean hasProblems() {
		return !fProblemPositions.isEmpty();
	}	
	
	protected void enablePainting() {
		if (!fIsPainting && hasProblems()) {
			fIsPainting= true;
			fTextWidget.addPaintListener(this);
			handleDrawRequest(null);
		}
	}
	
	protected void disablePainting(boolean redraw) {
		if (fIsPainting) {
			fIsPainting= false;
			fTextWidget.removePaintListener(this);
			if (redraw && hasProblems())
				handleDrawRequest(null);
		}
	}
	
	protected void setModel(IAnnotationModel model) {
		
		if (fModel != model) {
			if (fModel != null)
				fModel.removeAnnotationModelListener(this);
			fModel= model;
			if (fModel != null)
				fModel.addAnnotationModelListener(this);
		}
		
		if (fProblemPositions != null) {
			fProblemPositions.clear();
			if (fModel != null) {
				Iterator e= new ProblemAnnotationIterator(fModel);
				while (e.hasNext()) {
					IProblemAnnotation pa= (IProblemAnnotation) e.next();
					if (pa.isProblem()) {
						Annotation a= (Annotation) pa;
						Position p= fModel.getPosition(a);
						fProblemPositions.add(p);
					}
				}
			}
		}
	}
	
	/*
	 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
	 */
	public void modelChanged(final IAnnotationModel model) {
		if (fTextWidget != null && !fTextWidget.isDisposed() && !fIsModelChanging) {
			Display d= fTextWidget.getDisplay();
			if (d != null) {
				d.asyncExec(new Runnable() {
					public void run() {
						disablePainting(true);
						try {
							fIsModelChanging= true;
							setModel(model);
						} finally {
							fIsModelChanging= false;
						}
						enablePainting();					
					}
				});
			}	
		}
	}
	
	public void setHighlightColor(Color color) {
		fColor= color;
	}
	
	/*
	 * @see IPainter#dispose()
	 */
	public void dispose() {
		fColor= null;
		fTextWidget= null;
		fModel= null;
		fProblemPositions= null;
	}
	
	/*
	 * @see PaintListener#paintControl(PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null)
			handleDrawRequest(event.gc);
	}
	
	private void handleDrawRequest(GC gc) {
		
		IRegion region= fSourceViewer.getVisibleRegion();
		int offset= region.getOffset();
		int length= region.getLength();
		
		for (Iterator e = fProblemPositions.iterator(); e.hasNext();) {
			Position p = (Position) e.next();
			if (p.overlapsWith(offset, length)) {
				int p1= Math.max(offset, p.getOffset());
				int p2= Math.min(offset + length, p.getOffset() + p.getLength());
				draw(gc, p1 - offset, p2 - p1);
			}
		}
	}
	
	private int[] computePolyline(Point left, Point right, int height) {
		
		final int WIDTH= 4; // must be even
		final int HEIGHT= 2; // can be any number
		
		int leftX= left.x;
		int peeks= (right.x - left.x) / WIDTH;
				
		// compute (number of point) * 2
		int length= ((2 * peeks) + 1) * 2;
		if (length < 0)
			return new int[0];
			
		int[] coordinates= new int[length];
		
		// cache peeks' y-coordinates
		int bottom= left.y + height - 1;
		int top= bottom - HEIGHT;
		
		// populate array with peek coordinates
		for (int i= 0; i < peeks; i++) {
			int index= 4 * i;
			coordinates[index]= leftX + (WIDTH * i);
			coordinates[index+1]= bottom;
			coordinates[index+2]= coordinates[index] + WIDTH/2;
			coordinates[index+3]= top;
		}
		
		// the last down flank is missing
		coordinates[length-2]= left.x + (WIDTH * peeks);
		coordinates[length-1]= bottom;
		
		return coordinates;
	}
	
	private void draw(GC gc, int offset, int length) {
		if (gc != null) {
			
			Point left= fTextWidget.getLocationAtOffset(offset);
			Point right= fTextWidget.getLocationAtOffset(offset + length);
			
			gc.setForeground(fColor);
			int[] polyline= computePolyline(left, right, gc.getFontMetrics().getHeight());
			gc.drawPolyline(polyline);
								
		} else {
			fTextWidget.redrawRange(offset, length, true);
		}
	}
	
	/*
	 * @see IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			disablePainting(redraw);
			setModel(null);
		}
	}
	
	/*
	 * @see IPainter#paint(int)
	 */
	public void paint(int reason) {
		if (!fIsActive) {
			fIsActive= true;
			IDocumentProvider provider= fTextEditor.getDocumentProvider();
			setModel(provider.getAnnotationModel(fTextEditor.getEditorInput()));
			enablePainting();
		}
	}

	/*
	 * @see IPainter#setPositionManager(IPositionManager)
	 */
	public void setPositionManager(IPositionManager manager) {
	}
}


