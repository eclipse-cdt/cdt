/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;


/**
 * A settings processor that imports and exports include paths.
 * 
 * @author Mike Kucera
 * @since 5.1
 * 
 */
public class IncludePathsSettingsProcessor extends SettingsProcessor {

	private static final String SECTION_NAME = "org.eclipse.cdt.internal.ui.wizards.settingswizards.IncludePaths"; //$NON-NLS-1$
	
	private static final String INCLUDE_PATH_ELEMENT = "includepath"; //$NON-NLS-1$
	
	
	public Image getIcon() {
		return CUIPlugin.getImageDescriptorRegistry().get(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER));
	}

	public String getDisplayName() {
		return Messages.ProjectSettingsWizardPage_Processor_Includes;
	}
	
	public String getSectionName() {
		return SECTION_NAME;
	}

	@Override
	protected int getSettingsType() {
		return ICSettingEntry.INCLUDE_PATH;
	}
	
	@Override
	protected void writeSettings(ContentHandler content, ICLanguageSettingEntry setting) throws SettingsImportExportException {
		char[] value = setting.getValue().toCharArray();
		
		try {
			content.startElement(NONE, NONE, INCLUDE_PATH_ELEMENT, null);
			content.characters(value, 0, value.length);
			content.endElement(NONE, NONE, INCLUDE_PATH_ELEMENT);
			newline(content);
			
		} catch (SAXException e) {
			throw new SettingsImportExportException(e);
		}
	}

	
	@Override
	protected void readSettings(ICLanguageSetting setting, Element language) throws SettingsImportExportException {
		List<ICLanguageSettingEntry> includes = new ArrayList<ICLanguageSettingEntry>();
		
		List<Element> includeNodes = XMLUtils.extractChildElements(language, INCLUDE_PATH_ELEMENT);
		for(Element includeElement : includeNodes) {
			String include = includeElement.getTextContent();
			if(include != null && include.length() > 0)
				includes.add(new CIncludePathEntry(include, 0));
		}

		if(includes.isEmpty())
			return;
		
		// need to do this or existing settings will disappear
		includes.addAll(setting.getSettingEntriesList(ICLanguageSettingEntry.INCLUDE_PATH));
		setting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, includes);
	}
}
