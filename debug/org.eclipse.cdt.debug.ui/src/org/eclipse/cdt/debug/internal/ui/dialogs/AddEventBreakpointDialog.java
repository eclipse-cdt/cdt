/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - Catchpoints support https://bugs.eclipse.org/bugs/show_bug.cgi?id=226689
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.dialogs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CEventBreakpoint;
import org.eclipse.cdt.debug.internal.ui.propertypages.CBreakpointPreferenceStore;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.CBreakpointUIContributionFactory;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContribution;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The "Add Catchpoint" dialog of the "Add catchpoint" action.
 */
public class AddEventBreakpointDialog extends Dialog implements ModifyListener, SelectionListener {

	private Combo fEventTypeInput;
	private String fEventType;
	private String fEventArgument;
	private Composite fEventArgumentControl;
	private HashMap<String, String> fIdLabelMap = new LinkedHashMap<String, String>();
	private FieldEditorPreferencePage page;
	private CBreakpointUIContributionFactory factory;
	private String debugModelId;

	static class ContributedFields extends FieldEditorPreferencePage {
		private String modelId;
		private String eventType;

		ContributedFields(String modelId, String eventType) {
			this.modelId = modelId;
			this.eventType = eventType;
			noDefaultAndApplyButton();
			setPreferenceStore(new CBreakpointPreferenceStore() {
				@Override
				public String getDefaultString(String name) {
					return ""; //$NON-NLS-1$
				}
			});
		}

		@Override
		protected void createFieldEditors() {
			Composite parent = getFieldEditorParent();
			try {
				Map map = new HashMap();
				map.put(ICEventBreakpoint.EVENT_TYPE_ID, eventType);
				ICBreakpointsUIContribution cons[] = CBreakpointUIContributionFactory.getInstance()
						.getBreakpointUIContributions(modelId, CEventBreakpoint.getMarkerType(), map);
				for (ICBreakpointsUIContribution con : cons) {

					if (con.getId().equals(ICEventBreakpoint.EVENT_TYPE_ID)) continue;
					if (con.getId().equals(ICBreakpointType.TYPE)) continue;
					FieldEditor fieldEditor = con.getFieldEditor(con.getId(), con.getLabel(),
							parent);
					getPreferenceStore().setValue(con.getId(),""); //$NON-NLS-1$
					if (fieldEditor != null)
						addField(fieldEditor);
				}
			} catch (Exception ce) {
				CDebugUIPlugin.log(ce);
			}
		}

	}

	/**
	 * Constructor
	 * 
	 * @param parentShell
	 */
	public AddEventBreakpointDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		factory = CBreakpointUIContributionFactory.getInstance();
		debugModelId = getDebugModelId();
		// Load events
		loadEventTypes();
	}

	protected String getDebugModelId() {
		return CDIDebugModel.getPluginIdentifier();
	}

	private void loadEventTypes() {
		ICBreakpointsUIContribution[] cons = factory.getBreakpointUIContributions(debugModelId,
				CEventBreakpoint.getMarkerType(), null);
		for (int i = 0; i < cons.length; i++) {
			ICBreakpointsUIContribution con = cons[i];
			if (con.getId().equals(ICEventBreakpoint.EVENT_TYPE_ID)) {
				String[] possibleValues = con.getPossibleValues();
				for (String value : possibleValues) {
					fIdLabelMap.put(value, con.getLabelForValue(value));
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		// The button bar will work better if we make the parent composite
		// a single column grid layout. For the widgets we add, we want a
		// a two-column grid, so we just create a sub composite for that.
		GridLayout gridLayout = new GridLayout();
		parent.setLayout(gridLayout);

		Composite composite = new Composite(parent, SWT.None);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		
		layoutData.heightHint=100;
		composite.setLayoutData(layoutData);
		composite.setLayout(new GridLayout(1, false));

		// Create the controls
		createEventTypeControl(composite);
		createEventArgumentControl(composite);

		fEventTypeInput.setFocus();
		if (fEventTypeInput.getItemCount() > 0)
			fEventTypeInput.select(0);
		updateUI();
		return parent;
	}

	protected void createEventTypeControl(Composite parent) {
		fEventTypeInput = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		fEventTypeInput.setLayoutData(gridData);
		for (String id : fIdLabelMap.keySet()) {
			addEventType(id, fIdLabelMap.get(id));
		}
		fEventTypeInput.addSelectionListener(this);
		fEventTypeInput.select(0);

	}

	protected void addEventType(String id, String label) {
		int index = fEventTypeInput.getItemCount();
		fEventTypeInput.add(label, index);
		fEventTypeInput.setData(getIdDataKey(index), id);
	}

	private String getIdDataKey(int index) {
		return "index." + index; //$NON-NLS-1$
	}

	protected void createEventArgumentControl(Composite parent) {
		fEventArgumentControl = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		//layout.marginHeight = 0;
		//layout.marginWidth = 0;
		fEventArgumentControl.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		fEventArgumentControl.setLayoutData(gridData);
		updateEventType();
		if (fEventType != null) {
			createArgumentSpecificControl();
		}
	}

	private void createArgumentSpecificControl() {
		page = new ContributedFields(getDebugModelId(), fEventType);
		page.createControl(fEventArgumentControl);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		page.getControl().setLayoutData(gridData);

	}

	private void updateArgumentsControl() {
		updateEventType();
		if (fEventTypeInput != null) {
			if (page != null) {
				Control control = page.getControl();
				control.dispose();
				page.dispose();
				page = null;
			}
			createArgumentSpecificControl();
			fEventArgumentControl.layout(true);
			// resize dialog to fit new fields
			getShell().pack();
		}
	}

	public boolean isActive(){
		return fIdLabelMap.size()>0;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DialogMessages.getString("AddEventBreakpointDialog.2")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if (fEventTypeInput != null) {
			updateEventType();
		}
		if (page != null) {
			page.performOk();
			IPreferenceStore preferenceStore = page.getPreferenceStore();
			if (preferenceStore != null) {
				fEventArgument = preferenceStore.getString(ICEventBreakpoint.EVENT_ARG);
			}
			else
				fEventArgument = null;
		}

		super.okPressed();
	}

	private void updateEventType() {
		int index = fEventTypeInput.indexOf(fEventTypeInput.getText());
		if (index == -1) {
			fEventType = null;
			return;
		}
		Object data = fEventTypeInput.getData(getIdDataKey(index));
		fEventType = (String) data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		if (e.getSource() == fEventTypeInput) {
			updateArgumentsControl();
		}
		updateUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		return super.createButtonBar(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == fEventTypeInput) {
			updateArgumentsControl();
		}
		updateUI();

	}

	private void updateUI() {
		Button b = getButton(IDialogConstants.OK_ID);
		if (b == null) {
			return;
		}
		b.setEnabled(okayEnabled());

	}

	private boolean okayEnabled() {
		// validate fields here
		String text = fEventTypeInput.getText();
		int index = fEventTypeInput.indexOf(text);
		if (index == -1) {
			return false;
		}
		return true;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// override so we can change the initial okay enabled state
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true).setEnabled(
				okayEnabled());
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public String getEventTypeId() {
		return fEventType;
	}

	public String getEventArgument() {
		return fEventArgument;
	}

}
