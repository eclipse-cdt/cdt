/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Enter type comment.
 * 
 * @since: Dec 12, 2002
 */
public class AttachSourceLocationBlock
{
	private Composite fControl = null;
	private Text fLocationText = null;
	private Text fAssociationText = null;
	private Button fAssocitedCheckButton = null;
	private FontMetrics fFontMetrics;
	private Shell fShell = null;

	/**
	 * Constructor for AttachSourceLocationBlock.
	 */
	public AttachSourceLocationBlock()
	{
	}
	
	public void createControl( Composite parent )
	{
		fShell = parent.getShell();
		fControl = new Composite( parent, SWT.NONE );
		fControl.setLayout( new GridLayout() );
		fControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		fControl.setFont( JFaceResources.getDialogFont() );	
		initializeDialogUnits( fControl );

		createLocationControls( fControl );
		createAssociationControls( fControl );
	}
	
	public void setInitialLocationPath( IPath path )
	{
		if ( path != null )
		{
			fLocationText.setText( path.toOSString() );
		}
	}

	public void setInitialAssociationPath( IPath path )
	{
		if ( path != null )
		{
			fAssocitedCheckButton.setSelection( true );
			fAssociationText.setText( path.toOSString() );
		}
	}
	
	public Control getControl()
	{
		return fControl;
	}

	protected void createLocationControls( Composite parent )
	{
		Label label = new Label( parent, SWT.NONE );
		label.setText( "Select location directory:" );
		label.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ) );
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout( 2, false ) );
		GridData data = new GridData( GridData.FILL_BOTH );
		data.widthHint = Dialog.convertWidthInCharsToPixels( fFontMetrics, 70 );
		composite.setLayoutData( data );
		fLocationText = new Text( composite, SWT.SINGLE | SWT.BORDER );
		fLocationText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ) );
		Button button = createButton( composite, "&Browse..." );
		button.addSelectionListener( new SelectionAdapter() 
											{
												/* (non-Javadoc)
												 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(SelectionEvent)
												 */
												public void widgetSelected( SelectionEvent e )
												{
													selectLocation();
												}
											} );
	}

	protected void selectLocation()
	{
		DirectoryDialog dialog = new DirectoryDialog( fShell );
		dialog.setMessage( "Select Location Directory" );
		String result = dialog.open();
		if ( result != null )
		{
			fLocationText.setText( result );
		}
	}

	protected void createAssociationControls( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout() );
		GridData data = new GridData( GridData.FILL_BOTH );
		composite.setLayoutData( data );
		fAssocitedCheckButton = new Button( composite, SWT.CHECK );
		fAssocitedCheckButton.setText( "&Associate with" );
		fAssociationText = new Text( composite, SWT.SINGLE | SWT.BORDER );
		fAssociationText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		fAssocitedCheckButton.addSelectionListener( new SelectionAdapter()
														{
															/* (non-Javadoc)
															 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(SelectionEvent)
															 */
															public void widgetSelected(SelectionEvent e)
															{
																associationSelectionChanged();
															}

														} );
	}

	protected void associationSelectionChanged()
	{
		boolean checked = fAssocitedCheckButton.getSelection();
		fAssociationText.setEnabled( checked );
		if ( !checked )
			fAssociationText.setText( "" );
	}

	protected Button createButton( Composite parent, String label )
	{
		Button button = new Button( parent, SWT.PUSH );
		button.setText( label );
		GridData data = new GridData( GridData.END );
		data.heightHint = convertVerticalDLUsToPixels( IDialogConstants.BUTTON_HEIGHT );
		int widthHint = convertHorizontalDLUsToPixels( IDialogConstants.BUTTON_WIDTH );
		data.widthHint = Math.max( widthHint, button.computeSize( SWT.DEFAULT, SWT.DEFAULT, true ).x );
		button.setLayoutData( data );
		button.setFont( parent.getFont() );

		return button;
	}

	protected int convertVerticalDLUsToPixels( int dlus )
	{
		if ( fFontMetrics == null )
			return 0;
		return Dialog.convertVerticalDLUsToPixels( fFontMetrics, dlus );
	}

	protected int convertHorizontalDLUsToPixels( int dlus )
	{
		if ( fFontMetrics == null )
			return 0;
		return Dialog.convertHorizontalDLUsToPixels( fFontMetrics, dlus );
	}

	protected void initializeDialogUnits( Control control )
	{
		// Compute and store a font metric
		GC gc = new GC( control );
		gc.setFont( control.getFont() );
		fFontMetrics = gc.getFontMetrics();
		gc.dispose();
	}
	
	public String getLocationPath()
	{
		return fLocationText.getText().trim();
	}
	
	public String getAssociationPath()
	{
		if ( fAssocitedCheckButton.getSelection() )
		{
			return fAssociationText.getText().trim();
		}
		return "";
	}
}
