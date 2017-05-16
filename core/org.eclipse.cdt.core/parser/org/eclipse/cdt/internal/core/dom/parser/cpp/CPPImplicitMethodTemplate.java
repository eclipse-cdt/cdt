/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;

/**
 * Binding for implicit method templates.
 * 
 * Used for the function call operator and pointer-to-function conversion operator
 * of a generic lambda.
 */
public class CPPImplicitMethodTemplate extends CPPImplicitMethod implements ICPPFunctionTemplate {
	private ICPPTemplateParameter[] fTemplateParameters;
	
	public CPPImplicitMethodTemplate(ICPPTemplateParameter[] templateParameters, ICPPClassScope scope, 
			char[] name, ICPPFunctionType type, ICPPParameter[] params, boolean isConstexpr) {
		super(scope, name, type, params, isConstexpr);
		fTemplateParameters = templateParameters;
		for (ICPPTemplateParameter parameter : templateParameters) {
			if (parameter instanceof CPPImplicitTemplateTypeParameter) {
				((CPPImplicitTemplateTypeParameter) parameter).setContainingTemplate(this);
			}
		}
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return fTemplateParameters;
	}
}
