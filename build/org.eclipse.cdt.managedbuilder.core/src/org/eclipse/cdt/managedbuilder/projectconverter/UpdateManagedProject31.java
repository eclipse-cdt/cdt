/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class UpdateManagedProject31 {
	
	static void doProjectUpdate(IProgressMonitor monitor, final IProject project) throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		((ManagedBuildInfo)info).setVersion(ManagedBuildManager.getBuildInfoVersion().toString());

		info.setValid(true);		
		adjustProperties(info);
	}

	
	private static void adjustProperties(IManagedBuildInfo info){
		IManagedProject mProj = info.getManagedProject();
		IConfiguration[] cfgs = mProj.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			adjustProperties(cfgs[i]);
		}
	}

	private static void adjustProperties(IConfiguration cfg){
		IBuildObjectProperties props = cfg.getBuildProperties();
		if(props == null)
			return;
		
		IToolChain tc = cfg.getToolChain();
		IToolChain extTc = ManagedBuildManager.getExtensionToolChain(tc);
		if(tc == null)
			return;
		String id = extTc.getId();
		if(id == null)
			return;
		
		if(props.supportsType(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID)){
			String val = getBuildArtefactTypeFromId(id);
			if(val != null && props.supportsValue(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID, val)){
				try {
					props.setProperty(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID, val);
				} catch (CoreException e) {
					ManagedBuilderCorePlugin.log(e);
				}
			}
		}
		if(props.supportsType(ManagedBuildManager.BUILD_TYPE_PROPERTY_ID)){
			String val = getBuildTypeFromId(id);
			if(val != null && props.supportsValue(ManagedBuildManager.BUILD_TYPE_PROPERTY_ID, val)){
				try {
					props.setProperty(ManagedBuildManager.BUILD_TYPE_PROPERTY_ID, val);
				} catch (CoreException e) {
					ManagedBuilderCorePlugin.log(e);
				}
			}
		}
	}
	
	private static String getBuildArtefactTypeFromId(String id){
		if(id.indexOf(".exe") != -1)
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_EXE;
		if(id.indexOf(".so") != -1)
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_SHAREDLIB;
		if(id.indexOf(".lib") != -1)
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_STATICLIB;
		return null;
	}

	private static String getBuildTypeFromId(String id){
		if(id.indexOf(".debug") != -1)
			return ManagedBuildManager.BUILD_TYPE_PROPERTY_DEBUG;
		if(id.indexOf(".release") != -1)
			return ManagedBuildManager.BUILD_TYPE_PROPERTY_RELEASE;
		return null;
	}
}
