/*******************************************************************************
 * Copyright (c) 2005 QnX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}

	public String getTemplateSignature() throws CModelException {
		StringBuffer sig = new StringBuffer(fTemplate.getTemplateSignature());
		sig.append(this.getParameterClause());
		if(isConst())
			sig.append(" const"); //$NON-NLS-1$
		if(isVolatile())
			sig.append(" volatile"); //$NON-NLS-1$

		if((this.getReturnType() != null) && (this.getReturnType().length() > 0)){ 
			sig.append(" : "); //$NON-NLS-1$
			sig.append(this.getReturnType());
		}
		
		return sig.toString();

	}

	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	/**
	 * Sets the fParameterTypes.
	 * @param fParameterTypes The fParameterTypes to set
	 */
	public void setTemplateParameterTypes(String[] templateParameterTypes) {
		fTemplate.setTemplateParameterTypes(templateParameterTypes);
	}

}
