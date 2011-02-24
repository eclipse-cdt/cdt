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

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	
	private TreeMap<IProject, Set<IResource>> fProjectToResourcesMap;
	private TreeMap<IResource, List<RefreshExclusion>> fResourceToExclusionsMap;
	
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
	public Set<IResource> getResourcesToRefresh(IProject project) {
		Set<IResource> retval = fProjectToResourcesMap.get(project);
		
		if(retval == null) {
			return Collections.emptySet();
		}
		
		return retval;
	}
	
	public List<RefreshExclusion> getExclusions(IResource resource) {
		return fResourceToExclusionsMap.get(resource);
	}
	
	public void addExclusion(IResource resource, RefreshExclusion exclusion) {
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
		exclusions.add(exclusion);
	}
	
	public void removeExclusion(IResource resource, RefreshExclusion exclusion) {
		List<RefreshExclusion> exclusions = fResourceToExclusionsMap.get(resource);
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
	
	public void loadSettings() {
		
	}

}
