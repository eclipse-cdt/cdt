/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
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
package org.eclipse.cdt.utils.cdtvariables;

/**
 * This interface represents the context information.
 *
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IVariableContextInfo {

	/**
	 * Returns suppliers to be used for this context
	 *
	 * @return IBuildMacroSupplier[]
	 */
	public ICdtVariableSupplier[] getSuppliers();

	/**
	 * Returns context info for the next lower-precedence context
	 *
	 * @return IMacroContextInfo
	 */
	public IVariableContextInfo getNext();

	@Override
	public boolean equals(Object otherInfo);
}
