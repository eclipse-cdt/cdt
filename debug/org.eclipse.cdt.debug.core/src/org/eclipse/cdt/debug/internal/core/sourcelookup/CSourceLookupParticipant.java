/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
 
/**
 * A source lookup participant that searches for C/C++ source code.
 */
public class CSourceLookupParticipant extends AbstractSourceLookupParticipant {

	static private class NoSourceElement {
	}

	private static final NoSourceElement gfNoSource = new NoSourceElement();

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
		if ( object instanceof IAdaptable ) {
			ICStackFrame frame = (ICStackFrame)((IAdaptable)object).getAdapter( ICStackFrame.class );
			if ( frame != null ) {
				String name = frame.getFile();
				if ( name == null || name.trim().length() == 0 )
					return new Object[] { gfNoSource };
			}
		}
		return super.findSourceElements( object );
	}
}
