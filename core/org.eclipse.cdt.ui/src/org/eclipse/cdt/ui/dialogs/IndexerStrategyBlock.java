/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

/**
 * This OptionPage is used in the IndexerPreference page to allow for adjusting
 * various parsing related caches.
 */
public class IndexerStrategyBlock extends AbstractCOptionPage {

    private Button fAutoUpdateButton;
	private Button fImmediateUpdateButton;
//	private Button fUseActiveBuildButton;
//	private Button fUseFixedBuildConfig;

	public IndexerStrategyBlock(ICOptionContainer container){
    	setContainer(container);
    }

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
		
//		group= ControlFactory.createGroup(composite, DialogsMessages.IndexerStrategyBlock_buildConfigGroup, 1);
//		gd= (GridData) group.getLayoutData();
//		gd.grabExcessHorizontalSpace= true;
//		gd.horizontalAlignment= GridData.FILL;
//		fUseActiveBuildButton= ControlFactory.createRadioButton(group, DialogsMessages.IndexerStrategyBlock_activeBuildConfig, null, null);
//		fUseFixedBuildConfig= ControlFactory.createRadioButton(group, DialogsMessages.IndexerStrategyBlock_specificBuildConfig, null, null);
		
		initializeValues();
    }

    protected void updateEnablement() {
    	fImmediateUpdateButton.setEnabled(fAutoUpdateButton.getSelection());
	}

	private void initializeValues() {
    	int updatePolicy= IndexerPreferences.getUpdatePolicy(null);
    	initUpdatePolicy(updatePolicy);

//    	fUseActiveBuildButton.setSelection(false);
//    	fUseFixedBuildConfig.setSelection(false);
    	
    	updateEnablement();
	}

	private void initUpdatePolicy(int updatePolicy) {
		fAutoUpdateButton.setSelection(updatePolicy != IndexerPreferences.UPDATE_POLICY_MANUAL);
    	fImmediateUpdateButton.setSelection(updatePolicy == IndexerPreferences.UPDATE_POLICY_IMMEDIATE);
	}

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
    }

    public void performDefaults() {
    	initUpdatePolicy(IndexerPreferences.getDefaultUpdatePolicy());
    	updateEnablement();
    }
}
