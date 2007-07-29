/*******************************************************************************
 * Copyright (c) 2004, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - Added support for AbsoluteSourceContainer( 159833 ) 
 * Ken Ryall (Nokia) - Added support for CSourceNotFoundElement ( 167305 )
 * Ken Ryall (Nokia) - Option to open disassembly view when no source ( 81353 )
*******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceLookupChangeListener;
import org.eclipse.cdt.debug.internal.core.ListenerList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
 
/**
 * A source lookup participant that searches for C/C++ source code.
 */
public class CSourceLookupParticipant extends AbstractSourceLookupParticipant {

	static class NoSourceElement {
	}

	private static final NoSourceElement gfNoSource = new NoSourceElement();

	private ListenerList fListeners;

	/** 
	 * Constructor for CSourceLookupParticipant. 
	 */
	public CSourceLookupParticipant() {
		super();
		fListeners = new ListenerList( 1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.lang.Object)
	 */
	public String getSourceName( Object object ) throws CoreException {
		if ( object instanceof String ) {
			return (String)object;
		}
		if ( object instanceof IAdaptable ) {
			ICStackFrame frame = (ICStackFrame)((IAdaptable)object).getAdapter( ICStackFrame.class );
			if ( frame != null ) {
				String name = frame.getFile();
				return ( name != null && name.trim().length() > 0 ) ? name : null;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#findSourceElements(java.lang.Object)
	 */
	public Object[] findSourceElements( Object object ) throws CoreException {
		// Workaround for cases when the stack frame doesn't contain the source file name 
		String name = null;
		if ( object instanceof IAdaptable ) {
			ICStackFrame frame = (ICStackFrame)((IAdaptable)object).getAdapter( ICStackFrame.class );
			if ( frame != null ) {
				name = frame.getFile().trim();
				if ( name == null || name.length() == 0 )
				{
					if (object instanceof IDebugElement)
						return new Object[] { new CSourceNotFoundElement( (IDebugElement) object ) };
					else
						return new Object[] { gfNoSource };					
				}
			}
		}
		else if ( object instanceof String ) {
			name = (String)object;
		}
		Object[] foundElements = super.findSourceElements( object );
		if (foundElements.length == 0 && (object instanceof IDebugElement))
			foundElements = new Object[] { new CSourceNotFoundElement( (IDebugElement) object ) };
		return foundElements;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#dispose()
	 */
	public void dispose() {
		fListeners.removeAll();
		super.dispose();
	}

	public void addSourceLookupChangeListener( ISourceLookupChangeListener listener ) {
		fListeners.add( listener );
	}

	public void removeSourceLookupChangeListener( ISourceLookupChangeListener listener ) {
		fListeners.remove( listener );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#sourceContainersChanged(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public void sourceContainersChanged( ISourceLookupDirector director ) {
		Object[] listeners = fListeners.getListeners();
		for ( int i = 0; i < listeners.length; ++i )
			((ISourceLookupChangeListener)listeners[i]).sourceContainersChanged( director );
		super.sourceContainersChanged( director );
	}
}
