/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IMacroFileEntry;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.core.model.PathEntryManager;
import org.eclipse.cdt.internal.core.settings.model.ScannerInfoProviderProxy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Provides scanner information from {@link PathEntryManager}.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 * @deprecated Since CDT 4.0 replaced by {@link ScannerInfoProviderProxy}. Still
 *     used as a default for projects created by earlier CDT versions.
 */
@Deprecated
public class ScannerProvider extends AbstractCExtension implements IScannerInfoProvider, IElementChangedListener {

	// Listeners interested in build model changes
	private static Map<IProject, List<IScannerInfoChangeListener>> listeners;

	private static ScannerProvider fProvider;

	// Map of the cache scannerInfos

	public static synchronized IScannerInfoProvider getInstance() {
		if (fProvider == null) {
			fProvider = new ScannerProvider();
			CoreModel.getDefault().addElementChangedListener(fProvider);
		}
		return fProvider;
	}

	/*
	 * @return
	 */
	private static Map<IProject, List<IScannerInfoChangeListener>> getListeners() {
		if (listeners == null) {
			listeners = new HashMap<>();
		}
		return listeners;
	}

	/**
	 * @param project
	 * @param info
	 */
	protected static void notifyInfoListeners(IProject project, IScannerInfo info) {
		// Call in the cavalry
		List<?> listeners = getListeners().get(project);
		if (listeners == null) {
			return;
		}
		IScannerInfoChangeListener[] observers = new IScannerInfoChangeListener[listeners.size()];
		listeners.toArray(observers);
		for (IScannerInfoChangeListener observer : observers) {
			observer.changeNotification(project, info);
		}
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		IPath resPath = resource.getFullPath();

		try {
			// get the includes
			IIncludeEntry[] includeEntries = CoreModel.getIncludeEntries(resPath);
			int localCount = 0, systemCount = 0;
			for (int i = 0; i < includeEntries.length; ++i) {
				if (includeEntries[i].isSystemInclude()) {
					++systemCount;
				} else {
					++localCount;
				}
			}
			String[] localIncludes = new String[localCount];
			String[] systemIncludes = new String[systemCount];
			for (int i = 0, j = 0, k = 0; i < includeEntries.length; ++i) {
				if (includeEntries[i].isSystemInclude()) {
					systemIncludes[j++] = includeEntries[i].getFullIncludePath().toOSString();
				} else {
					localIncludes[k++] = includeEntries[i].getFullIncludePath().toOSString();
				}
			}

			// get the includeFile
			IIncludeFileEntry[] includeFileEntries = CoreModel.getIncludeFileEntries(resPath);
			String[] includeFiles = new String[includeFileEntries.length];
			for (int i = 0; i < includeFiles.length; ++i) {
				includeFiles[i] = includeFileEntries[i].getFullIncludeFilePath().toOSString();
			}

			// get the macros
			IMacroEntry[] macros = CoreModel.getMacroEntries(resPath);
			Map<String, String> symbolMap = new HashMap<>();
			for (int i = 0; i < macros.length; ++i) {
				symbolMap.put(macros[i].getMacroName(), macros[i].getMacroValue());
			}

			// get the macro files
			IMacroFileEntry[] macroFileEntries = CoreModel.getMacroFileEntries(resPath);
			String[] macroFiles = new String[macroFileEntries.length];
			for (int i = 0; i < macroFiles.length; ++i) {
				macroFiles[i] = macroFileEntries[i].getFullMacroFilePath().toOSString();
			}
			return new ScannerInfo(systemIncludes, localIncludes, includeFiles, symbolMap, macroFiles);
		} catch (CModelException e) {
			//
		}
		return new ScannerInfo(null, null, null, null, null);
	}

	@Override
	public synchronized void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}
		IProject project = resource.getProject();
		// Get listeners for this resource
		Map<IProject, List<IScannerInfoChangeListener>> map = getListeners();
		List<IScannerInfoChangeListener> list = map.get(project);
		if (list == null) {
			// Create a new list
			list = new ArrayList<>();
			map.put(project, list);
		}
		if (!list.contains(listener)) {
			// Add the new listener for the resource
			list.add(listener);
		}
	}

	@Override
	public synchronized void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}
		IProject project = resource.getProject();
		// Remove the listener
		Map<IProject, List<IScannerInfoChangeListener>> map = getListeners();
		List<IScannerInfoChangeListener> list = map.get(project);
		if (list != null && !list.isEmpty()) {
			// The list is not empty so try to remove listener
			list.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 */
	@Override
	public void elementChanged(ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch (CModelException e) {
		}
	}

	protected boolean isPathEntryChange(ICElementDelta delta) {
		int flags = delta.getFlags();
		return (delta.getKind() == ICElementDelta.CHANGED && ((flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0
				|| (flags & ICElementDelta.F_CHANGED_PATHENTRY_MACRO) != 0
				|| (flags & ICElementDelta.F_PATHENTRY_REORDER) != 0));
	}

	/**
	 * Processes a delta recursively.
	 */
	protected void processDelta(ICElementDelta delta) throws CModelException {
		ICElement element = delta.getElement();

		if (isPathEntryChange(delta)) {
			IResource res = element.getResource();
			IProject project = element.getCProject().getProject();
			if (res == null) {
				res = project;
			}
			IScannerInfo info = getScannerInformation(res);
			notifyInfoListeners(project, info);
		}

		ICElementDelta[] affectedChildren = delta.getAffectedChildren();
		for (ICElementDelta element2 : affectedChildren) {
			processDelta(element2);
		}
	}

}
