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
 * @since Sep 5, 2002
 */
public class WatchpointExpressionVerifier
{
	/**
	 * Returns whether the specified expression is valid for a watchpoint.
	 */
	public boolean isValidExpression( IDocument doc, String expression ) 
	{
		// for now
		return expression.trim().length() > 0;
	}
}
