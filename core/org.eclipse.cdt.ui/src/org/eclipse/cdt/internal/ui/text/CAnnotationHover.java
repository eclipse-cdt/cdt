/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import org.eclipse.cdt.internal.ui.CUIMessages;

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
	 * Selects a set of markers from the two lists. By default, it just returns
	 * the set of exact matches.
	 */
	protected List select(List exactMatch, List including) {
		return exactMatch;
	}

	/**
	 * Returns one marker which includes the ruler's line of activity.
	 */
	protected List getMarkersForLine(ISourceViewer viewer, int line) {
		
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

	/*
	 * @see IVerticalRulerHover#getHoverInfo(ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		List markers= getMarkersForLine(sourceViewer, lineNumber);
		if (markers != null && markers.size() > 0) {
			
			if (markers.size() == 1) {
				
				// optimization
				IMarker marker= (IMarker) markers.get(0);
				String message= marker.getAttribute(IMarker.MESSAGE, (String) null);
				if (message != null && message.trim().length() > 0)
					return formatSingleMessage(message);
					
			} else {
					
				List messages= new ArrayList();
				
				Iterator e= markers.iterator();
				while (e.hasNext()) {
					IMarker marker= (IMarker) e.next();
					String message= marker.getAttribute(IMarker.MESSAGE, (String) null);
					if (message != null && message.trim().length() > 0)
						messages.add(message.trim());
				}
				
				if (messages.size() == 1)
					return formatSingleMessage((String) messages.get(0));
					
				if (messages.size() > 1)
					return formatMultipleMessages(messages);
			}
		}
		
		return null;
	}
		
	
	/*
	 * Formats a message as HTML text.
	 */
	private String formatSingleMessage(String message) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
	
	/*
	 * Formats several message as HTML text.
	 */
	private String formatMultipleMessages(List messages) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(CUIMessages.getString("CAnnotationHover.multipleMarkers"))); //$NON-NLS-1$
		
		HTMLPrinter.startBulletList(buffer);
		Iterator e= messages.iterator();
		while (e.hasNext())
			HTMLPrinter.addBullet(buffer, HTMLPrinter.convertToHTMLContent((String) e.next()));
		HTMLPrinter.endBulletList(buffer);	
		
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
}
