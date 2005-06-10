/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The "Add Watchpoint" dialog of the "Toggle watchpoint" action.
 */
public class AddWatchpointDialog extends Dialog {

	private Button fBtnOk = null;

	private Text fTextExpression;

	private Button fChkBtnWrite;

	private Button fChkBtnRead;

	private boolean fWrite = true;

	private boolean fRead = false;

	private String fExpression = ""; //$NON-NLS-1$

	private boolean fEditable = true;

	/**
	 * Constructor for AddWatchpointDialog.
	 * 
	 * @param parentShell
	 */
	public AddWatchpointDialog( Shell parentShell, boolean write, boolean read, String expression, boolean editable ) {
		super( parentShell );
		fWrite = write;
		fRead = read;
		if ( expression != null )
			fExpression = expression;
		fEditable = editable;
	}

	protected void configureShell( Shell shell ) {
		super.configureShell( shell );
		shell.setText( ActionMessages.getString( "AddWatchpointDialog.0" ) ); //$NON-NLS-1$
		shell.setImage( CDebugImages.get( CDebugImages.IMG_OBJS_WATCHPOINT_ENABLED ) );
	}

	protected Control createContents( Composite parent ) {
		Control control = super.createContents( parent );
		setOkButtonState();
		return control;
	}

	protected Control createDialogArea( Composite parent ) {
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout() );
		((GridLayout)composite.getLayout()).marginWidth = 10;
		composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		createDataWidgets( composite );
		initializeDataWidgets();
		return composite;
	}

	protected void createButtonsForButtonBar( Composite parent ) {
		fBtnOk = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
		createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
	}

	private void createDataWidgets( Composite parent ) {
		fTextExpression = createExpressionText( parent );
		createAccessWidgets( parent );
	}

	private void initializeDataWidgets() {
		fTextExpression.setText( fExpression );
		fChkBtnRead.setSelection( fRead );
		fChkBtnWrite.setSelection( fWrite );
		setOkButtonState();
	}

	private Text createExpressionText( Composite parent ) {
		Label label = new Label( parent, SWT.RIGHT );
		label.setText( ActionMessages.getString( "AddWatchpointDialog.1" ) ); //$NON-NLS-1$
		final Text text = new Text( parent, SWT.BORDER );
		GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.widthHint = 300;
		text.setLayoutData( gridData );
		text.setEnabled( fEditable );
		addModifyListener( text );
		return text;
	}

	private void createAccessWidgets( Composite parent ) {
		Group group = new Group( parent, SWT.NONE );
		group.setLayout( new GridLayout() );
		group.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		group.setText( ActionMessages.getString( "AddWatchpointDialog.2" ) ); //$NON-NLS-1$
		fChkBtnWrite = new Button( group, SWT.CHECK );
		fChkBtnWrite.setText( ActionMessages.getString( "AddWatchpointDialog.3" ) ); //$NON-NLS-1$
		addSelectionListener( fChkBtnWrite );
		fChkBtnRead = new Button( group, SWT.CHECK );
		fChkBtnRead.setText( ActionMessages.getString( "AddWatchpointDialog.4" ) ); //$NON-NLS-1$
		addSelectionListener( fChkBtnRead );
	}

	private void addSelectionListener( Button button ) {
		button.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent e ) {
				setOkButtonState();
			}
		} );
	}

	protected void setOkButtonState() {
		if ( fBtnOk == null )
			return;
		fBtnOk.setEnabled( (fChkBtnRead.getSelection() || fChkBtnWrite.getSelection()) && fTextExpression.getText().trim().length() > 0 );
	}

	private void storeData() {
		fExpression = fTextExpression.getText().trim();
		fRead = fChkBtnRead.getSelection();
		fWrite = fChkBtnWrite.getSelection();
	}

	private void addModifyListener( Text text ) {
		text.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent e ) {
				setOkButtonState();
			}
		} );
	}

	public String getExpression() {
		return fExpression;
	}

	public boolean getWriteAccess() {
		return fWrite;
	}

	public boolean getReadAccess() {
		return fRead;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		storeData();
		super.okPressed();
	}
}
