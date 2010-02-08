/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.macros;

import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;

/**
 * This interface represents the status of a build macro operation 
 * 
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildMacroStatus extends ICdtVariableStatus {
	
	/**
	 * returns the context type used in the operation
	 * @return int
	 */
	public int getContextType();
	
	/**
	 * returns the context data used in the operation
	 * @return Object
	 */
	public Object getContextData();
	
	public String getMacroName();
	
}
