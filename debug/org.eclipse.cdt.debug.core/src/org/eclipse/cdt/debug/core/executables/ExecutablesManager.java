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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.executables.ExecutablesChangeEvent;
import org.eclipse.cdt.debug.internal.core.executables.StandardExecutableImporter;
import org.eclipse.cdt.debug.internal.core.executables.StandardExecutableProvider;
import org.eclipse.cdt.debug.internal.core.executables.StandardSourceFileRemapping;
import org.eclipse.cdt.debug.internal.core.executables.StandardSourceFilesProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;

/**
 * The Executables Manager maintains a collection of executables built by all of
 * the projects in the workspace. Executables are contributed by instances of
 * IExecutablesProvider.
 * 
 * @author Ken Ryall
 * 
 */
public class ExecutablesManager extends PlatformObject {

	private final HashMap<String, Executable> executables = new HashMap<String, Executable>();
	private final List<IExecutablesChangeListener> changeListeners = Collections.synchronizedList(new ArrayList<IExecutablesChangeListener>());
	private final List<ISourceFileRemapping> sourceFileRemappings = Collections.synchronizedList(new ArrayList<ISourceFileRemapping>());
	private final List<IExecutableProvider> executableProviders = Collections.synchronizedList(new ArrayList<IExecutableProvider>());
	private final List<ISourceFilesProvider> sourceFileProviders = Collections.synchronizedList(new ArrayList<ISourceFilesProvider>());
	private final List<IExecutableImporter> executableImporters = Collections.synchronizedList(new ArrayList<IExecutableImporter>());
	private boolean refreshNeeded = true;
	private boolean tempDisableRefresh = false;
	
	private final Job refreshJob = new Job("Get Executables") {

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
		addSourceFilesProvider(new StandardSourceFilesProvider());
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

	/**
	 * @since 6.0
	 */
	public void addSourceFilesProvider(ISourceFilesProvider provider) {
		sourceFileProviders.add(provider);
	}

	/**
	 * @since 6.0
	 */
	public void removeSourceFilesProvider(ISourceFilesProvider provider) {
		sourceFileProviders.remove(provider);
	}

	public void removeExecutablesProvider(IExecutableProvider provider) {
		executableProviders.remove(provider);
	}

	public IStatus refreshExecutables(IProgressMonitor monitor) {
		if (tempDisableRefresh) {
			return Status.OK_STATUS;
		}

		
		synchronized (executables) {
			HashMap<String, Executable> oldList = new HashMap<String, Executable>(executables);
			executables.clear();

			IExecutableProvider[] exeProviders = getExecutableProviders();

			Arrays.sort(exeProviders, new Comparator<IExecutableProvider>() {

				public int compare(IExecutableProvider arg0, IExecutableProvider arg1) {
					int p0 = arg0.getPriority();
					int p1 = arg1.getPriority();
					if (p0 > p1)
						return 1;
					if (p0 < p1)
						return -1;
					return 0;
				}});

			refreshNeeded = false;
			monitor.beginTask("Refresh Executables", exeProviders.length);
			for (IExecutableProvider provider : exeProviders) {
				Executable[] exes = provider.getExecutables(new SubProgressMonitor(monitor, 1));
				for (Executable executable : exes) {
					executables.put(executable.getPath().toOSString(), executable);
				}
			}
			monitor.done();

			synchronized (changeListeners) {
				Collection<Executable> newExes = executables.values();
				Executable[] exeArray = newExes.toArray(new Executable[newExes.size()]);
				Collection<Executable> oldExes = oldList.values();
				Executable[] oldArray = oldExes.toArray(new Executable[oldExes.size()]);				
				for (IExecutablesChangeListener listener : changeListeners) {
					listener.executablesChanged(new ExecutablesChangeEvent(oldArray, exeArray));
				}
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
				DebugPlugin.log( e );
			}
		}
		
		synchronized (executables)
		{
			Collection<Executable> exes = executables.values();
			return exes.toArray(new Executable[exes.size()]);
		}
	}

	/**
	 * @since 6.0
	 */
	public String remapSourceFile(Executable executable, String filePath) {
		synchronized (sourceFileRemappings) {
			for (ISourceFileRemapping remapping : sourceFileRemappings) {
				String remappedPath = remapping.remapSourceFile(executable, filePath);
				if (!remappedPath.equals(filePath))
					return remappedPath;
			}
		}
		return filePath;
	}

	public void importExecutables(final String[] fileNames, IProgressMonitor monitor) {
		boolean handled = false;
		try {
			
			tempDisableRefresh = true;
			monitor.beginTask("Import Executables", executableImporters.size());
			synchronized (executableImporters) {
				Collections.sort(executableImporters, new Comparator<IExecutableImporter>() {

					public int compare(IExecutableImporter arg0, IExecutableImporter arg1) {
						int p0 = arg0.getPriority(fileNames);
						int p1 = arg1.getPriority(fileNames);
						if (p0 < p1)
							return 1;
						if (p0 > p1)
							return -1;
						return 0;
					}});

				for (IExecutableImporter importer : executableImporters) {
					handled = importer.importExecutables(fileNames, new SubProgressMonitor(monitor, 1));
					if (handled || monitor.isCanceled()) {
						break;
					}
				}
			}

		} finally {
			tempDisableRefresh = false;
		}
		
		if (handled)
			refreshExecutables(monitor);
		monitor.done();
	}

	public ISourceFileRemapping[] getSourceFileRemappings() {
		return sourceFileRemappings.toArray(new ISourceFileRemapping[sourceFileRemappings.size()]);
	}

	public IExecutableProvider[] getExecutableProviders() {
		return executableProviders.toArray(new IExecutableProvider[executableProviders.size()]);
	}

	/**
	 * @since 6.0
	 */
	public ISourceFilesProvider[] getSourceFileProviders() {
		return sourceFileProviders.toArray(new ISourceFilesProvider[sourceFileProviders.size()]);
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
		synchronized (executables) {
			return executables.containsKey(exePath.toOSString());			
		}
	}

	/**
	 * @since 6.0
	 */
	public String[] getSourceFiles(final Executable executable,
			IProgressMonitor monitor) {
		String[] result = new String[0];
		synchronized (sourceFileProviders) {
			Collections.sort(sourceFileProviders, new Comparator<ISourceFilesProvider>() {

				public int compare(ISourceFilesProvider arg0, ISourceFilesProvider arg1) {
					int p0 = arg0.getPriority(executable);
					int p1 = arg1.getPriority(executable);
					if (p0 < p1)
						return 1;
					if (p0 > p1)
						return -1;
					return 0;
				}});
			
			monitor.beginTask("Finding source files in " + executable.getName(), sourceFileProviders.size());
			for (ISourceFilesProvider provider : sourceFileProviders) {
				String[] sourceFiles = provider.getSourceFiles(executable, new SubProgressMonitor(monitor, 1));
				if (sourceFiles.length > 0)
				{
					result = sourceFiles;
					break;
				}
			}
			monitor.done();
		}
		return result;
	}

	/**
	 * @since 6.0
	 */
	public IStatus removeExecutables(Executable[] executables, IProgressMonitor monitor) {
		IExecutableProvider[] exeProviders = getExecutableProviders();

		IStatus result = Status.OK_STATUS;
		
		Arrays.sort(exeProviders, new Comparator<IExecutableProvider>() {

			public int compare(IExecutableProvider arg0, IExecutableProvider arg1) {
				int p0 = arg0.getPriority();
				int p1 = arg1.getPriority();
				if (p0 > p1)
					return 1;
				if (p0 < p1)
					return -1;
				return 0;
			}
		});

		MultiStatus combinedStatus = new MultiStatus(CDebugCorePlugin.PLUGIN_ID, IStatus.WARNING, "Couldn't remove all of the selected executables", null);
		refreshNeeded = false;
		monitor.beginTask("Remove Executables", exeProviders.length);
		for (Executable executable : executables) {
			boolean handled = false;
			IStatus rmvStatus = Status.OK_STATUS;;
			for (IExecutableProvider provider : exeProviders) {
				if (!handled)
				{
					rmvStatus = provider.removeExecutable(executable, new SubProgressMonitor(monitor, 1));
					handled = rmvStatus.getSeverity() == IStatus.OK;
				}				
			}
			if (!handled)
			{
				combinedStatus.add(rmvStatus);
				result = combinedStatus;
			}
		}
		monitor.done();
		
		return result;
	}

	/**
	 * @since 6.0
	 */
	public void setRefreshNeeded(boolean refresh) {
		refreshNeeded = true;
	}

}