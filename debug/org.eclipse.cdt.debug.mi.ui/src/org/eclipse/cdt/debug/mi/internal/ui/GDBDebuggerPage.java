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
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.ui.IMILaunchConfigurationComponent;
import org.eclipse.cdt.debug.mi.ui.MIUIUtils;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for gdb-based debugger implementations.
 */
public class GDBDebuggerPage extends AbstractLaunchConfigurationTab implements Observer {

	protected TabFolder fTabFolder;

	protected Text fGDBCommandText;

	protected Text fGDBInitText;

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
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, "" ); //$NON-NLS-1$
		if ( fSolibBlock != null )
			fSolibBlock.setDefaults( configuration );
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
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
		String gdbInit = ""; //$NON-NLS-1$
		try {
			gdbCommand = configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb" ); //$NON-NLS-1$
			gdbInit = configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, "" ); //$NON-NLS-1$
		}
		catch( CoreException e ) {
		}
		if ( fSolibBlock != null )
			fSolibBlock.initializeFrom( configuration );
		fGDBCommandText.setText( gdbCommand );
		fGDBInitText.setText( gdbInit );
		setInitializing( false ); 
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		String gdbStr = fGDBCommandText.getText();
		gdbStr.trim();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbStr );
		gdbStr = fGDBInitText.getText();
		gdbStr.trim();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, gdbStr );
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
		IMILaunchConfigurationComponent block = MIUIUtils.createGDBSolibBlock( MIUIUtils.createSolibSearchPathBlock( null ), true, true ); 
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
		Composite comp = ControlFactory.createCompositeEx( fTabFolder, 1, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		tabItem.setControl( comp );
		Composite subComp = ControlFactory.createCompositeEx( comp, 3, GridData.FILL_HORIZONTAL );
		((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false;
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
		label = ControlFactory.createLabel( comp, MIUIMessages.getString( "GDBDebuggerPage.9" ), //$NON-NLS-1$
				200, SWT.DEFAULT, SWT.WRAP );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 1;
		gd.widthHint = 200;
		label.setLayoutData( gd );
	}

	public void createSolibTab( TabFolder tabFolder ) {
		TabItem tabItem = new TabItem( tabFolder, SWT.NONE );
		tabItem.setText( MIUIMessages.getString( "GDBDebuggerPage.10" ) ); //$NON-NLS-1$
		Composite comp = ControlFactory.createCompositeEx( fTabFolder, 1, GridData.FILL_BOTH );
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
}