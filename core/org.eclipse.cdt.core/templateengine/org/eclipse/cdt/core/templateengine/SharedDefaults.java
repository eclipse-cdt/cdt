/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Processes the shared default values. Updates and Persists new key - value (default) pair
 */
public class SharedDefaults extends HashMap<String, String> {
	private static final long serialVersionUID = 0000000000L;

	public Document document;
	private File parsedXML;
	private File backUpSharedXML;

	/**
	 * HashMap's for persistence
	 */
	private HashMap<String, String> sharedDefaultsMap;
	private HashMap<String, String> persistDataMap;
	private HashMap<String, String> tableDataMap;

	/**
	 * Two XML files here supports to provide consistent writing of data into
	 * them even during some destructive events which can happen during data
	 * persistence
	 */
	private static final String SHARED_DEFAULTS_DOT_XML = "shareddefaults.xml"; //$NON-NLS-1$
	private static final String SHARED_DEFAULTS_DOT_BACKUP_DOT_XML = "shareddefaults.backup.xml"; //$NON-NLS-1$

	/**
	 * Static reference string for getting (GET) and storing (SET)
	 * shareddefaults.xml
	 */

	public static final String SET = "SET"; //$NON-NLS-1$
	public static final String GET = "GET"; //$NON-NLS-1$

	/**
	 * Specifies the folder name present in the plugin
	 */
	public static final String ResourceFolder = "resources"; //$NON-NLS-1$

	/**
	 * Static reference of Singleton SharedDefault Instance
	 */
	private static SharedDefaults SHAREDDEFAULTS = new SharedDefaults();

	/**
	 * @return the shared SharedDefaults Instance
	 */
	public static SharedDefaults getInstance() {
		return SHAREDDEFAULTS;
	}

	/**
	 * Default Constructor for creating and instantiating objects. On the
	 * startup of Template Engine, if it checks for the existence of
	 * TempSharedDefaultsXML file, then it is determined that the last Template
	 * Engine process under went some System destructive events and takes up
	 * reconstructive process to regain the consistent data by persisting all
	 * information first into temporary file and then into actual file.
	 */

	public SharedDefaults() {
		sharedDefaultsMap = new HashMap<>();
		persistDataMap = new HashMap<>();
		tableDataMap = new HashMap<>();

		// The conditional controls here is provided to have consistent
		// data storage in the file during System crash or
		// Power shutdown during data persistence into the file.

		parsedXML = TemplateEngineHelper.getSharedDefaultLocation(SHARED_DEFAULTS_DOT_XML);
		backUpSharedXML = TemplateEngineHelper.getSharedDefaultLocation(SHARED_DEFAULTS_DOT_BACKUP_DOT_XML);

		if (backUpSharedXML.exists())
			swapXML();

		initSharedDefaults();
	}

	/**
	 * This method instantiates the SharedDefaults process by gathering XML
	 * document and creating shared key-value pair in HashMap. Also creates a
	 * new XML file if none exists and adds the default XML format.
	 */

	private void initSharedDefaults() {
		String key = null;
		String value = null;

		try {
			long length = parsedXML.length();
			// Adds defaultXML format if the file length is zero
			if (length == 0) {
				parsedXML = createDefaultXMLFormat(parsedXML);
			}
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(parsedXML.toURI().toURL().openStream());
		} catch (Exception exp) {
			TemplateEngineUtil.log(exp);
		}

		List<Element> sharedElementList = TemplateEngine.getChildrenOfElement(document.getDocumentElement());
		int listSize = sharedElementList.size();
		for (int i = 0; i < listSize; i++) {
			Element xmlElement = sharedElementList.get(i);
			key = xmlElement.getAttribute(TemplateEngineHelper.ID);
			value = xmlElement.getAttribute(TemplateEngineHelper.VALUE);
			if (key != null && !key.trim().isEmpty()) {
				sharedDefaultsMap.put(key, value);
			}
		}
	}

	/**
	 * This method updates the HashMap with new key-value pair into the XML file
	 *
	 * @param sharedMap
	 */

	public void updateShareDefaultsMap(Map<String, String> sharedMap) {
		sharedDefaultsMap.putAll(sharedMap);
		persistSharedValueMap();
	}

	/**
	 * This method persists the latest data (HashMap) in the XML file New data
	 * obtained from the PreferencePage GUI.
	 */

	public void persistSharedValueMap() {
		generateSharedXML(backUpSharedXML);
		generateSharedXML(parsedXML);
		swapXML();
	}

	/**
	 * This method returns the latest key value pair (HashMap)
	 *
	 * @return HashMap
	 */

	public Map<String, String> getSharedDefaultsMap() {
		return sharedDefaultsMap;
	}

	/**
	 * Adds data to the backend XML (persistence) Data obtained from the
	 * PreferencePage GUI.
	 */

	public void addToBackEndStorage(String name, String value) {
		if (sharedDefaultsMap != null) {
			tableDataMap.putAll(sharedDefaultsMap);
		}

		tableDataMap.put(name, value);
		updateShareDefaultsMap(tableDataMap);
	}

	/**
	 * Updates backend with changed value for a specific key(name)
	 *
	 * @param updateName
	 * @param updateValue
	 */
	public void updateToBackEndStorage(String updateName, String updateValue) {
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(parsedXML.toURI().toURL().openStream());
		} catch (Exception exp) {
			TemplateEngineUtil.log(exp);
		}

		persistDataMap.putAll(sharedDefaultsMap);
		List<Element> sharedElementList = TemplateEngine.getChildrenOfElement(document.getDocumentElement());
		int elementListSize = sharedElementList.size();

		for (int i = 0; i < elementListSize; i++) {
			Element xmlElement = sharedElementList.get(i);
			String name = xmlElement.getAttribute(TemplateEngineHelper.ID);

			if (updateName.equals(name)) {
				persistDataMap.put(updateName, updateValue);
			}
		}

		updateShareDefaultsMap(persistDataMap);
	}

	/**
	 * Deletes the key-value pair from the backend with Key as identifier.
	 *
	 * @param deleteName
	 */
	public void deleteBackEndStorage(String[] deleteName) {
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(parsedXML.toURI().toURL().openStream());
		} catch (Exception exp) {
			TemplateEngineUtil.log(exp);
		}

		List<Element> sharedElementList = TemplateEngine.getChildrenOfElement(document.getDocumentElement());
		int elementListSize = sharedElementList.size();
		for (int i = 0; i < elementListSize; i++) {

			Element xmlElement = sharedElementList.get(i);
			String name = xmlElement.getAttribute(TemplateEngineHelper.ID);

			for (int k = 0; k < deleteName.length; k++) {
				if (deleteName[k].equals(name)) {
					xmlElement.removeAttribute(name);
					sharedDefaultsMap.remove(name);
				}
			}
		}

		updateShareDefaultsMap(sharedDefaultsMap);
	}

	/**
	 * This method returns the default XMLFormat for the newly created XML file
	 *
	 * @param parsedXML
	 * @return
	 */

	private File createDefaultXMLFormat(File xmlFile) {
		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			TemplateEngineUtil.log(e);
			return xmlFile;
		}
		Node rootElement = d.appendChild(d.createElement("SharedRoot")); //$NON-NLS-1$
		Element element = (Element) rootElement.appendChild(d.createElement("SharedProperty")); //$NON-NLS-1$
		element.setAttribute(TemplateEngineHelper.ID, ""); //$NON-NLS-1$
		element.setAttribute(TemplateEngineHelper.VALUE, ""); //$NON-NLS-1$

		DOMSource domSource = new DOMSource(d);
		TransformerFactory transFactory = TransformerFactory.newInstance();

		try {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(xmlFile);
				Result fileResult = new StreamResult(fos);
				transFactory.newTransformer().transform(domSource, fileResult);
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		} catch (IOException ioe) {
			TemplateEngineUtil.log(ioe);
		} catch (TransformerConfigurationException tce) {
			TemplateEngineUtil.log(tce);
		} catch (TransformerException te) {
			TemplateEngineUtil.log(te);
		}
		return xmlFile;
	}

	/**
	 * This method generates XML file for backupshareddefaults and
	 * shareddefaults to support consistent persistency
	 */

	private void generateSharedXML(File xmlFile) {
		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			TemplateEngineUtil.log(e);
			return;
		}
		Node rootElement = d.appendChild(d.createElement("SharedRoot")); //$NON-NLS-1$

		for (String key : sharedDefaultsMap.keySet()) {
			Element element = (Element) rootElement.appendChild(d.createElement("SharedProperty")); //$NON-NLS-1$
			element.setAttribute(TemplateEngineHelper.ID, key);
			element.setAttribute(TemplateEngineHelper.VALUE, sharedDefaultsMap.get(key));
		}

		DOMSource domSource = new DOMSource(d);
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Result fileResult = new StreamResult(xmlFile);
		try {
			transFactory.newTransformer().transform(domSource, fileResult);
		} catch (Throwable t) {
			TemplateEngineUtil.log(t);
		}
	}

	/**
	 * This method swaps the backup file name to XML file containing latest or
	 * persisted data
	 */

	private void swapXML() {
		if (parsedXML.exists())
			parsedXML.delete();
		backUpSharedXML.renameTo(parsedXML);
	}
}
