/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import java.util.Vector;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.IUserActionsModelChangeEvents;
import org.eclipse.rse.internal.useractions.ui.ISystemCommandTextAdditionalGUIProvider;
import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.internal.useractions.ui.SystemCommandTextField;
import org.eclipse.rse.internal.useractions.ui.SystemCommandViewerConfiguration;
import org.eclipse.rse.internal.useractions.ui.validators.ValidatorUserActionComment;
import org.eclipse.rse.internal.useractions.ui.validators.ValidatorUserActionName;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.shells.ui.view.ISystemCommandTextModifyListener;
import org.eclipse.rse.ui.ISystemMassager;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ISystemValidatorUniqueString;
import org.eclipse.rse.ui.widgets.SystemEditPaneStateMachine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This is the default edit pane shown on the right, when 
 *  an action is selected on the left. It is also used when
 *  "new..." is selected on the left, so there are three states
 *  for this class: new, edit and not-set.
 */
public class SystemUDActionEditPane implements SelectionListener, // for the checkboxes
		ISelectionChangedListener, Listener, ISystemUDSelectTypeListener, ISystemCommandTextAdditionalGUIProvider, ISystemCommandTextModifyListener, MouseListener, KeyListener {
	// gui widgets
	protected Text textName, textComment;
	//protected SourceViewer textCommand;
	protected SystemCommandTextField commandField;
	//protected SystemUDASourceViewerConfiguration sourceViewerConfiguration;
	protected Button promptCB, refreshCB, showCB, singleSelCB, collectCB;
	protected Button resetButton;
	//protected Button insertVariableButton;
	protected Label typesSeparator;
	protected SystemUDSelectTypesForm selectTypesForm;
	protected boolean menuListenerAdded;
	protected String testActionName;
	// actions for popup of command SourceViewer
	//	private Map fGlobalActions= new HashMap(10);
	//	private List fSelectionActions = new ArrayList(3);		
	// Current selection not valid if errorMessage not null
	protected SystemMessage errorMessage;
	protected ISubSystem subsystem;
	protected SystemUDActionSubsystem udaActionSubsys;
	protected ISubSystemConfiguration subsystemFactory;
	protected ISystemProfile profile;
	public SystemUDActionElement currentAction;
	protected ISystemValidator nameValidator;
	//protected ISystemValidator cmdValidator;
	protected ISystemValidator cmtValidator;
	//protected ISystemMassager cmdMassager;
	private boolean isEnabled = false;
	private boolean recursiveCall = false;
	private boolean ignoreChanges = false;
	// entry fields enabled from last selection
	private NameModifyListener nameML = new NameModifyListener();
	private NameFocusListener nameFL = new NameFocusListener();
	private CommentModifyListener commentML = new CommentModifyListener();
	//protected SystemWorkWithUDAsDialog parentDialog;
	protected ISystemUDAEditPaneHoster parentDialog;
	protected ISystemUDTreeView treeView;
	// Switch to trigger a tree view refresh when the item's name is changed.
	private boolean nameChanged = false;
	protected boolean newMode = false;
	private int newModeDomain = -1;
	private SystemUDTreeViewNewItem newModeNewItem;
	private Vector EMPTY_VECTOR = new Vector();
	// state
	private boolean grabFocus = true; // grab the focus away from the tree when processing selection events
	// state machine
	private SystemEditPaneStateMachine stateMachine;

	/**
	 * Constructor when we have a subsystem or a subsystemconfiguration/profile pair.
	 */
	public SystemUDActionEditPane(SystemUDActionSubsystem udaActionSubsys, ISystemUDAEditPaneHoster parent, ISystemUDTreeView tv) {
		super();
		this.udaActionSubsys = udaActionSubsys;
		this.subsystem = udaActionSubsys.getSubsystem();
		this.subsystemFactory = subsystem.getSubSystemConfiguration();
		this.profile = (profile == null) ? subsystem.getSystemProfile() : profile;
		parentDialog = parent;
		treeView = tv;
		commandField = new SystemCommandTextField(getCommandTextViewerConfiguration());
		testActionName = getUDActionSubsystem().getTestActionName().toLowerCase();
	}

	/**
	 * Return the user defined action subsystem
	 */
	protected SystemUDActionSubsystem getUDActionSubsystem() {
		return udaActionSubsys;
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

	/**
	 * For child classes to access current subsystem. If null, use getSubSystemFactory and getProfile
	 */
	protected ISubSystem getSubSystem() {
		return subsystem;
	}

	/**
	 * For child classes to access current profile
	 */
	protected ISystemProfile getProfile() {
		return profile;
	}

	/**
	 * For child classes to access current shell
	 */
	protected Shell getShell() {
		return parentDialog.getShell();
	}

	// ------------------------------	
	// CONFIGURATION/INPUT METHODS...
	// ------------------------------
	/**
	 * Set the state machine.
	 * Called by the UDA dialog
	 */
	public void setStateMachine(SystemEditPaneStateMachine sm) {
		this.stateMachine = sm;
	}

	/**
	 * Set the action name validator
	 */
	public void setNameValidator(ISystemValidator validator) {
		this.nameValidator = validator;
	}

	/**
	 * Set the action comment validator
	 */
	public void setCommentValidator(ISystemValidator validator) {
		this.cmtValidator = validator;
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

	// ------------------------------	
	// DATA EXTRACTION METHODS
	// ------------------------------
	// ------------------------------	
	// EXTERNAL LIFECYCLE METHODS...
	// ------------------------------
	/**
	 * Method createContents.
	 * @param parent parent of this pane
	 * @return Control
	 */
	public Control createContents(Composite parent) {
		if (nameValidator == null) nameValidator = new ValidatorUserActionName();
		if (cmtValidator == null) cmtValidator = new ValidatorUserActionComment();
		//Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 1); // why???
		// Inner composite
		final int nbrColumns = 3; // 2
		Composite comp = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		// Action name
		textName = SystemWidgetHelpers.createLabeledTextField(comp, null, SystemUDAResources.RESID_UDA_NAME_LABEL, SystemUDAResources.RESID_UDA_NAME_TOOLTIP);
		((GridData) textName.getLayoutData()).horizontalSpan = nbrColumns - 1;
		// Comment
		textComment = SystemWidgetHelpers.createLabeledTextField(comp, null, SystemUDAResources.RESID_UDA_COMMENT_LABEL, SystemUDAResources.RESID_UDA_COMMENT_TOOLTIP);
		((GridData) textComment.getLayoutData()).horizontalSpan = nbrColumns - 1;
		// Re-usable command field...
		commandField.setMRI(SystemUDAResources.RESID_UDA_COMMAND_LABEL, SystemUDAResources.RESID_UDA_COMMAND_TOOLTIP, SystemUDAResources.RESID_UDA_INSERTVAR_BUTTON_LABEL,
				SystemUDAResources.RESID_UDA_INSERTVAR_BUTTON_TOOLTIP);
		commandField.createContents(comp, nbrColumns, this);
		// old way...
		/*
		 Label labelCommand = SystemWidgetHelpers.createLabel(comp,rb,RESID_UDA_COMMAND_ROOT);
		 String s = labelCommand.getText();
		 if (!s.endsWith(":"))
		 labelCommand.setText(s+":");
		 if (!createCommandLabelLineControls(comp, nbrColumns-1))
		 ((GridData)labelCommand.getLayoutData()).horizontalSpan = nbrColumns;

		 int cmdSpan = nbrColumns;
		 textCommand = createEditor(comp, cmdSpan, sourceViewerConfiguration);
		 textCommand.getControl().setToolTipText(rb.getString(RESID_UDA_COMMAND_ROOT_TOOLTIP);        
		 // Insert Variable... button
		 insertVariableButton = SystemWidgetHelpers.createPushButton(comp, null, rb, ISystemConstants.RESID_UDA_INSERTVAR_BUTTON_ROOT);
		 // SUBCLASS-SUPPLIED BUTTONS
		 if (!createExtraButtons(comp, nbrColumns-1))
		 addFillerLine(comp, nbrColumns-1);
		 */
		//Label filler = SystemWidgetHelpers.createLabel(comp, "");
		//((GridData)filler.getLayoutData()).horizontalSpan = nbrColumns;
		// SEPARATOR BEFORE OPTIONS
		/*
		 SystemWidgetHelpers.createLabel(comp, "");
		 addSeparatorLine(comp, 1);
		 SystemWidgetHelpers.createLabel(comp, "");
		 */
		addFillerLine(comp, nbrColumns);
		// OPTION CHECKBOXES
		Composite options_composite = SystemWidgetHelpers.createTightComposite(comp, 3);
		((GridData) options_composite.getLayoutData()).horizontalSpan = nbrColumns;
		// Prompt before
		promptCB = SystemWidgetHelpers.createCheckBox(options_composite, 1, null, SystemUDAResources.RESID_UDA_OPTION_PROMPT_LABEL, SystemUDAResources.RESID_UDA_OPTION_PROMPT_TOOLTIP);
		// Refresh after
		refreshCB = SystemWidgetHelpers.createCheckBox(options_composite, 1, null, SystemUDAResources.RESID_UDA_OPTION_REFRESH_LABEL, SystemUDAResources.RESID_UDA_OPTION_REFRESH_TOOLTIP);
		// Show action
		showCB = SystemWidgetHelpers.createCheckBox(options_composite, 1, null, SystemUDAResources.RESID_UDA_OPTION_SHOW_LABEL, SystemUDAResources.RESID_UDA_OPTION_SHOW_TOOLTIP);
		// Single selection only
		singleSelCB = SystemWidgetHelpers.createCheckBox(options_composite, 1, null, SystemUDAResources.RESID_UDA_OPTION_SINGLESEL_LABEL, SystemUDAResources.RESID_UDA_OPTION_SINGLESEL_TOOLTIP);
		// Collect names of selected object into delimited string
		collectCB = SystemWidgetHelpers.createCheckBox(options_composite, 1, null, getInvokeOnceLabel(), getInvokeOnceTooltip());
		// SUBCLASS-SUPPLIED OPTION CHECKBOXES
		Control[] extraOptions = createExtraOptionCheckBoxes(options_composite);
		// SEPARATOR BEFORE TYPES
		if (getUDActionSubsystem().supportsTypes()) {
			//SystemWidgetHelpers.createLabel(comp, "");
			//typesSeparator = addSeparatorLine(comp, 1);
			//SystemWidgetHelpers.createLabel(comp, "");	
			addFillerLine(comp, nbrColumns);
		}
		//Label filler2 = SystemWidgetHelpers.createLabel(comp, "");
		//((GridData)filler2.getLayoutData()).horizontalSpan = nbrColumns;
		// TYPE SELECTION FORM
		if (udaActionSubsys != null)
			selectTypesForm = createSelectTypesForm(parentDialog.getShell(), subsystem, udaActionSubsys);
		else
		{
			// FIXME: Xuan  - selectTypesForm = createSelectTypesForm(parentDialog.getShell(), profile);
		}
		if (selectTypesForm != null) {
			selectTypesForm.createContents(comp, nbrColumns);
		}
		// CONFIGURE THE WIDGETS...
		//resetButton = SystemWidgetHelpers.createPushButton(comp,this,rb,"com.ibm.etools.systems.core.ui.uda.ResetButton.");
		//((GridData)resetButton.getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		textName.setTextLimit(ValidatorUserActionName.MAX_UDANAME_LENGTH);
		textComment.setTextLimit(ValidatorUserActionComment.MAX_UDACMT_LENGTH);
		//textName.setEditable(false);
		//textComment.setEditable(false);		
		promptCB.addSelectionListener(this);
		refreshCB.addSelectionListener(this);
		showCB.addSelectionListener(this);
		singleSelCB.addSelectionListener(this);
		collectCB.addSelectionListener(this);
		if (extraOptions != null) for (int idx = 0; idx < extraOptions.length; idx++)
			if (extraOptions[idx] instanceof Button)
				((Button) extraOptions[idx]).addSelectionListener(this);
			else if (extraOptions[idx] instanceof Combo)
				((Combo) extraOptions[idx]).addSelectionListener(this);
			else if (extraOptions[idx] instanceof org.eclipse.swt.widgets.List) ((org.eclipse.swt.widgets.List) extraOptions[idx]).addSelectionListener(this);
		if (selectTypesForm != null) selectTypesForm.addSelectionListener(this);
		// ??? id ???
		//		SystemWidgetHelpers.setHelp(comp, this, RSEUIPlugin.HELPPREFIX+"cprf0000");	
		textName.addModifyListener(nameML);
		textName.addFocusListener(nameFL);
		textComment.addModifyListener(commentML);
		commandField.addModifyListener(this);
		return comp;
	}

	protected String getInvokeOnceLabel() {
		return SystemUDAResources.RESID_UDA_OPTION_COLLECT_LABEL;
	}

	protected String getInvokeOnceTooltip() {
		return SystemUDAResources.RESID_UDA_OPTION_COLLECT_TOOLTIP;
	}

	/**
	 * Are errors pending? If so, don't allow user to change selection
	 *  or profile or anything!
	 */
	public boolean areErrorsPending() {
		return ((errorMessage != null) && ((currentAction != null) || newMode));
	}

	/**
	 * This is called when user changes their selection in the left-side tree view
	 */
	public void selectionChanged(SelectionChangedEvent se) {
		if (recursiveCall) return; // ignore!
		IStructuredSelection ss = (IStructuredSelection) se.getSelection();
		Object so = ss.getFirstElement();
		if (areErrorsPending()) {
			if (newMode || SystemUDBaseManager.inCurrentTree(currentAction.getElement())) {
				if (!newMode && (so != currentAction))
					treeView.setSelection(new StructuredSelection(currentAction));
				else if (newMode && (so != newModeNewItem)) treeView.setSelection(new StructuredSelection(newModeNewItem));
				return;
			}
		}
		// We need to test for pending changes, and if any are pending, prompt
		// user to continue (and lose changes) or cancel...
		if ((stateMachine != null) && stateMachine.isSaveRequired()) {
			saveData();
			if (newMode) {
				// interesting problem! The save of the new data resulted in a new node,
				//  but this is not visible in the tree view. To make it visible means we
				//  we will lose focus, and this method will be recalled recursively...
				recursiveCall = true;
				treeView.refreshElementParent(currentAction); // show new item in tree view
				recursiveCall = false;
				if (so instanceof SystemUDActionElement) // if user was selecting an action, it might have a new binary address after the refresh
					treeView.selectElement((SystemUDActionElement) so);
				else if (so != null) treeView.setSelection(new StructuredSelection(so)); // restore what user selected
				return; // avoid recursion!
			}
		}
		recursiveCall = false;
		// Clear any page-valid errors remaining from previous selection
		// (Since validation on the new selection is only run if editing
		// changes are made
		errorMessage = null;
		resetPageValidation();
		newMode = ((so instanceof SystemUDTreeViewNewItem) && ((SystemUDTreeViewNewItem) so).isExecutable());
		if (newMode) newModeDomain = ((SystemUDTreeViewNewItem) so).getDomain();
		// Refresh tree view if name changed on last item
		if (nameChanged) {
			nameChanged = false;
			if (currentAction != null) treeView.refresh(currentAction);
		}
		SystemUDActionElement sn = null;
		//temp = null;
		if ((null != so) && (so instanceof SystemUDActionElement)) sn = (SystemUDActionElement) so;
		currentAction = sn;
		setIgnoreChanges(true);
		boolean changeMode = false;
		// entering un-selected mode? we turn invisible for this...
		if (!newMode && ((null == sn) || sn.isDomain())) {
			isEnabled = false;
			textName.setText(""); //$NON-NLS-1$
			textComment.setText(""); //$NON-NLS-1$
			setCommandText(""); //$NON-NLS-1$
			enableExtraButtons(false);
		}
		// entering new or selected mode? we turn visible for this...
		else if (newMode) {
			isEnabled = true;
			SystemCmdSubstVarList varList = null;
			/*
			 if (!newMode) // existing action selected
			 {
			 textName.setText(sn.toString());
			 textComment.setText(sn.getComment());
			 setCommandText(sn.getCommand());

			 promptCB.setSelection(sn.getPrompt());
			 refreshCB.setSelection(sn.getRefresh());
			 showCB.setSelection(sn.getShow());
			 singleSelCB.setSelection(sn.getSingleSelection());
			 collectCB.setSelection( sn.getCollect());
			 //if (singleSelCB.getSelection())
			 collectCB.setEnabled(!singleSelCB.getSelection());
			 resetExtraOptions(sn);
			 
			 varList = getActionSubstVarList(sn.getDomain());
			 //setEditable(!parentDialog.isSelectionVendorSupplied(), parentDialog.getVendorOfSelection()); todo
			 }
			 else // new mode
			 {*/
			textName.setText(""); //$NON-NLS-1$
			textComment.setText(""); //$NON-NLS-1$
			setCommandText(""); //$NON-NLS-1$
			promptCB.setSelection(false);
			refreshCB.setSelection(false);
			showCB.setSelection(true);
			singleSelCB.setSelection(false);
			collectCB.setSelection(false);
			resetExtraOptionsForNewMode();
			if (so != null) {
				varList = getActionSubstVarList(((SystemUDTreeViewNewItem) so).getDomain());
			}
			/*}*/
			// update the substitution variable list
			//sourceViewerConfiguration.setSubstVarList(varList);
			commandField.setSubstitutionVariableList(varList);
			//varList.printDisplayStrings(); // temp. todo - remove            
			enableExtraButtons(true);
		}
		// existing action
		else {
			isEnabled = true;
			changeMode = true;
			setAction(sn);
		}
		setIgnoreChanges(false); // re-enable modify listeners
		if (newMode) {
			//System.out.println("entering New mode");
			stateMachine.setNewMode(); // resets Apply/Reset button status
			newModeNewItem = (SystemUDTreeViewNewItem) so;
			//System.out.println("newModeDomain = " + newModeDomain);
		} else if ((sn == null) || sn.isDomain()) {
			//System.out.println("entering Unset mode");
			stateMachine.setUnsetMode(); // resets Apply/Reset button status		   
		} else {
			//System.out.println("entering Edit mode");
			stateMachine.setEditMode(); // resets Apply/Reset button status		}
		}
		if (isEnabled && (typesSeparator != null)) {
			int domain = getDomain();
			if (domain == -1)
				typesSeparator.setVisible(getUDActionSubsystem().supportsTypes());
			else
				typesSeparator.setVisible(getUDActionSubsystem().supportsTypes(domain));
		}
		if (!changeMode && (selectTypesForm != null)) // already done for change mode
			reConfigureSelectTypesForm(selectTypesForm);
		if (isEnabled && newMode) // we have already done it for new mode
		{
			if (grabFocus) {
				textName.setFocus();
			}
			//if (!wasEnabled)
			if (nameValidator instanceof ISystemValidatorUniqueString) {
				((ISystemValidatorUniqueString) nameValidator).setExistingNamesList(getExistingActionNames());
				/*
				 System.out.println("...got existing names: ");
				 Vector v = getExistingActionNames();
				 for (int idx=0; idx<v.size(); idx++)
				 System.out.print(v.elementAt(idx) + ", ");
				 System.out.println();
				 */
			}
		}
		setPageComplete();
	}

	/**
	 * This is called when to set the input to an existing action
	 */
	public void setAction(SystemUDActionElement action) {
		currentAction = action;
		boolean currentIgnoreState = ignoreChanges;
		setIgnoreChanges(true);
		textName.setText(action.toString());
		textComment.setText(action.getComment());
		setCommandText(action.getCommand());
		promptCB.setSelection(action.getPrompt());
		refreshCB.setSelection(action.getRefresh());
		showCB.setSelection(action.getShow());
		singleSelCB.setSelection(action.getSingleSelection());
		collectCB.setSelection(action.getCollect());
		//if (singleSelCB.getSelection())
		collectCB.setEnabled(!singleSelCB.getSelection());
		resetExtraOptions(action);
		// update the substitution variable list
		commandField.setSubstitutionVariableList(getActionSubstVarList(action.getDomain()));
		setIgnoreChanges(currentIgnoreState); // re-enable modify listeners
		if (selectTypesForm != null) reConfigureSelectTypesForm(selectTypesForm);
		if (grabFocus) textName.setFocus();
		if (nameValidator instanceof ISystemValidatorUniqueString) ((ISystemValidatorUniqueString) nameValidator).setExistingNamesList(getExistingActionNames());
		enableExtraButtons(true);
	}

	/*
	 * Mouse and key listeners for mouse and key events in the tree.  Used
	 * to determine focus for accessibility.
	 */
	/*
	 * Process the mouse down event.
	 */
	public void mouseDown(MouseEvent e) {
		grabFocus = true;
	}

	/*
	 * Process the mouse up event.
	 */
	public void mouseUp(MouseEvent e) {
		grabFocus = true;
	}

	/*
	 * Process the mouse double-click event.
	 */
	public void mouseDoubleClick(MouseEvent e) {
		grabFocus = true;
	}

	/*
	 * Process the key pressed event.
	 */
	public void keyPressed(KeyEvent e) {
		grabFocus = false;
	}

	/*
	 * Process the key released event.
	 */
	public void keyReleased(KeyEvent e) {
		grabFocus = false;
	}

	/**
	 * Return the current domain of the selected node or New item
	 */
	protected int getDomain() {
		if (newMode)
			return newModeDomain;
		else if (currentAction != null)
			return currentAction.getDomain();
		else
			return -1;
	}

	/**
	 * Revert button pressed
	 */
	public void revertPressed() {
		if ((currentAction != null) && !currentAction.isDomain()) {
			textName.setText(currentAction.toString());
			setCommandText(currentAction.getCommand());
			textComment.setText(currentAction.getComment());
			promptCB.setSelection(currentAction.getPrompt());
			refreshCB.setSelection(currentAction.getRefresh());
			showCB.setSelection(currentAction.getShow());
			singleSelCB.setSelection(currentAction.getSingleSelection());
			collectCB.setSelection(currentAction.getCollect());
			collectCB.setEnabled(!singleSelCB.getSelection());
			resetExtraOptions(currentAction);
			if (selectTypesForm != null) selectTypesForm.setTypes(currentAction.getFileTypes());
			if (stateMachine != null) stateMachine.resetPressed();
		} else if (newMode) {
			resetPageValidation();
			setIgnoreChanges(true); // disable modify listeners
			textName.setText(""); //$NON-NLS-1$
			setCommandText(""); //$NON-NLS-1$
			textComment.setText(""); //$NON-NLS-1$
			promptCB.setSelection(false);
			refreshCB.setSelection(false);
			showCB.setSelection(true);
			singleSelCB.setSelection(false);
			collectCB.setSelection(false);
			collectCB.setEnabled(true);
			resetExtraOptionsForNewMode();
			if (selectTypesForm != null) selectTypesForm.resetTypes();
			setIgnoreChanges(false); // re-enable modify listeners
			if (stateMachine != null) stateMachine.resetPressed();
		}
		resetPageValidation(); // defect 45772
		//setPageComplete();
	}

	/**
	 * Process the apply button
	 */
	public void applyPressed() {
		if ((newMode || ((currentAction != null) && !currentAction.isDomain())) && validateInput(true, null)) {
			saveData();
			if (stateMachine != null) stateMachine.applyPressed();
			if (newMode) {
				// Now update tree view to show new item
				recursiveCall = true;
				treeView.refreshElementParent(currentAction);
				recursiveCall = false;
				treeView.selectElement(currentAction);
			} else
				treeView.refresh(currentAction);
		}
		setPageComplete();
	} //apply

	// ------------------------------	
	// OVERRIDABLE METHODS
	// ------------------------------
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
	 * Create the select-types form.
	 * Override if you want to change the mri, but first call super.createSelectTypeForm()!
	 * Or override and return null to not prompt user for file types in your edit pane.
	 * @return the created form, or null if you don't wish to include the GUI for selecting types
	 */
	protected SystemUDSelectTypesForm createSelectTypesForm(Shell shell, ISubSystem subsystem, SystemUDActionSubsystem udaActionSubsys) {
		
		if (udaActionSubsys.supportsTypes())
		{
			  return new SystemUDSelectTypesForm(shell, subsystem, udaActionSubsys.getUDTypeManager());
		}
		return null;
	}

	/**
	 * Re-configure the type-selection form when the state changes.
	 * Important to set the input type lists (master and current selection)
	 * Overridable, but you MUST call super.xxx first!
	 */
	protected void reConfigureSelectTypesForm(SystemUDSelectTypesForm form) {
		SystemUDActionSubsystem udas = getUDActionSubsystem();
		SystemUDTypeManager udtm = udas.getUDTypeManager();
		// Populate the listboxes
		if (currentAction != null) {
			//String domainName = udas.getDomainName(currentAction);
			int domainType = currentAction.getDomain();
			if ((domainType != -1) && !udas.supportsTypes(domainType))
				form.setVisible(false);
			else {
				form.setVisible(true);
				form.setMasterTypes(udtm.getTypeNames(currentAction.getDomain()));
				form.setTypes(currentAction.getFileTypes());
				form.setDomain(domainType);
			}
		} else if (newMode) {
			//form.reset();
			if ((newModeDomain != -1) && !udas.supportsTypes(newModeDomain)) {
				form.setVisible(false);
			} else {
				form.setVisible(true);
				form.setMasterTypes(udtm.getTypeNames(newModeDomain));
				form.resetTypes();
				form.setDomain(newModeDomain);
			}
		} else
			form.reset();
	}

	/**
	 * Return the list of substitutation variables for the given domain type
	 */
	protected SystemCmdSubstVarList getActionSubstVarList(int actionDomainType) {
		SystemCmdSubstVarList varList = getUDActionSubsystem().getActionSubstVarList(actionDomainType);
		//varList.printDisplayStrings();
		return varList;
	}

	/**
	 * Create additional option checkboxes, to go under default options.
	 * Overridable.
	 * @param parent - the options composite to place checkboxes in
	 * @return An array of the widgets created
	 */
	protected Control[] createExtraOptionCheckBoxes(Composite parent) {
		return null;
	}

	/**
	 * Enable/disable extra buttons added by subclass.
	 * Called when state changes 
	 * Overridable
	 */
	protected void enableExtraButtons(boolean b) {
	}

	/**
	 * Overridable method for saving data in subclass-supplied additional options
	 */
	protected void processExtraOptionsChanges(SystemUDActionElement currentAction) {
	}

	/**
	 * Overridable method for resetting options in subclass-supplied additional options,
	 *  when in "edit" mode
	 */
	protected void resetExtraOptions(SystemUDActionElement originalAction) {
	}

	/**
	 * Overridable method for resetting options in subclass-supplied additional options,
	 *  when in "new" mode
	 */
	protected void resetExtraOptionsForNewMode() {
	}

	// ------------------------------	
	// INTERNAL LISTENER METHODS...
	// ------------------------------
	/**
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
	 */
	public void handleEvent(Event arg0) {
	}

	/**
	 * SelectionListener Interface:
	 * For the checkboxes
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * SelectionListener Interface:
	 * For the checkboxes
	 */
	public void widgetSelected(SelectionEvent e) {
		if (!newMode && (currentAction == null)) return;
		Object source = e.getSource();
		if (source == singleSelCB) {
			if (singleSelCB.getSelection()) {
				collectCB.setSelection(false);
				collectCB.setEnabled(false);
			} else
				collectCB.setEnabled(true);
		}
		if ((source instanceof Button) || (source instanceof Combo)) {
			//errorMessage = null;
			//parentDialog.clearErrorMessage(); 
			validateInput(false, null);
		}
		setChangesMade();
		setPageComplete();
	}

	/**
	 * Scenario:  User edits an item, producing a syntax error.
	 *  (eg. clear action name field)  Gets error msg, OK button disabled.
	 * then changes selection to another item.
	 * Current Problem: Error msg stays, OK remains disabled, until
	 * they edit a field.  (ValidateInput isnt re-reun until
	 * another field is changed.)
	 * Solution:  When changing selection, reset the errorMessage and
	 * page-valid status, since we are not saving invalid field changes .
	 * Need to add/remove listeners around selection changes, so
	 * I can set text fields without triggering modify  event.
	 * So listeners implemented as internal classes
	 */
	private class NameModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			if (ignoreChanges) return;
			setChangesMade();
			errorMessage = validateName(textName.getText().trim());
			if (errorMessage != null) {
				parentDialog.setErrorMessage(errorMessage);
				setPageComplete();
			} else {
				validateInput(false, textName);
				nameChanged = true;
			}
		}
	} //class

	/**
	 * Focus listener for name field
	 */
	private class NameFocusListener implements FocusListener {
		public void focusLost(FocusEvent event) {
			if (ignoreChanges) return;
			String name = textName.getText().trim().toLowerCase();
			if (name.startsWith(testActionName) && (textComment.getText().trim().length() == 0) && (getCommandText().length() == 0)) {
				textComment.setText(getUDActionSubsystem().getTestFilePath());
				setCommandText(getUDActionSubsystem().getTestFileName());
			}
		}

		public void focusGained(FocusEvent event) {
		}
	}

	/**
	 * Modify listener for comment field
	 */
	private class CommentModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			if (ignoreChanges) return;
			setChangesMade();
			errorMessage = validateComment(textComment.getText().trim());
			if (errorMessage != null) {
				parentDialog.setErrorMessage(errorMessage);
				setPageComplete();
			} else
				validateInput(false, textComment);
		}
	} //class

	/**
	 * From ISystemUDSelectTypeListener interface, called from
	 *  SystemUDSelectTypesFrom widget.
	 * <p>
	 * The user has added or removed a type.
	 * Call getTypes() on given form to get the new list.
	 */
	public void selectedTypeListChanged(SystemUDSelectTypesForm form) {
		setChangesMade();
		setPageComplete();
		validateInput(false, null);
	}

	/**
	 * From ISystemUDSelectTypeListener interface, called from
	 *  SystemUDSelectTypesFrom widget.
	 * <p>
	 * The user has edited the master list of types. It needs to be refreshed.
	 * We must call setMasterTypes() to update the form's master type list
	 */
	public void masterTypeListChanged(SystemUDSelectTypesForm form) {
		SystemUDActionSubsystem udas = getUDActionSubsystem();
		SystemUDTypeManager udtm = udas.getUDTypeManager();
		// Re-populate the master listbox
		if (currentAction != null) {
			form.setMasterTypes(udtm.getTypeNames(currentAction.getDomain()));
		} else if (newMode) {
			form.setMasterTypes(udtm.getTypeNames(newModeDomain));
		}
	}

	// ------------------------------	
	// INTERNAL VALIDATION METHODS...
	// ------------------------------
	/**
	 * Scenario:  User edits an item, producing a syntax error.
	 * (eg. clear action name field)  Gets error msg, OK button disabled.
	 * then changes selection to another item.
	 * Current Problem: Error msg stays, OK remains disabled, until
	 * they edit a field.  (ValidateInput isnt re-reun until
	 * another field is changed.)
	 * <p>
	 * Solution:  When changing selection, reset the errorMessage and
	 * page-valid status.  Can get away with this because we
	 * do not propagate invalid field changes to the UDA data in memory.
	 */
	private void resetPageValidation() {
		//		setMessage( null);	
		errorMessage = null;
		parentDialog.clearErrorMessage();
		parentDialog.setPageComplete(true);
	}

	/**
	 * Check all input for errors.
	 * Subclasses should not override. Rather, they should override
	 *  doAdditionalValidation(boolean) which this method calls.
	 * @param setFocus - true if to set focus on offending control
	 * @param skipControl - control to skip since already checked
	 * @return true if no errors
	 */
	protected final boolean validateInput(boolean setFocus, Control skipControl) {
		Control errCtl = null;
		errorMessage = null;
		if (skipControl != textName) errorMessage = validateName(textName.getText().trim());
		errCtl = textName;
		if ((errorMessage == null) && (skipControl != textComment)) {
			errorMessage = validateComment(textComment.getText().trim());
			errCtl = textComment;
		}
		if ((errorMessage == null) && (skipControl != getCommandWidget())) {
			errorMessage = validateCommand();
			errCtl = getCommandWidget();
		}
		if (errorMessage == null) errorMessage = doAdditionalValidation(setFocus); // let child classes try
		if (errorMessage != null) {
			parentDialog.setErrorMessage(errorMessage);
			if (setFocus) errCtl.setFocus();
		} else
			parentDialog.clearErrorMessage();
		setPageComplete();
		return (errorMessage == null);
	}

	/**
	 * Overridable extension point for subclasses to do validation of options and
	 *  such when Apply is pressed. If you do report an error, consider setting the
	 *  focus to the appropriate widget, if setFocus is set.
	 * @return error message if an error detected, else null
	 */
	protected SystemMessage doAdditionalValidation(boolean doSetFocus) {
		return null;
	}

	/**
	 * Validate name input
	 */
	protected SystemMessage validateName(String input) {
		return nameValidator.validate(input);
	}

	/**
	 * Validate comment input
	 */
	protected SystemMessage validateComment(String input) {
		return cmtValidator.validate(input);
	}

	/**
	 * Validate command input
	 */
	protected SystemMessage validateCommand() {
		return commandField.validateCommand();
	}

	// -------------------------------------------
	// METHODS FOR USE BY US AND OUR CHILD CLASSES
	// -------------------------------------------
	/**
	 * Return the control widget for the command prompt
	 */
	protected Control getCommandWidget() {
		return commandField.getCommandWidget();
	}

	/**
	 * Set the text contents of the command widget
	 */
	protected void setCommandText(String text) {
		commandField.setCommandText(text);
	}

	/**
	 * Enable/disable command widget
	 */
	protected void enableCommandWidget(boolean enable) {
		commandField.enableCommandWidget(enable);
	}

	/**
	 * Get the contents of the command field
	 */
	protected String getCommandText() {
		return commandField.getCommandText();
	}

	/**
	 * Turn on or off event ignoring flag
	 */
	protected void setIgnoreChanges(boolean ignore) {
		ignoreChanges = ignore;
		commandField.setIgnoreChanges(ignore);
	}

	// ------------------------------	
	// INTERNAL METHODS...
	// ------------------------------	
	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by setPageComplete
	 */
	protected boolean isPageComplete() {
		return ((errorMessage == null) && (textName.getText().trim().length() > 0) && (getCommandText().length() > 0));
	}

	/**
	 * Set page complete... enables/disables Apply button
	 */
	protected void setPageComplete() {
		boolean complete = isPageComplete();
		parentDialog.setPageComplete(complete);
	}

	/**
	 * When user presses Apply, commit all pending changes...
	 */
	protected void processChanges() {
		currentAction.setComment(textComment.getText().trim());
		currentAction.setName(textName.getText().trim());
		String cmd = commandField.getMassagedCommandText();
		if (commandField.getCommandMassager() != null) {
			if (!newMode) {
				setIgnoreChanges(true); // disable modify listeners
				setCommandText(cmd);
				setIgnoreChanges(false); // re-enable modify listeners
			}
		}
		currentAction.setCommand(cmd);
		currentAction.setPrompt(promptCB.getSelection());
		currentAction.setRefresh(refreshCB.getSelection());
		currentAction.setShow(showCB.getSelection());
		currentAction.setSingleSelection(singleSelCB.getSelection());
		currentAction.setCollect(collectCB.getSelection());
		processExtraOptionsChanges(currentAction);
		if (selectTypesForm != null) currentAction.setFileTypes(selectTypesForm.getTypes());
	} //process changes

	/**
	 * Save current state to disk
	 */
	protected void saveData() {
		if (newMode) {
			currentAction = createNewAction(textName.getText().trim(), newModeDomain);
		}
		processChanges();
		SystemUDActionSubsystem udas = getUDActionSubsystem();
		SystemUDActionManager udam = udas.getUDActionManager();
		ISystemProfile currentProfile = udam.getCurrentProfile();
		if (currentProfile == null) // shouldn't!
			currentProfile = profile;
		//subsystem.getUDActionSubsystem().getUDActionManager().saveUserData();			
		udam.saveUserData(currentProfile);
		// inform anybody registered as listeners that we have created/changed model object...
		if (newMode)
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_USERACTION, currentAction, null);
		else
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_USERACTION, currentAction, null);
	}

	/**
	 * Call this whenever the user makes ANY changes.
	 * Used to enable/disable apply/revert buttons
	 */
	protected void setChangesMade() {
		if (stateMachine != null) stateMachine.setChangesMade();
	}

	/**
	 * In "new" mode, create a new action when Apply is pressed.
	 * This only creates the action. It does not populate the attributes
	 * @return The new action
	 */
	protected SystemUDActionElement createNewAction(String actionName, int domain) {
		// code was originally in SystemNewUDAsWizardMainPage
		SystemUDActionSubsystem udas = getUDActionSubsystem();
		SystemUDActionManager udam = udas.getUDActionManager();
		ISystemProfile currentProfile = udam.getCurrentProfile();
		if (currentProfile == null) // shouldn't!
			currentProfile = profile;
		SystemUDActionElement na = udam.addAction(currentProfile, actionName, domain);
		// ??? handle failure ???  check for ???
		if (null != na) {
			// Set default types to ALL, if ALL is not a type we create it with types string *
			if (domain != SystemUDActionSubsystem.DOMAIN_NONE) {
				SystemUDTypeManager typeManager = getUDActionSubsystem().getUDTypeManager();
				if (typeManager.findChildByName(currentProfile, SystemUDTypeManager.ALL_TYPE, domain) == null) {
					SystemUDTypeElement nt = typeManager.addType(domain, SystemUDTypeManager.ALL_TYPE);
					nt.setTypes("*"); //$NON-NLS-1$
					typeManager.saveUserData(currentProfile);
				}
				// Add the ALL type to the action
				String[] types = new String[1];
				types[0] = SystemUDTypeManager.ALL_TYPE;
				na.setFileTypes(types);
			}
			// Now update tree view to show new item, and set selection to it.
			//SystemUDActionElement parentEl =	(SystemUDActionElement) udas.getUDActionManager().getParent(na);
			//treeView.internalRefresh(parentEl);
		}
		return na;
	}

	/**
	 * For uniqueness checking, get the list of existing action names
	 */
	protected Vector getExistingActionNames() {
		if (newMode) {
			SystemUDActionSubsystem udas = getUDActionSubsystem();
			SystemUDActionManager udam = udas.getUDActionManager();
			ISystemProfile currentProfile = udam.getCurrentProfile();
			if (currentProfile == null) // shouldn't!
				currentProfile = profile;
			//System.out.println("Asking for existing names for newModeDomain " + newModeDomain);
			return udam.getExistingNames(currentProfile, newModeDomain);
		} else if (currentAction != null)
			return currentAction.getExistingNames();
		else
			return EMPTY_VECTOR;
	}

	/**
	 * Callback from SystemCommandTextField when the user modifies the command.
	 * @param cmdText - current contents of the field
	 * @param errorMessage - potential error detected by the default validator
	 */
	public void commandModified(String cmdText, SystemMessage errorMessage) {
		this.errorMessage = errorMessage;
		setChangesMade();
		if (errorMessage != null) {
			parentDialog.setErrorMessage(errorMessage);
			setPageComplete();
		} else
			validateInput(false, getCommandWidget());
		processCommandTextChange(cmdText, (errorMessage != null));
	}

	/**
	 * Method called as user types into the command field
	 * Encapsulated out so that it can be called from various types of listeners.
	 * Further, it is easily overridden
	 */
	protected void processCommandTextChange(String newText, boolean hasError) {
	}

	// -----------------------------
	// Helper methods...
	// -----------------------------
	/**
	 * Add a separator line. This is a physically visible line.
	 */
	protected Label addSeparatorLine(Composite parent, int nbrColumns) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		separator.setLayoutData(data);
		return separator;
	}

	/**
	 * Add a spacer line
	 */
	protected Label addFillerLine(Composite parent, int nbrColumns) {
		Label filler = new Label(parent, SWT.LEFT);
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		filler.setLayoutData(data);
		return filler;
	}
}
