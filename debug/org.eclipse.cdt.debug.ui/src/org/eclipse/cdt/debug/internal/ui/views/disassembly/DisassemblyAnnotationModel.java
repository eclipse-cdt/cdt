/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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
public class DisassemblyAnnotationModel extends AnnotationModel implements IBreakpointsListener {

	private DisassemblyEditorInput fInput;

	private IDocument fDisassemblyDocument;

	/**
	 * Constructor for DisassemblyAnnotationModel.
	 */
	public DisassemblyAnnotationModel( IDocument document ) {
		super();
		fDisassemblyDocument = document;
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded( final IBreakpoint[] breakpoints ) {
		if ( getInput().equals( DisassemblyEditorInput.EMPTY_EDITOR_INPUT ) ||
			 getInput().equals( DisassemblyEditorInput.PENDING_EDITOR_INPUT ) )
			 return;
		asyncExec( new Runnable() {		
			public void run() {
				breakpointsAdded0( breakpoints );
			}
		} ); 			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved( final IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		if ( getInput().equals( DisassemblyEditorInput.EMPTY_EDITOR_INPUT ) ||
			 getInput().equals( DisassemblyEditorInput.PENDING_EDITOR_INPUT ) )
			 return;
		asyncExec( new Runnable() {		
			public void run() {
				breakpointsRemoved0( breakpoints );
			}
		} );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged( final IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		if ( getInput().equals( DisassemblyEditorInput.EMPTY_EDITOR_INPUT ) ||
			 getInput().equals( DisassemblyEditorInput.PENDING_EDITOR_INPUT ) )
			 return;
		asyncExec( new Runnable() {		
			public void run() {
				breakpointsChanged0( breakpoints );
			}
		} );
	}

	protected void breakpointsAdded0( IBreakpoint[] breakpoints ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( breakpoints[i] instanceof ICLineBreakpoint && isApplicable( breakpoints[i] ) ) {
				addBreakpointAnnotation( (ICLineBreakpoint)breakpoints[i] );
			}
		}
		fireModelChanged();
	}

	protected void breakpointsRemoved0( IBreakpoint[] breakpoints ) {
		removeAnnotations( findAnnotationsforBreakpoints( breakpoints ), true, false );
	}

	protected void breakpointsChanged0( IBreakpoint[] breakpoints ) {
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
				addBreakpointAnnotation( (ICLineBreakpoint)breakpoints[i] );
			}
		}
		fireModelChanged();
	}

	protected DisassemblyEditorInput getInput() {
		return this.fInput;
	}

	protected void setInput( DisassemblyEditorInput input ) {
		DisassemblyEditorInput oldInput = this.fInput;
		this.fInput = input;
		if ( this.fInput != null && !this.fInput.equals( oldInput ) )
			updateAnnotations();
	}

	private boolean isApplicable( IBreakpoint breakpoint ) {
		return true;
	}

	private void addBreakpointAnnotation( ICLineBreakpoint breakpoint ) {
		Position position = createBreakpointPosition( breakpoint );
		if ( position != null ) {
			try {
				addAnnotation( createMarkerAnnotation( breakpoint ), position, false );
			}
			catch( BadLocationException e ) {
			}
		}
	}

	private Position createBreakpointPosition( ICLineBreakpoint breakpoint ) {
		Position position = null;
		DisassemblyEditorInput input = getInput();
		if ( input != null ) {
			long address = input.getBreakpointAddress( breakpoint );
			int start = -1;
			if ( address > 0 && fDisassemblyDocument != null ) {
				int instrNumber = input.getInstructionNumber( address );
				if ( instrNumber > 0 ) {
					try {
						start = fDocument.getLineOffset( instrNumber - 1 );
						if ( start > -1 ) {
							return new Position( start, fDisassemblyDocument.getLineLength( instrNumber - 1 ) );
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
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
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
		Display display = DebugUIPlugin.getStandardDisplay();
		if ( display != null )
			display.asyncExec( r );
	}

	private void updateAnnotations() {
		asyncExec( new Runnable() {		
			public void run() {
				doUpdateAnnotations();
			}
		} );
	}

	protected void doUpdateAnnotations() {
		breakpointsAdded0( DebugPlugin.getDefault().getBreakpointManager().getBreakpoints() );
	}
}
