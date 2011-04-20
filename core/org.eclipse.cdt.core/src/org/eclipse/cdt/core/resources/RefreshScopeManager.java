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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
	private int fVersion = 1;
	
	private RefreshScopeManager(){
		
	}
	
	private HashMap<IProject, LinkedHashSet<IResource>> fProjectToResourcesMap;
	private HashMap<IResource, List<RefreshExclusion>> fResourceToExclusionsMap;
	
	private static RefreshScopeManager fInstance;
	
	public static synchronized RefreshScopeManager getInstance() {
		if(fInstance == null) {
			fInstance = new RefreshScopeManager();
		}
		
		return fInstance;
	}
	
	public int getVersion() {
		return fVersion;
	}
	
	
	/**
	 * Returns the set of resources that should be refreshed for a project.
	 * These resources might have associated exclusions.
	 * 
	 * @param project
	 * @return Set<IResource>
	 */
	public List<IResource> getResourcesToRefresh(IProject project) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resources = fProjectToResourcesMap.get(project);
		List<IResource> retval;
		if (resources == null)
			retval= new LinkedList<IResource>();
		else
			retval= new LinkedList<IResource>(resources);
		
		return retval;
	}
	
	public void setResourcesToRefresh(IProject project, List<IResource> resources) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = new LinkedHashSet<IResource>(resources);
		
		fProjectToResourcesMap.put(project,  resourceSet);
	}
	
	public void addResourceToRefresh(IProject project, IResource resource) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = fProjectToResourcesMap.get(project);
		
		if(resourceSet == null) {
			resourceSet = new LinkedHashSet<IResource>();
			fProjectToResourcesMap.put(project, resourceSet);
		}
		
		resourceSet.add(resource);
		
	}
	
	public void deleteResourceToRefresh(IProject project, IResource resource) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = fProjectToResourcesMap.get(project);
		
		if(resourceSet == null) {
			resourceSet = new LinkedHashSet<IResource>();
			return;
		}
		
		resourceSet.remove(resource);
	}
	
	public void clearResourcesToRefresh(IProject project) {
		getProjectToResourcesMap();
		LinkedHashSet<IResource> resourceSet = fProjectToResourcesMap.get(project);
		
		if(resourceSet == null) {
			resourceSet = new LinkedHashSet<IResource>();
			return;
		}
		
		resourceSet.clear();
		
	}
	
	public void clearAllResourcesToRefresh() {
		fProjectToResourcesMap.clear();
	}
	
	public void clearAllData() {
		clearAllResourcesToRefresh();
		clearAllExclusions();
	}

	private HashMap<IProject, LinkedHashSet<IResource>> getProjectToResourcesMap() {
		if(fProjectToResourcesMap == null) {
			fProjectToResourcesMap = new HashMap<IProject, LinkedHashSet<IResource>>();
		}
		
		return fProjectToResourcesMap;
	}
	
	public List<RefreshExclusion> getExclusions(IResource resource) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if(exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}
		
		return exclusions;
	}
	
	public void addExclusion(IResource resource, RefreshExclusion exclusion) {
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

	public void removeExclusion(IResource resource, RefreshExclusion exclusion) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if(exclusions == null) {
			exclusions = new LinkedList<RefreshExclusion>();
			fResourceToExclusionsMap.put(resource, exclusions);
		}
		
		exclusions.remove(exclusion);
	}
	
	public void persistSettings() throws CoreException {
		for(IProject project : fProjectToResourcesMap.keySet()) {
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
	            for(RefreshExclusion exclusion : fResourceToExclusionsMap.get(resource)) {
	            	exclusion.persistData(doc, resourceElement);
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
	
	public void loadSettings() throws CoreException {
		// iterate through all projects in the workspace.  If they are C projects, attempt to load settings from them.
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		for(IProject project : workspaceRoot.getProjects()) {
			if(project.isOpen()) {
				if(project.hasNature(CProjectNature.C_NATURE_ID)) {
					String xmlString = project.getPersistentProperty(REFRESH_SCOPE_PROPERTY_NAME);
					
					// if there are no settings, then configure the default behaviour of refreshing the entire project,
					// with no exclusions
					if (xmlString == null) {
						addResourceToRefresh(project, project);
					}
					
					else {
						// convert the XML string to a DOM model
						
						DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				        DocumentBuilder docBuilder = null;
						try {
							docBuilder = docBuilderFactory.newDocumentBuilder();
						} catch (ParserConfigurationException e) {
							throw new CoreException(CCorePlugin.createStatus(Messages.RefreshScopeManager_0, e));
						}
						
						Document doc = null;
						
						try {
							doc = docBuilder.parse(new InputSource(new StringReader(xmlString)));
						} catch (SAXException e) {
							throw new CoreException(CCorePlugin.createStatus(MessageFormat.format(Messages.RefreshScopeManager_3, project.getName()), e));
						} catch (IOException e) {
							throw new CoreException(CCorePlugin.createStatus(MessageFormat.format(Messages.RefreshScopeManager_3, project.getName()), e));
						}
						
						// walk the DOM and load the settings
						
						// for now ignore the version attribute, as we only have version 1 at this time
						
						// iterate through the resource element nodes
						NodeList nodeList = doc.getElementsByTagName(RESOURCE_ELEMENT_NAME);
						
						for(int k = 0; k < nodeList.getLength(); k++) {
							Node node = nodeList.item(k);
							
							// node will be an element
							if(node instanceof Element) {
								Element resourceElement = (Element) node;
								
								// get the resource path
								String resourcePath = resourceElement.getAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME);
								
								if(resourcePath == null) {
									// error
									
								}
								
								else {
									// find the resource
									IResource resource = workspaceRoot.findMember(resourcePath);
									
									if(resource == null) {
										// error
									}
									
									else {
										addResourceToRefresh(project, resource);
										
										// load any exclusions
										List<RefreshExclusion> exclusions = RefreshExclusion.loadData(resourceElement, null);
										
										// add them
										for(RefreshExclusion exclusion : exclusions) {
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

	public void clearExclusions(IResource resource) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		if(exclusions != null) {
			exclusions.clear();	
		}
	}
	
	public void setExclusions(IResource resource, List<RefreshExclusion> newExclusions) {
		getResourcesToExclusionsMap();
		List<RefreshExclusion> exclusions = new LinkedList<RefreshExclusion>(newExclusions);
		
		fResourceToExclusionsMap.put(resource, exclusions);
	}
	
	public void clearAllExclusions() {
		fResourceToExclusionsMap.clear();
	}

}
