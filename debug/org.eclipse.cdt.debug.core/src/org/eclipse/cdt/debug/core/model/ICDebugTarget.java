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
}
