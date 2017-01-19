/*******************************************************************************
 * Copyright (c) 2014 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - First Implementation and API (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.core.runtime.CoreException;

/**
 * Describes a register Group
 */
public interface IRegisterGroupDescriptor {
	/**
	 * @return the register group's name
	 */
	public String getName();

	/**
	 * @return the enabled state
	 */
	public boolean isEnabled();

	/**
	 * @return the registers associated to this group
	 * @throws CoreException
	 */
	public IRegisterDescriptor[] getChildren() throws CoreException;
	
	/**
	 * The id of the container this register group belongs to.
	 * If null, the register group applies to the entire launch, 
	 * otherwise it applies only to a given core, or process, within the launch. 
	 */
	default String getContainerId() {
	    return null;
	};
}
