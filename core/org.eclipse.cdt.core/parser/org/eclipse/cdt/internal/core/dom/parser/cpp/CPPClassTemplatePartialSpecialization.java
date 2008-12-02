/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * A partial class template specialization.
 */
public class CPPClassTemplatePartialSpecialization extends CPPClassTemplate 
		implements ICPPClassTemplatePartialSpecialization, ICPPSpecialization {

	private ICPPTemplateArgument[] arguments;

	public CPPClassTemplatePartialSpecialization(ICPPASTTemplateId name) {
		super(name);
	}

	public ICPPTemplateArgument[] getTemplateArguments() throws DOMException {
		if (arguments == null) {
			arguments= CPPTemplates.createTemplateArgumentArray((ICPPASTTemplateId) getTemplateName());
		}
		return arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization#getPrimaryClassTemplate()
	 */
	public ICPPClassTemplate getPrimaryClassTemplate() {
		ICPPASTTemplateId id = (ICPPASTTemplateId) getTemplateName();
		return (ICPPClassTemplate) id.getTemplateName().resolveBinding();
	}

	public IBinding getSpecializedBinding() {
		return getPrimaryClassTemplate();
	}

	public ICPPTemplateParameterMap getTemplateParameterMap() {
		try {
			return CPPTemplates.createParameterMap(getPrimaryClassTemplate(), getTemplateArguments());
		} catch (DOMException e) {
			return CPPTemplateParameterMap.EMPTY;
		}
	}
	
	@Override
	protected ICPPDeferredClassInstance createDeferredInstance() throws DOMException {
		return new CPPDeferredClassInstance(this, getTemplateArguments(), getCompositeScope());
	}

	@Override
	public String toString() {
		try {
			return super.toString() + ASTTypeUtil.getArgumentListString(getTemplateArguments(), true);
		} catch (DOMException e) {
			return super.toString() + '<' + e.getProblem().toString() + '>';
		}
	}
	
	@Deprecated
	public ObjectMap getArgumentMap() {
		return CPPTemplates.getArgumentMap(getPrimaryClassTemplate(), getTemplateParameterMap());
	}
	
	@Deprecated
	public IType[] getArguments() throws DOMException {
		return CPPTemplates.getArguments(getTemplateArguments());
	}
}
