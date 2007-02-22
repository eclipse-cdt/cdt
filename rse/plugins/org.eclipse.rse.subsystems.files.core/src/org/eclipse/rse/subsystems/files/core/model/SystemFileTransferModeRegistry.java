/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;

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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;



/**
 * An internal class. Clients must not instantiate or subclass it.
 */

public class SystemFileTransferModeRegistry  
	implements ISystemFileTransferModeRegistry, IPropertyListener {




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
	public static SystemFileTransferModeRegistry getDefault() {
		
		if (instance == null) {
			instance = new SystemFileTransferModeRegistry();
		}
		
		return instance;
	}
	
	
	/**
	 * Initialize the registry from storage.
	 */
	private void initialize() {
		
		// load our current associations (if any)
		loadAssociations();
		
		// now we need to ensure that our mapping is in sync with the
		// editor registry. We can be out of sync because we may not have
		// been listening for editor registry changes (e.g. if our plugin wasn't
		// started while those changes were made).
		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb != null)
		{
			IEditorRegistry registry = wb.getEditorRegistry();
			syncWithEditorRegistry(registry);
		
			registry.addPropertyListener(this);
		}
	}
	
	
	/**
	 * Listen for changes to the Editor Registry content.
	 * Update our registry by changing the hashmap and saving the new
	 * mappings on disk.
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
	
		if ((source instanceof IEditorRegistry) && (propId == IEditorRegistry.PROP_CONTENTS)) {
			IEditorRegistry registry = (IEditorRegistry)source;
			syncWithEditorRegistry(registry);
		}
	}
	
	
	/**
	 * Ensures that our registry is in sync with the editor registry, e.g.
	 * we have exactly the same types as in the editor registry. We can be
	 * out of sync if changes are made to the registry while we are not listening.
	 */
	private void syncWithEditorRegistry(IEditorRegistry registry) {
		
		IFileEditorMapping[] editorMappings = registry.getFileEditorMappings();
		
		SystemFileTransferModeMapping[] modeMappings = new SystemFileTransferModeMapping[editorMappings.length];
		
		for (int i = 0; i < editorMappings.length; i++) {
			modeMappings[i] = getMapping(editorMappings[i]);
		}
 
		setModeMappings(modeMappings);		// set new mappings
		saveAssociations();					// now save associations
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
	 * Get a mode mapping given a file editor mapping
	 */
	public SystemFileTransferModeMapping getMapping(IFileEditorMapping editorMapping) {
		return getMapping(editorMapping.getLabel());
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

		
		// if there is no extension
		if ((extIndex == -1) || (extIndex == (fileName.length() - 1))) {
			return null;
		}
		
		
		String name = fileName.substring(0, extIndex);
		String extension = fileName.substring(extIndex + 1);		
		
		SystemFileTransferModeMapping mapping = (SystemFileTransferModeMapping)(typeModeMappings.get(getMappingKey(new SystemFileTransferModeMapping(extension))));
		
		if (mapping == null)
			return null;
			
		
		SystemFileTransferModeMapping fileMapping = new SystemFileTransferModeMapping(name, extension);
		
		if (mapping.isText())
		{
			fileMapping.setAsText();
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
	
	
		// check if it's a default text file name
		for (int i = 0; i < DEFAULT_TEXT_FILE_NAMES.length; i++) 
		{			
			if (fileName.equalsIgnoreCase(DEFAULT_TEXT_FILE_NAMES[i])) 
			{				
				mapping.setAsText();
				return mapping;
			}
		}
			
		// DKM
		// no longer default to text - instead we default based on preferences
		// after the if (extension != null), we pick up preferences for default
		/*
		if (extension == null)
		{		
			// DY mapping.setAsBinary();
			mapping.setAsText();
			return mapping;
		}			*/	
		

		
		if (extension != null)
		{
			// check if it's a default text file extension
			for (int i = 0; i < DEFAULT_TEXT_FILE_EXTENSIONS.length; i++) 
			{			
				if (extension.equalsIgnoreCase(DEFAULT_TEXT_FILE_EXTENSIONS[i])) 
				{
					mapping.setAsText();
					return mapping;
				}
			}
			
			// check if it's a default LPEX text file extension
			for (int i = 0; i < DEFAULT_LPEX_TEXT_FILE_EXTENSIONS.length; i++) 
			{			
				if (extension.equalsIgnoreCase(DEFAULT_LPEX_TEXT_FILE_EXTENSIONS[i])) 
				{
					mapping.setAsText();
					return mapping;
				}
			}
			
			// check if it's a default iSeries LPEX text file extension
			for (int i = 0; i < DEFAULT_ISERIES_LPEX_TEXT_FILE_EXTENSIONS.length; i++) 
			{			
				if (extension.equalsIgnoreCase(DEFAULT_ISERIES_LPEX_TEXT_FILE_EXTENSIONS[i])) 
				{
					mapping.setAsText();
					return mapping;
				}
			}
			
			// check if it's a default Universal LPEX text file extension (Phil 10/16/2002)
			for (int i = 0; i < DEFAULT_UNIX_LPEX_TEXT_FILE_EXTENSIONS.length; i++) 
			{			
				if (extension.equalsIgnoreCase(DEFAULT_UNIX_LPEX_TEXT_FILE_EXTENSIONS[i])) 
				{
					mapping.setAsText();
					return mapping;
				}
			}				
						
			// check for known binary types
			for (int i = 0; i < DEFAULT_BINARY_FILE_EXTENSIONS.length; i++) 
			{			
				if (extension.equalsIgnoreCase(DEFAULT_BINARY_FILE_EXTENSIONS[i])) 
				{
					mapping.setAsBinary();
					return mapping;
				}
			}				
		}
		
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
				else {
					mapping.setAsBinary();
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
			infoMemento.putString(MODE_ATTRIBUTE, mapping.isBinary() ? BINARY_VALUE : TEXT_VALUE); 
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
