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
