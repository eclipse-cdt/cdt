/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.internal.filetype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ResolverChangeEvent;
import org.eclipse.cdt.core.filetype.ResolverDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;

/**
 * The new CFileTypeAssociations are save in a property String
 * The removed CFileTypeAssociations are save in a property String
 * 
 */
public class WorkspaceResolver extends CFileTypeResolver {

	public static final String PREFS_ASSOCIATIONS_INCLUSION = CCorePlugin.PLUGIN_ID + ".associationInclusion"; //$NON-NLS-1$
	public static final String PREFS_ASSOCIATIONS_EXCLUSION = CCorePlugin.PLUGIN_ID + ".associationExclusion"; //$NON-NLS-1$

	ResolverModel fModel;
	List extensionsList;
	
	private static final String EXTENSION_ASSOC = "CFileTypeAssociation"; //$NON-NLS-1$
	private static final String ATTR_TYPE 		= "type"; //$NON-NLS-1$
	private static final String ATTR_PATTERN 	= "pattern"; //$NON-NLS-1$
	private static final String ATTR_FILE 		= "file"; //$NON-NLS-1$

	public WorkspaceResolver() {
		this(ResolverModel.getDefault());
	}

	public WorkspaceResolver(ResolverModel model) {
		super(ResourcesPlugin.getWorkspace().getRoot());
		fModel = model;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.internal.filetype.CFileTypeResolver#doAdjustAssociations(org.eclipse.cdt.core.filetype.ICFileTypeAssociation[], org.eclipse.cdt.core.filetype.ICFileTypeAssociation[], boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doAdjustAssociations(ICFileTypeAssociation[] addAssocs,
			ICFileTypeAssociation[] delAssocs, boolean triggerEvent) {

		List deltas = new ArrayList();

		// add
		if (triggerEvent && null != addAssocs && addAssocs.length > 0) {
			for (int i = 0; i < addAssocs.length; i++) {
				deltas.add(new ResolverDelta(addAssocs[i], ResolverDelta.EVENT_ADD));
			}
		}

		// remove
		if (triggerEvent && null != delAssocs && delAssocs.length > 0) {
			for (int i = 0; i < delAssocs.length; i++) {
				deltas.add(new ResolverDelta(delAssocs[i], ResolverDelta.EVENT_REMOVE));
			}
		}

		// add
		// For adding we have to
		//  - check if the association was define in an extension
		//     if yes make sure we remove in the inclusion list
		//     if not add as a new association in the inclusion list
		if (null != addAssocs && addAssocs.length > 0) {
			List newIncList = new ArrayList();
			List newExcList = new ArrayList();
			List extensionsList = getExtensionsAssociations();
			for (int i = 0; i < addAssocs.length; ++i) {
				if (!extensionsList.contains(addAssocs[i])) {
					newIncList.add(addAssocs[i]);
				} else {
					newExcList.add(addAssocs[i]);
				}
			}
			if (!newIncList.isEmpty()) {
				List inclusion = getInclusionAssociations();
				inclusion.addAll(newIncList);
				ICFileTypeAssociation[] newInclusion =  ((ICFileTypeAssociation[]) inclusion.toArray(new ICFileTypeAssociation[inclusion.size()]));		
				setInclusionAssociations(newInclusion);
			}
			if (!newExcList.isEmpty()) {
				List exclusion = getExclusionAssociations();
				exclusion.removeAll(newExcList);
				ICFileTypeAssociation[] newInclusion =  ((ICFileTypeAssociation[]) exclusion.toArray(new ICFileTypeAssociation[exclusion.size()]));		
				setInclusionAssociations(newInclusion);				
			}
		}

		// remove
		// For removing we have to
		//  - check if the association was define in an extension
		//     if yes make sure we remove in the exclusion list
		//     if not remove in the inclusion list
		if (null != delAssocs && delAssocs.length > 0) {
			List newIncList = new ArrayList();
			List newExcList = new ArrayList();
			List extensionsList = getExtensionsAssociations();
			for (int i = 0; i < delAssocs.length; ++i) {
				if (extensionsList.contains(delAssocs[i])) {
					newExcList.add(delAssocs[i]);
				} else {
					newIncList.add(delAssocs[i]);
				}
			}
			if (!newExcList.isEmpty()) {
				List exclusion = getExclusionAssociations();
				exclusion.addAll(newExcList);
				ICFileTypeAssociation[] newExclusion =  ((ICFileTypeAssociation[]) exclusion.toArray(new ICFileTypeAssociation[exclusion.size()]));		
				setExclusionAssociations(newExclusion);
			}
			if (!newIncList.isEmpty()) {
				List inclusion = getInclusionAssociations();
				inclusion.removeAll(newIncList);
				ICFileTypeAssociation[] newInclusion =  ((ICFileTypeAssociation[]) inclusion.toArray(new ICFileTypeAssociation[inclusion.size()]));		
				setInclusionAssociations(newInclusion);				
			}
		}

		if ((null != addAssocs && addAssocs.length > 0) || (null != delAssocs && delAssocs.length > 0)) {
			CCorePlugin.getDefault().savePluginPreferences();
		}

		// fire the deltas.
		if (triggerEvent && !deltas.isEmpty()) {
			ResolverChangeEvent event = new ResolverChangeEvent(fModel, this);
			for (int i = 0; i < deltas.size(); ++i) {
				ResolverDelta delta = (ResolverDelta)deltas.get(i);
				event.addDelta(delta);
			}
			fModel.fireEvent(event);
		}

	}

	public List getExtensionsAssociations() {
		if (extensionsList == null) {
			extensionsList		= new ArrayList();
			IExtensionPoint			point		= getExtensionPoint(EXTENSION_ASSOC);
			IExtension[]			extensions	= point.getExtensions();
			IConfigurationElement[]	elements	= null;
			
			for (int i = 0; i < extensions.length; i++) {
				elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					ICFileType typeRef = fModel.getFileTypeById(elements[j].getAttribute(ATTR_TYPE));
					if (null != typeRef) {
						extensionsList.addAll(getAssocFromExtension(typeRef, elements[j]));
						extensionsList.addAll(getAssocFromFile(typeRef, elements[j]));
					}
				}
			}
		}
		return extensionsList;
	}

	public List getDefaultInclusionAssociations() {
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		String s = prefs.getDefaultString(PREFS_ASSOCIATIONS_INCLUSION);
		String[] items = s.split(";"); //$NON-NLS-1$
		List assoc = getAssocFromPreferences(items);
		return assoc;		
	}

	public List getInclusionAssociations() {
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		String s = prefs.getString(PREFS_ASSOCIATIONS_INCLUSION);
		String[] items = s.split(";"); //$NON-NLS-1$
		List assoc = getAssocFromPreferences(items);
		return assoc;
	}

	private void setInclusionAssociations(ICFileTypeAssociation[] addAssocs) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < addAssocs.length; ++i) {
			if (sb.length() > 0) {
				sb.append(';');
			}
			sb.append(addAssocs[i].getPattern());
			sb.append("!!"); //$NON-NLS-1$
			sb.append(addAssocs[i].getType().getId());
		}
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		String s = prefs.getString(PREFS_ASSOCIATIONS_INCLUSION);
		if (s.length() > 0) {
			sb.append(';').append(s);
			prefs.setValue(PREFS_ASSOCIATIONS_INCLUSION, sb.toString());
		}
	}

	public List getDefaultExclusionAssociations() {
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		String s = prefs.getDefaultString(PREFS_ASSOCIATIONS_EXCLUSION);
		String[] items = s.split(";"); //$NON-NLS-1$
		List assocs = getAssocFromPreferences(items);
		return assocs;
	}

	public List getExclusionAssociations() {
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		String s = prefs.getString(PREFS_ASSOCIATIONS_EXCLUSION);
		String[] items = s.split(";"); //$NON-NLS-1$
		List assocs = getAssocFromPreferences(items);
		return assocs;
	}

	private void setExclusionAssociations(ICFileTypeAssociation[] addAssocs) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < addAssocs.length; ++i) {
			if (sb.length() > 0) {
				sb.append(';');
			}
			sb.append(addAssocs[i].getPattern());
			sb.append("!!"); //$NON-NLS-1$
			sb.append(addAssocs[i].getType().getId());
		}
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		String s = prefs.getString(PREFS_ASSOCIATIONS_EXCLUSION);
		if (s.length() > 0) {
			sb.append(';').append(s);
		}
		prefs.setValue(PREFS_ASSOCIATIONS_EXCLUSION, sb.toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.internal.filetype.CFileTypeResolver#loadAssociations()
	 */
	protected ICFileTypeAssociation[] loadAssociations() {
		List assocs = new ArrayList();
		List exclusion = getExclusionAssociations();
		List inclusion = getInclusionAssociations();
		for (int i = 0; i < inclusion.size(); ++i) {
			Object inc = inclusion.get(i);
			if (!exclusion.contains(inc)) {
				assocs.add(inc);
			}
		}
		List extensions = getExtensionsAssociations();
		for (int i = 0; i < extensions.size(); ++i) {
			Object ext = extensions.get(i);
			if (!exclusion.contains(ext)) {
				assocs.add(ext);
			}
		}
		return ((ICFileTypeAssociation[]) assocs.toArray(new ICFileTypeAssociation[assocs.size()]));		
	}

	/**
	 * Associate one or more file extensions with an ICFileType instance.
	 * 
	 * @param typeRef reference to the ICFileType instance
	 * 
	 * @param element configuration element to get file extensions from
	 */
	private List getAssocFromExtension(ICFileType typeRef, IConfigurationElement element) {
		List assocs = new ArrayList();
		String attr = element.getAttribute(ATTR_PATTERN);
		if (null != attr) {
			String[] item = attr.split(","); //$NON-NLS-1$
			for (int i = 0; i < item.length; i++) {
				try {
					assocs.add(fModel.createAssocation(item[i].trim(), typeRef));
				} catch (IllegalArgumentException e) {
					CCorePlugin.log(e);
				}
			}
		}
		return assocs;
	}

	private List getAssocFromPreferences(String[] items) {
		List assocs = new ArrayList();
		for (int i = 0; i < items.length; ++i) {
			String[] item = items[i].split("!!"); //$NON-NLS-1$
			if (item.length == 2) {
				String pattern = item[0].trim();
				ICFileType typeRef = fModel.getFileTypeById(item[1]);
				try {
					assocs.add(fModel.createAssocation(pattern, typeRef));
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
		        		assocs.add(fModel.createAssocation(line.trim(), typeRef));
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

	private IExtensionPoint getExtensionPoint(String extensionPointId) {
        return Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, extensionPointId);
	}

}
