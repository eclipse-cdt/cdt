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

import org.eclipse.cdt.internal.ui.browser.util.ArrayUtil;

/**
 * The default type filter.
 */
public class TypeInfoFilter implements ITypeInfoFilter {

	public int[] getCElementTypes() {
		return TypeInfo.getAllCElementTypes();
	}
	
	public TypeInfoFilter() {
	}

	protected boolean hideSystemTypes() {
		return true;
	}

	public boolean match(ITypeInfo info) {
		// check if type is handled
		if (!ArrayUtil.contains(getCElementTypes(), info.getElementType()))
			return false;

		// filter out low-level system types eg __FILE
		//TODO make this a preferences option
		if (hideSystemTypes() && info.isSystemType())
			return false;

		return true;
	}
}
