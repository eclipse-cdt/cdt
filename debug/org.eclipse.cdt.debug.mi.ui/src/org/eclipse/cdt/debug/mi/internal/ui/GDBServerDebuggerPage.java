/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui;

import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.ComboDialogField;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * The dynamic debugger tab for remote launches using gdb server.
 */
public class GDBServerDebuggerPage extends GDBDebuggerPage {

	private final static String CONNECTION_TCP = MIUIMessages.getString( "GDBServerDebuggerPage.0" ); //$NON-NLS-1$

	private final static String CONNECTION_SERIAL = MIUIMessages.getString( "GDBServerDebuggerPage.1" ); //$NON-NLS-1$

	private ComboDialogField fConnectionField;

	private String[] fConnections = new String[]{ CONNECTION_TCP, CONNECTION_SERIAL };

	private TCPSettingsBlock fTCPBlock;

	private SerialPortSettingsBlock fSerialBlock;

	private Composite fConnectionStack;

	private boolean fIsInitializing = false;

	public GDBServerDebuggerPage() {
		super();
		fConnectionField = createConnectionField();
		fTCPBlock = new TCPSettingsBlock();
		fSerialBlock = new SerialPortSettingsBlock();
		fTCPBlock.addObserver( this );
		fSerialBlock.addObserver( this );
	}

	protected void createConnectionTab( TabFolder tabFolder ) {
		TabItem tabItem = new TabItem( tabFolder, SWT.NONE );
		tabItem.setText( MIUIMessages.getString( "GDBServerDebuggerPage.10" ) ); //$NON-NLS-1$
		Composite comp1 = ControlFactory.createCompositeEx( tabFolder, 1, GridData.FILL_BOTH );
		((GridLayout)comp1.getLayout()).makeColumnsEqualWidth = false;
		comp1.setFont( tabFolder.getFont() );
		tabItem.setControl( comp1 );
		Composite comp = ControlFactory.createCompositeEx( comp1, 2, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		comp.setFont( comp1.getFont() );
		fConnectionField.doFillIntoGrid( comp, 2 );
		((GridData)fConnectionField.getComboControl( null ).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		PixelConverter converter = new PixelConverter( comp );
		LayoutUtil.setWidthHint( fConnectionField.getComboControl( null ), converter.convertWidthInCharsToPixels( 15 ) );
		fConnectionStack = ControlFactory.createCompositeEx( comp, 1, GridData.FILL_BOTH );
		StackLayout stackLayout = new StackLayout();
		fConnectionStack.setLayout( stackLayout );
		((GridData)fConnectionStack.getLayoutData()).horizontalSpan = 2;
		fTCPBlock.createBlock( fConnectionStack );
		fSerialBlock.createBlock( fConnectionStack );		
	}

	private ComboDialogField createConnectionField() {
		ComboDialogField field = new ComboDialogField( SWT.DROP_DOWN | SWT.READ_ONLY );
		field.setLabelText( MIUIMessages.getString( "GDBServerDebuggerPage.9" ) ); //$NON-NLS-1$
		field.setItems( fConnections );
		field.setDialogFieldListener( new IDialogFieldListener() {

			public void dialogFieldChanged( DialogField f ) {
				if ( !isInitializing() )
					connectionTypeChanged();
			}
		} );
		return field;
	}

	protected void connectionTypeChanged() {
		connectionTypeChanged0();
		updateLaunchConfigurationDialog();
	}

	private void connectionTypeChanged0() {
		((StackLayout)fConnectionStack.getLayout()).topControl = null;
		int index = fConnectionField.getSelectionIndex();
		if ( index >= 0 && index < fConnections.length ) {
			String[] connTypes = fConnectionField.getItems();
			if ( CONNECTION_TCP.equals( connTypes[index] ) )
				((StackLayout)fConnectionStack.getLayout()).topControl = fTCPBlock.getControl();
			else if ( CONNECTION_SERIAL.equals( connTypes[index] ) )
				((StackLayout)fConnectionStack.getLayout()).topControl = fSerialBlock.getControl();
		}
		fConnectionStack.layout();
	}

	public boolean isValid( ILaunchConfiguration launchConfig ) {
		if ( super.isValid( launchConfig ) ) {
			setErrorMessage( null );
			setMessage( null );
			int index = fConnectionField.getSelectionIndex();
			if ( index >= 0 && index < fConnections.length ) {
				String[] connTypes = fConnectionField.getItems();
				if ( CONNECTION_TCP.equals( connTypes[index] ) ) {
					if ( !fTCPBlock.isValid( launchConfig ) ) {
						setErrorMessage( fTCPBlock.getErrorMessage() );
						return false;
					}
				}
				else if ( CONNECTION_SERIAL.equals( connTypes[index] ) ) {
					if ( !fSerialBlock.isValid( launchConfig ) ) {
						setErrorMessage( fSerialBlock.getErrorMessage() );
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public void initializeFrom( ILaunchConfiguration configuration ) {
		setInitializing( true );
		super.initializeFrom( configuration );
		boolean isTcp = false;
		try {
			isTcp = configuration.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false );
		}
		catch( CoreException e ) {
		}
		fTCPBlock.initializeFrom( configuration );
		fSerialBlock.initializeFrom( configuration );
		fConnectionField.selectItem( (isTcp) ? 0 : 1 );
		connectionTypeChanged0();
		setInitializing( false );
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		super.performApply( configuration );
		if ( fConnectionField != null )
			configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, fConnectionField.getSelectionIndex() == 0 );
		fTCPBlock.performApply( configuration );
		fSerialBlock.performApply( configuration );
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		super.setDefaults( configuration );
		configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false );
		fTCPBlock.setDefaults( configuration );
		fSerialBlock.setDefaults( configuration );
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	private void setInitializing( boolean isInitializing ) {
		fIsInitializing = isInitializing;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.GDBDebuggerPage#createTabs(org.eclipse.swt.widgets.TabFolder)
	 */
	public void createTabs( TabFolder tabFolder ) {
		super.createTabs( tabFolder );
		createConnectionTab( tabFolder );
	}
}
