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

package org.eclipse.cdt.debug.core.cdi.model;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * Describes the configuration of debug session.
 * 
 * @since Aug 6, 2002
 */
public interface ICDIRuntimeOptions extends ICDIObject {

	/**
	 * Program/Inferior arguments.
	 *
	 * @param args the string representing the arguments.
	 */
	void setArguments(String[] args) throws CDIException;

	/**
	 * Program/Inferior environment settings.
	 *
	 * @param props the new environment variable to add.
	 */
	void setEnvironment(Properties props) throws CDIException;

	/**
	 * Program/Inferior working directory.
	 *
	 * @param wd the working directory to start the program.
	 */
	void setWorkingDirectory(String wd) throws CDIException;
}
