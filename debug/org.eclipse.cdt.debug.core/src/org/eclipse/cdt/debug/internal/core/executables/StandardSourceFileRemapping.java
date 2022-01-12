/*******************************************************************************
 * Copyright (c) 2008, 2015 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		srcFinder = binary.getAdapter(ISourceFinder.class);
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
	public void finalize() {
		srcFinder.dispose();
	}
}