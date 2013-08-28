package org.eclipse.cdt.internal.ui.wizards.settingswizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

public class LibrariesSettingsProcessor extends SettingsProcessor {

	private static final String SECTION_NAME = "org.eclipse.cdt.internal.ui.wizards.settingswizards.Libraries"; //$NON-NLS-1$
	private static final String LIB_ELEMENT = "lib"; //$NON-NLS-1$
	private static final String WORKSPACE_PATH_ATTR = "workspace_path"; //$NON-NLS-1$
	
	
	@Override
	public Image getIcon() {
		return CUIPlugin.getImageDescriptorRegistry().get(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_LIBRARY));
	}

	@Override
	public String getDisplayName() {
		return Messages.ProjectSettingsWizardPage_Processor_Libs;
	}

	@Override
	public String getSectionName() {
		return SECTION_NAME;
	}

	@Override
	protected int getSettingsType() {
		return ICSettingEntry.LIBRARY_FILE;
	}

	@Override
	protected void writeSettings(ContentHandler content, ICLanguageSettingEntry setting) throws SettingsImportExportException {
		char[] value = setting.getValue().toCharArray();

		try {
			AttributesImpl attrib = null;
			if( (setting.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) > 0 ) {
				attrib = new AttributesImpl();
				attrib.addAttribute(NONE, NONE, WORKSPACE_PATH_ATTR, NONE, Boolean.TRUE.toString());
			}
			content.startElement(NONE, NONE, LIB_ELEMENT, attrib);
			content.characters(value, 0, value.length);
			content.endElement(NONE, NONE, LIB_ELEMENT);
			newline(content);

		} catch (SAXException e) {
			throw new SettingsImportExportException(e);
		}
	}


	@Override
	protected void readSettings(ICLanguageSetting setting, Element language) throws SettingsImportExportException {
		List<ICLanguageSettingEntry> libs = new ArrayList<ICLanguageSettingEntry>();

		List<Element> libNodes = XMLUtils.extractChildElements(language, LIB_ELEMENT);
		for(Element libElement : libNodes) {
			String lib = libElement.getTextContent();
			int flags = 0;
			if(lib != null && lib.length() > 0) {
				if( libElement.getAttribute(WORKSPACE_PATH_ATTR).equalsIgnoreCase(Boolean.TRUE.toString()) )
					flags |= ICSettingEntry.VALUE_WORKSPACE_PATH;
				libs.add(CDataUtil.createCLibraryFileEntry(lib, flags));
			}
		}

		if(libs.isEmpty())
			return;

		// need to do this or existing settings will disappear
		libs.addAll(setting.getSettingEntriesList(ICSettingEntry.LIBRARY_FILE));
		setting.setSettingEntries(ICSettingEntry.LIBRARY_FILE, libs);
	}

}
