/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core.services.local;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.remote.core.IProcessFactory;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.internal.core.RemoteCorePlugin;

public class LocalProcessBuilder extends AbstractRemoteProcessBuilder {
	private static final String EXTENSION_POINT_ID = "processFactory"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private final IProcessFactory fProcessFactory;
	private final Map<String, String> fRemoteEnv = new HashMap<String, String>();

	public LocalProcessBuilder(List<String> command) {
		super(command);
		fRemoteEnv.putAll(System.getenv());
		fProcessFactory = getProcessFactory();
	}

	public LocalProcessBuilder(String... command) {
		this(Arrays.asList(command));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.AbstractRemoteProcessBuilder#directory()
	 */
	@Override
	public IFileStore directory() {
		IFileStore dir = super.directory();
		if (dir == null) {
			String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
			if (userDir != null) {
				dir = EFS.getLocalFileSystem().getStore(new Path(userDir));
				directory(dir);
			}
		}
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		return fRemoteEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.AbstractRemoteProcessBuilder#getSupportedFlags
	 * ()
	 */
	@Override
	public int getSupportedFlags() {
		return NONE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessBuilder#start(int)
	 */
	@Override
	public IRemoteProcess start(int flags) throws IOException {
		String commandArray[] = command().toArray(new String[0]);
		String environmentArray[] = new String[environment().size()];
		int index = 0;
		for (Entry<String, String> entry : environment().entrySet()) {
			environmentArray[index++] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
		}
		Process localProc;
		if (directory() != null) {
			try {
				localProc = fProcessFactory.exec(commandArray, environmentArray,
						directory().toLocalFile(EFS.NONE, new NullProgressMonitor()));
			} catch (CoreException e) {
				throw new IOException(e.getMessage());
			}
		} else {
			localProc = fProcessFactory.exec(commandArray, environmentArray);
		}
		return new LocalProcess(localProc, redirectErrorStream());
	}

	private IProcessFactory getProcessFactory() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(RemoteCorePlugin.getUniqueIdentifier(), EXTENSION_POINT_ID);

		IProcessFactory processFactory = null;

		for (IExtension ext : extensionPoint.getExtensions()) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();

			for (IConfigurationElement ce : elements) {
				try {
					processFactory = (IProcessFactory) ce.createExecutableExtension(ATTR_CLASS);
				} catch (CoreException e) {
					// Use default factory
				}
			}
		}

		if (processFactory == null) {
			processFactory = new IProcessFactory() {
				@Override
				public Process exec(String cmd) throws IOException {
					return Runtime.getRuntime().exec(cmd);
				}

				@Override
				public Process exec(String[] cmdarray) throws IOException {
					return Runtime.getRuntime().exec(cmdarray);
				}

				@Override
				public Process exec(String[] cmdarray, String[] envp) throws IOException {
					return Runtime.getRuntime().exec(cmdarray, envp);
				}

				@Override
				public Process exec(String cmd, String[] envp) throws IOException {
					return Runtime.getRuntime().exec(cmd, envp);
				}

				@Override
				public Process exec(String cmd, String[] envp, File dir) throws IOException {
					return Runtime.getRuntime().exec(cmd, envp, dir);
				}

				@Override
				public Process exec(String[] cmdarray, String[] envp, File dir) throws IOException {
					return Runtime.getRuntime().exec(cmdarray, envp, dir);
				}
			};
		}

		return processFactory;
	}
}
