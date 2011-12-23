/*******************************************************************************
 * Copyright (c) 2010 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.tracepoints.TraceControlView.FailedTraceVariableCreationException;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceVariableDMData;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public final class TraceVarDetailsDialog extends Dialog {
	
	private Button createButton = null;
	private Button refreshButton = null;
	private Table table = null;
	private TraceControlView fView = null;
	private Text nameInput = null;
	private Text valueInput = null;
	private Image warningImage = null;
	private Label warningIconLabel = null;
	private Label warningTextLabel = null;

	public TraceVarDetailsDialog(Shell shell, TraceControlView view) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		fView = view;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(TracepointsMessages.TraceControlView_trace_variable_details_dialog_title);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create the table to show the existing variables and their values
		Composite tableComposite = new Composite(composite, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		tableComposite.setLayout(gridLayout);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		table = new Table(tableComposite, SWT.BORDER | SWT.HIDE_SELECTION);

		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 6 * table.getItemHeight();
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		nameTableColumn.setWidth(150);
		nameTableColumn.setText(TracepointsMessages.TraceControlView_trace_variable_details_column_name);
		nameTableColumn.setAlignment(SWT.LEFT);
		
		final TableColumn initValTableColumn = new TableColumn(table, SWT.NONE);
		initValTableColumn.setWidth(120);
		initValTableColumn.setText(TracepointsMessages.TraceControlView_trace_variable_details_column_init_value);
		initValTableColumn.setAlignment(SWT.RIGHT);

		final TableColumn currentValTableColumn = new TableColumn(table, SWT.NONE);
		currentValTableColumn.setWidth(120);
		currentValTableColumn.setText(TracepointsMessages.TraceControlView_trace_variable_details_column_curr_value);
		currentValTableColumn.setAlignment(SWT.RIGHT);

		refreshButton = new Button(tableComposite, SWT.NONE);
		refreshButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRefresh();
			}
		});
		refreshButton.setText(TracepointsMessages.TraceControlView_trace_variable_details_refresh_button);
		refreshButton.setEnabled(true);
		
		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// Create the section to create a new variable
		Composite createComposite = new Composite(composite, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		createComposite.setLayout(gridLayout);
		createComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Now a composite just for the input
		Composite inputComposite = new Composite(createComposite, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		inputComposite.setLayout(gridLayout);
		inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label nameLabel = new Label(inputComposite, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		nameLabel.setText(TracepointsMessages.TraceControlView_trace_variable_details_name_label);
		nameInput = new Text(inputComposite, SWT.BORDER);
		nameInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label valueLabel = new Label(inputComposite, SWT.NONE);
		valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		valueLabel.setText(TracepointsMessages.TraceControlView_trace_variable_details_value_label);
		valueInput = new Text(inputComposite, SWT.BORDER);
		valueInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// And a composite for the create button and its warning icon and text
		Composite createButtonComposite = new Composite(createComposite, SWT.NONE);
		gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		createButtonComposite.setLayout(gridLayout);
		createButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		createButton = new Button(createButtonComposite, SWT.NONE);
		createButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		createButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleCreate();
			}
		});
		createButton.setText(TracepointsMessages.TraceControlView_trace_variable_details_create_button);
		createButton.setEnabled(true);

		warningIconLabel = new Label(createButtonComposite, SWT.NONE);
		warningIconLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		warningImage = AbstractUIPlugin
				.imageDescriptorFromPlugin(
						GdbUIPlugin.PLUGIN_ID, "icons/full/obj16/warning.gif").createImage(); //$NON-NLS-1$
		warningIconLabel.setImage(warningImage);
		warningIconLabel.setVisible(false);
		
		warningTextLabel = new Label(createButtonComposite, SWT.NONE);
		warningTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		parent.addDisposeListener(new DisposeListener() {
            @Override
			public void widgetDisposed(DisposeEvent e) {
				if (warningImage != null)
					warningImage.dispose();
			}
		});
		
		// When the user goes into either input field, then pressing return
		// should try to create the command
		FocusListener clearWarningFocusListener = new FocusListener() {
            @Override
			public void focusGained(FocusEvent e) {
				getShell().setDefaultButton(createButton);
			}
            @Override
			public void focusLost(FocusEvent e) {
			}
		};
		
		nameInput.addFocusListener(clearWarningFocusListener);
		valueInput.addFocusListener(clearWarningFocusListener);

		// When the user modifies any entry in the input, we should clear any warning
		ModifyListener clearWarningListener = new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent e) {
				setWarningVisible(false);
			}
		};

		nameInput.addModifyListener(clearWarningListener);
		valueInput.addModifyListener(clearWarningListener);

		resetInputFields();
		handleRefresh();

		return composite;
	}
	
	/**
	 * Set the visibility of the warning. Should be set to true when there
	 * is a problem creating the specific variable; false otherwise
	 * 
	 * @param visible
	 *            True for visible, false for hidden.
	 */
	public void setWarningVisible(boolean visible) {
		if (warningIconLabel == null)
			return;
		warningIconLabel.setVisible(visible);
		
		if (visible == false) {
			if (warningTextLabel != null) {
				warningTextLabel.setText(""); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Set the visibility of the warning icon to true and set its
	 * text to the parameter message.
	 */
	public void setWarningVisible(String message) {
		if (warningIconLabel == null || warningTextLabel == null)
			return;
		warningTextLabel.setText(message);
		warningIconLabel.setVisible(true);
		nameInput.setFocus();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create Close button only
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL,
				true);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.CLOSE_ID == buttonId) {
			close();
		}
	}

	protected void handleRefresh() {
		ITraceVariableDMData[] vars = fView.getTraceVarList();
		if (vars == null) {
			setWarningVisible(TracepointsMessages.TraceControlView_refresh_variable_error);
			createButton.setEnabled(false);
			return;
		}
		
		// Remove the warning and re-enable the create button, since we now
		// have a list of trace variables.
		setWarningVisible(false);
		createButton.setEnabled(true);

		table.removeAll();
		for (ITraceVariableDMData var : vars) {
			String currentVal = var.getValue();
			if (currentVal == null) {
				currentVal = ""; //$NON-NLS-1$
			}
			addTableItem(var.getName(), var.getInitialValue(), currentVal);
		}
	}

	private void addTableItem(String name, String initVal, String currentVal) {
		// Find where to insert the new element
		int index = -1;
		TableItem[] items = table.getItems();
		Collator collator = Collator.getInstance(Locale.getDefault());
		for (int i = 0; i < items.length; i++) {
			String rowName = items[i].getText(0);
			if (collator.compare(name, rowName) < 0) {
				index = i;
				break;
			}
		}
		
		if (index == -1) {
			index = items.length;
		}

		TableItem tableItem = new TableItem(table, SWT.NONE, index);
		tableItem.setText(0, name);
		tableItem.setText(1, initVal);
		tableItem.setText(2, currentVal);
	}

	protected void resetInputFields() {
		nameInput.setText(""); //$NON-NLS-1$
		valueInput.setText("0"); //$NON-NLS-1$
	}
	
	protected void handleCreate() {
		String name = nameInput.getText();
		if (name != null && name.length() > 0) {
			String value = valueInput.getText();
			if (value != null && value.length() == 0) {
				value = null; // No value specified
			}
			
			try {
				fView.createVariable(name, value);
				resetInputFields();
				nameInput.setFocus();
				handleRefresh();
			} catch (FailedTraceVariableCreationException e) {
				setWarningVisible(e.getMessage());
			}
		} else {
			setWarningVisible(TracepointsMessages.TraceControlView_create_variable_empty_name_error);
		}
	}
}
