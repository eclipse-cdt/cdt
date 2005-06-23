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
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.Iterator;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Abstract base implementation of the breakpoint ruler actions.
 */
public abstract class AbstractBreakpointRulerAction extends Action implements IUpdate {

	private IVerticalRulerInfo fInfo;

	private IWorkbenchPart fTargetPart;

	private IBreakpoint fBreakpoint;

	protected IBreakpoint determineBreakpoint() {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints( CDebugCorePlugin.getUniqueIdentifier() );
		for( int i = 0; i < breakpoints.length; i++ ) {
			IBreakpoint breakpoint = breakpoints[i];
			if ( breakpoint instanceof ILineBreakpoint ) {
				ILineBreakpoint lineBreakpoint = (ILineBreakpoint)breakpoint;
				if ( breakpointAtRulerLine( lineBreakpoint ) ) {
					return lineBreakpoint;
				}
			}
		}
		return null;
	}

	protected IVerticalRulerInfo getInfo() {
		return fInfo;
	}

	protected void setInfo( IVerticalRulerInfo info ) {
		fInfo = info;
	}

	protected IWorkbenchPart getTargetPart() {
		return this.fTargetPart;
	}
	protected void setTargetPart( IWorkbenchPart targetPart ) {
		this.fTargetPart = targetPart;
	}

	protected IBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint( IBreakpoint breakpoint ) {
		fBreakpoint = breakpoint;
	}

	protected boolean breakpointAtRulerLine( ILineBreakpoint cBreakpoint ) {
		int lineNumber = getBreakpointLine( cBreakpoint );
		int rulerLine = getInfo().getLineOfLastMouseButtonActivity();
		return ( rulerLine == lineNumber );
	}

	private int getBreakpointLine( ILineBreakpoint breakpoint ) {
		if ( getTargetPart() instanceof ISaveablePart && ((ISaveablePart)getTargetPart()).isDirty() ) {
			try {
				return breakpoint.getLineNumber();
			}
			catch( CoreException e ) {
				DebugPlugin.log( e );
			}
		}
		else {
			Position position = getBreakpointPosition( breakpoint );
			if ( position != null ) {
				IDocument doc = getDocument();
				if ( doc != null ) {
					try {
						return doc.getLineOfOffset( position.getOffset() );
					}
					catch ( BadLocationException x ) {
						DebugPlugin.log( x );
					}
				}
			}
		}
		return -1;
	}

	private Position getBreakpointPosition( ILineBreakpoint breakpoint ) {
		IAnnotationModel model = getAnnotationModel();
		if ( model != null ) {
			Iterator it = model.getAnnotationIterator();
			while( it.hasNext() ) {
				Annotation ann = (Annotation)it.next();
				if ( ann instanceof MarkerAnnotation && ((MarkerAnnotation)ann).getMarker().equals( breakpoint.getMarker() ) ) {
					return model.getPosition( ann );
				}
			}
		}
		return null;
	}

	private IDocument getDocument() {
		IWorkbenchPart targetPart = getTargetPart();
		if ( targetPart instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if ( provider != null )
				return provider.getDocument( textEditor.getEditorInput() );
		}
		else if ( targetPart instanceof DisassemblyView ) {
			DisassemblyView dv = (DisassemblyView)targetPart;
			IDocumentProvider provider = dv.getDocumentProvider();
			if ( provider != null )
				return provider.getDocument( dv.getInput() );
		}
		return null;
	}

	private IAnnotationModel getAnnotationModel() {
		IWorkbenchPart targetPart = getTargetPart();
		if ( targetPart instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if ( provider != null )
				return provider.getAnnotationModel( textEditor.getEditorInput() );
		}
		else if ( targetPart instanceof DisassemblyView ) {
			DisassemblyView dv = (DisassemblyView)targetPart;
			IDocumentProvider provider = dv.getDocumentProvider();
			if ( provider != null )
				return provider.getAnnotationModel( dv.getInput() );
		}
		return null;
	}
}
