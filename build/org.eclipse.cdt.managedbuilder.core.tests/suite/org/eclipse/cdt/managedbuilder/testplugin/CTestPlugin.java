/*******************************************************************************
 * Copyright (c) 2004, 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.testplugin;

import java.io.File;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

public class CTestPlugin extends Plugin {

	private static CTestPlugin fgDefault;

	public CTestPlugin() {
		fgDefault = this;
	}

	public static CTestPlugin getDefault() {
		return fgDefault;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static File getFileInPlugin(IPath path) {
		try {
			URL url = getDefault().find(path, null);
			if (url != null) {
				url = Platform.asLocalURL(url);
				return new File(url.getFile());
			}
			return null;

		} catch (Exception e) {
			return null;
		}
	}
}
