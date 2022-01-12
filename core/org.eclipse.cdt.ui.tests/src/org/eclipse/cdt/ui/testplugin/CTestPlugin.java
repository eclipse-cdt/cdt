/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.swt.widgets.Display;

public class CTestPlugin extends Plugin {
	public static String PLUGIN_ID = "org.eclipse.cdt.ui.tests"; //$NON-NLS-1$
	private static CTestPlugin fgDefault;

	public CTestPlugin() {
		super();
		fgDefault = this;
	}

	public static CTestPlugin getDefault() {
		return fgDefault;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static void enableAutobuild(boolean enable) throws CoreException {
		// disable auto build
		IWorkspace workspace = CTestPlugin.getWorkspace();
		IWorkspaceDescription desc = workspace.getDescription();
		desc.setAutoBuilding(enable);
		workspace.setDescription(desc);
	}

	public File getFileInPlugin(IPath path) {
		try {
			return new File(FileLocator.toFileURL(FileLocator.find(getBundle(), path, null)).getFile());
		} catch (IOException e) {
			return null;
		}
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}
}
