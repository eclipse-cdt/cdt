/*******************************************************************************
 * Copyright (c) 2001, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * The interface is used to model, class or function templates and their partial or
 * explicit specializations.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITemplate {
	/**
	 * Returns the template parameter types.
	 * @return String
	 */
	String[] getTemplateParameterTypes();

	/**
	 * Returns the template arguments in a printable format. For templates that are no specialization,
	 * this will return the names of the template parameters.
	 * @since 5.2
	 */
	String[] getTemplateArguments();

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
	 */
	int getNumberOfTemplateParameters();
}
