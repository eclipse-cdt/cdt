/*******************************************************************************
 * Copyright (c) 2008, 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Represents a partially instantiated C++ class template, declaration of which is not yet available.
 *
 * @author Sergey Prigogin
 */
public class CPPUnknownClassInstance extends CPPUnknownClass implements ICPPUnknownClassInstance {
	private final ICPPTemplateArgument[] arguments;

	public CPPUnknownClassInstance(ICPPUnknownBinding scopeBinding, char[] name, ICPPTemplateArgument[] arguments) {
		super(scopeBinding, name);
		this.arguments = arguments;
	}

	public ICPPTemplateArgument[] getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return getName() + " " + ASTTypeUtil.getArgumentListString(arguments, true); //$NON-NLS-1$ 
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (this == type) 
			return true;
		
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof ICPPUnknownClassInstance) { 
			ICPPUnknownClassInstance rhs= (ICPPUnknownClassInstance) type;
			if (CharArrayUtils.equals(getNameCharArray(), rhs.getNameCharArray())) {
				ICPPTemplateArgument[] lhsArgs= getArguments();
				ICPPTemplateArgument[] rhsArgs= rhs.getArguments();
				if (lhsArgs != rhsArgs) {
					if (lhsArgs == null || rhsArgs == null)
						return false;
				
					if (lhsArgs.length != rhsArgs.length)
						return false;
				
					for (int i= 0; i < lhsArgs.length; i++) {
						if (!lhsArgs[i].isSameValue(rhsArgs[i])) 
							return false;
					}
				}
				final IBinding lhsContainer = getOwner();
				final IBinding rhsContainer = rhs.getOwner();
				if (lhsContainer instanceof IType && rhsContainer instanceof IType) {
					 return (((IType)lhsContainer).isSameType((IType) rhsContainer));
				}
			}
		}
		return false;
	}
}
