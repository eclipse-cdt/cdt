/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/** 
 * Handles the access to the content types of the platform.
 * @author markus.schorn@windriver.com
 */
public class CContentTypes {
	private static final String PREF_LOCAL_CONTENT_TYPE_SETTINGS = "enabled"; //$NON-NLS-1$
	private static final Preferences PROJECT_SCOPE = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE);
	private static final String CONTENT_TYPE_PREF_NODE = "content-types"; //$NON-NLS-1$
	private static final String FULLPATH_CONTENT_TYPE_PREF_NODE = Platform.PI_RUNTIME + IPath.SEPARATOR + CONTENT_TYPE_PREF_NODE;

	/**
	 * Implementation for {@link CCorePlugin#getContentType(IProject, String)}.
	 */
	public static IContentType getContentType(IProject project, String filename) {
		IContentTypeMatcher matcher= null;
		IScopeContext scopeCtx= null;
		boolean preferCpp= true;
		if (project != null) {
			// try with the project settings
			try {
				matcher= project.getContentTypeMatcher();
				if (usesProjectSpecificContentTypes(project)) {
					scopeCtx= new ProjectScope(project);
				}
				preferCpp= CoreModel.hasCCNature(project);
			} catch (CoreException e) {
				// fallback to workspace wide definitions.
				matcher= Platform.getContentTypeManager();
			}
		}
		else {
			matcher= Platform.getContentTypeManager();
		}

		IContentType[] cts = matcher.findContentTypesFor(filename);
		switch (cts.length) {
		case 0:
			return null;
		case 1:
			return cts[0];
		}
		
		int maxPossiblePriority= scopeCtx == null ? 11 : 101;
		int bestPriority= -1;
		IContentType bestResult= null;
		
		for (int i = 0; i < cts.length; i++) {
			IContentType candidate= cts[i];
			int priority= 0;
			try {
				if (scopeCtx != null) {
					IContentTypeSettings settings= candidate.getSettings(scopeCtx);
					if (isStrictlyAssociatedWith(settings, filename)) {
						priority= 100;
					}
				}
				if (priority == 0 && bestPriority < 100) {
					if (isStrictlyAssociatedWith(candidate, filename)) {
						priority= 10;
					}
				}
				if (isPreferredContentType(candidate, preferCpp)) {
					priority+= 1;
				}
			}
			catch (CoreException e) {
				// skip it
			}
			if (priority > bestPriority) {
				if (priority == maxPossiblePriority) {
					return candidate;
				}
				bestPriority= priority;
				bestResult= candidate;
			}
		}
		return bestResult;
	}
	
	private static boolean isPreferredContentType(IContentType candidate, boolean preferCpp) {
		while (candidate != null) {
			String id= candidate.getId();
			if (CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(id) || 
					CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id)) {	
				return preferCpp;
			}
			
			if (CCorePlugin.CONTENT_TYPE_CHEADER.equals(id) || 
					CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id)) { 
				return !preferCpp;
			}
			candidate= candidate.getBaseType();
		}
		return false;
	}

	private static boolean isStrictlyAssociatedWith(IContentTypeSettings settings, String filename) {
		String[] namespecs= settings.getFileSpecs(IContentType.FILE_NAME_SPEC);
		for (int i = 0; i < namespecs.length; i++) {
			String name = namespecs[i];
			if (name.equals(filename)) {
				return true;
			}
		}
		// check the file extensions only
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition >= 0 && dotPosition < filename.length()-1) {
			String fileExtension= filename.substring(dotPosition + 1); 
			String[] extensions= settings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
			for (int i = 0; i < extensions.length; i++) {
				String ext = extensions[i];
				if (ext.equals(fileExtension)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method is copied from the resources plugin and figures out whether
	 * project specific settings are enabled or not.
	 * Implementation for {@link CCorePlugin#usesProjectSpecificContentTypes(IProject)}.
	 */
	public static boolean usesProjectSpecificContentTypes(IProject project) {
		String projectName= project.getName();
		try {
			// be careful looking up for our node so not to create any nodes as side effect
			Preferences node = PROJECT_SCOPE;
			//TODO once bug 90500 is fixed, should be simpler
			// for now, take the long way
			if (!node.nodeExists(projectName))
				return false;
			node = node.node(projectName);
			if (!node.nodeExists(Platform.PI_RUNTIME))
				return false;
			node = node.node(Platform.PI_RUNTIME);
			if (!node.nodeExists(CONTENT_TYPE_PREF_NODE))
				return false;
			node = node.node(CONTENT_TYPE_PREF_NODE);
			return node.getBoolean(PREF_LOCAL_CONTENT_TYPE_SETTINGS, false);
		} catch (BackingStoreException e) {
			// exception treated when retrieving the project preferences
		}
		return false;
	}

	/**
	 * Implementation for {@link CCorePlugin#setUseProjectSpecificContentTypes(IProject, boolean)}. 
	 */
	public static void setUseProjectSpecificContentTypes(IProject project, boolean val) {
		ProjectScope projectScope = new ProjectScope(project);
		Preferences contentTypePrefs = projectScope.getNode(FULLPATH_CONTENT_TYPE_PREF_NODE);
		if (usesProjectSpecificContentTypes(project) != val) {
			// enable project-specific settings for this project
			contentTypePrefs.putBoolean(PREF_LOCAL_CONTENT_TYPE_SETTINGS, val);
			try {
				contentTypePrefs.flush();
			} catch (BackingStoreException e) {
				// ignore ??
			}
		}
	}
}
