/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	 * a partial specialization will have the specialized parameter list.
	 *
	 * @return an array of template parameters
	 */
	public ICPPTemplateParameter[] getTemplateParameters();
}
