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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.ICLanguage;
import org.eclipse.cdt.core.filetype.IResolverChangeListener;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.cdt.core.filetype.ResolverChangeEvent;
import org.eclipse.cdt.core.filetype.ResolverDelta;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

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

	// XML tag names, etc.
	private static final String EXTENSION_LANG	= "CLanguage"; //$NON-NLS-1$
	private static final String EXTENSION_TYPE	= "CFileType"; //$NON-NLS-1$
	private static final String EXTENSION_ASSOC = "CFileTypeAssociation"; //$NON-NLS-1$
	private static final String ATTR_TYPE 		= "type"; //$NON-NLS-1$
	private static final String ATTR_ID	 		= "id"; //$NON-NLS-1$
	private static final String ATTR_LANGUAGE	= "language"; //$NON-NLS-1$
	private static final String ATTR_NAME 		= "name"; //$NON-NLS-1$
	private static final String ATTR_VAL_SOURCE = "source"; //$NON-NLS-1$
	private static final String ATTR_VAL_HEADER = "header"; //$NON-NLS-1$
	
	// Trace flag
	public static boolean VERBOSE = false;
	
	// Singleton
	private static ResolverModel fInstance = null;

	// Qualified names used to identify project session properties
	private static final String			QN_RESOLVER_MODEL_ID = CCorePlugin.PLUGIN_ID + ".resolver"; //$NON-NLS-1$
	private static final QualifiedName	QN_CUSTOM_RESOLVER = new QualifiedName(QN_RESOLVER_MODEL_ID, "custom"); //$NON-NLS-1$

	// List of listeners on the model
	private List fListeners = new Vector();
	
	// Private ctor to preserve singleton status
	private ResolverModel() {
		try {
			loadDeclaredLanguages();
			loadDeclaredTypes();
			
			fDefaultResolver = loadDefaultResolver();
			convertFrom20();
			
			initRegistryChangeListener();
			initPreferenceChangeListener();
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
	public boolean addLanguages(ICLanguage[] langs) {
		ResolverChangeEvent event = new ResolverChangeEvent(this, getResolver());
		boolean result = addLanguages(langs, event);
		if (true == result) {
			fireEvent(event);
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
	public boolean addFileTypes(ICFileType[] types) {
		ResolverChangeEvent event = new ResolverChangeEvent(this, getResolver());
		boolean result = addFileTypes(types, event);
		if (true == result) {
			fireEvent(event);
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
	public boolean removeLanguages(ICLanguage[] langs) {
		ResolverChangeEvent event = new ResolverChangeEvent(this, getResolver());
		boolean result = removeLanguages(langs, event);
		if (true == result) {
			fireEvent(event);
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
	public boolean removeFileTypes(ICFileType[] types) {
		ResolverChangeEvent event = new ResolverChangeEvent(this, getResolver());
		boolean result = removeFileTypes(types, event);
		if (true == result) {
			fireEvent(event);
		}
		return result;
	}

	public ICFileTypeResolver getResolver() {
		return fDefaultResolver;
	}

	public boolean hasCustomResolver(IProject project) {
		return CustomResolver.hasCustomResolver(project);
	}
	
	public ICFileTypeResolver createCustomResolver(IProject project, ICFileTypeResolver copy) {
		ICFileTypeAssociation[] oldAssocs = null;
		ICFileTypeAssociation[] newAssocs = null;

		// get the old resolver first
		ICFileTypeResolver oldResolver = getResolver(project);
		oldAssocs = oldResolver.getFileTypeAssociations();

		// erase the old stuff.
		if (hasCustomResolver(project)) {
			CustomResolver.removeCustomResover(project);
		}

		ICFileTypeResolver newResolver = new CustomResolver(this, project);

		if (copy != null) {
			newAssocs = copy.getFileTypeAssociations();
			newResolver.addAssociations(newAssocs);
		}

		// cache the result in project session property.
		try {
			project.setSessionProperty(QN_CUSTOM_RESOLVER, newResolver);
		} catch (CoreException e) {
		}

		ResolverChangeEvent event = new ResolverChangeEvent(this, newResolver, oldResolver);

		fireResolverChangeEvent(event, newAssocs, oldAssocs);
		return newResolver;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.filetype.IResolverModel#removeCustomResolver(org.eclipse.core.resources.IProject)
	 */
	public void removeCustomResolver(IProject project) {
		if (hasCustomResolver(project)) {
			ICFileTypeAssociation[] oldAssocs = null;
			ICFileTypeAssociation[] newAssocs = null;
			
			ICFileTypeResolver oldResolver = getResolver(project);
			oldAssocs = oldResolver.getFileTypeAssociations();

			ICFileTypeResolver newResolver = getResolver();
			newAssocs = newResolver.getFileTypeAssociations();
			
			// remove the cache in project session property.
			try {
				project.setSessionProperty(QN_CUSTOM_RESOLVER, null);
			} catch (CoreException e) {
			}
			
			CustomResolver.removeCustomResover(project);
			
			ResolverChangeEvent event = new ResolverChangeEvent(this, newResolver, oldResolver);
			fireResolverChangeEvent(event, newAssocs, oldAssocs);
		}
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
				if (hasCustomResolver(project)) {
					resolver = new CustomResolver(this, project);
					// cache the result in the session property.
					try {
						project.setSessionProperty(QN_CUSTOM_RESOLVER, resolver);
					} catch (CoreException e) {
					}
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

	public void fireEvent(final ResolverChangeEvent event) {
		
		if (event == null) {
			return;
		}

		if (isDebugging()) {
			debugLog(event.toString());
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
					listeners[index].resolverChanged(event);
				}
			});
		}
	}

	private void fireResolverChangeEvent(ResolverChangeEvent event, ICFileTypeAssociation[] newAssocs,
			ICFileTypeAssociation[] oldAssocs) {
		
		if ((null != oldAssocs) && (null != newAssocs)) {
				
			for (int i = 0; i < oldAssocs.length; i++) {
				if (Arrays.binarySearch(newAssocs, oldAssocs[i], ICFileTypeAssociation.Comparator) < 0) {
					event.addDelta(new ResolverDelta(oldAssocs[i], ResolverDelta.EVENT_REMOVE));
				}
			}
			
			for (int i = 0; i < newAssocs.length; i++) {
				if (Arrays.binarySearch(oldAssocs, newAssocs[i], ICFileTypeAssociation.Comparator) < 0) {
					event.addDelta(new ResolverDelta(newAssocs[i], ResolverDelta.EVENT_ADD));
				}
			}
		}
		fireEvent(event);
	}

	private void initRegistryChangeListener() {
		Platform.getExtensionRegistry().addRegistryChangeListener(new IRegistryChangeListener() {
			public void registryChanged(IRegistryChangeEvent event) {
				handleRegistryChanged(event);
			}
		}, CCorePlugin.PLUGIN_ID);
	}

	private void initPreferenceChangeListener() {
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		prefs.addPropertyChangeListener(new Preferences.IPropertyChangeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				handlePropertyChanged(event);
			}
		});
	}

	protected void handlePropertyChanged(Preferences.PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property != null) {
			if (WorkspaceResolver.PREFS_ASSOCIATIONS_EXCLUSION.equals(property) ||
					WorkspaceResolver.PREFS_ASSOCIATIONS_INCLUSION.equals(property)) {
				fDefaultResolver = loadDefaultResolver();
			}
		}
	}

	protected void handleRegistryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[]	deltas = null;
		ResolverChangeEvent	modelEvent = new ResolverChangeEvent(this, getResolver());
		
		deltas = event.getExtensionDeltas(CCorePlugin.PLUGIN_ID, EXTENSION_LANG);
		for (int i = 0; i < deltas.length; i++) {
			processLanguageExtension(modelEvent, deltas[i].getExtension(), IExtensionDelta.ADDED == deltas[i].getKind());
		}
		
		deltas = event.getExtensionDeltas(CCorePlugin.PLUGIN_ID, EXTENSION_TYPE);
		for (int i = 0; i < deltas.length; i++) {
			processTypeExtension(modelEvent, deltas[i].getExtension(), IExtensionDelta.ADDED == deltas[i].getKind());
		}
		
		deltas = event.getExtensionDeltas(CCorePlugin.PLUGIN_ID, EXTENSION_ASSOC);			
		if (deltas.length != 0) {
			fDefaultResolver	= loadDefaultResolver();
			//fWkspResolver		= loadWorkspaceResolver();
		}
		
		fireEvent(modelEvent);
	}

	private boolean addLanguages(ICLanguage[] langs, ResolverChangeEvent event) { 
		boolean added = false;
		for (int i = 0; i < langs.length; ++i) {
			ICLanguage lang = langs[i];
			if (!fLangMap.containsValue(lang)) {
				fLangMap.put(lang.getId(), lang);
				if (null != event) {
					event.addDelta(new ResolverDelta(lang, ResolverDelta.EVENT_ADD));
				}
				added = true;
			}
		}
		return added;
	}
	
	private boolean addFileTypes(ICFileType[] types, ResolverChangeEvent event) { 
		boolean added = false;
		for (int i = 0; i < types.length; ++i) {
			ICFileType type = types[i];
			if (!fTypeMap.containsValue(type)) {
				fTypeMap.put(type.getId(), type);
				event.addDelta(new ResolverDelta(type, ResolverDelta.EVENT_ADD));
				added = true;
			}
		}
		return added;
	}

	private boolean removeLanguages(ICLanguage[] langs, ResolverChangeEvent event) {
		boolean del = false;
		ArrayList removeTypeList = new ArrayList(langs.length);
		for (int i = 0; i < langs.length; ++i) {
			ICLanguage lang = langs[i];
			boolean removed = (null != fLangMap.remove(lang.getId()));
			
			if (removed) {
				event.addDelta(new ResolverDelta(lang, ResolverDelta.EVENT_REMOVE));
				del = true;
				for (Iterator iter = fTypeMap.values().iterator(); iter.hasNext();) {
					ICFileType type = (ICFileType) iter.next();
					if (lang.equals(type.getLanguage())) {
						removeTypeList.add(type); 
					}
				}
			}
		}
		if (!removeTypeList.isEmpty()) {
			ICFileType[] types = (ICFileType[]) removeTypeList.toArray(new ICFileType[removeTypeList.size()]);
			removeFileTypes(types, event);
		}
		return del;
	}
	
	private boolean removeFileTypes(ICFileType[] types, ResolverChangeEvent event) {
		boolean changed = false;
		for (int i = 0; i < types.length; ++i) {
			ICFileType type = types[i];
			boolean removed = (null != fTypeMap.remove(type.getId())); 
			if (removed) {
				changed = true;
				if (null != event) {
					event.addDelta(new ResolverDelta(type, ResolverDelta.EVENT_REMOVE));
				}
				// TODO: must remove any associations based on this file type as well
				// Unforuntately, at this point, that means iterating over the contents
				// of the default, workspace, and project resolvers.  Ugh.
			}
		}
		return changed;
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
		ResolverChangeEvent	event 		= new ResolverChangeEvent(this, getResolver());

		for (int i = 0; i < extensions.length; i++) {
			processLanguageExtension(event, extensions[i], true);
		}

		// Shouldn't have anything listening here, but generating
		// the events helps maintain internal consistency w/logging
		
		fireEvent(event);
	}
	
	/**
	 * Load file type declared through the CFileType extension point.
	 */
	private void loadDeclaredTypes() {
		IExtensionPoint		point 		= getExtensionPoint(EXTENSION_TYPE);
		IExtension[]		extensions 	= point.getExtensions();
		ResolverChangeEvent	event 		= new ResolverChangeEvent(this, getResolver());

		for (int i = 0; i < extensions.length; i++) {
			processTypeExtension(event, extensions[i], true);
		}
		
		// Shouldn't have anything listening here, but generating
		// the events helps maintain internal consistency w/logging
		
		fireEvent(event);
	}
	
	private void processLanguageExtension(ResolverChangeEvent event, IExtension extension, boolean add) {
		IConfigurationElement[]	elements = extension.getConfigurationElements();
		List list = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			String id   = elements[i].getAttribute(ATTR_ID);
			String name = elements[i].getAttribute(ATTR_NAME);
			try {
				ICLanguage element = new CLanguage(id, name);
				list.add (element);
			} catch (IllegalArgumentException e) {
				CCorePlugin.log(e);
			}
		}
		ICLanguage[] langs = (ICLanguage[]) list.toArray(new ICLanguage[list.size()]);
		if (add) {
			addLanguages(langs, event);
		} else { 
			removeLanguages(langs, event);
		}
	}

	private void processTypeExtension(ResolverChangeEvent event, IExtension extension, boolean add) {
		IConfigurationElement[]	elements = extension.getConfigurationElements();
		List list = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			String	id	 = elements[i].getAttribute(ATTR_ID);
			String	lang = elements[i].getAttribute(ATTR_LANGUAGE);
			String	name = elements[i].getAttribute(ATTR_NAME);
			String	type = elements[i].getAttribute(ATTR_TYPE);
			
			try {
				ICFileType element = new CFileType(id, getLanguageById(lang), name, parseType(type));
				list.add(element);
			} catch (IllegalArgumentException e) {
				CCorePlugin.log(e);
			}
		}
		ICFileType[] types = (ICFileType[]) list.toArray(new ICFileType[list.size()]);
		if (add) {
			addFileTypes(types, event);
		} else {
			removeFileTypes(types, event);
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
		return new WorkspaceResolver(this);
	}

	//----------------------------------------------------------------------
	// Workspace resolver
	//----------------------------------------------------------------------

	private static final String WKSP_STATE_FILE	= "resolver.properties"; //$NON-NLS-1$

	private void convertFrom20() {
		if (customWorkspaceResolverExists()) {
			ICFileTypeAssociation[] assocs = loadWorkspaceResolver();
			if (assocs != null && assocs.length > 0) {
				getResolver().addAssociations(assocs);
			}
			try {
				getWorkspaceResolverStateFilePath().toFile().delete();
			} catch (Exception e) {
				//
			}
		}
	}

	private boolean customWorkspaceResolverExists() {
		return getWorkspaceResolverStateFilePath().toFile().exists();
	}

	private IPath getWorkspaceResolverStateFilePath() {
		return CCorePlugin.getDefault().getStateLocation().append(WKSP_STATE_FILE);
	}

	private ICFileTypeAssociation[] loadWorkspaceResolver() {
		List				assocs		= new ArrayList();
		File				file		= getWorkspaceResolverStateFilePath().toFile();
		
		if (file.exists()) {
			Properties 		props	= new Properties();
			FileInputStream	in		= null;
			
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
				
		    } catch (IOException e) {
		    	CCorePlugin.log(e);
		    } finally {
		    	if (in != null) {
		    		try {
		    			in.close();
		    		} catch (IOException e) {
		    			//
		    		}
		    	}
		    }
		}

	    return ((ICFileTypeAssociation[]) assocs.toArray(new ICFileTypeAssociation[assocs.size()]));
	}
	
}
