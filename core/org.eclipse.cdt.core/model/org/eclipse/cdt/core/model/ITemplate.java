package org.eclipse.cdt.core.model;
/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
public interface ITemplate extends IDeclaration {
	/**
	 * Returns the template parameter types.
	 * @return String
	 */
	String[] getTemplateParameterTypes();
	/**
	 * Sets the template parameter types.
	 * @param paramTypes
	 */
	void setTemplateParameterTypes(String[] templateParameterTypes);
	/**
	 * Returns the template signature
	 * @return String
	 */
	String getTemplateSignature();
	/**
	 * Returns the number of template parameters
	 * @return int
	 */
	int getNumberOfTemplateParameters();
}
