/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;

/**
 * Acts as an Helper class for Template Engine
 * 
 * @since 4.0
 */
public class TemplateEngineHelper {
	public static final String US = "_";  //$NON-NLS-1$
	public static final String OPEN_MARKER = "$("; //$NON-NLS-1$
	public static final String CLOSE_MARKER = ")"; //$NON-NLS-1$
	public static final String STRING_EXTERNALIZATION_MARKER = "%"; //$NON-NLS-1$
	public static final String LOGGER_FILE_NAME = "Process"; //$NON-NLS-1$
	// This is used while getting the Plugin Path.
	public static final String PROJRESOURCE = "plugin.xml"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "pluginId"; //$NON-NLS-1$
	public static final String PLUGIN_PROPERTIES = "plugin.properties"; //$NON-NLS-1$
	public static final String TEMPLATE_PROPERTIES = "template.properties"; //$NON-NLS-1$
	public static final String BOOLTRUE = "true"; //$NON-NLS-1$
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String SDLOG_FILE_NAME = "sharedDefaults"; //$NON-NLS-1$
	public static final String LOCATION = "location"; //$NON-NLS-1$
	public static final String WIZARD_ID = "wizardId"; //$NON-NLS-1$
	public static final String FILTER_PATTERN = "filterPattern"; //$NON-NLS-1$
	public static final String USAGE_DESCRIPTION = "usageDescription"; //$NON-NLS-1$
	public static final String PROJECT_TYPE = "projectType"; //$NON-NLS-1$
	public static final String TOOL_CHAIN = "toolChain"; //$NON-NLS-1$
	public static final String EXTRA_PAGES_PROVIDER = "pagesAfterTemplateSelectionProvider"; //$NON-NLS-1$
	public static final String IS_CATEGORY = "isCategory"; //$NON-NLS-1$

	/**
	 * Gets the backup shareddefaults XML file. Presence of the file indicates
	 * that the template engine or the application underwent some crash or
	 * destruction.
	 * 
	 * @param sharedLocation
	 * @return sharedXMLFile
     * 
     * @since 4.0
	 */

	public static File getSharedDefaultLocation(String sharedLocation) {
		File sharedXMLFile = findLocation(sharedLocation);
		return sharedXMLFile;
	}

	/**
	 * Finds the location of the shareddefaults backup and original xml file.
	 * 
	 * @param fileLocation
	 * @return file
     * 
     * @since 4.0
	 */
	private static File findLocation(String fileLocation) {
		Plugin plugin = CCorePlugin.getDefault();
		IPath stateLoc = plugin.getStateLocation();
		fileLocation = stateLoc.toString() + File.separator + fileLocation;
		File file = new File(fileLocation);
		return file;
	}

	/**
	 * Stores the shareddefaults xml file in
	 * "${workspace}/.metadata/.plugins/${plugin.name}/shareddefaults.xml" path.
	 * 
	 * @param sharedLocation the relative path within the plug-in
	 * @return a File object corresponding to the location within the plug-in
     * 
     * @since 4.0
	 */
	public static File storeSharedDefaultLocation(String sharedLocation) {
		File sharedXMLFile = findLocation(sharedLocation);
		try {
			if(!sharedXMLFile.exists()) {
				sharedXMLFile.createNewFile();
			}
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return sharedXMLFile;
	}

	/**
	 * This method returns the workspace path present in the workspace
	 * 
	 * @return String Example : file:/C:/eclipse/workspace/
     * 
     * @since 4.0
	 */
	public static IPath getWorkspacePath() {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath workSpacePath = new Path(root.getLocation().toString() + File.separator);
		return workSpacePath;

	}

	/**
	 * @param markerString
	 * @return the first content of a region matching $(.*) e.g. given a String of the form "foo $(ID) bar", return ID.
     * 
     * @since 4.0
	 */
	public static String getFirstMarkerID(String markerString) {
		String key = null;
		if (markerString.indexOf(OPEN_MARKER) != -1) {
			key = markerString.substring(markerString.indexOf(OPEN_MARKER) + OPEN_MARKER.length(), markerString
					.indexOf(CLOSE_MARKER));
		}
		return key;
	}

	/**
	 * Check whether there is a directory existing in present workspace, with
	 * the given name.
	 * 
	 * @param directoryName
	 * @return true, if directory exists.
     * 
     * @since 4.0
	 */
	public static boolean checkDirectoryInWorkspace(String directoryName) {

		boolean retVal = false;
		File file = null;

		try {
			file = new File(getWorkspacePath() + directoryName);
		} catch (Exception exp) {

		}
		if ((file != null) && (file.exists()) && (file.isDirectory())) {
			retVal = true;
		}
		return retVal;
	}
	/**
	 * Return Template Source path as URL
	 * @param pluginId
	 * @param resourcePath
	 * @return URL, of the Template Resource
	 * @throws IOException
     * 
     * @since 4.0
	 */

	public static URL getTemplateResourceURL(String pluginId, String resourcePath) throws IOException {
		return FileLocator.find(Platform.getBundle(pluginId), new Path(resourcePath), null);
	}

	/**
	 * 
	 * Returns the Template Resource Relative Path as URL 
	 * @param template
	 * @param resourcePath
	 * @return URL, of the Template Resource
	 * @throws IOException
     * 
     * @since 4.0
	 */
	public static URL getTemplateResourceURLRelativeToTemplate(TemplateCore template, String resourcePath) throws IOException {
		TemplateInfo templateInfo = template.getTemplateInfo();
		String path = templateInfo.getTemplatePath();
		int slash = path.lastIndexOf("/"); //$NON-NLS-1$
		if (slash == -1) {
			path = resourcePath;
		} else {
			path = path.substring(0, slash + 1) + resourcePath;
		}
		URL entry = FileLocator.find(Platform.getBundle(templateInfo.getPluginId()), new Path(path), null);
		if (entry == null) {
			return null;
		}
		return FileLocator.toFileURL(entry);
	}

	public static String externalizeTemplateString(TemplateInfo ti, String key) {
		if (key.startsWith(STRING_EXTERNALIZATION_MARKER)) {
			String pluginId = ti.getPluginId();
			String path = ti.getTemplatePath();
			IPath p = new Path(path);
			String propertiesPath = TEMPLATE_PROPERTIES;
			if(p.segmentCount() != 0){
				p = p.removeLastSegments(1);
				propertiesPath = p.append(propertiesPath).toString();
			}
			return externalizeTemplateString(pluginId, propertiesPath, key);
		}
		return key;
	}

	public static String externalizeTemplateString(String pluginId, String location, String key) {
		String value= null;
		if (key != null && key.startsWith(STRING_EXTERNALIZATION_MARKER)) {
			try {
				value = location != null ? getValueFromProperties(pluginId, location, key.substring(1)) : null;
				if (value == null) {
					value = getValueFromProperties(pluginId, PLUGIN_PROPERTIES, key.substring(1));
				}
			} catch (IOException e) {
				value = key;
				e.printStackTrace();
			}
		}
		return value == null ? key : value;
	}
	
	private static String getValueFromProperties(String pluginId, String propertiesFile, String key) throws IOException {
		String value = null;
		Bundle b = Platform.getBundle(pluginId);
		URL url= getResourceURL(b, propertiesFile);
		if (url != null) {
			InputStream in= url.openStream();
			Properties p = new Properties();
			p.load(in);
			value = (String) p.get(key);
		}
		return value;
		
	}
	
	private static URL getResourceURL(Bundle bundle, String propertiesFile) {
		return FileLocator.find(bundle, new Path(propertiesFile), null);
	}
}
