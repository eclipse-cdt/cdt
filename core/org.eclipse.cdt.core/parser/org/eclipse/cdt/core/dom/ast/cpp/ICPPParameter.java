/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IValue;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPParameter extends IParameter, ICPPVariable {
	/**
	 * @since 5.2
	 */
	ICPPParameter[] EMPTY_CPPPARAMETER_ARRAY = {};

	/**
	 * if there is a default value or not.
	 */
	public boolean hasDefaultValue();

	/**
	 * Returns the default value of this parameter if it has one,
	 * or null otherwise.
	 * @since 5.7
	 */
	public IValue getDefaultValue();

	/**
	 * Returns whether this parameter is a parameter pack.
	 * @since 5.2
	 */
	public boolean isParameterPack();
}
