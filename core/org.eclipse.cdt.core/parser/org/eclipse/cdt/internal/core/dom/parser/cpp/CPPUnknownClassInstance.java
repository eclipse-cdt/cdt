/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/*
 * Represents a partially instantiated C++ class template, declaration of which is not yet available.
 *
 * @author Sergey Prigogin
 */
public class CPPUnknownClassInstance extends CPPUnknownClass implements ICPPUnknownClassInstance {
	private final IType[] arguments;

	public CPPUnknownClassInstance(ICPPUnknownBinding scopeBinding, IASTName name, IType[] arguments) {
		super(scopeBinding, name);
		this.arguments = arguments;
	}

	public IType[] getArguments() {
		return arguments;
	}

	@Override
	public IBinding resolvePartially(ICPPUnknownBinding parentBinding, ObjectMap argMap, ICPPScope instantiationScope) {
		IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap, instantiationScope);
		if (parentBinding == unknownContainerBinding && newArgs == arguments) {
			return this;
		}
		return new CPPUnknownClassInstance(parentBinding, name, newArgs);
	}

	@Override
	public String toString() {
		return getName() + " <" + ASTTypeUtil.getTypeListString(arguments) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
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
				IType[] lhsArgs= getArguments();
				IType[] rhsArgs= rhs.getArguments();
				if (lhsArgs != rhsArgs) {
					if (lhsArgs == null || rhsArgs == null)
						return false;
				
					if (lhsArgs.length != rhsArgs.length)
						return false;
				
					for (int i= 0; i < lhsArgs.length; i++) {
						if (!CPPTemplates.isSameTemplateArgument(lhsArgs[i],rhsArgs[i])) 
							return false;
					}
				}
				final ICPPUnknownBinding lhsContainer = getUnknownContainerBinding();
				final ICPPUnknownBinding rhsContainer = rhs.getUnknownContainerBinding();
				if (lhsContainer instanceof IType && rhsContainer instanceof IType) {
					 return (((IType)lhsContainer).isSameType((IType) rhsContainer));
				}
			}
		}
		return false;
	}
}
