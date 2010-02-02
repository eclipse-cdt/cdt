/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [208951] Use remoteFileTypes extension point to determine file types
 * Martin Oberhuber (Wind River) - [220020][api][breaking] SystemFileTransferModeRegistry should be internal
 * David McKnight (IBM)  - [283033] remoteFileTypes extension point should include "xml" type
 *******************************************************************************/
package org.eclipse.rse.internal.subsystems.files.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeRegistry;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;



/**
 * An internal class.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */

public class SystemFileTransferModeRegistry implements ISystemFileTransferModeRegistry {

	private static SystemFileTransferModeRegistry instance;

	private HashMap typeModeMappings;

	private RSEUIPlugin plugin;

	// Constants for reading from and writing to xml file
	private static final String FILENAME = "fileTransferMode.xml"; //$NON-NLS-1$
	private static final String ENCODING = SystemEncodingUtil.ENCODING_UTF_8;
	private static final String ROOT_NODE = "mode"; //$NON-NLS-1$
	private static final String INFO_NODE = "info"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	private static final String EXTENSION_ATTRIBUTE = "extension"; //$NON-NLS-1$
	private static final String MODE_ATTRIBUTE = "mode"; //$NON-NLS-1$
	private static final String BINARY_VALUE = "binary";  //$NON-NLS-1$
	private static final String TEXT_VALUE = "text"; //$NON-NLS-1$
	private static final String XML_VALUE = "xml"; //$NON-NLS-1$s
	private static final String PRIORITY_ATTRIBUTE = "priority"; //$NON-NLS-1$

	/**
	 * Constructor for SystemFileTransferModeRegistry
	 */
	private SystemFileTransferModeRegistry() {
		super();
		this.plugin = RSEUIPlugin.getDefault();
		initialize();
	}


	/**
	 * Get the singleton instance
	 */
	public static SystemFileTransferModeRegistry getInstance() {

		if (instance == null) {
			instance = new SystemFileTransferModeRegistry();
		}

		return instance;
	}

	/**
	 * Delete's the existing file associations and reinitializes with defaults
	 */
	public void renit()
	{
		deleteAssociations();
		initialize();
	}

	/**
	 * Initialize the registry from storage.
	 */
	private void initialize() {

		// load our current associations (if any)
		loadAssociations();


		// get reference to the extension registry
		IExtensionRegistry extRegistry = Platform.getExtensionRegistry();

		// get extensions to our extension point
		IConfigurationElement[] elements = extRegistry.getConfigurationElementsFor("org.eclipse.rse.subsystems.files.core", "remoteFileTypes"); //$NON-NLS-1$ //$NON-NLS-2$

		// go through all extensions
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];

			// get the extension attribute value
			String extension = element.getAttribute("extension"); //$NON-NLS-1$

			if (extension != null && !extension.equals("")) { //$NON-NLS-1$

				// get the type attribute value
				String type = element.getAttribute("type"); //$NON-NLS-1$

				if (type != null && !type.equals("")) { //$NON-NLS-1$

					SystemFileTransferModeMapping mapping = new SystemFileTransferModeMapping(extension);

					// add extension to list of text types
					if (type.equalsIgnoreCase("text")) { //$NON-NLS-1$
						mapping.setAsText();
					}
					// add extension to list of binary types
					if (type.equalsIgnoreCase("binary")) { //$NON-NLS-1$
						mapping.setAsBinary();
					}
					
					// add extension to list of xml types
					if (type.equalsIgnoreCase("xml")) { //$NON-NLS-1$
						mapping.setAsXML();
					}

					int priority = SystemFileTransferModeMapping.DEFAULT_PRIORITY;
					String priorityStr = element.getAttribute("priority"); //$NON-NLS-1$
					try
					{
						if (priorityStr != null && !priorityStr.equals("")){ //$NON-NLS-1$
							priority = Integer.parseInt(priorityStr);
						}
					}
					catch (Exception e)
					{
					}
					mapping.setPriority(priority);

					String key = getMappingKey(mapping);
					if (!typeModeMappings.containsKey(key)){
						typeModeMappings.put(key, mapping);
					}
					else {
						SystemFileTransferModeMapping existingMapping = (SystemFileTransferModeMapping)typeModeMappings.get(key);
						int existingPriority = existingMapping.getPriority();
						if (priority < existingPriority){

							// change properties of existing mapping to that of new priority
							if (mapping.isBinary() && existingMapping.isText()){
								existingMapping.setAsBinary();
							}
							else if (mapping.isText() && existingMapping.isBinary()){
								existingMapping.setAsText();
							}

							existingMapping.setPriority(priority);
						}
					}
				}

			}
			else {
				continue;
			}
		}
	}

	/**
	 * @see ISystemFileTransferModeRegistry#getModeMappings()
	 */
	public ISystemFileTransferModeMapping[] getModeMappings() {
		List sortedMappings = sortedTypeModeMappings();				// sort hash table elements
		ISystemFileTransferModeMapping[] array = new ISystemFileTransferModeMapping[sortedMappings.size()];
		sortedMappings.toArray(array);
		return array;
	}


	/**
	 * The mappings are kept in a hash map for fast lookup. Sorting is
	 * typically only needed for certain dialogs/choices etc.
	 */
	private List sortedTypeModeMappings() {
		Comparator c = new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = ((ISystemFileTransferModeMapping)o1).getLabel().toUpperCase();
				String s2 = ((ISystemFileTransferModeMapping)o2).getLabel().toUpperCase();
				return s1.compareTo(s2);
			}
		};
		SortedSet s = new TreeSet(c);
		s.addAll(typeModeMappings.values());
		List result = new ArrayList(s);
		return result;
	}


	/**
	 * Sets new mode mappings
	 */
	public void setModeMappings(SystemFileTransferModeMapping[] newMappings) {
		typeModeMappings = new HashMap();

		for (int i = 0; i < newMappings.length; i++) {
			SystemFileTransferModeMapping mapping = newMappings[i];
			typeModeMappings.put(getMappingKey(mapping), mapping);
		}
	}


	/**
	 * Return a key given the mapping
	 */
	private String getMappingKey(ISystemFileTransferModeMapping mapping) {
		return mapping.getLabel().toLowerCase();
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isBinary(String)
	 */
	public boolean isBinary(String fileName) {
		return getMapping(fileName).isBinary();
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isBinary(File)
	 */
	public boolean isBinary(File file) {
		return isBinary(file.getName());
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isBinary(IFile)
	 */
	public boolean isBinary(IFile file) {
		return isBinary(file.getName());
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isBinary(IRemoteFile)
	 */
	public boolean isBinary(IRemoteFile remoteFile) {
		return isBinary(remoteFile.getName());
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isText(String)
	 */
	public boolean isText(String fileName) {
		return getMapping(fileName).isText();
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isText(File)
	 */
	public boolean isText(File file) {
		return isText(file.getName());
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isText(IFile)
	 */
	public boolean isText(IFile file) {
		return isText(file.getName());
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isText(IRemoteFile)
	 */
	public boolean isText(IRemoteFile remoteFile) {
		return isText(remoteFile.getName());
	}

	/**
	 * @see ISystemFileTransferModeRegistry#isText(String)
	 */
	public boolean isXML(String fileName) {
		return getMapping(fileName).isXML();
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isText(File)
	 */
	public boolean isXML(File file) {
		return isXML(file.getName());
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isText(IFile)
	 */
	public boolean isXML(IFile file) {
		return isXML(file.getName());
	}


	/**
	 * @see ISystemFileTransferModeRegistry#isText(IRemoteFile)
	 */
	public boolean isXML(IRemoteFile remoteFile) {
		return isXML(remoteFile.getName());
	}
	
	/**
	 * Get the mode mapping given a file name
	 */
	private SystemFileTransferModeMapping getMapping(String fileName) {
		SystemFileTransferModeMapping mapping = (SystemFileTransferModeMapping)(typeModeMappings.get(fileName.toLowerCase()));

		if (mapping == null) {
			mapping = createMappingFromModeMappings(fileName);
		}

		if (mapping == null) {
			return getDefaultMapping(fileName);
		}

		return mapping;
	}

	private SystemFileTransferModeMapping createMappingFromModeMappings(String fileName)
	{
		// get file extension
		//DY int extIndex = fileName.indexOf('.');
		int extIndex = fileName.lastIndexOf('.');

		String name = null;
		String extension = null;

		// if there is no extension
		if ((extIndex == -1) || (extIndex == (fileName.length() - 1))) {
			name = fileName;
		}
		else {
			name = fileName.substring(0, extIndex);
			extension = fileName.substring(extIndex + 1);
		}

		// check if the name and extension combination exists already
		SystemFileTransferModeMapping mapping = (SystemFileTransferModeMapping)(typeModeMappings.get(getMappingKey(new SystemFileTransferModeMapping(name, extension))));

		// if not, check only for the extension
		if (mapping == null) {
			mapping = (SystemFileTransferModeMapping)(typeModeMappings.get(getMappingKey(new SystemFileTransferModeMapping(extension))));
		}

		if (mapping == null) {
			return null;
		}


		SystemFileTransferModeMapping fileMapping = new SystemFileTransferModeMapping(name, extension);

		if (mapping.isText())
		{
			fileMapping.setAsText();
		}
		else if (mapping.isXML())
		{
			fileMapping.setAsXML();
		}
		else 
		{
			fileMapping.setAsBinary();
		}


		return fileMapping;
	}

	/**
	 * Return whether to automatically detect, use binary or text during file transfer
	 * for unspecified file types
	 */
	public static int getFileTransferModeDefaultPreference()
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getInt(ISystemFilePreferencesConstants.FILETRANSFERMODEDEFAULT);
	}

	/**
	 * Get a default mapping given an extension. Should never return null.
	 */
	private SystemFileTransferModeMapping getDefaultMapping(String fileName) {

		// now we check if the file has an extension
		// DY int extIndex = fileName.indexOf('.');
		int extIndex = fileName.lastIndexOf('.');
		String name, extension;

		// if there is no extension
		// DY 04-23-2002 changed from default binary to default text for files that
		//		a)  Have no extension
		//		b)  Ends with a period
		//		c)  Start with a '.' i.e. .bash_history
		if ((extIndex == -1) || (extIndex == (fileName.length() - 1)) || (extIndex == 0))
		{
			name = fileName;
			extension = null;
		}
		else
		{
			name = fileName.substring(0, extIndex);
			extension = fileName.substring(extIndex + 1);
		}


		SystemFileTransferModeMapping mapping = new SystemFileTransferModeMapping(name, extension);

		// default
		int defaultFileTransferMode = getFileTransferModeDefaultPreference();

		if (defaultFileTransferMode == ISystemFilePreferencesConstants.FILETRANSFERMODE_BINARY)
		{
			mapping.setAsBinary();
		}
		else if (defaultFileTransferMode == ISystemFilePreferencesConstants.FILETRANSFERMODE_TEXT)
		{
			mapping.setAsText();
		}

		return mapping;
	}


	/**
	 * Load the saved associations to the registry
	 * 
	 * @return true if operation successful, false otherwise
	 */
	public boolean loadAssociations() {

		typeModeMappings = new HashMap();

		String location = getFileLocation();

		File file = new File(location);

		if (!file.exists())
			return false;

		FileInputStream stream = null;
		InputStreamReader reader = null;

		boolean result = false;

		try {
			stream = new FileInputStream(file);
			reader = new InputStreamReader(stream, ENCODING);
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			IMemento[] mementos = memento.getChildren(INFO_NODE);

			for (int i = 0; i < mementos.length; i++) {
				String name = mementos[i].getString(NAME_ATTRIBUTE);
				String extension = mementos[i].getString(EXTENSION_ATTRIBUTE);
				String mode = mementos[i].getString(MODE_ATTRIBUTE);


				SystemFileTransferModeMapping mapping = new SystemFileTransferModeMapping(name, extension);

				if (mode.equals(TEXT_VALUE)) {
					mapping.setAsText();
				}
				else if (mode.equals(XML_VALUE)){
					mapping.setAsXML();
				}
				else {
					mapping.setAsBinary();
				}

				try
				{
					Integer priorityInt = mementos[i].getInteger(PRIORITY_ATTRIBUTE);
					if (priorityInt != null){
						int priority = priorityInt.intValue();
						mapping.setPriority(priority);
					}
				}
				catch (Exception e)
				{
				}


				typeModeMappings.put(getMappingKey(mapping), mapping);
			}

			result = true;
		}
		catch (Exception e) {
			SystemBasePlugin.logError("Could not read transfer mode xml file", e); //$NON-NLS-1$
			result = false;
		}
		finally {

			try {

				if (reader != null)
					reader.close();
			}
			catch (Exception e) {
				SystemBasePlugin.logError("Could not close reader for transfer mode xml file", e); //$NON-NLS-1$
			}
		}


		return result;
	}

	private void deleteAssociations()
	{
		String location = getFileLocation();
		File assFile = new File(location);
		assFile.delete();
	}

	/**
	 * Save the contents of the registry
	 */
	public void saveAssociations() {

		String location = getFileLocation();

		XMLMemento memento = XMLMemento.createWriteRoot(ROOT_NODE);

		Iterator iter = typeModeMappings.values().iterator();

		while (iter.hasNext()) {
			ISystemFileTransferModeMapping mapping = (ISystemFileTransferModeMapping)iter.next();
			IMemento infoMemento = memento.createChild(INFO_NODE);
			infoMemento.putString(NAME_ATTRIBUTE, mapping.getName());
			infoMemento.putString(EXTENSION_ATTRIBUTE, mapping.getExtension());
			
			if (mapping.isText()){
				infoMemento.putString(MODE_ATTRIBUTE, TEXT_VALUE);
			}
			else if (mapping.isXML()){
				infoMemento.putString(MODE_ATTRIBUTE, XML_VALUE);
			}
			else {
				infoMemento.putString(MODE_ATTRIBUTE, BINARY_VALUE);
			}

			infoMemento.putInteger(PRIORITY_ATTRIBUTE, mapping.getPriority());
		}

		FileOutputStream stream = null;
		OutputStreamWriter writer = null;

		try {
			stream = new FileOutputStream(location);
			writer = new OutputStreamWriter(stream, ENCODING);
			memento.save(writer);
		}
		catch (Exception e) {
			SystemBasePlugin.logError("Could not write to transfer mode xml file", e); //$NON-NLS-1$
		}
		finally {

			try {

				if (writer != null)
					writer.close();
			}
			catch (Exception e) {
				SystemBasePlugin.logError("Could not close writer for transfer mode xml file", e); //$NON-NLS-1$
			}
		}
	}


	/**
	 * Get the file location
	 */
	private String getFileLocation() {
		return plugin.getStateLocation().append(FILENAME).toOSString();
	}
}
