/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.resources.ScannerProvider;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Provider of both user specified and discovered scanner info
 *
 * @deprecated as of CDT 4.0.
 * @author vhirsl
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class DiscoveredScannerInfoProvider extends ScannerProvider {

	// This is the id of the IScannerInfoProvider extension point entry
	public static final String INTERFACE_IDENTITY = MakeCorePlugin.getUniqueIdentifier()
			+ ".DiscoveredScannerInfoProvider"; //$NON-NLS-1$

	// Name we will use to store build property with the project
	private static final QualifiedName scannerInfoProperty = new QualifiedName(MakeCorePlugin.getUniqueIdentifier(),
			"discoveredMakeBuildInfo"); //$NON-NLS-1$

	// Singleton
	private static DiscoveredScannerInfoProvider instance;

	public static DiscoveredScannerInfoProvider getDefault() {
		if (instance == null) {
			instance = new DiscoveredScannerInfoProvider();
		}
		return instance;
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		try {
			getDiscoveredScannerInfo(resource.getProject(), true);
		} catch (CoreException e) {
		}
		return super.getScannerInformation(resource);
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		super.subscribe(resource, listener);
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		super.unsubscribe(resource, listener);
	}

	public DiscoveredScannerInfo getDiscoveredScannerInfo(IProject project, boolean cacheInfo) throws CoreException {
		DiscoveredScannerInfo scannerInfo = null;
		// See if there's already one associated with the resource for this
		// session
		scannerInfo = (DiscoveredScannerInfo) project.getSessionProperty(scannerInfoProperty);

		if (scannerInfo == null) {
			scannerInfo = new DiscoveredScannerInfo(project);
			// this will convert user info
			org.eclipse.cdt.make.core.MakeScannerInfo makeScannerInfo = org.eclipse.cdt.make.core.MakeScannerProvider
					.getDefault().getMakeScannerInfo(project, cacheInfo);
			scannerInfo.setUserScannerInfo(makeScannerInfo);

			// migrate to new C Path Entries
			IContainerEntry container = CoreModel.newContainerEntry(DiscoveredPathContainer.CONTAINER_ID);
			ICProject cProject = CoreModel.getDefault().create(project);
			if (cProject != null) {
				IPathEntry[] entries = cProject.getRawPathEntries();
				List<IPathEntry> newEntries = new ArrayList<>(Arrays.asList(entries));
				if (!newEntries.contains(container)) {
					newEntries.add(container);
					cProject.setRawPathEntries(newEntries.toArray(new IPathEntry[newEntries.size()]), null);
				}
			}
			ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
			descriptor.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID); // remove scanner provider which will fallback to default
																		// cpath provider.
																		// place holder to that we don't convert again.
			project.setSessionProperty(scannerInfoProperty, scannerInfo);
		}
		return scannerInfo;
	}

	/**
	 * The build model manager for standard builds only caches the build information for a resource on a per-session basis. This
	 * method allows clients of the build model manager to programmatically remove the association between the resource and the
	 * information while the reource is still open or in the workspace. The Eclipse core will take care of removing it if a resource
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
	static void updateScannerInfo(DiscoveredScannerInfo scannerInfo) throws CoreException {
		//		 no longer supported!
	}
}
