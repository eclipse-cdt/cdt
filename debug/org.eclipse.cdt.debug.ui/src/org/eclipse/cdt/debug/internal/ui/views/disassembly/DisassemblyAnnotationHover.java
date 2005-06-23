/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.internal.ui.HTMLPrinter;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Annotation hovering support for disassembly view.
 */
public class DisassemblyAnnotationHover implements IAnnotationHover {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	public String getHoverInfo( ISourceViewer sourceViewer, int lineNumber ) {
		List markers = getMarkersForLine( sourceViewer, lineNumber );
		if ( markers != null ) {
			if ( markers.size() == 1 ) {
				// optimization
				IMarker marker = (IMarker)markers.get( 0 );
				String message = marker.getAttribute( IMarker.MESSAGE, (String)null );
				if ( message != null && message.trim().length() > 0 )
					return formatSingleMessage( message );
			}
			else {
				List messages = new ArrayList();
				Iterator e = markers.iterator();
				while( e.hasNext() ) {
					IMarker marker = (IMarker)e.next();
					String message = marker.getAttribute( IMarker.MESSAGE, (String)null );
					if ( message != null && message.trim().length() > 0 )
						messages.add( message.trim() );
				}
				if ( messages.size() == 1 )
					return formatSingleMessage( (String)messages.get( 0 ) );
				if ( messages.size() > 1 )
					return formatMultipleMessages( messages );
			}
		}
		return null;
	}

	/**
	 * Returns the distance to the ruler line.
	 */
	protected int compareRulerLine( Position position, IDocument document, int line ) {
		if ( position.getOffset() > -1 && position.getLength() > -1 ) {
			try {
				int markerLine = document.getLineOfOffset( position.getOffset() );
				if ( line == markerLine )
					return 1;
				if ( markerLine <= line && line <= document.getLineOfOffset( position.getOffset() + position.getLength() ) )
					return 2;
			}
			catch( BadLocationException x ) {
			}
		}
		return 0;
	}

	/*
	 * Formats a message as HTML text.
	 */
	private String formatSingleMessage( String message ) {
		StringBuffer buffer = new StringBuffer();
		HTMLPrinter.addPageProlog( buffer );
		HTMLPrinter.addParagraph( buffer, HTMLPrinter.convertToHTMLContent( message ) );
		HTMLPrinter.addPageEpilog( buffer );
		return buffer.toString();
	}

	/*
	 * Formats several message as HTML text.
	 */
	private String formatMultipleMessages( List messages ) {
		StringBuffer buffer = new StringBuffer();
		HTMLPrinter.addPageProlog( buffer );
		HTMLPrinter.addParagraph( buffer, HTMLPrinter.convertToHTMLContent( DisassemblyMessages.getString( "DisassemblyAnnotationHover.Multiple_markers_at_this_line_1" ) ) ); //$NON-NLS-1$
		HTMLPrinter.startBulletList( buffer );
		Iterator e = messages.iterator();
		while( e.hasNext() )
			HTMLPrinter.addBullet( buffer, HTMLPrinter.convertToHTMLContent( (String)e.next() ) );
		HTMLPrinter.endBulletList( buffer );
		HTMLPrinter.addPageEpilog( buffer );
		return buffer.toString();
	}

	/**
	 * Returns one marker which includes the ruler's line of activity.
	 */
	protected List getMarkersForLine( ISourceViewer viewer, int line ) {
		IDocument document = viewer.getDocument();
		IAnnotationModel model = viewer.getAnnotationModel();
		if ( model == null )
			return null;
		List exact = new ArrayList();
		List including = new ArrayList();
		Iterator e = model.getAnnotationIterator();
		while( e.hasNext() ) {
			Object o = e.next();
			if ( o instanceof MarkerAnnotation ) {
				MarkerAnnotation a = (MarkerAnnotation)o;
				switch( compareRulerLine( model.getPosition( a ), document, line ) ) {
					case 1:
						exact.add( a.getMarker() );
						break;
					case 2:
						including.add( a.getMarker() );
						break;
				}
			}
		}
		return select( exact, including );
	}

	/**
	 * Selects a set of markers from the two lists. By default, it just returns
	 * the set of exact matches.
	 */
	protected List select( List exactMatch, List including ) {
		return exactMatch;
	}
}
