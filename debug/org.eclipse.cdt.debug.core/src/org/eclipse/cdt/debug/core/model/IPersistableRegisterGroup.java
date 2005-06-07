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
	public void initializeFromMemento( String memento ) throws CoreException;

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
	public void setRegisterDescriptors( IRegisterDescriptor[] registerDescriptors );
}
