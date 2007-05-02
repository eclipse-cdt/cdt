/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Composite;

public class STDWizardHandler extends MBSWizardHandler {

	public STDWizardHandler(Composite p, IWizard w) {
		super(Messages.getString("StdBuildWizard.0"), p, w); //$NON-NLS-1$
	}

	public void addTc(IToolChain tc) {
		if (tc == null) {
			full_tcs.put(UIMessages.getString("StdProjectTypeHandler.0"), null); //$NON-NLS-1$
		} else {
			if (tc.isAbstract() || tc.isSystemObject()) return;
		// 	unlike CWizardHandler, we don't check for configs
			full_tcs.put(tc.getUniqueRealName(), tc);
		}
	}

	/**
	 * Note that configurations parameter is ignored
	 */
	public void createProject(IProject project, boolean defaults)  throws CoreException {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.createProjectDescription(project, false);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		ManagedProject mProj = new ManagedProject(des);
		info.setManagedProject(mProj);

		CfgHolder[] cfgs = fConfigPage.getCfgItems(defaults);

		for (int i=0; i<cfgs.length; i++) {
			String s = (cfgs[i].getToolChain() == null) ? "0" : ((ToolChain)(cfgs[i].getToolChain())).getId();  //$NON-NLS-1$
			Configuration cfg = new Configuration(mProj, (ToolChain)cfgs[i].getToolChain(), ManagedBuildManager.calculateChildId(s, null), cfgs[i].getName());
			IBuilder bld = cfg.getEditableBuilder();
			if (bld != null) {
				if(bld.isInternalBuilder()){
					IConfiguration prefCfg = ManagedBuildManager.getPreferenceConfiguration(false);
					IBuilder prefBuilder = prefCfg.getBuilder();
					cfg.changeBuilder(prefBuilder, ManagedBuildManager.calculateChildId(cfg.getId(), null), prefBuilder.getName());
					bld = cfg.getEditableBuilder();
					bld.setBuildPath(null);
				}
				bld.setManagedBuildOn(false);
			} else {
				System.out.println(UIMessages.getString("StdProjectTypeHandler.3")); //$NON-NLS-1$
			}
			cfg.setArtifactName(removeSpaces(project.getName()));
			CConfigurationData data = cfg.getConfigurationData();
			des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
		}
		coreModel.setProjectDescription(project, des);
		
		doPostProcess(project);
	}
	public boolean canCreateWithoutToolchain() { return true; } 
	
	/**
	 * If no toolchains selected by user, use default toolchain
	 */
	public IToolChain[] getSelectedToolChains() {
		if (full_tcs.size() == 0 || table.getSelection().length == 0) 
			return new IToolChain[] { null };
		else
			return super.getSelectedToolChains();
	}
}
