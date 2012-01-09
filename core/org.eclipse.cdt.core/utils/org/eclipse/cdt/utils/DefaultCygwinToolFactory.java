/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.IOException;

import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


public class DefaultCygwinToolFactory extends DefaultGnuToolFactory implements ICygwinToolsFactroy {

	
	/**
	 * 
	 */
	public DefaultCygwinToolFactory(ICExtension ext) {
		super(ext);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.CygwinToolsProvider#getCygPath()
	 */
	@Override
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
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("cygpath"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "cygpath"; //$NON-NLS-1$
		}
		return new Path(value);
	}

}
