/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
