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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * A settings processor that imports and exports symbols.
 * 
 * @author Mike Kucera
 * @since 5.1
 * 
 */
public class MacroSettingsProcessor extends SettingsProcessor {

	public static final String SECTION_NAME = "org.eclipse.cdt.internal.ui.wizards.settingswizards.Macros"; //$NON-NLS-1$
	
	private static final String MACRO_ELEMENT = "macro"; //$NON-NLS-1$
	private static final String NAME_ELEMENT = "name";   //$NON-NLS-1$
	private static final String VALUE_ELEMENT = "value"; //$NON-NLS-1$
	
	
	@Override
	public Image getIcon() {
		return CUIPlugin.getImageDescriptorRegistry().get(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_MACRO));
	}

	@Override
	public String getDisplayName() {
		return Messages.ProjectSettingsWizardPage_Processor_Macros;
	}

	@Override
	public String getSectionName() {
		return SECTION_NAME;
	}

	@Override
	protected int getSettingsType() {
		return ICSettingEntry.MACRO;
	}
	
	@Override
	protected void writeSettings(ContentHandler content, ICLanguageSettingEntry setting) throws SettingsImportExportException {
		char[] name = setting.getName().toCharArray();
		char[] value = setting.getValue().toCharArray();
		
		try {
			content.startElement(NONE, NONE, MACRO_ELEMENT, null);
			newline(content);
			
			content.startElement(NONE, NONE, NAME_ELEMENT, null);
			content.characters(name, 0, name.length);
			content.endElement(NONE, NONE, NAME_ELEMENT);
			
			content.startElement(NONE, NONE, VALUE_ELEMENT, null);
			content.characters(value, 0, value.length);
			content.endElement(NONE, NONE, VALUE_ELEMENT);
			newline(content);
			
			content.endElement(NONE, NONE, MACRO_ELEMENT);
			newline(content);
			
		} catch(SAXException e) {
			throw new SettingsImportExportException(e);
		}
	}
	
	
	@Override
	protected void readSettings(ICLanguageSetting setting, Element language) throws SettingsImportExportException {
		List<ICLanguageSettingEntry> macros = new ArrayList<ICLanguageSettingEntry>();
		
		List<Element> macrosNodes = XMLUtils.extractChildElements(language, MACRO_ELEMENT);
		
		for(Element macroElement : macrosNodes) {
			String name = null;
			String value = null;
			
			NodeList nodeList = macroElement.getChildNodes();
			for(int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				switch(node.getNodeType()) {
				case Node.TEXT_NODE:
					Text text = (Text)node;
					if(XMLUtils.isWhitespace(text.getData()))
						break;
					throw new SettingsImportExportException("Unknown text: '" + text.getData() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				case Node.ELEMENT_NODE:
					Element element = (Element)node;
					String tagName = element.getTagName();
					if(name == null && tagName.equals(NAME_ELEMENT))
						name = element.getTextContent();
					else if(value == null && tagName.equals(VALUE_ELEMENT))
						value = element.getTextContent();
					else
						throw new SettingsImportExportException("Unknown or extra tag: " + tagName); //$NON-NLS-1$
					break;
				default:
					throw new SettingsImportExportException("Unknown node: " + node.getNodeName()); //$NON-NLS-1$
				}
			}
			
			if(name == null)
				throw new SettingsImportExportException("There must be one <name> element"); //$NON-NLS-1$
			if(value == null)
				throw new SettingsImportExportException("There must be one <value> element"); //$NON-NLS-1$
			
			macros.add(new CMacroEntry(name, value, 0));
		}
		
		if(macros.isEmpty())
			return;
		
		// need to do this or existing settings will disappear
		macros.addAll(setting.getSettingEntriesList(ICSettingEntry.MACRO));
		setting.setSettingEntries(ICSettingEntry.MACRO, macros);
	}
	
}
