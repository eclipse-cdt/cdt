/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Base class implementing standard import and export functionality
 * for a section of the file.
 *
 * @author Mike Kucera
 * @since 5.1
 */
public abstract class SettingsProcessor implements ISettingsProcessor {

	protected static final String NONE = ""; //$NON-NLS-1$
	protected static final String CDATA = "CDATA"; //$NON-NLS-1$

	protected static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	protected static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	protected static final String LANGUAGE_ELEMENT = "language"; //$NON-NLS-1$

	/**
	 * Returns a constant from the ICSettingEntry interface.
	 */
	protected abstract int getSettingsType();

	protected abstract void writeSettings(ContentHandler content, ICLanguageSettingEntry setting)
			throws SettingsImportExportException;

	protected abstract void readSettings(ICLanguageSetting setting, Element language)
			throws SettingsImportExportException;

	@Override
	public void writeSectionXML(ICFolderDescription projectRoot, ContentHandler content)
			throws SettingsImportExportException {
		ICLanguageSetting[] languages = projectRoot.getLanguageSettings();
		AttributesImpl attributes = new AttributesImpl();

		try {
			for (ICLanguageSetting language : languages) {
				String languageId = language.getLanguageId();
				String languageName = language.getName();
				attributes.clear();
				if (languageId != null) {
					attributes.addAttribute(NONE, NONE, ID_ATTRIBUTE, CDATA, languageId);
				}
				attributes.addAttribute(NONE, NONE, NAME_ATTRIBUTE, CDATA, languageName);
				content.startElement(NONE, NONE, LANGUAGE_ELEMENT, attributes);

				ICLanguageSettingEntry[] settings = language.getSettingEntries(getSettingsType());

				for (ICLanguageSettingEntry setting : settings) {
					if (!setting.isBuiltIn()) {
						writeSettings(content, setting);
					}
				}

				content.endElement(NONE, NONE, LANGUAGE_ELEMENT);
			}

		} catch (SAXException e) {
			throw new SettingsImportExportException(e);
		}
	}

	@Override
	public void readSectionXML(ICFolderDescription projectRoot, Element section) throws SettingsImportExportException {
		ICLanguageSetting[] languageSettings = projectRoot.getLanguageSettings();

		Map<String, ICLanguageSetting> languageIdMap = new HashMap<>();
		Map<String, ICLanguageSetting> languageMap = new HashMap<>();
		for (ICLanguageSetting language : languageSettings) {
			languageIdMap.put(language.getLanguageId(), language);
			languageMap.put(language.getName(), language);
		}

		List<Element> elements = XMLUtils.extractChildElements(section, LANGUAGE_ELEMENT); // throws SettingsImportExportException
		for (Element languageElement : elements) {
			String languageId = languageElement.getAttribute(ID_ATTRIBUTE);
			ICLanguageSetting setting = languageIdMap.get(languageId);
			if (setting == null) {
				String languageName = languageElement.getAttribute(NAME_ATTRIBUTE);
				setting = languageMap.get(languageName);
			}
			if (setting != null)
				readSettings(setting, languageElement);
		}
	}

}
