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

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Represents a instantiation that cannot be performed because of dependent arguments or an unknown template.
 */
public class CPPDeferredClassInstance extends CPPUnknownClass implements ICPPDeferredClassInstance {
	
	private final ICPPTemplateArgument[] fArguments;
	private final ICPPClassTemplate fClassTemplate;

	public CPPDeferredClassInstance(ICPPClassTemplate template, ICPPTemplateArgument[] arguments) throws DOMException {
		super(template.getOwner(), new CPPASTName(template.getNameCharArray()));

		fArguments= arguments;
		fClassTemplate= template;
	}
	
	public ICPPClassTemplate getClassTemplate() {
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
			
			return CPPTemplates.haveSameArguments(this, rhs);
		} 
		return false;
	}

    @Override
	public int getKey() throws DOMException {
    	return getClassTemplate().getKey();
    }
    
	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
	}

	public ICPPTemplateArgument[] getTemplateArguments() {
		return fArguments;
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		return fClassTemplate;
	}

	public ObjectMap getArgumentMap() {
		return ObjectMap.EMPTY_MAP;
	}
	
	public CPPTemplateParameterMap getTemplateParameterMap() {
		return CPPTemplateParameterMap.EMPTY;
	}

	public IBinding getSpecializedBinding() {
		return getTemplateDefinition();
	}
	
	@Override
	public IScope getScope() throws DOMException {
		return fClassTemplate.getScope();
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this, true);
	}
}
