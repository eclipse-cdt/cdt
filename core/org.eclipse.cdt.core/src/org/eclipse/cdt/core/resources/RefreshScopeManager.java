/*******************************************************************************
 *  Copyright (c) 2011, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
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
	/**
	 * @since 5.4
	 */
	public static final String CONFIGURATION_ELEMENT = "configuration"; //$NON-NLS-1$
	/**
	 * @since 5.4
	 */
	public static final String CONFIGURATION_ELEMENT_NAME = "configurationName"; //$NON-NLS-1$

	public static synchronized RefreshScopeManager getInstance() {
		if (fInstance == null) {
			fInstance = new RefreshScopeManager();
		}

		return fInstance;
	}

	private HashMap<String, RefreshExclusionFactory> fClassnameToExclusionFactoryMap;
	private boolean fIsLoaded = false;

	private boolean fIsLoading = false;
	private HashMap<IProject,HashMap<String,HashMap<IResource, List<RefreshExclusion>>>> fProjToConfToResToExcluMap;	
	private int fVersion = 2;	

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

	/**
	 * @since 5.4
	 */
	public synchronized void addExclusion(IProject project, String configName, IResource resource, RefreshExclusion exclusion) {
		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(project,configName);

		List<RefreshExclusion> exclusions = resourceMap.get(resource);
		if (exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			resourceMap.put(resource, exclusions);
		}
		
		exclusions.add(exclusion);
	}

	/**
	 * @since 5.4
	 */
	// We are adding a new resource.
	public synchronized void addResourceToRefresh(IProject project, String configName, IResource resource) {
		
		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(project, configName);		
		
		if (!resourceMap.containsKey(resource)) {
			// create a new one:
			LinkedList<RefreshExclusion> exclusions = new LinkedList<RefreshExclusion>();
			resourceMap.put(resource, exclusions);
		}
	}
	
	public synchronized void clearAllData() {
		getProjectToConfigurationToResourcesMap();
		fProjToConfToResToExcluMap.clear();
		fIsLoaded = false;
	}

	private synchronized void clearDataForProject(IProject project) {
		HashMap<String,HashMap<IResource, List<RefreshExclusion>>> configMap = fProjToConfToResToExcluMap.get(project);
		if (configMap != null)
			configMap.clear();
	}

	/**
	 * @since 5.4
	 */
	public synchronized void clearExclusions(IProject project, String configName, IResource resource) {
		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(project, configName);
		List<RefreshExclusion> exclusions = resourceMap.get(resource);
		if (exclusions != null) {
			exclusions.clear();
		}
	}
	
	public synchronized void clearResourcesToRefresh(IProject project) {
		// Clear all resources for the given project.
		HashMap<String,HashMap<IResource, List<RefreshExclusion>>> configMap = getConfigurationToResourcesMap(project);
		HashMap<IResource, List<RefreshExclusion>> resourceMap = null;

		Iterator<String> it = configMap.keySet().iterator();		
		while (it.hasNext()) {		
			String configName = it.next();
			resourceMap = configMap.get(configName);
			resourceMap.clear();
		}
	}

	/**
	 * @since 5.4
	 */
	public synchronized void deleteResourceToRefresh(IProject project, String configName, IResource resource) {
		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(project, configName);
		
		if (resourceMap.containsKey(resource))
			resourceMap.remove(resource);
	}
	
	public synchronized RefreshExclusion getExclusionForClassName(String className) {
		RefreshExclusionFactory factory = getFactoryForClassName(className);

		if (factory == null) {
			return null;
		}

		return factory.createNewExclusion();
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

	private HashMap<IResource, List<RefreshExclusion>> getResourcesToExclusionsMap(IProject project, String configName) {
		getProjectToConfigurationToResourcesMap();
		HashMap<String, HashMap<IResource, List<RefreshExclusion>>> configMap = getConfigurationToResourcesMap(project);
		HashMap<IResource, List<RefreshExclusion>> resourceMap = configMap.get(configName);
		
		if (resourceMap == null) {
			resourceMap = new HashMap<IResource, List<RefreshExclusion>>();
			resourceMap.put(project, new LinkedList<RefreshExclusion>());
			configMap.put(configName, resourceMap);
		}
		
		return resourceMap;
	}

	/**
	 * @since 5.4
	 */
	public synchronized  HashMap<String, HashMap<IResource, List<RefreshExclusion>>> getConfigurationToResourcesMap(IProject project)
	{		
		getProjectToConfigurationToResourcesMap();
		HashMap<String,HashMap<IResource, List<RefreshExclusion>>> configMap = fProjToConfToResToExcluMap.get(project);
		
		if (configMap == null) {
			configMap = new HashMap<String,HashMap<IResource, List<RefreshExclusion>>>();
			fProjToConfToResToExcluMap.put(project,configMap);			
		} 
			
		return configMap;
	
	}
	
	private HashMap<IProject,HashMap<String,HashMap<IResource, List<RefreshExclusion>>>> getProjectToConfigurationToResourcesMap() {
		if (fProjToConfToResToExcluMap == null) {
			fProjToConfToResToExcluMap = new HashMap<IProject,HashMap<String,HashMap<IResource, List<RefreshExclusion>>>>();
		}		
		
		return fProjToConfToResToExcluMap;
	}
		
	
	public IWorkspaceRunnable getRefreshRunnable(final IProject project) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			/**
			 * @param q
			 * @param resource
			 * @throws CoreException
			 */
			private void refreshResources(String configName, IResource resource, List<RefreshExclusion> exclusions,
					IProgressMonitor monitor) throws CoreException {
				if (resource instanceof IContainer) {
					IContainer container = (IContainer) resource;

					if (shouldResourceBeRefreshed(configName, resource)) {
						resource.refreshLocal(IResource.DEPTH_ONE, monitor);

					}

					for (IResource child : container.members()) {
						refreshResources(configName, child, exclusions, monitor);
					}
				}
			}

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {

				HashMap<String,HashMap<IResource, List<RefreshExclusion>>> configMap = getConfigurationToResourcesMap(project);

				Iterator<String> it = configMap.keySet().iterator();		
				while (it.hasNext()) {
					String configName = it.next();
					List<IResource> resourcesToRefresh = getResourcesToRefresh(project,configName);
					for (IResource resource : resourcesToRefresh) {
						List<RefreshExclusion> exclusions = getExclusions(project,configName,resource);
						refreshResources(configName, resource, exclusions, monitor);
					}
				}	
			}
		};

		return runnable;
	}

	/**
	 * @since 5.4
	 */
	public synchronized ISchedulingRule getRefreshSchedulingRule(IProject project, String configName) {
		return new MultiRule(getResourcesToRefresh(project, configName).toArray(new ISchedulingRule[0]));
	}

	/**
	 * @since 5.4
	 */
	public synchronized List<IResource> getResourcesToRefresh(IProject project, String configName) {
		getProjectToConfigurationToResourcesMap();	

		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(project,configName);
		return new ArrayList<IResource>(resourceMap.keySet());
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
			
				String str = storageElement.getAttribute(VERSION_NUMBER_ATTRIBUTE_NAME);
				int version = (str != null) ? Integer.valueOf(str) : 2;

				// iterate through the child nodes
				ICStorageElement[] children = storageElement.getChildren();

				if (version == 1) {
					ICConfigurationDescription cfgDescs[] = projectDescription.getConfigurations();	
					for (ICConfigurationDescription cfgDesc : cfgDescs) 
						loadResourceData(workspaceRoot, project, cfgDesc.getName(), children);
					
				} else {
					for (ICStorageElement child : children) {
						if (child.getName().equals(CONFIGURATION_ELEMENT) ) {
							String configName = child.getAttribute(CONFIGURATION_ELEMENT_NAME);
							loadResourceData(workspaceRoot, project, configName, child.getChildren());							
						} 						
					} 					
				} 
			} 
		} 
	}

	/**
	 * @since 5.4
	 */
	public synchronized void loadResourceData(IWorkspaceRoot workspaceRoot, IProject project, String configName, ICStorageElement[] children) {
	
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
							addResourceToRefresh(project,configName, resource);
	
						// load any exclusions
						List<RefreshExclusion> exclusions;
						try {
							exclusions = RefreshExclusion.loadData(
									child, null, resource, this);

							// add them
							for (RefreshExclusion exclusion : exclusions) {
								addExclusion(project, configName, resource, exclusion);
							}
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public synchronized void persistSettings(ICProjectDescription projectDescription)
			throws CoreException {
		
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

				HashMap<String,HashMap<IResource, List<RefreshExclusion>>> configMap = getConfigurationToResourcesMap(project);
				if (!configMap.isEmpty()) {
					
					Iterator<String> it = configMap.keySet().iterator();
					
					while (it.hasNext()) {		
						String configName = it.next();
						
						// for the current configuration, create a storage element
						ICStorageElement configElement = storageElement.createChild(CONFIGURATION_ELEMENT);
						configElement.setAttribute(CONFIGURATION_ELEMENT_NAME, configName);
						
						// set the resource to exclusion map for this config name.
						HashMap<IResource, List<RefreshExclusion>> resourceMap = configMap.get(configName);
						
						// for each resource 
						for (IResource resource : resourceMap.keySet()) {
							persistDataResource(configElement, resource,resourceMap);
						}
					}		
				}
			}
		}
	}

	/**
	 * @since 5.4
	 */
	public synchronized void persistDataResource(ICStorageElement storageElement, IResource resource, HashMap<IResource, List<RefreshExclusion>> resourceMap) {
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
		List<RefreshExclusion> exclusions = resourceMap.get(resource);
		if (exclusions != null) {
			for (RefreshExclusion exclusion : exclusions) {
				exclusion.persistData(resourceElement);
			}
		}
	}
	
	/**
	 * @since 5.4
	 */
	public synchronized void removeExclusion(IProject project, String configName, IResource resource, RefreshExclusion exclusion) {
		
		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(project,configName);
		List<RefreshExclusion> exclusions = resourceMap.get(resource);
		if (exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			resourceMap.put(resource, exclusions);
		}

		exclusions.remove(exclusion);
	}

	/**
	 * @since 5.4
	 */
	public synchronized void setExclusions(IProject project, String configName, IResource resource, List<RefreshExclusion> newExclusions) {
		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(project,configName);
		List<RefreshExclusion> exclusions = new LinkedList<RefreshExclusion>(newExclusions);

		resourceMap.put(resource, exclusions);
	}

	/**
	 * @since 5.4
	 */
	public synchronized List<RefreshExclusion> getExclusions(IProject project, String configName, IResource resource) {
	
		HashMap<IResource, List<RefreshExclusion>> resourceMap = getResourcesToExclusionsMap(project, configName); 
		
		List<RefreshExclusion> exclusions = resourceMap.get(resource);
		if (exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			resourceMap.put(resource, exclusions);
		}
	
		return exclusions;
	}

	/**
	 * @since 5.4
	 */
	public synchronized void setResourcesToExclusionsMap(IProject project, String configName, HashMap<IResource, List<RefreshExclusion>> source_resourceMap) { // List<IResource> resources) {

		HashMap<IResource, List<RefreshExclusion>> target_resourceMap = getResourcesToExclusionsMap(project,configName);
		target_resourceMap.clear();
		
		Iterator<IResource> resource_iterator = source_resourceMap.keySet().iterator();
		while (resource_iterator.hasNext()) {
			IResource source_resource = resource_iterator.next();
			List<RefreshExclusion> source_exclusions = source_resourceMap.get(source_resource);	
			List<RefreshExclusion> target_exclusions = new LinkedList<RefreshExclusion>();
			for (RefreshExclusion exclusion : source_exclusions) {
				target_exclusions.add(exclusion);
			}

			// ADD the exclusion list for this resource
			target_resourceMap.put(source_resource, target_exclusions);
		}
	}
	
	/**
	 * @since 5.4
	 */
	public synchronized boolean shouldResourceBeRefreshed(String configName, IResource resource) {
		IProject project = resource.getProject();
		List<IResource> resourcesToRefresh = getResourcesToRefresh(project,configName);
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

		for (RefreshExclusion exclusion : getExclusions(project, configName, topLevelResource)) {
			if (exclusion.testExclusionChain(resource)) {
				isExcluded = true;
				break;
			}
		}

		return !isExcluded;
	}
}
