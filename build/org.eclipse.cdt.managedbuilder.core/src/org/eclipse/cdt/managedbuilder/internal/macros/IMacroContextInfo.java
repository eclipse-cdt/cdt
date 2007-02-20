/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public interface IMacroContextInfo extends IVariableContextInfo{
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
