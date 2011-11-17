/*******************************************************************************
 *  Copyright (c) 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * The RefreshScopeManager provides access to settings pertaining to refreshes performed during a build. Each
 * project may have a set of resources associated with it that are the set of resources to be refreshed. An
 * exclusion mechanism exists that allows for one to specify arbitrarily complicated, nested logic that
 * determines whether or not a given resource is refreshed according to previously specified rules.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 *
 * @author crecoskie
 * @since 5.3
 *
 */
public class RefreshScopeManager {

	public static final String EXCLUSION_CLASS = "exclusionClass"; //$NON-NLS-1$
	public static final Object EXCLUSION_FACTORY = "exclusionFactory"; //$NON-NLS-1$
	public static final String EXTENSION_ID = "RefreshExclusionFactory"; //$NON-NLS-1$
	public static final String FACTORY_CLASS = "factoryClass"; //$NON-NLS-1$
	public static final String FILE_VALUE = "FILE"; //$NON-NLS-1$
	private static RefreshScopeManager fInstance;
	public static final String FOLDER_VALUE = "FOLDER"; //$NON-NLS-1$
	public static final String INSTANCE_CLASS = "instanceClass"; //$NON-NLS-1$
	public static final String OTHER_VALUE = "OTHER"; //$NON-NLS-1$
	public static final String PROJECT_VALUE = "PROJECT"; //$NON-NLS-1$
	public static final String REFRESH_SCOPE_STORAGE_NAME = "refreshScope"; //$NON-NLS-1$
	public static final String RESOURCE_ELEMENT_NAME = "resource"; //$NON-NLS-1$
	public static final String RESOURCE_TYPE_ATTRIBUTE_NAME = "resourceType"; //$NON-NLS-1$
	public static final String VERSION_ELEMENT_NAME = "version"; //$NON-NLS-1$
	public static final String VERSION_NUMBER_ATTRIBUTE_NAME = "versionNumber"; //$NON-NLS-1$
	public static final String WORKSPACE_PATH_ATTRIBUTE_NAME = "workspacePath"; //$NON-NLS-1$

	public static synchronized RefreshScopeManager getInstance() {
		if (fInstance == null) {
			fInstance = new RefreshScopeManager();
		}

		return fInstance;
	}

	private HashMap<String, RefreshExclusionFactory> fClassnameToExclusionFactoryMap;
	private boolean fIsLoaded = false;

	private boolean fIsLoading = false;
	private HashMap<IProject, LinkedHashSet<IResource>> fProjectToResourcesMap;
	private HashMap<IResource, List<RefreshExclusion>> fResourceToExclusionsMap;

	private int fVersion = 1;

	private RefreshScopeManager() {
		fClassnameToExclusionFactoryMap = new HashMap<String, RefreshExclusionFactory>();
		loadExtensions();
		try {
			loadSettings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		// Add a resource change listener that will try to load settings for projects when they open
		// and delete settings when projects are deleted or closed.
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				new IResourceChangeListener() {

					@Override
					public void resourceChanged(IResourceChangeEvent event) {

						if (event.getType() == IResourceChangeEvent.PRE_CLOSE
								|| event.getType() == IResourceChangeEvent.PRE_DELETE) {
							IProject project = event.getResource().getProject();

							try {
								if (project.exists() && project.isOpen()
										&& project.hasNature(CProjectNature.C_NATURE_ID)) {
									clearDataForProject(project);
								}
							} catch (CoreException e) {
								// should never happen due to checks above
							}

							return;
						}

						IResourceDelta delta = event.getDelta();

						if (delta != null) {
							try {
								delta.accept(new IResourceDeltaVisitor() {

									@Override
									public boolean visit(IResourceDelta delta) throws CoreException {
										if (delta.getResource() instanceof IProject) {
											IProject project = (IProject) delta.getResource();

											if (delta.getKind() == IResourceDelta.ADDED
													|| (delta.getKind() == IResourceDelta.CHANGED && ((delta
															.getFlags() & IResourceDelta.OPEN) != 0))) {

												loadSettings(ResourcesPlugin.getWorkspace()
														.getRoot(), project);
												return false;

											}

										}

										else if (delta.getResource() instanceof IWorkspaceRoot) {
											return true;
										}

										return false;
									}

								});
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

				},
				IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_CLOSE
						| IResourceChangeEvent.PRE_DELETE);
	}

	public synchronized void addExclusion(IResource resource, RefreshExclusion exclusion) {
		getResourcesToExclusionsMap();

		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if (exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}

		exclusions.add(exclusion);
	}

	public synchronized void addResourceToRefresh(IProject project, IResource resource) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = fProjectToResourcesMap.get(project);

		if (resourceSet == null) {
			resourceSet = new LinkedHashSet<IResource>();
			fProjectToResourcesMap.put(project, resourceSet);
		}

		resourceSet.add(resource);

	}

	public synchronized void clearAllData() {
		clearAllResourcesToRefresh();
		clearAllExclusions();
		fIsLoaded = false;
	}

	public synchronized void clearAllExclusions() {
		if (fResourceToExclusionsMap != null)
			fResourceToExclusionsMap.clear();
	}

	public synchronized void clearAllResourcesToRefresh() {
		getProjectToResourcesMap();
		fProjectToResourcesMap.clear();
	}

	private synchronized void clearDataForProject(IProject project) {
		clearResourcesToRefresh(project);
		clearExclusionsForProject(project);
	}

	public synchronized void clearExclusions(IResource resource) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if (exclusions != null) {
			exclusions.clear();
		}
	}

	public synchronized void clearExclusionsForProject(IProject project) {
		getResourcesToExclusionsMap();
		List<IResource> resourcesToRemove = new LinkedList<IResource>();

		for (IResource resource : fResourceToExclusionsMap.keySet()) {
			IProject project2 = resource.getProject();
			if (project2.equals(project)) {
				resourcesToRemove.add(resource);
			}
		}

		for (IResource resource : resourcesToRemove) {
			fResourceToExclusionsMap.remove(resource);
		}
	}

	public synchronized void clearResourcesToRefresh(IProject project) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = null;

		fProjectToResourcesMap.put(project, resourceSet);
	}

	public synchronized void deleteResourceToRefresh(IProject project, IResource resource) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = fProjectToResourcesMap.get(project);

		if (resourceSet == null) {
			resourceSet = new LinkedHashSet<IResource>();
			return;
		}

		resourceSet.remove(resource);
	}

	public synchronized RefreshExclusion getExclusionForClassName(String className) {
		RefreshExclusionFactory factory = getFactoryForClassName(className);

		if (factory == null) {
			return null;
		}

		return factory.createNewExclusion();
	}

	public synchronized List<RefreshExclusion> getExclusions(IResource resource) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if (exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}

		return exclusions;
	}

	public synchronized RefreshExclusionFactory getFactoryForClassName(String className) {
		RefreshExclusionFactory factory = fClassnameToExclusionFactoryMap.get(className);

		return factory;
	}

	public synchronized ExclusionInstance getInstanceForClassName(String className) {
		RefreshExclusionFactory factory = getFactoryForClassName(className);

		if (factory == null) {
			return null;
		}

		return factory.createNewExclusionInstance();
	}

	private HashMap<IProject, LinkedHashSet<IResource>> getProjectToResourcesMap() {
		if (fProjectToResourcesMap == null) {
			fProjectToResourcesMap = new HashMap<IProject, LinkedHashSet<IResource>>();
		}

		return fProjectToResourcesMap;
	}

	public IWorkspaceRunnable getRefreshRunnable(final IProject project) {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			/**
			 * @param q
			 * @param resource
			 * @throws CoreException
			 */
			private void refreshResources(IResource resource, List<RefreshExclusion> exclusions,
					IProgressMonitor monitor) throws CoreException {
				if (resource instanceof IContainer) {
					IContainer container = (IContainer) resource;

					if (shouldResourceBeRefreshed(resource)) {
						resource.refreshLocal(IResource.DEPTH_ONE, monitor);

					}

					for (IResource child : container.members()) {
						refreshResources(child, exclusions, monitor);
					}
				}
			}

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {

				List<IResource> resourcesToRefresh = getResourcesToRefresh(project);
				for (IResource resource : resourcesToRefresh) {
					List<RefreshExclusion> exclusions = getExclusions(resource);
					refreshResources(resource, exclusions, monitor);
				}

			}
		};

		return runnable;
	}

	public synchronized ISchedulingRule getRefreshSchedulingRule(IProject project) {
		return new MultiRule(getResourcesToRefresh(project).toArray(new ISchedulingRule[0]));
	}

	private HashMap<IResource, List<RefreshExclusion>> getResourcesToExclusionsMap() {
		if (fResourceToExclusionsMap == null) {
			fResourceToExclusionsMap = new HashMap<IResource, List<RefreshExclusion>>();
		}

		return fResourceToExclusionsMap;
	}

	/**
	 * Returns the set of resources that should be refreshed for a project. These resources might have
	 * associated exclusions.
	 *
	 * @param project
	 * @return List<IResource>
	 */
	public synchronized List<IResource> getResourcesToRefresh(IProject project) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resources = fProjectToResourcesMap.get(project);

		if (resources == null) {
			// there are no settings yet for the project, setup the defaults
			resources = new LinkedHashSet<IResource>();
			resources.add(project);
			fProjectToResourcesMap.put(project, resources);
		}

		return new LinkedList<IResource>(resources);
	}

	public int getVersion() {
		return fVersion;
	}

	public synchronized void loadExtensions() {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				CCorePlugin.PLUGIN_ID, EXTENSION_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension extension2 : extensions) {
				IConfigurationElement[] configElements = extension2.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {

					if (configElement.getName().equals(EXCLUSION_FACTORY)) {
						String exclusionClassName = configElement.getAttribute(EXCLUSION_CLASS);
						String factoryClassName = configElement.getAttribute(FACTORY_CLASS);
						String instanceClassName = configElement.getAttribute(INSTANCE_CLASS);

						if (factoryClassName != null) {
							try {
								Object execExt = configElement
										.createExecutableExtension(FACTORY_CLASS);
								if ((execExt instanceof RefreshExclusionFactory)) {
									RefreshExclusionFactory exclusionFactory = (RefreshExclusionFactory) execExt;

									if (exclusionClassName != null) {
										fClassnameToExclusionFactoryMap.put(exclusionClassName,
												exclusionFactory);
									}

									if (instanceClassName != null) {
										fClassnameToExclusionFactoryMap.put(instanceClassName,
												exclusionFactory);
									}
								}
							} catch (CoreException e) {
								CCorePlugin.log(e);
							}
						}
					}
				}
			}
		}
	}

	public synchronized void loadSettings() throws CoreException {
		if (!fIsLoaded && !fIsLoading) {
			fIsLoading = true;
			// iterate through all projects in the workspace. If they are C projects, attempt to load settings
			// from them.
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

			for (IProject project : workspaceRoot.getProjects()) {
				loadSettings(workspaceRoot, project);
			}

			fIsLoaded = true;
			fIsLoading = false;
		}
	}

	/**
	 * @param workspaceRoot
	 * @param project
	 * @throws CoreException
	 */
	public synchronized void loadSettings(IWorkspaceRoot workspaceRoot, IProject project)
			throws CoreException {
		if (project.isOpen()) {
			if (project.hasNature(CProjectNature.C_NATURE_ID)) {
				CProjectDescriptionManager descriptionManager = CProjectDescriptionManager
						.getInstance();
				ICProjectDescription projectDescription = descriptionManager.getProjectDescription(
						project, false);

				if (projectDescription == null) {
					/*
					 * then there's nothing to load... could be an old project that pre-dates the project
					 * description's existence, or the project could have been just created but the project
					 * description hasn't been created yet. Either way, just do nothing, because there's
					 * nothing to load.
					 */
					return;
				}

				ICStorageElement storageElement = projectDescription.getStorage(
						REFRESH_SCOPE_STORAGE_NAME, true);

				// walk the tree and load the settings

				// for now ignore the version attribute, as we only have version 1 at this time

				// iterate through the child nodes
				ICStorageElement[] children = storageElement.getChildren();

				for (ICStorageElement child : children) {

					if (child.getName().equals(RESOURCE_ELEMENT_NAME)) {

						// get the resource path
						String resourcePath = child.getAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME);

						if (resourcePath == null) {
							// error... skip this resource
							continue;

						}

						else {
							String resourceTypeString = child
									.getAttribute(RESOURCE_TYPE_ATTRIBUTE_NAME);

							if (resourceTypeString == null) {
								// we'll do our best, but we won't be able to create handles to non-existent
								// resources
								resourceTypeString = OTHER_VALUE;
							}

							// find the resource
							IResource resource = null;

							if (resourceTypeString.equals(PROJECT_VALUE)) {
								resource = workspaceRoot.getProject(resourcePath);
							}

							else if (resourceTypeString.equals(FILE_VALUE)) {
								resource = workspaceRoot.getFile(new Path(resourcePath));
							}

							else if (resourceTypeString.equals(FOLDER_VALUE)) {
								resource = workspaceRoot.getFolder(new Path(resourcePath));
							}

							else {
								// Find arbitrary resource.
								// The only way to do this is to ask the workspace root to find
								// it, if it exists. If it doesn't exist, we have no way of
								// creating a handle to the right type of object, so we must
								// give up. In practice, this would likely happen if we had
								// a virtual group resource that has been deleted somehow since
								// the settings were created, and since the resource is virtual,
								// it's impossible to refresh it if it doesn't exist anyway.
								resource = workspaceRoot.findMember(resourcePath);
							}

							if (resource == null) {
								// error.. skip this resource
								continue;
							}

							else {
								addResourceToRefresh(project, resource);

								// load any exclusions
								List<RefreshExclusion> exclusions = RefreshExclusion.loadData(
										child, null, resource, this);

								// add them
								for (RefreshExclusion exclusion : exclusions) {
									addExclusion(resource, exclusion);
								}
							}
						}
					}
				}
			}
		}
	}

	public synchronized void persistSettings(ICProjectDescription projectDescription)
			throws CoreException {
		getProjectToResourcesMap();
		getResourcesToExclusionsMap();
		IProject project = projectDescription.getProject();

		if (!project.exists()) {
			return;
		}

		// serialize all settings for the project to the C Project Description
		if (project.isOpen()) {
			if (project.hasNature(CProjectNature.C_NATURE_ID)) {

				ICStorageElement storageElement = projectDescription.getStorage(
						REFRESH_SCOPE_STORAGE_NAME, true);
				storageElement.clear();

				storageElement.setAttribute(VERSION_NUMBER_ATTRIBUTE_NAME,
						Integer.toString(fVersion));

				for (IResource resource : fProjectToResourcesMap.get(project)) {

					// create a resource node
					ICStorageElement resourceElement = storageElement
							.createChild(RESOURCE_ELEMENT_NAME);
					resourceElement.setAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME, resource
							.getFullPath().toString());

					String resourceTypeString = null;

					if (resource instanceof IFile) {
						resourceTypeString = FILE_VALUE;
					}

					else if (resource instanceof IFolder) {
						resourceTypeString = FOLDER_VALUE;
					}

					else if (resource instanceof IProject) {
						resourceTypeString = PROJECT_VALUE;
					}

					else {
						resourceTypeString = OTHER_VALUE;
					}

					resourceElement.setAttribute(RESOURCE_TYPE_ATTRIBUTE_NAME, resourceTypeString);

					// populate the node with any exclusions
					List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
					if (exclusions != null) {
						for (RefreshExclusion exclusion : exclusions) {
							exclusion.persistData(resourceElement);
						}
					}

				}

			}
		}

	}

	public synchronized void removeExclusion(IResource resource, RefreshExclusion exclusion) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if (exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}

		exclusions.remove(exclusion);
	}

	public synchronized void setExclusions(IResource resource, List<RefreshExclusion> newExclusions) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = new LinkedList<RefreshExclusion>(newExclusions);

		fResourceToExclusionsMap.put(resource, exclusions);
	}

	public synchronized void setResourcesToRefresh(IProject project, List<IResource> resources) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = new LinkedHashSet<IResource>(resources);

		fProjectToResourcesMap.put(project, resourceSet);
	}

	public synchronized boolean shouldResourceBeRefreshed(IResource resource) {
		IProject project = resource.getProject();
		List<IResource> resourcesToRefresh = getResourcesToRefresh(project);
		boolean isInSomeTree = false;
		IResource topLevelResource = null;

		for (IResource resourceToRefresh : resourcesToRefresh) {
			if (resourceToRefresh.equals(resource)) {
				isInSomeTree = true;
				topLevelResource = resource;
				break;
			}

			// see if the resource is a child of our top level resources
			if (resourceToRefresh instanceof IContainer) {
				IContainer container = (IContainer) resourceToRefresh;
				if (container.getFullPath().isPrefixOf(resource.getFullPath())) {
					isInSomeTree = true;
					topLevelResource = resourceToRefresh;
					break;
				}
			}

		}

		if (!isInSomeTree) {
			return false;
		}

		// get any exclusions
		boolean isExcluded = false;

		for (RefreshExclusion exclusion : getExclusions(topLevelResource)) {
			if (exclusion.testExclusionChain(resource)) {
				isExcluded = true;
				break;
			}
		}

		return !isExcluded;

	}

}
