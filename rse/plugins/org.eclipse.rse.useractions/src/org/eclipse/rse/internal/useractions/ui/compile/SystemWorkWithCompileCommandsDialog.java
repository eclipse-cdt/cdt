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
 * David Dykstal (IBM) - [232318] fixing layout problems with button composite
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.compile;

import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.internal.ui.view.SystemViewMenuListener;
import org.eclipse.rse.internal.useractions.IUserActionsModelChangeEvents;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.rse.ui.widgets.ISystemEditPaneStates;
import org.eclipse.rse.ui.widgets.SystemEditPaneStateMachine;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * A dialog that allows the user to work with the compile commands for the compile actions in this subsystem.
 */
public class SystemWorkWithCompileCommandsDialog extends SystemPromptDialog implements SelectionListener, ISystemCompileCommandEditPaneListener, IMenuListener, Runnable,
		ISystemCompileCommandEditPaneHoster {
	protected Button applyButton, revertButton, newSrcTypeButton, rmvSrcTypeButton;
	protected Combo profileCombo;
	protected Combo srcTypeCombo;
	protected List listView;
	protected Label ccLabel;
	protected SystemEditPaneStateMachine sm;
	// context menu actions support
	private SystemCompileCommandActionCopy copyAction;
	private SystemCompileCommandActionPaste pasteAction;
	private SystemCompileCommandActionDelete deleteAction;
	private SystemCompileCommandActionMoveUp moveUpAction;
	private SystemCompileCommandActionMoveDown moveDownAction;
	private SystemCompileCommandActionRestoreDefaults restoreAction;
	private MenuManager menuMgr;
	private Clipboard clipboard;
	private boolean menuListenerAdded;
	// inputs
	protected SystemCompileManager compileManager;
	protected SystemCompileCommandEditPane editpane;
	protected SystemCompileProfile[] compProfiles;
	protected SystemCompileProfile currentCompProfile;
	protected SystemCompileType currentCompType;
	protected boolean caseSensitive;
	protected boolean supportsAddSrcTypeButton;
	private String srcTypeLabel, srcTypeTooltip;
	// state
	protected boolean ignoreEvents = false;
	protected boolean restoreProfileComboSelection = true;
	protected boolean showProfileCombo;
	protected boolean resetting = false;
	protected boolean giveEditorFocus = true;
	protected int prevProfileComboSelection = 0;
	protected int prevSrcTypeComboSelection = 0;
	protected int prevListSelection = 0;
	protected String[] compileTypeNames;
	private boolean traceTest;

	/**
	 * Constructor
	 */
	public SystemWorkWithCompileCommandsDialog(Shell shell, SystemCompileManager compileManager, SystemCompileProfile currentCompProfile) {
		this(shell, compileManager, currentCompProfile, SystemUDAResources.RESID_WWCOMPCMDS_TITLE);
	}

	/**
	 * Constructor, when unique title desired
	 */
	public SystemWorkWithCompileCommandsDialog(Shell shell, SystemCompileManager compileManager, SystemCompileProfile currentCompProfile, String title) {
		super(shell, title);
		this.compileManager = compileManager;
		this.currentCompProfile = currentCompProfile;
		this.compProfiles = new SystemCompileProfile[] { currentCompProfile };
		this.supportsAddSrcTypeButton = true;
		setCancelButtonLabel(SystemUDAResources.BUTTON_CLOSE);
		setShowOkButton(false);
		setOutputObject(null);
		setHelp();
		// default mri values...
		setSourceTypePromptMRI(compileManager.getSourceTypePromptMRILabel(), compileManager.getSourceTypePromptMRITooltip());
	}

	/**
	 * Overridable extension point for setting dialog help
	 */
	protected void setHelp() {
		setHelp(RSEUIPlugin.HELPPREFIX + "wwcc0000"); //$NON-NLS-1$
	}

	// INPUT/CONFIGURATION
	/**
	 * Specify an edit pane that prompts the user for the contents of a compile command
	 */
	public void setCompileCommandEditPane(SystemCompileCommandEditPane editPane) {
		this.editpane = editPane;
	}

	/**
	 * Set the compile profiles to show in the profile combo. 
	 * @param profiles - array of profiles to show
	 */
	public void setProfiles(SystemCompileProfile[] profiles) {
		if (profiles == null)
			compProfiles = new SystemCompileProfile[0];
		else {
			compProfiles = profiles;
			showProfileCombo = true;
		}
	}

	/**
	 * Set whether the source types, labels, etc are case sensitive
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Set supports the "Add..." button beside the source type combo.
	 * The default is true.
	 */
	public void setSupportsAddSrcTypeButton(boolean supports) {
		this.supportsAddSrcTypeButton = supports;
	}

	/**
	 * Set the mri for the source type prompt
	 */
	public void setSourceTypePromptMRI(String srcTypeMRILabel, String srcTypeMRITooltip) {
		this.srcTypeLabel = srcTypeMRILabel;
		this.srcTypeTooltip = srcTypeMRITooltip;
	}

	/**
	 * Set the compile to pre-select in the types combo
	 */
	public void setCompileType(SystemCompileType type) {
		this.currentCompType = type;
	}

	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() {
		return editpane.getInitialFocusControl();
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) {
		editpane = getCompileCommandEditPane(getShell());
		//editpane.setSubSystem(subsystem);
		// Inner composite
		int nbrColumns = 4;
		Composite composite = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		// profile combo 
		profileCombo = SystemWidgetHelpers.createLabeledReadonlyCombo(composite, null, SystemUDAResources.RESID_WWCOMPCMDS_PROFILE_LABEL, SystemUDAResources.RESID_WWCOMPCMDS_PROFILE_TOOLTIP);
		((GridData) profileCombo.getLayoutData()).horizontalSpan = nbrColumns - 1;
		// source type combo
		srcTypeCombo = SystemWidgetHelpers.createLabeledReadonlyCombo(composite, null, srcTypeLabel, srcTypeTooltip);
		if (supportsAddSrcTypeButton) {
			newSrcTypeButton = SystemWidgetHelpers.createPushButton(composite, null, SystemUDAResources.RESID_WWCOMPCMDS_TYPES_BUTTON_ADD_LABEL,
					SystemUDAResources.RESID_WWCOMPCMDS_TYPES_BUTTON_ADD_TOOLTIP);
			rmvSrcTypeButton = SystemWidgetHelpers.createPushButton(composite, null, SystemUDAResources.RESID_WWCOMPCMDS_TYPES_BUTTON_RMV_LABEL,
					SystemUDAResources.RESID_WWCOMPCMDS_TYPES_BUTTON_RMV_TOOLTIP);
			rmvSrcTypeButton.setEnabled(false);
		} else
			((GridData) srcTypeCombo.getLayoutData()).horizontalSpan = nbrColumns - 1;
		//SystemWidgetHelpers.setHelp(profileCombo, RSEUIPlugin.HELPPREFIX + "ccon0001", parentHelpId);     
		addFillerLine(composite, nbrColumns);
		// create list view on left
		listView = SystemWidgetHelpers.createListBox(composite, SystemUDAResources.RESID_WWCOMPCMDS_LIST_LABEL, null, false, 1);
		//listView.setToolTipText(listPromptTip); annoying!
		GridData data = (GridData) listView.getLayoutData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = GridData.FILL;
		data.widthHint = 110;
		// we want the tree view on the left to extend to the bottom of the page, so on the right
		// we create a 1-column composite that will hold the edit pane on top, and the apply/revert
		// buttons on the bottom...
		Composite rightSideComposite = SystemWidgetHelpers.createFlushComposite(composite, 1);
		((GridData) rightSideComposite.getLayoutData()).horizontalSpan = nbrColumns - 1;
		// now add a top spacer line and visual separator line, for the right side
		addFillerLine(rightSideComposite, 1);
		ccLabel = SystemWidgetHelpers.createLabel(rightSideComposite, ""); //$NON-NLS-1$
		addSeparatorLine(rightSideComposite, 1);
		// now populate top of right-side composite with edit pane...
		editpane.createContents(rightSideComposite);
		// now add a bottom visual separator line
		addSeparatorLine(rightSideComposite, 1);
		// now populate bottom of right-side composite with apply/revert buttons within their own composite
		int nbrColumns_buttonComposite = 3;
		Composite applyResetButtonComposite = SystemWidgetHelpers.createFlushComposite(rightSideComposite, nbrColumns_buttonComposite);
		// now populate the buttons composite with apply and revert buttons
		Label filler = SystemWidgetHelpers.createLabel(applyResetButtonComposite, ""); //$NON-NLS-1$
		((GridData) filler.getLayoutData()).grabExcessHorizontalSpace = true;
		((GridData) filler.getLayoutData()).horizontalAlignment = GridData.FILL;
		applyButton = SystemWidgetHelpers.createPushButton(applyResetButtonComposite, this, SystemUDAResources.RESID_WWCOMPCMDS_BUTTON_APPLY_LABEL,
				SystemUDAResources.RESID_WWCOMPCMDS_BUTTON_APPLY_TOOLTIP);
		((GridData) applyButton.getLayoutData()).grabExcessHorizontalSpace = false;
		revertButton = SystemWidgetHelpers.createPushButton(applyResetButtonComposite, this, SystemUDAResources.RESID_WWCOMPCMDS_BUTTON_REVERT_LABEL,
				SystemUDAResources.RESID_WWCOMPCMDS_BUTTON_REVERT_TOOLTIP);
		((GridData) revertButton.getLayoutData()).grabExcessHorizontalSpace = false;
		// now add a spacer to soak up left-over height...
		addGrowableFillerLine(rightSideComposite, 1);
		// create state machine to manage edit pane
		sm = new SystemEditPaneStateMachine(rightSideComposite, applyButton, revertButton);
		sm.setApplyLabelForNewMode(SystemUDAResources.RESID_WWCOMPCMDS_BUTTON_CREATE_LABEL, SystemUDAResources.RESID_WWCOMPCMDS_BUTTON_CREATE_TOOLTIP);
		sm.setUnsetMode();
		// populate profile dropdown
		initProfileCombo();
		composite.layout(true);
		// add listeners
		profileCombo.addSelectionListener(this);
		srcTypeCombo.addSelectionListener(this);
		if (supportsAddSrcTypeButton) {
			newSrcTypeButton.addSelectionListener(this);
			rmvSrcTypeButton.addSelectionListener(this);
		}
		listView.addSelectionListener(this);
		applyButton.addSelectionListener(this);
		revertButton.addSelectionListener(this);
		editpane.addChangeListener(this);
		// add special listeners for accessibility -- do not change focus when navigating list with keys
		listView.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				giveEditorFocus = true;
			}

			public void mouseDown(MouseEvent e) {
				giveEditorFocus = true;
			}

			public void mouseUp(MouseEvent e) {
				giveEditorFocus = true;
			}
		});
		listView.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				giveEditorFocus = false;
			}

			public void keyReleased(KeyEvent e) {
				giveEditorFocus = false;
			}
		});
		// add context menu
		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		menuMgr = new MenuManager("#WWCompCmdsPopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(listView);
		listView.setMenu(menu);
		editpane.configureHeadingLabel(ccLabel);
		editpane.isComplete();// side effect is initial enablement of test button
		return composite;
	}

	/**
	 * Intercept of parent so we can reset the default button
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getShell().setDefaultButton(applyButton); // defect 46129
	}

	/**
	 * Return our edit pane. Overriding this is an alternative to calling setEditPane.
	 * This is called in createContents
	 */
	protected SystemCompileCommandEditPane getCompileCommandEditPane(Shell shell) {
		if (editpane == null) editpane = compileManager.getCompileCommandEditPane(shell, this, caseSensitive);
		return editpane;
	}

	/**
	 * Initialize contents and selection of profile combo
	 */
	private void initProfileCombo() {
		if (profileCombo != null) {
			if ((compProfiles != null) && (compProfiles.length > 0)) {
				String[] names = new String[compProfiles.length];
				int selIdx = -1;
				for (int idx = 0; idx < names.length; idx++) {
					names[idx] = compProfiles[idx].getProfileName();
					if ((currentCompProfile != null) && (currentCompProfile == compProfiles[idx])) selIdx = idx;
				}
				if (selIdx == -1) {
					selIdx = 0;
					currentCompProfile = compProfiles[0];
				}
				profileCombo.setItems(names);
				profileCombo.setText(names[selIdx]);
				prevProfileComboSelection = selIdx;
				if (currentCompProfile != null) {
					if (currentCompType != null)
						processProfileSelected(currentCompType.getType());
					else
						processProfileSelected(null);
				}
			}
		}
	}

	/**
	 * Process when a profile is selected
	 */
	private void processProfileSelected(String srcType) {
		if (srcType == null) {
			ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(getInputObject());
			if (rmtAdapter != null) {
				srcType = rmtAdapter.getRemoteSourceType(getInputObject());
				/*
				 if (currentCompProfile.getCompileType(srcType) == null)
				 {
				 currentCompProfile.addCompileType(new SystemCompileType(currentCompProfile, srcType));
				 saveData();
				 }*/
			}
		}
		Vector types = currentCompProfile.getCompileTypes();
		if (srcType == null) {
			if (types.size() > 0)
				srcType = ((SystemCompileType) types.elementAt(0)).getType();
			else {
				srcType = ""; //$NON-NLS-1$
			}
		}
		compileTypeNames = new String[types.size()];
		int index = 0;
		// when profile is selected, show the member type
		// of the DataElement selected.
		for (int i = 0; i < compileTypeNames.length; i++) {
			compileTypeNames[i] = ((SystemCompileType) types.get(i)).getType();
			if (!caseSensitive) {
				if (srcType.equalsIgnoreCase(compileTypeNames[i])) index = i;
			} else {
				if (srcType.equals(compileTypeNames[i])) index = i;
			}
		}
		srcTypeCombo.setItems(compileTypeNames);
		if (srcTypeCombo.getItemCount() > 0) {
			srcTypeCombo.setText(srcTypeCombo.getItem(index));
			prevSrcTypeComboSelection = index;
			currentCompType = currentCompProfile.getCompileType(srcTypeCombo.getText());
			processSrcTypeSelected(0);
		} else {
			prevSrcTypeComboSelection = -1;
			currentCompType = null;
			processSrcTypeSelected(-1);
		}
	}

	/**
	 * Process when src type selected
	 */
	private void processSrcTypeSelected(int selection) {
		Vector cmds = new Vector();
		if (currentCompType != null) cmds = currentCompType.getCompileCommands();
		if (rmvSrcTypeButton != null) rmvSrcTypeButton.setEnabled((currentCompType != null) && (cmds.size() == 0));
		if (currentCompType != null) {
			String[] listItems = new String[1 + cmds.size()];
			listItems[0] = SystemUDAResources.RESID_WWCOMPCMDS_LIST_NEWITEM;
			for (int idx = 0; idx < cmds.size(); idx++)
				listItems[idx + 1] = ((SystemCompileCommand) cmds.get(idx)).getLabel();
			listView.setItems(listItems);
			listView.setSelection(selection);
		} else {
			listView.removeAll();
		}
		processCommandsListSelected();
	}

	/**
	 * Process when compile command is selected in the list view
	 */
	private void processCommandsListSelected() {
		int index = listView.getSelectionIndex();
		if (index == 0) {
			sm.setNewMode();
			editpane.setCompileCommand(currentCompType, null);
			editpane.configureHeadingLabel(ccLabel);
		} else if (index > -1) // and not zero
		{
			SystemCompileCommand currCmd = getCurrentlySelectedCompileCommand();
			sm.setEditMode();
			editpane.setCompileCommand(currentCompType, currCmd);
			editpane.configureHeadingLabel(ccLabel);
		} else {
			sm.setUnsetMode();
			editpane.setCompileCommand(null, null);
		}
		prevListSelection = index;
	}

	/**
	 * Gets the current compile profile, given the profile selection index
	 */
	private SystemCompileProfile getCompileProfile(int currSelIdx) {
		return compProfiles[currSelIdx];
	}

	/**
	 * Parent override.
	 * Called when user presses CLOSE button. 
	 * We simply close the dialog (since we save as we go), unless there are pending changes.
	 */
	protected boolean processCancel() {
		if (sm.isSaveRequired()) {
			ignoreEvents = true;
			if ((editpane.verify() != null)) {
				ignoreEvents = false;
				sm.setChangesMade(); // defect 45773
				return false; // pending errors. Cannot save, so cannot close!
			}
			ignoreEvents = false;
			//saveData();
			applyPressed(false); // defect 46379
		}
		return super.processCancel();
	}

	/**
	 * Save the data in the currently selected profile
	 */
	private void saveData() {
		if (currentCompProfile != null) currentCompProfile.writeToDisk();
	}

	/**
	 * Handles events generated by controls on this page.
	 */
	public void widgetSelected(SelectionEvent e) {
		clearMessage();
		Widget source = e.widget;
		if (resetting) return;
		if (source == applyButton) {
			applyPressed(true);
		} else if (source == revertButton) {
			revertPressed();
		} else if (source == newSrcTypeButton) {
			newSrcTypePressed();
		} else if (source == rmvSrcTypeButton) {
			rmvSrcTypePressed();
		} else if (source == listView) {
			if (traceTest) System.out.println("Inside widgetSelected for listView: " + listView.getSelectionIndex()); //$NON-NLS-1$
			// change for pendings changes or unresolved errors...
			if (editpane.areErrorsPending()) {
				//System.out.println("errors pending in editpane");
				e.doit = false; // dang, this doesn't work!
				resetting = true;
				listView.select(prevListSelection);
				resetting = false;
				return;
			} else if (sm.isSaveRequired()) {
				boolean newMode = (sm.getMode() == ISystemEditPaneStates.MODE_NEW);
				if (editpane.verify() != null) {
					//System.out.println("verify in editpane returned an error");
					e.doit = false; // dang, this doesn't work!
					resetting = true;
					listView.select(prevListSelection);
					resetting = false;
					sm.setChangesMade(); // isSaveRequired() resets it so we need to undo that
					return;
				}
				int newSelection = listView.getSelectionIndex();
				saveCompileCommand(editpane.saveChanges(), newMode, prevListSelection);
				listView.select(newSelection); // the save changed the selection, so we need to restore it
			}
			processCommandsListSelected();
			if (giveEditorFocus) {
				Control c = editpane.getInitialFocusControl();
				if ((c != null) && !c.isDisposed() && c.isVisible()) c.setFocus();
			}
		} else if (source == profileCombo) {
			if (editpane.areErrorsPending()) {
				restoreProfileComboSelection = true;
				profileCombo.getDisplay().asyncExec(this);
				return;
			} else if (sm.isSaveRequired()) // defect 46318
			{
				boolean newMode = (sm.getMode() == ISystemEditPaneStates.MODE_NEW);
				if (editpane.verify() != null) {
					restoreProfileComboSelection = true;
					sm.setChangesMade(); // isSaveRequired() resets it so we need to undo that
					profileCombo.getDisplay().asyncExec(this);
					return; // newly-found errors are pending so go no further
				}
				//int newSelection = listView.getSelectionIndex();
				saveCompileCommand(editpane.saveChanges(), newMode, prevListSelection);
				//listView.select(newSelection); // the save changed the selection, so we need to restore it
			}
			int idx = profileCombo.getSelectionIndex();
			currentCompProfile = getCompileProfile(idx);
			//processProfileSelected(srcTypeCombo.getText());
			processProfileSelected(null);
			prevProfileComboSelection = idx;
		} else if (source == srcTypeCombo) {
			if (editpane.areErrorsPending()) {
				restoreProfileComboSelection = false;
				srcTypeCombo.getDisplay().asyncExec(this);
				return;
			} else if (sm.isSaveRequired()) // defect 46318
			{
				boolean newMode = (sm.getMode() == ISystemEditPaneStates.MODE_NEW);
				if (editpane.verify() != null) {
					restoreProfileComboSelection = false;
					sm.setChangesMade(); // isSaveRequired() resets it so we need to undo that
					profileCombo.getDisplay().asyncExec(this);
					return; // newly-found errors are pending so go no further
				}
				//int newSelection = listView.getSelectionIndex();
				saveCompileCommand(editpane.saveChanges(), newMode, prevListSelection);
				//listView.select(newSelection); // the save changed the selection, so we need to restore it
			}
			int idx = srcTypeCombo.getSelectionIndex();
			currentCompType = currentCompProfile.getCompileType(srcTypeCombo.getText());
			processSrcTypeSelected(0);
			prevSrcTypeComboSelection = idx;
		}
	}

	/**
	 * User pressed Apply to save the pending changes the current filter string
	 */
	protected void applyPressed(boolean doVerify) {
		ignoreEvents = true;
		if (!doVerify || (editpane.verify() == null)) {
			SystemCompileCommand editedCompileCmd = editpane.saveChanges();
			boolean ok = (editedCompileCmd != null);
			if (ok) {
				boolean newMode = (sm.getMode() == ISystemEditPaneStates.MODE_NEW);
				sm.applyPressed();
				saveCompileCommand(editedCompileCmd, newMode, prevListSelection);
				processCommandsListSelected();
				if (newMode)
					RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_COMPILECMD, editedCompileCmd,
							null);
				else
					RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_COMPILECMD,
							editedCompileCmd, null);
			}
		}
		ignoreEvents = false;
	}

	/**
	 * User pressed Revert to discard the pending changes the current filter string
	 */
	protected void revertPressed() {
		ignoreEvents = true;
		editpane.clearErrorMessage();
		sm.resetPressed();
		if (isNewSelected() || (listView.getSelectionIndex() == -1))
			editpane.setCompileCommand(currentCompType, null);
		else
			editpane.setCompileCommand(currentCompType, getCurrentlySelectedCompileCommand());
		setPageComplete(true);
		clearErrorMessage();
		ignoreEvents = false;
	}

	/**
	 * User pressed New... beside the Source Type combo
	 */
	protected void newSrcTypePressed() {
		if (sm.isSaveRequired()) {
			ignoreEvents = true;
			if ((editpane.verify() != null)) {
				ignoreEvents = false;
				return; // pending errors. Cannot save, so cannot process!
			}
			ignoreEvents = false;
			//saveData();
			applyPressed(false); // defect 46379
		}
		SystemNewCompileSrcTypeDialog dlg = compileManager.getNewSrcTypeDialog(getShell(), caseSensitive);
		dlg.setExistingSrcTypes(compileTypeNames);
		dlg.open();
		if (!dlg.wasCancelled()) {
			String newSrcType = dlg.getNewSrcType();
			currentCompProfile.addCompileType(new SystemCompileType(currentCompProfile, newSrcType));
			saveData();
			processProfileSelected(newSrcType);
			//System.out.println("New src type: " + newSrcType);
		}
	}

	/**
	 * User pressed Remove... beside the Source Type combo
	 */
	protected void rmvSrcTypePressed() {
		if (sm.isSaveRequired()) {
			ignoreEvents = true;
			if ((editpane.verify() != null)) {
				ignoreEvents = false;
				return; // pending errors. Cannot save, so cannot process!
			}
			ignoreEvents = false;
			//saveData();
			applyPressed(false); // defect 46379
		}
		currentCompProfile.removeCompileType(currentCompType);
		saveData();
		processProfileSelected(null);
	}

	/**
	 * Handles events generated by controls on this page.
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * Save the given edited/new compile command and updates the gui list.
	 */
	protected void saveCompileCommand(SystemCompileCommand editedCompileCommand, boolean newMode, int selectionIndex) {
		if (newMode) {
			currentCompType.addCompileCommand(editedCompileCommand);
			listView.add(editedCompileCommand.getLabel());
			saveData();
			//listView.select(listView.getItemCount()-1);
			processSrcTypeSelected(listView.getItemCount() - 1);
		} else {
			saveData();
			//listView.select(selectionIndex);
			//listView.setItem(selectionIndex, editedCompileCommand.getLabel());
			//processCommandsListSelected();
			processSrcTypeSelected(selectionIndex);
		}
	}

	/**
	 * Intercept of parent method so we can direct it to the Apply button versus the OK button (which we don't have).
	 */
	public void setPageComplete(boolean complete) {
		// d45795
		if (applyButton != null) {
			if (!complete) applyButton.setEnabled(false);
			// else: we never enable it because the state machine does that anyway on any user-input change
		}
	}

	// ---------------
	// HELPER METHODS
	// ---------------  
	/**
	 * Returns the implementation of ISystemRemoteElement for the given
	 * object.  Returns null if this object does not adaptable to this.
	 */
	protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) {
		ISystemRemoteElementAdapter adapter = null;
		if (!(o instanceof IAdaptable))
			adapter = (ISystemRemoteElementAdapter) Platform.getAdapterManager().getAdapter(o, ISystemRemoteElementAdapter.class);
		else
			adapter = (ISystemRemoteElementAdapter) ((IAdaptable) o).getAdapter(ISystemRemoteElementAdapter.class);
		return adapter;
	}

	// ------------	
	// List methods
	// ------------
	/**
	 * Return true if currently selected item is "New"
	 */
	protected boolean isNewSelected() {
		return (listView.getSelectionIndex() == 0);
	}

	/**
	 * Return true if currently selected item is IBM- or vendor-supplied
	 */
	protected boolean isIBMSupplied() {
		if (listView.getSelectionIndex() > 0) {
			return !getCurrentlySelectedCompileCommand().isUserSupplied();
		} else
			return false;
	}

	/**
	 * Return currently selected list item
	 */
	protected String getCurrentSelection() {
		if (listView.getSelectionCount() >= 1)
			return listView.getSelection()[0];
		else
			return null;
	}

	/**
	 * Return the currently selected compile command
	 */
	protected SystemCompileCommand getCurrentlySelectedCompileCommand() {
		int selIdx = listView.getSelectionIndex();
		if (selIdx > 0) // item 0 is "new" so skip it
			return currentCompType.getCompileCommand(selIdx - 1);
		else
			return null;
	}

	// ----------------------------------------------
	// EDIT PANE CHANGE LISTENER INTERFACE METHODS...
	// ----------------------------------------------
	/**
	 * Callback method. The user has changed the compile command. It may or may not
	 *  be valid. If not, the given message is non-null. If it is, and you want it,
	 *  call getCompileCommand() in the edit pane.
	 */
	public void compileCommandChanged(SystemMessage message) {
		if (message != null)
			setErrorMessage(message);
		else
			clearErrorMessage();
		if (!ignoreEvents) // this is set on while verifying, indicating these are not real change events per se
		{
			sm.setChangesMade();
		}
		setPageComplete(message == null); // d45795
	}

	// ------------------------------
	// CONTEXT MENU ACTION SUPPORT...
	// ------------------------------
	/**
	 * Called when the context menu is about to open.
	 * Calls {@link #fillContextMenu(IMenuManager)}
	 */
	public void menuAboutToShow(IMenuManager menu) {
		fillContextMenu(menu);
		if (!menuListenerAdded) {
			if (menu instanceof MenuManager) {
				Menu m = ((MenuManager) menu).getMenu();
				if (m != null) {
					menuListenerAdded = true;
					SystemViewMenuListener ml = new SystemViewMenuListener();
					//ml.setShowToolTipText(true, wwDialog.getMessageLine()); does not work for some reason
					m.addMenuListener(ml);
				}
			}
		}
	}

	/**
	 * This is method is called to populate the popup menu
	 */
	public void fillContextMenu(IMenuManager menu) {
		String currentString = getCurrentSelection();
		IStructuredSelection selection = null;
		if (currentString != null) selection = new StructuredSelection(currentString);
		// Partition into groups...
		createStandardGroups(menu);
		ISystemAction action = null;
		boolean isNewSelected = isNewSelected();
		//System.out.println("new selected? " + isNewSelected);
		if ((selection != null) && !isNewSelected) {
			action = getDeleteAction(selection);
			menu.appendToGroup(action.getContextMenuGroup(), action);
			action = getCopyAction(selection);
			menu.appendToGroup(action.getContextMenuGroup(), action);
			action = getMoveUpAction(selection);
			menu.appendToGroup(action.getContextMenuGroup(), action);
			action = getMoveDownAction(selection);
			menu.appendToGroup(action.getContextMenuGroup(), action);
			action = getRestoreAction(selection);
			menu.appendToGroup(action.getContextMenuGroup(), action);
		}
		//if (!isNewSelected)
		{
			action = getPasteAction(selection);
			menu.appendToGroup(action.getContextMenuGroup(), action);
		}
	}

	/**
	 * Creates the Systems plugin standard groups in a context menu.
	 */
	public void createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty()) return;
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE)); // rename,move,copy,delete,bookmark,refactoring
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER)); // move up, move down		
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE)); // restore
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS)); // user or BP/ISV additions
	}

	/**
	 * Get the delete action
	 */
	private SystemCompileCommandActionDelete getDeleteAction(ISelection selection) {
		if (deleteAction == null) deleteAction = new SystemCompileCommandActionDelete(this);
		deleteAction.setShell(getShell());
		deleteAction.setSelection(selection);
		return deleteAction;
	}

	/**
	 * Get the move up action
	 */
	private SystemCompileCommandActionMoveUp getMoveUpAction(ISelection selection) {
		if (moveUpAction == null) moveUpAction = new SystemCompileCommandActionMoveUp(this);
		moveUpAction.setShell(getShell());
		moveUpAction.setSelection(selection);
		return moveUpAction;
	}

	/**
	 * Get the move down action
	 */
	private SystemCompileCommandActionMoveDown getMoveDownAction(ISelection selection) {
		if (moveDownAction == null) moveDownAction = new SystemCompileCommandActionMoveDown(this);
		moveDownAction.setShell(getShell());
		moveDownAction.setSelection(selection);
		return moveDownAction;
	}

	/**
	 * Get the copy action
	 */
	private SystemCompileCommandActionCopy getCopyAction(ISelection selection) {
		if (copyAction == null) copyAction = new SystemCompileCommandActionCopy(this);
		copyAction.setShell(getShell());
		copyAction.setSelection(selection);
		return copyAction;
	}

	/**
	 * Get the paste action
	 */
	private SystemCompileCommandActionPaste getPasteAction(ISelection selection) {
		if (pasteAction == null) pasteAction = new SystemCompileCommandActionPaste(this);
		pasteAction.setShell(getShell());
		if (selection != null) pasteAction.setSelection(selection);
		return pasteAction;
	}

	/**
	 * Get the restore defaults action
	 */
	private SystemCompileCommandActionRestoreDefaults getRestoreAction(ISelection selection) {
		if (restoreAction == null) restoreAction = new SystemCompileCommandActionRestoreDefaults(this);
		restoreAction.setShell(getShell());
		if (selection != null) restoreAction.setSelection(selection);
		return restoreAction;
	}

	// -------------------------------------------------------------
	// CALLBACK METHODS FROM THE RIGHT CLICK CONTEXT MENU ACTIONS...
	// -------------------------------------------------------------
	/**
	 * Decide if we can do the delete or not.
	 * Will decide the enabled state of the delete action.
	 */
	public boolean canDelete() {
		return (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isNewSelected() && (listView.getSelectionIndex() != -1) && !isIBMSupplied();
	}

	/**
	 * Perform the delete action
	 */
	public void doDelete() {
		int idx = listView.getSelectionIndex();
		SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONFIRM_DELETE);
		SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), msg);
		try {
			if (msgDlg.openQuestion()) {
				SystemCompileCommand deletedCmd = getCurrentlySelectedCompileCommand();
				currentCompType.removeCompileCommand(deletedCmd);
				saveData();
				//traceTest = true;
				listView.remove(idx); // remove item from list		     
				if (idx <= (listView.getItemCount() - 1)) // can we select next item?
					listView.select(idx); // select next item
				else
					listView.select(idx - 1); // select previous item
				processCommandsListSelected();
				if (listView.getItemCount() == 1) //d47206
					rmvSrcTypeButton.setEnabled(true);
				//traceTest = false;
				// fire model change event in case any BP code is listening...
				RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_COMPILECMD, deletedCmd, null);
			}
		} catch (Exception exc) {
		}
	}

	/**
	 * Decide if we can do the move up or not.
	 * Will decide the enabled state of the move up action.
	 */
	public boolean canMoveUp() {
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isNewSelected() && (listView.getSelectionIndex() != -1);
		if (can) {
			int idx = listView.getSelectionIndex();
			can = (idx > 1); // skip new at index 0, and skip first actual compile command as it can't be moved up
		}
		return can;
	}

	/**
	 * Perform the move up action
	 */
	public void doMoveUp() {
		int idx = listView.getSelectionIndex();
		SystemCompileCommand currCmd = getCurrentlySelectedCompileCommand();
		if (currCmd == null) return; // better never happen!
		// remove and add in model...
		currentCompType.removeCompileCommand(currCmd); // remove item from model
		currentCompType.insertCompileCommand(currCmd, idx - 2); // re-add one position up (remembering that the UI has one extra node at the top for "new")
		saveData();
		// remove and add in UI...
		listView.remove(idx); // remove item from UI list
		listView.add(currCmd.getLabel(), idx - 1); // re-add one position up
		listView.select(idx - 1);
		listView.showSelection();
		processCommandsListSelected();
		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_COMPILECMD, currCmd, null);
	}

	/**
	 * Decide if we can do the move down or not.
	 * Will decide the enabled state of the move down action.
	 */
	public boolean canMoveDown() {
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isNewSelected() && (listView.getSelectionIndex() != -1);
		if (can) {
			int idx = listView.getSelectionIndex();
			can = (idx <= (listView.getItemCount() - 2)); // -1 is to be zero-based. Another -1 is to discount "New".
		}
		return can;
	}

	/**
	 * Perform the move down action
	 */
	public void doMoveDown() {
		int idx = listView.getSelectionIndex();
		SystemCompileCommand currCmd = getCurrentlySelectedCompileCommand();
		if (currCmd == null) return; // better never happen!
		// remove and add in model...
		currentCompType.removeCompileCommand(currCmd); // remove item from model
		currentCompType.insertCompileCommand(currCmd, idx); // re-add one position down (remembering that the UI has one extra node at the top for "new")
		saveData();
		// remove and add in UI...
		listView.remove(idx); // remove item from UI list
		listView.add(currCmd.getLabel(), idx + 1); // re-add one position down
		listView.select(idx + 1);
		listView.showSelection();
		processCommandsListSelected();
		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_COMPILECMD, currCmd, null);
	}

	/**
	 * Decide if we can do the copy or not.
	 * Will decide the enabled state of the copy action.
	 */
	public boolean canCopy() {
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isNewSelected() && (listView.getSelectionIndex() != -1);
		return can;
	}

	/**
	 * Actually do the copy of the current filter string to the clipboard.
	 */
	public void doCopy() {
		if (clipboard == null) clipboard = new Clipboard(getShell().getDisplay());
		String selection = getCurrentSelection();
		TextTransfer transfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[] { selection }, new Transfer[] { transfer });
	}

	/**
	 * Decide if we can do the paste or not.
	 * Will decide the enabled state of the copy action.
	 */
	public boolean canPaste() {
		if (clipboard == null) return false;
		TextTransfer textTransfer = TextTransfer.getInstance();
		String textData = (String) clipboard.getContents(textTransfer);
		return ((textData != null) && (textData.length() > 0));
	}

	/**
	 * Actually do the copy of the current filter string to the clipboard.
	 * If an existing string is selected, it is pasted before it. Else. it is appended to the end of the list.
	 */
	public void doPaste() {
		if (clipboard == null) return;
		TextTransfer textTransfer = TextTransfer.getInstance();
		String textData = (String) clipboard.getContents(textTransfer);
		SystemCompileCommand oldCmd = currentCompType.getCompileLabel(textData);
		if (oldCmd == null) return;
		SystemCompileCommand newCmd = (SystemCompileCommand) oldCmd.clone();
		newCmd.setLabel(getUniqueCloneLabel(oldCmd));
		String newCopy = newCmd.getLabel();
		int newLocation = listView.getSelectionIndex();
		if (newLocation <= 0) {
			listView.add(newCopy);
			listView.select(listView.getItemCount() - 1);
		} else {
			listView.add(newCopy, newLocation);
			listView.select(newLocation);
		}
		listView.showSelection();
		currentCompType.insertCompileCommand(newCmd, listView.getSelectionIndex() - 1); // the "-1" is to discount for the "new" item at the top
		saveData();
		processCommandsListSelected();
		// we don't need to do the following but for consistency with change user actions and types, we do
		clipboard.dispose();
		clipboard = null;
		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, IUserActionsModelChangeEvents.SYSTEM_RESOURCETYPE_COMPILECMD, oldCmd, null);
	}

	/**
	 * Return a new unique label to assign to a pastable compile command node clone
	 */
	private String getUniqueCloneLabel(SystemCompileCommand clonableCmd) {
		String newName = SystemUDAResources.RESID_WWCOMPCMDS_COPY_NAME_1;
		newName = SystemMessage.sub(newName, "%1", clonableCmd.getLabel()); //$NON-NLS-1$
		Vector existingNames = currentCompType.getExistingLabels();
		boolean nameInUse = (existingNames.indexOf(newName) >= 0);
		int nbr = 2;
		while (nameInUse) {
			newName = SystemUDAResources.RESID_WWCOMPCMDS_COPY_NAME_N;
			newName = SystemMessage.sub(newName, "%1", clonableCmd.getLabel()); //$NON-NLS-1$
			newName = SystemMessage.sub(newName, "%2", Integer.toString(nbr)); //$NON-NLS-1$
			nameInUse = (existingNames.indexOf(newName) >= 0);
			++nbr;
		}
		return newName;
	}

	/**
	 * Decide if we can do the restore defaults or not.
	 * Will decide the enabled state of the restore defaults action.
	 */
	public boolean canRestore() {
		boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isNewSelected();
		if (can) {
			SystemCompileCommand cmd = getCurrentlySelectedCompileCommand();
			if (cmd != null)
				can = !cmd.isUserSupplied() && !cmd.getDefaultString().equals(cmd.getCurrentString());
			else
				can = false;
		}
		return can;
	}

	/**
	 * Perform the restore defaults action
	 */
	public void doRestore() {
		SystemCompileCommand cmd = getCurrentlySelectedCompileCommand();
		cmd.setCurrentString(cmd.getDefaultString());
		saveData();
		processCommandsListSelected();
	}

	/**
	 * For asynch exec we defer some operations until other pending events are processed.
	 */
	public void run() {
		if (restoreProfileComboSelection)
			profileCombo.select(prevProfileComboSelection);
		else
			srcTypeCombo.select(prevSrcTypeComboSelection);
		super.run();
	}
}
