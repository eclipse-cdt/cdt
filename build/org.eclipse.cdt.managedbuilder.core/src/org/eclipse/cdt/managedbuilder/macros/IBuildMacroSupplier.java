/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.macros;

/**
 * 
 * @since 3.0
 */
public interface IBuildMacroSupplier {
	
	/**
	 * 
	 * @param macroName macro name
	 * @param contextType context type
	 * @param contextData context data
	 * @return IBuildMacro
	 */
	public IBuildMacro getMacro(String macroName,
					int contextType, 
					Object contextData);
	
	/**
	 * 
	 * @param contextType context type
	 * @param contextData context data
	 * @return IBuildMacro[]
	 */
	public IBuildMacro[] getMacros(int contextType, 
			Object contextData);
}
