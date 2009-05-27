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

import java.util.List;

import org.eclipse.core.runtime.IPath;

public class SynchronizeFilter implements ISynchronizeFilter {
	/**
	 * the paths of synchronize elements
	 */
	private List<IPath> paths;

	public SynchronizeFilter(List<IPath> sycnhronizeRelativePaths) {
		super();
		this.paths = sycnhronizeRelativePaths;
	}

	public boolean isExcluded(IPath relativePath) {
		if(paths.contains(relativePath)){
			return false;
		}else{
			return true;
		}
	}

}