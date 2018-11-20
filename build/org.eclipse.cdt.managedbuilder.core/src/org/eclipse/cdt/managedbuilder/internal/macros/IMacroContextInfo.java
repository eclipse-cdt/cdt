/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;

/**
 * This interface represents the context information.
 *
 * @since 3.0
 */
public interface IMacroContextInfo extends IVariableContextInfo {
	/**
	 * returns the context type
	 *
	 * @return int
	 */
	public int getContextType();

	/**
	 * returns the context data
	 *
	 * @return Object
	 */
	public Object getContextData();
}
