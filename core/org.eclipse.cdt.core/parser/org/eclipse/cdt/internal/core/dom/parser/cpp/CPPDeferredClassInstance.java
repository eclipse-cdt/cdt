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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
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
	private ICPPScope fUnknownScope;

	public CPPDeferredClassInstance(ICPPClassTemplate orig,	ObjectMap argMap, IType[] arguments) {
		super(orig);
		fArgmap= argMap;
		fArguments= arguments;
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

		// allow some fuzziness here.
		ICPPClassTemplate classTemplate = getClassTemplate();
		if (type instanceof ICPPDeferredClassInstance) {
			ICPPClassTemplate typeClass =
				(ICPPClassTemplate) ((ICPPDeferredTemplateInstance) type).getSpecializedBinding();
			return typeClass == classTemplate;
		} else if (type instanceof ICPPClassTemplate && classTemplate == type) {
			return true;
		} else if (type instanceof ICPPTemplateInstance &&
				((ICPPTemplateInstance) type).getTemplateDefinition() == classTemplate) {
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
		return (ICPPTemplateDefinition) scopeBinding;
	}

	public ObjectMap getArgumentMap() {
		return fArgmap;
	}

	@Override
	public IBinding resolvePartially(ICPPUnknownBinding parentBinding, ObjectMap argMap) {
		IType[] arguments = getArguments();
		IType[] newArgs = new IType[arguments.length];
		int size = arguments.length;
		for (int i = 0; i < size; i++) {
			newArgs[i] = CPPTemplates.instantiateType(arguments[i], argMap);
		}

		ICPPClassTemplate classTemplate = getClassTemplate();
		if (argMap.containsKey(classTemplate)) {
			classTemplate = (ICPPClassTemplate) argMap.get(classTemplate);
		}

		return ((ICPPInternalTemplateInstantiator) classTemplate).instantiate(newArgs);
	}

	public IBinding getSpecializedBinding() {
		return getTemplateDefinition();
	}

	@Override
	public ICPPScope getUnknownScope() throws DOMException {
		if (fUnknownScope != null)
			return fUnknownScope;
		
		final ICPPClassTemplate classTemplate = getClassTemplate();
		if (classTemplate.getPartialSpecializations().length == 0) {
			if (fArgmap == null) {
				return fUnknownScope= (ICPPScope) classTemplate.getCompositeScope();
			}
			return fUnknownScope= new CPPClassSpecializationScope(this);
		}
		return super.getUnknownScope();
	}
}
