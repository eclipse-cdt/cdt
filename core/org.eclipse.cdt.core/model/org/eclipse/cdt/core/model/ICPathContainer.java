/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.core.model;

/**
 * @author alain
 */
public interface ICPathContainer {

	ICPathEntry[] getCPathEntries();
	
	/**
	 * Answers a readable description of this container
	 *
	 * @return String - a string description of the container
	 */
	String getDescription();
	
}
