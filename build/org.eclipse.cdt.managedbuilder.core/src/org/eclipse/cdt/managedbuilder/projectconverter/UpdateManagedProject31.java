/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.projectconverter;

import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UpdateManagedProject31 {
	private static final String INEXISTEND_PROP_ID = ""; //$NON-NLS-1$

	static void doProjectUpdate(IProgressMonitor monitor, final IProject project) throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		((ManagedBuildInfo) info).setVersion(ManagedBuildManager.getBuildInfoVersion().toString());

		info.setValid(true);
		adjustProperties(info);
	}

	private static void adjustProperties(IManagedBuildInfo info) {
		IManagedProject mProj = info.getManagedProject();
		IConfiguration[] cfgs = mProj.getConfigurations();
		for (int i = 0; i < cfgs.length; i++) {
			adjustProperties(cfgs[i]);
		}
	}

	private static void adjustProperties(IConfiguration cfg) {
		IBuildObjectProperties props = cfg.getBuildProperties();
		if (props == null)
			return;

		boolean artefactTypeSupported = props.supportsType(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID);
		boolean buildTypeSupported = props.supportsType(ManagedBuildManager.BUILD_TYPE_PROPERTY_ID);
		if (!artefactTypeSupported && !buildTypeSupported)
			return;

		String artefactType = artefactTypeSupported ? null : INEXISTEND_PROP_ID;
		String buildType = buildTypeSupported ? null : INEXISTEND_PROP_ID;
		String artExt = ((Configuration) cfg).getArtifactExtensionAttribute(false);
		String id = cfg.getId();
		if (artefactType == null) {
			artefactType = getBuildArtefactTypeFromId(id);
		}
		if (buildType == null) {
			buildType = getBuildTypeFromId(id);
		}

		if (artefactType == null || buildType == null) {
			for (IToolChain tc = cfg.getToolChain(); tc != null
					&& (artefactType == null || buildType == null); tc = tc.getSuperClass()) {
				id = tc.getId();
				if (artefactType == null) {
					artefactType = getBuildArtefactTypeFromId(id);
				}
				if (buildType == null) {
					buildType = getBuildTypeFromId(id);
				}
			}
		}

		if (artefactType != null && artefactType != INEXISTEND_PROP_ID) {
			try {
				props.setProperty(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID, artefactType);
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
		if (buildType != null && buildType != INEXISTEND_PROP_ID) {
			try {
				props.setProperty(ManagedBuildManager.BUILD_TYPE_PROPERTY_ID, buildType);
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}

		if (artExt != null)
			cfg.setArtifactExtension(artExt);
	}

	private static String getBuildArtefactTypeFromId(String id) {
		if (id.indexOf(".exe") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_EXE;
		if (id.indexOf(".so") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_SHAREDLIB;
		if (id.indexOf(".lib") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_STATICLIB;
		return null;
	}

	private static String getBuildTypeFromId(String id) {
		if (id.indexOf(".debug") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_TYPE_PROPERTY_DEBUG;
		if (id.indexOf(".release") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_TYPE_PROPERTY_RELEASE;
		return null;
	}
}
