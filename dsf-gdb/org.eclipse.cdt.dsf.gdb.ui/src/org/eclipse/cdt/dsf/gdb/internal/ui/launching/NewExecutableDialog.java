/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.io.File;

import org.eclipse.cdt.dsf.gdb.internal.ui.commands.Messages;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewExecutableDialog extends TitleAreaDialog {

	public static final int REMOTE = 0x1;

	private int fFlags = 0;
	private NewExecutableInfo fInfo = null;
	
	private Text fHostBinaryText;
	private Text fTargetBinaryText;
	private Text fArgumentsText;

	public NewExecutableDialog( Shell parentShell, int flags ) {
		super( parentShell );
		setShellStyle( getShellStyle() | SWT.RESIZE );
		fFlags = flags;
	}

	@Override
	protected Control createContents( Composite parent ) {
		Control control = super.createContents( parent );
		validate();
		return control;
	}

	@Override
	protected Control createDialogArea( Composite parent ) {
		boolean remote = (fFlags & REMOTE) > 0;

		getShell().setText( Messages.GdbDebugNewExecutableCommand_Debug_New_Executable ); 
		setTitle( Messages.GdbDebugNewExecutableCommand_Select_Binary );
		String message = ( remote ) ? 
				Messages.GdbDebugNewExecutableCommand_Select_binaries_on_host_and_target :
				Messages.GdbDebugNewExecutableCommand_Select_binary_and_specify_arguments;
		setMessage( message );

		Composite control = (Composite)super.createDialogArea( parent );
		Composite comp = new Composite( control, SWT.NONE );
		GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
		GridLayout layout = new GridLayout( 3, false );
		comp.setLayout( layout );
		comp.setLayoutData( gd );
		
		new Label( comp, SWT.None ).setText( remote ? Messages.GdbDebugNewExecutableCommand_Binary_on_host : Messages.GdbDebugNewExecutableCommand_Binary );
		fHostBinaryText = new Text( comp, SWT.BORDER );
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
		
		if ( remote ) {
			new Label( comp, SWT.None ).setText( Messages.GdbDebugNewExecutableCommand_Binary_on_target );
			fTargetBinaryText = new Text( comp, SWT.BORDER );
			fTargetBinaryText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
			fTargetBinaryText.addModifyListener( new ModifyListener() {
				
				@Override
				public void modifyText( ModifyEvent e ) {
					validate();
				}
			} );
		}

		new Label( comp, SWT.None ).setText( Messages.GdbDebugNewExecutableCommand_Arguments );
		fArgumentsText = new Text( comp, SWT.BORDER );
		fArgumentsText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

		return control;
	}

	@Override
	protected void okPressed() {
		String targetPath = ( fTargetBinaryText != null ) ? fTargetBinaryText.getText().trim() : null;
		String args = fArgumentsText.getText().trim();
		fInfo = new NewExecutableInfo( fHostBinaryText.getText().trim(), targetPath, args );
		super.okPressed();
	}

	public NewExecutableInfo getExecutableInfo() {
		return fInfo;
	}
	
	private void validate() {
		boolean remote = (fFlags & REMOTE) > 0;
		StringBuilder sb = new StringBuilder();
		String hostBinary = fHostBinaryText.getText().trim();
		if ( hostBinary.isEmpty() ) {
			sb.append( ( remote ) ? 
				Messages.GdbDebugNewExecutableCommand_Host_binary_must_be_specified : 
				Messages.GdbDebugNewExecutableCommand_Binary_must_be_specified );
		}
		else {
			File file = new File( hostBinary );
			if ( !file.exists() ) {
				sb.append( ( remote ) ? 
					Messages.GdbDebugNewExecutableCommand_Host_binary_file_does_not_exist :
					Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist );
			}
			else if ( file.isDirectory() ) {
				sb.append( ( remote ) ?
					Messages.GdbDebugNewExecutableCommand_Invalid_host_binary :
					Messages.GdbDebugNewExecutableCommand_Invalid_binary );
			}
		}
		if ( fTargetBinaryText != null ) {
			if ( fTargetBinaryText.getText().trim().length() == 0 ) {
				if ( sb.length() != 0 ) {
					sb.append( "\n " ); //$NON-NLS-1$
				}
				sb.append( Messages.GdbDebugNewExecutableCommand_Binary_on_target_must_be_specified );
			}
		}
		setErrorMessage( ( sb.length() != 0 ) ? sb.toString() : null );
		getButton( IDialogConstants.OK_ID ).setEnabled( getErrorMessage() == null );
	}
}