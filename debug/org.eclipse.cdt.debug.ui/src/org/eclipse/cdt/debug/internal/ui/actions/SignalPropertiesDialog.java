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

import java.text.MessageFormat;

import org.eclipse.cdt.debug.internal.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Signal Properties dialog.
 */
public class SignalPropertiesDialog extends Dialog {

	private String fName;
	private String fDescription;
	private boolean fPass = false;
	private boolean fStop = false;
	private boolean fCanModify = false;
	
	private SelectionButtonDialogField fPassButton;
	private SelectionButtonDialogField fStopButton;

	/**
	 * Constructor for SignalPropertiesDialog.
	 * @param parentShell
	 */
	public SignalPropertiesDialog( Shell parentShell, 
								   String name, 
								   String description,
								   boolean pass,
								   boolean stop,
								   boolean canModify ) {
		super( parentShell );
		fName = name;
		fDescription = description;
		fStop = stop;
		fPass = pass;
		fCanModify = canModify;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell( Shell shell ) {
		super.configureShell( shell );
		shell.setText( MessageFormat.format( ActionMessages.getString( "SignalPropertiesDialog.Title_1" ), new String[] { getName() } ) ); //$NON-NLS-1$
	}

	protected String getDescription() {
		return this.fDescription;
	}

	protected String getName() {
		return this.fName;
	}

	protected boolean isPassEnabled() {
		return this.fPass;
	}
	
	protected void enablePass( boolean enable ) {
		this.fPass = enable;
	}
	
	protected boolean isStopEnabled() {
		return this.fStop;
	}
	
	protected void enableStop( boolean enable ) {
		this.fStop = enable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea( Composite parent ) {
		Font font = parent.getFont();
		Composite composite = (Composite)super.createDialogArea( parent );

		// Create description field
		Label label = new Label( composite, SWT.WRAP );
		label.setText( MessageFormat.format( ActionMessages.getString( "SignalPropertiesDialog.Description_label_1" ), new String[] { getDescription() } ) ); //$NON-NLS-1$
		GridData data = new GridData( GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER );
		data.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
		label.setLayoutData( data );
		label.setFont( font );

		// Create pass button
		fPassButton = new SelectionButtonDialogField( SWT.CHECK );
		fPassButton.setLabelText( ActionMessages.getString( "SignalPropertiesDialog.Pass_label_1" ) ); //$NON-NLS-1$
		fPassButton.setSelection( fPass );
		fPassButton.setEnabled( fCanModify );
		fPassButton.doFillIntoGrid( composite, 1 );

		// Create stop button
		fStopButton = new SelectionButtonDialogField( SWT.CHECK );
		fStopButton.setLabelText( ActionMessages.getString( "SignalPropertiesDialog.Stop_label_1" ) ); //$NON-NLS-1$
		fStopButton.setSelection( fStop );
		fStopButton.setEnabled( fCanModify );
		fStopButton.doFillIntoGrid( composite, 1 );

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if ( fPassButton != null )
			fPass = fPassButton.isSelected();
		if ( fStopButton != null )
			fStop = fStopButton.isSelected();
		super.okPressed();
	}
}
