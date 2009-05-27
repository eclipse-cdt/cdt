/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.provisional;

import org.eclipse.core.runtime.IPath;

/**
 * Captures the filter that a user sets up in the import/export wizard, in order
 * to exclude certain files or directories from synchronization. All file system
 * paths are relative to the synchronization root.
 */
public interface ISynchronizeFilter {
	/**
	 * Return if the resource specified by relativePath is excluded or not.
	 * 
	 * @param relativePath
	 * @return
	 */
	public boolean isExcluded(IPath relativePath);
}
