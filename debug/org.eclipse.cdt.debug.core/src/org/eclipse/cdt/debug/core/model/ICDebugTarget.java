/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * C/C++ extension of <code>IDebugTarget</code>.
 */
public interface ICDebugTarget extends IDebugTarget,
									   IExecFileInfo,
									   IRestart,
									   IRunToLine,
									   IRunToAddress,
									   IJumpToLine,
									   IJumpToAddress,
									   IResumeWithoutSignal,
									   ICDebugElement,
									   IBreakpointTarget,
									   ISteppingModeTarget,
									   ITargetProperties {

	/**
	 * Returns whether this target is little endian.
	 * 
	 * @return whether this target is little endian
	 */
	public boolean isLittleEndian();
	
	/**
	 * Returns whether this target supports signals.
	 * 
	 * @return whether this target supports signals
	 * @throws DebugException if this method fails.
	 */
	public boolean hasSignals() throws DebugException;

	/**
	 * Returns the list of signals defined for this target.
	 * 
	 * @return the list of signals defined for this target
	 * @throws DebugException if this method fails.
	 */
	public ICSignal[] getSignals() throws DebugException;

	/**
	 * Returns the disassembly provider of this debug target.
	 * 
	 * @return the disassembly provider of this debug target
	 * @throws DebugException if this method fails.
	 */
	public IDisassembly getDisassembly() throws DebugException;

	/**
	 * Returns whether this target is a post mortem type.
	 * 
	 * @return whether this target is a post mortem type
	 */
	public boolean isPostMortem();

	/**
	 * Returns whether there are modules currently loaded in this debug target.
	 * 
	 * @return whether there are modules currently loaded in this debug target
	 * 
	 * @throws DebugException
	 */
	public boolean hasModules() throws DebugException;

	/**
	 * Returns the array of the currently loaded modules.
	 *  
	 * @return the array of the currently loaded modules
	 * @throws DebugException if this method fails. Reasons include:
	 */
	public ICModule[] getModules() throws DebugException;

	/**
	 * Load symbols for all currently loaded modules.
	 * 
	 * @throws DebugException if this method fails. Reasons include:
	 */
	public void loadSymbolsForAllModules() throws DebugException;

	/**
	 * Returns the list of descriptors of the target registers
	 * 
	 * @return the list register descriptors
	 * @throws DebugException if this method fails. Reasons include:
	 * 
	 * @since 3.0
	 */
	public IRegisterDescriptor[] getRegisterDescriptors() throws DebugException;

	/**
	 * Adds a new user-defined register group to this target
	 * 
	 * @param name the group name
	 * @param descriptors the list of registers to be grouped
	 * 
	 * @since 3.0
	 */
	public void addUserDefinedRegisterGroup( String name, IRegisterDescriptor[]  descriptors );

	/**
	 * Removes the given register group from the target
	 * 
	 * @param group a group to be removed
	 * 
	 * @since 3.0
	 */
	public void removeRegisterGroups( IRegisterGroup[] groups );
}
