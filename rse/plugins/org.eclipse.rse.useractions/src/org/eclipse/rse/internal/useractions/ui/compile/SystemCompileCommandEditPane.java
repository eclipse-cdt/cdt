package org.eclipse.rse.internal.useractions.ui.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 *******************************************************************************/
import java.util.Vector;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.useractions.ui.ISystemCommandTextAdditionalGUIProvider;
import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.internal.useractions.ui.SystemCommandTextField;
import org.eclipse.rse.internal.useractions.ui.SystemCommandViewerConfiguration;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.internal.useractions.ui.validators.ValidatorCompileCommandLabel;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.shells.ui.view.ISystemCommandTextModifyListener;
import org.eclipse.rse.ui.ISystemMassager;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ISystemValidatorUniqueString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class prompts the user to create or edit the contents of a single 
 * compile command. This edit pane is used in the Work With Compile Commands dialog.
 * <p>
 * So what is the "contract" the edit pane has to fulfill?
 * <ul>
 *  <li>work in "new" or "edit" mode. In the latter case it is given a SystemCompileCommand as input. 
 *      This needs to be switchable on the fly. This is typically automated by use of a "state machine".
 *  <li>give as output a new or updated SystemCompileCommand
 *  <li>allow interested parties to know when the contents have been changed, as they change,
 *        and whether there are errors in those changes
 * </ul>
 * Contractually, here are the methods called by the main page of the new filter wizard:
 * <ul> 
 *   <li>addChangeListener                           ... no need to ever override
 *   <li>setSubSystem                                ... no need to ever override
 *   <li>setCompileCommandValidator                  ... no need to ever override
 *   <li>isComplete                                  ... no need to ever override
 *   <li>createContents                              ... you will typically override
 *   <li>verify                                      ... you will typically override
 *   <li>getInitialFocusControl                      ... you will typically override
 *   <li>getCompileCommand                           ... you will typically override
 *   <li>areFieldsComplete                           ... you will typically override
 * </ul>
 */
public class SystemCompileCommandEditPane implements SelectionListener, ISystemCommandTextAdditionalGUIProvider, ISystemCommandTextModifyListener {
	// inputs
	protected Shell shell;
	protected ISystemCompileCommandEditPaneHoster hoster;
	protected SystemCompileManager compileManager;
	protected SystemCompileCommand inputCompileCommand;
	protected SystemCompileType parentCompileType;
	protected Vector listeners = new Vector();
	protected SystemCmdSubstVarList varList;
	protected boolean newMode = true;
	protected boolean ignoreChanges;
	// default GUI
	protected Label labelLabel;
	protected Text textLabel;
	protected SystemCommandTextField commandField;
	// state
	protected SystemMessage errorMessage;
	protected boolean skipEventFiring;
	protected boolean fromVerify;
	protected boolean caseSensitive;
	protected Control controlInError = null;
	// validators
	protected ISystemValidator cmdLabelValidator;

	/**
	 * Constructor 
	 * @param compileManager - the compile manager owner of this compile command
	 * @param shell - the shell of the wizard or dialog host this
	 * @param owner - the dialog or property page hosting this edit pane
	 * @param caseSensitive - whether the file system is case sensitive for where this compile command will run. Usually from isCaseSensitive() of a subsystem factory.
	 */
	public SystemCompileCommandEditPane(SystemCompileManager compileManager, Shell shell, ISystemCompileCommandEditPaneHoster owner, boolean caseSensitive) {
		super();
		this.compileManager = compileManager;
		this.shell = shell;
		this.caseSensitive = caseSensitive;
		this.commandField = new SystemCommandTextField(getCommandTextViewerConfiguration());
		this.commandField.setSubstitutionVariableList(compileManager.getSubstitutionVariableList());
		this.hoster = owner;
	}

	// ------------------------------	
	// HELPER METHODS...
	// ------------------------------
	/**
	 * Return the shell given us in the ctor
	 */
	protected Shell getShell() {
		return shell;
	}

	/**
	 * Return the input compile command as given us in setCompileCommand
	 */
	protected SystemCompileCommand getInputCompileCommand() {
		return inputCompileCommand;
	}

	/**
	 * For subclasses: return the input compile manager
	 */
	protected SystemCompileManager getCompileManager() {
		return compileManager;
	}

	/**
	 * For subclasses within the subsystem factory framework: return the system connection
	 *  within which this dialog was launched.
	 */
	protected IHost getSystemConnection() {
		return compileManager.getSystemConnection();
	}

	// ------------------------------	
	// CONFIGURATION/INPUT METHODS...
	// ------------------------------	
	/**
	 * Set the validator to use for the compile command. By default, ValidatorCompileCommandLabel is used.
	 */
	public void setCompileLabelValidator(ISystemValidator validator) {
		this.cmdLabelValidator = validator;
	}

	/**
	 * Turn on ignore changes mode. Subclasses typically can just query the inherited
	 *  field ignoreChanges, unless they need to set the ignoreChanges mode in their 
	 *  own composite widgets, in which case they can override and intercept this.
	 */
	protected void setIgnoreChanges(boolean ignoreChanges) {
		this.ignoreChanges = ignoreChanges;
		commandField.setIgnoreChanges(ignoreChanges);
	}

	/**
	 * Identify a listener interested in any changes made to the filter string,
	 * as they happen
	 */
	public void addChangeListener(ISystemCompileCommandEditPaneListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a listener interested in any changes made to the filter string,
	 * as they happen
	 */
	public void removeChangeListener(ISystemCompileCommandEditPaneListener l) {
		listeners.remove(l);
	}

	/**
	 * Set the action command validator. This is called per keystroke as
	 *  the user types the command.
	 */
	public void setCommandValidator(ISystemValidator validator) {
		commandField.setCommandValidator(validator);
	}

	/**
	 * Set the action command massager. This is called before saving the 
	 *  command to the persistent store, to allow for massaging what the
	 *  user typed, such as doing intelligent uppercasing.
	 */
	public void setCommandMassager(ISystemMassager massager) {
		commandField.setCommandMassager(massager);
	}

	/**
	 * Set the substitution variable list that Insert Variable will use.
	 */
	public void setSubstitutionVariableList(SystemCmdSubstVarList varList) {
		commandField.setSubstitutionVariableList(varList);
	}

	/**
	 * For child classes to return their own subclasses of the default configurator
	 * used to enable proposal support in the command entry field.
	 */
	protected SystemCommandViewerConfiguration getCommandTextViewerConfiguration() {
		return new SystemCommandViewerConfiguration();
	}

	/**
	 * For child classes (such as iSeries IFS) that need to dynamically change the command
	 *  entry field configuration, on the fly.
	 */
	protected void setCommandTextViewerConfiguration(SystemCommandViewerConfiguration cmdAssistant) {
		commandField.setCommandTextViewerConfiguration(cmdAssistant);
	}

	// ------------------------------	
	// LIFECYCLE METHODS...
	// ------------------------------	
	/**
	 * Set the input filter string, in edit mode. 
	 * Or pass null if reseting to new mode.
	 */
	public void setCompileCommand(SystemCompileType parentCompileType, SystemCompileCommand compileCommand) {
		this.inputCompileCommand = compileCommand;
		//System.out.println("inside setCompileCommand: input null? " + (compileCommand==null));
		this.parentCompileType = parentCompileType;
		if ((parentCompileType != null) && (cmdLabelValidator instanceof ISystemValidatorUniqueString)) {
			Vector existingLabels = parentCompileType.getExistingLabels();
			if (compileCommand != null) existingLabels.removeElement(compileCommand.getLabel());
			((ISystemValidatorUniqueString) cmdLabelValidator).setExistingNamesList(existingLabels);
		}
		newMode = (compileCommand == null);
		setIgnoreChanges(true);
		resetFields();
		if (compileCommand != null) doInitializeFields();
		enableExtraButtons();
		if (newMode)
			resetExtraButtonsForNewMode();
		else
			resetExtraButtons(compileCommand);
		setIgnoreChanges(false);
	}

	/**
	 * Save all pending changes. Called by dialog when user Presses Apply.
	 * @return new or updated compile command object. Caller must call writeToDisk() on the parent SystemCompileProfile object 
	 */
	public SystemCompileCommand saveChanges() {
		String cmdLabel = textLabel.getText().trim();
		if (cmdLabel.length() == 0) return null;
		String cmdString = commandField.getMassagedCommandText();
		if (cmdString.length() == 0) return null;
		cmdString = preSaveMassage(cmdString);
		SystemCompileCommand currentCmd = inputCompileCommand;
		if (currentCmd == null) // new mode? Must create the new compile command object
		{
			currentCmd = new SystemCompileCommand(parentCompileType);
			currentCmd.setDefaultString(cmdString);
			currentCmd.setIsUserSupplied();
		} else if (commandField.getCommandMassager() != null) {
			setIgnoreChanges(true); // disable modify listeners
			setCommandText(cmdString);
			setIgnoreChanges(false); // re-enable modify listeners
		}
		if (!caseSensitive) {
			//cmdLabel = cmdLabel.toUpperCase(); I0 decision not to do this anymore
			//cmdString = cmdString.toUpperCase(); we use a massager now
		}
		currentCmd.setLabel(cmdLabel);
		currentCmd.setCurrentString(cmdString);
		processExtraButtonsChanges(currentCmd); // allow subclasses to save their extra data
		/*
		 String option = null;				
		 if (yesPromptButton.getSelection() && noPromptButton.getSelection())
		 option = ISystemCompileXMLConstants.MENU_BOTH_VALUE;
		 else if (yesPromptButton.getSelection() && !noPromptButton.getSelection())
		 option = ISystemCompileXMLConstants.MENU_PROMPTABLE_VALUE;
		 else if (!yesPromptButton.getSelection() && noPromptButton.getSelection())	
		 option = ISystemCompileXMLConstants.MENU_NON_PROMPTABLE_VALUE;
		 else
		 option = ISystemCompileXMLConstants.MENU_NONE_VALUE;
		 currentCmd.setMenuOption(option);
		 */
		return currentCmd;
	}

	/**
	 * Opportunity for subclasses to perform any additional massaging of the 
	 *  user-entered command string, just prior to saving it.
	 */
	protected String preSaveMassage(String commandString) {
		return commandString;
	}

	/**
	 * In the Work With dialog, this edit pane is shown on the right side, beside
	 *  the compile command selection list. Above it is a label, that shows something
	 *  like "Selected Compile Command" in edit mode, or "New Compile Command" in new mode.
	 * <p>
	 * This method gives subclasses the opportunity to specify unique values for this label.
	 * In addition to setting the text, the tooltip text should also be set.
	 */
	public void configureHeadingLabel(Label label) {
		if (!newMode) {
			label.setText(SystemUDAResources.RESID_WWCOMPCMDS_EDITCMD_LABEL);
			label.setToolTipText(SystemUDAResources.RESID_WWCOMPCMDS_EDITCMD_TOOLTIP);
		} else {
			label.setText(SystemUDAResources.RESID_WWCOMPCMDS_NEWCMD_LABEL);
			label.setToolTipText(SystemUDAResources.RESID_WWCOMPCMDS_NEWCMD_TOOLTIP);
		}
	}

	/**
	 * Populate the pane with the GUI widgets
	 * @param parent of the pane
	 * @return Control
	 */
	public Control createContents(Composite parent) {
		if (cmdLabelValidator == null) cmdLabelValidator = new ValidatorCompileCommandLabel();
		if (cmdLabelValidator instanceof ISystemValidatorUniqueString) ((ISystemValidatorUniqueString) cmdLabelValidator).setCaseSensitive(caseSensitive);
		// Inner composite
		int nbrColumns = 3;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		((GridLayout) composite_prompts.getLayout()).marginWidth = 0;
		// COMPILE LABEL PROMPT
		textLabel = SystemWidgetHelpers.createLabeledTextField(composite_prompts, null, getCompileCommandLabel(), getCompileCommandTooltip());
		labelLabel = SystemWidgetHelpers.getLastLabel();
		textLabel.setTextLimit(cmdLabelValidator.getMaximumNameLength());
		((GridData) textLabel.getLayoutData()).horizontalSpan = nbrColumns - 1;
		// COMPILE COMMAND PROMPT
		/*
		 textString = SystemWidgetHelpers.createLabeledTextField(composite_prompts,null,rb, getCompileCommandPromptRBKey());
		 labelString = SystemWidgetHelpers.getLastLabel();
		 //textString.setTextLimit(1000);
		 ((GridData)textString.getLayoutData()).widthHint=300;
		 */
		commandField.createContents(composite_prompts, nbrColumns, this);
		resetFields();
		doInitializeFields();
		// add keystroke listeners...
		textLabel.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateLabelInput();
			}
		});
		commandField.addModifyListener(this);
		return composite_prompts;
	}

	/**
	 * Return the control to recieve initial focus. Should be overridden if you override createContents
	 */
	public Control getInitialFocusControl() {
		if (textLabel.isEnabled())
			return textLabel;
		else
			return commandField.getCommandWidget();
	}

	/**
	 * Overridable entry point for subclasses that wish to put something to the right of the "Command:" label
	 * From interface ISystemCommandTextAdditionalGUIProvider.
	 * @return true if something entered to take up the available columns, false otherwise (will be padded)
	 */
	public boolean createCommandLabelLineControls(Composite parent, int availableColumns) {
		return false;
	}

	/**
	 * Create additional buttons, to go under command prompt.
	 * Overridable.
	 * From interface ISystemCommandTextAdditionalGUIProvider.
	 * @return true if something entered to take up the available columns, false otherwise (will be padded)
	 */
	public boolean createExtraButtons(Composite parent, int availableColumns) {
		return false;
	}

	/**
	 * Enable/disable extra buttons added by subclass.
	 * Called when state changes 
	 * Overridable
	 */
	protected void enableExtraButtons() {
	}

	/**
	 * Overridable method for resetting GUI in subclass-supplied additional GUI,
	 *  when in "new" mode
	 */
	protected void resetExtraButtonsForNewMode() {
	}

	/**
	 * Overridable method for resetting GUI in subclass-supplied additional GUI,
	 *  when in "edit" mode
	 */
	protected void resetExtraButtons(SystemCompileCommand originalCmd) {
	}

	/**
	 * Overridable method for saving data in subclass-supplied additional GUI.
	 */
	protected void processExtraButtonsChanges(SystemCompileCommand currentCmd) {
	}

	protected String getCompileCommandLabel() {
		return SystemUDAResources.RESID_WWCOMPCMDS_CMDLABEL_LABEL;
	}

	protected String getCompileCommandTooltip() {
		return SystemUDAResources.RESID_WWCOMPCMDS_CMDLABEL_TOOLTIP;
	}

	protected String getCompileCommandPromptLabel() {
		return SystemUDAResources.RESID_WWCOMPCMDS_CMD_LABEL;
	}

	protected String getCompileCommandPromptTooltip() {
		return SystemUDAResources.RESID_WWCOMPCMDS_CMD_TOOLTIP;
	}

	/**
	 * Initialize the input fields based on the inputCompileCommand, and perhaps subsystem.
	 * This can be called before createContents, so test for null widgets first!
	 * Prior to this being called, resetFields is called to set the initial default state prior to input
	 */
	protected void doInitializeFields() {
		//System.out.println("inside doInitializeFields: textString null? " + (commandField==null) + " input null? " + (inputCompileCommand==null));
		if (commandField == null) return; // do nothing
		if (inputCompileCommand != null) {
			textLabel.setText(inputCompileCommand.getLabel());
			/* if (!inputCompileCommand.isLabelEditable()) { // ibm or vendor supplied?
			 textLabel.setEnabled(false);
			 } */
			textLabel.setEnabled(inputCompileCommand.isLabelEditable());
			commandField.setCommandText(inputCompileCommand.getCurrentString());
			/* if (!inputCompileCommand.isCommandStringEditable()) {
			 System.out.println("Compile command not editable");
			 commandField.enableCommandWidget(false);
			 } */
			commandField.enableCommandWidget(inputCompileCommand.isCommandStringEditable());
		}
	}

	/**
	 * This is called in the work with compile commands dialog when the user selects "new", or selects another command.
	 * You must override this if you override createContents. Be sure to test if the contents have even been created yet!
	 */
	protected void resetFields() {
		textLabel.setEnabled(true);
		textLabel.setText(""); //$NON-NLS-1$
		commandField.setCommandText(""); //$NON-NLS-1$
		errorMessage = null;
	}

	/**
	 * Do not override. Instead, override areFieldsComplete().
	 * <p>
	 * This is called by the dialog when first shown, to decide if the default information
	 *  is complete enough to enable finish. It doesn't do validation, that will be done when
	 *  finish is pressed.
	 */
	public boolean isComplete() {
		boolean complete = true;
		if (errorMessage != null) // pending errors?
			complete = false; // clearly not complete.
		else
			complete = areFieldsComplete();
		return complete;
	}

	/**
	 * Must be overridden if createContents is overridden.
	 * <p>
	 * This is called by the isComplete, to decide if the default information
	 *  is complete enough to enable finish. It doesn't do validation, that will be done when
	 *  finish is pressed.
	 */
	protected boolean areFieldsComplete() {
		if (commandField == null)
			return false;
		else
			return (textLabel.getText().trim().length() > 0) && (commandField.getCommandText().length() > 0);
	}

	/**
	 * Are errors pending? Used in dialog to prevent changing the compile command selection
	 */
	public boolean areErrorsPending() {
		return (errorMessage != null);
	}

	/**
	 * Clear any pending errors. Called when Revert pressed.
	 */
	public void clearErrorMessage() {
		errorMessage = null;
	}

	/**
	 * Callback from SystemCommandTextField when the user modifies the command.
	 * @param cmdText - current contents of the field
	 * @param errorMessage - potential error detected by the default validator
	 */
	public void commandModified(String cmdText, SystemMessage errorMessage) {
		this.errorMessage = errorMessage;
		processCommandTextChange(cmdText, (errorMessage != null));
		if (!fromVerify) fireChangeEvent(errorMessage);
	}

	/**
	 * Method called as user types into the command field
	 * Encapsulated out so that it can be called from various types of listeners.
	 * Further, it is easily overridden
	 */
	protected void processCommandTextChange(String newText, boolean hasError) {
	}

	/**
	 * Set the command text
	 */
	protected void setCommandText(String text) {
		commandField.setCommandText(text);
	}

	/**
	 * Get the command text as is, no massaging done.
	 */
	protected String getCommandText() {
		return commandField.getCommandText();
	}

	// ------------------------------	
	// PRIVATE METHODS
	// ------------------------------
	/**
	 * Fire an event to all registered listeners, that the user has changed the 
	 *  compile command. Include the error message, if in error, so it can be displayed to the user.
	 * <p>
	 * Because this is used to enable/disable the Next and Finish buttons it is important
	 *  to call it when asked to do verification, even if nothing has changed.
	 * <p>
	 * It is more efficient, however, to defer the event firing during a full verification
	 *  until after the last widget has been verified. To enable this, set the protected
	 *  variable "skipEventFiring" to true at the top of your verify event, then to "false"
	 *  at the end. Then do fireChangeEvent(errorMessage);
	 */
	protected void fireChangeEvent(SystemMessage error) {
		if (skipEventFiring) return;
		for (int idx = 0; idx < listeners.size(); idx++) {
			ISystemCompileCommandEditPaneListener l = (ISystemCompileCommandEditPaneListener) listeners.elementAt(idx);
			l.compileCommandChanged(error);
		}
	}

	// ---------------------------------------------
	// METHODS FOR VERIFYING INPUT PER KEYSTROKE ...
	// ---------------------------------------------
	/**
	 * Validates compile command label as entered so far in the text field.
	 * Not called if you override createContents() and verify()
	 */
	protected SystemMessage validateLabelInput() {
		if (ignoreChanges) return errorMessage;
		errorMessage = cmdLabelValidator.validate(textLabel.getText().trim());
		//if ((errorMessage == null) && !fromVerify)
		//  errorMessage = validate(true, false);
		if (!fromVerify) fireChangeEvent(errorMessage);
		return errorMessage;
	}

	/**
	 * Validates compile command string as entered so far in the text field.
	 * Not called if you override createContents() and verify()
	 */
	protected SystemMessage validateStringInput() {
		if (ignoreChanges) return errorMessage;
		errorMessage = commandField.validateCommand();
		if (!fromVerify) fireChangeEvent(errorMessage);
		return errorMessage;
	}

	/**
	 * Does complete verification of input fields. This has to handle being called
	 *  from a particular validator method or from verify.
	 *
	 * @return error message if there is one, else null if ok
	 */
	public SystemMessage validate(boolean skipLabel, boolean skipString) {
		errorMessage = null;
		controlInError = null;
		if (!skipLabel) {
			errorMessage = validateLabelInput();
			if (errorMessage != null) controlInError = textLabel;
		}
		if ((errorMessage == null) && !skipString) {
			errorMessage = validateStringInput();
			if (errorMessage != null) controlInError = commandField.getCommandWidget();
		}
		return errorMessage;
	}

	// ---------------------------------
	// METHODS FOR VERIFICATION... 
	// ---------------------------------
	/**
	 * Does complete verification of input fields. If this 
	 * method returns null, there are no errors and the dialog or wizard can close; 
	 *
	 * @return error message if there is one, else null if ok
	 */
	public SystemMessage verify() {
		fromVerify = true;
		errorMessage = validate(false, false);
		if (errorMessage != null) {
			if (controlInError != null) controlInError.setFocus();
		}
		fromVerify = false;
		fireChangeEvent(errorMessage);
		return errorMessage;
	}

	// ------------------	
	// EVENT LISTENERS...
	// ------------------
	/**
	 * User has selected something
	 */
	public void widgetSelected(SelectionEvent event) {
	}

	/**
	 * User has selected something via enter/dbl-click
	 */
	public void widgetDefaultSelected(SelectionEvent event) {
	}

	// ---------------
	// HELPER METHODS
	// ---------------  
	/**
	 * Add a separator line. This is a physically visible line.
	 */
	protected void addSeparatorLine(Composite parent, int nbrColumns) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		separator.setLayoutData(data);
	}

	/**
	 * Add a spacer line
	 */
	protected void addFillerLine(Composite parent, int nbrColumns) {
		Label filler = new Label(parent, SWT.LEFT);
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		filler.setLayoutData(data);
	}

	/**
	 * Add a spacer line that grows in height to absorb extra space
	 */
	protected void addGrowableFillerLine(Composite parent, int nbrColumns) {
		Label filler = new Label(parent, SWT.LEFT);
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		filler.setLayoutData(data);
	}
}
