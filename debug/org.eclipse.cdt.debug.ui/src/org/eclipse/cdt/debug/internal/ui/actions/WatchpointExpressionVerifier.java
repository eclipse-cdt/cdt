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
