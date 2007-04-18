/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.browser;

/**
 * Provide function related information.
 * 
 * <p>
 * Not intended to be implemented by clients.
 * </p>
 * 
 * @since 4.0
 */
public interface IFunctionInfo {

	/**
	 * @return the function parameter types
	 */
	public String[] getParameters();
	/**
	 * @return the function return type
	 */
	public String getReturnType();
	
}
