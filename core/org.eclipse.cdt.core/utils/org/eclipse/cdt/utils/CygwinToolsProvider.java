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
package org.eclipse.cdt.utils;

import java.io.IOException;

import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * CygwinToolsProvider 
*/
public class CygwinToolsProvider extends ToolsProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.ICygwinToolsProvider#getCygPath()
	 */
	public CygPath getCygPath() {
		IPath cygPathPath = getCygPathPath();
		CygPath cygpath = null;
		if (cygPathPath != null && !cygPathPath.isEmpty()) {
			try {
				cygpath = new CygPath(cygPathPath.toOSString());
			} catch (IOException e1) {
			}
		}
		return cygpath;
	}

	protected IPath getCygPathPath() {
		ICExtensionReference ref = getExtensionReference();
		String value =  ref.getExtensionData("cygpath"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "cygpath"; //$NON-NLS-1$
		}
		return new Path(value);
	}

}
