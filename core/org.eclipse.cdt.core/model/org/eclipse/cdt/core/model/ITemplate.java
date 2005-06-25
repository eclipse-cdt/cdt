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
public interface ITemplate {
	/**
	 * Returns the template parameter types.
	 * @return String
	 */
	String[] getTemplateParameterTypes();

	/**
	 * Returns the template signature
	 * The signature depends on the type of template. 
	 * If it is a template of a structure or a variable, it will include the structure name 
	 * and the list of parameters. If it is a template of a method or a function,  it might 
	 * include the class name with its template parameters (if any), as well as the function/method
	 * name, its  template parameters, followed by its normal parameters.
	 * @return String
	 * @throws CModelException
	 */	
	String getTemplateSignature() throws CModelException;

	/**
	 * Returns the number of template parameters
	 * @return int
	 */
	int getNumberOfTemplateParameters();
}
