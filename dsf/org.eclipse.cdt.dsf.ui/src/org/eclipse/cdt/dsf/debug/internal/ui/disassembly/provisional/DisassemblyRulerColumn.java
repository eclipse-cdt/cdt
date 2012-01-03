/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional;

import java.util.Arrays;

import org.eclipse.cdt.debug.ui.disassembly.rulers.AbstractContributedRulerColumn;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * Vertical ruler column for use with disassembly parts.
 * <p>
 * Derived from {@link org.eclipse.jface.text.source.LineNumberRulerColumn}.
 * </p>
 */
public class DisassemblyRulerColumn extends AbstractContributedRulerColumn implements IPropertyChangeListener {
	protected final static String DOTS =   "......................................................................"; //$NON-NLS-1$
	protected final static String SPACES = "                                                                      "; //$NON-NLS-1$

	/**
	 * Internal listener class.
	 */
	class InternalListener implements IViewportListener, ITextListener, ISelectionChangedListener {

		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		@Override
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}

		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		@Override
		public void textChanged(TextEvent event) {

			if (updateNumberOfDigits()) {
				computeIndentations();
				layout(event.getViewerRedrawState());
				return;
			}

			if (!event.getViewerRedrawState())
				return;

			if (fSensitiveToTextChanges || event.getDocumentEvent() == null)
				postRedraw();

		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			postRedraw();
		}
	}

	/**
	 * Handles all the mouse interaction in this line number ruler column.
	 */
	class MouseHandler implements MouseListener, MouseMoveListener, MouseTrackListener {

		/** The cached view port size */
		private int fCachedViewportSize;
		/** The area of the line at which line selection started */
		private IRegion fStartLine;
		/** The number of the line at which line selection started */
		private int fStartLineNumber;
		/** The auto scroll direction */
		private int fAutoScrollDirection;

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseUp(MouseEvent event) {
			// see bug 45700
			if (event.button == 1) {
				stopSelecting();
				stopAutoScroll();
				postRedraw();
			}
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDown(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			// see bug 45700
			if (event.button == 1) {
				startSelecting();
			}
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDoubleClick(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			stopSelecting();
			stopAutoScroll();
		}

		/*
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseMove(MouseEvent event) {
			if (!autoScroll(event)) {
				int newLine = fParentRuler.toDocumentLineNumber(event.y);
				expandSelection(newLine);
			}
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseEnter(MouseEvent event) {
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseExit(MouseEvent event) {
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseHover(MouseEvent event) {
		}

		/**
		 * Called when line drag selection started. Adds mouse move and track
		 * listeners to this column's control.
		 */
		private void startSelecting() {
			try {

				// select line
				IDocument document = fCachedTextViewer.getDocument();
				fStartLineNumber = fParentRuler.getLineOfLastMouseButtonActivity();
				fStartLine = document.getLineInformation(fStartLineNumber);
				fCachedTextViewer.setSelectedRange(fStartLine.getOffset(), fStartLine.getLength());
				fCachedViewportSize = getVisibleLinesInViewport();

				// prepare for drag selection
				fCanvas.addMouseMoveListener(this);
				fCanvas.addMouseTrackListener(this);

			} catch (BadLocationException x) {
			}
		}

		/**
		 * Called when line drag selection stopped. Removes all previously
		 * installed listeners from this column's control.
		 */
		private void stopSelecting() {
			// drag selection stopped
			fCanvas.removeMouseMoveListener(this);
			fCanvas.removeMouseTrackListener(this);
		}

		/**
		 * Expands the line selection from the remembered start line to the
		 * given line.
		 * 
		 * @param lineNumber
		 *            the line to which to expand the selection
		 */
		private void expandSelection(int lineNumber) {
			try {

				IDocument document = fCachedTextViewer.getDocument();
				IRegion lineInfo = document.getLineInformation(lineNumber);

				int start = Math.min(fStartLine.getOffset(), lineInfo.getOffset());
				int end = Math.max(fStartLine.getOffset() + fStartLine.getLength(), lineInfo.getOffset()
					+ lineInfo.getLength());

				if (lineNumber < fStartLineNumber)
					fCachedTextViewer.setSelectedRange(end, start - end);
				else
					fCachedTextViewer.setSelectedRange(start, end - start);

			} catch (BadLocationException x) {
			}
		}

		/**
		 * Called when auto scrolling stopped. Clears the auto scroll direction.
		 */
		private void stopAutoScroll() {
			fAutoScrollDirection = SWT.NULL;
		}

		/**
		 * Called on drag selection.
		 * 
		 * @param event
		 *            the mouse event caught by the mouse move listener
		 * @return <code>true</code> if scrolling happened, <code>false</code>
		 *         otherwise
		 */
		private boolean autoScroll(MouseEvent event) {
			Rectangle area = fCanvas.getClientArea();

			if (event.y > area.height) {
				autoScroll(SWT.DOWN);
				return true;
			}

			if (event.y < 0) {
				autoScroll(SWT.UP);
				return true;
			}

			stopAutoScroll();
			return false;
		}

		/**
		 * Scrolls the viewer into the given direction.
		 * 
		 * @param direction
		 *            the scroll direction
		 */
		private void autoScroll(int direction) {

			if (fAutoScrollDirection == direction)
				return;

			final int TIMER_INTERVAL = 5;
			final Display display = fCanvas.getDisplay();
			Runnable timer = null;
			switch (direction) {
			case SWT.UP:
				timer = new Runnable() {
					@Override
					public void run() {
						if (fAutoScrollDirection == SWT.UP) {
							int top = getInclusiveTopIndex();
							if (top > 0) {
								fCachedTextViewer.setTopIndex(top - 1);
								expandSelection(top - 1);
								display.timerExec(TIMER_INTERVAL, this);
							}
						}
					}
				};
				break;
			case SWT.DOWN:
				timer = new Runnable() {
					@Override
					public void run() {
						if (fAutoScrollDirection == SWT.DOWN) {
							int top = getInclusiveTopIndex();
							fCachedTextViewer.setTopIndex(top + 1);
							expandSelection(top + 1 + fCachedViewportSize);
							display.timerExec(TIMER_INTERVAL, this);
						}
					}
				};
				break;
			}

			if (timer != null) {
				fAutoScrollDirection = direction;
				display.timerExec(TIMER_INTERVAL, timer);
			}
		}

		/**
		 * Returns the viewer's first visible line, even if only partially
		 * visible.
		 * 
		 * @return the viewer's first visible line
		 */
		private int getInclusiveTopIndex() {
			if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed()) {
				int top = fCachedTextViewer.getTopIndex();
				if ((fCachedTextWidget.getTopPixel() % fCachedTextWidget.getLineHeight()) != 0)
					--top;
				return top;
			}
			return -1;
		}
	}

	/** This column's parent ruler */
	private CompositeRuler fParentRuler;
	/** Cached text viewer */
	private ITextViewer fCachedTextViewer;
	/** Cached text widget */
	private StyledText fCachedTextWidget;
	/** The columns canvas */
	private Canvas fCanvas;
	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The drawable for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private InternalListener fInternalListener = new InternalListener();
	/** The font of this column */
	private Font fFont;
	/** The indentation cache */
	private int[] fIndentation;
	/** Indicates whether this column reacts on text change events */
	private boolean fSensitiveToTextChanges = false;
	/** The foreground color */
	private Color fForeground;
	/** The background color */
	private Color fBackground;
	/** Cached number of displayed digits */
	private int fCachedNumberOfDigits = -1;
	/** Flag indicating whether a relayout is required */
	private boolean fRelayoutRequired = false;
	/**
	 * Redraw runnable lock
	 */
	private Object fRunnableLock = new Object();
	/**
	 * Redraw runnable state
	 */
	private boolean fIsRunnablePosted = false;
	/**
	 * Redraw runnable
	 */
	private Runnable fRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (fRunnableLock) {
				fIsRunnablePosted = false;
			}
			redraw();
		}
	};
	private boolean fAlignRight;
	private boolean fPaintStyleBackground;
	private boolean fPaintSelectionBackground;

	/**
	 * Constructs a new vertical ruler column.
	 * 
	 */
	public DisassemblyRulerColumn() {
		this(SWT.LEFT);
		// default constructor
	}

	public DisassemblyRulerColumn(int align) {
		this(align, true, false);
	}

	public DisassemblyRulerColumn(int align, boolean paintSelection, boolean paintStyle) {
		fAlignRight = (align & SWT.RIGHT) != 0;
		fPaintSelectionBackground = paintSelection;
		fPaintStyleBackground = paintStyle;
	}

	@Override
	public void columnCreated() {
		super.columnCreated();
		getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public void columnRemoved() {
		getPreferenceStore().removePropertyChangeListener(this);
		super.columnRemoved();
	}

	/**
	 * Sets the foreground color of this column.
	 * 
	 * @param foreground
	 *            the foreground color
	 */
	public void setForeground(Color foreground) {
		fForeground = foreground;
	}

	/**
	 * Returns the foreground color being used to print the line numbers.
	 * 
	 * @return the configured foreground color
	 */
	protected Color getForeground() {
		return fForeground;
	}

	/**
	 * Sets the background color of this column.
	 * 
	 * @param background
	 *            the background color
	 */
	public void setBackground(Color background) {
		fBackground = background;
		if (fCanvas != null && !fCanvas.isDisposed())
			fCanvas.setBackground(getBackground(fCanvas.getDisplay()));
	}

	/**
	 * Returns the System background color for list widgets.
	 * 
	 * @param display
	 *            the display
	 * @return the System background color for list widgets
	 */
	protected Color getBackground(Display display) {
		if (fBackground == null)
			return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		return fBackground;
	}

	public IPreferenceStore getPreferenceStore() {
		return DsfUIPlugin.getDefault().getPreferenceStore();
	}

	/*
	 * @see IVerticalRulerColumn#getControl()
	 */
	@Override
	public Control getControl() {
		return fCanvas;
	}

	/*
	 * @see IVerticalRuleColumnr#getWidth
	 */
	@Override
	public int getWidth() {
		return fIndentation[0];
	}

	/**
	 * Computes the number of digits to be displayed. Returns <code>true</code>
	 * if the number of digits changed compared to the previous call of this
	 * method. If the method is called for the first time, the return value is
	 * also <code>true</code>.
	 * 
	 * @return whether the number of digits has been changed
	 */
	protected boolean updateNumberOfDigits() {
		if (fCachedTextViewer == null)
			return false;

		int digits = computeNumberOfCharacters();

		if (fCachedNumberOfDigits != digits) {
			fCachedNumberOfDigits = digits;
			return true;
		}

		return false;
	}

	/**
	 * Does the real computation of the number of characters. The default
	 * implementation computes the number of digits for the line number.
	 * Subclasses may override this method if they need extra space on the ruler.
	 * 
	 * @return the number of characters to be displayed on the ruler.
	 */
	protected int computeNumberOfCharacters() {
		IDocument document = fCachedTextViewer.getDocument();
		int lines= document == null ? 0 : document.getNumberOfLines();

		int digits= 2;
		while (lines > Math.pow(10, digits) - 1) {
			++digits;
		}
		return digits;
	}

	/**
	 * Layouts the enclosing viewer to adapt the layout to changes of the size
	 * of the individual components.
	 * 
	 * @param redraw
	 *            <code>true</code> if this column can be redrawn
	 */
	protected void layout(boolean redraw) {
		if (!redraw) {
			fRelayoutRequired= true;
			return;
		}

		fRelayoutRequired= false;
		if (fCachedTextViewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension= (ITextViewerExtension) fCachedTextViewer;
			Control control= extension.getControl();
			if (control instanceof Composite && !control.isDisposed()) {
				Composite composite= (Composite) control;
				composite.layout(true);
			}
		}
	}

	/**
	 * Computes the indentations for the given font and stores them in
	 * <code>fIndentation</code>.
	 */
	protected void computeIndentations() {
		if (fCanvas == null)
			return;

		GC gc= new GC(fCanvas);
		try {

			gc.setFont(fCanvas.getFont());

			fIndentation= new int[fCachedNumberOfDigits + 1];
			char[] digitStr= new char[fCachedNumberOfDigits + 1];
			Arrays.fill(digitStr, '9');
			Point p= gc.stringExtent(new String(digitStr, 0, fCachedNumberOfDigits + 1));
			fIndentation[0]= p.x;

			for (int i= 1; i <= fCachedNumberOfDigits; i++) {
				p= gc.stringExtent(new String(digitStr, 0, i));
				fIndentation[i]= fIndentation[0] - p.x;
			}

		} finally {
			gc.dispose();
		}
	}

	/*
	 * @see IVerticalRulerColumn#createControl(CompositeRuler, Composite)
	 */
	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {

		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();

		fCanvas= new Canvas(parentControl, SWT.NONE);
		fCanvas.setBackground(getBackground(fCanvas.getDisplay()));
		fCanvas.setForeground(fForeground);

		fCanvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent event) {
				if (fCachedTextViewer != null)
					doubleBufferPaint(event.gc);
			}
		});

		fCanvas.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
				fCachedTextViewer= null;
				fCachedTextWidget= null;
			}
		});

		fCanvas.addMouseListener(new MouseHandler());

		if (fCachedTextViewer != null) {

			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);
			fCachedTextViewer.getSelectionProvider().addSelectionChangedListener(fInternalListener);

			if (fFont == null) {
				if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed())
					fFont= fCachedTextWidget.getFont();
			}
		}

		if (fFont != null)
			fCanvas.setFont(fFont);

		updateNumberOfDigits();
		computeIndentations();
		return fCanvas;
	}

	/**
	 * Disposes the column's resources.
	 */
	protected void handleDispose() {

		if (fCachedTextViewer != null) {
			fCachedTextViewer.removeViewportListener(fInternalListener);
			fCachedTextViewer.removeTextListener(fInternalListener);
			fCachedTextViewer.getSelectionProvider().removeSelectionChangedListener(fInternalListener);
		}

		if (fBuffer != null) {
			fBuffer.dispose();
			fBuffer= null;
		}
	}

	protected Color getColor(String key) {
		return EditorsUI.getSharedTextColors().getColor(PreferenceConverter.getColor(getPreferenceStore(), key));
	}

	protected Color getColor(RGB color) {
		return EditorsUI.getSharedTextColors().getColor(color);
	}

	/**
	 * Double buffer drawing.
	 * 
	 * @param dest
	 *            the gc to draw into
	 */
	private void doubleBufferPaint(GC dest) {

		Point size= fCanvas.getSize();

		if (size.x <= 0 || size.y <= 0)
			return;

		if (fBuffer != null) {
			Rectangle r= fBuffer.getBounds();
			if (r.width != size.x || r.height != size.y) {
				fBuffer.dispose();
				fBuffer= null;
			}
		}
		if (fBuffer == null)
			fBuffer= new Image(fCanvas.getDisplay(), size.x, size.y);

		GC gc= new GC(fBuffer);
		gc.setFont(fCanvas.getFont());
		if (fForeground != null)
			gc.setForeground(fForeground);

		try {
			gc.setBackground(getBackground(fCanvas.getDisplay()));
			gc.fillRectangle(0, 0, size.x, size.y);

			if (fCachedTextViewer instanceof ITextViewerExtension5)
				doPaint1(gc);
			else
				doPaint(gc);

		} finally {
			gc.dispose();
		}

		dest.drawImage(fBuffer, 0, 0);
	}

	/**
	 * Returns the viewport height in lines.
	 * 
	 * @return the viewport height in lines
	 */
	protected int getVisibleLinesInViewport() {
		Rectangle clArea= fCachedTextWidget.getClientArea();
		return clArea.height / fCachedTextWidget.getLineHeight();
	}

	/**
	 * Draws the ruler column.
	 * 
	 * @param gc
	 *            the gc to draw into
	 */
	private void doPaint(GC gc) {

		if (fCachedTextViewer == null)
			return;

		if (fCachedTextWidget == null)
			return;

		int firstLine= 0;

		int topLine= fCachedTextWidget.getTopIndex();
		fScrollPos= fCachedTextWidget.getTopPixel();
		int lineheight= fCachedTextWidget.getLineHeight();
		int partialLineHidden= fScrollPos % lineheight;

		if (partialLineHidden > 0 && topLine > 0) // widgetTopLine shows the
			// first fully visible line
			--topLine;

		int bottomLine;

		try {

			IRegion region= fCachedTextViewer.getVisibleRegion();
			IDocument doc= fCachedTextViewer.getDocument();

			if (doc == null)
				return;

			firstLine= doc.getLineOfOffset(region.getOffset());
			if (firstLine > topLine)
				topLine= firstLine;

			bottomLine= doc.getLineOfOffset(region.getOffset() + region.getLength());

		} catch (BadLocationException x) {
			return;
		}

		fSensitiveToTextChanges= bottomLine - topLine < getVisibleLinesInViewport();

		int baselineBias= getBaselineBias(gc);

		int topInset= fCachedTextViewer.getTopInset();
		int y= topInset - partialLineHidden;
		Point canvasSize= fCanvas.getSize();
		Point selection= fCachedTextWidget.getSelection();
		boolean selectedLine= false;
		Color defaultForeground= gc.getForeground();
		Color defaultBackground= gc.getBackground();

		for (int line= topLine; y < canvasSize.y && line <= bottomLine; line++, y += lineheight) {
			int widgetOffset= fCachedTextWidget.getOffsetAtLine(line);
			if (fPaintSelectionBackground && widgetOffset >= selection.x && widgetOffset < selection.y) {
				if (!selectedLine) {
					selectedLine= true;
					gc.setForeground(fCachedTextWidget.getSelectionForeground());
					gc.setBackground(fCachedTextWidget.getSelectionBackground());
				}
			} else if (selectedLine) {
				selectedLine= false;
				gc.setForeground(defaultForeground);
				gc.setBackground(defaultBackground);
			}
			if (selectedLine) {
				gc.fillRectangle(0, y, canvasSize.x, lineheight);
			} else if (fPaintStyleBackground && widgetOffset >= 0 && widgetOffset < fCachedTextWidget.getCharCount()) {
				StyleRange style= fCachedTextWidget.getStyleRangeAtOffset(widgetOffset);
				if (style != null && style.background != null) {
					gc.setBackground(style.background);
					gc.fillRectangle(0, y + baselineBias, canvasSize.x, lineheight - baselineBias);
					gc.setBackground(defaultBackground);
				}
			}
			paintLine(line, y, lineheight, gc, fCachedTextWidget.getDisplay());
			String s= createDisplayString(line);
			int indentation= fAlignRight ? fIndentation[s.length()] : 0;
			gc.drawString(s, indentation, y + baselineBias, true);
		}
	}

	/**
	 * Computes the string to be printed for <code>line</code>. The default
	 * implementation returns <code>Integer.toString(line + 1)</code>.
	 * 
	 * @param line
	 *            the line number for which the string is generated
	 * @return the string to be printed on the ruler column for <code>line</code>
	 */
	protected String createDisplayString(int line) {
		return Integer.toString(line + 1);
	}

	/**
	 * Draws the ruler column. Uses <code>ITextViewerExtension5</code> for the
	 * implementation. Will replace <code>doPinat(GC)</code>.
	 * 
	 * @param gc
	 *            the gc to draw into
	 */
	private void doPaint1(GC gc) {

		if (fCachedTextViewer == null)
			return;

		ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;

		int widgetTopLine= fCachedTextWidget.getTopIndex();
		fScrollPos= fCachedTextWidget.getTopPixel();
		int lineheight= fCachedTextWidget.getLineHeight();
		int partialLineHidden= fScrollPos % lineheight;

		if (partialLineHidden > 0 && widgetTopLine > 0) // widgetTopLine shows
			// the first fully
			// visible line
			--widgetTopLine;

		int modelTopLine= extension.widgetLine2ModelLine(widgetTopLine);
		int modelBottomLine= fCachedTextViewer.getBottomIndex();
		if (modelBottomLine >= 0)
			++modelBottomLine;

		try {

			IRegion region= extension.getModelCoverage();
			IDocument doc= fCachedTextViewer.getDocument();

			if (doc == null)
				return;

			int coverageTopLine= doc.getLineOfOffset(region.getOffset());
			if (coverageTopLine > modelTopLine || modelTopLine == -1)
				modelTopLine= coverageTopLine;

			int coverageBottomLine= doc.getLineOfOffset(region.getOffset() + region.getLength());
			if (coverageBottomLine < modelBottomLine || modelBottomLine == -1)
				modelBottomLine= coverageBottomLine;

		} catch (BadLocationException x) {
			return;
		}

		fSensitiveToTextChanges= modelBottomLine - modelTopLine < getVisibleLinesInViewport();

		int baselineBias= getBaselineBias(gc);

		int topInset= fCachedTextViewer.getTopInset();
		int y= topInset - partialLineHidden;
		Point canvasSize= fCanvas.getSize();
		Point selection= fCachedTextWidget.getSelection();
		boolean selectedLine= false;
		Color defaultForeground= gc.getForeground();
		Color defaultBackground= gc.getBackground();

		for (int modelLine= modelTopLine; y < canvasSize.y && modelLine <= modelBottomLine; modelLine++) {

			// don't draw hidden (e.g. folded) lines
			int widgetLine= extension.modelLine2WidgetLine(modelLine);
			if (widgetLine == -1)
				continue;
			int widgetOffset= fCachedTextWidget.getOffsetAtLine(widgetLine);
			if (fPaintSelectionBackground && widgetOffset >= selection.x && widgetOffset < selection.y) {
				if (!selectedLine) {
					selectedLine= true;
					gc.setForeground(fCachedTextWidget.getSelectionForeground());
					gc.setBackground(fCachedTextWidget.getSelectionBackground());
				}
			} else if (selectedLine) {
				selectedLine= false;
				gc.setForeground(defaultForeground);
				gc.setBackground(defaultBackground);
			}
			if (selectedLine) {
				gc.fillRectangle(0, y, canvasSize.x, lineheight);
			} else if (fPaintStyleBackground && widgetOffset >= 0 && widgetOffset < fCachedTextWidget.getCharCount()) {
				StyleRange style= fCachedTextWidget.getStyleRangeAtOffset(widgetOffset);
				if (style != null && style.background != null) {
					gc.setBackground(style.background);
					gc.fillRectangle(0, y + baselineBias, canvasSize.x, lineheight - baselineBias);
					gc.setBackground(defaultBackground);
				}
			}

			paintLine(modelLine, y, lineheight, gc, fCachedTextWidget.getDisplay());

			String s= createDisplayString(modelLine);
			int indentation= fAlignRight ? fIndentation[s.length()] : 0;
			gc.drawString(s, indentation, y + baselineBias, true);
			y += lineheight;
		}
	}

	/**
	 * Returns the difference between the baseline of the widget and the
	 * baseline as specified by the font for <code>gc</code>. When drawing
	 * text, the returned bias should be added to obtain text line up on
	 * the correct base line of the text widget.
	 * 
	 * @param gc
	 *            the <code>GC</code> to get the font metrics from
	 * @return the baseline bias to use when drawing text that is line up with
	 *         <code>fCachedTextWidget</code>
	 */
	private int getBaselineBias(GC gc) {
		/*
		 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62951 widget line
		 * height may be more than the font height used for the text,
		 * since font styles (bold, italics...) can have larger font metrics
		 * than the simple font used for the numbers.
		 */
		int widgetBaseline= fCachedTextWidget.getBaseline();
		FontMetrics fm= gc.getFontMetrics();
		int fontBaseline= fm.getAscent() + fm.getLeading();
		Assert.isTrue(widgetBaseline >= fontBaseline);
		int baselineBias= widgetBaseline - fontBaseline;
		return baselineBias;
	}

	/**
	 * Paints the line. After this method is called the text is painted
	 * on top of the result of this method.
	 * <p>
	 * This default implementation does nothing.
	 * </p>
	 * 
	 * @param line
	 *            the line of the document which the ruler is painted for
	 * @param y
	 *            the y-coordinate of the box being painted for
	 *            <code>line</code>, relative to <code>gc</code>
	 * @param lineheight
	 *            the height of one line (and therefore of the box being
	 *            painted)
	 * @param gc
	 *            the drawing context the client may choose to draw on.
	 * @param display
	 *            the display the drawing occurs on
	 */
	protected void paintLine(int line, int y, int lineheight, GC gc, Display display) {
	}

	/**
	 * Triggers a redraw in the display thread.
	 */
	protected final void postRedraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			Display d= fCanvas.getDisplay();
			if (d != null) {
				synchronized (fRunnableLock) {
					if (fIsRunnablePosted)
						return;
					fIsRunnablePosted= true;
				}
				d.asyncExec(fRunnable);
			}
		}
	}

	/*
	 * @see IVerticalRulerColumn#redraw()
	 */
	@Override
	public void redraw() {

		if (fRelayoutRequired) {
			layout(true);
			return;
		}

		if (fCanvas != null && !fCanvas.isDisposed()) {
			GC gc= new GC(fCanvas);
			doubleBufferPaint(gc);
			gc.dispose();
		}
	}

	/*
	 * @see IVerticalRulerColumn#setModel(IAnnotationModel)
	 */
	@Override
	public void setModel(IAnnotationModel model) {
	}

	/*
	 * @see IVerticalRulerColumn#setFont(Font)
	 */
	@Override
	public void setFont(Font font) {
		fFont= font;
		if (fCanvas != null && !fCanvas.isDisposed()) {
			fCanvas.setFont(fFont);
			updateNumberOfDigits();
			computeIndentations();
		}
	}

	/**
	 * Returns the parent (composite) ruler of this ruler column.
	 * 
	 * @return the parent ruler
	 */
	protected CompositeRuler getParentRuler() {
		return fParentRuler;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
	}

}
