/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Enter type comment.
 * 
 * @since: Jan 15, 2003
 */
public interface ICSharedLibraryManager extends IAdaptable
{
	void sharedLibraryLoaded( ICDISharedLibrary library );

	void sharedLibraryUnloaded( ICDISharedLibrary library );
	
	void symbolsLoaded( ICDISharedLibrary library );

	ICSharedLibrary[] getSharedLibraries();

	void dispose();
}
