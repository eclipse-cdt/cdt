/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.executables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The Executables Manager maintains a collection of executables built by all of
 * the projects in the workspace. Executables are contributed by instances of
 * IExecutablesProvider.
 * 
 * @author Ken Ryall
 * 
 */
public class ExecutablesManager extends PlatformObject {

	private ArrayList<Executable> executables = new ArrayList<Executable>();
	private List<IExecutablesChangeListener> changeListeners = Collections.synchronizedList(new ArrayList<IExecutablesChangeListener>());
	private List<ISourceFileRemapping> sourceFileRemappings = Collections.synchronizedList(new ArrayList<ISourceFileRemapping>());
	private List<IExecutableProvider> executableProviders = Collections.synchronizedList(new ArrayList<IExecutableProvider>());
	private List<IExecutableImporter> executableImporters = Collections.synchronizedList(new ArrayList<IExecutableImporter>());
	private boolean refreshNeeded = true;
	private boolean tempDisableRefresh = false;
	
	private Job refreshJob = new Job("Get Executables") {

		@Override
		public IStatus run(IProgressMonitor monitor) {
			refreshExecutables(monitor);
			return Status.OK_STATUS;
		}
	};

	private static ExecutablesManager executablesManager = null;

	public static ExecutablesManager getExecutablesManager() {
		if (executablesManager == null)
			executablesManager = new ExecutablesManager();
		return executablesManager;
	}

	public ExecutablesManager() {
		addSourceFileRemapping(new StandardSourceFileRemapping());
		addExecutableImporter(new StandardExecutableImporter());
		addExecutablesProvider(new StandardExecutableProvider());
	}

	public void addExecutablesChangeListener(IExecutablesChangeListener listener) {
		changeListeners.add(listener);
	}

	public void removeExecutablesChangeListener(IExecutablesChangeListener listener) {
		changeListeners.remove(listener);
	}

	public void addSourceFileRemapping(ISourceFileRemapping remapping) {
		sourceFileRemappings.add(remapping);
	}

	public void removeSourceFileRemapping(ISourceFileRemapping remapping) {
		sourceFileRemappings.remove(remapping);
	}

	public void addExecutableImporter(IExecutableImporter importer) {
		executableImporters.add(importer);
	}

	public void removeExecutableImporter(IExecutableImporter importer) {
		executableImporters.remove(importer);
	}

	public void addExecutablesProvider(IExecutableProvider provider) {
		executableProviders.add(provider);
	}

	public void removeExecutablesProvider(IExecutableProvider provider) {
		executableProviders.remove(provider);
	}

	public IStatus refreshExecutables(IProgressMonitor monitor) {
		if (tempDisableRefresh) {
			return Status.OK_STATUS;
		}

		ArrayList<Executable> oldList = executables;
		executables = new ArrayList<Executable>();
		synchronized (executableProviders) {
			monitor.beginTask("Refresh Executables", executableProviders.size());
			for (IExecutableProvider provider : executableProviders) {
				executables.addAll(provider.getExecutables(new SubProgressMonitor(monitor, 1)));
			}
			monitor.done();
		}
		refreshNeeded = false;

		synchronized (changeListeners) {
			for (IExecutablesChangeListener listener : changeListeners) {
				listener.executablesChanged(new ExecutablesChangeEvent(oldList, executables) {
				});
			}
		}
		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	public Executable[] getExecutables() {
		if (refreshNeeded) {
			try {
				refreshJob.schedule();
				refreshJob.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return executables.toArray(new Executable[executables.size()]);
	}

	public String remapSourceFile(String filePath) {
		synchronized (sourceFileRemappings) {
			for (ISourceFileRemapping remapping : sourceFileRemappings) {
				String remappedPath = remapping.remapSourceFile(filePath);
				if (!remappedPath.equals(filePath))
					return remappedPath;
			}
		}
		return filePath;
	}

	public void importExecutables(String[] fileNames, IProgressMonitor monitor) {
		try {
			synchronized (executableImporters) {
				tempDisableRefresh = true;

				monitor.beginTask("Import Executables", executableImporters.size());
				for (IExecutableImporter importer : executableImporters) {
					importer.importExecutables(fileNames, new SubProgressMonitor(monitor, 1));
					if (monitor.isCanceled()) {
						break;
					}
				}
			}
		} finally {
			tempDisableRefresh = false;
		}
		
		refreshExecutables(monitor);
		monitor.done();
	}

	public ISourceFileRemapping[] getSourceFileRemappings() {
		return sourceFileRemappings.toArray(new ISourceFileRemapping[sourceFileRemappings.size()]);
	}

	public IExecutableProvider[] getExecutableProviders() {
		return executableProviders.toArray(new IExecutableProvider[executableProviders.size()]);
	}

	public IExecutableImporter[] getExecutableImporters() {
		return executableImporters.toArray(new IExecutableImporter[executableImporters.size()]);
	}

	public void scheduleRefresh(IExecutableProvider provider, long delay) {
		refreshNeeded = true;
		refreshJob.schedule(delay);
	}

	public boolean refreshNeeded() {
		return refreshNeeded;
	}
	
	public boolean executableExists(IPath exePath) {
		for (Executable executable : executables) {
			if (executable.getPath().equals(exePath))
				return true;
		}
		return false;
	}

}