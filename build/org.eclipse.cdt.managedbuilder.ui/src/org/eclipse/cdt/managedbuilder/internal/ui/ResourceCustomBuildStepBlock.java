/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ResourceBuildPropertyPage;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class ResourceCustomBuildStepBlock extends AbstractCOptionPage {

		/*
		 * String constants
		 */
		private static final String PREFIX = "ResourceCustomBuildStepBlock";	//$NON-NLS-1$
		private static final String TIP = PREFIX + ".tip";	//$NON-NLS-1$
		private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
		private static final String SETTINGS_LABEL = LABEL + ".settings";	//$NON-NLS-1$
		private static final String RCBS_TOOL_GROUP = LABEL + ".tool.group";	//$NON-NLS-1$
		private static final String RCBS_APPLICABILITY = LABEL + ".applicability";	//$NON-NLS-1$
		private static final String RCBS_BEFORE = LABEL + ".applicability.rule.before";	//$NON-NLS-1$
		private static final String RCBS_AFTER = LABEL + ".applicability.rule.after";	//$NON-NLS-1$
		private static final String RCBS_OVERRIDE = LABEL + ".applicability.rule.override";	//$NON-NLS-1$
		private static final String RCBS_DISABLE = LABEL + ".applicability.rule.disable";	//$NON-NLS-1$
		private static final String INPUT_FILENAMES = LABEL + ".input.filenames";	//$NON-NLS-1$
		private static final String OUTPUT_FILENAMES = LABEL + ".output.filenames";	//$NON-NLS-1$
		private static final String COMMAND_CMD = LABEL + ".command.cmd";	//$NON-NLS-1$
		private static final String DESCRIPTION_DESC = LABEL + ".description.desc";	//$NON-NLS-1$
		private static final String APPLICABILITY_TIP = TIP + ".applicability";	//$NON-NLS-1$
		private static final String INPUTS_TIP = TIP + ".inputs";	//$NON-NLS-1$
		private static final String OUTPUTS_TIP = TIP + ".outputs";	//$NON-NLS-1$
		private static final String COMMAND_TIP = TIP + ".command";	//$NON-NLS-1$
		private static final String ANNOUNCEMENT_TIP = TIP + ".announcement";	//$NON-NLS-1$
		private static final String CONFIRM_DEFAULT_TITLE = PREFIX + ".defaults.title";	//$NON-NLS-1$
		private static final String CONFIRM_DEFAULT_MESSAGE = PREFIX + ".defaults.message";	//$NON-NLS-1$
		
		/*
		 * TODO:  Currently, the makefile generator code would need significant work to support
		 * the concept of ordering multiple tools.  For CDT 3.0, we will implement the ability to
		 * apply a resource custom build tool as an override to other tools or to be disabled.
		 * When there is more time to enhance the makefile generator code, the RCBS_BEFORE and RCBS_AFTER
		 * entries in this array can be uncommented again.  
		 */
		private static final String[] rcbsApplicabilityRules = {
							new String(ManagedBuilderUIMessages.getResourceString(RCBS_OVERRIDE)),
//							new String(ManagedBuilderUIMessages.getResourceString(RCBS_BEFORE)),
//							new String(ManagedBuilderUIMessages.getResourceString(RCBS_AFTER)),
							new String(ManagedBuilderUIMessages.getResourceString(RCBS_DISABLE)),
		};
		private static final String EMPTY_STRING = new String();

		private static final String rcbsToolId = new String("org.eclipse.cdt.managedbuilder.ui.rcbs");	//$NON-NLS-1$
		private static final String rcbsToolName = new String("Resource Custom Build Step");	//$NON-NLS-1$
		private static final String rcbsToolInputTypeId = new String("org.eclipse.cdt.managedbuilder.ui.rcbs.inputtype");	//$NON-NLS-1$
		private static final String rcbsToolInputTypeName = new String("Resource Custom Build Step Input Type");	//$NON-NLS-1$
		private static final String rcbsToolOutputTypeId = new String("org.eclipse.cdt.managedbuilder.ui.rcbs.outputtype");	//$NON-NLS-1$
		private static final String rcbsToolOutputTypeName = new String("Resource Custom Build Step Output Type");	//$NON-NLS-1$
		private static final String PATH_SEPERATOR = ";";	//$NON-NLS-1$
		/*
		 * Dialog widgets
		 */
		protected Combo rcbsApplicabilitySelector;
		protected Text buildInputs;
		protected Text buildOutputs;
		protected Text buildCommand;
		protected Text buildDescription;

		/*
		 * Bookeeping variables
		 */
		private ResourceBuildPropertyPage resParent;
		// Has the page been changed?
		private boolean dirty = false;

		private ModifyListener widgetModified = new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		    	if(e.widget == buildInputs){
		    		String val = buildInputs.getText().trim();
		    		IResourceConfiguration rcCfg = resParent.getCurrentResourceConfigClone();
		    		ITool rcbs = getRcbsTool(rcCfg,!"".equals(val));	//$NON-NLS-1$
		    		if(rcbs != null){
		    			IAdditionalInput input = rcbs.getInputTypes()[0].getAdditionalInputs()[0];
		    			if(!createList(input.getPaths()).equals(val)){
		    				input.setPaths(val);
		    				setValues();
		    		        setDirty(true);
		    			}
		    		}
		    	} else if(e.widget == buildOutputs){
		    		String val = buildOutputs.getText().trim();
		    		IResourceConfiguration rcCfg = resParent.getCurrentResourceConfigClone();
		    		ITool rcbs = getRcbsTool(rcCfg,!"".equals(val));	//$NON-NLS-1$
		    		if(rcbs != null){
		    			IOutputType output = rcbs.getOutputTypes()[0];
		    			if(!createList(output.getOutputNames()).equals(val)){
		    				output.setOutputNames(val);
		    				setValues();
		    		        setDirty(true);
		    			}
		    		}
		    	} else if(e.widget == buildCommand){
		    		String val = buildCommand.getText().trim();
		    		IResourceConfiguration rcCfg = resParent.getCurrentResourceConfigClone();
		    		ITool rcbs = getRcbsTool(rcCfg,!"".equals(val));	//$NON-NLS-1$
		    		if(rcbs != null && !rcbs.getToolCommand().equals(val)){
		    			rcbs.setToolCommand(val);
	    				setValues();
	    		        setDirty(true);
		    		}
		    	} else if(e.widget == buildDescription){
		    		String val = buildDescription.getText().trim();
		    		IResourceConfiguration rcCfg = resParent.getCurrentResourceConfigClone();
		    		ITool rcbs = getRcbsTool(rcCfg,!"".equals(val));	//$NON-NLS-1$
		    		if(rcbs != null && !rcbs.getAnnouncement().equals(val)){
		    			rcbs.setAnnouncement(val);
	    				setValues();
	    		        setDirty(true);
		    		}
		    	}
		    }
		};
		
		
		/*
		 *  Constructor
		 */
		public ResourceCustomBuildStepBlock(ResourceBuildPropertyPage resParent)
		{
			super(ManagedBuilderUIMessages.getResourceString(SETTINGS_LABEL));
			super.setContainer(resParent);
			this.resParent = resParent;
		}

		public void createControl(Composite resParent)  {
			Composite comp = new Composite(resParent, SWT.NULL);
			comp.setFont(resParent.getFont());
			comp.setLayout(new GridLayout(1, true));
			comp.setLayoutData(new GridData(GridData.FILL_BOTH));
			setControl(comp);
			
			// Create a group to present the controls that make up the rcbs tool
			final Group rcbsToolGroup = new Group(comp, SWT.NONE);
			rcbsToolGroup.setFont(resParent.getFont());
			rcbsToolGroup.setText(ManagedBuilderUIMessages.getResourceString(RCBS_TOOL_GROUP));
			rcbsToolGroup.setLayout(new GridLayout(1, false));
			rcbsToolGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 

			// Create a control for the rcbs Applicability combo and label
			createRcbsApplicabilityControl(rcbsToolGroup);
		
			// Create controls for the rcbs Additional Build Input Files Text and Label
			createBuildInputControl(rcbsToolGroup);
		
			// Create controls for the rcbs Additional Build Output Files Text and Label
			createBuildOutputControl(rcbsToolGroup);
			
			// Create controls for the rcbs Build Command Text and Label
			createBuildCommandControl(rcbsToolGroup);
			
			// Create controls for the rcbs Command Description (Announcement) Text and Label
			createCommandDescriptionControl(rcbsToolGroup);
			
		}

		/* (non-Javadoc)
		 * Creates the group that contains the rcbs applicability combo.
		 */
		private void createRcbsApplicabilityControl(Group rcbsApplicabilityGroup) {
			// One label
			final Label applicabilityLabel = new Label(rcbsApplicabilityGroup, SWT.LEFT);
			applicabilityLabel.setFont(rcbsApplicabilityGroup.getFont());
			applicabilityLabel.setText(ManagedBuilderUIMessages.getResourceString(RCBS_APPLICABILITY));
			applicabilityLabel.setToolTipText(ManagedBuilderUIMessages.getResourceString(APPLICABILITY_TIP));
			applicabilityLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			rcbsApplicabilitySelector = new Combo(rcbsApplicabilityGroup, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			rcbsApplicabilitySelector.setFont(rcbsApplicabilityGroup.getFont());
			rcbsApplicabilitySelector.setItems(rcbsApplicabilityRules);
			int idx = rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_OVERRIDE));
			rcbsApplicabilitySelector.select(idx);
			GridData gd1 = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			gd1.horizontalSpan = 1;
			gd1.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			rcbsApplicabilitySelector.setLayoutData(gd1);
			rcbsApplicabilitySelector.addSelectionListener(
					new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e) {
							resParent.getCurrentResourceConfigClone().setRcbsApplicability(
									selectionToApplicability(rcbsApplicabilitySelector.getSelectionIndex()));
							setDirty(true);
						}
					});
		}

		/* (non-Javadoc)
		 * Creates the controls for the rcbs additional build input file names text and label.
		 */
		private void createBuildInputControl(Group inputGroup) {
			// One label
			final Label inputLabel = new Label(inputGroup, SWT.LEFT);
			inputLabel.setFont(inputGroup.getFont());
			inputLabel.setText(ManagedBuilderUIMessages.getResourceString(INPUT_FILENAMES));
			inputLabel.setToolTipText(ManagedBuilderUIMessages.getResourceString(INPUTS_TIP));
			inputLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// Now we need one text widget
			buildInputs = new Text(inputGroup, SWT.SINGLE | SWT.BORDER);
			buildInputs.setFont(inputGroup.getFont());
			GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
			buildInputs.setLayoutData(gd2);
			buildInputs.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					buildInputs = null;
				}
			});
			buildInputs.getAccessible().addAccessibleListener(new AccessibleAdapter(){
				public void getName(AccessibleEvent e) {
					e.result = ManagedBuilderUIMessages.getResourceString(INPUT_FILENAMES);
				}
			});
			buildInputs.addModifyListener(widgetModified);
		}

		/* (non-Javadoc)
		 * Creates the controls for the rcbs additional build output file names text and label.
		 */
		private void createBuildOutputControl(Group outputGroup) {
			// One label
			final Label outputLabel = new Label(outputGroup, SWT.LEFT);
			outputLabel.setFont(outputGroup.getFont());
			outputLabel.setText(ManagedBuilderUIMessages.getResourceString(OUTPUT_FILENAMES));
			outputLabel.setToolTipText(ManagedBuilderUIMessages.getResourceString(OUTPUTS_TIP));
			outputLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// Now we need one text widget
			buildOutputs = new Text(outputGroup, SWT.SINGLE | SWT.BORDER);
			buildOutputs.setFont(outputGroup.getFont());
			GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
			buildOutputs.setLayoutData(gd3);
			buildOutputs.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					buildOutputs = null;
				}
			});
			buildOutputs.getAccessible().addAccessibleListener(new AccessibleAdapter(){
				public void getName(AccessibleEvent e) {
					e.result = ManagedBuilderUIMessages.getResourceString(OUTPUT_FILENAMES);
				}
			});
			buildOutputs.addModifyListener(widgetModified);
		}

		/* (non-Javadoc)
		 * Creates the controls for the rcbs build command text and label.
		 */
		private void createBuildCommandControl(Group commandGroup) {
			// One label
			final Label commandLabel = new Label(commandGroup, SWT.LEFT);
			commandLabel.setFont(commandGroup.getFont());
			commandLabel.setText(ManagedBuilderUIMessages.getResourceString(COMMAND_CMD));
			commandLabel.setToolTipText(ManagedBuilderUIMessages.getResourceString(COMMAND_TIP));
			commandLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// Now we need one text widget
			buildCommand = new Text(commandGroup, SWT.SINGLE | SWT.BORDER);
			buildCommand.setFont(commandGroup.getFont());
			GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
			buildCommand.setLayoutData(gd4);
			buildCommand.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					buildCommand = null;
				}
			});
			buildCommand.getAccessible().addAccessibleListener(new AccessibleAdapter(){
				public void getName(AccessibleEvent e) {
					e.result = ManagedBuilderUIMessages.getResourceString(COMMAND_CMD);
				}
			});
			buildCommand.addModifyListener(widgetModified);
		}

		/* (non-Javadoc)
		 * Creates the controls for the rcbs build command description (announcement) text and label.
		 */
		private void createCommandDescriptionControl(Group descriptionGroup) {
			// One label
			final Label descriptionLabel = new Label(descriptionGroup, SWT.LEFT);
			descriptionLabel.setFont(descriptionGroup.getFont());
			descriptionLabel.setText(ManagedBuilderUIMessages.getResourceString(DESCRIPTION_DESC));
			descriptionLabel.setToolTipText(ManagedBuilderUIMessages.getResourceString(ANNOUNCEMENT_TIP));
			descriptionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// Now we need one text widget
			buildDescription = new Text(descriptionGroup, SWT.SINGLE | SWT.BORDER);
			buildDescription.setFont(descriptionGroup.getFont());
			GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
			buildDescription.setLayoutData(gd5);
			buildDescription.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					buildDescription = null;
				}
			});
			buildDescription.getAccessible().addAccessibleListener(new AccessibleAdapter(){
				public void getName(AccessibleEvent e) {
					e.result = ManagedBuilderUIMessages.getResourceString(DESCRIPTION_DESC);
				}
			});
			buildDescription.addModifyListener(widgetModified);
		}

		protected void initializeValues() {
			setValues();
			setDirty(false);
		}

		public void updateValues() {
			setValues();
		}

		protected void setValues() {
			IResourceConfiguration resConfig = resParent.getCurrentResourceConfigClone();

			/*
			 * Examine the tools defined for the resource configuration.
			 * There should be at most one tool defined for a custom build step which was not an
			 * extension element (not defined by a tool integrator in a manifest).
			 * If the rcbs tool has been defined, set the field values from the defined tool.
			 * If the rcbs tool has not been defined yet, clear the field values.
			 * Finally, set the rcbsApplicability selector from the current value in the resource configuration.
			 */
			ITool tool = getRcbsTool(resConfig,false);
			
			if(tool != null){
				String tmp = createList(tool.getInputTypes()[0].getAdditionalInputs()[0].getPaths());
				if(!tmp.equals(buildInputs.getText()))
					buildInputs.setText(tmp);

				tmp = createList(tool.getOutputTypes()[0].getOutputNames());
				if(!tmp.equals(buildOutputs.getText()))
					buildOutputs.setText(tmp);
				
				tmp = tool.getToolCommand();
				if(!tmp.equals(buildCommand.getText()))
					buildCommand.setText(tmp);
				
				tmp = tool.getAnnouncement();
				if(!tmp.equals(buildDescription.getText()))
				buildDescription.setText(tmp);
			} else {
				buildInputs.setText("");	//$NON-NLS-1$
				buildOutputs.setText("");	//$NON-NLS-1$
				buildCommand.setText("");	//$NON-NLS-1$
				buildDescription.setText("");	//$NON-NLS-1$
			}

			/*
			 * Set the state of the rcbs applicability selector.
			 */
			rcbsApplicabilitySelector.select(applicabilityToSelection(resConfig.getRcbsApplicability()));

//			setDirty(false);
		}

		private int selectionToApplicability(int index){
			String sel = rcbsApplicabilitySelector.getItem(index);
			if(ManagedBuilderUIMessages.getResourceString(RCBS_OVERRIDE).equals(sel)){
				return IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AS_OVERRIDE;
			} else if(ManagedBuilderUIMessages.getResourceString(RCBS_AFTER).equals(sel)){
				return IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AFTER;
			} else if(ManagedBuilderUIMessages.getResourceString(RCBS_BEFORE).equals(sel)){
				return IResourceConfiguration.KIND_APPLY_RCBS_TOOL_BEFORE;
			}
			return IResourceConfiguration.KIND_DISABLE_RCBS_TOOL;
		}
		
		private int applicabilityToSelection(int val){
			switch(val){
			case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AFTER:
				return rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_AFTER));
			case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_BEFORE:
				return rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_BEFORE));
			case IResourceConfiguration.KIND_DISABLE_RCBS_TOOL:
				return rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_DISABLE));
			case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AS_OVERRIDE:
			default:
				return rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_OVERRIDE));
			}
			
		}
		
		public void removeValues(String id) {
			// Nothing to do...
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
		 */
		public void performDefaults() {
			IResourceConfiguration cloneResConfig;

			cloneResConfig = resParent.getCurrentResourceConfigClone();
			removeRcbsTools(cloneResConfig);
	
			setValues();
			setDirty(true);
			}
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(IProgressMonitor)
		 */
		public void performApply(IProgressMonitor monitor) throws CoreException {
			IResourceConfiguration cloneResConfig;
			IResourceConfiguration rcConfig;
			boolean rebuildNeeded = false;
			boolean rcbsStillDisabledSoNoRebuild = false;
			
			/*
			 * Gather the users input.
			 * Examine the tools defined for the resource configuration.
			 * There should be at most one tool defined for a custom build step which was not an
			 * extension element (not defined by a tool integrator in a manifest).
			 * If the rcbs tool has been defined, set the tool values from the user supplied values.
			 * If the rcbs tool has not been defined yet, create the tool and set the tool values.
			 * No validity checking of the users input is performed.  The user is responsible for providing
			 * proper input.
			 * Finally, set the rcbsApplicability attribute in the resource configuration according to the user's
			 * selection.
			 */
			
			cloneResConfig = resParent.getCurrentResourceConfigClone();
			ITool cloneTool = getRcbsTool(cloneResConfig, false);
			
			rcConfig = resParent.getCurrentResourceConfig(false);
			if(cloneTool == null){
				if(rcConfig != null)
					rebuildNeeded = removeRcbsTools(rcConfig);
			} else {
				if(rcConfig == null)
					rcConfig = resParent.getCurrentResourceConfig(true);
				
				
				ITool realTool = getRcbsTool(rcConfig,true); 
				
				realTool.getInputTypes()[0].getAdditionalInputs()[0].setPaths(
						createList(
								cloneTool.getInputTypes()[0].getAdditionalInputs()[0].getPaths()));
				realTool.getOutputTypes()[0].setOutputNames(
						createList(
								cloneTool.getOutputTypes()[0].getOutputNames()));
				realTool.setToolCommand(
						cloneTool.getToolCommand());
				realTool.setAnnouncement(
						cloneTool.getAnnouncement());
				if (realTool.isDirty()) {
					rebuildNeeded = true;
				}

				/*
				 * Get the state of the rcbs applicability selector and set the rcbsApplicability attribute in the
				 * resource configuration.
				 */
				rcConfig.setRcbsApplicability(
						cloneResConfig.getRcbsApplicability());
				
				if(rcConfig.getRcbsApplicability() == IResourceConfiguration.KIND_DISABLE_RCBS_TOOL)
					rcbsStillDisabledSoNoRebuild = true;
				
				if (rcConfig.isDirty()) {
					rebuildNeeded = true;
				}
	
				if (rebuildNeeded && !rcbsStillDisabledSoNoRebuild) {
					rcConfig.getParent().setRebuildState(true);
				}
				
				setDirty(false);
			}
		}
		
		private String createList(String[] items) {
			if(items == null)
				return new String();
			
			StringBuffer path = new StringBuffer(""); //$NON-NLS-1$
		
			for (int i = 0; i < items.length; i++) {
				path.append(items[i]);
				if (i < (items.length - 1)) {
					path.append(PATH_SEPERATOR);
				}
			}
			return path.toString();
		}
		
		private boolean removeRcbsTools(IResourceConfiguration rcConfig){
			ITool tools[] = getRcbsTools(rcConfig);
			if(tools != null){
				for(int i = 0; i < tools.length; i++)
					rcConfig.removeTool(tools[i]);
				
				boolean rebuildNeeded = 
					rcConfig.getRcbsApplicability() != IResourceConfiguration.KIND_DISABLE_RCBS_TOOL;

				rcConfig.setRcbsApplicability(IResourceConfiguration.KIND_DISABLE_RCBS_TOOL);

				return rebuildNeeded;
			}
			return false;
		}

		private ITool getRcbsTool(IResourceConfiguration rcConfig, boolean create){
			ITool rcbsTools[] = getRcbsTools(rcConfig);
			ITool rcbsTool = null; 
			if(rcbsTools != null)
				rcbsTool = rcbsTools[0];
			else if (create) {
				rcbsTool = rcConfig.createTool(null,rcbsToolId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolName,false);	//$NON-NLS-1$
				rcbsTool.setCustomBuildStep(true);
				IInputType rcbsToolInputType = rcbsTool.createInputType(null,rcbsToolInputTypeId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolInputTypeName,false);	//$NON-NLS-1$
				IAdditionalInput rcbsToolInputTypeAdditionalInput = rcbsToolInputType.createAdditionalInput(new String());
				rcbsToolInputTypeAdditionalInput.setKind(IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY);
				rcbsTool.createOutputType(null,rcbsToolOutputTypeId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolOutputTypeName,false);	//$NON-NLS-1$
			}
			return rcbsTool;
		}
		
		private ITool[] getRcbsTools(IResourceConfiguration rcConfig){
			List list = new ArrayList();
			ITool tools[] = rcConfig.getTools();
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				if (tool.getCustomBuildStep() && !tool.isExtensionElement()) {
					list.add(tool);
				}
			}
			if(list.size() != 0)
				return (ITool[])list.toArray(new ITool[list.size()]);
			return null;
		}

		public IPreferenceStore getPreferenceStore() {
			return null;
		}

		/*
		 * Sets the "dirty" state
		 */
		public void setDirty(boolean b) {
		    dirty = b;
		}

		/*
		 * Returns the "dirty" state
		 */
		public boolean isDirty() {
		    return dirty;
		}
		
		public void setVisible(boolean visible){
			if(visible)
				setValues();
			super.setVisible(visible);
		}
		
		public boolean containsDefaults(){
			return containsDefaults(resParent.getCurrentResourceConfigClone());
		}
		
		protected boolean containsDefaults(IResourceConfiguration rcCfg){
			ITool tools[] = getRcbsTools(rcCfg);
			
			if(tools == null)
				return true;
			
			return false;
		}

}
