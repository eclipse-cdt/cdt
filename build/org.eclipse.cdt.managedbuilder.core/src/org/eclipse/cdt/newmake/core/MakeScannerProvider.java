/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.newmake.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.resources.ScannerProvider;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

/**
 * @deprecated @author DInglis
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class MakeScannerProvider extends ScannerProvider {
	private static final String MAKE_CORE_ID = "org.eclipse.cdt.make.core"; //$NON-NLS-1$
	// This is the id of the IScannerInfoProvider extension point entry
	public static final String INTERFACE_IDENTITY = MAKE_CORE_ID + ".MakeScannerProvider"; //$NON-NLS-1$

	// Name we will use to store build property with the project
	private static final QualifiedName scannerInfoProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(),
			"makeBuildInfo"); //$NON-NLS-1$
	static final String CDESCRIPTOR_ID = MAKE_CORE_ID + ".makeScannerInfo"; //$NON-NLS-1$

	public static final String INCLUDE_PATH = "includePath"; //$NON-NLS-1$
	public static final String PATH = "path"; //$NON-NLS-1$
	public static final String DEFINED_SYMBOL = "definedSymbol"; //$NON-NLS-1$
	public static final String SYMBOL = "symbol"; //$NON-NLS-1$

	private static MakeScannerProvider defaultProvider;

	public static MakeScannerProvider getDefault() {
		if (defaultProvider == null) {
			defaultProvider = new MakeScannerProvider();
		}
		return defaultProvider;
	}

	public MakeScannerInfo getMakeScannerInfo(IProject project, boolean cacheInfo) throws CoreException {
		MakeScannerInfo scannerInfo = null;
		// See if there's already one associated with the resource for this
		// session
		scannerInfo = (MakeScannerInfo)project.getSessionProperty(scannerInfoProperty);

		// Try to load one for the project
		if (scannerInfo == null) {
			scannerInfo = loadScannerInfo(project);
		} else {
			return scannerInfo;
		}

		// There is nothing persisted for the session, or saved in a file so
		// create a build info object
		if (scannerInfo != null && cacheInfo == true) {
			project.setSessionProperty(scannerInfoProperty, scannerInfo);
		}

		// migrate to new C Path Entries
		if (scannerInfo != null) {
			updateScannerInfo(scannerInfo);
		}
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
		descriptor.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID); // remove scanner provider which will fallback to default cpath
		// provider.
		return scannerInfo;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#getScannerInformation(org.eclipse.core.resources.IResource)
	 */
	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		try {
			getMakeScannerInfo(resource.getProject(), true);
		} catch (CoreException e) {
		}
		return super.getScannerInformation(resource);
	}

	/*
	 * Loads the build file and parses the nodes for build information. The information is then associated with the resource for the
	 * duration of the session.
	 */
	private MakeScannerInfo loadScannerInfo(IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
		ICStorageElement root = descriptor.getProjectStorageElement(CDESCRIPTOR_ID);
		ArrayList<String> includes = new ArrayList<String>();
		ArrayList<String> symbols = new ArrayList<String>();
		for (ICStorageElement child : root.getChildren()) {
			if (child.getName().equals(INCLUDE_PATH)) {
				// Add the path to the property list
				includes.add(child.getAttribute(PATH));
			} else if (child.getName().equals(DEFINED_SYMBOL)) {
				// Add the symbol to the symbol list
				symbols.add(child.getAttribute(SYMBOL));
			}
		}
		MakeScannerInfo info = new MakeScannerInfo(project);
		info.setIncludePaths(includes.toArray(new String[includes.size()]));
		info.setPreprocessorSymbols(symbols.toArray(new String[symbols.size()]));
		return info;
	}

	static void migrateToCPathEntries(MakeScannerInfo info) throws CoreException {
		Map<String, String> symbols = info.getDefinedSymbols();
		String[] includes = info.getIncludePaths();
		ICProject cProject = CoreModel.getDefault().create(info.getProject());
		IPathEntry[] entries = cProject.getRawPathEntries();
		List<IPathEntry> cPaths = new ArrayList<IPathEntry>(Arrays.asList(entries));

		Iterator<IPathEntry> cpIter = cPaths.iterator();
		while(cpIter.hasNext()) {
			int kind = cpIter.next().getEntryKind();
			if(kind == IPathEntry.CDT_INCLUDE || kind == IPathEntry.CDT_MACRO) {
				cpIter.remove();
			}
		}
		for (int i = 0; i < includes.length; i++) {
			IIncludeEntry include = CoreModel.newIncludeEntry(info.getProject().getFullPath(), null, new Path(includes[i]), true);
			if (!cPaths.contains(include)) {
				cPaths.add(include);
			}
		}
		Iterator<Entry<String, String>> syms = symbols.entrySet().iterator();
		while (syms.hasNext()) {
			Entry<String, String> entry = syms.next();
			IMacroEntry sym = CoreModel.newMacroEntry(info.getProject().getFullPath(), entry.getKey(), entry.getValue());
			if (!cPaths.contains(sym)) {
				cPaths.add(sym);
			}
		}
		cProject.setRawPathEntries(cPaths.toArray(new IPathEntry[cPaths.size()]), null);
	}

	/**
	 * The build model manager for standard builds only caches the build information for a resource on a per-session basis. This
	 * method allows clients of the build model manager to programmatically remove the association between the resource and the
	 * information while the resource is still open or in the workspace. The Eclipse core will take care of removing it if a resource
	 * is closed or deleted.
	 */
	public static void removeScannerInfo(IResource resource) {
		try {
			resource.getProject().setSessionProperty(scannerInfoProperty, null);
		} catch (CoreException e) {
		}
	}

	/**
	 * Persists build-specific information in the build file. Build information for standard make projects consists of preprocessor
	 * symbols and includes paths. Other project-related information is stored in the persistent properties of the project.
	 */
	public static void updateScannerInfo(final MakeScannerInfo scannerInfo) throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IProject project = scannerInfo.getProject();

				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);

				ICStorageElement rootElement = descriptor.getProjectStorageElement(CDESCRIPTOR_ID);

				// Clear out all current children
				// Note: Probably would be a better idea to merge in the data
				rootElement.clear();

				descriptor.saveProjectData();
				migrateToCPathEntries(scannerInfo);
			}
		}, null);
	}
}
