package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;


import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;


import org.eclipse.ui.texteditor.MarkerAnnotation;


import org.eclipse.cdt.internal.ui.text.LineBreakingReader;


import org.eclipse.cdt.internal.ui.CPlugin;


public class CAnnotationHover implements IAnnotationHover {
	
	/**
	 * Returns the distance to the ruler line.
	 */
	protected int compareRulerLine(Position position, IDocument document, int line) {
		
		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				int markerLine= document.getLineOfOffset(position.getOffset());
				if (line == markerLine)
					return 1;
				if (markerLine <= line && line <= document.getLineOfOffset(position.getOffset() + position.getLength()))
					return 2;
			} catch (BadLocationException x) {
			}
		}
		
		return 0;
	}
	
	/**
	 * Selects one marker from the two lists.
	 */
	protected IMarker select(List firstChoice, List secondChoice) {
		if (!firstChoice.isEmpty())
			return (IMarker) firstChoice.get(0);
		if (!secondChoice.isEmpty())
			return (IMarker) secondChoice.get(0);
		return null;
	}
		
	/**
	 * Returns one marker which includes the ruler's line of activity.
	 */
	protected IMarker getMarker(ISourceViewer viewer, int line) {
		
		IDocument document= viewer.getDocument();
		IAnnotationModel model= viewer.getAnnotationModel();
		
		if (model == null)
			return null;
			
		List exact= new ArrayList();
		List including= new ArrayList();
		
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof MarkerAnnotation) {
				MarkerAnnotation a= (MarkerAnnotation) o;
				switch (compareRulerLine(model.getPosition(a), document, line)) {
					case 1:
						exact.add(a.getMarker());
						break;
					case 2:
						including.add(a.getMarker());
						break;
				}
			}
		}
		
		return select(exact, including);
	}


		
	/**
	 * @see IVerticalRulerHover#getHoverInfo(ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IMarker marker= getMarker(sourceViewer, lineNumber);
		if (marker != null) {
			String text= marker.getAttribute(IMarker.MESSAGE, (String) null);
			if (text != null) {
				return formatHoverText(text, sourceViewer.getTextWidget().getDisplay());
			}
		}
		return null;
	}
	
	/*
	 * Formats the message of this hover to fit onto the screen.
	 */
	private String formatHoverText(String text, Display display) {
		String lineDelim= System.getProperty("line.separator", "\n");
				
		Reader textReader= new StringReader(text);
		GC gc= new GC(display);
		try {
			StringBuffer buf= new StringBuffer();
			
			LineBreakingReader reader= new LineBreakingReader(textReader, gc, getHoverWidth(display));
			String line= reader.readLine();
			while (line != null) {
				if (buf.length() != 0) {
					buf.append(lineDelim);
				}
				buf.append(line);
				line= reader.readLine();
			}
			return buf.toString();
		} catch (IOException e) {
			CPlugin.log(e);
		} finally {
			gc.dispose();
		}
		return null;
	}
	
	private int getHoverWidth(Display display) {
		Rectangle displayBounds= display.getBounds();
		int hoverWidth= displayBounds.width - (display.getCursorLocation().x - displayBounds.x);
		hoverWidth-= 12; // XXX: Add some space to the border, Revisit
		if (hoverWidth < 200) {
			hoverWidth= 200;
		}
		return hoverWidth;
	}	
	
	
}
