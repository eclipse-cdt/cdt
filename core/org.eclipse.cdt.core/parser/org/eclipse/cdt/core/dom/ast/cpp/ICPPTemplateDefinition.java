/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;


/**
 * @author Doug Schaefer
 */
public interface ICPPTemplateDefinition extends ICPPBinding{

	/**
	 * Returns an array of the template parameters. 
	 * In the case of a specialization, the array will be empty,
	 * a partial specialization will have the specialized parameter list
	 * @return array of ICPPTemplateParameter
	 */
	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException;
	
	
}
