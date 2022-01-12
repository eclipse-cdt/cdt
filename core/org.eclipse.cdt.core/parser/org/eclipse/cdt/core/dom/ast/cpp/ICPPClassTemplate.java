/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPClassTemplate extends ICPPClassType, ICPPPartiallySpecializable {
	/**
	 * Returns the partial specializations of this class template.
	 */
	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations();

	/**
	 * Returns a deferred instance that allows lookups within this class template.
	 * @since 5.1
	 */
	public ICPPTemplateInstance asDeferredInstance();
}
