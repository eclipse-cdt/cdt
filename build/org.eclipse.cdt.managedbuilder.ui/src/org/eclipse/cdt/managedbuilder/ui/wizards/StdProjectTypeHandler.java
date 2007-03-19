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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class StdProjectTypeHandler extends CWizardHandler {

	public StdProjectTypeHandler(String _name, Image _image, Composite p) {
		super(_name, null, _image, p, null);
	}

	public void addTc(IToolChain tc) {
		if (tc == null) {
			tcs.put(Messages.getString("StdProjectTypeHandler.0"), null); //$NON-NLS-1$
		} else {
			if (tc.isAbstract() || tc.isSystemObject()) return;
		// 	unlike CWizardHandler, we don't check for configs
			tcs.put(tc.getUniqueRealName(), tc);
		}
	}

	/**
	 * Note that configurations parameter is ignored
	 */
	public void createProject(IProject project, CfgHolder[] cfgs)  throws CoreException {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.getProjectDescription(project);
		des = coreModel.createProjectDescription(project, true);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		ManagedProject mProj = new ManagedProject(des);
		info.setManagedProject(mProj);
		
		for (int i=0; i<cfgs.length; i++) {
			String s = (cfgs[i].tc == null) ? "0" : cfgs[i].tc.getId();  //$NON-NLS-1$
			Configuration cfg = new Configuration(mProj, (ToolChain)cfgs[i].tc, ManagedBuildManager.calculateChildId(s, null), cfgs[i].name);
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
				System.out.println(Messages.getString("StdProjectTypeHandler.3"));			 //$NON-NLS-1$
			}
			cfg.setArtifactName(project.getName());
			CConfigurationData data = cfg.getConfigurationData();
			des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
		}
		coreModel.setProjectDescription(project, des);
	}
	public boolean canCreateWithoutToolchain() { return true; } 
	
	/**
	 * If no toolchains selected by user, use default toolchain
	 */
	public IToolChain[] getSelectedToolChains() {
		if (tcs.size() == 0 || table.getSelection().length == 0) 
			return new IToolChain[] { null };
		else
			return super.getSelectedToolChains();
	}

}
