/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class CPPLanguageServer extends ProcessStreamConnectionProvider {

	public static final String ID = "org.eclipse.lsp4e.languages.cpp"; //$NON-NLS-1$

	private IResourceChangeListener fResourceListener;

	private static final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	public CPPLanguageServer() {
		List<String> commands = new ArrayList<>();
		File clangServerLocation = getLanguageServerLocation();
		String parent = ""; //$NON-NLS-1$
		String flags = store.getString(PreferenceConstants.P_FLAGS);
		if (clangServerLocation != null) {
			commands.add(clangServerLocation.getAbsolutePath());
			if (!flags.isEmpty()) {
				commands.add(flags);
			}
			parent = clangServerLocation.getParent();
		}
		setWorkingDirectory(parent);
		setCommands(commands);
	}

	@Override
	public void stop() {
		super.stop();
		if (fResourceListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fResourceListener = null;
		}
	}

	@Override
	public Object getInitializationOptions(URI rootPath) {
		installResourceChangeListener(rootPath);
		return super.getInitializationOptions(rootPath);
	}

	private void installResourceChangeListener(URI rootPath) {
		if (rootPath == null || fResourceListener != null) {
			return;
		}

		IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(rootPath);
		if (containers.length == 0) {
			return;
		}

		for (IContainer c : containers) {
			if (!(c instanceof IProject)) {
				continue;
			}
			IProject project = (IProject) c;
			fResourceListener = new CPPResourceChangeListener(project);
			project.getWorkspace().addResourceChangeListener(fResourceListener);

			break;
		}
	}

	@Override
	public String toString() {
		return "C/C++ Language Server: " + super.toString(); //$NON-NLS-1$
	}

	private static File getLanguageServerLocation() {
		String path = store.getString(PreferenceConstants.P_PATH);

		if (path.isEmpty()) {
			return null;
		}
		File f = new File(path);
		if (f.canExecute()) {
			return f;
		}

		return null;
	}
}
