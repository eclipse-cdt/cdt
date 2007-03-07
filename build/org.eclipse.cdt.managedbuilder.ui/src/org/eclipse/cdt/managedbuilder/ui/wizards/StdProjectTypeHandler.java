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
			tcs.put(IDEWorkbenchMessages.getString("StdProjectTypeHandler.0"), null); //$NON-NLS-1$
		} else {
			if (tc.isAbstract() || tc.isSystemObject()) return;
		// 	unlike CWizardHandler, we don't check for configs
			tcs.put(tc.getUniqueRealName(), tc);
		}
	}

	/**
	 * Note that configurations parameter is ignored
	 */
	public void createProject(IProject project, IConfiguration[] cfgs, String[] names)  throws CoreException {
		IToolChain[] tcs = getSelectedToolChains();
		if (tcs == null || tcs.length == 0) {
			tcs = new IToolChain[1]; // null value
		}
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.getProjectDescription(project);
		des = coreModel.createProjectDescription(project, true);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		ManagedProject mProj = new ManagedProject(des);
		info.setManagedProject(mProj);
		
		for (int i=0; i<tcs.length; i++) {
			String s = "0";  //$NON-NLS-1$
			String name = IDEWorkbenchMessages.getString("StdProjectTypeHandler.2"); //$NON-NLS-1$
			if (tcs[i] != null) {
				s = tcs[i].getId();
				name = tcs[i].getName(); 
			}
			
			Configuration cfg = new Configuration(mProj, (ToolChain)tcs[i], ManagedBuildManager.calculateChildId(s, null), name);
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
				System.out.println(IDEWorkbenchMessages.getString("StdProjectTypeHandler.3"));			 //$NON-NLS-1$
			}
			cfg.setArtifactName(project.getName());
			CConfigurationData data = cfg.getConfigurationData();
			des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
		}
		coreModel.setProjectDescription(project, des);
	}

	public boolean needsConfig() { return false; }
	public boolean canCreateWithoutToolchain() { return true; } 
}
