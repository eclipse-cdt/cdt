/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.internal.ui;

import java.io.File;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Enter type comment.
 * 
 * @since Nov 20, 2003
 */
public class GDBServerDebuggerPage extends GDBDebuggerPage
{
	private final static String CONNECTION_TCP = MIUIMessages.getString( "GDBServerDebuggerPage.0" ); //$NON-NLS-1$
	private final static String CONNECTION_SERIAL = MIUIMessages.getString( "GDBServerDebuggerPage.1" ); //$NON-NLS-1$

	private ComboDialogField fConnectionField;

	private String[] fConnections = new String[] { CONNECTION_TCP, CONNECTION_SERIAL };
	private TCPSettingsBlock fTCPBlock;
	private SerialPortSettingsBlock fSerialBlock;
	private Composite fConnectionStack;

	public GDBServerDebuggerPage()
	{
		super();
		fConnectionField = createConnectionField();
		fTCPBlock = new TCPSettingsBlock();
		fSerialBlock = new SerialPortSettingsBlock();
		fTCPBlock.addObserver( this );
		fSerialBlock.addObserver( this );
	}

	public void createMainTab( TabFolder tabFolder )
	{
		TabItem tabItem = new TabItem( tabFolder, SWT.NONE );
		tabItem.setText( MIUIMessages.getString( "GDBServerDebuggerPage.2" ) ); //$NON-NLS-1$

		Composite comp = ControlFactory.createCompositeEx( fTabFolder, 1, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		tabItem.setControl( comp );			

		Composite subComp = ControlFactory.createCompositeEx( comp, 3, GridData.FILL_HORIZONTAL );
		((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false;

		Label label = ControlFactory.createLabel( subComp, MIUIMessages.getString( "GDBServerDebuggerPage.3" ) ); //$NON-NLS-1$
		GridData gd = new GridData();
//		gd.horizontalSpan = 2;
		label.setLayoutData( gd );

		fGDBCommandText = ControlFactory.createTextField( subComp, SWT.SINGLE | SWT.BORDER );
		fGDBCommandText.addModifyListener( 
						new ModifyListener() 
							{
								public void modifyText( ModifyEvent evt ) 
								{
									updateLaunchConfigurationDialog();
								}
							} );

		Button button = createPushButton( subComp, MIUIMessages.getString( "GDBServerDebuggerPage.4" ), null ); //$NON-NLS-1$
		button.addSelectionListener( 
						new SelectionAdapter() 
							{
								public void widgetSelected( SelectionEvent evt ) 
								{
									handleGDBButtonSelected();
									updateLaunchConfigurationDialog();
								}
								
								private void handleGDBButtonSelected() 
								{
									FileDialog dialog = new FileDialog( getShell(), SWT.NONE );
									dialog.setText( MIUIMessages.getString( "GDBServerDebuggerPage.5" ) ); //$NON-NLS-1$
									String gdbCommand = fGDBCommandText.getText().trim();
									int lastSeparatorIndex = gdbCommand.lastIndexOf( File.separator );
									if ( lastSeparatorIndex != -1 ) 
									{
										dialog.setFilterPath( gdbCommand.substring( 0, lastSeparatorIndex ) );
									}
									String res = dialog.open();
									if ( res == null ) 
									{
										return;
									}
									fGDBCommandText.setText( res );
								}
							} );

		label = ControlFactory.createLabel( subComp, MIUIMessages.getString( "GDBServerDebuggerPage.6" ) ); //$NON-NLS-1$
		gd = new GridData();
//		gd.horizontalSpan = 2;
		label.setLayoutData( gd );

		fGDBInitText = ControlFactory.createTextField( subComp, SWT.SINGLE | SWT.BORDER );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		fGDBInitText.setLayoutData( gd );
		fGDBInitText.addModifyListener( new ModifyListener() 
											{
												public void modifyText( ModifyEvent evt ) 
												{
													updateLaunchConfigurationDialog();
												}
											} );
		button = createPushButton( subComp, MIUIMessages.getString( "GDBServerDebuggerPage.7" ), null ); //$NON-NLS-1$
		button.addSelectionListener(
						new SelectionAdapter() 
						{
							public void widgetSelected( SelectionEvent evt ) 
							{
								handleGDBInitButtonSelected();
								updateLaunchConfigurationDialog();
							}
							
							private void handleGDBInitButtonSelected() 
							{
								FileDialog dialog = new FileDialog( getShell(), SWT.NONE );
								dialog.setText( MIUIMessages.getString( "GDBServerDebuggerPage.8" ) ); //$NON-NLS-1$
								String gdbCommand = fGDBInitText.getText().trim();
								int lastSeparatorIndex = gdbCommand.lastIndexOf( File.separator );
								if ( lastSeparatorIndex != -1 ) 
								{
									dialog.setFilterPath( gdbCommand.substring( 0, lastSeparatorIndex ) );
								}
								String res = dialog.open();
								if ( res == null ) 
								{
									return;
								}
								fGDBInitText.setText( res );
							}
						} );

		extendMainTab( comp );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.GDBDebuggerPage#extendMainTab(org.eclipse.swt.widgets.Composite)
	 */
	protected void extendMainTab( Composite parent )
	{
		Composite comp = ControlFactory.createCompositeEx( parent, 2, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;


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
		connectionTypeChanged();
	}

	private ComboDialogField createConnectionField()
	{
		ComboDialogField field = new ComboDialogField( SWT.DROP_DOWN | SWT.READ_ONLY );
		field.setLabelText( MIUIMessages.getString( "GDBServerDebuggerPage.9" ) ); //$NON-NLS-1$
		field.setItems( fConnections );
		field.setDialogFieldListener( 
						new IDialogFieldListener()
							{
								public void dialogFieldChanged( DialogField f )
								{
									connectionTypeChanged();
								}
							} );
		return field;
	}

	protected void connectionTypeChanged()
	{
		((StackLayout)fConnectionStack.getLayout()).topControl = null;
		int index = fConnectionField.getSelectionIndex();
		if ( index >= 0 && index < fConnections.length )
		{
			String[] connTypes = fConnectionField.getItems();
			if ( CONNECTION_TCP.equals( connTypes[index] ) )
				((StackLayout)fConnectionStack.getLayout()).topControl = fTCPBlock.getControl();
			else if ( CONNECTION_SERIAL.equals( connTypes[index] ) )
				((StackLayout)fConnectionStack.getLayout()).topControl = fSerialBlock.getControl();
		}
		fConnectionStack.layout();
		updateLaunchConfigurationDialog();
	}

	public boolean isValid( ILaunchConfiguration launchConfig )
	{
		if ( super.isValid( launchConfig ) )
		{
			setErrorMessage( null );
			setMessage( null );

			int index = fConnectionField.getSelectionIndex();
			if ( index >= 0 && index < fConnections.length )
			{
				String[] connTypes = fConnectionField.getItems();
				if ( CONNECTION_TCP.equals( connTypes[index] ) )
				{
					if ( !fTCPBlock.isValid( launchConfig ) )
					{
						setErrorMessage( fTCPBlock.getErrorMessage() );
						return false;
					}
				}
				else if ( CONNECTION_SERIAL.equals( connTypes[index] ) )
				{
					if ( !fSerialBlock.isValid( launchConfig ) )
					{
						setErrorMessage( fSerialBlock.getErrorMessage() );
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public void initializeFrom( ILaunchConfiguration configuration )
	{
		super.initializeFrom( configuration );

		boolean isTcp = false;
		try 
		{
			isTcp = configuration.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false );
		} 
		catch( CoreException e ) 
		{
		}

		fTCPBlock.initializeFrom( configuration );
		fSerialBlock.initializeFrom( configuration );

		fConnectionField.selectItem( ( isTcp ) ? 0 : 1 );
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration )
	{
		super.performApply( configuration );
		if ( fConnectionField != null )
			configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, fConnectionField.getSelectionIndex() == 0 );
		fTCPBlock.performApply( configuration );
		fSerialBlock.performApply( configuration );
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
	{
		super.setDefaults( configuration );
		configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false );
		fTCPBlock.setDefaults( configuration );
		fSerialBlock.setDefaults( configuration );
	}
}

