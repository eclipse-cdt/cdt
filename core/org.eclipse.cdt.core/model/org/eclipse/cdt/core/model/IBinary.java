/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;


/**
 * Represents a Binary file, for example an ELF excutable.
 * An ELF parser will inspect the binary.
 */
public interface IBinary extends ICElement, IParent, IOpenable {
	/**
	 * Return whether the file was compiling with debug symbols.
	 */
	public boolean hasDebug();

	public boolean isExecutable();

	public boolean isObject();

	public boolean isSharedLib();
	
	public boolean isCore();

	public String [] getNeededSharedLibs();

	public String getSoname();

	public String getCPU();

	public long getText();

	public long getData();

	public long getBSS();
	
	public boolean isLittleEndian();

	//public IAddressFactory getAddressFactory();

}
