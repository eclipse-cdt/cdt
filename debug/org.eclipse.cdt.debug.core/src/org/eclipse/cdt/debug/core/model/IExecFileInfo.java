/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.resources.IFile;

/**
 * Provides access to executable file information.
 * 
 * @since: Nov 1, 2002
 */
public interface IExecFileInfo
{
	public IFile getExecFile();

	public boolean isLittleEndian();
	
	public IGlobalVariable[] getGlobals();	
}
