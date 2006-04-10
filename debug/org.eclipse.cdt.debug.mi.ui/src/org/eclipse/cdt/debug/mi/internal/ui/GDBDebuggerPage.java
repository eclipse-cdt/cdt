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

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.ui.IMILaunchConfigurationComponent;
import org.eclipse.cdt.debug.mi.ui.MIUIUtils;
import org.eclipse.cdt.debug.ui.AbstractCDebuggerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for gdb-based debugger implementations.
 */
public class GDBDebuggerPage extends AbstractCDebuggerPage implements Observer {

	final private static String DEFAULT_MI_PROTOCOL = MIUIMessages.getString( "GDBDebuggerPage.12" );  //$NON-NLS-1$
	final protected String[] protocolItems = new String[] { DEFAULT_MI_PROTOCOL, "mi1", "mi2", "mi3" };  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

	protected TabFolder fTabFolder;

	protected Text fGDBCommandText;

	protected Text fGDBInitText;

	protected Combo fProtocolCombo;

	private IMILaunchConfigurationComponent fSolibBlock;
	
	private boolean fIsInitializing = false;

	public void createControl( Composite parent ) {
		Composite comp = new Composite( parent, SWT.NONE );
		comp.setLayout( new GridLayout() );
		comp.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		fTabFolder = new TabFolder( comp, SWT.NONE );
		fTabFolder.setLayoutData( new GridData( GridData.FILL_BOTH | GridData.GRAB_VERTICAL ) );
		createTabs( fTabFolder );
		fTabFolder.setSelection( 0 );
		setControl( parent );
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb" ); //$NON-NLS-1$
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, "mi" ); //$NON-NLS-1$
		if ( fSolibBlock != null )
			fSolibBlock.setDefaults( configuration );
	}

	public boolean isValid( ILaunchConfiguration launchConfig ) {
		boolean valid = fGDBCommandText.getText().length() != 0;
		if ( valid ) {
			setErrorMessage( null );
			setMessage( null );
		}
		else {
			setErrorMessage( MIUIMessages.getString( "GDBDebuggerPage.0" ) ); //$NON-NLS-1$
			setMessage( null );
		}
		return valid;
	}

	public void initializeFrom( ILaunchConfiguration configuration ) {
		setInitializing( true );
		String gdbCommand = "gdb"; //$NON-NLS-1$
		String gdbInit = IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT;
		try {
			gdbCommand = configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb" ); //$NON-NLS-1$
		}
		catch( CoreException e ) {
		}
		try {
			gdbInit = configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT );
		}
		catch( CoreException e ) {
		}
		String miVersion = MIPlugin.getMIVersion( configuration );
		if ( miVersion.compareTo( "mi" ) == 0 ) { //$NON-NLS-1$
			miVersion = DEFAULT_MI_PROTOCOL;
		}
		if ( fSolibBlock != null )
			fSolibBlock.initializeFrom( configuration );
		fGDBCommandText.setText( gdbCommand );
		fGDBInitText.setText( gdbInit );
		int index = 0;
		if ( miVersion.length() > 0 ) {
			for( int i = 0; i < protocolItems.length; ++i ) {
				if ( protocolItems[i].equals( miVersion ) ) {
					index = i;
					break;
				}
			}
		}
		fProtocolCombo.select( index );
		setInitializing( false ); 
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		String str = fGDBCommandText.getText();
		str.trim();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, str );
		str = fGDBInitText.getText();
		str.trim();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, str );
		str = fProtocolCombo.getText();
		if ( str.compareTo( DEFAULT_MI_PROTOCOL ) == 0 ) {
			str = "mi"; //$NON-NLS-1$
		}
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, str );
		if ( fSolibBlock != null )
			fSolibBlock.performApply( configuration );
	}

	public String getName() {
		return MIUIMessages.getString( "GDBDebuggerPage.1" ); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getShell()
	 */
	protected Shell getShell() {
		return super.getShell();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg ) {
		if ( !isInitializing() )
			updateLaunchConfigurationDialog();
	}

	public IMILaunchConfigurationComponent createSolibBlock( Composite parent ) {
		IMILaunchConfigurationComponent block = MIUIUtils.createGDBSolibBlock( true, true ); 
		block.createControl( parent );
		return block;
	}

	public void createTabs( TabFolder tabFolder ) {
		createMainTab( tabFolder );
		createSolibTab( tabFolder );
	}

	public void createMainTab( TabFolder tabFolder ) {
		TabItem tabItem = new TabItem( tabFolder, SWT.NONE );
		tabItem.setText( MIUIMessages.getString( "GDBDebuggerPage.2" ) ); //$NON-NLS-1$
		Composite comp = ControlFactory.createCompositeEx( tabFolder, 1, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		comp.setFont( tabFolder.getFont() );
		tabItem.setControl( comp );
		Composite subComp = ControlFactory.createCompositeEx( comp, 3, GridData.FILL_HORIZONTAL );
		((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false;
		subComp.setFont( tabFolder.getFont() );
		Label label = ControlFactory.createLabel( subComp, MIUIMessages.getString( "GDBDebuggerPage.3" ) ); //$NON-NLS-1$
		GridData gd = new GridData();
		//		gd.horizontalSpan = 2;
		label.setLayoutData( gd );
		fGDBCommandText = ControlFactory.createTextField( subComp, SWT.SINGLE | SWT.BORDER );
		fGDBCommandText.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent evt ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
		} );
		Button button = createPushButton( subComp, MIUIMessages.getString( "GDBDebuggerPage.4" ), null ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent evt ) {
				handleGDBButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleGDBButtonSelected() {
				FileDialog dialog = new FileDialog( getShell(), SWT.NONE );
				dialog.setText( MIUIMessages.getString( "GDBDebuggerPage.5" ) ); //$NON-NLS-1$
				String gdbCommand = fGDBCommandText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf( File.separator );
				if ( lastSeparatorIndex != -1 ) {
					dialog.setFilterPath( gdbCommand.substring( 0, lastSeparatorIndex ) );
				}
				String res = dialog.open();
				if ( res == null ) {
					return;
				}
				fGDBCommandText.setText( res );
			}
		} );
		label = ControlFactory.createLabel( subComp, MIUIMessages.getString( "GDBDebuggerPage.6" ) ); //$NON-NLS-1$
		gd = new GridData();
		//		gd.horizontalSpan = 2;
		label.setLayoutData( gd );
		fGDBInitText = ControlFactory.createTextField( subComp, SWT.SINGLE | SWT.BORDER );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		fGDBInitText.setLayoutData( gd );
		fGDBInitText.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent evt ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
		} );
		button = createPushButton( subComp, MIUIMessages.getString( "GDBDebuggerPage.7" ), null ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent evt ) {
				handleGDBInitButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleGDBInitButtonSelected() {
				FileDialog dialog = new FileDialog( getShell(), SWT.NONE );
				dialog.setText( MIUIMessages.getString( "GDBDebuggerPage.8" ) ); //$NON-NLS-1$
				String gdbCommand = fGDBInitText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf( File.separator );
				if ( lastSeparatorIndex != -1 ) {
					dialog.setFilterPath( gdbCommand.substring( 0, lastSeparatorIndex ) );
				}
				String res = dialog.open();
				if ( res == null ) {
					return;
				}
				fGDBInitText.setText( res );
			}
		} );
		label = ControlFactory.createLabel( subComp, MIUIMessages.getString( "GDBDebuggerPage.9" ), //$NON-NLS-1$
				200, SWT.DEFAULT, SWT.WRAP );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		gd.widthHint = 200;
		label.setLayoutData( gd );
		createProtocolCombo( subComp );
	}

	public void createSolibTab( TabFolder tabFolder ) {
		TabItem tabItem = new TabItem( tabFolder, SWT.NONE );
		tabItem.setText( MIUIMessages.getString( "GDBDebuggerPage.10" ) ); //$NON-NLS-1$
		Composite comp = ControlFactory.createCompositeEx( fTabFolder, 1, GridData.FILL_BOTH );
		comp.setFont( tabFolder.getFont() );
		tabItem.setControl( comp );
		fSolibBlock = createSolibBlock( comp );
		if ( fSolibBlock instanceof Observable )
			((Observable)fSolibBlock).addObserver( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		if ( fSolibBlock != null ) {
			if ( fSolibBlock instanceof Observable )
				((Observable)fSolibBlock).deleteObserver( this );
			fSolibBlock.dispose();
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated( ILaunchConfigurationWorkingCopy workingCopy ) {
		// Override the default behavior
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	private void setInitializing( boolean isInitializing ) {
		fIsInitializing = isInitializing;
	}

	protected void createProtocolCombo( Composite parent ) {
		Label label = new Label( parent, SWT.NONE );
		label.setText( MIUIMessages.getString( "GDBDebuggerPage.11" ) ); //$NON-NLS-1$
		fProtocolCombo = new Combo( parent, SWT.READ_ONLY | SWT.DROP_DOWN );
		fProtocolCombo.setItems( protocolItems );
		fProtocolCombo.addSelectionListener( new SelectionListener() {

			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
			
			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
		} );
	}
}
