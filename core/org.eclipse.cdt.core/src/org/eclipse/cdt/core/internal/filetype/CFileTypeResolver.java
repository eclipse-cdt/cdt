/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.internal.filetype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.ICLanguage;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Implementation of the file type resolver interface.
 */
public class CFileTypeResolver implements ICFileTypeResolver {

	public static boolean VERBOSE = false;
	
	private static final String EXTENSION_LANG	= "CLanguage"; //$NON-NLS-1$
	private static final String EXTENSION_TYPE	= "CFileType"; //$NON-NLS-1$
	private static final String EXTENSION_ASSOC = "CFileTypeAssociation"; //$NON-NLS-1$
	private static final String ATTR_ID	 		= "id"; //$NON-NLS-1$
	private static final String ATTR_LANGUAGE	= "language"; //$NON-NLS-1$
	private static final String ATTR_NAME 		= "name"; //$NON-NLS-1$
	private static final String ATTR_TYPE 		= "type"; //$NON-NLS-1$
	private static final String ATTR_EXT  		= "pattern"; //$NON-NLS-1$
	private static final String ATTR_FILE 		= "file"; //$NON-NLS-1$
	private static final String ATTR_VAL_SOURCE = "source"; //$NON-NLS-1$
	private static final String ATTR_VAL_HEADER = "header"; //$NON-NLS-1$
	
	private static final String NAME_UNKNOWN	= "Unknown";
	
	/**
	 * Default language, returned when no other language matches a language id.
	 */
	public static final ICLanguage DEFAULT_LANG_TYPE =
		new CLanguage(ICFileTypeConstants.LANG_UNKNOWN, NAME_UNKNOWN);	

	/**
	 * Default file type, returned when no other file type matches a file name.
	 */
	public static final ICFileType DEFAULT_FILE_TYPE =
		new CFileType(ICFileTypeConstants.FT_UNKNOWN, DEFAULT_LANG_TYPE, NAME_UNKNOWN, ICFileType.TYPE_UNKNOWN);	

	// Singleton
	private static ICFileTypeResolver instance = null;

	// Private ctor to preserve singleton status
	private CFileTypeResolver() {
		loadLanguages();
		loadTypes();
		loadAssociations();
	}

	/**
	 * @return the default instance of this singleton
	 */
	synchronized public static ICFileTypeResolver getDefault() {
		if (null == instance) {
			instance = new CFileTypeResolver();
		}
		return instance;
	}
	
	/**
	 * The language map holds a map of language IDs to descriptive strings.
	 */
	private Map fLangMap = new HashMap();

	/**
	 * The type map holds a map of file type IDs to file types.
	 */
	private Map fTypeMap = new HashMap();
	
	/**
	 * The association list holds a list of known file associations.
	 */
	private List fAssocList	= new ArrayList();

	/**
	 * Get the file type assocated with the specified file name.
	 * Returns DEFAULT_FILE_TYPE if the file name could not be
	 * resolved to a particular file type.
	 * 
	 * @param fileName name of the file to resolve
	 * 
	 * @return associated file type, or DEFAULT_FILE_TYPE
	 */
	public ICFileType getFileType(String fileName) {
		for (Iterator iter = fAssocList.iterator(); iter.hasNext();) {
			ICFileTypeAssociation element = (ICFileTypeAssociation) iter.next();
			if (element.matches(fileName)) {
				return element.getType();
			}
		}
		return DEFAULT_FILE_TYPE;
	}

	/**
	 * Get the file type that has the specified id.
	 * Returns null if no file type has that id.
	 * 
	 * @param typeId file type id
	 * 
	 * @return file type with the specified id, or null
	 */
	public ICFileType getFileTypeById(String typeId) {
		ICFileType type = (ICFileType) fTypeMap.get(typeId);
		return ((null != type) ? type : DEFAULT_FILE_TYPE);
	}

	/**
	 * Get the language that has the specified id.
	 * Returns null if no language has that id.
	 * 
	 * @param languageId language id
	 * 
	 * @return language with the specified id, or null
	 */
	public ICLanguage getLanguageById(String languageId) {
		ICLanguage lang = (ICLanguage) fLangMap.get(languageId);
		return ((null != lang) ? lang : DEFAULT_LANG_TYPE);
	}


	/**
	 * @return the languages known to the resolver
	 */
	public ICLanguage[] getLanguages() {
		Collection values = fLangMap.values();
		return (ICLanguage[]) values.toArray(new ICLanguage[values.size()]);
	}

	/**
	 * @return the file types known to the resolver
	 */
	public ICFileType[] getFileTypes() {
		Collection values = fTypeMap.values();
		return (ICFileType[]) values.toArray(new ICFileType[values.size()]);
	}

	/**
	 * @return the file type associations known to the resolver
	 */
	public ICFileTypeAssociation[] getFileTypeAssociations() {
		return (ICFileTypeAssociation[]) fAssocList.toArray(new ICFileTypeAssociation[fAssocList.size()]);
	}

	/**
	 * Add an instance of a language to the languages known to the
	 * resolver.
	 * 
	 * Returns true if the instance is added; returns false if the
	 * instance is not added, or if it is already present in the list.
	 * 
	 * @param lang language instance to add
	 * 
	 * @return true if the language is added, false otherwise
	 */
	public boolean addLanguage(ICLanguage lang) {
		if (VERBOSE) {
			debugLog("+ language " + lang.getId() + " as " + lang.getName());
		}
		boolean added = false;
		if (!fLangMap.containsValue(lang)) {
			fLangMap.put(lang.getId(), lang);
			added = true;
		}
		return added;
	}

	/**
	 * Remove a language from the list of languages known to the resolver.
	 * 
	 * @param lang language to remove
	 * 
	 * @return true if the language is removed, false otherwise
	 */
	public boolean removeLanguage(ICLanguage lang) {
		if (VERBOSE) {
			debugLog("- language " + lang.getId() + " as " + lang.getName());
		}
		// TODO: must remove any file types based on this language as well
		return (null != fLangMap.remove(lang.getId()));
	}

	/**
	 * Add an instance of a file type to the file types known to the
	 * resolver.
	 * 
	 * Returns true if the instance is added; returns false if the
	 * instance is not added, or if it is already present in the list.
	 * 
	 * @param type file type to add
	 * 
	 * @return true if the file type is added, false otherwise
	 */
	public boolean addFileType(ICFileType type) {
		if (VERBOSE) {
			debugLog("+ type " + type.getId() + " as " + type.getName());
		}
		boolean added = false;
		if (!fTypeMap.containsValue(type)) {
			fTypeMap.put(type.getId(), type);
			added = true;
		}
		return added;
	}

	/**
	 * Remove a file type from the list of type known to the resolver.
	 * 
	 * @param type file type to remove
	 * 
	 * @return true if the file type is removed, false otherwise
	 */
	public boolean removeFileType(ICFileType type) {
		if (VERBOSE) {
			debugLog("- type " + type.getId() + " as " + type.getName());
		}
		// TODO: must remove any associations based on this file type as well
		return (null != fTypeMap.remove(type.getId()));
	}

	/**
	 * Add an instance of a file type association to the associations
	 * known to the resolver.
	 * 
	 * Returns true if the instance is added; returns false if the
	 * instance is not added, or if it is already present in the list.
	 * 
	 * @param assoc association to add
	 * 
	 * @return true if the association is added, false otherwise
	 */
	public boolean addFileTypeAssociation(ICFileTypeAssociation assoc) {
		if (VERBOSE) {
			debugLog("+ association " + assoc.getPattern() + " as " + assoc.getType().getId());
		}
		boolean added = false;
		if (!fAssocList.contains(assoc)) {
			added = fAssocList.add(assoc);
		}
		return added;
	}

	public boolean removeFileTypeAssociation(ICFileTypeAssociation assoc) {
		if (VERBOSE) {
			debugLog("- association " + assoc.getPattern() + " as " + assoc.getType().getId());
		}
		return fAssocList.remove(assoc);
	}

	/**
	 * Load languages declared through the CLanguage extension point.
	 */
	private void loadLanguages() {
		IExtensionPoint			point 		= getExtensionPoint(EXTENSION_LANG);
		IExtension[]			extensions 	= point.getExtensions();
		IConfigurationElement[]	elements 	= null;

		for (int i = 0; i < extensions.length; i++) {
			elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				String id   = elements[j].getAttribute(ATTR_ID);
				String name = elements[j].getAttribute(ATTR_NAME);
 
				try {
					addLanguage(new CLanguage(id, name));
				} catch (IllegalArgumentException e) {
					CCorePlugin.log(e);
				}
			}
		}
		
	}
	
	/**
	 * Load file type declared through the CFileType extension point.
	 */
	private void loadTypes() {
		IExtensionPoint			point 		= getExtensionPoint(EXTENSION_TYPE);
		IExtension[]			extensions 	= point.getExtensions();
		IConfigurationElement[]	elements 	= null;
		
		for (int i = 0; i < extensions.length; i++) {
			elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				String	id	 = elements[j].getAttribute(ATTR_ID);
				String	lang = elements[j].getAttribute(ATTR_LANGUAGE);
				String	name = elements[j].getAttribute(ATTR_NAME);
				String	type = elements[j].getAttribute(ATTR_TYPE);
				
				try {
					addFileType(new CFileType(id, getLanguageById(lang), name, parseType(type)));
				} catch (IllegalArgumentException e) {
					CCorePlugin.log(e);
				}
			
			}
		}
	}

	/**
	 * Load file type associations declared through the CFileTypeAssociation
	 * extension point.
	 */
	private void loadAssociations() {
		IExtensionPoint			point		= getExtensionPoint(EXTENSION_ASSOC);
		IExtension[]			extensions	= point.getExtensions();
		IConfigurationElement[]	elements	= null;
		//ICFileTypeAssociation[]	assocs		= null;
		
		for (int i = 0; i < extensions.length; i++) {
			elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				ICFileType typeRef = getFileTypeById(elements[j].getAttribute(ATTR_TYPE));
				if (null != typeRef) {
					addAssocFromPattern(typeRef, elements[j]);
					addAssocFromFile(typeRef, elements[j]);
				}
			}
		}
	}

	/**
	 * Convenience method for getting an IExtensionPoint instance.
	 * 
	 * @see org.eclipse.core.runtime.IPluginDescriptor#getExtensionPoint(String)
	 * 
	 * @param extensionPointId the simple identifier of the extension point
	 * 
	 * @return the extension point, or null
	 */
	private IExtensionPoint getExtensionPoint(String extensionPointId) {
        return Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, extensionPointId);
	}

	/**
	 * Turn a type string into an ICFileType.TYPE_* value. 
	 * 
	 * @param typeString type string ("source" or "header")
	 * 
	 * @return corresponding ICFileType.TYPE_* value, or ICFileType.UNKNOWN 
	 */
	private int parseType(String typeString) {
		int type = ICFileType.TYPE_UNKNOWN;

		if (typeString.equals(ATTR_VAL_SOURCE)) {
			type = ICFileType.TYPE_SOURCE;
		} else if (typeString.equals(ATTR_VAL_HEADER)) {
			type = ICFileType.TYPE_HEADER;
		}

		return type;
	}
	
	/**
	 * Associate one or more file extensions with an ICFileType instance.
	 * 
	 * @param typeRef reference to the ICFileType instance
	 * 
	 * @param element configuration element to get file extensions from
	 */
	private void addAssocFromPattern(ICFileType typeRef, IConfigurationElement element) {
		String attr = element.getAttribute(ATTR_EXT);
		if (null != attr) {
			String[] item = attr.split(",");
			for (int i = 0; i < item.length; i++) {
				try {
					addFileTypeAssociation(new CFileTypeAssociation(item[i].trim(), typeRef));
				} catch (IllegalArgumentException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

	/**
	 * Associate the contents of a file with an ICFileType instance.
	 * 
	 * The file is read, one entry per line; each line is taken as
	 * a pattern that should be associated with the specified ICFileType
	 * instance.
	 * 
	 * @param typeRef reference to the ICFileType instance
	 * 
	 * @param element configuration element to get file extensions from
	 */
	private void addAssocFromFile(ICFileType typeRef, IConfigurationElement element) {
		String attr = element.getAttribute(ATTR_FILE);
		
		if (null != attr) {
			URL 			baseURL	= null;
			URL				fileURL = null;
			BufferedReader	in		= null;
			String			line	= null;
			
		    try {
				//baseURL = element.getDeclaringExtension().getDeclaringPluginDescriptor().getInstallURL();
		    	baseURL =  Platform.getBundle(element.getDeclaringExtension().getNamespace()).getEntry("/"); //$NON-NLS-1$
				fileURL = new URL(baseURL, attr);
				in		= new BufferedReader(new InputStreamReader(fileURL.openStream()));
		        line	= in.readLine();
		        while (null != line) {
		        	try {
			        	addFileTypeAssociation(new CFileTypeAssociation(line, typeRef));
					} catch (IllegalArgumentException e) {
						CCorePlugin.log(e);
					}
		        	line = in.readLine();
		        }
		        in.close();
		    } catch (IOException e) {
		    }
		}
	}
	
	private void debugLog(String message) {
		System.out.println("CDT Resolver: " + message);
	}
}
