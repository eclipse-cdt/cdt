/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi.model;

import java.io.File;

/**
 * 
 * Represents a shared library which has been loaded into 
 * the debug target.
 * 
 * @since Jul 8, 2002
 */
public interface ICSharedLibrary extends ICObject
{
	/**
	 * Returns the shared library file.
	 * 
	 * @return the shared library file
	 */
	File getFile();
}
