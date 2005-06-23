/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
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
import java.util.LinkedList;
import java.util.List;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Annotation model for Disassembly view.
 */
public class DisassemblyAnnotationModel extends AnnotationModel {

	private DisassemblyEditorInput fInput;

//	private IDocument fDisassemblyDocument;

	/**
	 * Constructor for DisassemblyAnnotationModel.
	 */
	public DisassemblyAnnotationModel() {
		super();
	}

	protected void breakpointsAdded( final IBreakpoint[] breakpoints, final IDocument document ) {
		DisassemblyEditorInput input = getInput();
		if ( DisassemblyEditorInput.EMPTY_EDITOR_INPUT.equals( input ) ||
			 DisassemblyEditorInput.PENDING_EDITOR_INPUT.equals( input ) )
			 return;
		asyncExec( new Runnable() {		
			public void run() {
				breakpointsAdded0( breakpoints, document );
			}
		} ); 			
	}

	protected void breakpointsRemoved( final IBreakpoint[] breakpoints, final IDocument document ) {
		DisassemblyEditorInput input = getInput();
		if ( DisassemblyEditorInput.EMPTY_EDITOR_INPUT.equals( input ) ||
			 DisassemblyEditorInput.PENDING_EDITOR_INPUT.equals( input ) )
			 return;
		asyncExec( new Runnable() {		
			public void run() {
				breakpointsRemoved0( breakpoints, document );
			}
		} );
	}

	protected void breakpointsChanged( final IBreakpoint[] breakpoints, final IDocument document ) {
		DisassemblyEditorInput input = getInput();
		if ( DisassemblyEditorInput.EMPTY_EDITOR_INPUT.equals( input ) ||
			 DisassemblyEditorInput.PENDING_EDITOR_INPUT.equals( input ) )
			 return;
		asyncExec( new Runnable() {		
			public void run() {
				breakpointsChanged0( breakpoints, document );
			}
		} );
	}

	protected void breakpointsAdded0( IBreakpoint[] breakpoints, IDocument document ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( breakpoints[i] instanceof ICLineBreakpoint && isApplicable( breakpoints[i] ) ) {
				addBreakpointAnnotation( (ICLineBreakpoint)breakpoints[i], document );
			}
		}
		fireModelChanged();
	}

	protected void breakpointsRemoved0( IBreakpoint[] breakpoints, IDocument document ) {
		removeAnnotations( findAnnotationsforBreakpoints( breakpoints ), true, false );
	}

	protected void breakpointsChanged0( IBreakpoint[] breakpoints, IDocument document ) {
		List annotations = findAnnotationsforBreakpoints( breakpoints );
		List markers = new ArrayList( annotations.size() );
		Iterator it = annotations.iterator();
		while( it.hasNext() ) {
			MarkerAnnotation ma = (MarkerAnnotation)it.next();
			markers.add( ma.getMarker() );
			modifyAnnotationPosition( ma, getPosition( ma ), false );
		}
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( breakpoints[i] instanceof ICLineBreakpoint && !markers.contains( breakpoints[i].getMarker() ) ) {
				addBreakpointAnnotation( (ICLineBreakpoint)breakpoints[i], document );
			}
		}
		fireModelChanged();
	}

	protected DisassemblyEditorInput getInput() {
		return this.fInput;
	}

	protected void setInput( DisassemblyEditorInput input, IDocument document ) {
		DisassemblyEditorInput oldInput = this.fInput;
		this.fInput = input;
		if ( this.fInput != null && !this.fInput.equals( oldInput ) )
			updateAnnotations( document );
	}

	private boolean isApplicable( IBreakpoint breakpoint ) {
		return true;
	}

	private void addBreakpointAnnotation( ICLineBreakpoint breakpoint, IDocument document ) {
		Position position = createBreakpointPosition( breakpoint, document );
		if ( position != null ) {
			try {
				addAnnotation( createMarkerAnnotation( breakpoint ), position, false );
			}
			catch( BadLocationException e ) {
			}
		}
	}

	private Position createBreakpointPosition( ICLineBreakpoint breakpoint, IDocument document ) {
		Position position = null;
		DisassemblyEditorInput input = getInput();
		if ( input != null ) {
			int start = -1;
			if ( document != null ) {
				int instrNumber = input.getInstructionLine( breakpoint );
				if ( instrNumber > 0 ) {
					try {
						start = fDocument.getLineOffset( instrNumber - 1 );
						if ( start > -1 ) {
							return new Position( start, document.getLineLength( instrNumber - 1 ) );
						}
					}
					catch( BadLocationException e ) {
					}
				}
			}
		}
		return position;
	}

	private MarkerAnnotation createMarkerAnnotation( IBreakpoint breakpoint ) {
		return new MarkerAnnotation( breakpoint.getMarker() );
	}

	protected void dispose() {
	}

	private List findAnnotationsforBreakpoints( IBreakpoint[] breakpoints ) {
		List annotations = new LinkedList();
		Iterator it = getAnnotationIterator();
		while ( it.hasNext() ) {
			Annotation ann = (Annotation)it.next();
			if ( ann instanceof MarkerAnnotation ) {
				IMarker marker = ((MarkerAnnotation)ann).getMarker();
				if ( marker != null ) {
					for ( int i = 0; i < breakpoints.length; ++i ) {
						if ( marker.equals( breakpoints[i].getMarker() ) ) {
							annotations.add( ann );
						}
					}
				}
			}
		}
		return annotations;
	}

	private void asyncExec( Runnable r ) {
		Display display = Display.getDefault();
		if ( display != null )
			display.asyncExec( r );
	}

	private void updateAnnotations( final IDocument document ) {
		asyncExec( new Runnable() {		
			public void run() {
				doUpdateAnnotations( document );
			}
		} );
	}

	protected void doUpdateAnnotations( IDocument document ) {
		breakpointsAdded0( DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(), document );
	}
}
