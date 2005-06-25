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

import org.eclipse.cdt.core.model.ITemplate;

public class Template implements ITemplate {

	protected static final String[] fgEmptyList= new String[] {};
	protected String[] templateParameterTypes;
	protected String fName;
	
	public Template(String name) {
		fName = name;
		templateParameterTypes= fgEmptyList;
	}
	/**
	 * Returns the parameterTypes.
	 * @see org.eclipse.cdt.core.model.ITemplate#getParameters()
	 * @return String[]
	 */
	public String[] getTemplateParameterTypes() {
		return templateParameterTypes;
	}

	/**
	 * Sets the fParameterTypes.
	 * @param fParameterTypes The fParameterTypes to set
	 */
	public void setTemplateParameterTypes(String[] templateParameterTypes) {
		this.templateParameterTypes = templateParameterTypes;
	}

	/**
	 * @see org.eclipse.cdt.core.model.ITemplate#getNumberOfTemplateParameters()
	 */
	public int getNumberOfTemplateParameters() {
		return templateParameterTypes == null ? 0 : templateParameterTypes.length;
	}

	/**
	 * @see org.eclipse.cdt.core.model.ITemplate#getTemplateSignature()
	 */	
	public String getTemplateSignature() {
		StringBuffer sig = new StringBuffer(fName);
		if(getNumberOfTemplateParameters() > 0){
			sig.append("<"); //$NON-NLS-1$
			String[] paramTypes = getTemplateParameterTypes();
			int i = 0;
			sig.append(paramTypes[i++]);
			while (i < paramTypes.length){
				sig.append(", "); //$NON-NLS-1$
				sig.append(paramTypes[i++]);
			}
			sig.append(">"); //$NON-NLS-1$
		}
		else{
			sig.append("<>"); //$NON-NLS-1$
		}
		return sig.toString();
	}

}
