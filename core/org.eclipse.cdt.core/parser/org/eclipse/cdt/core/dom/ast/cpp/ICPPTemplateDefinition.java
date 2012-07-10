/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Base interface for all template definitions including explicit (partial) specializations.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPTemplateDefinition extends ICPPBinding {
	/**
	 * Returns an array of the template parameters. 
	 * In the case of a specialization, the array will be empty,
	 * a partial specialization will have the specialized parameter list
	 * @return array of ICPPTemplateParameter
	 */
	public ICPPTemplateParameter[] getTemplateParameters();
}
