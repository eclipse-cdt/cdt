/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Jan 15, 2003
 */
public interface ICSharedLibraryManager extends ICUpdateManager, IAdaptable
{
	ICSharedLibrary[] getSharedLibraries();

	void loadSymbolsForAll() throws DebugException;

	void loadSymbols( ICSharedLibrary[] libraries ) throws DebugException;

	void dispose();
}
