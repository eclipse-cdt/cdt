/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.cdtvariables;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;

/**
 * 
 * @since 3.0
 */
public interface ICdtVariableSupplier {
	
	/**
	 * @param macroName macro name
	 * @return IBuildMacro
	 */
	public ICdtVariable getVariable(String macroName,
			IVariableContextInfo context);
	
	/**
	 * @return IBuildMacro[]
	 */
	public ICdtVariable[] getVariables(IVariableContextInfo context);
}
