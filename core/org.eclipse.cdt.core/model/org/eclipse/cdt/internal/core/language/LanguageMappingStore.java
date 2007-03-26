/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.language.LanguageMappingConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LanguageMappingStore {
	private static final String LANGUAGE_MAPPING_ID = "org.eclipse.cdt.core.language.mapping"; //$NON-NLS-1$

	private static final String PROJECT_MAPPINGS = "project-mappings"; //$NON-NLS-1$

	private static final String PROJECT_MAPPING = "project-mapping"; //$NON-NLS-1$

	private static final String ATTRIBUTE_CONTENT_TYPE = "content-type"; //$NON-NLS-1$

	private static final String ATTRIBUTE_LANGUAGE = "language"; //$NON-NLS-1$

	private IProject fProject;

	public LanguageMappingStore(IProject project) {
		fProject = project;
	}
	
	public LanguageMappingConfiguration decodeMappings() throws CoreException {
		LanguageMappingConfiguration config = new LanguageMappingConfiguration();
		ICDescriptor descriptor = getProjectDescription();
		Element rootElement = descriptor.getProjectData(LANGUAGE_MAPPING_ID);
		if (rootElement == null) {
			return config;
		}
		config.setProjectMappings(decodeProjectMappings(rootElement));
		return config;
	}
	
	protected ICDescriptor getProjectDescription() throws CoreException {
		return CCorePlugin.getDefault().getCProjectDescription(fProject, true);
	}
	
	private Map decodeProjectMappings(Element rootElement) throws CoreException {
		Map decodedMappings = new TreeMap();
		NodeList elements = rootElement.getElementsByTagName(PROJECT_MAPPINGS);
		for (int i = 0; i < elements.getLength(); i++) {
			Element projectMappings = (Element) elements.item(i);
			NodeList mappingElements = projectMappings.getElementsByTagName(PROJECT_MAPPING);
			for (int j = 0; j < mappingElements.getLength(); j++) {
				Element mapping = (Element) mappingElements.item(j);
				String contentType = mapping.getAttribute(ATTRIBUTE_CONTENT_TYPE);
				String language = mapping.getAttribute(ATTRIBUTE_LANGUAGE);
				decodedMappings.put(contentType, language);
			}
		}
		return decodedMappings;
	}

	public void storeMappings(LanguageMappingConfiguration config) throws CoreException {
		ICDescriptor descriptor = getProjectDescription();
		Element rootElement = descriptor.getProjectData(LANGUAGE_MAPPING_ID);
		clearChildren(rootElement);
		addProjectMappings(config.getProjectMappings(), rootElement);
		descriptor.saveProjectData();
	}

	private void clearChildren(Element element) {
		Node child = element.getFirstChild();
		while (child != null) {
			element.removeChild(child);
			child = element.getFirstChild();
		}
	}

	private void addProjectMappings(Map mappings, Element rootElement) {
		Document document = rootElement.getOwnerDocument();
		Element projectMappings = document.createElement(PROJECT_MAPPINGS);
		Iterator entries = mappings.entrySet().iterator();
		while (entries.hasNext()) {
			Entry entry = (Entry) entries.next();
			Element mapping = document.createElement(PROJECT_MAPPING);
			mapping.setAttribute(ATTRIBUTE_CONTENT_TYPE, (String) entry.getKey());
			mapping.setAttribute(ATTRIBUTE_LANGUAGE, (String) entry.getValue());
			projectMappings.appendChild(mapping);
		}
		rootElement.appendChild(projectMappings);
	}
}
