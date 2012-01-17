/*******************************************************************************
 * Copyright (c) 2004, 2008, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceManagement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The "Add Watchpoint" dialog of the "Toggle watchpoint" action.
 */
public class AddWatchpointDialog extends Dialog implements ModifyListener, SelectionListener {

	private Combo fExpressionInput;
	private String fExpression;
	private static List<String> sExpressionHistory = new ArrayList<String>();

	private boolean fHasMemorySpaceControls;
	private Button fMemorySpaceEnableButton;
	private Combo fMemorySpaceInput;
	private String fMemorySpace;

	private boolean fRangeInitialEnable;
	private Button fRangeEnableButton;
	private Text fRangeField;
	private String fRange = "";  //$NON-NLS-1$
	
	private Button fChkBtnWrite;
	private Button fChkBtnRead;
	private boolean fRead;
	private boolean fWrite;
	
	private ICDIMemorySpaceManagement fMemManagement;
	

	/**
	 * Constructor for AddWatchpointDialog.
	 * 
	 * @param parentShell
	 */
	public AddWatchpointDialog( Shell parentShell, ICDIMemorySpaceManagement memMgmt ) {
		super( parentShell );
		setShellStyle( getShellStyle() | SWT.RESIZE );
		fMemManagement = memMgmt;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea( Composite parent ) {
		// The button bar will work better if we make the parent composite
		// a single column grid layout. For the widgets we add, we want a 
		// a two-column grid, so we just create a sub composite for that.
		GridLayout gridLayout = new GridLayout();
		parent.setLayout( gridLayout );
		GridData gridData = new GridData( GridData.FILL_BOTH );
		parent.setLayoutData( gridData );
		Composite composite = new Composite( parent, SWT.None );
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout( gridLayout );
		parent = composite;

		// Create the controls
		createExpressionControl( parent );
		boolean hasDebugContext = DebugUITools.getDebugContext() != null;
		boolean hasMemorySpaces = hasDebugContext && fMemManagement != null && fMemManagement.getMemorySpaces() != null && fMemManagement.getMemorySpaces().length > 0;
		fHasMemorySpaceControls = !hasDebugContext || hasMemorySpaces;
		if ( fHasMemorySpaceControls ) {
			createMemorySpaceControl( parent, hasMemorySpaces );
		}
		createCountField( parent );
		createAccessWidgets( parent );
		
		// Initialize the inter-control state
		if ( fExpression != null && fExpression.length() > 0 ) {
			fExpressionInput.add( fExpression, 0 );
			fExpressionInput.select( 0 );
		}
		fExpressionInput.setFocus();
		if ( fHasMemorySpaceControls ) {
			fMemorySpaceInput.setEnabled( fMemorySpaceEnableButton.getEnabled() );
		}
		fRangeField.setEnabled( fRangeEnableButton.getEnabled() );
		updateUI();
		return parent;
	}
	
	private void createExpressionControl(Composite parent ) {

		Label l = new Label( parent, GridData.FILL_HORIZONTAL );
		l.setText( ActionMessages.getString( "AddWatchpointDialog.1" ) ); //$NON-NLS-1$
		GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 2;
		l.setLayoutData( gridData );			

		fExpressionInput = new Combo( parent, SWT.BORDER );
		gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 2;
		fExpressionInput.setLayoutData( gridData );		
		fExpressionInput.addModifyListener( this );
		for (String expression : sExpressionHistory) {
			fExpressionInput.add( expression );			
		}
	}
	
	private void createMemorySpaceControl( Composite parent, boolean hasMemorySpaces ) {
		fMemorySpaceEnableButton = new Button( parent, SWT.CHECK );
		GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 1;
		fMemorySpaceEnableButton.setLayoutData( gridData );
		fMemorySpaceEnableButton.setText( ActionMessages.getString( "AddWatchpointDialog.5" ) ); //$NON-NLS-1$
		fMemorySpaceEnableButton.setSelection( false );
		fMemorySpaceEnableButton.addSelectionListener( this );

		if ( hasMemorySpaces ) {
			fMemorySpaceInput = new Combo( parent, SWT.BORDER | SWT.READ_ONLY );
		} else {
			fMemorySpaceInput = new Combo( parent, SWT.BORDER );
		}
		gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 1;
		fMemorySpaceInput.setLayoutData( gridData );
		fMemorySpaceInput.addSelectionListener( this );
		if ( fMemManagement != null ) {
			String [] memorySpaces = fMemManagement.getMemorySpaces();
			for ( int i = 0; i < memorySpaces.length; i++ ) {
				fMemorySpaceInput.add( memorySpaces[i] );
			}
		}
		if ( fMemorySpace != null && fMemorySpace.length() > 0 ) {
			int i = fMemorySpaceInput.indexOf( fMemorySpace );
			if ( i >= 0 ) {
				fMemorySpaceInput.select( i );
				fMemorySpaceEnableButton.setSelection( true );
			} else {
				fMemorySpaceInput.add( fMemorySpace );
			}
		}
		fMemorySpaceInput.addModifyListener( this );
		//234909 - for accessibility
		fMemorySpaceInput.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					@Override
					public void getName(AccessibleEvent e) {
						e.result = ActionMessages.getString( "AddWatchpointDialog.5" ); //$NON-NLS-1$
					}
					
				});
	}

	/**
	 * @param 	text
	 * @param 	c
	 * @return	true if the concatenation of text + c results
	 *			in a valid string representation of an integer
	 * @see		verifyIntegerText()
	 */
	private static boolean verifyIntegerTextAddition( String text, char c ) {
		
		// pass through all control characters
		if ( Character.isISOControl( c ) ) {
			return true;
		}
		
		// case-insensitive
		c = Character.toLowerCase( c );
		text = text.toLowerCase();
		
		// first character has to be 0-9
		if ( text.length() == 0 ) {
			return Character.isDigit( c );
		}
		
		// second character must be x if preceded by a 0, otherwise 0-9 will do
		if ( text.length() == 1 ) {
			if ( text.equals( "0" ) ) { //$NON-NLS-1$
				return c == 'x';
			}
			return Character.isDigit( c );
		}
		
		// all subsequent characters must be 0-9 or a-f if started with 0x
		return Character.isDigit( c )
				|| text.startsWith( "0x" ) && 'a' <= c && c <= 'f'; //$NON-NLS-1$
	}
	
	/**
	 * @param	text integer string built up using verifyIntegerTextAddition()
	 * @return	true if text represents a valid string representation of
	 * 			an integer
	 */
	private static boolean verifyIntegerText( String text ) {
		if ( text.length() == 0 ) {
			return false;
		}
		if ( text.length() == 1 ) {
			return true;
		}
		if ( text.length() == 2 ) {
			return !text.equals("0x"); //$NON-NLS-1$
		}
		return true;
	}

	private void createCountField( Composite parent ) {
		fRangeEnableButton = new Button( parent, SWT.CHECK );
		GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 1;
		fRangeEnableButton.setLayoutData( gridData );
		fRangeEnableButton.setText( ActionMessages.getString( "AddWatchpointDialog.6" ) ); //$NON-NLS-1$
		fRangeEnableButton.setSelection( fRangeInitialEnable && fRange.length() > 0 );
		fRangeEnableButton.addSelectionListener( this );

		fRangeField = new Text( parent, SWT.BORDER );
		gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 1;
		GC gc = new GC( fRangeField );
		FontMetrics fm = gc.getFontMetrics();
		gridData.minimumWidth = 8 * fm.getAverageCharWidth();
		fRangeField.setLayoutData( gridData );
		fRangeField.setText( fRange );
		fRangeField.addVerifyListener( new VerifyListener() {
			@Override
			public void verifyText( VerifyEvent e ) {
				e.doit = verifyIntegerTextAddition( fRangeField.getText(), e.character );
			}
		});
		fRangeField.addModifyListener( this );
		//234909 - for accessibility
		fRangeField.getAccessible().addAccessibleListener(
			new AccessibleAdapter() {
				@Override
				public void getName(AccessibleEvent e) {
					e.result = ActionMessages.getString( "AddWatchpointDialog.6" ); //$NON-NLS-1$
				}
				
			});
	}
	
	private void createAccessWidgets( Composite parent ) {
		GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 3;

		Group group = new Group( parent, SWT.NONE );
		group.setLayout( new GridLayout() );
		group.setLayoutData( gridData );
		group.setText( ActionMessages.getString( "AddWatchpointDialog.2" ) ); //$NON-NLS-1$
		fChkBtnWrite = new Button( group, SWT.CHECK );
		fChkBtnWrite.setText( ActionMessages.getString( "AddWatchpointDialog.3" ) ); //$NON-NLS-1$
		fChkBtnWrite.setSelection( true );
		fChkBtnWrite.addSelectionListener( this );
		fChkBtnRead = new Button( group, SWT.CHECK );
		fChkBtnRead.setText( ActionMessages.getString( "AddWatchpointDialog.4" ) ); //$NON-NLS-1$
		fChkBtnRead.setSelection( false );
		fChkBtnRead.addSelectionListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell( Shell newShell ) {
		super.configureShell( newShell );

		// use the same title used by the platform dialog
		newShell.setText( ActionMessages.getString( "AddWatchpointDialog.0" ) ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fExpression = fExpressionInput.getText().trim();
		if ( fExpression.length() > 0 ) {
			addHistory( fExpression );
		}
		if ( fHasMemorySpaceControls ) {
			fMemorySpace = fMemorySpaceEnableButton.getSelection() ? fMemorySpaceInput.getText().trim() : ""; //$NON-NLS-1$
		}
		fRange = fRangeEnableButton.getSelection() ? fRangeField.getText().trim() : "0"; //$NON-NLS-1$
		fRead = fChkBtnRead.getSelection();
		fWrite = fChkBtnWrite.getSelection();
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText( ModifyEvent e ) {
		updateUI();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar( Composite parent ) {
		return super.createButtonBar( parent );
	}

	public String getExpression() {
		return fExpression;
	}

	public String getMemorySpace() {
		return fMemorySpace;
	}

	private static void addHistory( String item )	{		
		if ( !sExpressionHistory.contains( item ) ) {
			sExpressionHistory.add( 0, item );

			if ( sExpressionHistory.size() > 5 )
				sExpressionHistory.remove( sExpressionHistory.size() - 1 );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected( SelectionEvent e ) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected( SelectionEvent e ) {
		updateUI();
	}
	
	private void updateUI() {
		if ( fHasMemorySpaceControls ) {
			fMemorySpaceInput.setEnabled( fMemorySpaceEnableButton.getSelection() );
		}
		fRangeField.setEnabled( fRangeEnableButton.getSelection() );
		Button b = getButton( IDialogConstants.OK_ID );
		if ( b == null ) {
			return;
		}
		b.setEnabled( okayEnabled() );
	}
	
	private boolean okayEnabled() {
		if ( !fChkBtnRead.getSelection() && !fChkBtnWrite.getSelection() ) {
			return false ;
		}
		if ( fExpressionInput.getText().length() == 0 ) {
			return false;
		}
		if ( fHasMemorySpaceControls && fMemorySpaceInput.getEnabled() && fMemorySpaceInput.getText().length() == 0 ) {
			return false;
		}
		if ( fRangeField.getEnabled()
				&& ( fRangeField.getText().length() == 0 || !verifyIntegerText( fRangeField.getText() ) ) ) {
			return false;
		}
		return true;
	}

	public boolean getWriteAccess() {
		return fWrite;
	}
	
	public boolean getReadAccess() {
		return fRead;
	}

	public void setExpression(String expressionString ) {
		fExpression = expressionString;
	}

	public BigInteger getRange() {
		return BigInteger.valueOf( Long.decode(fRange).longValue() );
	}
	
	public void initializeRange( boolean enable, String range ) {
		fRangeInitialEnable = enable;
		fRange = range; 
	}
	
	public void initializeMemorySpace( String memorySpace ) {
		fMemorySpace = memorySpace;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// override so we can change the initial okay enabled state
		createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true ).setEnabled( okayEnabled() );
		createButton( parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false );
	}

}
