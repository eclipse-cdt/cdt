/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.ICLanguage;
import org.eclipse.cdt.core.filetype.IResolverChangeListener;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.cdt.core.filetype.ResolverChangeEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
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
	public static final String NAME_UNKNOWN	= "Unknown"; //$NON-NLS-1$

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

	// Default resolver
	private ICFileTypeResolver fDefaultResolver = null;

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
	private static final String			RESOLVER_MODEL_ID = CCorePlugin.PLUGIN_ID + ".resolver"; //$NON-NLS-1$
	private static final QualifiedName	QN_CUSTOM_RESOLVER = new QualifiedName(RESOLVER_MODEL_ID, TAG_CUSTOM);

	// List of listeners on the model
	private List fListeners = new Vector();
	
	// Private ctor to preserve singleton status
	private ResolverModel() {
		try {
			loadDeclaredLanguages();
			loadDeclaredTypes();
			
			fDefaultResolver = loadDefaultResolver();
			fWkspResolver	 = loadWorkspaceResolver();
			
			initRegistryChangeListener();
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
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

	public void setResolver(ICFileTypeResolver newResolver) {
		ICFileTypeResolver oldResolver = getResolver();
		fWkspResolver = newResolver;
		saveWorkspaceResolver(newResolver);
		fireResolverChangeEvent(null, oldResolver);
	}
	
	public ICFileTypeResolver getResolver() {
		return ((null != fWkspResolver) ? fWkspResolver : fDefaultResolver) ;
	}

	public void setResolver(IProject project, ICFileTypeResolver newResolver) {
		ICFileTypeResolver oldResolver = getResolver(project);
		try {
			project.setSessionProperty(QN_CUSTOM_RESOLVER, newResolver);
		} catch (CoreException e) {
		}
		saveProjectResolver(project, newResolver);
		fireResolverChangeEvent(project, oldResolver);
	}

	public ICFileTypeResolver getResolver(IProject project) {
		ICFileTypeResolver resolver = null;

		if (null != project) {
			try {
				Object obj = project.getSessionProperty(QN_CUSTOM_RESOLVER);
				if (obj instanceof ICFileTypeResolver) {
					resolver = (ICFileTypeResolver) obj;
				}
			} catch (CoreException e) {
			}
			if (null == resolver) {
				if (customProjectResolverExists(project)) {
					resolver = loadProjectResolver(project);
				}
			}
		}
		
		if (null == resolver) {
			resolver = getResolver();
		}
		
		return resolver;
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
		IContainer root = ResourcesPlugin.getWorkspace().getRoot();
		List list = new ArrayList(1);
		boolean result = addLanguage(root, list, lang);
		if (true == result) {
			ResolverChangeEvent[] events = new ResolverChangeEvent[list.size()];
			list.toArray(events);
			fireEvent(events);
		}
		return result;
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
		IContainer root = ResourcesPlugin.getWorkspace().getRoot();
		List list = new ArrayList(1);
		boolean result = addFileType(root, list, type);
		if (true == result) {
			ResolverChangeEvent[] events = new ResolverChangeEvent[list.size()];
			list.toArray(events);
			fireEvent(events);
		}
		return result;
	}

	/**
	 * Remove a language from the list of languages known to the resolver.
	 * 
	 * @param lang language to remove
	 * 
	 * @return true if the language is removed, false otherwise
	 */
	public boolean removeLanguage(ICLanguage lang) {
		IContainer root = ResourcesPlugin.getWorkspace().getRoot();
		List list = new ArrayList(1);
		boolean result = removeLanguage(root, list, lang);
		if (true == result) {
			ResolverChangeEvent[] events = new ResolverChangeEvent[list.size()];
			list.toArray(events);
			fireEvent(events);
		}
		return result;
	}

	/**
	 * Remove a file type from the list of type known to the resolver.
	 * 
	 * @param type file type to remove
	 * 
	 * @return true if the file type is removed, false otherwise
	 */
	public boolean removeFileType(ICFileType type) {
		IContainer root = ResourcesPlugin.getWorkspace().getRoot();
		List list = new ArrayList(1);
		boolean result = removeFileType(root, list, type);
		if (true == result) {
			ResolverChangeEvent[] events = new ResolverChangeEvent[list.size()];
			list.toArray(events);
			fireEvent(events);
		}
		return result;
	}

	public void addResolverChangeListener(IResolverChangeListener listener) {
		fListeners.add(listener);
	}

	public void removeResolverChangeListener(IResolverChangeListener listener) {
		fListeners.remove(listener);
	}

	//----------------------------------------------------------------------
	// Misc. internal
	//----------------------------------------------------------------------

	private IExtensionPoint getExtensionPoint(String extensionPointId) {
        return Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, extensionPointId);
	}

	private static boolean isDebugging() {
		return VERBOSE;
	}
	
	private static void debugLog(String message) {
		System.out.println("CDT Resolver: " + message); //$NON-NLS-1$
	}

	//----------------------------------------------------------------------
	// Registry change event handling
	//----------------------------------------------------------------------

	private boolean addLanguage(IContainer container, List list, ICLanguage lang) { 
		boolean added = false;
		if (!fLangMap.containsValue(lang)) {
			fLangMap.put(lang.getId(), lang);
			if (null != list) {
				ResolverChangeEvent event = new ResolverChangeEvent(container, ResolverChangeEvent.EVENT_ADD, lang);
				list.add(event);
			}
			added = true;
		}
		return added;
	}
	
	private boolean addFileType(IContainer container, List list, ICFileType type) { 
		boolean added = false;
		if (!fTypeMap.containsValue(type)) {
			fTypeMap.put(type.getId(), type);
			if (null != list) {
				ResolverChangeEvent event = new ResolverChangeEvent(container, ResolverChangeEvent.EVENT_ADD, type);
				list.add(event);
			}
			added = true;
		}
		return added;
	}

	private boolean removeLanguage(IContainer container, List list, ICLanguage lang) {
		boolean removed = (null != fLangMap.remove(lang.getId()));
		
		if (removed) {
			if (null != list) {
				ResolverChangeEvent event = new ResolverChangeEvent(container, ResolverChangeEvent.EVENT_REMOVE, lang);
				list.add(event);
			}
			ArrayList removeList = new ArrayList();
			for (Iterator iter = fTypeMap.values().iterator(); iter.hasNext();) {
				ICFileType type = (ICFileType) iter.next();
				if (lang.equals(type.getLanguage())) {
					removeList.add(type); 
				}
			}
			for (Iterator iter = removeList.iterator(); iter.hasNext();) {
				removeFileType(container, list, (ICFileType) iter.next());
			}
		}
		return removed;
	}
	
	private boolean removeFileType(IContainer container, List list, ICFileType type) {
		boolean removed = (null != fTypeMap.remove(type.getId())); 
		if (removed) {
			if (null != list) {
				ResolverChangeEvent event = new ResolverChangeEvent(container, ResolverChangeEvent.EVENT_REMOVE, type);
				list.add(event);
			}
			// TODO: must remove any associations based on this file type as well
			// Unforuntately, at this point, that means iterating over the contents
			// of the default, workspace, and project resolvers.  Ugh.
		}
		return removed;
	}	

	private void fireEvent(final ResolverChangeEvent[] events) {
		
		if (events == null || events.length == 0) {
			return;
		}

		if (isDebugging()) {
			for (int i = 0; i < events.length; i++) {
				debugLog(events[i].toString());
			}
		}

		if (fListeners.isEmpty()) {
			return;
		}

		final IResolverChangeListener[] listeners;
		listeners = (IResolverChangeListener[]) fListeners.toArray(new IResolverChangeListener[fListeners.size()]);

		for (int i = 0; i < listeners.length; i++) {
			final int index = i;
			Platform.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
							CCorePlugin.getResourceString("ResolverModel.exception.listenerError"), exception); //$NON-NLS-1$
					CCorePlugin.log(status);
				}
				public void run() throws Exception {
					listeners[index].resolverChanged(events);
				}
			});
		}
	}

	private void fireResolverChangeEvent(IProject project, ICFileTypeResolver oldResolver) {
		IContainer container = project;
		ICFileTypeResolver	newResolver	= getResolver(project);
		//ResolverChangeEvent event		= new ResolverChangeEvent(newResolver);
		//int					element 	= ResolverDelta.ELEMENT_WORKSPACE;
		//if (null != project) {
		//	element = ResolverDelta.ELEMENT_PROJECT; 
		//}
		//event.addDelta(new ResolverDelta(ResolverDelta.EVENT_SET, element, project));

		List list = new ArrayList();
		if (container == null) {
			container = ResourcesPlugin.getWorkspace().getRoot();
		}
		list.add(new ResolverChangeEvent(container, ResolverChangeEvent.EVENT_SET, newResolver));
		
		if ((null != oldResolver) && (null != newResolver)) {
			ICFileTypeAssociation[] oldAssoc = oldResolver.getFileTypeAssociations();
			ICFileTypeAssociation[] newAssoc = newResolver.getFileTypeAssociations();
				
			for (int i = 0; i < oldAssoc.length; i++) {
				if (Arrays.binarySearch(newAssoc, oldAssoc[i], ICFileTypeAssociation.Comparator) < 0) {
					//event.addDelta(new ResolverDelta(oldAssoc[i], ResolverDelta.EVENT_REMOVE));
					list.add(new ResolverChangeEvent(container, ResolverChangeEvent.EVENT_REMOVE, oldAssoc[i]));
				}
			}
			
			for (int i = 0; i < newAssoc.length; i++) {
				if (Arrays.binarySearch(oldAssoc, newAssoc[i], ICFileTypeAssociation.Comparator) < 0) {
					//event.addDelta(new ResolverDelta(newAssoc[i], ResolverDelta.EVENT_ADD));
					list.add(new ResolverChangeEvent(container, ResolverChangeEvent.EVENT_ADD, newAssoc[i]));
				}
			}
		}
		ResolverChangeEvent[] events = new ResolverChangeEvent[list.size()];
		list.toArray(events);
		fireEvent(events);
	}

	private void initRegistryChangeListener() {
		Platform.getExtensionRegistry().addRegistryChangeListener(new IRegistryChangeListener() {
			public void registryChanged(IRegistryChangeEvent event) {
				handleRegistryChanged(event);
			}
		}, CCorePlugin.PLUGIN_ID);
	}

	protected void handleRegistryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[]	deltas = null;
		//ResolverChangeEvent	modelEvent = new ResolverChangeEvent(null);
		List list = new ArrayList();
		IContainer container = ResourcesPlugin.getWorkspace().getRoot();
		
		deltas = event.getExtensionDeltas(CCorePlugin.PLUGIN_ID, EXTENSION_LANG);
		for (int i = 0; i < deltas.length; i++) {
			processLanguageExtension(container, list, deltas[i].getExtension(), IExtensionDelta.ADDED == deltas[i].getKind());
		}
		
		deltas = event.getExtensionDeltas(CCorePlugin.PLUGIN_ID, EXTENSION_TYPE);
		for (int i = 0; i < deltas.length; i++) {
			processTypeExtension(container, list, deltas[i].getExtension(), IExtensionDelta.ADDED == deltas[i].getKind());
		}
		
		deltas = event.getExtensionDeltas(CCorePlugin.PLUGIN_ID, EXTENSION_ASSOC);			
		if (deltas.length != 0) {
			fDefaultResolver	= loadDefaultResolver();
			fWkspResolver		= loadWorkspaceResolver();
		}
		if (!list.isEmpty()) {
			ResolverChangeEvent[] events = new ResolverChangeEvent[list.size()];
			list.toArray(events);
			fireEvent(events);			
		}
	}

	//----------------------------------------------------------------------
	// Extension point handling
	//----------------------------------------------------------------------

	/**
	 * Load languages declared through the CLanguage extension point.
	 */
	private void loadDeclaredLanguages() {
		IExtensionPoint		point 		= getExtensionPoint(EXTENSION_LANG);
		IExtension[]		extensions 	= point.getExtensions();
		//ResolverChangeEvent	event 		= new ResolverChangeEvent(null);
		IContainer root = ResourcesPlugin.getWorkspace().getRoot();
		List list = new ArrayList();

		for (int i = 0; i < extensions.length; i++) {
			processLanguageExtension(root, list, extensions[i], true);
		}

		// Shouldn't have anything listening here, but generating
		// the events helps maintain internal consistency w/logging

		if (!list.isEmpty()) {
			ResolverChangeEvent[] events = new ResolverChangeEvent[list.size()];
			list.toArray(events);
			fireEvent(events);
		}
	}
	
	/**
	 * Load file type declared through the CFileType extension point.
	 */
	private void loadDeclaredTypes() {
		IExtensionPoint		point 		= getExtensionPoint(EXTENSION_TYPE);
		IExtension[]		extensions 	= point.getExtensions();
		//ResolverChangeEvent	event 		= new ResolverChangeEvent(null);
		IContainer root = ResourcesPlugin.getWorkspace().getRoot();
		List list = new ArrayList();

		for (int i = 0; i < extensions.length; i++) {
			processTypeExtension(root, list, extensions[i], true);
		}
		
		// Shouldn't have anything listening here, but generating
		// the events helps maintain internal consistency w/logging
		if (!list.isEmpty()) {
			ResolverChangeEvent[] events = new ResolverChangeEvent[list.size()];
			list.toArray(events);
			fireEvent(events);
		}		
	}
	
	private void processLanguageExtension(IContainer container, List list, IExtension extension, boolean add) {
		IConfigurationElement[]	elements = extension.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			String id   = elements[i].getAttribute(ATTR_ID);
			String name = elements[i].getAttribute(ATTR_NAME);

			try {
				ICLanguage element = new CLanguage(id, name);
				if (add) {
					addLanguage(container, list, element);
				} else { 
					removeLanguage(container, list, element);
				}
			} catch (IllegalArgumentException e) {
				CCorePlugin.log(e);
			}
		}
	}

	private void processTypeExtension(IContainer container, List list, IExtension extension, boolean add) {
		IConfigurationElement[]	elements = extension.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			String	id	 = elements[i].getAttribute(ATTR_ID);
			String	lang = elements[i].getAttribute(ATTR_LANGUAGE);
			String	name = elements[i].getAttribute(ATTR_NAME);
			String	type = elements[i].getAttribute(ATTR_TYPE);
			
			try {
				ICFileType element = new CFileType(id, getLanguageById(lang), name, parseType(type));
				if (add) {
					addFileType(container, list, element);
				} else {
					removeFileType(container, list, element);
				}
			} catch (IllegalArgumentException e) {
				CCorePlugin.log(e);
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


	//----------------------------------------------------------------------
	// Default resolver
	//----------------------------------------------------------------------
	
	/**
	 * Initialize the default resolver by loading data from
	 * declared extension points.
	 */
	private ICFileTypeResolver loadDefaultResolver() {
		List					assoc		= new ArrayList();
		ICFileTypeResolver		resolver	= new CFileTypeResolver(ResourcesPlugin.getWorkspace().getRoot());
		IExtensionPoint			point		= getExtensionPoint(EXTENSION_ASSOC);
		IExtension[]			extensions	= point.getExtensions();
		IConfigurationElement[]	elements	= null;
		
		for (int i = 0; i < extensions.length; i++) {
			elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				ICFileType typeRef = getFileTypeById(elements[j].getAttribute(ATTR_TYPE));
				if (null != typeRef) {
					assoc.addAll(getAssocFromPattern(typeRef, elements[j]));
					assoc.addAll(getAssocFromFile(typeRef, elements[j]));
				}
			}
		}

		resolver.addAssociations((ICFileTypeAssociation[]) assoc.toArray(new ICFileTypeAssociation[assoc.size()]));
		
		return resolver;
	}
	
	/**
	 * Associate one or more file extensions with an ICFileType instance.
	 * 
	 * @param typeRef reference to the ICFileType instance
	 * 
	 * @param element configuration element to get file extensions from
	 */
	private List getAssocFromPattern(ICFileType typeRef, IConfigurationElement element) {
		List assocs = new ArrayList();
		String attr = element.getAttribute(ATTR_PATTERN);
		if (null != attr) {
			String[] item = attr.split(","); //$NON-NLS-1$
			for (int i = 0; i < item.length; i++) {
				try {
					assocs.add(createAssocation(item[i].trim(), typeRef));
				} catch (IllegalArgumentException e) {
					CCorePlugin.log(e);
				}
			}
		}
		return assocs;
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
	private List getAssocFromFile(ICFileType typeRef, IConfigurationElement element) {
		List assocs = new ArrayList();
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
		        		assocs.add(createAssocation(line.trim(), typeRef));
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
		return assocs;
	}
	
	//----------------------------------------------------------------------
	// Workspace resolver
	//----------------------------------------------------------------------
	
	private boolean customWorkspaceResolverExists() {
		return getWorkspaceResolverStateFilePath().toFile().exists();
	}

	private IPath getWorkspaceResolverStateFilePath() {
		return CCorePlugin.getDefault().getStateLocation().append(WKSP_STATE_FILE);
	}

	private ICFileTypeResolver loadWorkspaceResolver() {
		List				assocs		= new ArrayList();
		ICFileTypeResolver	resolver	= null;
		File				file		= getWorkspaceResolverStateFilePath().toFile();
		
		if (file.exists()) {
			Properties 		props	= new Properties();
			FileInputStream	in		= null;

			resolver = new CFileTypeResolver(ResourcesPlugin.getWorkspace().getRoot());
			
			try {
		    	in = new FileInputStream(file);
				
		    	props.load(in);
		
		    	for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
		    		Map.Entry	element = (Map.Entry) iter.next();
					ICFileType	type	= getFileTypeById(element.getValue().toString());
		        	try {
		        		assocs.add(createAssocation(element.getKey().toString(), type));
		        	} catch (IllegalArgumentException e) {
						CCorePlugin.log(e);
					}
				}
				
				in.close();
		    } catch (IOException e) {
		    	CCorePlugin.log(e);
		    }
		
		    if (null != in) {
		    	in = null;
		    }

		    resolver.addAssociations((ICFileTypeAssociation[]) assocs.toArray(new ICFileTypeAssociation[assocs.size()]));
		}

		return resolver;
	}
	
	private void saveWorkspaceResolver(ICFileTypeResolver resolver) {
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

	private boolean customProjectResolverExists(IProject project) {
		Element	data	= getProjectData(project, false);
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
	
	private ICDescriptor getProjectDescriptor(IProject project, boolean create) throws CoreException {
		ICDescriptor descriptor = null;
		descriptor = CCorePlugin.getDefault().getCProjectDescription(project, create);
		return descriptor;
	}

	private Element getProjectData(IProject project, boolean create) {
		Element data = null;
		try {
			ICDescriptor desc = getProjectDescriptor(project, create);
			if (desc != null) {
				data = desc.getProjectData(CDT_RESOLVER);
			}
		} catch (CoreException e) {
		}
		return data;
	}
	
	private ICFileTypeResolver loadProjectResolver(IProject project) {
		List				assocs		= new ArrayList();
		ICFileTypeResolver	resolver 	= new CFileTypeResolver(project);
		Element				data		= getProjectData(project, false);
		Node				child 		= ((null != data) ? data.getFirstChild() : null);
		
		while (child != null) {
			if (child.getNodeName().equals(TAG_ASSOC)) {
				Node assoc = child.getFirstChild();
				while (assoc != null) {
					if (assoc.getNodeName().equals(TAG_ENTRY)) {
						Element element	= (Element) assoc;
						String	pattern	= element.getAttribute(ATTR_PATTERN);
						String 	typeId	= element.getAttribute(ATTR_TYPE);
						try {
							assocs.add(createAssocation(pattern, getFileTypeById(typeId)));
			        	} catch (IllegalArgumentException e) {
							CCorePlugin.log(e);
						}
					}
					assoc = assoc.getNextSibling();
				}
			}
			child = child.getNextSibling();
		}
		
		resolver.addAssociations((ICFileTypeAssociation[]) assocs.toArray(new ICFileTypeAssociation[assocs.size()]));
		
		return resolver;
	}

	private void saveProjectResolver(IProject project, ICFileTypeResolver resolver) {
		Element			root	= getProjectData(project, true);
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
			getProjectDescriptor(project, true).saveProjectData();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
}
