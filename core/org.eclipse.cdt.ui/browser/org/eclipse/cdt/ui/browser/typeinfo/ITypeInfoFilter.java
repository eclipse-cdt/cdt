/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

/**
 * Filter for type info.
 */
public interface ITypeInfoFilter {
	
	/**
	 * Gets the C types handled by this filter.
	 * 
	 * @return An array of ICElement types.
	 * 
	 */
	public int[] getCElementTypes();

	/**
	 * Matches type info against filter.
	 * 
	 * @param info The object to filter.
	 * @return <code>true</code> if successful match.
	 */
	public boolean match(ITypeInfo info);
	/**
	 * Returns <code>true</code> if <code>info</code> matches the filter.
	 */
}
