/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
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

public interface IExecutableImporter {

	/**
	 * @since 6.0
	 */
	public static final int LOW_PRIORITY = 25;
	/**
	 * @since 6.0
	 */
	public static final int NORMAL_PRIORITY = 50;
	/**
	 * @since 6.0
	 */
	public static final int HIGH_PRIORITY = 75;
	
	/**
	 * Gets the priority to be used when importing these executables.
	 * The priority is used by the Executables Manager when multiple IExecutableImporters are available.
	 * IExecutableImporter.importExecutables will be called for each one in priority order and will 
	 * stop with the first one that returns TRUE.
	 * 
	 * @param executable
	 * @return the priority level to be used for this ISourceFilesProvider
	 * @since 6.0
	 */
	int getPriority(String[] fileNames);

	/**
	 * @since 6.0
	 */
	public boolean importExecutables(String[] fileNames, IProgressMonitor monitor);

}