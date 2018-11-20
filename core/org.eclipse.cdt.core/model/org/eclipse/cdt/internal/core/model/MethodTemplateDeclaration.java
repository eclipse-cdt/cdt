/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Qnx Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodTemplateDeclaration;

public class MethodTemplateDeclaration extends MethodDeclaration implements IMethodTemplateDeclaration {

	protected Template fTemplate;

	public MethodTemplateDeclaration(ICElement parent, String name) {
		super(parent, name, ICElement.C_TEMPLATE_METHOD_DECLARATION);
		fTemplate = new Template(name);
	}

	@Override
	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}

	@Override
	public String[] getTemplateArguments() {
		return fTemplate.getTemplateArguments();
	}

	@Override
	public String getTemplateSignature() throws CModelException {
		StringBuilder sig = new StringBuilder(fTemplate.getTemplateSignature());
		sig.append(this.getParameterClause());
		if (isConst())
			sig.append(" const"); //$NON-NLS-1$
		if (isVolatile())
			sig.append(" volatile"); //$NON-NLS-1$

		if ((this.getReturnType() != null) && (this.getReturnType().length() > 0)) {
			sig.append(" : "); //$NON-NLS-1$
			sig.append(this.getReturnType());
		}

		return sig.toString();

	}

	@Override
	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	/**
	 * Sets the fParameterTypes.
	 * @param templateParameterTypes The template parameter types to set
	 */
	public void setTemplateParameterTypes(String[] templateParameterTypes) {
		fTemplate.setTemplateInfo(templateParameterTypes, null);
	}

}
