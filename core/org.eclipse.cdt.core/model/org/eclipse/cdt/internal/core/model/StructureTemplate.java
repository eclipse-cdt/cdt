/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructureTemplate;

public class StructureTemplate extends Structure implements IStructureTemplate {

	protected Template fTemplate;

	public StructureTemplate(ICElement parent, int kind, String name) {
		super(parent, kind, name);
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
		return  fTemplate.getTemplateArguments();
	}

	/**
	 * Sets the fParameterTypes.
	 * @param templateParameterTypes The template parameter types to set
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
	@Override
	public String getTemplateSignature() {
		return fTemplate.getTemplateSignature();
	}

	@Override
	public void getHandleMemento(StringBuilder buff) {
		super.getHandleMemento(buff);
		if (fTemplate.getNumberOfTemplateParameters() > 0) {
			final String[] parameterTypes= fTemplate.getTemplateParameterTypes();
			for (String parameterType : parameterTypes) {
				buff.append(CEM_PARAMETER);
				escapeMementoName(buff, parameterType);
			}
		}
	}

}
