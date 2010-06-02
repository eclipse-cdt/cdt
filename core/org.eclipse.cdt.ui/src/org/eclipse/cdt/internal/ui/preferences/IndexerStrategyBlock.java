/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionWorkspacePreferences;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.DialogsMessages;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

/**
 * This OptionPage is used in the IndexerPreference page to allow for adjusting
 * various parsing related caches.
 */
public class IndexerStrategyBlock extends AbstractCOptionPage {

    private Button fAutoUpdateButton;
	private Button fImmediateUpdateButton;
	private Button fUseActiveBuildButton;
	private Button fUseFixedBuildConfig;

	public IndexerStrategyBlock(ICOptionContainer container){
    	setContainer(container);
    }

    @Override
	public void createControl(Composite parent) {
    	GridData gd;
    	GridLayout gl;
        Composite composite = ControlFactory.createComposite(parent, 1);
		gl=  (GridLayout)composite.getLayout();
		gl.marginWidth= 0;
		gl.verticalSpacing= gl.marginHeight*2;
		
		gd= (GridData) composite.getLayoutData();
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalAlignment= GridData.FILL;

		setControl(composite);
      
		SelectionListener updateEnablement= new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		};

		Group group= ControlFactory.createGroup(composite, DialogsMessages.IndexerStrategyBlock_strategyGroup, 1);
		gd= (GridData) group.getLayoutData();
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalAlignment= GridData.FILL;	
		fAutoUpdateButton= ControlFactory.createCheckBox(group, DialogsMessages.IndexerStrategyBlock_autoUpdate);
		fImmediateUpdateButton= ControlFactory.createCheckBox(group, DialogsMessages.IndexerStrategyBlock_immediateUpdate);
		fAutoUpdateButton.addSelectionListener(updateEnablement);
		
		if (IndexerPreferencePage.showBuildConfiguration()) {
			group= ControlFactory.createGroup(composite, DialogsMessages.IndexerStrategyBlock_buildConfigGroup, 1);
			gd= (GridData) group.getLayoutData();
			gd.grabExcessHorizontalSpace= true;
			gd.horizontalAlignment= GridData.FILL;
			fUseActiveBuildButton= ControlFactory.createRadioButton(group, DialogsMessages.IndexerStrategyBlock_activeBuildConfig, null, null);
			fUseFixedBuildConfig= ControlFactory.createRadioButton(group, DialogsMessages.IndexerStrategyBlock_specificBuildConfig, null, null);
		}		
		initializeValues();
    }

    protected void updateEnablement() {
    	fImmediateUpdateButton.setEnabled(fAutoUpdateButton.getSelection());
	}

	private void initializeValues() {
    	int updatePolicy= IndexerPreferences.getUpdatePolicy(null);
    	initUpdatePolicy(updatePolicy);

    	if (fUseActiveBuildButton != null) {
    		ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
    		ICProjectDescriptionWorkspacePreferences prefs= prjDescMgr.getProjectDescriptionWorkspacePreferences(false);
    		boolean useActive= prefs.getConfigurationRelations() == ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE;
    		fUseActiveBuildButton.setSelection(useActive);
    		fUseFixedBuildConfig.setSelection(!useActive);
    	}    	
    	updateEnablement();
	}

	private void initUpdatePolicy(int updatePolicy) {
		fAutoUpdateButton.setSelection(updatePolicy != IndexerPreferences.UPDATE_POLICY_MANUAL);
    	fImmediateUpdateButton.setSelection(updatePolicy == IndexerPreferences.UPDATE_POLICY_IMMEDIATE);
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		int updatePolicy;
		if (!fAutoUpdateButton.getSelection()) {
			updatePolicy= IndexerPreferences.UPDATE_POLICY_MANUAL;
		}
		else if (fImmediateUpdateButton.getSelection()) {
			updatePolicy= IndexerPreferences.UPDATE_POLICY_IMMEDIATE;
		}
		else {
			updatePolicy= IndexerPreferences.UPDATE_POLICY_LAZY;
		}			
		IndexerPreferences.setUpdatePolicy(null, updatePolicy);

    	if (fUseActiveBuildButton != null) {
    		boolean useActive= fUseActiveBuildButton.getSelection();
    		int relation=  useActive
    		? ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE
    				: ICProjectDescriptionPreferences.CONFIGS_INDEPENDENT;
    		ICProjectDescriptionManager prjDescMgr= CCorePlugin.getDefault().getProjectDescriptionManager();
    		ICProjectDescriptionWorkspacePreferences prefs= prjDescMgr.getProjectDescriptionWorkspacePreferences(true);
    		prefs.setConfigurationRelations(relation);
    		prjDescMgr.setProjectDescriptionWorkspacePreferences(prefs, false, new NullProgressMonitor());
    	}
	}

    @Override
	public void performDefaults() {
    	initUpdatePolicy(IndexerPreferences.getDefaultUpdatePolicy());
    	if (fUseActiveBuildButton != null) {
    		fUseActiveBuildButton.setSelection(false);
    		fUseFixedBuildConfig.setSelection(true);
    	}
    	updateEnablement();
    }
}
