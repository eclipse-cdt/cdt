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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Serializes and deserializes language mappings to and from persistent storage.
 */
public class LanguageMappingStore {
	private static final String LANGUAGE_MAPPING_ID = "org.eclipse.cdt.core.language.mapping"; //$NON-NLS-1$

	private static final String PROJECT_MAPPINGS = "project-mappings"; //$NON-NLS-1$

	private static final String WORKSPACE_MAPPINGS = "workspace-mappings"; //$NON-NLS-1$
	
	private static final String CONTENT_TYPE_MAPPING = "content-type-mapping"; //$NON-NLS-1$
	
	private static final String FILE_MAPPING = "file-mapping"; //$NON-NLS-1$

	private static final String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	
	private static final String ATTRIBUTE_CONTENT_TYPE = "content-type"; //$NON-NLS-1$

	private static final String ATTRIBUTE_LANGUAGE = "language"; //$NON-NLS-1$

	private static final String ATTRIBUTE_CONFIGURATION = "configuration"; //$NON-NLS-1$

	public LanguageMappingStore() {
	}
	
	public ProjectLanguageConfiguration decodeMappings(IProject project) throws CoreException {
		ProjectLanguageConfiguration config = new ProjectLanguageConfiguration();
		ICDescriptor descriptor = getProjectDescription(project);
		Element rootElement = descriptor.getProjectData(LANGUAGE_MAPPING_ID);
		if (rootElement == null) {
			return config;
		}
		
		NodeList mappingElements = rootElement.getElementsByTagName(PROJECT_MAPPINGS);
		if (mappingElements.getLength() > 0) {
			Element element = (Element) mappingElements.item(0);
			config.setContentTypeMappings(decodeProjectContentTypeMappings(element));
			config.setFileMappings(decodeFileMappings(element));
		}
		return config;
	}
	
	private Map decodeProjectContentTypeMappings(Element rootElement) {
		Map decodedMappings = new TreeMap();
		NodeList mappingElements = rootElement.getElementsByTagName(CONTENT_TYPE_MAPPING);
		for (int j = 0; j < mappingElements.getLength(); j++) {
			Element mapping = (Element) mappingElements.item(j);
			String configuration = mapping.getAttribute(ATTRIBUTE_CONFIGURATION);
			
			Map contentTypeMappings = (Map) decodedMappings.get(configuration);
			if (contentTypeMappings == null) {
				contentTypeMappings = new TreeMap();
				decodedMappings.put(configuration, contentTypeMappings);
			}
			String contentType = mapping.getAttribute(ATTRIBUTE_CONTENT_TYPE);
			String language = mapping.getAttribute(ATTRIBUTE_LANGUAGE);
			contentTypeMappings.put(contentType, language);
		}
		return decodedMappings;
	}

	protected ICDescriptor getProjectDescription(IProject project) throws CoreException {
		return CCorePlugin.getDefault().getCProjectDescription(project, true);
	}
	
	private Map decodeContentTypeMappings(Element rootElement) throws CoreException {
		return decodeMappings(rootElement, CONTENT_TYPE_MAPPING, ATTRIBUTE_CONTENT_TYPE, ATTRIBUTE_LANGUAGE);
	}
	
	private Map decodeFileMappings(Element rootElement) throws CoreException {
		Map decodedMappings = new TreeMap();
		NodeList mappingElements = rootElement.getElementsByTagName(FILE_MAPPING);
		for (int j = 0; j < mappingElements.getLength(); j++) {
			Element mapping = (Element) mappingElements.item(j);
			String path = mapping.getAttribute(ATTRIBUTE_PATH);
			
			Map configurationMappings = (Map) decodedMappings.get(path);
			if (configurationMappings == null) {
				configurationMappings = new TreeMap();
				decodedMappings.put(path, configurationMappings);
			}
			String configuration = mapping.getAttribute(ATTRIBUTE_CONFIGURATION);
			String language = mapping.getAttribute(ATTRIBUTE_LANGUAGE);
			configurationMappings.put(configuration, language);
		}
		return decodedMappings;
	}
	
	private Map decodeMappings(Element rootElement, String category, String keyName, String valueName) {
		Map decodedMappings = new TreeMap();
		NodeList mappingElements = rootElement.getElementsByTagName(category);
		for (int j = 0; j < mappingElements.getLength(); j++) {
			Element mapping = (Element) mappingElements.item(j);
			String key = mapping.getAttribute(keyName);
			String value = mapping.getAttribute(valueName);
			decodedMappings.put(key, value);
		}
		return decodedMappings;
	}

	public void storeMappings(IProject project, ProjectLanguageConfiguration config) throws CoreException {
		ICDescriptor descriptor = getProjectDescription(project);
		Element rootElement = descriptor.getProjectData(LANGUAGE_MAPPING_ID);
		clearChildren(rootElement);

		Document document = rootElement.getOwnerDocument();
		Element projectMappings = document.createElement(PROJECT_MAPPINGS);
		rootElement.appendChild(projectMappings);
		
		addProjectContentTypeMappings(config.getContentTypeMappings(), projectMappings);
		addFileMappings(config.getFileMappings(), projectMappings);
		descriptor.saveProjectData();
	}

	private void addProjectContentTypeMappings(Map contentTypeMappings, Element rootElement) {
		Document document = rootElement.getOwnerDocument();
		Iterator entries = contentTypeMappings.entrySet().iterator();
		while (entries.hasNext()) {
			Entry entry = (Entry) entries.next();
			Element mapping = document.createElement(CONTENT_TYPE_MAPPING);
			
			String configuration = (String) entry.getKey();
			Iterator contentTypeEntries = ((Map) entry.getValue()).entrySet().iterator();
			while (contentTypeEntries.hasNext()) {
				Entry configurationEntry = (Entry) contentTypeEntries.next();
				String contentType = (String) configurationEntry.getKey();
				String language = (String) configurationEntry.getValue();
				
				mapping.setAttribute(ATTRIBUTE_CONTENT_TYPE, contentType);
				mapping.setAttribute(ATTRIBUTE_CONFIGURATION, configuration);
				mapping.setAttribute(ATTRIBUTE_LANGUAGE, language);
				rootElement.appendChild(mapping);
			}
		}
	}

	public void storeMappings(WorkspaceLanguageConfiguration config) throws CoreException {
		try {
			// Encode mappings as XML and serialize as a String.
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element rootElement = doc.createElement(WORKSPACE_MAPPINGS);
			doc.appendChild(rootElement);
			addContentTypeMappings(config.getWorkspaceMappings(), rootElement);
			Transformer serializer = createSerializer();
			DOMSource source = new DOMSource(doc);
			StringWriter buffer = new StringWriter();
			StreamResult result = new StreamResult(buffer);
			serializer.transform(source, result);
			String encodedMappings = buffer.getBuffer().toString();
			
			Preferences node = CCorePlugin.getDefault().getPluginPreferences();;
			node.setValue(CCorePreferenceConstants.WORKSPACE_LANGUAGE_MAPPINGS, encodedMappings);
			CCorePlugin.getDefault().savePluginPreferences();
		} catch (ParserConfigurationException e) {
			throw new CoreException(Util.createStatus(e));
		} catch (TransformerException e) {
			throw new CoreException(Util.createStatus(e));
		} 
	}
	
	public WorkspaceLanguageConfiguration decodeWorkspaceMappings() throws CoreException {
		Preferences node = CCorePlugin.getDefault().getPluginPreferences();;
		String encodedMappings = node.getString(CCorePreferenceConstants.WORKSPACE_LANGUAGE_MAPPINGS);
		WorkspaceLanguageConfiguration config = new WorkspaceLanguageConfiguration();
		
		if (encodedMappings == null || encodedMappings.length() == 0) {
			return config;
		}
		
		// The mappings are encoded as XML in a String so we need to parse it.
		InputSource input = new InputSource(new StringReader(encodedMappings));
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			config.setWorkspaceMappings(decodeContentTypeMappings(document.getDocumentElement()));
			return config;
		} catch (SAXException e) {
			throw new CoreException(Util.createStatus(e));
		} catch (IOException e) {
			throw new CoreException(Util.createStatus(e));
		} catch (ParserConfigurationException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	private Transformer createSerializer() throws CoreException {
		try {
			return TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new CoreException(Util.createStatus(e));
		} catch (TransformerFactoryConfigurationError e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	private void clearChildren(Element element) {
		Node child = element.getFirstChild();
		while (child != null) {
			element.removeChild(child);
			child = element.getFirstChild();
		}
	}

	private void addMappings(Map mappings, Element rootElement, String category, String keyName, String valueName) {
		Document document = rootElement.getOwnerDocument();
		Iterator entries = mappings.entrySet().iterator();
		while (entries.hasNext()) {
			Entry entry = (Entry) entries.next();
			Element mapping = document.createElement(category);
			mapping.setAttribute(keyName, (String) entry.getKey());
			mapping.setAttribute(valueName, (String) entry.getValue());
			rootElement.appendChild(mapping);
		}
	}
	
	private void addContentTypeMappings(Map mappings, Element rootElement) {
		addMappings(mappings, rootElement, CONTENT_TYPE_MAPPING, ATTRIBUTE_CONTENT_TYPE, ATTRIBUTE_LANGUAGE);
	}
	
	private void addFileMappings(Map mappings, Element rootElement) {
		Document document = rootElement.getOwnerDocument();
		Iterator entries = mappings.entrySet().iterator();
		while (entries.hasNext()) {
			Entry entry = (Entry) entries.next();
			Element mapping = document.createElement(FILE_MAPPING);
			
			String path = (String) entry.getKey();
			Iterator configurationEntries = ((Map) entry.getValue()).entrySet().iterator();
			while (configurationEntries.hasNext()) {
				Entry configurationEntry = (Entry) configurationEntries.next();
				String configuration = (String) configurationEntry.getKey();
				String language = (String) configurationEntry.getValue();
				
				mapping.setAttribute(ATTRIBUTE_PATH, path);
				mapping.setAttribute(ATTRIBUTE_CONFIGURATION, configuration);
				mapping.setAttribute(ATTRIBUTE_LANGUAGE, language);
				rootElement.appendChild(mapping);
			}
		}
	}
}
