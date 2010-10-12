/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * The instantiation of a function template.
 */
public class CPPFunctionInstance extends CPPFunctionSpecialization implements ICPPTemplateInstance {
	private ICPPTemplateArgument[] fArguments;

	public CPPFunctionInstance(ICPPFunction orig, IBinding owner, CPPTemplateParameterMap argMap, ICPPTemplateArgument[] args) {
		super(orig, owner, argMap);
		fArguments = args;
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}

	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(fArguments);
	}

	public ICPPTemplateArgument[] getTemplateArguments() {
		return fArguments;
	}

	public boolean isExplicitSpecialization() {
		if (getDefinition() != null)
			return true;
		IASTNode[] decls = getDeclarations();
		if (decls != null) {
			for (IASTNode decl : decls) {
				if (decl != null)
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		return getName() + " " + ASTTypeUtil.getArgumentListString(fArguments, true); //$NON-NLS-1$ 
	}
	
    @Override
	public boolean equals(Object obj) {
    	if( (obj instanceof ICPPTemplateInstance) && (obj instanceof ICPPFunction)){
    		final ICPPTemplateInstance inst = (ICPPTemplateInstance)obj;
			ICPPFunctionType ct1= ((ICPPFunction)getSpecializedBinding()).getType();
			ICPPFunctionType ct2= ((ICPPFunction)inst.getTemplateDefinition()).getType();
			if(!ct1.isSameType(ct2))
				return false;

			return CPPTemplates.haveSameArguments(this, inst);
    	}

    	return false;
    }
}
