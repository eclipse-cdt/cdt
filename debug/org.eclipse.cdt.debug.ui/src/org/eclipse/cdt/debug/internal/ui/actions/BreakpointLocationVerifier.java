/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
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
	public int getValidBreakpointLocation( IDocument doc, int lineNumber ) 
	{
		// for now
		return lineNumber + 1;
	}
}
