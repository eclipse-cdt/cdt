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
package org.eclipse.cdt.core.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class ScannerProvider extends AbstractCExtension implements IScannerInfoProvider, IElementChangedListener {

	// Listeners interested in build model changes
	private static Map listeners;

	private static ScannerProvider fProvider;
	
	// Map of the cache scannerInfos
	
	public static synchronized IScannerInfoProvider getInstance() {
		if ( fProvider == null) {
			fProvider = new ScannerProvider();
			CoreModel.getDefault().addElementChangedListener(fProvider);
		}
		return fProvider;
	}

	/*
	 * @return
	 */
	private static Map getListeners() {
		if (listeners == null) {
			listeners = new HashMap();
		}
		return listeners;
	}

	/**
	 * @param project
	 * @param info
	 */
	protected static void notifyInfoListeners(IProject project, IScannerInfo info) {
		// Call in the cavalry
		List listeners = (List)getListeners().get(project);
		if (listeners == null) {
			return;
		}
		IScannerInfoChangeListener[] observers = new IScannerInfoChangeListener[listeners.size()];
		listeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].changeNotification(project, info);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#getScannerInformation(org.eclipse.core.resources.IResource)
	 */
	public IScannerInfo getScannerInformation(IResource resource) {
		IPath resPath = resource.getFullPath();

		try {
			IIncludeEntry[] includeEntries = CoreModel.getIncludeEntries(resPath);
			String[] includes = new String[includeEntries.length];
			for (int i = 0; i < includes.length; ++i) {
				includes[i] = includeEntries[i].getFullIncludePath().toOSString();
			}
			IMacroEntry[] macros = CoreModel.getMacroEntries(resPath);
			Map symbolMap = new HashMap();
			for (int i = 0; i < macros.length; ++i) {
				symbolMap.put(macros[i].getMacroName(), macros[i].getMacroValue());
			}
			return new ScannerInfo(includes, symbolMap);
		} catch (CModelException e) {
			//
		}
		return new ScannerInfo(null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#subscribe(org.eclipse.core.resources.IResource,
	 *      org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}
		IProject project = resource.getProject();
		// Get listeners for this resource
		Map map = getListeners();
		List list = (List)map.get(project);
		if (list == null) {
			// Create a new list
			list = new ArrayList();
			map.put(project, list);
		}
		if (!list.contains(listener)) {
			// Add the new listener for the resource
			list.add(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#unsubscribe(org.eclipse.core.resources.IResource,
	 *      org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}
		IProject project = resource.getProject();
		// Remove the listener
		Map map = getListeners();
		List list = (List)map.get(project);
		if (list != null && !list.isEmpty()) {
			// The list is not empty so try to remove listener
			list.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
		}
	}

	protected boolean isPathEntryChange(ICElementDelta delta) {
		int flags= delta.getFlags();
		return (delta.getKind() == ICElementDelta.CHANGED && 
				((flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0 ||
				(flags & ICElementDelta.F_CHANGED_PATHENTRY_MACRO) != 0 ||
				(flags & ICElementDelta.F_PATHENTRY_REORDER) !=0));
	}

	/**
	 * Processes a delta recursively.
	 */
	protected void processDelta(ICElementDelta delta) throws CModelException {
		ICElement element= delta.getElement();

		if (isPathEntryChange(delta)) {
			IResource res = element.getResource();
			IProject project = element.getCProject().getProject();
			if (res == null) {
				res = project;
			}
			IScannerInfo info = getScannerInformation(res);
			notifyInfoListeners(project, info);
		}
			
		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}

}
