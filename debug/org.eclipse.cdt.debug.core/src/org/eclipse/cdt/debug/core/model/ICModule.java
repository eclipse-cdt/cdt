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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
 
/**
 * Represents a module in the process being debugged.
 * 
 * @since 3.0
 */
public interface ICModule extends ICDebugElement {

	/**
	 * Type constant which identifies executables.
	 */
	public static final int EXECUTABLE = 1;

	/**
	 * Type constant which identifies shared libraries.
	 */
	public static final int SHARED_LIBRARY = 2;

	/**
	 * Returns the type of this module.
	 * The returned value will be one of <code>EXECUTABLE</code>, 
	 * <code>SHARED_LIBRARY</code>, <code>CORE</code>.
	 * 
	 * @return the type of this module
	 */
	public int getType();

	/**
	 * Returns the name of this module.
	 * 
	 * @return the name of this module
	 */
	public String getName();

	/**
	 * Returns the image name of this module. The name may or may not 
	 * contain a full path.
	 * 
	 * @return the image name of this module
	 */
	public IPath getImageName();

	/**
	 * Returns the full path of the file from which symbols to be loaded.
	 * 
	 * @return the full path of the file from which symbols to be loaded
	 */
	public IPath getSymbolsFileName();

	/**
	 * Associate the specified file as a symbol provider for this module.
	 * If <code>null</code> is passed as a file name the internal symbols 
	 * search mechanism will be used.
	 * 
	 * @param symbolsFile the symbol provider for this module.
	 * @throws DebugException if this method fails. Reasons include:
	 */
	public void setSymbolsFileName( IPath symbolsFile ) throws DebugException;

	/**
	 * Returns the base address of this module.
	 * 
	 * @return the base address of this module
	 */
	public IAddress getBaseAddress();

	/**
	 * Returns the size of this module.
	 * 
	 * @return the size of this module
	 */
	public long getSize();

	/**
	 * Returns whether the symbols of this module are read.
	 * 
	 * @return whether the symbols of this module are read
	 */
	public boolean areSymbolsLoaded();

	/**
	 * Returns whether the module's symbols can be loaded or reloaded.
	 * 
	 * @return whether the module's symbols can be loaded or reloaded
	 */
	public boolean canLoadSymbols();

	/**
	 * Loads the module symbols from the specified file.
	 * 
	 * @throws DebugException if this method fails. Reasons include:
	 */
	public void loadSymbols() throws DebugException;

	/**
	 * Returns the name of the platform.
	 * 
	 * @return the name of the platform
	 */
	public String getPlatform();

	/**
	 * Returns whether this module is little endian.
	 * 
	 * @return whether this module is little endian
	 */
	public boolean isLittleEndian();

	/**
	 * Returns the address factory associated with this module.
	 * 
	 * @return the address factory
	 */
	public IAddressFactory getAddressFactory();

	/**
	 * Returns the CPU identifier.
	 * 
	 * @return the CPU identifier
	 */
	public String getCPU();
}
