/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.executables;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.executables.StandardExecutableImporter;
import org.eclipse.cdt.debug.internal.core.executables.StandardSourceFileRemappingFactory;
import org.eclipse.cdt.debug.internal.core.executables.StandardSourceFilesProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The Executables Manager maintains a collection of executables built by all of
 * the projects in the workspace. Executables are contributed by instances of
 * IExecutablesProvider.
 * 
 * @author Ken Ryall
 * 
 */
public class ExecutablesManager extends PlatformObject implements IResourceChangeListener, ICProjectDescriptionListener {

	private static final String EXECUTABLES_MANAGER_DEBUG_TRACING = CDebugCorePlugin.PLUGIN_ID + "EXECUTABLES_MANAGER_DEBUG_TRACING"; //$NON-NLS-1$
	
	private Map<IProject, IProjectExecutablesProvider> executablesProviderMap = new HashMap<IProject, IProjectExecutablesProvider>();
	private Map<IProject, List<Executable>> executablesMap = new HashMap<IProject, List<Executable>>();
	private List<IExecutablesChangeListener> changeListeners = Collections.synchronizedList(new ArrayList<IExecutablesChangeListener>());
	private List<IProjectExecutablesProvider> executableProviders;
	private List<ISourceFilesProvider> sourceFileProviders;
	private List<ISourceFileRemappingFactory> sourceFileRemappingFactories;
	private List<IExecutableImporter> executableImporters;
	
	private boolean DEBUG;
	
	private Job refreshJob = new Job("Get Executables") { //$NON-NLS-1$

		@Override
		public IStatus run(IProgressMonitor monitor) {
				
			trace("Get Executables job started at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$
			
			List<IProject> projects = getProjectsToCheck();

			SubMonitor subMonitor = SubMonitor.convert(monitor, projects.size());

			for (IProject project : projects) {
				if (subMonitor.isCanceled()) {
					trace("Get Executables job cancelled at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$
					return Status.CANCEL_STATUS;
				}
				
				subMonitor.subTask("Checking project: " + project.getName()); //$NON-NLS-1$

				// get the executables provider for this project
				IProjectExecutablesProvider provider = getExecutablesProviderForProject(project);
				if (provider != null) {
					trace("Getting executables for project: " + project.getName() + " using " + provider.toString());  //$NON-NLS-1$//$NON-NLS-2$

					List<Executable> executables = provider.getExecutables(project, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
					// store the list of executables for this project
					synchronized (executablesMap) {
						if (!monitor.isCanceled()) {
							executablesMap.put(project, executables);
						}
					}
				}
			}
			
			// notify the listeners
			synchronized (changeListeners) {
				for (IExecutablesChangeListener listener : changeListeners) {
					listener.executablesListChanged();
				}
			}

			trace("Get Executables job finished at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$

			return Status.OK_STATUS;
		}
	};

	private static ExecutablesManager executablesManager = null;

	/**
	 * Get the executables manager instance
	 * @return the executables manager
	 */
	public static ExecutablesManager getExecutablesManager() {
		if (executablesManager == null)
			executablesManager = new ExecutablesManager();
		return executablesManager;
	}

	public ExecutablesManager() {
		
		// check if debugging is enabled
		BundleContext context = CDebugCorePlugin.getDefault().getBundle().getBundleContext();
		if (context != null) {
			ServiceReference reference = CDebugCorePlugin.getDefault().getBundle().getBundleContext().getServiceReference(DebugOptions.class.getName());
			if (reference != null) {
				DebugOptions service = (DebugOptions) context.getService(reference);
				if (service != null) {
					try {
						DEBUG = service.getBooleanOption(EXECUTABLES_MANAGER_DEBUG_TRACING, false);
					} finally {
						// we have what we want - release the service
						context.ungetService(reference);
					}
				}
			}
		}

		refreshJob.setPriority(Job.SHORT);
		
		// load the extension points
		loadExecutableProviderExtensions();
		loadSoureFileProviderExtensions();
		loadSoureRemappingExtensions();
		loadExecutableImporterExtensions();
		
		// add the standard providers
		executableProviders.add(0, new StandardExecutableProvider());
		sourceFileProviders.add(0, new StandardSourceFilesProvider());
		sourceFileRemappingFactories.add(0, new StandardSourceFileRemappingFactory());
		executableImporters.add(0, new StandardExecutableImporter());
		
		// listen for events we're interested in
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.POST_BUILD);
		CoreModel.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(this,
				CProjectDescriptionEvent.APPLIED);
		
		// schedule a refresh so we get up to date
		scheduleRefresh();
	}

	/**
	 * Adds an executable listener
	 * @param listener the listener to add
	 */
	public void addExecutablesChangeListener(IExecutablesChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Removes an executable listener
	 * @param listener the listener to remove
	 */
	public void removeExecutablesChangeListener(IExecutablesChangeListener listener) {
		changeListeners.remove(listener);
	}

	/**
	 * Gets the list of executables in the workspace.
	 * @param wait whether or not to wait if the list is being refreshed when this
	 * method is called.  when true, this call will not return until the list is
	 * complete.  when false, it will return with the last known list.  if calling
	 * from any UI, you should not block the UI waiting for this to return, but rather
	 * register as an {@link IExecutablesChangeListener} to get notifications when the
	 * list changes.
	 * @return the list of executables which may be empty
	 * @since 7.0
	 */
	public Collection<Executable> getExecutables(boolean wait) {
		
		trace("getExecutables called at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$

		List<Executable> executables = new ArrayList<Executable>();

		if (wait && refreshJob.getState() != Job.NONE) {
			trace("waiting for refresh job to finish at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$
			try {
				refreshJob.join();
			} catch (InterruptedException e) {
			}
			trace("refresh job finished at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$
		}
		
		synchronized (executablesMap) {
			for (List<Executable> exes : executablesMap.values()) {
				for (Executable exe : exes) {
					if (!executables.contains(exe)) {
						executables.add(exe);
					}
				}
			}
		}

		trace("getExecutables returned at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$

		return executables;
	}

	/**
	 * Gets the list of executables in the workspace.  Equivalent to {@link ExecutablesManager}{@link #getExecutables(false)}.
	 * Just kept for older API compatibility.
	 * @return the list of executables which may be empty
	 * @since 7.0
	 */
	public Collection<Executable> getExecutables() {
		return getExecutables(false);
	}
	
	/**
	 * @since 7.0
	 * Gets the collection of executables for the given project
	 * @param project the project
	 * @return collection of executables which may be empty
	 */
	public Collection<Executable> getExecutablesForProject(IProject project) {
		List<Executable> executables = new ArrayList<Executable>();

		synchronized (executablesMap) {
			List<Executable> exes = executablesMap.get(project);
			if (exes != null) {
				for (Executable exe : exes) {
					if (!executables.contains(exe)) {
						executables.add(exe);
					}
				}
			}
		}

		return executables;
	}

	/**
	 * Import the given executables into the manager
	 * @param fileNames the absolute paths of the executables to import
	 * @param monitor progress monitor
	 */
	public void importExecutables(final String[] fileNames, IProgressMonitor monitor) {

		boolean handled = false;
		monitor.beginTask("Import Executables", executableImporters.size()); //$NON-NLS-1$
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
		
		if (handled)
			scheduleRefresh();
	}

	/**
	 * Determines if the given executable is currently known by the manager
	 * @param exePath the absolute path to the executable
	 * @return true if the manager knows about it, false otherwise
	 */
	public boolean executableExists(IPath exePath) {
		synchronized (executablesMap) {
			for (List<Executable> exes : executablesMap.values()) {
				for (Executable exe : exes) {
					if (exe.getPath().equals(exePath)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	/**
	 * Get the list of source files for the given executable
	 * @param executable the executable
	 * @param monitor progress monitor
	 * @return an array of source files which may be empty
	 */
	public String[] getSourceFiles(final Executable executable, IProgressMonitor monitor) {
		String[] result = new String[0];

		trace("getSourceFiles called at " + getStringFromTimestamp(System.currentTimeMillis()) + " for " + executable.getPath().toOSString());  //$NON-NLS-1$//$NON-NLS-2$

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
			
			monitor.beginTask("Finding source files in " + executable.getName(), sourceFileProviders.size() * 1000); //$NON-NLS-1$
			for (ISourceFilesProvider provider : sourceFileProviders) {
				String[] sourceFiles = provider.getSourceFiles(executable, new SubProgressMonitor(monitor, 1000));
				if (sourceFiles.length > 0) {
					result = sourceFiles;

					trace("getSourceFiles got " + sourceFiles.length + " files from " + provider.toString()); //$NON-NLS-1$ //$NON-NLS-2$

					break;
				}
			}
			monitor.done();
		}

		trace("getSourceFiles returned at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$

		return result;
	}

	/**
	 * Removes the given executables
	 * @param executables the array of executables to be removed
	 * @param monitor progress monitor
	 * @return IStatus of the operation
	 * 
	 * @since 6.0
	 */
	public IStatus removeExecutables(Executable[] executables, IProgressMonitor monitor) {
		MultiStatus status = new MultiStatus(CDebugCorePlugin.PLUGIN_ID, IStatus.WARNING, "Couldn't remove all of the selected executables", null); //$NON-NLS-1$
		
		monitor.beginTask("Remove Executables", executables.length); //$NON-NLS-1$
		for (Executable executable : executables) {
			
			IProjectExecutablesProvider provider = getExecutablesProviderForProject(executable.getProject());
			if (provider != null) {
				IStatus result = provider.removeExecutable(executable, new SubProgressMonitor(monitor, 1));
				if (result.isOK()) {
					// remove the exe from the list
					List<Executable> exes = executablesMap.get(executable.getProject());
					if (exes != null) {
						exes.remove(executable);
					}
				} else {
					status.add(result);
				}
			}
		}

		// notify listeners that the list has changed.  only do this if at least one delete succeeded.
		if (status.getChildren().length != executables.length) {
			synchronized (changeListeners) {
				for (IExecutablesChangeListener listener : changeListeners) {
					listener.executablesListChanged();
				}
			}
		}
		
		return status;
	}

	/**
	 * Refresh the list of executables for the given projects
	 * @param projects the list of projects, or null.  if null or the list
	 * is empty, all projects will be refreshed.
	 * @since 7.0
	 */
	public void refresh(List<IProject> projects) {
		if (projects == null || projects.size() == 0) {
			// clear the entire cache
			executablesMap.clear();
		} else {
			for (IProject project : projects) {
				executablesMap.remove(project);
			}
		}
		
		scheduleRefresh();
	}

	/**
	 * @since 7.0
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		synchronized (executablesMap) {
			// project needs to be refreshed after a build/clean as the binary may
			// be added/removed/renamed etc.
			if (event.getType() == IResourceChangeEvent.POST_BUILD) {
				Object obj = event.getSource();
				if (obj != null && obj instanceof IProject) {
					try {
						// make sure there's at least one builder for the project.  this gets called even
						// when there are no builder (e.g. the Executables project for imported executables).
						IProject project = (IProject)obj;
						if (project.getDescription().getBuildSpec().length > 0) {
							if (executablesMap.containsKey(obj)) {
								List<Executable> executables = executablesMap.remove(obj);

								trace("Scheduling refresh because project " + ((IProject)obj).getName() + " built or cleaned");  //$NON-NLS-1$//$NON-NLS-2$
								
								scheduleRefresh();

								// notify the listeners that these executables have possibly changed
								if (executables != null && executables.size() > 0) {
									synchronized (changeListeners) {
										for (IExecutablesChangeListener listener : changeListeners) {
											listener.executablesChanged(executables);
										}
									}
								}
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
				return;
			}
			
			// refresh when projects are opened or closed. note that deleted
			// projects are handled later in this method. new projects are handled
			// in handleEvent.  resource changed events always start at the workspace
			// root, so projects are the next level down
			boolean refreshNeeded = false;
			IResourceDelta[] projects = event.getDelta().getAffectedChildren();
			for (IResourceDelta projectDelta : projects) {
				if ((projectDelta.getFlags() & IResourceDelta.OPEN) != 0) {
					if (projectDelta.getKind() == IResourceDelta.CHANGED) {
						// project was opened or closed
						if (executablesMap.containsKey(projectDelta.getResource())) {
							executablesMap.remove(projectDelta.getResource());
						}
						refreshNeeded = true;
					}
				}
			}
			
			if (refreshNeeded) {
				trace("Scheduling refresh because project(s) opened or closed"); //$NON-NLS-1$

				scheduleRefresh();
				return;
			}

			try {
				event.getDelta().accept(new IResourceDeltaVisitor() {

					public boolean visit(IResourceDelta delta) throws CoreException {
						if (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED) {
							IResource deltaResource = delta.getResource();
							if (deltaResource != null) {
								boolean refresh = false;
								if (delta.getKind() == IResourceDelta.REMOVED && deltaResource instanceof IProject) {
									// project deleted
									if (executablesMap.containsKey(deltaResource)) {
										executablesMap.remove(deltaResource);
										refresh = true;

										trace("Scheduling refresh because project " + deltaResource.getName() + " deleted");  //$NON-NLS-1$//$NON-NLS-2$
									}
								} else {
									// see if a binary has been added/removed
									IPath resourcePath = deltaResource.getLocation();
									if (resourcePath != null && Executable.isExecutableFile(resourcePath)) {
										if (executablesMap.containsKey(deltaResource.getProject())) {
											executablesMap.remove(deltaResource.getProject());
											refresh = true;

											trace("Scheduling refresh because a binary was added/removed"); //$NON-NLS-1$
										}
									}
								}

								if (refresh) {
									scheduleRefresh();
									return false;
								}
							}
						}
						return true;
					}
				});
			} catch (CoreException e) {
			}
		}
	}

	/**
	 * @since 7.0
	 */
	public void handleEvent(CProjectDescriptionEvent event) {
		// this handles the cases where the active build configuration changes,
		// and when new projects are created or loaded at startup.
		boolean refresh = false;

		int eventType = event.getEventType();

		if (eventType == CProjectDescriptionEvent.APPLIED) {
			
			synchronized (executablesMap) {
				// see if the active build config has changed
				ICProjectDescription newDesc = event.getNewCProjectDescription();
				ICProjectDescription oldDesc = event.getOldCProjectDescription();
				if (oldDesc != null && newDesc != null) {
					String newConfigName = newDesc.getActiveConfiguration().getName();
					String oldConfigName = oldDesc.getActiveConfiguration().getName();
					if (!newConfigName.equals(oldConfigName)) {
						if (executablesMap.containsKey(newDesc.getProject())) {
							executablesMap.remove(newDesc.getProject());
							refresh = true;

							trace("Scheduling refresh because active build configuration changed"); //$NON-NLS-1$
						}
					}
				} else if (newDesc != null && oldDesc == null) {
					// project just created
					refresh = true;

					trace("Scheduling refresh because project " + newDesc.getProject().getName() + " created");  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}

		if (refresh) {
			scheduleRefresh();
		}
	}

	private List<IProject> getProjectsToCheck() {

		List<IProject> projects = new ArrayList<IProject>();
		
		synchronized (executablesMap) {
			// look for any CDT projects not in our cache
			for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				if (!executablesMap.containsKey(project)) {
					if (CoreModel.hasCNature(project)) {
						projects.add(project);
					}
				}
			}
		}

		return projects;
	}

	private void scheduleRefresh() {
		trace("scheduleRefresh called at " + getStringFromTimestamp(System.currentTimeMillis())); //$NON-NLS-1$

		refreshJob.cancel();
		refreshJob.schedule();
	}

	private IProjectExecutablesProvider getExecutablesProviderForProject(IProject project) {
		IProjectExecutablesProvider provider = executablesProviderMap.get(project);
		if (provider == null) {
			// not cached yet.  get the list of project natures from the providers and
			// pick the one with the closest match
			try {
				IProjectDescription description = project.getDescription();
				int mostNaturesMatched = 0;
				for (IProjectExecutablesProvider exeProvider : executableProviders) {
					List<String> natures = exeProvider.getProjectNatures();

					int naturesMatched = 0;
					for (String nature : description.getNatureIds()) {
						if (natures.contains(nature)) {
							naturesMatched++;
						}
					}
					
					if (naturesMatched > mostNaturesMatched) {
						provider = exeProvider;
						mostNaturesMatched = naturesMatched;
					}
				}

				// cache it
				executablesProviderMap.put(project, provider);

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return provider;
	}
	
	ISourceFileRemappingFactory[] getSourceFileRemappingFactories() {
		return sourceFileRemappingFactories.toArray(new ISourceFileRemappingFactory[sourceFileRemappingFactories.size()]);
	}

	private void loadExecutableProviderExtensions() {
		executableProviders = Collections.synchronizedList(new ArrayList<IProjectExecutablesProvider>());

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(CDebugCorePlugin.PLUGIN_ID + ".ExecutablesProvider"); //$NON-NLS-1$
		IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			IConfigurationElement element = elements[0];
			
			boolean failed = false;
			try {
				Object extObject = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (extObject instanceof IProjectExecutablesProvider) {
					executableProviders.add((IProjectExecutablesProvider)extObject);
				} else {
					failed = true;
				}
			} 
			catch (CoreException e) {
				failed = true;
			}
			
			if (failed) {
				CDebugCorePlugin.log("Unable to load ExecutablesProvider extension from " + extension.getContributor().getName()); //$NON-NLS-1$
			}
		}
	}
	
	private void loadSoureFileProviderExtensions() {
		sourceFileProviders = Collections.synchronizedList(new ArrayList<ISourceFilesProvider>());

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(CDebugCorePlugin.PLUGIN_ID + ".SourceFilesProvider"); //$NON-NLS-1$
		IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			IConfigurationElement element = elements[0];
			
			boolean failed = false;
			try {
				Object extObject = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (extObject instanceof ISourceFilesProvider) {
					sourceFileProviders.add((ISourceFilesProvider)extObject);
				} else {
					failed = true;
				}
			} 
			catch (CoreException e) {
				failed = true;
			}
			
			if (failed) {
				CDebugCorePlugin.log("Unable to load SourceFilesProvider extension from " + extension.getContributor().getName()); //$NON-NLS-1$
			}
		}
	}

	private void loadSoureRemappingExtensions() {
		sourceFileRemappingFactories = Collections.synchronizedList(new ArrayList<ISourceFileRemappingFactory>());

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(CDebugCorePlugin.PLUGIN_ID + ".SourceRemappingProvider"); //$NON-NLS-1$
		IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			IConfigurationElement element = elements[0];
			
			boolean failed = false;
			try {
				Object extObject = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (extObject instanceof ISourceFileRemappingFactory) {
					sourceFileRemappingFactories.add((ISourceFileRemappingFactory)extObject);
				} else {
					failed = true;
				}
			} 
			catch (CoreException e) {
				failed = true;
			}
			
			if (failed) {
				CDebugCorePlugin.log("Unable to load SourceRemappingProvider extension from " + extension.getContributor().getName()); //$NON-NLS-1$
			}
		}
	}

	private void loadExecutableImporterExtensions() {
		executableImporters = Collections.synchronizedList(new ArrayList<IExecutableImporter>());

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(CDebugCorePlugin.PLUGIN_ID + ".ExecutablesImporter"); //$NON-NLS-1$
		IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			IConfigurationElement element = elements[0];
			
			boolean failed = false;
			try {
				Object extObject = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (extObject instanceof IExecutableImporter) {
					executableImporters.add((IExecutableImporter)extObject);
				} else {
					failed = true;
				}
			} 
			catch (CoreException e) {
				failed = true;
			}
			
			if (failed) {
				CDebugCorePlugin.log("Unable to load ExecutablesImporter extension from " + extension.getContributor().getName()); //$NON-NLS-1$
			}
		}
	}

	private void trace(String msg) {
		if (DEBUG) {
			// TODO use Logger?
			System.out.println(msg);
		}
	}
	
	private String getStringFromTimestamp(long timestamp) {
		return DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date(timestamp));
	}
}
