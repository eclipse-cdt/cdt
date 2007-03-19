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
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.newui.NewUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;


public class BuildStepsTab extends AbstractCBuildPropertyTab {
	Combo combo;
	Text preCmd;
	Text preDes;
	Text postCmd;
	Text postDes;
	ITool tool;
	IConfiguration config;
	ICResourceDescription cfgdescr;
	IFileInfo rcfg;
	
	private static final String label1 = Messages.getString("BuildStepsTab.0"); //$NON-NLS-1$
	private static final String label2 = Messages.getString("BuildStepsTab.1"); //$NON-NLS-1$
	private static final String PATH_SEPERATOR = ";";	//$NON-NLS-1$
	private static final String rcbsToolId = new String("org.eclipse.cdt.managedbuilder.ui.rcbs");	//$NON-NLS-1$
	private static final String rcbsToolName = new String("Resource Custom Build Step");	//$NON-NLS-1$
	private static final String rcbsToolInputTypeId = new String("org.eclipse.cdt.managedbuilder.ui.rcbs.inputtype");	//$NON-NLS-1$
	private static final String rcbsToolInputTypeName = new String("Resource Custom Build Step Input Type");	//$NON-NLS-1$
	private static final String rcbsToolOutputTypeId = new String("org.eclipse.cdt.managedbuilder.ui.rcbs.outputtype");	//$NON-NLS-1$
	private static final String rcbsToolOutputTypeName = new String("Resource Custom Build Step Output Type");	//$NON-NLS-1$
	
	private static final String PREFIX = "ResourceCustomBuildStepBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String RCBS_APPLICABILITY = LABEL + ".applicability";	//$NON-NLS-1$
	private static final String RCBS_BEFORE = LABEL + ".applicability.rule.before";	//$NON-NLS-1$
	private static final String RCBS_AFTER = LABEL + ".applicability.rule.after";	//$NON-NLS-1$
	private static final String RCBS_OVERRIDE = LABEL + ".applicability.rule.override";	//$NON-NLS-1$
	private static final String RCBS_DISABLE = LABEL + ".applicability.rule.disable";	//$NON-NLS-1$

	private static final String[] rcbsApplicabilityRules = {
		new String(NewUIMessages.getResourceString(RCBS_OVERRIDE)),
//		new String(ManagedBuilderUIMessages.getResourceString(RCBS_BEFORE)),
//		new String(ManagedBuilderUIMessages.getResourceString(RCBS_AFTER)),
		new String(NewUIMessages.getResourceString(RCBS_DISABLE)),
	};
	
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

		if (page.isForProject()) 
			createForProject();
		else
			createForFile();
	}

	/**
	 * 
	 */
	private void createForProject() {
		Group g1 = setupGroup (usercomp, Messages.getString("BuildStepsTab.2"), 1, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(g1, label1, 1, GridData.BEGINNING);
		preCmd = setupText(g1, 1, GridData.FILL_HORIZONTAL);
		preCmd.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getCfg().setPrebuildStep(preCmd.getText());
			}});

		setupLabel(g1, label2, 1, GridData.BEGINNING);
		preDes = setupText(g1, 1, GridData.FILL_HORIZONTAL);
		preDes.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getCfg().setPreannouncebuildStep(preDes.getText());
			}});

		Group g2 = setupGroup (usercomp, Messages.getString("BuildStepsTab.3"), 1, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(g2, label1, 1, GridData.BEGINNING);
		postCmd = setupText(g2, 1, GridData.FILL_HORIZONTAL);
		postCmd.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getCfg().setPostbuildStep(postCmd.getText());
			}});

		setupLabel(g2, label2, 1, GridData.BEGINNING);
		postDes = setupText(g2, 1, GridData.FILL_HORIZONTAL);
		postDes.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getCfg().setPostannouncebuildStep(postDes.getText());
			}});
	}

	/**
	 * 
	 */
	private void createForFile() {
		Group g1 = setupGroup (usercomp, Messages.getString("BuildStepsTab.4"), 1, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(g1, NewUIMessages.getResourceString(RCBS_APPLICABILITY), 1, GridData.BEGINNING);
		
		combo = new Combo(g1, SWT.BORDER);
		combo.setItems(rcbsApplicabilityRules);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				rcfg.setRcbsApplicability(sel2app(combo.getSelectionIndex()));
			}});
		
		setupLabel(g1, Messages.getString("BuildStepsTab.5"), 1, GridData.BEGINNING);		 //$NON-NLS-1$
		preCmd = setupText(g1, 1, GridData.FILL_HORIZONTAL);
		preCmd.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (page.isForProject())
					getCfg().setPrebuildStep(preCmd.getText());
				else {
					IInputType[] ein = tool.getInputTypes();
					if (ein != null && ein.length > 0) {
						IAdditionalInput[] add = ein[0].getAdditionalInputs();
						if (add != null && add.length > 0) {
							add[0].setPaths(preCmd.getText());
						}
	    			}
				}
			}});

		setupLabel(g1, Messages.getString("BuildStepsTab.6"), 1, GridData.BEGINNING); //$NON-NLS-1$
		preDes = setupText(g1, 1, GridData.FILL_HORIZONTAL);
		preDes.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (page.isForProject())
					getCfg().setPreannouncebuildStep(preDes.getText());
				else {
					IOutputType[] out = tool.getOutputTypes();
					if (valid(out))
						out[0].setOutputNames(preDes.getText());
				}
			}});

		setupLabel(g1, label1, 1, GridData.BEGINNING);
		postCmd = setupText(g1, 1, GridData.FILL_HORIZONTAL);
		postCmd.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (page.isForProject())
					getCfg().setPostbuildStep(postCmd.getText());
				else 
					tool.setToolCommand(postCmd.getText());
			}});

		setupLabel(g1, label2, 1, GridData.BEGINNING);
		postDes = setupText(g1, 1, GridData.FILL_HORIZONTAL);
		postDes.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (page.isForProject())
					getCfg().setPostannouncebuildStep(postDes.getText());
				else 
					tool.setAnnouncement(postDes.getText());
			}});
	}
	
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null) return;
		config = getCfg(cfgd.getConfiguration());
		cfgdescr = cfgd;
		update();
	}
		
	private void update() {	
		if (page.isForProject()) {
			preCmd.setText(config.getPrebuildStep());
			preDes.setText(config.getPreannouncebuildStep());
			postCmd.setText(config.getPostbuildStep());
			postDes.setText(config.getPostannouncebuildStep());
		} else {
			rcfg = (IFileInfo)getResCfg(cfgdescr);
			combo.select(app2sel(rcfg.getRcbsApplicability()));
			tool = getRcbsTool(rcfg);
			
			if(tool != null){
				String s = EMPTY_STR;
				IInputType[] tmp = tool.getInputTypes();
				if (tmp != null && tmp.length > 0) {
					IAdditionalInput[] add = tmp[0].getAdditionalInputs();
					if (add != null && add.length > 0)
						s = createList(add[0].getPaths());
				}
				preCmd.setText(s);
				s = EMPTY_STR;
				IOutputType[] tmp2 = tool.getOutputTypes();
				if (tmp2 != null && tmp2.length > 0) {
					s = createList(tmp2[0].getOutputNames());
				}
				preDes.setText(s);
				postCmd.setText(tool.getToolCommand());
				postDes.setText(tool.getAnnouncement());
			} else {
				preCmd.setText(EMPTY_STR);
				preDes.setText(EMPTY_STR);
				postCmd.setText(EMPTY_STR);
				postDes.setText(EMPTY_STR);
			}
		}
	}
	
	private ITool getRcbsTool(IFileInfo rcConfig){
		ITool rcbsTools[] = getRcbsTools(rcConfig);
		ITool rcbsTool = null; 
		
		if(rcbsTools != null)
			rcbsTool = rcbsTools[0];
		else {
			rcbsTool = rcConfig.createTool(null,rcbsToolId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolName,false);	//$NON-NLS-1$
			rcbsTool.setCustomBuildStep(true);
			IInputType rcbsToolInputType = rcbsTool.createInputType(null,rcbsToolInputTypeId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolInputTypeName,false);	//$NON-NLS-1$
			IAdditionalInput rcbsToolInputTypeAdditionalInput = rcbsToolInputType.createAdditionalInput(new String());
			rcbsToolInputTypeAdditionalInput.setKind(IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY);
			rcbsTool.createOutputType(null,rcbsToolOutputTypeId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolOutputTypeName,false);	//$NON-NLS-1$
		}
		return rcbsTool;
	}

	private ITool[] getRcbsTools(IResourceInfo rcConfig){
		List list = new ArrayList();
		ITool tools[] = rcConfig.getTools();
		
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			if (tool.getCustomBuildStep() && !tool.isExtensionElement()) {
				list.add(tool);
			}
		}
		if(list.size() != 0) {
			return (ITool[])list.toArray(new ITool[list.size()]);
		}
		return null;
	}

	private String createList(String[] items) {
		if(items == null)
			return new String();
		
		StringBuffer path = new StringBuffer(EMPTY_STR);
	
		for (int i = 0; i < items.length; i++) {
			path.append(items[i]);
			if (i < (items.length - 1)) {
				path.append(PATH_SEPERATOR);
			}
		}
		return path.toString();
	}

	
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		if (page.isForProject()) {
			IConfiguration cfg1 = getCfg(src.getConfiguration());
			IConfiguration cfg2 = getCfg(dst.getConfiguration());
			cfg2.setPrebuildStep(cfg1.getPrebuildStep());
			cfg2.setPreannouncebuildStep(cfg1.getPreannouncebuildStep());
			cfg2.setPostbuildStep(cfg1.getPostbuildStep());
			cfg2.setPostannouncebuildStep(cfg1.getPostannouncebuildStep());
		} else {
			IFileInfo rcfg1 = (IFileInfo)getResCfg(src);
			IFileInfo rcfg2 = (IFileInfo)getResCfg(dst);
			rcfg2.setRcbsApplicability(rcfg1.getRcbsApplicability());
			ITool tool1 = getRcbsTool(rcfg1);
			ITool tool2 = getRcbsTool(rcfg2);

			IInputType[] ein1 = tool1.getInputTypes();
			IInputType[] ein2 = tool2.getInputTypes();
			if (valid(ein1) && valid(ein2)) {
				IAdditionalInput[] add1 = ein1[0].getAdditionalInputs();
				IAdditionalInput[] add2 = ein2[0].getAdditionalInputs();
				if (valid(add1) && valid(add2)) {
//				if (add1 != null && add2 != null && add1.length > 0 && add2.length > 0) {
					add2[0].setPaths(createList(add1[0].getPaths()));
				}
			}
			IOutputType[] tmp1 = tool1.getOutputTypes();			
			IOutputType[] tmp2 = tool2.getOutputTypes();
//			if (tmp1 != null && tmp2 != null && tmp1.length > 0 && tmp2.length > 0) {
			if (valid(tmp1) && valid(tmp2)) {
				tmp2[0].setOutputNames(createList(tmp1[0].getOutputNames()));
			}
			tool2.setToolCommand(tool1.getToolCommand());
			tool2.setAnnouncement(tool1.getAnnouncement());
		}
	}
	
	private int sel2app(int index){
		String sel = combo.getItem(index);
		if(NewUIMessages.getResourceString(RCBS_OVERRIDE).equals(sel)){
			return IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AS_OVERRIDE;
		} else if(NewUIMessages.getResourceString(RCBS_AFTER).equals(sel)){
			return IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AFTER;
		} else if(NewUIMessages.getResourceString(RCBS_BEFORE).equals(sel)){
			return IResourceConfiguration.KIND_APPLY_RCBS_TOOL_BEFORE;
		}
		return IResourceConfiguration.KIND_DISABLE_RCBS_TOOL;
	}

	private boolean valid(Object[] arr) { return (arr != null && arr.length > 0); }
	
	private int app2sel(int val){
		switch(val){
		case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AFTER:
			return combo.indexOf(NewUIMessages.getResourceString(RCBS_AFTER));
		case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_BEFORE:
			return combo.indexOf(NewUIMessages.getResourceString(RCBS_BEFORE));
		case IResourceConfiguration.KIND_DISABLE_RCBS_TOOL:
			return combo.indexOf(NewUIMessages.getResourceString(RCBS_DISABLE));
		case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AS_OVERRIDE:
		default:
			return combo.indexOf(NewUIMessages.getResourceString(RCBS_OVERRIDE));
		}
	}
	
	
	// This page can be displayed for managed project only
	public boolean canBeVisible() {
		if (page.isForProject() || page.isForFile())
			return getCfg().getBuilder().isManagedBuildOn();
		else 
			return false;
	}

	protected void performDefaults() {
		if (page.isForProject()) {
			config.setPrebuildStep(null);
			config.setPreannouncebuildStep(null);
			config.setPostbuildStep(null);
			config.setPostannouncebuildStep(null);
		} else {
			rcfg.setRcbsApplicability(IResourceConfiguration.KIND_DISABLE_RCBS_TOOL);
			ITool tool = getRcbsTool(rcfg);
			IInputType[] ein = tool.getInputTypes();
			if (valid(ein)) {
				IAdditionalInput[] add = ein[0].getAdditionalInputs();
				if (valid(add)) add[0].setPaths(null);
			}
			IOutputType[] tmp = tool.getOutputTypes();			
			if (valid(tmp)) tmp[0].setOutputNames(null);
			tool.setToolCommand(null);
			tool.setAnnouncement(null);
		}
		update();
	}
}
