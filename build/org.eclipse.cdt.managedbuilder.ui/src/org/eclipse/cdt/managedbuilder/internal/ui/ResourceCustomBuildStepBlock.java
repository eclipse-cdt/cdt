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

import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ResourceBuildPropertyPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;


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
		private String resBuildInputs;
		private String resBuildOutputs;
		private String resBuildAnnouncement;
		private String resBuildCommand;
		// Has the page been changed?
		private boolean dirty = false;

		private ModifyListener widgetModified = new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		        setDirty(true);
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
		}

		public void updateValues() {
			setValues();
		}

		protected void setValues() {
			IResourceConfiguration resConfig;
			String[] buildInputsPaths;
			String[] buildOutputsPaths;
			boolean foundRcbsTool = false;
			int idx;
			/*
			 * Examine the tools defined for the resource configuration.
			 * There should be at most one tool defined for a custom build step which was not an
			 * extension element (not defined by a tool integrator in a manifest).
			 * If the rcbs tool has been defined, set the field values from the defined tool.
			 * If the rcbs tool has not been defined yet, clear the field values.
			 * Finally, set the rcbsApplicability selector from the current value in the resource configuration.
			 */
			resConfig = resParent.getCurrentResourceConfig();
			ITool [] tools = resConfig.getTools();
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				if (tool.getCustomBuildStep() && !tool.isExtensionElement()) {
					buildInputsPaths = tool.getInputTypes()[0].getAdditionalInputs()[0].getPaths();
					resBuildInputs = "";	//$NON-NLS-1$
					for ( int j = 0; j < buildInputsPaths.length; j++ ){
						resBuildInputs += buildInputsPaths[j] + ";";	//$NON-NLS-1$
					}
					int len = resBuildInputs.length();
					resBuildInputs = resBuildInputs.substring(0,len-1);
					buildInputs.setText(resBuildInputs);

					buildOutputsPaths = tool.getOutputTypes()[0].getOutputNames();
					resBuildOutputs = "";	//$NON-NLS-1$
					for ( int j = 0; j < buildOutputsPaths.length; j++ ){
						resBuildOutputs += buildOutputsPaths[j] + ";";	//$NON-NLS-1$
					}
					len = resBuildOutputs.length();
					resBuildOutputs = resBuildOutputs.substring(0,len-1);
					buildOutputs.setText(resBuildOutputs);

					resBuildCommand = tool.getToolCommand();
					buildCommand.setText(resBuildCommand);

					resBuildAnnouncement = tool.getAnnouncement();
					buildDescription.setText(resBuildAnnouncement);

					foundRcbsTool = true;
					break;
				}
			}

			/*
			 * If an rcbs tool has not been created yet, just blank the fields.
			 */
			if(!foundRcbsTool) {
				buildInputs.setText("");	//$NON-NLS-1$
				buildOutputs.setText("");	//$NON-NLS-1$
				buildCommand.setText("");	//$NON-NLS-1$
				buildDescription.setText("");	//$NON-NLS-1$
			}

			/*
			 * Set the state of the rcbs applicability selector.
			 */
			switch(resConfig.getRcbsApplicability()){
			case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AS_OVERRIDE:
				idx = rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_OVERRIDE));
				break;
			case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AFTER:
				idx = rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_AFTER));
				break;
			case IResourceConfiguration.KIND_APPLY_RCBS_TOOL_BEFORE:
				idx = rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_BEFORE));
				break;
			case IResourceConfiguration.KIND_DISABLE_RCBS_TOOL:
				idx = rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_DISABLE));
				break;
			default:
				/*
				 * If we get an unexpected value, use the normal default of override.
				 */
				idx = rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_OVERRIDE));
				break;
			}
			rcbsApplicabilitySelector.select(idx);

			setDirty(false);
		}

		public void removeValues(String id) {
			// Nothing to do...
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
		 */
		public void performDefaults() {
			IResourceConfiguration resConfig;

			// Display a "Confirm" dialog box, since:
			//   1.  The defaults are immediately applied
			//   2.  The action cannot be undone
			Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
			boolean shouldDefault = MessageDialog.openConfirm(shell,
						ManagedBuilderUIMessages.getResourceString(CONFIRM_DEFAULT_TITLE),
						ManagedBuilderUIMessages.getResourceString(CONFIRM_DEFAULT_MESSAGE));
			if (!shouldDefault) return;

			/*
			 * Examine the tools defined for the resource configuration.
			 * There should be at most one tool defined for a custom build step which was not an
			 * extension element (not defined by a tool integrator in a manifest).
			 * If the rcbs tool has been defined, remove the tool from the resource configuration.
			 * If the rcbs tool was not disabled before now, indicate that a rebuild will be needed.
			 * Set the rcbsApplicability in the resource configuration to "disabled" by default.
			 * Update the field values.
			 */
			resConfig = resParent.getCurrentResourceConfig();
			ITool [] tools = resConfig.getTools();
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				if (tool.getCustomBuildStep() && !tool.isExtensionElement()) {
					resConfig.removeTool(tool);
					break;
				}
			}

			/*
			 * If the rcbs tool was not disabled, it will be after restoring defaults.
			 * This transition implies a rebuild is needed.
			 */
			if(resConfig.getRcbsApplicability() != IResourceConfiguration.KIND_DISABLE_RCBS_TOOL){
				resConfig.getParent().setRebuildState(true);
			}
			resConfig.setRcbsApplicability(IResourceConfiguration.KIND_DISABLE_RCBS_TOOL);
			setValues();
			setDirty(false);
			}
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(IProgressMonitor)
		 */
		public void performApply(IProgressMonitor monitor) throws CoreException {
			IResourceConfiguration resConfig;
			boolean foundRcbsTool = false;
			boolean rebuildNeeded = false;
			boolean rcbsStillDisabledSoNoRebuild = false;
			int idx;
			
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
			
			resBuildInputs = buildInputs.getText().trim();
			resBuildOutputs = buildOutputs.getText().trim();
			resBuildCommand = buildCommand.getText().trim();
			resBuildAnnouncement = buildDescription.getText().trim();

			resConfig = resParent.getCurrentResourceConfig();
			ITool [] tools = resConfig.getTools();
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				if (tool.getCustomBuildStep() && !tool.isExtensionElement()) {
					tool.getInputTypes()[0].getAdditionalInputs()[0].setPaths(resBuildInputs);
					tool.getOutputTypes()[0].setOutputNames(resBuildOutputs);
					tool.setToolCommand(resBuildCommand);
					tool.setAnnouncement(resBuildAnnouncement);
					if (tool.isDirty()) {
						rebuildNeeded = true;
					}
					foundRcbsTool = true;
					break;
				}
			}
			if(!foundRcbsTool) {
				ITool rcbsTool;
				IInputType rcbsToolInputType;
				IAdditionalInput rcbsToolInputTypeAdditionalInput;
				IOutputType rcbsToolOutputType;

				rcbsTool = resConfig.createTool(null,rcbsToolId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolName,false);	//$NON-NLS-1$
				rcbsToolInputType = rcbsTool.createInputType(null,rcbsToolInputTypeId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolInputTypeName,false);	//$NON-NLS-1$
				rcbsToolInputTypeAdditionalInput = rcbsToolInputType.createAdditionalInput(resBuildInputs);
				rcbsToolInputTypeAdditionalInput.setKind(IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY);
				rcbsToolOutputType = rcbsTool.createOutputType(null,rcbsToolOutputTypeId + "." + ManagedBuildManager.getRandomNumber(),rcbsToolOutputTypeName,false);	//$NON-NLS-1$
				rcbsToolOutputType.setOutputNames(resBuildOutputs);
				rcbsTool.setCustomBuildStep(true);
				rcbsTool.setToolCommand(resBuildCommand);
				rcbsTool.setAnnouncement(resBuildAnnouncement);
				rebuildNeeded = true;
			}

			/*
			 * Get the state of the rcbs applicability selector and set the rcbsApplicability attribute in the
			 * resource configuration.
			 */
			idx = rcbsApplicabilitySelector.getSelectionIndex();
			if(idx ==  rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_AFTER))) {
				resConfig.setRcbsApplicability(IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AFTER);
			} else
			if(idx ==  rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_BEFORE))) {
				resConfig.setRcbsApplicability(IResourceConfiguration.KIND_APPLY_RCBS_TOOL_BEFORE);
			} else
			if (idx == rcbsApplicabilitySelector.indexOf(ManagedBuilderUIMessages.getResourceString(RCBS_DISABLE))) {
				/*
				 * If the rcbs tool was disabled and will remain disabled, no rebuild is required.
				 */
				if(resConfig.getRcbsApplicability() == IResourceConfiguration.KIND_DISABLE_RCBS_TOOL){
					rcbsStillDisabledSoNoRebuild = true;
				}
				resConfig.setRcbsApplicability(IResourceConfiguration.KIND_DISABLE_RCBS_TOOL);
			} else {
				resConfig.setRcbsApplicability(IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AS_OVERRIDE);
			}
			if (resConfig.isDirty()) {
				rebuildNeeded = true;
			}

			if (rebuildNeeded && !rcbsStillDisabledSoNoRebuild) {
				resConfig.getParent().setRebuildState(true);
			}
			
			setDirty(false);
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

}
