package org.eclipse.cdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITemplate;

public class FunctionTemplate extends FunctionDeclaration implements ITemplate{
	
	protected String[] templateParameterTypes;
	
	public FunctionTemplate(ICElement parent, String name) {
		super(parent, name, ICElement.C_TEMPLATE_FUNCTION);
		templateParameterTypes= fgEmptyStrings;
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
	/*
	 * The signature in the outline view will be: 
	 * The class X followed by its template parameters, 
	 * then the scope resolution, then the function name, 
	 * followed by its template parameters, folowed by its 
	 * normal parameter list, then a colon then the function's 
	 * return type.
	 */	
	public String getTemplateSignature() throws CModelException {
		StringBuffer sig = new StringBuffer(getElementName());
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

}
