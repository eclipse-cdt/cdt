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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Custom behavior for the Import wizard.
 * 
 * @author Mike Kucera
 * @since 5.1
 *
 */
public class ProjectSettingsImportStrategy implements IProjectSettingsWizardPageStrategy {


	@Override
	public String getMessage(MessageType type) {
		switch(type) {
		case TITLE:    return Messages.ProjectSettingsWizardPage_Import_title;
		case MESSAGE:  return Messages.ProjectSettingsWizardPage_Import_message;
		case CHECKBOX: return Messages.ProjectSettingsWizardPage_Import_checkBox;
		case FILE:     return Messages.ProjectSettingsWizardPage_Import_file;
		case SETTINGS: return Messages.ProjectSettingsWizardPage_Import_selectSettings;
		default:       return null;
		}
	}
	

	/*
	 * Start with an empty list of processors.
	 */
	@Override
	public void pageCreated(IProjectSettingsWizardPage page) {
		page.setDisplayedSettingsProcessors(Collections.<ISettingsProcessor>emptyList());
	}
	
	
	/*
	 * Collects the importers that can be applied to the file and displays 
	 * them to the user.
	 */
	@Override
	public void fileSelected(IProjectSettingsWizardPage page) {
		List<ImporterSectionPair> pairs = Collections.emptyList();
		try {
			pairs = extractSectionsFromFile(page);
			page.setMessage(getMessage(MessageType.MESSAGE), IMessageProvider.NONE); // its all good
		} catch (FileNotFoundException e) {
			page.setMessage(Messages.ProjectSettingsWizardPage_Import_openError, IMessageProvider.ERROR);
		} catch (SettingsImportExportException e) {
			page.setMessage(Messages.ProjectSettingsWizardPage_Import_parseError, IMessageProvider.ERROR);
		}
		
		List<ISettingsProcessor> importersToDisplay = new ArrayList<ISettingsProcessor>();
		for(ImporterSectionPair pair : pairs) {
			importersToDisplay.add(pair.importer);
		}
		
		// if there was an error then importersToDisplay will be empty and this will clear the list
		page.setDisplayedSettingsProcessors(importersToDisplay);
	}
	
	
	/*
	 * Parse the file again and this time actually do the import.
	 */
	@Override
	public boolean finish(IProjectSettingsWizardPage page) {
		// get the selected project and configuration
		ICConfigurationDescription config = page.getSelectedConfiguration();
		IProject project = config.getProjectDescription().getProject();
		
		// get a writable copy of the project description so that we can make changes to it
		ICProjectDescription writableDescription = CoreModel.getDefault().getProjectDescription(project, true);
		ICConfigurationDescription writableConfig = writableDescription.getConfigurationById(config.getId());
		ICFolderDescription writableProjectRoot = writableConfig.getRootFolderDescription();
		
		List<ISettingsProcessor> selectedImporters = page.getSelectedSettingsProcessors();
		
		try {
			List<ImporterSectionPair> pairs = extractSectionsFromFile(page);
			
			for(ImporterSectionPair pair : pairs) {
				if(selectedImporters.contains(pair.importer))
					pair.importer.readSectionXML(writableProjectRoot, pair.section);
			}
			
		} catch (FileNotFoundException e) {
			CUIPlugin.log(e);
			page.showErrorDialog(Messages.ProjectSettingsImportStrategy_fileOpenError, 
					             Messages.ProjectSettingsImportStrategy_couldNotOpen);
			return false;
		} catch (SettingsImportExportException e) { // error during parsing or importing
			CUIPlugin.log(e);
			page.showErrorDialog(Messages.ProjectSettingsImportStrategy_importError, 
					             Messages.ProjectSettingsImportStrategy_couldNotImport);
			return false;
		}
		
		// only if all the importing was successful do we actually write out to the .cproject file
		try {
			CoreModel.getDefault().setProjectDescription(project, writableDescription);
		} catch (CoreException e) {
			CUIPlugin.log(e);
			page.showErrorDialog(Messages.ProjectSettingsImportStrategy_importError, 
					             Messages.ProjectSettingsImportStrategy_saveError);
			return false;
		}
		
		return true;
	}
	

	private static class ImporterSectionPair {
		Element section;
		ISettingsProcessor importer;
		ImporterSectionPair(ISettingsProcessor importer, Element section) {
			this.importer = importer;
			this.section = section;
		}
	}
	
	/*
	 * Attempts to parse the file the user selected.
	 */
	private List<ImporterSectionPair> extractSectionsFromFile(IProjectSettingsWizardPage page) throws FileNotFoundException, SettingsImportExportException {
		// get the file path that the user input
		String filePath = page.getDestinationFilePath();
		
		// get all the importers
		Map<String,ISettingsProcessor> importers = new HashMap<String,ISettingsProcessor>();
		for(ISettingsProcessor processor : page.getSettingsProcessors()) {
			importers.put(processor.getSectionName(), processor);
		}
		
		FileInputStream in = new FileInputStream(filePath); // throws FileNotFoundException
		
		// try to parse the file as generic XML with no schema
		Document document = parse(in);
		
		// now try to get a list of <section> elements
		Element root = document.getDocumentElement();
		List<Element> sections = XMLUtils.extractChildElements(root, ProjectSettingsExportStrategy.SECTION_ELEMENT);
		
		List<ImporterSectionPair> pairs = new ArrayList<ImporterSectionPair>();
		
		// associate an importer with each section
		for(Element section : sections) {
			String sectionName = section.getAttribute(ProjectSettingsExportStrategy.SECTION_NAME_ATTRIBUTE);
			if(sectionName != null) {
				ISettingsProcessor importer = importers.get(sectionName);
				
				// if there is an importer available for the section then delegate to it
				if(importer != null)
					pairs.add(new ImporterSectionPair(importer, section));
			}
		}
		
		return pairs;
	}
	
	
	/**
	 * An error handler that aborts the XML parse at the first sign
	 * of any kind of problem.
	 */
	private static ErrorHandler ABORTING_ERROR_HANDER = new ErrorHandler() {
		@Override
		public void error(SAXParseException e) throws SAXException {
			throw e;
		}
		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			throw e;
		}
		@Override
		public void warning(SAXParseException e) throws SAXException {
			throw e;
		}
	};
	
	
	/*
	 * Uses JAXP to parse the file. Returns null if the file could
	 * not be parsed as XML.
	 * 
	 * Not validating because I want to make it easy to add new settings processors.
     * Eventually there could be an extension point for adding settings processors
	 * so I'm coding everything with the assumption that each settings processor
	 * will do its own validation programatically.
	 */
	private static Document parse(InputStream in) throws SettingsImportExportException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		factory.setIgnoringComments(true);
		
		try {
			DocumentBuilder parser = factory.newDocumentBuilder();
			parser.setErrorHandler(ABORTING_ERROR_HANDER); // causes SAXException to be thrown on any parse error
			InputSource input = new InputSource(in); // TODO should I be using an InputSource?
			Document doc = parser.parse(input);
			return doc;
			
		} catch (Exception e) {
			throw new SettingsImportExportException(e);
		}
	}
	
	
	
}
