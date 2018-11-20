/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import java.util.Map;

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class AutotoolsBuildWizardHandler extends MBSWizardHandler {
	public AutotoolsBuildWizardHandler(Composite p, IWizard w) {
		super(AutotoolsWizardMessages.getResourceString("AutotoolsBuildWizard.0"), p, w); //$NON-NLS-1$
	}

	public AutotoolsBuildWizardHandler(IProjectType pt, Composite parent, IWizard wizard) {
		super(pt, parent, wizard);
	}

	@Override
	public void createProject(IProject project, boolean defaults, boolean onFinish, IProgressMonitor monitor)
			throws CoreException {
		super.createProject(project, defaults, onFinish, monitor);
		// Fix for bug #312298
		// Following is required to get around the fact that the Scanner Discovery BuildInfo isn't
		// created at this point.  This is due to some complications caused by us superclassing the
		// gnu gcc compiler or gnu g++ compiler as tools in our toolchain.  We are essentially
		// copying the logic from the Discovery Tab of the C/C++ Properties when the Ok button
		// gets pushed.  We reset the project description and this causes the Scanner Discovery
		// BuildInfo to be written to the .cproject file.  Without this fix, a new project
		// will require rebuilding upon startup of Eclipse each time to recreate the Scanner
		// Discovery info and avoid warnings regarding header files and errors regarding missing
		// macro definitions.  This code will likely go away when the Scanner Discovery mechanism
		// gets rewritten in CDT (post 8.0).
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgd = des.getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
		ICfgScannerConfigBuilderInfo2Set cbi = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
		IScannerConfigBuilderInfo2Set baseCbi = ScannerConfigProfileManager.createScannerConfigBuildInfo2Set(project);
		@SuppressWarnings("unused")
		Map<InfoContext, IScannerConfigBuilderInfo2> baseInfoMap = baseCbi.getInfoMap();
		Map<CfgInfoContext, IScannerConfigBuilderInfo2> infoMap = cbi.getInfoMap();
		for (Map.Entry<CfgInfoContext, IScannerConfigBuilderInfo2> e : infoMap.entrySet()) {
			@SuppressWarnings("unused")
			String s = null;
			CfgInfoContext cfgInfoContext = e.getKey();
			IResourceInfo rcInfo = cfgInfoContext.getResourceInfo();
			if (rcInfo == null) { // per configuration
				s = cfgInfoContext.getConfiguration().getName();
			} else { // per resource
				IInputType typ = cfgInfoContext.getInputType();
				s = typ.getName();
			}
			IScannerConfigBuilderInfo2 bi2 = infoMap.get(cfgInfoContext);
			String profileId = bi2.getSelectedProfileId();
			bi2.setSelectedProfileId(profileId);
		}
		CoreModel.getDefault().setProjectDescription(project, des);
	}

	@Override
	public void convertProject(IProject proj, IProgressMonitor monitor) throws CoreException {
		super.convertProject(proj, monitor);
		AutotoolsNewProjectNature.addAutotoolsNature(proj, monitor);

		// For each IConfiguration, create a corresponding Autotools Configuration
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(proj);
		IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
		for (int i = 0; i < cfgs.length; ++i) {
			IConfiguration cfg = cfgs[i];
			ICConfigurationDescription cfgd = ManagedBuildManager.getDescriptionForConfiguration(cfg);
			String id = cfgd.getId();
			AutotoolsConfigurationManager.getInstance().getConfiguration(proj, id, true);
		}
		AutotoolsConfigurationManager.getInstance().saveConfigs(proj);
	}
}
