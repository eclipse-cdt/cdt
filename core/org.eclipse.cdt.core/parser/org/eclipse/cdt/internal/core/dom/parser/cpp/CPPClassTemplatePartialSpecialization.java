/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * A partial class template specialization.
 */
public class CPPClassTemplatePartialSpecialization extends CPPClassTemplate 
		implements ICPPClassTemplatePartialSpecialization, ICPPSpecialization {

	private final ICPPTemplateArgument[] arguments;

	public CPPClassTemplatePartialSpecialization(ICPPASTTemplateId name, ICPPTemplateArgument[] arguments) {
		super(name);
		this.arguments= arguments;
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization#getPrimaryClassTemplate()
	 */
	@Override
	public ICPPClassTemplate getPrimaryClassTemplate() {
		ICPPASTTemplateId id = (ICPPASTTemplateId) getTemplateName();
		return (ICPPClassTemplate) id.getTemplateName().resolveBinding();
	}

	@Override
	public IBinding getSpecializedBinding() {
		return getPrimaryClassTemplate();
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		return CPPTemplates.createParameterMap(getPrimaryClassTemplate(), getTemplateArguments());
	}
	
	@Override
	public String toString() {
		return super.toString() + ASTTypeUtil.getArgumentListString(getTemplateArguments(), true);
	}
	
	@Override
	@Deprecated
	public ObjectMap getArgumentMap() {
		return CPPTemplates.getArgumentMap(getPrimaryClassTemplate(), getTemplateParameterMap());
	}
	
	@Override
	@Deprecated
	public IType[] getArguments() throws DOMException {
		return CPPTemplates.getArguments(getTemplateArguments());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
	 */
	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexBinding)
			return type.isSameType(this);

		if (type instanceof ICPPClassTemplatePartialSpecialization) {
			return isSamePartialClassSpecialization(this, (ICPPClassTemplatePartialSpecialization) type);
		}
		return false;
	}

	public static boolean isSamePartialClassSpecialization(
			ICPPClassTemplatePartialSpecialization lhs,
			ICPPClassTemplatePartialSpecialization rhs) {
		ICPPClassType ct1= lhs.getPrimaryClassTemplate();
		ICPPClassType ct2= rhs.getPrimaryClassTemplate();
		if(!ct1.isSameType(ct2))
			return false;

		ICPPTemplateArgument[] args1= lhs.getTemplateArguments();
		ICPPTemplateArgument[] args2= rhs.getTemplateArguments();
		if (args1.length != args2.length)
			return false;

		for (int i = 0; i < args2.length; i++) {
			if (!args1[i].isSameValue(args2[i])) 
				return false;
		}
		return true;
	}
}
