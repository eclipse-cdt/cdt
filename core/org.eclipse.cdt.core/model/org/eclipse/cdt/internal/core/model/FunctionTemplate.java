/*******************************************************************************
 * Copyright (c) 2002, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionTemplate;

public class FunctionTemplate extends Function implements IFunctionTemplate {

	protected Template fTemplate;

	public FunctionTemplate(ICElement parent, String name) {
		super(parent, name, ICElement.C_TEMPLATE_FUNCTION);
		fTemplate = new Template(name);
	}

	/**
	 * Returns the parameterTypes.
	 * @see org.eclipse.cdt.core.model.ITemplate#getTemplateParameterTypes()
	 * @return String[]
	 */
	@Override
	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}

	@Override
	public String[] getTemplateArguments() {
		return fTemplate.getTemplateArguments();
	}

	/**
	 * Sets the template parameter types.
	 */
	public void setTemplateParameterTypes(String[] templateParameterTypes) {
		fTemplate.setTemplateInfo(templateParameterTypes, null);
	}

	/**
	 * @see org.eclipse.cdt.core.model.ITemplate#getNumberOfTemplateParameters()
	 */
	@Override
	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	/**
	 * @see org.eclipse.cdt.core.model.ITemplate#getTemplateSignature()
	 */
	/*
	 * The signature in the outline view will be:
	 * The class X followed by its template parameters,
	 * then the scope resolution, then the function name,
	 * followed by its template parameters, folowed by its
	 * normal parameter list, then a colon then the function's
	 * return type.
	 */
	@Override
	public String getTemplateSignature() throws CModelException {
		StringBuilder sig = new StringBuilder(fTemplate.getTemplateSignature());
		sig.append(this.getParameterClause());
		if (isConst()) {
			sig.append(" const"); //$NON-NLS-1$
		}
		if (isVolatile()) {
			sig.append(" volatile"); //$NON-NLS-1$
		}
		if ((this.getReturnType() != null) && (this.getReturnType().length() > 0)) {
			sig.append(" : "); //$NON-NLS-1$
			sig.append(this.getReturnType());
		}
		return sig.toString();
	}

}
