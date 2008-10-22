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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
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

	public ICPPTemplateArgument[] getTemplateArguments() {
		createArguments();
		return arguments;
	}

	private void createArguments() {
		if (arguments == null) {
			arguments= CPPTemplates.convert(
					CPPTemplates.createTemplateArgumentArray((ICPPASTTemplateId) getTemplateName()));
		}
	}

	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
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

	public CPPTemplateParameterMap getTemplateParameterMap() {
		CPPTemplateParameterMap result= new CPPTemplateParameterMap();
		try {
			ICPPTemplateParameter[] params = getPrimaryClassTemplate().getTemplateParameters();
			ICPPTemplateArgument[] args= getTemplateArguments();
			int len= Math.min(params.length, args.length);
			for (int i = 0; i < len; i++) {
				result.put(params[i], args[i]);
			}
		} catch (DOMException e) {
		}
		return result;
	}

	@Deprecated
	public ObjectMap getArgumentMap() {
		return CPPTemplates.getArgumentMap(getPrimaryClassTemplate(), getTemplateParameterMap());
	}
}
