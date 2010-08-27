/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		QNX - Initial API and implementation
 * 		IBM Corporation
 *      Andrew Ferguson (Symbian)
 *      Anton Leherbauer (Wind River Systems)
 *      Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.index.IIndexMacroContainer;
import org.eclipse.cdt.core.model.ICElement;

/**
 * Convenience class for bridging the model gap between binding types and CModel types
 * 
 * This is internal in case some IBinding's do not have ICElement constants in future
 */
public class IndexModelUtil {
	private static final String[] EMPTY_STRING_ARRAY= {};

	/**
	 * Returns whether the binding is of any of the specified CElement type constants
	 * @param binding
	 * @param kinds
	 * @return whether the binding is of any of the specified CElement type constants
	 */
	public static boolean bindingHasCElementType(IBinding binding, int[] kinds) {
		for (int kind : kinds) {
			switch(kind) {
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
			case ICElement.C_FUNCTION:
				if(binding instanceof IFunction)
					return true;
				break;
			case ICElement.C_VARIABLE:
				if(binding instanceof IVariable)
					return true;
				break;
			}
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
			switch(classType.getKey()) {
			case ICPPClassType.k_class:
				elementType = ICElement.C_CLASS;
				break;
			case ICompositeType.k_struct:
				elementType = ICElement.C_STRUCT;
				break;
			case ICompositeType.k_union:
				elementType = ICElement.C_UNION;
				break;
			}
		}

		if (binding instanceof ICPPNamespace || binding instanceof ICPPNamespaceAlias) {
			elementType = ICElement.C_NAMESPACE;
		}
		if (binding instanceof IEnumeration) {
			elementType = ICElement.C_ENUMERATION;
		}
		if (binding instanceof ITypedef) {
			elementType = ICElement.C_TYPEDEF;
		}
		if (binding instanceof IFunction) {
			elementType = ICElement.C_FUNCTION;
		}
		if (binding instanceof IVariable) {
			IScope scope= null;
			try {
				scope = binding.getScope();
			} catch (DOMException e) {
			}
			if (scope != null && scope.getKind() == EScopeKind.eLocal) {
				elementType= ICElement.C_VARIABLE_LOCAL;
			} else {
				elementType = ICElement.C_VARIABLE;
			}
		}
		if (binding instanceof IEnumerator) {
			elementType = ICElement.C_ENUMERATOR;
		}
		if (binding instanceof IMacroBinding || binding instanceof IIndexMacroContainer) {
			elementType= ICElement.C_MACRO;
		}
		if (binding instanceof IParameter) {
			elementType= ICElement.C_VARIABLE_LOCAL;
		}
		return elementType;
	}

	/**
	 * Extract the parameter types of the given function as array of strings.
	 * @param function
	 * @return the parameter types of the function
	 * @throws DOMException
	 */
	public static String[] extractParameterTypes(IFunction function) throws DOMException {
		IParameter[] params= function.getParameters();
		String[] parameterTypes= new String[params.length];
		for (int i = 0; i < params.length; i++) {
			IParameter param = params[i];
			parameterTypes[i]= ASTTypeUtil.getType(param.getType(), false);
		}
		if (parameterTypes.length == 1 && parameterTypes[0].equals("void")) { //$NON-NLS-1$
			return EMPTY_STRING_ARRAY;
		}
		return parameterTypes;
	}

	/**
	 * Extract the return type of the given function as string.
	 * @param function
	 * @return the return type of the function
	 * @throws DOMException 
	 */
	public static String extractReturnType(IFunction function) throws DOMException {
		return ASTTypeUtil.getType(function.getType().getReturnType(), false);
	}
}
