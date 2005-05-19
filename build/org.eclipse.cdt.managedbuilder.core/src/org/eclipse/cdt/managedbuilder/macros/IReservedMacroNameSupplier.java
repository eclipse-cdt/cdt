/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.macros;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;

/**
 * This interface is to be implemented by the tool-integrator to specify to the MBS
 * the reserved builder variable names
 *  
 * @since 3.0
 */
public interface IReservedMacroNameSupplier{

	/**
	 * @return true if the given macro name is reserved by the builder or the makefile generator
	 */
	boolean isReservedName(String macroName, IConfiguration configuration);
}

