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

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The "Add Expression" dialog.
 */
public class ExpressionDialog extends Dialog {

	private Button fBtnOk = null;

	private Text fTextExpression;

	private String fExpression = ""; //$NON-NLS-1$

	/**
	 * Constructor for ExpressionDialog.
	 * 
	 * @param parentShell
	 */
	public ExpressionDialog( Shell parentShell, String expression ) {
		super( parentShell );
		if ( expression != null )
			fExpression = expression;
	}

	protected void configureShell( Shell shell ) {
		super.configureShell( shell );
		shell.setText( ActionMessages.getString( "ExpressionDialog.0" ) ); //$NON-NLS-1$
		shell.setImage( DebugPluginImages.getImage( IDebugUIConstants.IMG_OBJS_EXPRESSION ) );
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
	}

	private void initializeDataWidgets() {
		fTextExpression.setText( fExpression );
		fTextExpression.setSelection( fExpression.length() );
		fTextExpression.selectAll();
		setOkButtonState();
	}

	private Text createExpressionText( Composite parent ) {
		Label label = new Label( parent, SWT.RIGHT );
		label.setText( ActionMessages.getString( "ExpressionDialog.1" ) ); //$NON-NLS-1$
		final Text text = new Text( parent, SWT.BORDER );
		GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.widthHint = 300;
		text.setLayoutData( gridData );
		addModifyListener( text );
		return text;
	}

	protected void setOkButtonState() {
		if ( fBtnOk == null )
			return;
		fBtnOk.setEnabled( fTextExpression.getText().trim().length() > 0 );
	}

	private void storeData() {
		fExpression = fTextExpression.getText().trim();
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
