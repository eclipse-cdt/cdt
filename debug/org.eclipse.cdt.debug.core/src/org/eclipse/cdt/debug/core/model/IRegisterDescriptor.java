/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.core.model; 

/**
 * Describes a register.
 * 
 * @since 3.0
 */
public interface IRegisterDescriptor {

	/**
	 * Returns the regiser's name
	 * 
	 * @return the register's name
	 */
	public String getName();

	/**
	 * Returns the name of the hardware register group this register belongs to
	 * 
	 * @return the name of the hardware register group
	 */
	public String getGroupName();
}
