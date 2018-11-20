/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
