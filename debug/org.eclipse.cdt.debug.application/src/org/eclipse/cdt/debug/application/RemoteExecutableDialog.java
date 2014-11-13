/*******************************************************************************
 * Copyright (c) 2013, 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 * Red Hat Inc. - modified for use in Standalone Debugger
 * Marc Khouzam (Ericsson) - Modified for remote launch
 *******************************************************************************/

package org.eclipse.cdt.debug.application;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RemoteExecutableDialog extends TitleAreaDialog {

	private RemoteExecutableInfo fInfo = null;
	
	private Text fHostBinaryText;
	private Text fBuildLogText;
	private Text fAddressText;
	private Text fPortText;
	private Button fAttachButton;
	
	private final String fHostBinary;
	private final String fBuildLog;
	private final String fAddress;
	private final String fPort;

	public RemoteExecutableDialog (Shell parentShell) {
		this(parentShell, null, null, null, null);
	}

	public RemoteExecutableDialog( Shell parentShell, String hostBinary, String buildLog, String address, String port) {
		super( parentShell );
		setShellStyle( getShellStyle() | SWT.RESIZE );
		fHostBinary = hostBinary;
		fBuildLog = buildLog;
		fAddress = address;
		fPort = port;
	}

	@Override
	protected Control createContents( Composite parent ) {
		Control control = super.createContents( parent );
		validate();
		return control;
	}

	@Override
	protected Control createDialogArea( Composite parent ) {

		getShell().setText( Messages.GdbDebugNewExecutableCommand_Debug_New_Executable ); 
		setTitle( Messages.GdbDebugNewExecutableCommand_Select_Binary );
		String message = Messages.GdbDebugNewExecutableCommand_Select_binary_and_specify_arguments;
		setMessage( message );

		Composite control = (Composite)super.createDialogArea( parent );
		Composite comp = new Composite( control, SWT.NONE );
		GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
		GridLayout layout = new GridLayout( 3, false );
		comp.setLayout( layout );
		comp.setLayoutData( gd );
		
		new Label( comp, SWT.None ).setText( Messages.GdbDebugNewExecutableCommand_Binary );
		fHostBinaryText = new Text( comp, SWT.BORDER );
		if (fHostBinary != null)
			fHostBinaryText.setText(fHostBinary);
		fHostBinaryText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		fHostBinaryText.addModifyListener( new ModifyListener() {
			
			@Override
			public void modifyText( ModifyEvent e ) {
				validate();
			}
		} );
		Button browseButton = new Button( comp, SWT.PUSH );
		browseButton.setText( Messages.GdbDebugNewExecutableCommand_Browse );
		browseButton.setFont( JFaceResources.getDialogFont() );
		setButtonLayoutData( browseButton );
		browseButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected( SelectionEvent e ) {
				FileDialog dialog = new FileDialog( getShell() );
				dialog.setFileName( fHostBinaryText.getText() );
				String result = dialog.open();
				if ( result != null ) {
					fHostBinaryText.setText( result );
				}
			}
		} );
		
		new Label( comp, SWT.None ).setText( Messages.GdbDebugNewExecutableCommand_BuildLog );
		fBuildLogText = new Text( comp, SWT.BORDER );
		if (fBuildLog != null)
			fBuildLogText.setText(fBuildLog);
		fBuildLogText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
		fBuildLogText.addModifyListener( new ModifyListener() {
			
			@Override
			public void modifyText( ModifyEvent e ) {
				validate();
			}
		} );

		new Label( comp, SWT.None ).setText( "Host name or IP adress" );
		fAddressText = new Text( comp, SWT.BORDER );
		if (fAddress != null)
			fAddressText.setText(fAddress);
		fAddressText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
		fAddressText.addModifyListener( new ModifyListener() {
			
			@Override
			public void modifyText( ModifyEvent e ) {
				validate();
			}
		} );

		new Label( comp, SWT.None ).setText( "Port number" );
		fPortText = new Text( comp, SWT.BORDER );
		if (fPort != null)
			fPortText.setText(fPort);
		fPortText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
		fPortText.addModifyListener( new ModifyListener() {
			
			@Override
			public void modifyText( ModifyEvent e ) {
				validate();
			}
		} );

		fAttachButton = new Button( comp, SWT.CHECK);
		fAttachButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				validate();
			}
		});
		new Label( comp, SWT.None ).setText( "Attach" );

		return control;
	}

	@Override
	protected void okPressed() {
		fInfo = new RemoteExecutableInfo( fHostBinaryText.getText().trim(), 
				                          fBuildLogText.getText().trim(),
				                          fAddressText.getText().trim(),
				                          fPortText.getText().trim(),
				                          fAttachButton.getSelection() );
		super.okPressed();
	}

	public RemoteExecutableInfo getExecutableInfo() {
		return fInfo;
	}

	private void validate() {
		String error = null;
		
		String hostBinary = fHostBinaryText.getText().trim();
		if (hostBinary.isEmpty()) {
			boolean attach = fAttachButton.getSelection();
			if (!attach) error = Messages.GdbDebugNewExecutableCommand_Binary_must_be_specified;
		}
		else {
			File file = new File(hostBinary);
			if (!file.exists() ) {
				error = Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist;
			}
			else if (file.isDirectory()) {
				error = Messages.GdbDebugNewExecutableCommand_Invalid_binary;
			}
		}
		
		String buildLog = fBuildLogText.getText();
		if (error == null && !buildLog.isEmpty()) {
			File file = new File(buildLog);
			if (!file.exists()) {
				error = Messages.GdbDebugNewExecutableCommand_BuildLog_file_does_not_exist;
			}
			else if (file.isDirectory()) {
				error = Messages.GdbDebugNewExecutableCommand_Invalid_buildLog;
			}
		}

		String address = fAddressText.getText().trim();
		if (error == null && address.isEmpty()) {
			error = Messages.GdbDebugRemoteExecutableCommand_address_must_be_specified;
		}

		String port = fPortText.getText().trim();
		if (error == null) {
			if (port.isEmpty()) {
				error = Messages.GdbDebugRemoteExecutableCommand_port_must_be_specified;
			} else {
				try {
					Integer.parseInt(port);
				} catch (NumberFormatException e) {
					error = Messages.GdbDebugRemoteExecutableCommand_port_must_be_a_number;
				}
			}
		}

		setErrorMessage((error != null ) ? error : null);
		getButton(IDialogConstants.OK_ID).setEnabled(getErrorMessage() == null);
	}
}