/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.ui.CUIPlugin;

public class BuildConsoleViewer extends TextViewer
	implements  LineStyleListener,
				LineBackgroundListener,
				MouseTrackListener,
				MouseListener {

	protected InternalDocumentListener fInternalDocumentListener = new InternalDocumentListener();
	/**
	 * Whether the console scrolls as output is appended.
	 */
	private boolean fAutoScroll = true;
	/**
	 * Internal document listener.
	 */
	class InternalDocumentListener implements IDocumentListener {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		@Override
		public void documentAboutToBeChanged(DocumentEvent e) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		@Override
		public void documentChanged(DocumentEvent e) {
			revealEndOfDocument();
		}
	}

	/**
	 * Sets whether this viewer should auto-scroll as output is appended to the
	 * document.
	 *
	 * @param scroll
	 */
	public void setAutoScroll(boolean scroll) {
		fAutoScroll = scroll;
	}

	/**
	 * Returns whether this viewer should auto-scroll as output is appended to
	 * the document.
	 */
	public boolean isAutoScroll() {
		return fAutoScroll;
	}

	/**
	 * Creates a new console viewer and adds verification checking to only
	 * allow text modification if the text is being modified in the editable
	 * portion of the underlying document.
	 *
	 * @see org.eclipse.swt.events.VerifyListener
	 */
	public BuildConsoleViewer(Composite parent) {
		super(parent, getSWTStyles());
		StyledText styledText = getTextWidget();
		styledText.addLineStyleListener(this);
		styledText.addLineBackgroundListener(this);
		styledText.addMouseTrackListener(this);
		styledText.setFont(parent.getFont());
		styledText.setDoubleClickEnabled(true);
		styledText.setEditable(false);
		styledText.setWordWrap(true);
	}

	/**
	 * Returns the SWT style flags used when instantiating this viewer
	 */
	private static int getSWTStyles() {
		int styles = SWT.H_SCROLL | SWT.V_SCROLL;
		return styles;
	}

	/**
	 * Reveals (makes visible) the end of the current document
	 */
	protected void revealEndOfDocument() {
		if (isAutoScroll()) {
			IDocument doc = getDocument();
			int lines = doc.getNumberOfLines();
			try {
				// lines are 0-based
				int lineStartOffset = doc.getLineOffset(lines - 1);
				StyledText widget = getTextWidget();
				if (lineStartOffset > 0) {
					widget.setCaretOffset(lineStartOffset);
					widget.showSelection();
				}
				int lineEndOffset = lineStartOffset + doc.getLineLength(lines - 1);
				if (lineEndOffset > 0) {
					widget.setCaretOffset(lineEndOffset);
				}
			} catch (BadLocationException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.ITextViewer#setDocument(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public void setDocument(IDocument doc) {
		IDocument oldDoc = getDocument();
		IDocument document = doc;
		if (oldDoc == null && document == null) {
			return;
		}
		if (oldDoc != null) {
			oldDoc.removeDocumentListener(fInternalDocumentListener);
			if (oldDoc.equals(document)) {
				document.addDocumentListener(fInternalDocumentListener);
				return;
			}
		}

		super.setDocument(document);
		if (document != null) {
			revealEndOfDocument();
			document.addDocumentListener(fInternalDocumentListener);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt.custom.LineStyleEvent)
	 */
	@Override
	public void lineGetStyle(LineStyleEvent event) {
		IDocument document = getDocument();
		if (document == null) return;
		BuildConsolePartitioner partitioner = (BuildConsolePartitioner) document.getDocumentPartitioner();
		if (partitioner == null) return;

		BuildConsolePartition p = partitioner.fDocumentMarkerManager.getCurrentPartition();
		Color problemHighlightedColor =  partitioner.fManager.getProblemHighlightedColor();

		// Note, computePartitioning actually doesn't change anything in partitioning,
		// but only computes number of affected regions.
		ITypedRegion[] regions = partitioner.computePartitioning(event.lineOffset, event.lineText.length());
		StyleRange[] styles = new StyleRange[regions.length];
		for (int i = 0; i < regions.length; i++) {
			BuildConsolePartition partition = (BuildConsolePartition) regions[i];
			if (partition.getStream()== null) return;

			Color colorFG = partition.getStream().getColor();
			Color colorBG = null;

			// Highlight current partition
			if ( partition == p ) {
				colorFG = problemHighlightedColor;
			}
			StyleRange styleRange = new StyleRange(partition.getOffset(), partition.getLength(), colorFG, colorBG);
			styles[i] = styleRange;
		}
		event.styles = styles;
	}

	public void selectPartition(BuildConsolePartitioner partitioner, BuildConsolePartition p) {
		try {
			int start = partitioner.getDocument().getLineOfOffset(p.getOffset());
			int end = partitioner.getDocument().getLineOfOffset(p.getOffset()+p.getLength()-1);

			if ( fAutoScroll ) {
				// Check if area around this line is visible, scroll if needed
				int top = getTopIndex();
				int bottom = getBottomIndex();
				if ( start < top + 1 ) {
					setTopIndex(start - 1 > 0 ? start - 1 : 0);
				} else if ( end > bottom -1 ) {
					setTopIndex(top + start - bottom + 1);
				}
			}

			// Select line
			StyledText st = getTextWidget();
			st.redrawRange(0, partitioner.getDocument().getLength(), true);

		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	@Override
	public void mouseEnter(MouseEvent e) {
		getTextWidget().addMouseListener(this);
	}

	@Override
	public void mouseExit(MouseEvent e) {
		getTextWidget().removeMouseListener(this);
	}

	@Override
	public void mouseHover(MouseEvent e) {
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		int offset = -1;
		try {
			Point p = new Point(e.x, e.y);
			offset = getTextWidget().getOffsetAtLocation(p);
			BuildConsole.getCurrentPage().moveToError(offset);
		} catch (IllegalArgumentException ex) {
		}
	}

	@Override
	public void mouseDown(MouseEvent e) {
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		IDocument document = getDocument();
		if (document == null) return;
		BuildConsolePartitioner partitioner = (BuildConsolePartitioner) document.getDocumentPartitioner();
		if (partitioner == null) return;

		BuildConsolePartition partition = (BuildConsolePartition) partitioner.getPartition(event.lineOffset);
		// Set background for error partitions
		if (partition!=null) {
			String type = partition.getType();
			if (type==BuildConsolePartition.ERROR_PARTITION_TYPE) {
				event.lineBackground = partitioner.fManager.getProblemBackgroundColor();
			} else if (type==BuildConsolePartition.WARNING_PARTITION_TYPE) {
				event.lineBackground = partitioner.fManager.getWarningBackgroundColor();
			} else if (type==BuildConsolePartition.INFO_PARTITION_TYPE) {
				event.lineBackground = partitioner.fManager.getInfoBackgroundColor();
			}
		}
	}

}
