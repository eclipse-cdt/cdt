package org.eclipse.cdt.managedbuilder.ui.properties;

/*******************************************************************************
 * Copyright (c) 2002,2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v0.5 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: IBM Rational Software - Initial API and implementation
 ******************************************************************************/

import org.eclipse.cdt.internal.ui.dialogs.SelectionStatusDialog;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The BrowseEntryDialog allows clients to prompt the user for a path location.
 * The dialog will contain a browse button to make it easy to lcate absolute
 * locations onthe target file system. The user will also be able to specify a
 * location using defined variables. The client must be able to deal with these
 * variables.
 */
public class BrowseEntryDialog extends SelectionStatusDialog {
	// String constants
	private static final String PREFIX = "BuildPropertyCommon"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String BROWSE = LABEL + ".browse"; //$NON-NLS-1$
	private static final String HIDE = "hideAdvanced"; //$NON-NLS-1$
	private static final String SHOW = "showAdvanced"; //$NON-NLS-1$
	private static final String EMPTY = "NewFolderDialog.folderNameEmpty"; //$NON-NLS-1$
	private static final String ERROR_FOLDER_NAME_INVALID = PREFIX + ".error.Folder_name_invalid"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * The title of the dialog.
	 */
	private String title = ""; //$NON-NLS-1$

	/* (non-Javadoc)
	 * The message to display, or <code>null</code> if none.
	 */
	private String message = ""; //$NON-NLS-1$

	/* (non-Javadoc)
	 * The input value; the empty string by default.
	 */
	private String folderName = ""; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * 
	 */
	private int basicShellHeight;
	
	/* (non-Javadoc)
	 * 
	 */
//	private CreateLinkedResourceGroup linkedResourceGroup;

	// Widgets
	private Button advancedButton = null;
	private Button browseButton = null;
	private Label errorMessageLabel;
	private Composite macroComposite;
	private Text text = null;

	/**
	 * Creates an input dialog with OK, Cancel, a Browse button and a button to
	 * reveal path macros.
	 * 
	 * @param shell
	 *            the parent shell
	 * @param dialogTitle
	 *            the title of the dialog or <code>null</code> if none
	 * @param dialogMessage
	 *            the dialog message, or <code>null</code> if none
	 * @param initialValue
	 *            the initial input value, or <code>null</code> if none
	 *            (equivalent to the empty string)
	 */
	public BrowseEntryDialog(Shell shell, String dialogTitle, String dialogMessage, String initialValue) {
		super(shell);
		// We are editing the value argument if it is not an empty string
		if (dialogTitle != null) {
			title = dialogTitle;
		}
		// Cache the message to be shown in the label
		if (dialogMessage != null) {
			message = dialogMessage;
		}
		// Value for the text widget
		if (initialValue != null) {
			folderName = initialValue;
		}
		setStatusLineAboveButtons(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.SelectionStatusDialog#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		// Set the display title the user has specified
		if (title != null) {
			shell.setText(title);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.SelectionStatusDialog#create()
	 */
	public void create() {
		// Disable the OK button to start
		super.create();
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
	
	/* (non-Javadoc)
	 * 
	 * @param parent
	 */
	private void createAdvancedBrowseArea(Composite parent) {
		// Instantiate the macros button
		advancedButton = new Button(parent, SWT.PUSH);
		applyDialogFont(advancedButton);
		advancedButton.setText(ManagedBuilderUIMessages.getResourceString(SHOW));
		setButtonLayoutData(advancedButton);
		GridData data = (GridData) advancedButton.getLayoutData();
		data.horizontalAlignment = GridData.BEGINNING;
		advancedButton.setLayoutData(data);
		advancedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAdvancedPressed();
			}
		});
		advancedButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				advancedButton = null;
			}
		});
		
//		linkedResourceGroup = new CreateLinkedResourceGroup(
//				IResource.FOLDER, 
//				new Listener(){
//					public void handleEvent(Event event) {
//						// TODO Auto-generated method stub
//						
//					}
//				});
	}
	
	/* (non-Javadoc)
	 * 
	 * @param parent
	 */
	private void createBasicBrowseArea(Composite parent) {
		Composite basicGroup = new Composite(parent, SWT.NONE);
		basicGroup.setLayout(new GridLayout(2, false));
		basicGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridData data;
		// Create the label
		if (message != null) {
			Label label = new Label(basicGroup, SWT.WRAP);
			label.setText(message);
			data = new GridData(			
					GridData.FILL_HORIZONTAL |
					GridData.GRAB_VERTICAL |
					GridData.VERTICAL_ALIGN_BEGINNING);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			data.horizontalSpan = 2;
			label.setLayoutData(data);
			applyDialogFont(label);
		}

		// Entry widget next
		text = new Text(basicGroup, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		text.setLayoutData(data);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateLocation();
			}
		});
		applyDialogFont(text);
		
		// Finally make the browse button
		browseButton = new Button(basicGroup, SWT.PUSH);
		applyDialogFont(browseButton);
		browseButton.setText(ManagedBuilderUIMessages.getResourceString(BROWSE));
		setButtonLayoutData(browseButton);
		data = (GridData) browseButton.getLayoutData();
		data.horizontalAlignment = GridData.BEGINNING;
		browseButton.setLayoutData(data);
		browseButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleBrowsePressed();
			}
		});
		browseButton.addDisposeListener(new DisposeListener () {
			public void widgetDisposed(DisposeEvent e) {
				browseButton = null;
			}
		});
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createBasicBrowseArea(composite);
		createAdvancedBrowseArea(composite);
		
		return composite;
	}

	/**
	 * Answers the value the user has entered in the selection dialog. 
	 * 
	 * <p>The selection will be a folder location specified in the format appropriate 
	 * for the platform that Eclipse is running on, i.e. <code>C:\foo\mydir</code> 
	 * for Windows platforms and <code>/foo/mydir</code> on POSIX platforms.
	 * 
	 * <p>The answer may also contain a path variable as a component of the location. It 
	 * is the responsibility of the client to properly handle this situation.
	 *  
	 * @return String
	 */
	public String getValue() {
		return folderName;
	}

	/* (non-Javadoc)
	 * Shows/hides the path macro widgets.
	 */
	protected void handleAdvancedPressed() {
		Shell shell = getShell();
		Point shellSize = shell.getSize();

		if (macroComposite == null) {
			basicShellHeight = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
			Composite composite = (Composite) getDialogArea();
//			macroComposite = linkedResourceGroup.createContents(composite);
			macroComposite = ControlFactory.createComposite(composite, 1);
			shellSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			shell.setSize(shellSize);
			advancedButton.setText(ManagedBuilderUIMessages.getResourceString(HIDE)); 
		} else if (macroComposite.getVisible()) {
			macroComposite.setVisible(false);
			shell.setSize(shellSize.x, basicShellHeight);
			advancedButton.setText(ManagedBuilderUIMessages.getResourceString(SHOW));
		} else {
			macroComposite.setVisible(true);
			shellSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			shell.setSize(shellSize);
			advancedButton.setText(ManagedBuilderUIMessages.getResourceString(HIDE));
		}
		
	}
	
	/* (non-Javadoc)
	 * 
	 */
	protected void handleBrowsePressed() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * Utility method to send a status message to the status line of the dialog.
	 *   
	 * @param severity
	 * @param message
	 */
	private void updateStatus(int severity, String message) {
		updateStatus(new Status(severity, ManagedBuilderCorePlugin.getUniqueIdentifier(), severity, message, null));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dialogs.SelectionStatusDialog#updateStatus(org.eclipse.core.runtime.IStatus)
	 */
	protected void updateStatus(IStatus status) {
		// TODO Auto-generated method stub
		super.updateStatus(status);
	}

	/**
	 * 
	 */
	protected void validateLocation() {
		folderName = text.getText();
		// Empty or null string is invalid
		if (folderName == null || folderName.equals("")) { //$NON-NLS-1$
			updateStatus(IStatus.ERROR, ManagedBuilderUIMessages.getResourceString(EMPTY));
			return;
		} else {
			// Make sure that the specified location exists
			IPath path = new Path(folderName);
			if (!path.isValidPath(folderName)) {
				updateStatus(IStatus.ERROR, ManagedBuilderUIMessages.getResourceString(ERROR_FOLDER_NAME_INVALID)); //$NON-NLS-1$
				return;
			}
		}
		updateStatus(IStatus.OK, "");	//$NON-NLS-1$
		getButton(IDialogConstants.OK_ID).setEnabled(true);
	}
}
