package org.eclipse.cdt.core.build.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.BuildInfoFactory;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.resources.IStandardBuildInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

public class StandardBuildManager extends AbstractCExtension  implements IScannerInfoProvider {
	// Name we will use to store build property with the project
	private static final QualifiedName buildInfoProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "standardBuildInfo");
	private static final String ID = CCorePlugin.PLUGIN_ID + ".standardBuildInfo";
	// This is the id of the IScannerInfoProvider extension point entry
	public static final String INTERFACE_IDENTITY = CCorePlugin.PLUGIN_ID + "." + "StandardBuildManager";

	// Listeners interested in build model changes
	private static Map buildModelListeners; 

	/**
	 * @param project
	 * @return
	 */
	private static IStandardBuildInfo findBuildInfo(IResource resource, boolean create) throws CoreException {
		IStandardBuildInfo buildInfo = null;
		// See if there's already one associated with the resource for this session
		try {
			buildInfo = (IStandardBuildInfo)resource.getSessionProperty(buildInfoProperty);
		} catch (CoreException e) {
		}

		// Try to load one for the project		
		if (buildInfo == null && resource instanceof IProject) {
			buildInfo = loadBuildInfo((IProject)resource);
		}

		// There is nothing persisted for the session, or saved in a file so 
		// create a build info object
		if (buildInfo == null && create) {
			buildInfo = BuildInfoFactory.create((IProject)resource);
			try {
				((IProject)resource).setSessionProperty(buildInfoProperty, buildInfo);
			} catch (CoreException e) {
				buildInfo = null;
			}
		}
		return buildInfo;
	}

	public static IStandardBuildInfo getBuildInfo(IProject project) throws CoreException {
		return findBuildInfo(project, false);
	}
	
	public static IStandardBuildInfo getBuildInfo(IProject project, boolean create) throws CoreException {
		return findBuildInfo(project, create);
	}

	/*
	 * @return
	 */
	private static synchronized Map getBuildModelListeners() {
		if (buildModelListeners == null) {
			buildModelListeners = new HashMap();
		}
		return buildModelListeners;
	}

	public static void setPreprocessorSymbols(IProject project, String[] symbols)
		throws CoreException 
	{
		// Get the information for the project
		IStandardBuildInfo info = getBuildInfo(project);
		// Set the new information
		if (info != null) {
			String[] oldSymbols = info.getPreprocessorSymbols();
			if (!Arrays.equals(oldSymbols, symbols)) {
				info.setPreprocessorSymbols(symbols);
				// Alert the listeners
				setScannerInfoDirty(project, info);
			}
		}
	}
	
	public static void setIncludePaths(IProject project, String[] paths)
		throws CoreException
	{
		// Get the build info for the project
		IStandardBuildInfo info = getBuildInfo(project);
		if (info != null) {
			String[] oldPaths = info.getIncludePaths();
			if (!Arrays.equals(oldPaths, paths)) {
				info.setIncludePaths(paths);
				setScannerInfoDirty(project, info);
			}
		}
	}
	
	/**
	 * @param project
	 * @param info
	 */
	private static void setScannerInfoDirty(IProject project, IStandardBuildInfo info) {
		// Call in the cavalry
		List listeners = (List) getBuildModelListeners().get(project);
		if (listeners == null) {
			return;
		}
		ListIterator iter = listeners.listIterator();
		while (iter.hasNext()) {
			((IScannerInfoChangeListener)iter.next()).changeNotification(project, (IScannerInfo) info);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#getScannerInformation(org.eclipse.core.resources.IResource)
	 */
	public IScannerInfo getScannerInformation(IResource resource) {
		IStandardBuildInfo info;
		try {
			info = getBuildInfo((IProject)resource);
		} catch (CoreException e) {
			return null;
		}
		return (IScannerInfo)info;
	}

	/*
	 * Loads the build file and parses the nodes for build information. The
	 * information is then associated with the resource for the duration of 
	 * the session.
	 */
	private static IStandardBuildInfo loadBuildInfo(IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
		IStandardBuildInfo buildInfo = BuildInfoFactory.create(project, descriptor.getProjectData(ID));
		project.setSessionProperty(buildInfoProperty, buildInfo);
		return buildInfo;
	}

	/**
	 * The build model manager for standard builds only caches the build
	 * information for a resource on a per-session basis. This method
	 * allows clients of the build model manager to programmatically 
	 * remove the association between the resource and the information 
	 * while the reource is still open or in the workspace. The Eclipse core 
	 * will take care of removing it if a resource is closed or deleted. 
	 * 
	 * @param resource
	 */
	public static void removeBuildInfo(IResource resource) {
		try {
			resource.setSessionProperty(buildInfoProperty, null);
		} catch (CoreException e) {
		}
	}

	/**
	 * Persists build-specific information in the build file. Build 
	 * information for standard make projects consists of preprocessor 
	 * symbols and includes paths. Other project-related information is
	 * stored in the persistent properties of the project.  
	 * 
	 * @param project
	 */
	public static void saveBuildInfo(IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
		
		Element rootElement = descriptor.getProjectData(ID); 
		
		// Clear out all current children
		// Note: Probably would be a better idea to merge in the data
		Node child = rootElement.getFirstChild();
		while (child != null) {
			rootElement.removeChild(child);
			child = rootElement.getFirstChild();
		}
		
		// Save the build info
		IStandardBuildInfo buildInfo = getBuildInfo(project);
		if (buildInfo != null)
			buildInfo.serialize(rootElement.getOwnerDocument(), rootElement);
		descriptor.saveProjectData();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#subscribe(org.eclipse.core.resources.IResource, org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		IResource project = null;
		if (resource instanceof IProject) {
			project = resource;
		} else if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		} else {
			return;
		}
		// Get listeners for this resource
		Map map = getBuildModelListeners();
		List list = (List) map.get(project);
		if (list == null) {
			// Create a new list
			list = new ArrayList();
		}
		if (!list.contains(listener)) {
			// Add the new listener for the resource
			list.add(listener);
			map.put(project, list);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#unsubscribe(org.eclipse.core.resources.IResource, org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		IResource project = null;
		if (resource instanceof IProject) {
			project = resource;
		} else if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		} else {
			return;
		}
		// Remove the listener
		Map map = getBuildModelListeners();
		List list = (List) map.get(project);
		if (list != null && !list.isEmpty()) {
			// The list is not empty so try to remove listener
			list.remove(listener);
			map.put(project, list);
		}
	}

}
