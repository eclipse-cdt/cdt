/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Utility methods for C/C++ Debug UI.
 */
public class CDebugUIUtils {

	static public IRegion findWord( IDocument document, int offset ) {
		int start = -1;
		int end = -1;
		try {
			int pos = offset;
			char c;
			while( pos >= 0 ) {
				c = document.getChar( pos );
				if ( !Character.isJavaIdentifierPart( c ) )
					break;
				--pos;
			}
			start = pos;
			pos = offset;
			int length = document.getLength();
			while( pos < length ) {
				c = document.getChar( pos );
				if ( !Character.isJavaIdentifierPart( c ) )
					break;
				++pos;
			}
			end = pos;
		}
		catch( BadLocationException x ) {
		}
		if ( start > -1 && end > -1 ) {
			if ( start == offset && end == offset )
				return new Region( offset, 0 );
			else if ( start == offset )
				return new Region( start, end - start );
			else
				return new Region( start + 1, end - start - 1 );
		}
		return null;
	}

	/**
	 * Returns the currently selected stack frame or the topmost frame 
	 * in the currently selected thread in the Debug view 
	 * of the current workbench page. Returns <code>null</code> 
	 * if no stack frame or thread is selected, or if not called from the UI thread.
	 *  
	 * @return the currently selected stack frame or the topmost frame 
	 * 		   in the currently selected thread
	 */
	static public ICStackFrame getCurrentStackFrame() {
		IAdaptable context = DebugUITools.getDebugContext();
		return ( context != null ) ? (ICStackFrame)context.getAdapter( ICStackFrame.class ) : null;
	}
}
