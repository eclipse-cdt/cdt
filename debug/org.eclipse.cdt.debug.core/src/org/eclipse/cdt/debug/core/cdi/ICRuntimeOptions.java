/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

import java.util.Properties;

/**
 * Describes the configuration of debug session.
 * 
 * @since Aug 6, 2002
 */
public interface ICRuntimeOptions {

	/**
	 * Program/Inferior arguments.
	 *
	 * @param args the string representing the arguments.
	 */
	void setArguments(String args);

	/**
	 * Program/Inferior environment settings.
	 *
	 * @param props the new environment variable to add.
	 */
	void setEnvironment(Properties props);

	/**
	 * Program/Inferior working directory.
	 *
	 * @param wd the working directory to start the program.
	 */
	void setWorkingDirectory(String wd);
}
