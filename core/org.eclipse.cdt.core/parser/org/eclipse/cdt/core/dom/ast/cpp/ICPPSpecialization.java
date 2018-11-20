/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * For an instantiation of a class template, the members of that instantiation will be
 * specializations of the members of the original class template.
 * For an instantiation of a function template, the parameters will be specializations
 * of the parameters of the original function template.
 * Specializations can also be explicitly defined.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPSpecialization extends ICPPBinding {
	/**
	 * Return the binding that this specialization specializes.
	 * @return the original binding that this is a specialization of
	 */
	public IBinding getSpecializedBinding();

	/**
	 * Returns the mapping of template parameters to values.
	 * @since 5.1
	 */
	public ICPPTemplateParameterMap getTemplateParameterMap();
}
