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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.ICLanguage;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of the file type resolver interface.
 */
public class ResolverModel implements IResolverModel {

	/**
	 * Name used to describe an unknown language or  file type
	 */
	public static final String NAME_UNKNOWN	= "Unknown";

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

	// The language map holds a map of language IDs to descriptive strings.
	private Map fLangMap = new HashMap();

	// The type map holds a map of file type IDs to file types.
	private Map fTypeMap = new HashMap();

	// Workspace resolver
	private ICFileTypeResolver fWkspResolver = null;

	// XML tag names, etc.
	private static final String EXTENSION_LANG	= "CLanguage"; //$NON-NLS-1$
	private static final String EXTENSION_TYPE	= "CFileType"; //$NON-NLS-1$
	private static final String EXTENSION_ASSOC = "CFileTypeAssociation"; //$NON-NLS-1$
	private static final String TAG_CUSTOM 		= "custom"; //$NON-NLS-1$
	private static final String TAG_ASSOC 		= "associations"; //$NON-NLS-1$
	private static final String TAG_ENTRY 		= "entry"; //$NON-NLS-1$
	private static final String ATTR_TYPE 		= "type"; //$NON-NLS-1$
	private static final String ATTR_PATTERN 	= "pattern"; //$NON-NLS-1$
	private static final String ATTR_FILE 		= "file"; //$NON-NLS-1$
	private static final String ATTR_ID	 		= "id"; //$NON-NLS-1$
	private static final String ATTR_LANGUAGE	= "language"; //$NON-NLS-1$
	private static final String ATTR_NAME 		= "name"; //$NON-NLS-1$
	private static final String ATTR_VAL_SOURCE = "source"; //$NON-NLS-1$
	private static final String ATTR_VAL_HEADER = "header"; //$NON-NLS-1$
	private static final String ATTR_VALUE 		= "value"; //$NON-NLS-1$
	private static final String WKSP_STATE_FILE	= "resolver.properties"; //$NON-NLS-1$
	private static final String CDT_RESOLVER 	= "cdt_resolver"; //$NON-NLS-1$
	
	// Trace flag
	public static boolean VERBOSE = false;
	
	// Singleton
	private static ResolverModel fInstance = null;

	// Qualified names used to identify project session properties
	public static final String			RESOLVER_MODEL_ID = CCorePlugin.PLUGIN_ID + ".resolver"; //$NON-NLS-1$
	public static final QualifiedName	QN_CUSTOM_RESOLVER = new QualifiedName(RESOLVER_MODEL_ID, TAG_CUSTOM);
	
	// Private ctor to preserve singleton status
	private ResolverModel() {
		loadDeclaredLanguages();
		loadDeclaredTypes();
	}

	/**
	 * @return the default instance of this singleton
	 */
	synchronized public static ResolverModel getDefault() {
		if (null == fInstance) {
			fInstance = new ResolverModel();
		}
		return fInstance;
	}

	public ICLanguage[] getLanguages() {
		Collection values = fLangMap.values();
		return (ICLanguage[]) values.toArray(new ICLanguage[values.size()]);
	}
	
	public ICFileType[] getFileTypes() {
		Collection values = fTypeMap.values();
		return (ICFileType[]) values.toArray(new ICFileType[values.size()]);
	}

	public ICLanguage getLanguageById(String languageId) {
		ICLanguage lang = (ICLanguage) fLangMap.get(languageId);
		return ((null != lang) ? lang : DEFAULT_LANG_TYPE);
	}

	public ICFileType getFileTypeById(String typeId) {
		ICFileType type = (ICFileType) fTypeMap.get(typeId);
		return ((null != type) ? type : DEFAULT_FILE_TYPE);
	}

	synchronized public void setResolver(ICFileTypeResolver resolver) {
		fWkspResolver = resolver;
		saveWorkspaceResolver(resolver);
	}
	
	synchronized public ICFileTypeResolver getResolver() {
		if (null == fWkspResolver) {
			fWkspResolver = internalGetWorkspaceResolver();
		}
		return fWkspResolver;
	}

	public void setResolver(IProject project, ICFileTypeResolver resolver) {
		setResolverForSession(project, resolver);
		saveProjectResolver(project, resolver);
	}

	public ICFileTypeResolver getResolver(IProject project) {
		ICFileTypeResolver resolver = null;
		
		if (null == project) {
			resolver = getResolver();
		} else {
			resolver = getResolverForSession(project);
			if (null == resolver) {
				resolver = internalGetProjectResolver(project);
			}
		}
		
		return resolver;
	}

	private ICFileTypeResolver internalGetWorkspaceResolver() {
		ICFileTypeResolver resolver = null;
		if (customWorkspaceResolverExists()) {
			resolver = loadWorkspaceResolver();
		} else {
			resolver = loadResolverDefaults();
		}
		return resolver;
	}

	private ICFileTypeResolver internalGetProjectResolver(IProject project) {
		ICFileTypeResolver resolver = null;
		if (customProjectResolverExists(project)) {
			resolver = loadProjectResolver(project);
		} else {
			resolver = internalGetWorkspaceResolver();
		}
		return resolver;
	}
	
	public ICFileTypeResolver createResolver() {
		return new CFileTypeResolver();
	}
	
	public ICFileTypeAssociation createAssocation(String pattern, ICFileType type) {
		return new CFileTypeAssociation(pattern, type);
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
		if (isDebugging()) {
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
		if (isDebugging()) {
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
	 * Remove a language from the list of languages known to the resolver.
	 * 
	 * @param lang language to remove
	 * 
	 * @return true if the language is removed, false otherwise
	 */
	public boolean removeLanguage(ICLanguage lang) {
		if (isDebugging()) {
			debugLog("- language " + lang.getId() + " as " + lang.getName());
		}
		boolean removed = (null != fLangMap.remove(lang.getId()));
		
		if (removed) {
			ArrayList removeList = new ArrayList();
			for (Iterator iter = fTypeMap.values().iterator(); iter.hasNext();) {
				ICFileType type = (ICFileType) iter.next();
				if (lang.equals(type.getLanguage())) {
					removeList.add(type); 
				}
			}
			for (Iterator iter = removeList.iterator(); iter.hasNext();) {
				removeFileType((ICFileType) iter.next());
			}
		}
		return removed;
	}
	
	/**
	 * Remove a file type from the list of type known to the resolver.
	 * 
	 * @param type file type to remove
	 * 
	 * @return true if the file type is removed, false otherwise
	 */
	public boolean removeFileType(ICFileType type) {
		if (isDebugging()) {
			debugLog("- type " + type.getId() + " as " + type.getName());
		}
		// TODO: must remove any associations based on this file type as well
		// Unforuntately, at this point, that means iterating over the contents
		// of the default, workspace, and project resolvers.  Ugh.
		return (null != fTypeMap.remove(type.getId()));
	}
	
	/**
	 * Load languages declared through the CLanguage extension point.
	 */
	private void loadDeclaredLanguages() {
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
	private void loadDeclaredTypes() {
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
	 * Get the resolver cached in the project session properties,
	 * if available.
	 *  
	 * @param project project to query
	 * 
	 * @return cached file type resolver, or null
	 */
	private ICFileTypeResolver getResolverForSession(IProject project) {
		ICFileTypeResolver resolver = null;
		
		try {
			Object obj = project.getSessionProperty(QN_CUSTOM_RESOLVER);
			if (obj instanceof ICFileTypeResolver) {
				resolver = (ICFileTypeResolver) obj;
			}
		} catch (CoreException e) {
		}
	
		return resolver;
	}

	/**
	 * Store the currently active resolver in the project session
	 * properties.
	 * 
	 * @param project project to set resolver for
	 * @param resolver file type resolver to cache
	 */
	private void setResolverForSession(IProject project, ICFileTypeResolver resolver) {
		if (null != project) {
			try {
				project.setSessionProperty(QN_CUSTOM_RESOLVER, resolver);
			} catch (CoreException e) {
			}
		}
	}

	private static boolean isDebugging() {
		return VERBOSE;
	}
	
	private void debugLog(String message) {
		System.out.println("CDT Resolver: " + message);
	}

	private IExtensionPoint getExtensionPoint(String extensionPointId) {
        return Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, extensionPointId);
	}

	//----------------------------------------------------------------------
	// Default resolver
	//----------------------------------------------------------------------
	
	/**
	 * Initialize the default resolver by loading data from
	 * declared extension points.
	 */
	private ICFileTypeResolver loadResolverDefaults() {
		ICFileTypeResolver		resolver	= new CFileTypeResolver();
		IExtensionPoint			point		= getExtensionPoint(EXTENSION_ASSOC);
		IExtension[]			extensions	= point.getExtensions();
		IConfigurationElement[]	elements	= null;
		ResolverModel			model		= ResolverModel.getDefault();
		
		for (int i = 0; i < extensions.length; i++) {
			elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				ICFileType typeRef = model.getFileTypeById(elements[j].getAttribute(ATTR_TYPE));
				if (null != typeRef) {
					addAssocFromPattern(resolver, typeRef, elements[j]);
					addAssocFromFile(resolver, typeRef, elements[j]);
				}
			}
		}
		
		return resolver;
	}
	
	/**
	 * Associate one or more file extensions with an ICFileType instance.
	 * 
	 * @param typeRef reference to the ICFileType instance
	 * 
	 * @param element configuration element to get file extensions from
	 */
	private void addAssocFromPattern(ICFileTypeResolver resolver, ICFileType typeRef, IConfigurationElement element) {
		String attr = element.getAttribute(ATTR_PATTERN);
		if (null != attr) {
			String[] item = attr.split(",");
			for (int i = 0; i < item.length; i++) {
				try {
					resolver.addAssociation(item[i].trim(), typeRef);
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
	private void addAssocFromFile(ICFileTypeResolver resolver, ICFileType typeRef, IConfigurationElement element) {
		String attr = element.getAttribute(ATTR_FILE);
		
		if (null != attr) {
			URL 			baseURL	= null;
			URL				fileURL = null;
			BufferedReader	in		= null;
			String			line	= null;
			
		    try {
		    	baseURL =  Platform.getBundle(element.getDeclaringExtension().getNamespace()).getEntry("/"); //$NON-NLS-1$
				fileURL = new URL(baseURL, attr);
				in		= new BufferedReader(new InputStreamReader(fileURL.openStream()));
		        line	= in.readLine();
		        while (null != line) {
		        	try {
		        		resolver.addAssociation(line, typeRef);
					} catch (IllegalArgumentException e) {
						CCorePlugin.log(e);
					}
		        	line = in.readLine();
		        }
		        in.close();
		    } catch (IOException e) {
		    	CCorePlugin.log(e);
		    }
		}
	}
	
	//----------------------------------------------------------------------
	// Workspace resolver
	//----------------------------------------------------------------------
	
	public boolean customWorkspaceResolverExists() {
		return getWorkspaceResolverStateFilePath().toFile().exists();
	}

	private IPath getWorkspaceResolverStateFilePath() {
		return CCorePlugin.getDefault().getStateLocation().append(WKSP_STATE_FILE);
	}
	
	private ICFileTypeResolver loadWorkspaceResolver() {
		ICFileTypeResolver	resolver	= null;
		File				file		= getWorkspaceResolverStateFilePath().toFile();
		
		if (file.exists()) {
			Properties 		props	= new Properties();
			ResolverModel	model	= ResolverModel.getDefault();
			FileInputStream	in		= null;

			resolver = new CFileTypeResolver();
			
			try {
		    	in = new FileInputStream(file);
				
		    	props.load(in);
		
		    	for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
		    		Map.Entry	element = (Map.Entry) iter.next();
					ICFileType	type	= model.getFileTypeById(element.getValue().toString());
        			resolver.addAssociation(element.getKey().toString(), type);
				}
				
				in.close();
		    } catch (IOException e) {
		    	CCorePlugin.log(e);
		    }
		
		    if (null != in) {
		    	in = null;
		    }
		}
		
		return resolver;
	}
	
	public void saveWorkspaceResolver(ICFileTypeResolver resolver) {
		File			file	= getWorkspaceResolverStateFilePath().toFile();
		BufferedWriter	out		= null;
		
		try {
			if (null == resolver) {
				file.delete();
			} else {
				out = new BufferedWriter(new FileWriter(file));
				
				ICFileTypeAssociation[] assoc = resolver.getFileTypeAssociations();
				
				for (int i = 0; i < assoc.length; i++) {
					out.write(assoc[i].getPattern() + '=' + assoc[i].getType().getId() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (null != out) {
			out = null;
		}
	}
	
	//----------------------------------------------------------------------
	// Project resolver
	//----------------------------------------------------------------------

	public boolean customProjectResolverExists(IProject project) {
		Element	data	= getProjectData(project);
		Node	child	= ((null != data) ? data.getFirstChild() : null);
		Boolean custom	= new Boolean(false);
		
		while (child != null) {
			if (child.getNodeName().equals(TAG_CUSTOM)) { 
				custom = Boolean.valueOf(((Element)child).getAttribute(ATTR_VALUE));
			}
			child = child.getNextSibling();
		}
		
		return custom.booleanValue();
	}
	
	private ICDescriptor getProjectDescriptor(IProject project) throws CoreException {
		ICDescriptor descriptor = null;
		descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
		return descriptor;
	}
	
	private Element getProjectData(IProject project) {
		Element data = null;
		try {
			data = getProjectDescriptor(project).getProjectData(CDT_RESOLVER);
		} catch (CoreException e) {
		}
		return data;
	}
	
	public ICFileTypeResolver loadProjectResolver(IProject project) {
		ICFileTypeResolver	resolver 	= new CFileTypeResolver();
		ResolverModel		model 		= ResolverModel.getDefault();
		Element				data		= getProjectData(project);
		Node				child 		= ((null != data) ? data.getFirstChild() : null);
		
		while (child != null) {
			if (child.getNodeName().equals(TAG_ASSOC)) {
				Node assoc = child.getFirstChild();
				while (assoc != null) {
					if (assoc.getNodeName().equals(TAG_ENTRY)) {
						Element element	= (Element) assoc;
						String	pattern	= element.getAttribute(ATTR_PATTERN);
						String 	typeId	= element.getAttribute(ATTR_TYPE);
						resolver.addAssociation(pattern, model.getFileTypeById(typeId));
					}
					assoc = assoc.getNextSibling();
				}
			}
			child = child.getNextSibling();
		}
		
		return resolver;
	}

	public void saveProjectResolver(IProject project, ICFileTypeResolver resolver) {
		//ResolverModel	model	= ResolverModel.getDefault();
		Element			root	= getProjectData(project);
		Document 		doc 	= root.getOwnerDocument();
		Node			child	= root.getFirstChild();
		Element			element	= null;
		
		while (child != null) {
			root.removeChild(child);
			child = root.getFirstChild();
		}
		
		element = doc.createElement(TAG_CUSTOM);
		element.setAttribute(ATTR_VALUE, new Boolean(null != resolver).toString());
		root.appendChild(element);

		if (null != resolver) {
			element = doc.createElement(TAG_ASSOC);
			root.appendChild(element);
	
			root = element; // Note that root changes...
			
			ICFileTypeAssociation[] assoc = resolver.getFileTypeAssociations();
	
			for (int i = 0; i < assoc.length; i++) {
				element = doc.createElement(TAG_ENTRY);
				element.setAttribute(ATTR_PATTERN, assoc[i].getPattern());
				element.setAttribute(ATTR_TYPE, assoc[i].getType().getId());
				root.appendChild(element);
			}
		}
		
		try {
			getProjectDescriptor(project).saveProjectData();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	
}
