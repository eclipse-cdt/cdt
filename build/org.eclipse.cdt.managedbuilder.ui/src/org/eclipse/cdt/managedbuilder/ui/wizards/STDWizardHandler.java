/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Composite;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class STDWizardHandler extends MBSWizardHandler {

	public STDWizardHandler(Composite p, IWizard w) {
		super(Messages.StdBuildWizard_0, p, w); 
	}

	@Override
	public void addTc(IToolChain tc) {
		if (tc == null) {
			full_tcs.put(Messages.StdProjectTypeHandler_0, null); 
		} else {
			if (tc.isAbstract() || tc.isSystemObject()) return;
		// 	unlike CWizardHandler, we don't check for configs
			full_tcs.put(tc.getUniqueRealName(), tc);
		}
	}

	/**
	 * Note that configurations parameter is ignored
	 */
	@Override
	public void createProject(IProject project, boolean defaults, boolean onFinish, IProgressMonitor monitor)  throws CoreException {
		try {
			monitor.beginTask("", 100);//$NON-NLS-1$
		
			setProjectDescription(project, defaults, onFinish, monitor);
			
			doTemplatesPostProcess(project);
			doCustom(project);
			monitor.worked(30);
		} finally {
			monitor.done();
		}
	}

	private void setProjectDescription(IProject project, boolean defaults, boolean onFinish, IProgressMonitor monitor)
            throws CoreException {
	    ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
	    ICProjectDescription des = mngr.createProjectDescription(project, false, !onFinish);
	    ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
	    ManagedProject mProj = new ManagedProject(des);
	    info.setManagedProject(mProj);
	    monitor.worked(20);
	    cfgs = CfgHolder.unique(getCfgItems(false));
	    cfgs = CfgHolder.reorder(cfgs);
	    int work = 50/cfgs.length;
	    for (int i=0; i<cfgs.length; i++) {
	    	String s = (cfgs[i].getToolChain() == null) ? "0" : ((ToolChain)(cfgs[i].getToolChain())).getId();  //$NON-NLS-1$
	    	Configuration cfg = new Configuration(mProj, (ToolChain)cfgs[i].getToolChain(), ManagedBuildManager.calculateChildId(s, null), cfgs[i].getName());
	    	cfgs[i].setConfiguration(cfg);
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
	    		System.out.println(Messages.StdProjectTypeHandler_3); 
	    	}
	    	cfg.setArtifactName(mProj.getDefaultArtifactName());
	    	CConfigurationData data = cfg.getConfigurationData();
	    	des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
	    	monitor.worked(work);
	    }
	    mngr.setProjectDescription(project, des);
    }
	public boolean canCreateWithoutToolchain() { return true; } 
	
	@Override
	public void convertProject(IProject proj, IProgressMonitor monitor) throws CoreException {
	    setProjectDescription(proj, true, true, monitor);
	}
	
	/**
	 * If no toolchains selected by user, use default toolchain
	 */
	@Override
	public IToolChain[] getSelectedToolChains() {
		if (full_tcs.size() == 0 || table.getSelection().length == 0) 
			return new IToolChain[] { null };
		else
			return super.getSelectedToolChains();
	}
}
