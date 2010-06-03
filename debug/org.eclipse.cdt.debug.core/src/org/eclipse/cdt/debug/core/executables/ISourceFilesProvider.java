/*******************************************************************************
 * Copyright (c) 2008, 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.executables;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * ISourceFileProvider supplies a list of source files used by a given Executable.
 * 
 * @author Ken Ryall
 * @since 6.0
 * 
 */

public interface ISourceFilesProvider {
	
	public static final int LOW_PRIORITY = 25;
	public static final int NORMAL_PRIORITY = 50;
	public static final int HIGH_PRIORITY = 75;
	
	/**
	 * Gets the priority to be used for this executable.
	 * The priority is used by the Executables Manager when multiple ISourceFilesProviders are available.
	 * ISourceFilesProvider.getSourceFiles will be called for each one in priority order and will use the
	 * first one that returns a non empty result.
	 * 
	 * @param executable
	 * @return the priority level to be used for this ISourceFilesProvider
	 */
	int getPriority(Executable executable);
	
	/**
	 * Returns a list of source files used by an executable.
	 * @param executable
	 * @param monitor
	 * @return The list of source files for the executable. These may be file name, full or partial paths.
	 */
	String[] getSourceFiles(Executable executable, IProgressMonitor monitor);

}
