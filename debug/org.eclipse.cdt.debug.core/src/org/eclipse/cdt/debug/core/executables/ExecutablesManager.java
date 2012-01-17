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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.Trace;
import org.eclipse.cdt.debug.internal.core.executables.StandardExecutableImporter;
import org.eclipse.cdt.debug.internal.core.executables.StandardSourceFileRemappingFactory;
import org.eclipse.cdt.debug.internal.core.executables.StandardSourceFilesProvider;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * The Executables Manager maintains a collection of executables built by all of
 * the projects in the workspace. Executables are contributed by instances of
 * IExecutablesProvider.
 * 
 * @author Ken Ryall
 * 
 */
public class ExecutablesManager extends PlatformObject implements ICProjectDescriptionListener, IElementChangedListener, IResourceChangeListener {

	private Map<IProject, IProjectExecutablesProvider> executablesProviderMap = new HashMap<IProject, IProjectExecutablesProvider>();
	private List<IExecutablesChangeListener> changeListeners = Collections.synchronizedList(new ArrayList<IExecutablesChangeListener>());
	private List<IProjectExecutablesProvider> executableProviders;
	private List<ISourceFilesProvider> sourceFileProviders;
	private List<ISourceFileRemappingFactory> sourceFileRemappingFactories;
	private List<IExecutableImporter> executableImporters;
	
	
	/**
	 * Map of launch config names to the path locator memento string in the
	 * launch config, recorded in the most recent launch config change
	 * notification. We use this to ensure we flush source file mappings only
	 * when the launch config change involves a change to the source locators.
	 */
	private Map<String, String> locatorMementos = new HashMap<String,String>();
	

	/**
	 * A cache of the executables in the workspace, categorized by project. 
	 * 
	 * <p>
	 * This cache is updated by scheduling an asynchronous search. SearchJob is
	 * the only class that should <i>modify</i> this collection, including the
	 * sub collections of Executable objects. The collection can be read from
	 * any thread at any time. All access (read or write) must be serialized by
	 * synchronizing on the Map object.
	 * <p>
	 * The same Executable may appear more than once.
	 */
	private Map<IProject, List<Executable>> executablesMap = new HashMap<IProject, List<Executable>>();
	
	/**
	 * Provide a flat list of the executables in {@link #executablesMap}, with
	 * duplicates removed. That is effectively the list of all executables in
	 * the workspace that we know of as of now.
	 * 
	 * @return
	 */
	private List<Executable> flattenExecutablesMap() {
		List<Executable> result = new ArrayList<Executable>(executablesMap.size() * 5); // most projects will have less than five executables  
		synchronized (executablesMap) {
			for (List<Executable> exes : executablesMap.values()) {
				for (Executable exe : exes) {
					if (!result.contains(exe)) {
						result.add(exe);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Job which searches through CDT projects for executables. Only one thread
	 * should be running this job at any one time. Running job should be
	 * cancelled and verified terminated before initiating another.
	 */
	class SearchJob extends Job {
		SearchJob() {
			super("Executables Search"); //$NON-NLS-1$
		}


		/**
		 * The projects given to us when scheduled. If null, flush our entire
		 * cache and search all projects
		 */
		private IProject[] projectsToRefresh;
		
		@Override
		public IStatus run(IProgressMonitor monitor) {
			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Search for executables started"); //$NON-NLS-1$
			
			IStatus status = Status.OK_STATUS;

			// The executables we know of now. We'll compare the search results
			// to this and see if we need to notify change listeners
			List<Executable> before = flattenExecutablesMap();
			
			// Get the CDT projects in the workspace that we have no cached 
			// results for (are not in 'executablesMap'). Also, we may have been
			// asked to refresh the cache for some projects we've search before
			List<IProject> projects = new ArrayList<IProject>();
			synchronized (executablesMap) {
				if (projectsToRefresh == null) {
					executablesMap.clear();
				}
				else {
					for (IProject project : projectsToRefresh) {
						executablesMap.remove(project);
					}
				}
				
				// Get the list of projects we plan to search
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (!executablesMap.containsKey(project) && CoreModel.hasCNature(project)) {
						projects.add(project);
					}
				}
			}


			SubMonitor subMonitor = SubMonitor.convert(monitor, projects.size());

			for (IProject project : projects) {
				if (subMonitor.isCanceled()) {
					if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Search for executables canceled"); //$NON-NLS-1$
					status = Status.CANCEL_STATUS;
					break; // we've already changed our model; stop searching but proceed to notify listeners that the model changed
				}
				
				subMonitor.subTask("Checking project: " + project.getName()); //$NON-NLS-1$

				// get the executables provider for this project
				IProjectExecutablesProvider provider = getExecutablesProviderForProject(project);
				if (provider != null) {
					if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Getting executables for project: " + project.getName() + " using " + provider.toString());					 //$NON-NLS-1$ //$NON-NLS-2$

					List<Executable> executables = provider.getExecutables(project, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
					// store the list of executables for this project
					synchronized (executablesMap) {
						executablesMap.put(project, executables);
					}
				}
			}

			
			// See if, after all that work, there's a net change in the
			// executables list. If so, notify listeners.
			List<Executable> after = flattenExecutablesMap();
			List<Executable> removed = before;
			List<Executable> added = new ArrayList<Executable>(after.size());
			for (Executable a : after) {
				if (!removed.remove(a)) {
					added.add(a);
				}
			}
			// notify the listeners
			synchronized (changeListeners) {
				if (removed.size() > 0 || added.size() > 0) {
					for (IExecutablesChangeListener listener : changeListeners) {
						// New interface
						if (listener instanceof IExecutablesChangeListener2) {
							if (removed.size() > 0) {
								((IExecutablesChangeListener2)listener).executablesRemoved(removed);
							}
							if (added.size() > 0) {
								((IExecutablesChangeListener2)listener).executablesAdded(added);
							}
						}
						// Old interface
						listener.executablesListChanged();
					}
				}
			}

			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Search for executables finished"); //$NON-NLS-1$			

			return status;
		}

		/**
		 * Schedules the search job. Use this, not the standard Job.schedule()
		 * method.
		 * 
		 * @param projectsToRefresh
		 *            if null, all CDT projects in the workspace are searched.
		 *            If not null, we search only newly present projects and the
		 *            projects provided (even if searched before). Empty list
		 *            can be passed to search only newly present projects.
		 */
		public void schedule(IProject[] projectsToRefresh) {
			this.projectsToRefresh = projectsToRefresh;
			super.schedule();
		}
	};
	
	/** The search job. We only let one of these run at any one time */
	private SearchJob searchJob = new SearchJob();

	/** Lock used to serialize the search jobs */
	private Object searchSchedulingLock = new Object();

	/** The singleton */
	private static ExecutablesManager executablesManager;

	/**
	 * @return the singleton manager
	 */
	public static ExecutablesManager getExecutablesManager() {
		if (executablesManager == null)
			executablesManager = new ExecutablesManager();
		return executablesManager;
	}

	public ExecutablesManager() {
		searchJob.setPriority(Job.SHORT);
		
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
		CModelManager.getDefault().addElementChangedListener(this);
		CoreModel.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(this,
				CProjectDescriptionEvent.APPLIED);
	
		// Listen for changes to the global source locators. These locators
		// affect how source files are found locally. The Executable objects
		// cache their local source file paths and rely on us to tell them to
		// flush those caches when applicable locators change.
		CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().addParticipants(new ISourceLookupParticipant[] { new ISourceLookupParticipant(){

			@Override
			public void init(ISourceLookupDirector director) {}
			@Override
			public Object[] findSourceElements(Object object) { return new Object[0]; }
			@Override
			public String getSourceName(Object object) throws CoreException { return ""; } //$NON-NLS-1$
			@Override
			public void dispose() {}
			@Override
			public void sourceContainersChanged(ISourceLookupDirector director) {
				// Unfortunately, it would be extremely difficult/costly to 
				// determine which binaries are effected by the source locator 
				// change, so we have to tell all Executables to flush
				flushExecutablesSourceMappings();
			}
		} });
		
		// Source locators are also in launch configurations, and those too come
		// into play when an Executable looks for a source file locally. So,
		// listen for changes in those locators, too.
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(new ILaunchConfigurationListener() {
			@Override
			public void launchConfigurationChanged(ILaunchConfiguration configuration) {
				// Expect lots of noise for working copies. We only care about 
				// changes to actual configs
				if (configuration.isWorkingCopy()) {
					return;
				}
				
				// If the source locators in the launch config were not modified, then no-op   
				try {
					String configName = configuration.getName();
					String mementoBefore = locatorMementos.get(configName);
					String mementoNow = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, ""); //$NON-NLS-1$
					if (mementoNow.equals(mementoBefore)) {
						return; // launch config change had no affect on source locators
					}
					locatorMementos.put(configName, mementoNow); 
				} catch (CoreException e) {
					CDebugCorePlugin.log(e);
				}

				// TODO: For now, just tell all Executables to flush. Look 
				// into identifying which binary the config is associated 
				// with so we can flush only that Executable
				flushExecutablesSourceMappings();
			}
			@Override
			public void launchConfigurationRemoved(ILaunchConfiguration configuration) { configAddedOrRemoved(configuration); }
			@Override
			public void launchConfigurationAdded(ILaunchConfiguration configuration)  { configAddedOrRemoved(configuration); }
			private void configAddedOrRemoved(ILaunchConfiguration configuration) {
				// Expect lots of noise for working copies. We only care about 
				// changes to actual configs
				if (configuration.isWorkingCopy()) {
					return;
				}
				
				// The addition or removal of a launch config could affect 
				// how files are found. It would be extremely costly to 
				// determine here whether it will or not, so assume it will. 
				
				// TODO: For now, just tell all Executables to flush. Look 
				// into identifying which binary the config is associated 
				// with so we can flush only that Executable
				flushExecutablesSourceMappings();
			}
		});
		
		// schedule a refresh so we get up to date
		scheduleExecutableSearch(null);
	}
	
	/**
	 * Tell all Executable objects to flush their source file mappings, then
	 * notify our listeners that the executables changed. Even though the
	 * binaries may not have actually changed, the impact to a client of
	 * Executable is the same. If the client has cached any of the source file
	 * information the Executable provided, that info can no longer be trusted.
	 * The primary purpose of an Executable is to provide source file path
	 * information--not only the compile paths burned into the executable but
	 * also the local mappings of those paths. 
	 */
	private void flushExecutablesSourceMappings() {
		List<Executable> exes = flattenExecutablesMap();
		for (Executable exe : exes) {
			exe.setRemapSourceFiles(true);
		}
		synchronized (changeListeners) {
			for (IExecutablesChangeListener listener : changeListeners) {
				listener.executablesChanged(exes);
			}
		}
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
	 * Gets the list of executables in the workspace. This method doesn't
	 * initiate a search. It returns the cached results of the most recent
	 * search, or waits for the ongoing search to complete.
	 * 
	 * @param wait
	 *            Whether or not to wait if the cache is in the process of being
	 *            updated when this method is called. When true, the call will
	 *            block until the update is complete. When false, it will return
	 *            the current cache. Callers on the UI thread should pass false
	 *            to avoid temporarily freezing the UI. Note that clients can
	 *            register as a {@link IExecutablesChangeListener} or
	 *            {@link IExecutablesChangeListener2}to get notifications when
	 *            the cache changes.
	 * @return the list of executables; may be empty. List will not have
	 *         duplicates.
	 * @since 7.0
	 */
	public Collection<Executable> getExecutables(boolean wait) {
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null, Boolean.valueOf(wait));		
		
		// Wait for running search to finish, if asked to
		if (wait && searchJob.getState() != Job.NONE) {
			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Waiting for executable search to finish..."); //$NON-NLS-1$
			try {
				searchJob.join();
			} catch (InterruptedException e) {
			}
			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "...executable search finished."); //$NON-NLS-1$
		}
		
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceExit(null);
		return flattenExecutablesMap();
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

				@Override
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
			scheduleExecutableSearch(null);
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
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null, executable);
		
		String[] result = new String[0];

		synchronized (sourceFileProviders) {
			Collections.sort(sourceFileProviders, new Comparator<ISourceFilesProvider>() {

				@Override
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
					if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Got " + sourceFiles.length + " files from " + provider.toString()); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
			}
			monitor.done();
		}

		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceExit(null, result);
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
				if (!result.isOK()) {
					status.add(result);
				}
			}
		}
		
		// We don't need to directly call our listeners. The file removal will
		// cause a C model change, which we will react to by then calling the
		// listeners
		
		return status;
	}

	/**
	 * Initiates an asynchronous search of workspace CDT projects for
	 * executables. If a search is ongoing, it's cancelled and a new one is
	 * started. In all cases, this method returns quickly (does not wait/block).
	 * 
	 * <p>
	 * Listeners are notified when the search is complete and there is a change
	 * in the collection of found executables. The results of the search can be
	 * obtained by calling {@link #getExecutables(boolean)}.
	 * 
	 * @param projectsToRefresh
	 *            if null, we discard our entire Executables cache and search
	 *            all CDT projects in the workspace. If not null, we purge our
	 *            cache for only the given projects then search in all CDT
	 *            projects for which we have no cache. Passing a project that we
	 *            have no cache for is innocuous. In all cases, we search for
	 *            executables in any newly available projects. This parameter is
	 *            simply a way to get us to <i>not</i> skip one or more projects
	 *            we already have the executables list for.
	 * 
	 * @since 7.0
	 */
	public void refresh(List<IProject> projectsToRefresh) {
		scheduleExecutableSearch(projectsToRefresh != null ? projectsToRefresh.toArray(new IProject[projectsToRefresh.size()]) : null);
	}

	/**
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 * @since 7.0
	 * @deprecated we no longer listen directly for platform resource changes
	 *             but rather C model changes
	 */
	@Override
	@Deprecated
	public void resourceChanged(IResourceChangeEvent event) {}

	/**
	 * @since 7.0
	 */
	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null, event);
		
		// this handles the cases where the active build configuration changes,
		// and when new projects are created or loaded at startup.
		int eventType = event.getEventType();

		if (eventType == CProjectDescriptionEvent.APPLIED) {
			
			// see if the active build config has changed
			ICProjectDescription newDesc = event.getNewCProjectDescription();
			ICProjectDescription oldDesc = event.getOldCProjectDescription();
			if (oldDesc != null && newDesc != null) {
				String newConfigName = newDesc.getActiveConfiguration().getName();
				String oldConfigName = oldDesc.getActiveConfiguration().getName();
				if (!newConfigName.equals(oldConfigName)) {
					if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Scheduling refresh because active build configuration changed");					 //$NON-NLS-1$
					scheduleExecutableSearch(new IProject[]{newDesc.getProject()});
				}
			} else if (newDesc != null && oldDesc == null) {
				// project just created
				scheduleExecutableSearch(null);
				if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Scheduling refresh because project " + newDesc.getProject().getName() + " created");  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
	}

	/**
	 * Initiates an asynchronous search of workspace CDT projects for
	 * executables. For details, see {@link #refresh(List)}, which is a public
	 * wrapper for this internal method. This method is more aptly named and
	 * takes an array instead of a list
	 */
	private void scheduleExecutableSearch(final IProject[] projectsToRefresh) {
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null, projectsToRefresh);

		// Don't schedule multiple search jobs simultaneously. If one is
		// running, cancel it, wait for it to terminate, then schedule a new
		// one. However we must not block our caller, so spawn an intermediary
		// thread to do that leg work. This isn't an efficient design, but these
		// searches aren't done in high volume.
		Job job = new Job("Executable search scheduler") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronized (searchSchedulingLock) {
					searchJob.cancel();
					if (searchJob.getState() != Job.NONE) {
						try {
							if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Waiting for canceled job to terminate"); //$NON-NLS-1$
							searchJob.join();
						} catch (InterruptedException e) {
						}
					}
					if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Scheduling new search job"); //$NON-NLS-1$
					searchJob.schedule(projectsToRefresh);
				}
				
				return Status.OK_STATUS;
			}
			
		};
		job.setPriority(Job.SHORT);
		job.schedule();
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

	/**
	 * We listen to C model changes and see if they affect what executables are
	 * in the workspace, and/or if the executables we already know of have
	 * changed.
	 * 
	 * @since 7.1
	 */
	@Override
	public void elementChanged(ElementChangedEvent event) {
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null);
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "event = \n" + event); // must be done separately because of traceEntry() limitation //$NON-NLS-1$ 
		
		// Examine the event and figure out what needs to be done
		Set<IProject> refreshProjects = new HashSet<IProject>(5); 
		Set<Executable> executablesChanged = new HashSet<Executable>(5);
		Set<Executable> executablesRemoved = new HashSet<Executable>(5);
		processDeltas(event.getDelta().getAddedChildren(), null, refreshProjects, executablesRemoved, executablesChanged);
		processDeltas(event.getDelta().getChangedChildren(), null, refreshProjects, executablesRemoved, executablesChanged);
		processDeltas(event.getDelta().getRemovedChildren(), null, refreshProjects, executablesRemoved, executablesChanged);
		
		// Schedule executable searches in projects 
		if (refreshProjects.size() > 0) {
			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "One or more projects need to be re-searched");  //$NON-NLS-1$
			scheduleExecutableSearch(refreshProjects.toArray(new IProject[refreshProjects.size()]));
		}
		
		// Invalidate the source file cache in changed Executables and inform
		// listeners
		if (executablesChanged.size() > 0) {
			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "One or more executables changed");  //$NON-NLS-1$			
			for (Executable exec : executablesChanged) {
				exec.setRefreshSourceFiles(true);
			}
			List<Executable> list = Arrays.asList(executablesChanged.toArray(new Executable[executablesChanged.size()]));
			synchronized (changeListeners) {
				for (IExecutablesChangeListener listener : changeListeners) {
					listener.executablesChanged(list);
				}
			}
		}
		if (executablesRemoved.size() > 0) {
			// Update our model (i.e., our collection of Executables) and inform listeners
			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "One or more executables were removed");  //$NON-NLS-1$			
			synchronized (executablesMap) {
				for (Executable executableRemoved : executablesRemoved) {
					List<Executable> execs = executablesMap.get(executableRemoved.getProject());
					assert execs != null : "considering the list was used in populating 'executablesRemoved', how could it be gone now?"; //$NON-NLS-1$
					if (execs != null) {
						execs.remove(executableRemoved);
					}
				}
			}
			List<Executable> list = Arrays.asList(executablesRemoved.toArray(new Executable[executablesRemoved.size()]));
			synchronized (changeListeners) {
				for (IExecutablesChangeListener listener : changeListeners) {
					// call newer interface if supported
					if (listener instanceof IExecutablesChangeListener2) {
						((IExecutablesChangeListener2)listener).executablesRemoved(list);
					}
					// and call older interface, which is less informative
					listener.executablesListChanged();
				}
			}
		}
		return;
	}
	
	/**
	 * Drills down a hierarchy of CDT model change events to determine the
	 * course of action.
	 * 
	 * @param deltas
	 *            CDT model events received by the viewer
	 * @param cproject
	 *            the project the resources in [deltas] belong to
	 * @param projectsToRefresh
	 *            implementation populates (appends) this list with the projects
	 *            that need to be searched for executables. Note that Executable
	 *            objects are created by an async job. The best we can do here
	 *            is identify the projects that need to be searched. We can't
	 *            provide a list of added Executables objects since they haven't
	 *            been created yet.
	 * @param removedExecutables
	 *            implementation populates (appends) this list with the
	 *            Executable objects that have been removed, requiring listeners
	 *            to be notified.
	 * @param changedExecutables
	 *            implementation populates (appends) this list with the
	 *            Executable objects that have changed, requiring listeners to
	 *            be notified.
	 */
	private void processDeltas(ICElementDelta[] deltas, ICProject cproject, final Set<IProject> projectsToRefresh, final Set<Executable> removedExecutables, final Set<Executable> changedExecutables) {
		for (ICElementDelta delta : deltas) {
			ICElement element = delta.getElement();
			if (element instanceof ICProject) {
				// When a project is deleted, we get a REMOVED delta for the
				// project only--none for the elements in the project.  
				IProject project = ((ICProject)element).getProject();
				if (delta.getKind() == ICElementDelta.REMOVED) {
					projectsToRefresh.add(project);
					List<Executable> execs = null;
					synchronized (executablesMap) {
						execs = executablesMap.get(project);
					}
					if (execs != null) {
						for (Executable exec : execs) {
							if (exec.getResource().equals(delta.getElement().getResource())) {
								removedExecutables.add(exec);												
								break;
							}
						}

					}
					// Note that it's not our job to update 'executablesMap'. 
					// The async exec search job will do that.					
				}
			}
			else if (element instanceof IBinary) {
				IProject project = cproject.getProject();
				int deltaKind = delta.getKind();
				switch (deltaKind) {
				case ICElementDelta.ADDED:
					projectsToRefresh.add(project);
					break;
				case ICElementDelta.REMOVED: 
				case ICElementDelta.CHANGED: {
					List<Executable> execs = null;
					synchronized (executablesMap) {
						execs = executablesMap.get(project);
						if (execs == null) {
							// Somehow, we missed the addition of the project. 
							// Request that the project be researched for 
							// executables
							projectsToRefresh.add(project);
						}
						else {
							// See if it's one of the executables we already know
							// is in the project. If so, we'll update our 
							// executables map (if removed) and notifying
							// listeners
							for (Executable exec : execs) {
								if (exec.getResource().equals(delta.getElement().getResource())) {
									if (deltaKind == ICElementDelta.REMOVED)
										removedExecutables.add(exec);
									else
										changedExecutables.add(exec);
									break;
								}
							}
						}
					}
					break;
				}
				}
			}
			if (element instanceof ICProject) {
				cproject = (ICProject)element;
			}
			// recursively call ourselves to handle this delta's children
			processDeltas(delta.getAffectedChildren(), cproject, projectsToRefresh, removedExecutables, changedExecutables); 
		}
	}
}
