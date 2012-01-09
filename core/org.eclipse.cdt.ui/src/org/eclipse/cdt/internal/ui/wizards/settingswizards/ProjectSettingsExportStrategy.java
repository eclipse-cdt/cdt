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


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.eclipse.cdt.core.resources.ResourcesUtil;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.ui.CUIPlugin;


/**
 * Custom behavior for the Export wizard.
 * 
 * @author Mike Kucera
 * @since 5.1
 *
 */
public class ProjectSettingsExportStrategy implements IProjectSettingsWizardPageStrategy {

	public static final String ROOT_ELEMENT = "cdtprojectproperties"; //$NON-NLS-1$
	public static final String SECTION_ELEMENT = "section"; //$NON-NLS-1$
	public static final String SECTION_NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	
	
	
	private static final String NONE = ""; //$NON-NLS-1$
	private static final String CDATA = "CDATA"; //$NON-NLS-1$
	
	
	
	@Override
	public String getMessage(MessageType type) {
		switch(type) {
		case TITLE:    return Messages.ProjectSettingsWizardPage_Export_title;
		case MESSAGE:  return Messages.ProjectSettingsWizardPage_Export_message;
		case CHECKBOX: return Messages.ProjectSettingsWizardPage_Export_checkBox;
		case FILE:     return Messages.ProjectSettingsWizardPage_Export_file;
		case SETTINGS: return Messages.ProjectSettingsWizardPage_Export_selectSettings;
		default:       return null;
		}
	}
	

	@Override
	public void pageCreated(IProjectSettingsWizardPage page) {
		// do nothing
	}

	
	@Override
	public void fileSelected(IProjectSettingsWizardPage page) {
		// do nothing
	}

	
	private Writer getWriter(IProjectSettingsWizardPage page) throws IOException {
		IPath path = new Path(page.getDestinationFilePath());
		if(!IProjectSettingsWizardPage.FILENAME_EXTENSION.equals(path.getFileExtension()))
			path.addFileExtension(IProjectSettingsWizardPage.FILENAME_EXTENSION);
		
		return new FileWriter(path.toFile());
	}
	

	/**
	 * Exports the selected project settings to an XML file.
	 */
	@Override
	public boolean finish(IProjectSettingsWizardPage page) {
		SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler handler = null;
		try {
			handler = factory.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			CUIPlugin.log(e);
			page.showErrorDialog(Messages.ProjectSettingsExportStrategy_exportError, 
					             Messages.ProjectSettingsExportStrategy_exportFailed);
			return false;
		}
		
		// gets a writer for the file that was selected by the user
		Writer writer;
		try {
			writer = getWriter(page);
		} catch(IOException e) {
			page.showErrorDialog(Messages.ProjectSettingsExportStrategy_fileOpenError, 
					             Messages.ProjectSettingsExportStrategy_couldNotOpen);
			return false;
		}
		
		// stream the results to the writer as text
		handler.setResult(new StreamResult(writer));
		
		// write out the XML header
		Transformer transformer = handler.getTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		
		List<ISettingsProcessor> exporters = page.getSelectedSettingsProcessors();
		
		ICConfigurationDescription config = page.getSelectedConfiguration();
		ICFolderDescription projectRoot = config.getRootFolderDescription();
		
		boolean result = false;
		try {
			AttributesImpl attributes = new AttributesImpl();
			
			handler.startDocument();
			newline(handler);
			handler.startElement(NONE, NONE, ROOT_ELEMENT, null);
			newline(handler);
			
			for(ISettingsProcessor exporter : exporters) {
				attributes.clear();
				attributes.addAttribute(NONE, NONE, SECTION_NAME_ATTRIBUTE, CDATA, exporter.getSectionName());
				handler.startElement(NONE, NONE, SECTION_ELEMENT, attributes);
				newline(handler);
				
				// each exporter is responsible for writing out its own section of the file
				exporter.writeSectionXML(projectRoot, handler);
				
				handler.endElement(NONE, NONE, SECTION_ELEMENT);
				newline(handler);
			}
			
			handler.endElement(NONE, NONE, ROOT_ELEMENT);
			newline(handler);
			handler.endDocument();
			newline(handler);
			
			result = true;
		} catch (SAXException e) {
			CUIPlugin.log(e);
			page.showErrorDialog(Messages.ProjectSettingsExportStrategy_exportError, 
					             Messages.ProjectSettingsExportStrategy_xmlError);
			result = false;
		} catch(SettingsImportExportException e) {
			CUIPlugin.log(e);
			page.showErrorDialog(Messages.ProjectSettingsExportStrategy_fileOpenError, 
					             Messages.ProjectSettingsExportStrategy_couldNotOpen);
			result = false;
		}
		
		URI uri = URIUtil.toURI(page.getDestinationFilePath());
		ResourcesUtil.refreshWorkspaceFiles(uri);
		
		return result;
	}
	
	
	
	
	
	
	/**
	 * Outputs a newline (\n) to the given ContentHandler.
	 */
	private static void newline(ContentHandler handler) throws SAXException {
		handler.ignorableWhitespace("\n".toCharArray(), 0, 1); //$NON-NLS-1$
	}

}
