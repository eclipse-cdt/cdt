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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * A register group to be persisted and restored.
 * To be used for the user-defined register groups.
 *
 * @since 3.0
 */
public interface IPersistableRegisterGroup extends IRegisterGroup {

	/**
	 * Returns a memento that can be used to reconstruct this group
	 *
	 * @return a memento that can be used to reconstruct this group
	 * @exception CoreException if unable to construct a memento
	 */
	public String getMemento() throws CoreException;

	/**
	 * Initializes this group based on the given memento.
	 *
	 * @param memento a memento to initialize this group
	 * @exception CoreException on failure to initialize
	 */
	public void initializeFromMemento(String memento) throws CoreException;

	/**
	 * Returns the array of group's register descriptors.
	 *
	 * @return the array of group's register descriptors
	 */
	public IRegisterDescriptor[] getRegisterDescriptors();

	/**
	 * Replaces the group register descriptors by the specified descriptors.
	 *
	 * @param the array of register descriptors
	 */
	public void setRegisterDescriptors(IRegisterDescriptor[] registerDescriptors);
}
