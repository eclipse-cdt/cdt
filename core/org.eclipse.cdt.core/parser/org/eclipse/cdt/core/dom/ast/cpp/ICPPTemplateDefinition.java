/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import java.util.List;

/**
 * @author Doug Schaefer
 */
public interface ICPPTemplateDefinition {

	/**
	 * Returns the list of template parameters. If this is a template
	 * specialization, the parameters will be substituted by the arguments
	 * determined in the specialization.
	 * 
	 * @return List of ICPPTemplateParameter, IType, or IASTExpression. The type
	 *         or expression are arguments in a specialization.
	 */
	public List getParameters();

	/**
	 * Returns whether this is a template specialization.
	 * 
	 * @return is this a template specialization
	 */
	public boolean isSpecialization();

	/**
	 * If this is a template specialization, this returns the template
	 * definition this is specializing. It returns null if this template is not
	 * a specialization.
	 * 
	 * @return
	 */
	public ICPPTemplateDefinition getSpecializes();
}
