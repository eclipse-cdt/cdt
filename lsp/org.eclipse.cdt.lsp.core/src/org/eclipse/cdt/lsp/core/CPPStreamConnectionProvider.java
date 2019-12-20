/*******************************************************************************
 * Copyright (c) 2017-2019 Ericsson and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     Ericsson - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 558516
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.cdt.lsp.internal.core.LspCoreMessages;
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

	private ICPPLanguageServer languageServer;

	public static final String CLANGD_ID = "clangd"; //$NON-NLS-1$

	public static final String CQUERY_ID = "cquery"; //$NON-NLS-1$

	public CPPStreamConnectionProvider() throws UnsupportedOperationException {
		List<String> commands = new ArrayList<>();
		if (store.getString(PreferenceConstants.P_SERVER_CHOICE).equals(CQUERY_ID)) {
			languageServer = new CqueryLanguageServer();
		} else if (store.getString(PreferenceConstants.P_SERVER_CHOICE).equals(CLANGD_ID)) {
			languageServer = new ClangdLanguageServer();
		} else {
			throw new UnsupportedOperationException(LspCoreMessages.CPPStreamConnectionProvider_e_unsupported);
		}
		File defaultLSLocation = getDefaultLSLocation(store.getString(PreferenceConstants.P_SERVER_CHOICE));
		if (defaultLSLocation != null) {
			store.setDefault(PreferenceConstants.P_SERVER_PATH, defaultLSLocation.getAbsolutePath());
		}
		File languageServerLocation = getLanguageServerLocation();
		String parent = ""; //$NON-NLS-1$
		String flags = store.getString(PreferenceConstants.P_SERVER_OPTIONS);
		if (languageServerLocation != null) {
			commands.add(languageServerLocation.getAbsolutePath());
			if (!flags.isEmpty()) {
				commands.add(flags);
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
		Object defaultInitOptions = super.getInitializationOptions(rootPath);
		return languageServer.getLSSpecificInitializationOptions(defaultInitOptions, rootPath);
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

	static File getDefaultLSLocation(String selectedLanguageServer) {
		String res = null;
		String[] command = new String[] { "/bin/bash", "-c", "which " + selectedLanguageServer }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where " + selectedLanguageServer }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
