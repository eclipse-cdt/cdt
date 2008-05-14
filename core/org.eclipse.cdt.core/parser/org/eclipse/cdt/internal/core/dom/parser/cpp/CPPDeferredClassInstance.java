/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Represents a partially instantiated class template, where instance arguments contain at least one
 * template type parameter.
 *
 * @author aniefer
 */
public class CPPDeferredClassInstance extends CPPUnknownClass implements ICPPDeferredClassInstance {
	
	private IType[] fArguments;
	private ObjectMap fArgmap;
	private ICPPClassTemplate fClassTemplate;

	public CPPDeferredClassInstance(ICPPClassTemplate orig,	ObjectMap argMap, IType[] arguments) {
		super(orig);
		fArgmap= argMap;
		fArguments= arguments;
		fClassTemplate= orig;
	}
	
	private ICPPClassTemplate getClassTemplate() {
		return (ICPPClassTemplate) getSpecializedBinding();
	}

	 @Override
	public CPPDeferredClassInstance clone() {
		 CPPDeferredClassInstance cloned= (CPPDeferredClassInstance) super.clone();
		 return cloned;
    }

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;

		if (type instanceof ITypedef) 
			return type.isSameType(this);

		// allow some fuzziness here.
		ICPPClassTemplate classTemplate = getClassTemplate();
		if (type instanceof ICPPDeferredClassInstance) {
			final ICPPDeferredClassInstance rhs = (ICPPDeferredClassInstance) type;
			if (!classTemplate.isSameType((IType) rhs.getSpecializedBinding())) 
				return false;
			
			IType[] lhsArgs= getArguments();
			IType[] rhsArgs= rhs.getArguments();
			if (lhsArgs != rhsArgs) {
				if (lhsArgs == null || rhsArgs == null)
					return false;

				if (lhsArgs.length != rhsArgs.length)
					return false;

				for (int i= 0; i < lhsArgs.length; i++) {
					if (!lhsArgs[i].isSameType(rhsArgs[i])) 
						return false;
				}
			}
			return true;
		} 
		return false;
	}

    @Override
	public int getKey() throws DOMException {
    	return getClassTemplate().getKey();
    }

	public IType[] getArguments() {
		return fArguments;
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		return fClassTemplate;
	}

	public ObjectMap getArgumentMap() {
		return fArgmap;
	}

	@Override
	public IBinding resolvePartially(ICPPUnknownBinding parentBinding, ObjectMap argMap, ICPPScope instantiationScope) {
		IType[] arguments = getArguments();
		IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap, instantiationScope);

		boolean changed= arguments != newArgs;
		ICPPClassTemplate classTemplate = getClassTemplate();
		if (argMap.containsKey(classTemplate)) {
			classTemplate = (ICPPClassTemplate) argMap.get(classTemplate);
			changed= true;
		}

		if (!changed) {
			return this;
		}
		return ((ICPPInternalTemplateInstantiator) classTemplate).instantiate(newArgs);
	}

	public IBinding getSpecializedBinding() {
		return getTemplateDefinition();
	}
	
	@Override
	public IScope getScope() throws DOMException {
		return fClassTemplate.getScope();
	}
}
