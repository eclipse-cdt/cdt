/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.ui;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.text.CHelpBookDescriptor;
import org.eclipse.cdt.internal.ui.text.CHelpSettings;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is used to manage external plugins that contribute 
 * C/C++ help information through the CHelpProvider extension point
 */

public class CHelpProviderManager {
	final private static String C_HELP_SETTINGS_FILE_NAME = "cHelpSettings.xml"; //$NON-NLS-1$
	final private static String ELEMENT_ROOT = "cHelpSettings"; //$NON-NLS-1$

	private static Map fProjectHelpSettings = null;
	private static CHelpSettings fDefaultHelpSettings = null;
	
	private static File fSettingsFile = null;
	static private CHelpProviderManager fInstance = null;

	private static IProject fCurrentProject = null;
	private static CHelpSettings fCurrentSettings = null;

	private CHelpProviderManager() {
	}
	
	private static File getSettingsFile(){
		if(fSettingsFile == null){
			fSettingsFile = CUIPlugin.getDefault().getStateLocation().append(C_HELP_SETTINGS_FILE_NAME).toFile();
		}
		return fSettingsFile;
	}
	
	private static Map getSettingsMap(){
		if(fProjectHelpSettings == null)
			fProjectHelpSettings = new HashMap();
		return fProjectHelpSettings;
	}
	
	private static CHelpSettings getDefaultHelpSettings(){
		if(fDefaultHelpSettings == null){
			fDefaultHelpSettings = new CHelpSettings(null);
		}
		return fDefaultHelpSettings;
	}

	private static CHelpSettings getPersistedHelpSettings(IProject project){
/* uncomment to use Map
		Map settingsMap = getSettingsMap();
		CHelpSettings settings = (CHelpSettings)settingsMap.get(project.getName());
		if(settings == null){
			settings = createHelpSettings(project);
			settingsMap.put(project.getName(),settings);
		}
		return settings;
*/
		return createHelpSettings(project);
	}
	
	private static CHelpSettings createHelpSettings(IProject project){
		String projectName = project.getName();
		File file = getSettingsFile();
		CHelpSettings settings = null;
		Element rootElement = null;

		if(file.isFile()){
			try{
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = builder.parse(file);
				NodeList nodes = doc.getElementsByTagName(ELEMENT_ROOT);
				
				if (nodes.getLength() > 0)
					rootElement = (Element)nodes.item(0);
				
			}catch(ParserConfigurationException e){
			}catch(SAXException e){
			}catch(IOException e){
			}
		}

		settings = new CHelpSettings(project,rootElement);
		return settings;
	}
	
	private static CHelpSettings getHelpSettings(IProject project){
		if(project == null)
			return getDefaultHelpSettings();

		CHelpSettings settings = null;
		if(fCurrentProject != null && fCurrentSettings != null && project == fCurrentProject)
			settings = fCurrentSettings;
		else{
			fCurrentProject = project;
			fCurrentSettings = getPersistedHelpSettings(project);
			settings = fCurrentSettings;
		}
		return settings;
	}
	
	private static CHelpSettings getHelpSettings(ICHelpInvocationContext context){
		IProject project = getProjectFromContext(context);
		
		return getHelpSettings(project);
	}
	
	private static IProject getProjectFromContext(ICHelpInvocationContext context){
		IProject project = context.getProject();
		if(project == null){
			ITranslationUnit unit = context.getTranslationUnit();
			if(unit != null)
				project = unit.getCProject().getProject();
		}
		return project;
	}

	public static CHelpProviderManager getDefault() {
		if (fInstance == null) {
			fInstance = new CHelpProviderManager();
		}
		return fInstance;
	}

	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, String name) {
		CHelpSettings settings = getHelpSettings(context);
	
		return settings.getFunctionInfo(context,name);
	}

	public IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, String frag) {
		CHelpSettings settings = getHelpSettings(context);
		
		return settings.getMatchingFunctions(context,frag);
	}

	public ICHelpResourceDescriptor[] getHelpResources(ICHelpInvocationContext context, String name){
		CHelpSettings settings = getHelpSettings(context);
		
		return settings.getHelpResources(context,name);
	}
	
	public CHelpBookDescriptor[] getCHelpBookDescriptors(ICHelpInvocationContext context){
		return getHelpSettings(context).getCHelpBookDescriptors();
	}
	
	public void serialize(ICHelpInvocationContext context){
		CHelpSettings settings = getHelpSettings(context);

		File file = getSettingsFile();

		try{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc;
			Element rootElement = null;

			if(file.exists()){
				doc = builder.parse(file);
				NodeList nodes = doc.getElementsByTagName(ELEMENT_ROOT);
				
				if (nodes.getLength() > 0)
					rootElement = (Element)nodes.item(0);
			}
			else{
				doc = builder.newDocument();
			}

			if(rootElement == null){
				rootElement = doc.createElement(ELEMENT_ROOT);
				doc.appendChild(rootElement);
			}

			settings.serialize(doc,rootElement);

			FileWriter writer = new FileWriter(file);

			Transformer transformer=TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(writer);

			transformer.transform(source, result);

			writer.close();
		}catch(ParserConfigurationException e){
		}catch(SAXException e){
		}catch(TransformerConfigurationException e){
		}catch(TransformerException e){
		}catch(IOException e){
		}
	}
}
