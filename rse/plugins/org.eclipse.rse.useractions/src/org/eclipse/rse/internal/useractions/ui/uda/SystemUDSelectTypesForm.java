package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.Vector;

import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.ui.uda.actions.SystemWorkWithFileTypesAction;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * This is a subclassable and configurable encapsulation of a
 * composite that allows users to select file types from a master
 * list, as well as edit that master list.
 * <p>
 * It is used in the edit pane of the Work With User Actions
 * dialog, to allow the user to indicate which file types this
 * action is scoped to.
 */
public class SystemUDSelectTypesForm implements SelectionListener {
	// inputs
	protected Shell shell;
	protected ISubSystem subsystem = null;
	protected ISubSystemConfiguration subsystemFactory = null;
	protected ISystemProfile profile;
	protected SystemUDTypeManager udtm;
	protected int domain;
	protected String groupLabel, groupTooltip;
	protected String masterListLabel, masterListTooltip;
	protected String ourListLabel, ourListTooltip;
	protected String ALL_TYPE = "ALL"; //$NON-NLS-1$
	protected String[] inpMasterTypes = { ALL_TYPE };
	protected String[] inpSelectedTypes = { ALL_TYPE };
	protected Vector listeners = new Vector();
	// widgets
	protected Composite composite_prompts;
	protected List masterList;
	protected List ourList;
	protected Button addButton, rmvButton, editButton;
	protected Label verbageLabel;
	protected Label msgLine;
	// static
	protected String[] ALL_TYPE_ARRAY = { ALL_TYPE };

	/**
	 * Constructor for SystemUDSelectTypesForm, when we have a subsystem.
	 */
	public SystemUDSelectTypesForm(Shell shell, ISubSystem subsystem, SystemUDTypeManager mgr) {
		super();
		this.shell = shell;
		this.subsystem = subsystem;
		this.subsystemFactory = subsystem.getSubSystemConfiguration();
		this.profile = subsystem.getSystemProfile();
		this.udtm = mgr;
		setGroupLabel(SystemUDAResources.RESID_UDA_TYPE_LIST_LABEL, SystemUDAResources.RESID_UDA_TYPE_LIST_TOOLTIP);
		setMasterListLabel(SystemUDAResources.RESID_UDA_TYPE_LIST_MASTER_LABEL, SystemUDAResources.RESID_UDA_TYPE_LIST_MASTER_TOOLTIP);
		setSelectedListLabel(SystemUDAResources.RESID_UDA_TYPE_LIST_SELECTED_LABEL, SystemUDAResources.RESID_UDA_TYPE_LIST_SELECTED_TOOLTIP);
	}

	/**
	 * Constructor for SystemUDSelectTypesForm, when we have a subsystem factory and profile
	 */
	public SystemUDSelectTypesForm(Shell shell, ISubSystemConfiguration subsystemFactory, ISystemProfile profile, SystemUDTypeManager mgr) {
		super();
		this.shell = shell;
		this.subsystemFactory = subsystemFactory;
		this.profile = profile;
		this.udtm = mgr;
		setGroupLabel(SystemUDAResources.RESID_UDA_TYPE_LIST_LABEL, SystemUDAResources.RESID_UDA_TYPE_LIST_TOOLTIP);
		setMasterListLabel(SystemUDAResources.RESID_UDA_TYPE_LIST_MASTER_LABEL, SystemUDAResources.RESID_UDA_TYPE_LIST_MASTER_TOOLTIP);
		setSelectedListLabel(SystemUDAResources.RESID_UDA_TYPE_LIST_SELECTED_LABEL, SystemUDAResources.RESID_UDA_TYPE_LIST_SELECTED_TOOLTIP);
	}

	// ------------------------	
	// CONFIGURATION METHODS...
	// ------------------------
	/**
	 * Set what type string represents "all". 
	 * The default is "ALL"
	 */
	public void setAllType(String allType) {
		this.ALL_TYPE = allType;
		ALL_TYPE_ARRAY = new String[] { allType };
	}

	/**
	 * Configuration method.
	 * <p>
	 * Set the verbage and tooltip for the overall group
	 * <p>
	 */
	public void setGroupLabel(String label, String tooltip) {
		this.groupLabel = label;
		this.groupTooltip = tooltip;
	}

	/**
	 * Configuration method.
	 * Set the label and tooltip for the master list of all defined types
	 */
	public void setMasterListLabel(String label, String tooltip) {
		this.masterListLabel = label;
		this.masterListTooltip = tooltip;
	}

	/**
	 * Configuration method.
	 * Set the label and tooltip for the user-select list of types for this action,
	 */
	public void setSelectedListLabel(String label, String tooltip) {
		this.ourListLabel = label;
		this.ourListTooltip = tooltip;
	}

	/**
	 * Set the whole form to be visible or not
	 */
	public void setVisible(boolean visible) {
		if (composite_prompts != null) {
			verbageLabel.setVisible(visible);
			composite_prompts.setVisible(visible);
		}
	}

	/**
	 * Set the domain of the action we are creating or editing.
	 */
	public void setDomain(int domain) {
		this.domain = domain;
	}

	// ---------------------------------
	// LISTENER CONFIGURATION METHODS...
	// ---------------------------------
	public void addSelectionListener(ISystemUDSelectTypeListener l) {
		listeners.add(l);
	}

	// -----------------------------	
	// DATA CONFIGURATION METHODS...
	// -----------------------------
	/**
	 * Set the initial master list of all defined types
	 */
	public void setMasterTypes(String[] types) {
		this.inpMasterTypes = types;
		if (masterList != null) {
			masterList.removeAll();
			if (types != null) {
				masterList.setItems(types);
				if (types.length > 0) {
					masterList.select(0);
					addButton.setEnabled(true);
				}
			}
			setMessage(masterList);
		}
	}

	/**
	 * Set the initial list of all types selected for this action.
	 * For "new" actions, you don't have to call this to insert ALL,
	 * as that is done for you
	 */
	public void setTypes(String[] types) {
		this.inpSelectedTypes = types;
		if (ourList != null) {
			ourList.removeAll();
			if (types != null) {
				ourList.setItems(types);
				if (types.length > 0) {
					ourList.select(0);
				}
			}
			enableDisableRmvButton();
			setMessage(ourList);
		}
	}

	/**
	 * Reset the master types list to just "ALL"
	 */
	public void resetMasterTypes() {
		setMasterTypes(ALL_TYPE_ARRAY);
	}

	/**
	 * Reset the user-selected types to just "ALL"
	 */
	public void resetTypes() {
		setTypes(ALL_TYPE_ARRAY);
	}

	/**
	 * Reset state (like when now working on a new action)
	 */
	public void reset() {
		resetMasterTypes();
		resetTypes();
		/*
		 masterList.removeAll();
		 inpMasterTypes = ALL_TYPE_ARRAY;
		 masterList.setItems(inpMasterTypes);

		 inpSelectedTypes = ALL_TYPE_ARRAY;
		 ourList.removeAll();
		 ourList.setItems(inpSelectedTypes);
		 */
	}

	// --------------------------	
	// DATA EXTRACTION METHODS...
	// --------------------------
	/**
	 * Return the master list of defined types.
	 * This may have changed by way of the user pressing Edit
	 */
	public String[] getMasterTypes() {
		return masterList.getItems();
	}

	/**
	 * Return the list of user-selected types, as an array of strings.
	 * Never an empty list! Enforced to select at least one type, which is defaulted to <ALL>
	 */
	public String[] getTypes() {
		return ourList.getItems();
	}

	// ------------------------	
	// INTERNAL METHODS...
	// ------------------------
	/**
	 * Create the widgets and populate the composite.
	 * @param parent - the parent composite these widgets will be added to (actually we create our own composite to hold the widgets)
	 * @param span - the number of columns within the parent composite that our widgets are to span
	 */
	public Composite createContents(Composite parent, int span) {
		verbageLabel = SystemWidgetHelpers.createVerbiage(parent, groupLabel, span, false, -1);
		//addFillerLine(parent, span);
		int nbrColumns = 3;
		//composite_prompts = SystemWidgetHelpers.createGroupComposite(parent, nbrColumns, groupLabel);
		composite_prompts = SystemWidgetHelpers.createFlushComposite(parent, nbrColumns);
		composite_prompts.setToolTipText(groupTooltip);
		((GridData) composite_prompts.getLayoutData()).horizontalSpan = span;
		((GridLayout) composite_prompts.getLayout()).marginHeight = 0;
		((GridLayout) composite_prompts.getLayout()).marginWidth = 2;
		masterList = createListBox(composite_prompts, masterListLabel, masterListTooltip);
		Composite middle_composite = SystemWidgetHelpers.createComposite(composite_prompts, 1);
		((GridLayout) middle_composite.getLayout()).marginWidth = 0;
		SystemWidgetHelpers.createLabel(middle_composite, ""); //$NON-NLS-1$
		Composite button_composite = SystemWidgetHelpers.createTightComposite(middle_composite, 1);
		addButton = SystemWidgetHelpers.createPushButton(button_composite, null, SystemUDAResources.RESID_UDA_TYPE_ADD_BUTTON_LABEL, SystemUDAResources.RESID_UDA_TYPE_ADD_BUTTON_TOOLTIP);
		rmvButton = SystemWidgetHelpers.createPushButton(button_composite, null, SystemUDAResources.RESID_UDA_TYPE_RMV_BUTTON_LABEL, SystemUDAResources.RESID_UDA_TYPE_RMV_BUTTON_TOOLTIP);
		editButton = SystemWidgetHelpers.createPushButton(button_composite, null, SystemUDAResources.RESID_UDA_TYPE_EDIT_BUTTON_LABEL, SystemUDAResources.RESID_UDA_TYPE_EDIT_BUTTON_TOOLTIP);
		Label bottomFiller = SystemWidgetHelpers.createLabel(button_composite, ""); //$NON-NLS-1$
		((GridData) bottomFiller.getLayoutData()).grabExcessVerticalSpace = true;
		((GridData) bottomFiller.getLayoutData()).verticalAlignment = GridData.FILL;
		ourList = createListBox(composite_prompts, ourListLabel, ourListTooltip);
		msgLine = SystemWidgetHelpers.createLabel(composite_prompts, "");//, 1, true); //$NON-NLS-1$
		((GridData) msgLine.getLayoutData()).horizontalSpan = nbrColumns;
		((GridData) msgLine.getLayoutData()).widthHint = 150;
		// add our own listeners to our own widgets
		masterList.addSelectionListener(this);
		ourList.addSelectionListener(this);
		addButton.addSelectionListener(this);
		rmvButton.addSelectionListener(this);
		editButton.addSelectionListener(this);
		boolean enableAdd = false;
		boolean enableRmv = false;
		// prefill data
		if (inpMasterTypes != null) {
			masterList.setItems(inpMasterTypes);
			if (inpMasterTypes.length > 0) {
				masterList.select(0);
				enableAdd = true;
			}
		}
		if (inpSelectedTypes != null) {
			ourList.setItems(inpSelectedTypes);
			if (inpSelectedTypes.length > 0) {
				ourList.select(0);
				enableRmv = !inpSelectedTypes[0].equals(ALL_TYPE);
			}
		}
		setMessage(ourList);
		// initially disable buttons
		addButton.setEnabled(enableAdd);
		rmvButton.setEnabled(enableRmv);
		return composite_prompts;
	}

	/** 
	 * create list box
	 */
	private List createListBox(Composite c, String label, String tooltip) {
		List listbox = SystemWidgetHelpers.createListBox(c, null, false, label, tooltip);
		((GridData) listbox.getLayoutData()).widthHint = 50;
		((GridData) listbox.getLayoutData()).heightHint = 78; // 120
		return listbox;
	}

	/**
	 * enable/disable rmv button
	 */
	private void enableDisableRmvButton() {
		int selIdx = ourList.getSelectionIndex();
		rmvButton.setEnabled((selIdx >= 0) && !((ourList.getItemCount() == 1) && (ourList.getItem(0).equals(ALL_TYPE))));
	}

	/**
	 * SelectionListener interface.
	 * Called when button selected
	 */
	public void widgetSelected(SelectionEvent event) {
		Object src = event.getSource();
		boolean fireEvent = false;
		if (src == masterList) {
			int selIdx = masterList.getSelectionIndex();
			addButton.setEnabled(selIdx >= 0);
			enableDisableRmvButton();
			setMessage(masterList);
		} else if (src == ourList) {
			enableDisableRmvButton();
			setMessage(ourList);
		} else if (src == rmvButton) {
			int selIdx = ourList.getSelectionIndex();
			if (selIdx < 0) return;
			ourList.remove(selIdx);
			if (ourList.getItemCount() == 0) ourList.add(ALL_TYPE);
			enableDisableRmvButton();
			fireEvent = true;
		} else // add or edit
		{
			String[] selected = masterList.getSelection();
			String selection = null;
			if ((selected != null) && (selected.length > 0)) {
				selection = selected[0];
			}
			int selIdx = -1;
			if (src == addButton) {
				if (selected == null || selection == null) // should never happen if our enablement is correct
				{
					addButton.setEnabled(false);
					return;
				}
				// is the selected type already in the selected-list?
				selIdx = ourList.indexOf(selection);
				if (selIdx < 0) // no, not already in list
				{
					if (selection.equals(ALL_TYPE)) // adding ALL?
						ourList.removeAll();
					else {
						try {
							ourList.remove(ALL_TYPE);
						} catch (Exception exc) {
						}
					}
					ourList.add(selection);
					enableDisableRmvButton();
					fireEvent = true;
				}
			} else if (src == editButton) {
				SystemWorkWithFileTypesAction editTypesAction = null;
				if (subsystem != null)
					editTypesAction = new SystemWorkWithFileTypesAction(shell, udtm.getActionSubSystem());
				else
					editTypesAction = new SystemWorkWithFileTypesAction(shell, subsystemFactory, profile);
				if (selection != null) {
					editTypesAction.preSelectType(domain, selection);
				}
				editTypesAction.run();
				String outputSelectedTypeName = editTypesAction.getSelectedTypeName();
				int outputSelectedTypeDomain = editTypesAction.getSelectedTypeDomain();
				//System.out.println("outputSelectedTypeName = " + outputSelectedTypeName);
				// the following will result in a callback to us to refresh the master list
				fireSelectedListChange(false, true);
				// now, select something in master list
				if ((outputSelectedTypeName != null) && (outputSelectedTypeDomain == domain)) {
					masterList.setSelection(new String[] { outputSelectedTypeName });
					masterList.showSelection();
				} else if (selection != null) masterList.setSelection(new String[] { selection });
			}
		}
		if (fireEvent) fireSelectedListChange(true, false);
	}

	/**
	 * SelectionListener interface.
	 * Called when enter pressed on widget we are listening to
	 */
	public void widgetDefaultSelected(SelectionEvent event) {
	}

	/**
	 * Set the text in the message line below the lists
	 */
	private void setMessage(List listbox) {
		int selIdx = listbox.getSelectionIndex();
		if (selIdx < 0)
			msgLine.setText(""); //$NON-NLS-1$
		else {
			String type = listbox.getItem(selIdx);
			String types = udtm.getTypesForTypeName(type, domain);
			if (types == null) {
				msgLine.setText(""); //$NON-NLS-1$
				msgLine.setToolTipText(""); //$NON-NLS-1$
			} else {
				String msg = null;
				if (types.length() > 35)
					msg = type + ": " + types.substring(0, 34) + "..."; //$NON-NLS-1$ //$NON-NLS-2$
				else
					msg = type + ": " + types; //$NON-NLS-1$
				//System.out.println(msg);
				msgLine.setText(msg);
				msgLine.setToolTipText(types);
			}
		}
	}

	/**
	 * The user has changed the selected-types list.
	 * Inform all listeners
	 */
	private void fireSelectedListChange(boolean selectedListChanged, boolean masterListChanged) {
		for (int idx = 0; idx < listeners.size(); idx++) {
			if (selectedListChanged) ((ISystemUDSelectTypeListener) listeners.elementAt(idx)).selectedTypeListChanged(this);
			if (masterListChanged) ((ISystemUDSelectTypeListener) listeners.elementAt(idx)).masterTypeListChanged(this);
		}
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
