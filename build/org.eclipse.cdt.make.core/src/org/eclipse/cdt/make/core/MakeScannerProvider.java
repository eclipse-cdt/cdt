/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MakeScannerProvider extends AbstractCExtension implements IScannerInfoProvider {

	// This is the id of the IScannerInfoProvider extension point entry
	public static final String INTERFACE_IDENTITY = MakeCorePlugin.getUniqueIdentifier() + ".MakeScannerProvider"; //$NON-NLS-1$

	// Name we will use to store build property with the project
	private static final QualifiedName scannerInfoProperty = new QualifiedName(MakeCorePlugin.getUniqueIdentifier(), "makeBuildInfo"); //$NON-NLS-1$
	private static final String CDESCRIPTOR_ID = MakeCorePlugin.getUniqueIdentifier() + ".makeScannerInfo"; //$NON-NLS-1$

	public static final String INCLUDE_PATH = "includePath"; //$NON-NLS-1$
	public static final String PATH = "path"; //$NON-NLS-1$
	public static final String DEFINED_SYMBOL = "definedSymbol"; //$NON-NLS-1$
	public static final String SYMBOL = "symbol"; //$NON-NLS-1$

	// Listeners interested in build model changes
	private static Map listeners;

	private static MakeScannerProvider defaultProvider;

	public static MakeScannerProvider getDefault() {
		if ( defaultProvider == null) {
			defaultProvider = new MakeScannerProvider();
		}
		return defaultProvider;
	}
	
	public MakeScannerInfo getMakeScannerInfo(IProject project, boolean cacheInfo) throws CoreException {
		MakeScannerInfo scannerInfo = null;
		// See if there's already one associated with the resource for this session
		scannerInfo = (MakeScannerInfo)project.getSessionProperty(scannerInfoProperty);

		// Try to load one for the project		
		if (scannerInfo == null ) {
			scannerInfo = loadScannerInfo(project);
		}

		// There is nothing persisted for the session, or saved in a file so 
		// create a build info object
		if (scannerInfo != null && cacheInfo == true) {
			project.setSessionProperty(scannerInfoProperty, scannerInfo);
		}
		return scannerInfo;
	}

	/*
	 * @return
	 */
	private synchronized static Map getListeners() {
		if (listeners == null) {
			listeners = new HashMap();
		}
		return listeners;
	}

	/**
	 * @param project
	 * @param info
	 */
	private static void notifyInfoListeners(IProject project, IScannerInfo info) {
		// Call in the cavalry
		List listeners = (List)getListeners().get(project);
		if (listeners == null) {
			return;
		}
		ListIterator iter = listeners.listIterator();
		while (iter.hasNext()) {
			((IScannerInfoChangeListener)iter.next()).changeNotification(project, (IScannerInfo)info);
		}

	}

	/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#getScannerInformation(org.eclipse.core.resources.IResource)
		 */
	public IScannerInfo getScannerInformation(IResource resource) {
		IScannerInfo info = null;
		try {
			info = getMakeScannerInfo(resource.getProject(), true);
		} catch (CoreException e) {
		}
		return info;
	}

	/*
	 * Loads the build file and parses the nodes for build information. The
	 * information is then associated with the resource for the duration of 
	 * the session.
	 */
	private MakeScannerInfo loadScannerInfo(IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
		Node child = descriptor.getProjectData(CDESCRIPTOR_ID).getFirstChild();
		ArrayList includes = new ArrayList();
		ArrayList symbols = new ArrayList();
		while (child != null) {
			if (child.getNodeName().equals(INCLUDE_PATH)) {
				// Add the path to the property list
				includes.add(((Element)child).getAttribute(PATH));
			} else if (child.getNodeName().equals(DEFINED_SYMBOL)) {
				// Add the symbol to the symbol list
				symbols.add(((Element)child).getAttribute(SYMBOL));
			}
			child = child.getNextSibling();
		}
		MakeScannerInfo info = new MakeScannerInfo(project);
		info.setIncludePaths((String[])includes.toArray(new String[includes.size()]));
		info.setPreprocessorSymbols((String[])symbols.toArray(new String[symbols.size()]));
		return info;
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
	public static void removeScannerInfo(IResource resource) {
		try {
			resource.getProject().setSessionProperty(scannerInfoProperty, null);
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
	static void updateScannerInfo(MakeScannerInfo scannerInfo) throws CoreException {
		IProject project = scannerInfo.getProject();

		// See if there's already one associated with the resource for this session
		if ( project.getSessionProperty(scannerInfoProperty) != null ) {
			project.setSessionProperty(scannerInfoProperty, scannerInfo);
		} 
		
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);

		Element rootElement = descriptor.getProjectData(CDESCRIPTOR_ID);

		// Clear out all current children
		// Note: Probably would be a better idea to merge in the data
		Node child = rootElement.getFirstChild();
		while (child != null) {
			rootElement.removeChild(child);
			child = rootElement.getFirstChild();
		}

		// Save the build info
		if (scannerInfo != null) {
			// Serialize the include paths
			Document doc = rootElement.getOwnerDocument();
			ListIterator iter = Arrays.asList(scannerInfo.getIncludePaths()).listIterator();
			while (iter.hasNext()) {
				Element pathElement = doc.createElement(INCLUDE_PATH);
				pathElement.setAttribute(PATH, (String)iter.next());
				rootElement.appendChild(pathElement);
			}
			// Now do the same for the symbols
			iter = Arrays.asList(scannerInfo.getPreprocessorSymbols()).listIterator();
			while (iter.hasNext()) {
				Element symbolElement = doc.createElement(DEFINED_SYMBOL);
				symbolElement.setAttribute(SYMBOL, (String)iter.next());
				rootElement.appendChild(symbolElement);
			}
			descriptor.saveProjectData();
		}
		notifyInfoListeners(project, scannerInfo);
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
		Map map = getListeners();
		List list = (List)map.get(project);
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
		Map map = getListeners();
		List list = (List)map.get(project);
		if (list != null && !list.isEmpty()) {
			// The list is not empty so try to remove listener
			list.remove(listener);
			map.put(project, list);
		}
	}
}
