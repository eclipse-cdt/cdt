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

package org.eclipse.cdt.debug.internal.core.executables;

import org.eclipse.cdt.core.ISourceFinder;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.executables.ISourceFileRemapping;
import org.eclipse.core.runtime.IPath;

public class StandardSourceFileRemapping implements ISourceFileRemapping {

	ISourceFinder srcFinder;

	public StandardSourceFileRemapping(IBinary binary) {
		srcFinder = (ISourceFinder) binary.getAdapter(ISourceFinder.class);
	}
	
	@Override
	public String remapSourceFile(IPath executable, String filePath) {
		if (srcFinder != null) {
			String mappedPath = srcFinder.toLocalPath(filePath);
			if (mappedPath != null) {
				return mappedPath;
			}
		}
		return filePath;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize(){
		srcFinder.dispose();
	}
}