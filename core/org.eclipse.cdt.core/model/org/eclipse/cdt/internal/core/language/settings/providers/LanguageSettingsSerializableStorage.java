/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The class representing persistent storage for language settings entries {@link ICLanguageSettingEntry}.
 */
public class LanguageSettingsSerializableStorage extends LanguageSettingsStorage {
	private static final String ELEM_LANGUAGE = "language"; //$NON-NLS-1$
	private static final String ATTR_LANGUAGE_ID = "id"; //$NON-NLS-1$
	private static final String ELEM_RESOURCE = "resource"; //$NON-NLS-1$
	private static final String ATTR_PROJECT_PATH = "project-relative-path"; //$NON-NLS-1$

	private static final String ELEM_ENTRY = LanguageSettingsExtensionManager.ELEM_ENTRY;
	private static final String ATTR_ENTRY_KIND = LanguageSettingsExtensionManager.ATTR_ENTRY_KIND;
	private static final String ATTR_ENTRY_NAME = LanguageSettingsExtensionManager.ATTR_ENTRY_NAME;
	private static final String ATTR_ENTRY_VALUE = LanguageSettingsExtensionManager.ATTR_ENTRY_VALUE;
	private static final String ELEM_ENTRY_FLAG = LanguageSettingsExtensionManager.ELEM_ENTRY_FLAG;

	/**
	 * Serialize the provider entries under parent XML element.
	 *
	 * @param elementProvider - element where to serialize the entries.
	 */
	public void serializeEntries(Element elementProvider) {
		synchronized (fStorage) {
			for (Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang : fStorage.entrySet()) {
				serializeLanguage(elementProvider, entryLang.getKey(), entryLang.getValue());
			}
		}
	}

	/**
	 * Serialize the provider entries for a given language list.
	 */
	private void serializeLanguage(Element parentElement, String langId,
			Map<String, List<ICLanguageSettingEntry>> langMap) {
		if (langId != null) {
			Element elementLanguage = XmlUtil.appendElement(parentElement, ELEM_LANGUAGE,
					new String[] { ATTR_LANGUAGE_ID, langId });
			parentElement = elementLanguage;
		}
		for (Entry<String, List<ICLanguageSettingEntry>> entryRc : langMap.entrySet()) {
			serializeResource(parentElement, entryRc.getKey(), entryRc.getValue());
		}
	}

	/**
	 * Serialize the provider entries for a given resource list.
	 */
	private void serializeResource(Element parentElement, String rcProjectPath, List<ICLanguageSettingEntry> rcList) {
		if (rcProjectPath != null) {
			Element elementRc = XmlUtil.appendElement(parentElement, ELEM_RESOURCE,
					new String[] { ATTR_PROJECT_PATH, rcProjectPath });
			parentElement = elementRc;
		}
		serializeSettingEntries(parentElement, rcList);
	}

	/**
	 * Serialize given settings entries.
	 */
	private void serializeSettingEntries(Element parentElement, List<ICLanguageSettingEntry> settingEntries) {
		for (ICLanguageSettingEntry entry : settingEntries) {
			Element elementSettingEntry = XmlUtil.appendElement(parentElement, ELEM_ENTRY,
					new String[] { ATTR_ENTRY_KIND, LanguageSettingEntriesSerializer.kindToString(entry.getKind()),
							ATTR_ENTRY_NAME, entry.getName(), });
			switch (entry.getKind()) {
			case ICSettingEntry.MACRO:
				elementSettingEntry.setAttribute(ATTR_ENTRY_VALUE, entry.getValue());
				break;
			//			case ICLanguageSettingEntry.LIBRARY_FILE:
			//				// YAGNI: sourceAttachment fields may need to be covered
			//				break;
			}
			int flags = entry.getFlags();
			if (flags != 0) {
				// Element elementFlag =
				XmlUtil.appendElement(elementSettingEntry, ELEM_ENTRY_FLAG, new String[] { ATTR_ENTRY_VALUE,
						LanguageSettingEntriesSerializer.composeFlagsString(entry.getFlags()) });
			}
		}
	}

	/**
	 * Load provider entries from XML provider element.
	 *
	 * @param providerNode - parent XML element "provider" where entries are defined.
	 */
	public void loadEntries(Element providerNode) {
		List<ICLanguageSettingEntry> settings = new ArrayList<>();
		NodeList nodes = providerNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node elementNode = nodes.item(i);
			if (elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_LANGUAGE.equals(elementNode.getNodeName())) {
				loadLanguageElement(elementNode, null);
			} else if (ELEM_RESOURCE.equals(elementNode.getNodeName())) {
				loadResourceElement(elementNode, null, null);
			} else if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry != null) {
					settings.add(entry);
				}
			}
		}
		// set settings
		if (settings.size() > 0) {
			setSettingEntries(null, null, settings);
		}
	}

	/**
	 * Load a setting entry from XML element.
	 */
	private ICLanguageSettingEntry loadSettingEntry(Node parentElement) {
		String settingKind = XmlUtil.determineAttributeValue(parentElement, ATTR_ENTRY_KIND);
		String settingName = XmlUtil.determineAttributeValue(parentElement, ATTR_ENTRY_NAME);

		NodeList flagNodes = parentElement.getChildNodes();
		int flags = 0;
		for (int i = 0; i < flagNodes.getLength(); i++) {
			Node flagNode = flagNodes.item(i);
			if (flagNode.getNodeType() != Node.ELEMENT_NODE || !ELEM_ENTRY_FLAG.equals(flagNode.getNodeName()))
				continue;

			String settingFlags = XmlUtil.determineAttributeValue(flagNode, ATTR_ENTRY_VALUE);
			int bitFlag = LanguageSettingEntriesSerializer.composeFlags(settingFlags);
			flags |= bitFlag;

		}

		String settingValue = null;
		int kind = LanguageSettingEntriesSerializer.stringToKind(settingKind);
		if (kind == ICSettingEntry.MACRO)
			settingValue = XmlUtil.determineAttributeValue(parentElement, ATTR_ENTRY_VALUE);
		ICLanguageSettingEntry entry = (ICLanguageSettingEntry) CDataUtil.createEntry(kind, settingName, settingValue,
				null, flags);
		return entry;
	}

	/**
	 * Load entries defined in language element.
	 */
	private void loadLanguageElement(Node parentNode, String cfgId) {
		String langId = XmlUtil.determineAttributeValue(parentNode, ATTR_LANGUAGE_ID);
		if (langId.length() == 0) {
			langId = null;
		}

		List<ICLanguageSettingEntry> settings = new ArrayList<>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node elementNode = nodes.item(i);
			if (elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_RESOURCE.equals(elementNode.getNodeName())) {
				loadResourceElement(elementNode, cfgId, langId);
			} else if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry != null) {
					settings.add(entry);
				}
			}
		}
		// set settings
		if (settings.size() > 0) {
			setSettingEntries(null, langId, settings);
		}
	}

	/**
	 * Load entries defined in resource element.
	 */
	private void loadResourceElement(Node parentNode, String cfgId, String langId) {
		String rcProjectPath = XmlUtil.determineAttributeValue(parentNode, ATTR_PROJECT_PATH);

		List<ICLanguageSettingEntry> settings = new ArrayList<>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node elementNode = nodes.item(i);
			if (elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry != null) {
					settings.add(entry);
				}
			}
		}

		// set settings
		if (settings.size() > 0) {
			setSettingEntries(rcProjectPath, langId, settings);
		}
	}

	@Override
	public LanguageSettingsSerializableStorage clone() throws CloneNotSupportedException {
		return (LanguageSettingsSerializableStorage) super.clone();
	}

}
