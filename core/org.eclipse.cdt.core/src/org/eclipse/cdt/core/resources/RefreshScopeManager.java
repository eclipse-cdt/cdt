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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IContainer;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The RefreshScopeManager provides access to settings pertaining to refreshes performed during
 * a build.  Each project may have a set of resources associated with it that are the set of resources
 * to be refreshed.  An exclusion mechanism exists that allows for one to specify arbitrarily complicated,
 * nested logic that determines whether or not a given resource is refreshed according to previously
 * specified rules.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * @author crecoskie
 * @since 5.3
 *
 */
public class RefreshScopeManager {
	
	public static final String WORKSPACE_PATH_ATTRIBUTE_NAME = "workspacePath"; //$NON-NLS-1$
	public static final String RESOURCE_ELEMENT_NAME = "resource"; //$NON-NLS-1$
	public static final String VERSION_NUMBER_ATTRIBUTE_NAME = "versionNumber"; //$NON-NLS-1$
	public static final String VERSION_ELEMENT_NAME = "version"; //$NON-NLS-1$
	public static final QualifiedName REFRESH_SCOPE_PROPERTY_NAME = new QualifiedName(CCorePlugin.PLUGIN_ID, "refreshScope"); //$NON-NLS-1$
	public static final String EXTENSION_ID = "RefreshExclusionFactory"; //$NON-NLS-1$
	public static final Object EXCLUSION_FACTORY = "exclusionFactory"; //$NON-NLS-1$
	public static final String EXCLUSION_CLASS = "exclusionClass"; //$NON-NLS-1$
	public static final String FACTORY_CLASS = "factoryClass"; //$NON-NLS-1$
	public static final String INSTANCE_CLASS = "instanceClass"; //$NON-NLS-1$
	private int fVersion = 1;
	
	private HashMap<IProject, LinkedHashSet<IResource>> fProjectToResourcesMap;
	private HashMap<IResource, List<RefreshExclusion>> fResourceToExclusionsMap;
	private HashMap<String, RefreshExclusionFactory> fClassnameToExclusionFactoryMap;
	
	private static RefreshScopeManager fInstance;
	
	private RefreshScopeManager(){
		fClassnameToExclusionFactoryMap = new HashMap<String, RefreshExclusionFactory>();
		loadExtensions();
		try {
			loadSettings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		
		// add a resource change listener that will try to load settings for projects when they open
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

			public void resourceChanged(IResourceChangeEvent event) {
				
				if(event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
					IProject project = event.getResource().getProject();
					
					try {
						if(project.exists() && project.isOpen() && project.hasNature(CProjectNature.C_NATURE_ID)) {
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

									public boolean visit(IResourceDelta delta) throws CoreException {
										if (delta.getResource() instanceof IProject) {
											IProject project = (IProject) delta.getResource();

											if (delta.getKind() == IResourceDelta.ADDED
													|| (delta.getKind() == IResourceDelta.CHANGED && (delta
															.getFlags() & IResourceDelta.OPEN) != 0)) {
												loadSettings(ResourcesPlugin.getWorkspace()
														.getRoot(), project);
												return false;
											}

										}

										return true;
									}

								});
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

			}
			
		}, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
	}
	
	public synchronized void loadExtensions() {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				EXTENSION_ID);
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
								Object execExt = configElement.createExecutableExtension(FACTORY_CLASS);
								if ((execExt instanceof RefreshExclusionFactory)) {
									RefreshExclusionFactory exclusionFactory = (RefreshExclusionFactory) execExt;
									
									if(exclusionClassName != null) {
										fClassnameToExclusionFactoryMap.put(exclusionClassName, exclusionFactory);
									}
									
									if(instanceClassName != null) {
										fClassnameToExclusionFactoryMap.put(instanceClassName, exclusionFactory);
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
	
	public static synchronized RefreshScopeManager getInstance() {
		if(fInstance == null) {
			fInstance = new RefreshScopeManager();
		}
		
		return fInstance;
	}
	
	public int getVersion() {
		return fVersion;
	}
	
	public synchronized RefreshExclusionFactory getFactoryForClassName(String className) {
		RefreshExclusionFactory factory = fClassnameToExclusionFactoryMap.get(className);
		
		return factory;
	}
	
	public synchronized RefreshExclusion getExclusionForClassName(String className) {
		RefreshExclusionFactory factory = getFactoryForClassName(className);
		
		if(factory == null) {
			return null;
		}
		
		return factory.createNewExclusion();
	}
	
	
	/**
	 * Returns the set of resources that should be refreshed for a project.
	 * These resources might have associated exclusions.
	 * 
	 * @param project
	 * @return List<IResource>
	 */
	public synchronized List<IResource> getResourcesToRefresh(IProject project) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resources = fProjectToResourcesMap.get(project);
		
		if (resources == null) {
			resources = new LinkedHashSet<IResource>();
			resources.add(project);
			fProjectToResourcesMap.put(project, resources);
			
		}	
		
		return new LinkedList<IResource>(resources);
	}
	
	public synchronized void setResourcesToRefresh(IProject project, List<IResource> resources) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = new LinkedHashSet<IResource>(resources);
		
		fProjectToResourcesMap.put(project,  resourceSet);
	}
	
	public synchronized void addResourceToRefresh(IProject project, IResource resource) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = fProjectToResourcesMap.get(project);
		
		if(resourceSet == null) {
			resourceSet = new LinkedHashSet<IResource>();
			fProjectToResourcesMap.put(project, resourceSet);
		}
		
		resourceSet.add(resource);
		
	}
	
	public synchronized void deleteResourceToRefresh(IProject project, IResource resource) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = fProjectToResourcesMap.get(project);
		
		if(resourceSet == null) {
			resourceSet = new LinkedHashSet<IResource>();
			return;
		}
		
		resourceSet.remove(resource);
	}
	
	public synchronized void clearResourcesToRefresh(IProject project) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = fProjectToResourcesMap.get(project);
		
		if(resourceSet == null) {
			resourceSet = new LinkedHashSet<IResource>();
			fProjectToResourcesMap.put(project, resourceSet);
			return;
		}
		
		resourceSet.clear();
		
	}
	
	public synchronized void clearAllResourcesToRefresh() {
		fProjectToResourcesMap.clear();
	}
	
	public synchronized void clearAllData() {
		clearAllResourcesToRefresh();
		clearAllExclusions();
	}

	private HashMap<IProject, LinkedHashSet<IResource>> getProjectToResourcesMap() {
		if(fProjectToResourcesMap == null) {
			fProjectToResourcesMap = new HashMap<IProject, LinkedHashSet<IResource>>();
		}
		
		return fProjectToResourcesMap;
	}
	
	public synchronized List<RefreshExclusion> getExclusions(IResource resource) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if(exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}
		
		return exclusions;
	}
	
	public synchronized void addExclusion(IResource resource, RefreshExclusion exclusion) {
		getResourcesToExclusionsMap();
		
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if(exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}
		
		exclusions.add(exclusion);
	}
	
	private HashMap<IResource, List<RefreshExclusion>> getResourcesToExclusionsMap() {
		if(fResourceToExclusionsMap == null) {
			fResourceToExclusionsMap = new HashMap<IResource, List<RefreshExclusion>>();
		}
		
		return fResourceToExclusionsMap;
	}

	public synchronized void removeExclusion(IResource resource, RefreshExclusion exclusion) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if(exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}
		
		exclusions.remove(exclusion);
	}
	
	public synchronized void persistSettings() throws CoreException {
		getProjectToResourcesMap();
		getResourcesToExclusionsMap();
		for(IProject project : fProjectToResourcesMap.keySet()) {
			if (!project.exists()) {
				continue;
			}
			// serialize all settings for the project to an XML document which we will use to persist
			// the data to a persistent resource property
			 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder docBuilder = null;
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new CoreException(CCorePlugin.createStatus(Messages.RefreshScopeManager_0, e));
			}
	        
			// create document root
			Document doc = docBuilder.newDocument();
			Element root = doc.createElement("root"); //$NON-NLS-1$
	        doc.appendChild(root);
	        
	        Element versionElement = doc.createElement(VERSION_ELEMENT_NAME);
	        versionElement.setAttribute(VERSION_NUMBER_ATTRIBUTE_NAME, Integer.toString(fVersion));
	        root.appendChild(versionElement);
			
			for(IResource resource : fProjectToResourcesMap.get(project)) {
				
				// create a resource node
				Element resourceElement = doc.createElement(RESOURCE_ELEMENT_NAME);
	            resourceElement.setAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME, resource.getFullPath().toString());
	            root.appendChild(resourceElement);
	            
	            // populate the node with any exclusions
	            List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
	            if (exclusions != null) {
					for(RefreshExclusion exclusion : exclusions) {
		            	exclusion.persistData(doc, resourceElement);
		            }
	            }

			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer;
			try {
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException e) {
				throw new CoreException(CCorePlugin.createStatus(Messages.RefreshScopeManager_1, e));
			}
            //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
			
			//create a string from xml tree
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            DOMSource source = new DOMSource(doc);
            try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				throw new CoreException(CCorePlugin.createStatus(Messages.RefreshScopeManager_2, e));
			}
            String xmlString = stringWriter.toString();
            
            // use the string as a value for a persistent resource property on the project
            project.setPersistentProperty(REFRESH_SCOPE_PROPERTY_NAME, xmlString);

		}
	}
	
	public synchronized void loadSettings() throws CoreException {
		// iterate through all projects in the workspace. If they are C projects, attempt to load settings
		// from them.
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		for (IProject project : workspaceRoot.getProjects()) {
			loadSettings(workspaceRoot, project);
		}
	}

	/**
	 * @param workspaceRoot
	 * @param project
	 * @throws CoreException
	 */
	private synchronized void loadSettings(IWorkspaceRoot workspaceRoot, IProject project) throws CoreException {
		if (project.isOpen()) {
			if (project.hasNature(CProjectNature.C_NATURE_ID)) {
				String xmlString = project.getPersistentProperty(REFRESH_SCOPE_PROPERTY_NAME);

				// if there are no settings, then configure the default behaviour of refreshing the entire
				// project,
				// with no exclusions
				if (xmlString == null) {
					addResourceToRefresh(project, project);
				}

				else {
					// convert the XML string to a DOM model

					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder docBuilder = null;
					try {
						docBuilder = docBuilderFactory.newDocumentBuilder();
					} catch (ParserConfigurationException e) {
						throw new CoreException(CCorePlugin.createStatus(
								Messages.RefreshScopeManager_0, e));
					}

					Document doc = null;

					try {
						doc = docBuilder.parse(new InputSource(new StringReader(xmlString)));
					} catch (SAXException e) {
						throw new CoreException(CCorePlugin.createStatus(
								MessageFormat.format(Messages.RefreshScopeManager_3,
										project.getName()), e));
					} catch (IOException e) {
						throw new CoreException(CCorePlugin.createStatus(
								MessageFormat.format(Messages.RefreshScopeManager_3,
										project.getName()), e));
					}

					// walk the DOM and load the settings

					// for now ignore the version attribute, as we only have version 1 at this time

					// iterate through the child nodes
					NodeList nodeList = doc.getDocumentElement().getChildNodes();  // child of the doc is the root

					for (int k = 0; k < nodeList.getLength(); k++) {
						Node node = nodeList.item(k);

						// node will be an element
						if (node instanceof Element) {
							Element resourceElement = (Element) node;

							if (resourceElement.getNodeName().equals(RESOURCE_ELEMENT_NAME)) {

								// get the resource path
								String resourcePath = resourceElement
										.getAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME);

								if (resourcePath == null) {
									// error

								}

								else {
									// find the resource
									IResource resource = workspaceRoot.findMember(resourcePath);

									if (resource == null) {
										// error
									}

									else {
										addResourceToRefresh(project, resource);

										// load any exclusions
										List<RefreshExclusion> exclusions = RefreshExclusion.loadData(resourceElement, null, resource);

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
		}
	}

	public synchronized void clearExclusions(IResource resource) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if(exclusions != null) {
			exclusions.clear();	
		}
	}
	
	public synchronized void setExclusions(IResource resource, List<RefreshExclusion> newExclusions) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = new LinkedList<RefreshExclusion>(newExclusions);
		
		fResourceToExclusionsMap.put(resource, exclusions);
	}
	
	public synchronized void clearAllExclusions() {
		if(fResourceToExclusionsMap != null)
			fResourceToExclusionsMap.clear();
	}
	
	public synchronized void clearExclusionsForProject(IProject project) {
		getResourcesToExclusionsMap();
		List<IResource> resourcesToRemove = new LinkedList<IResource>();
		
		for(IResource resource : fResourceToExclusionsMap.keySet()) {
			IProject project2 = resource.getProject();
			if(project2.equals(project)) {
				resourcesToRemove.add(resource);
			}
		}
		
		for(IResource resource : resourcesToRemove) {
			fResourceToExclusionsMap.remove(resource);
		}
	}
	
	private synchronized void clearDataForProject(IProject project) {
		clearResourcesToRefresh(project);
		clearExclusionsForProject(project);
	}

	public synchronized ExclusionInstance getInstanceForClassName(String className) {
		RefreshExclusionFactory factory = getFactoryForClassName(className);
		
		if(factory == null) {
			return null;
		}
		
		return factory.createNewExclusionInstance();
	}
	
	public IWorkspaceRunnable getRefreshRunnable(final IProject project) {
		
		
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {	
				
				List<IResource> resourcesToRefresh  = getResourcesToRefresh(project);
				for(IResource resource : resourcesToRefresh) {
					List<RefreshExclusion> exclusions = getExclusions(resource);
					refreshResources(resource, exclusions, monitor);
				}
				
			}

			/**
			 * @param q
			 * @param resource
			 * @throws CoreException 
			 */
			private void refreshResources(IResource resource, List<RefreshExclusion> exclusions, IProgressMonitor monitor) throws CoreException {
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
		};
		
		return runnable;
	}
	
	public synchronized boolean shouldResourceBeRefreshed(IResource resource) {
		IProject project = resource.getProject();
		List<IResource> resourcesToRefresh = getResourcesToRefresh(project);
		boolean isInSomeTree = false;
		IResource topLevelResource = null;
		
		for(IResource resourceToRefresh : resourcesToRefresh) {
			if(resourceToRefresh.equals(resource)) {
				isInSomeTree = true;
				topLevelResource = resource;
				break;
			}
			
			// see if the resource is a child of our top level resources
			if(resourceToRefresh instanceof IContainer) {
				IContainer container = (IContainer) resourceToRefresh;
				if(container.getFullPath().isPrefixOf(resource.getFullPath())) {
					isInSomeTree = true;
					topLevelResource = resourceToRefresh;
					break;
				}
			}
			
		}
		
		if(!isInSomeTree) {
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
