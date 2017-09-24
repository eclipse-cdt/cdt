/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class CPPLanguageServer extends ProcessStreamConnectionProvider {

	public static final String ID = "org.eclipse.lsp4e.languages.cpp"; //$NON-NLS-1$

	private static final String CLANG_LANGUAGE_SERVER = "clangd"; //$NON-NLS-1$

	private IResourceChangeListener fResourceListener;

	public CPPLanguageServer() {
		List<String> commands = new ArrayList<>();
		File clangServerLocation = getClangServerLocation();
		String parent = ""; //$NON-NLS-1$
		if (clangServerLocation != null) {
			commands.add(clangServerLocation.getAbsolutePath());
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

	private static File getClangServerLocation() {
		String res = null;
		String[] command = new String[] {"/bin/bash", "-c", "which " + CLANG_LANGUAGE_SERVER}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] {"cmd", "/c", "where " + CLANG_LANGUAGE_SERVER}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			res = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}

		if (res == null) {
			return null;
		}

		File f = new File(res);
		if (f.canExecute()) {
			return f;
		}

		return null;
	}
}
