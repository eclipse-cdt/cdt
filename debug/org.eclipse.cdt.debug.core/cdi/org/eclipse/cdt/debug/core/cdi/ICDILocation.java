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
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.core.IAddress;


/**
 * 
 * Represents a location in the debuggable program.
 * 
 * @since Jul 9, 2002
 */
public interface ICDILocation {

	/**
	 * Returns the address of this location.
	 * 
	 * @return the address of this location
	 */
	IAddress getAddress();
	
	/**
	 * Returns the source file of this location or <code>null</code>
	 * if the source file is unknown.
	 *  
	 * @return the source file of this location
	 */
	String getFile();

	/**
	 * Returns the function of this location or <code>null</code>
	 * if the function is unknown.
	 *  
	 * @return the function of this location
	 */
	String getFunction();

	/**
	 * Returns the line number of this location or <code>null</code>
	 * if the line number is unknown.
	 *  
	 * @return the line number of this location
	 */
	int getLineNumber();
	
	/**
	 * Return true if the both location refers to the same
	 * place.
	 */
	boolean equals(ICDILocation location);

}
