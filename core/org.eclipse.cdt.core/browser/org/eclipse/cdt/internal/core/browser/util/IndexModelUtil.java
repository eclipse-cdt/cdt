/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		QNX - Initial API and implementation
 * 		IBM Corporation
 *      Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.util;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.model.ICElement;

/**
 * Convenience class for bridging the model gap between binding types and CModel types
 * 
 * This is internal in case some IBinding's do not have ICElement constants in future
 */
public class IndexModelUtil {
	/**
	 * Returns whether the binding is of any of the specified CElement type constants
	 * @param binding
	 * @param kinds
	 * @return whether the binding is of any of the specified CElement type constants
	 */
	public static boolean bindingHasCElementType(IBinding binding, int[] kinds) {
		try {
			for(int i=0; i<kinds.length; i++) {
				switch(kinds[i]) {
				case ICElement.C_STRUCT:
					if (binding instanceof ICompositeType
							&& ((ICompositeType)binding).getKey() == ICompositeType.k_struct)
						return true;
					break;
				case ICElement.C_UNION:
					if (binding instanceof ICompositeType
							&& ((ICompositeType)binding).getKey() == ICompositeType.k_union)
						return true;
					break;
				case ICElement.C_CLASS:
					if (binding instanceof ICompositeType
							&& ((ICompositeType)binding).getKey() == ICPPClassType.k_class)
						return true;
					break;
				case ICElement.C_NAMESPACE:
					if (binding instanceof ICPPNamespace || binding instanceof ICPPNamespaceAlias)
						return true;
					break;
				case ICElement.C_ENUMERATION:
					if (binding instanceof IEnumeration)
						return true;
					break;
				case ICElement.C_TYPEDEF:
					if(binding instanceof ITypedef)
						return true;
					break;
				}
			}
		} catch(DOMException de) {
			CCorePlugin.log(de);
		}
		return false;
	}
	
	/**
	 * Returns the CElement type constant for the specified binding
	 * @param binding
	 * @return the CElement type constant for the specified binding
	 */
	public static int getElementType(IBinding binding) {
		int elementType = Integer.MIN_VALUE;

		if (binding instanceof ICompositeType) {
			ICompositeType classType = (ICompositeType) binding;
			try {
				if(classType.getKey() == ICPPClassType.k_class) {
					elementType = ICElement.C_CLASS;
				} else if(classType.getKey() == ICPPClassType.k_struct) {
					elementType = ICElement.C_STRUCT;
				} else if(classType.getKey() == ICPPClassType.k_union) {
					elementType = ICElement.C_UNION;
				}
			} catch(DOMException de) {
				CCorePlugin.log(de);
			}
		}

		if (binding instanceof ICPPNamespace || binding instanceof ICPPNamespaceAlias) {
			elementType = ICElement.C_NAMESPACE;
		}

		if (binding instanceof IEnumeration) {
			elementType = ICElement.C_ENUMERATION; 		
		}
		return elementType;
	}
}
