/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.jface.text.IDocument;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 29, 2002
 */
public class BreakpointLocationVerifier
{
	/**
	 * Returns the line number closest to the given line number that represents a
	 * valid location for a breakpoint in the given document, or -1 if a valid location
	 * cannot be found.
	 */
	public int getValidLineBreakpointLocation( IDocument doc, int lineNumber ) 
	{
		// for now
		return lineNumber + 1;
	}

	public int getValidAddressBreakpointLocation( IDocument doc, int lineNumber ) 
	{
		// for now
		return lineNumber + 1;
	}
}
