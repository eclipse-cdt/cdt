/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import java.util.Comparator;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.ICElement;

public class TypeInfoComparator implements Comparator {
	/**
	 * Compares two ITypeInfo/ICElement types. A type is considered to be
	 * greater if it may contain the other.
	 * 
	 * @return		an int less than 0 if object1 is less than object2,
	 *				0 if they are equal, and > 0 if object1 is greater
	 * 
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		int t1 = getElementType(o1);
		int t2 = getElementType(o2);
		return getIdForElementType(t1) - getIdForElementType(t2);
	}

	/**
	 * Compares two C element types. A type is considered to be
	 * greater if it may contain the other.
	 * 
	 * @return		an int < 0 if object1 is less than object2,
	 *				0 if they are equal, and > 0 if object1 is greater
	 * 
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, int elementType) {
		int t1 = getElementType(o1);
		if (t1 == 0)
			throw new ClassCastException();
		return getIdForElementType(t1) - getIdForElementType(elementType);
	}

	int getElementType(Object obj) {
		if (obj instanceof ICElement) {
			return ((ICElement)obj).getElementType();
		} else if (obj instanceof ITypeInfo) {
			return ((ITypeInfo)obj).getCElementType();
		} else {
			return 0;
		}
	}

	int getIdForElementType(int elementType) {
		switch (elementType) {
			case ICElement.C_MODEL:
				return 100;
			case ICElement.C_PROJECT:
				return 90;
			case ICElement.C_CCONTAINER:
				return 80;
			case ICElement.C_UNIT:
				return 70;
			case ICElement.C_NAMESPACE:
				return 60;
			case ICElement.C_CLASS:
				return 50;
			case ICElement.C_STRUCT:
				return 40;
			case ICElement.C_UNION:
				return 30;
			case ICElement.C_ENUMERATION:
				return 20;
			case ICElement.C_TYPEDEF:
				return 10;
			default :
				return 1;
		}
	}
}
