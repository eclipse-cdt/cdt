/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
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
 * 
 * Enter type comment.
 * 
 * @since Sep 4, 2002
 */
public class AddWatchpointDialog extends Dialog
{
	private Button fBtnOk = null;
	private Button fBtnCancel = null;
	private Text fTextExpression;
	private Button fChkBtnWrite;
	private Button fChkBtnRead;
	
	private boolean fWrite = true;
	private boolean fRead = false;
	private String fExpression = "";

	/**
	 * Constructor for AddWatchpointDialog.
	 * @param parentShell
	 */
	public AddWatchpointDialog( Shell parentShell )
	{
		super( parentShell );
	}

	protected void configureShell( Shell shell ) 
	{
		super.configureShell( shell );
		shell.setText( "Add C/C++ Watchpoint" );
		shell.setImage( CDebugImages.get( CDebugImages.IMG_OBJS_WATCHPOINT_ENABLED ) );
	}

	protected Control createContents( Composite parent ) 
	{
		Control control = super.createContents( parent );
		setOkButtonState();
		return control;
	}

	protected Control createDialogArea( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout() );
		((GridLayout)composite.getLayout()).marginWidth = 10;
		composite.setLayoutData( new GridData( GridData.FILL_BOTH  ) );
		createDataWidgets( composite );
		initializeDataWidgets();
		return composite;
	}
	
	protected void createButtonsForButtonBar( Composite parent ) 
	{
		fBtnOk = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
		fBtnCancel = createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
	}

	private void createDataWidgets( Composite parent ) 
	{
		fTextExpression	= createExpressionText( parent );
		createAccessWidgets( parent );
	}

	private void initializeDataWidgets()
	{
		fTextExpression.setText( "" );
		fChkBtnRead.setSelection( false );
		fChkBtnWrite.setSelection( true );
		setOkButtonState();
	}

	private Text createExpressionText( Composite parent )
	{
		Label label = new Label( parent, SWT.RIGHT );
		label.setText( "Expression to watch:" );	
		final Text text = new Text( parent, SWT.BORDER );
		GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.widthHint = 300;
		text.setLayoutData( gridData );
		addModifyListener( text ); 
		return text;
	}

	private void createAccessWidgets( Composite parent )
	{
		Group group = new Group( parent, SWT.NONE );
		group.setLayout( new GridLayout() );
		group.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		group.setText( "Access" );
		fChkBtnWrite = new Button( group, SWT.CHECK );
		fChkBtnWrite.setText( "Write" );
        addSelectionListener( fChkBtnWrite );
		fChkBtnRead = new Button( group, SWT.CHECK );
		fChkBtnRead.setText( "Read" );
        addSelectionListener( fChkBtnRead );
	}

	private void addSelectionListener( Button button )
	{
        button.addSelectionListener(
        			new SelectionAdapter() 
        				{
							public void widgetSelected( SelectionEvent e ) 
							{
								setOkButtonState();
							}
        				} );
	}

	private void setOkButtonState()
	{
		if ( fBtnOk == null )
			return;
		fBtnOk.setEnabled( (fChkBtnRead.getSelection() || fChkBtnWrite.getSelection()) &&
							fTextExpression.getText().trim().length() > 0 );
	}

	private void storeData()
	{
		fExpression = fTextExpression.getText().trim();
		fRead = fChkBtnRead.getSelection();
		fWrite = fChkBtnWrite.getSelection();
	}

	private void addModifyListener( Text text )
	{
		text.addModifyListener( 
					new ModifyListener() 
						{
							public void modifyText( ModifyEvent e )
							{
								setOkButtonState();
							}
						} );
	}
	
	public String getExpression()
	{
		return fExpression;
	}
	
	public boolean getWriteAccess()
	{
		return fWrite;
	}
	
	public boolean getReadAccess()
	{
		return fRead;
	}
}
