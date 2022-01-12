/*******************************************************************************
 * Copyright (c) 2017, 2020 Ericsson and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 558516
 *     Philip Langer <planger@eclipsesource.com> - Bug 563280
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.lsp.LanguageServerConfiguration;
import org.eclipse.cdt.lsp.internal.core.ResolvePreferredServer;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class CPPStreamConnectionProvider extends ProcessStreamConnectionProvider {

	public static final String ID = "org.eclipse.cdt.lsp.core"; //$NON-NLS-1$

	private IResourceChangeListener fResourceListener;

	private static final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	private final LanguageServerConfiguration configuration;

	public CPPStreamConnectionProvider() throws UnsupportedOperationException {
		configuration = new ResolvePreferredServer().apply(getClass());
		File defaultLSLocation = getDefaultLSLocation(configuration.identifier());
		if (defaultLSLocation != null) {
			store.setDefault(PreferenceConstants.P_SERVER_PATH, defaultLSLocation.getAbsolutePath());
		}
		File languageServerLocation = getLanguageServerLocation();
		String parent = ""; //$NON-NLS-1$
		String flags = store.getString(PreferenceConstants.P_SERVER_OPTIONS);
		List<String> commands = new ArrayList<>();
		if (languageServerLocation != null) {
			commands.add(languageServerLocation.getAbsolutePath());
			if (!flags.isEmpty()) {
				commands.addAll(Arrays.asList(CommandLineUtil.argumentsToArray(flags)));
			}
			parent = languageServerLocation.getParent();
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
		return configuration.options(super.getInitializationOptions(rootPath), rootPath);
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
		String path = store.getString(PreferenceConstants.P_SERVER_PATH);

		if (path.isEmpty()) {
			return null;
		}
		File f = new File(path);
		if (f.canExecute()) {
			return f;
		}

		return null;
	}

	//FIXME: to be extracted to a separate type
	public static File getDefaultLSLocation(String selectedLanguageServer) {
		String res = null;
		String[] command = new String[] { "/bin/bash", "-c", "which " + selectedLanguageServer }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where " + selectedLanguageServer }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		try {
			Process p = Runtime.getRuntime().exec(command);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				res = reader.readLine();
			}
		} catch (IOException e) {
			//FIXME: rework this branch , it may contain valuable information to understand the problem
			e.printStackTrace();
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
