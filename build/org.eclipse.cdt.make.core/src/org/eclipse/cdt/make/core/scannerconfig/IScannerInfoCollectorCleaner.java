/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.core.resources.IResource;

/**
 * Utility functions some collectors may need
 *
 * @author vhirsl
 */
public interface IScannerInfoCollectorCleaner {
	/**
	 * Delete all discovered paths for the resource
	 */
	public void deleteAllPaths(IResource resource);

	/**
	 * Delete all discovered symbols for the resource
	 */
	public void deleteAllSymbols(IResource resource);

	/**
	 * Delete a specific include path
	 */
	public void deletePath(IResource resource, String path);

	/**
	 * Delete a specific symbol definition
	 */
	public void deleteSymbol(IResource resource, String symbol);

	/**
	 * Delete all discovered scanner info for the resource
	 */
	public void deleteAll(IResource resource);
}
