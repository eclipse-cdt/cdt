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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryDescriptor;
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
public class StandardGDBDebuggerPage extends AbstractCDebuggerPage implements Observer {

	private final static String DEFAULT_MI_VERSION = "mi"; //$NON-NLS-1$

	protected TabFolder fTabFolder;

	protected Text fGDBCommandText;

	protected Text fGDBInitText;

	protected Combo fCommandFactoryCombo;

	protected Combo fProtocolCombo;

	private IMILaunchConfigurationComponent fSolibBlock;

	private CommandFactoryDescriptor[] fCommandFactoryDescriptors;

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
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, MIPlugin.getDefault().getCommandFactoryManager().getDefaultDescriptor( getDebuggerIdentifier() ).getIdentifier() );
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
			setErrorMessage( MIUIMessages.getString( "StandardGDBDebuggerPage.0" ) ); //$NON-NLS-1$
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
		if ( fSolibBlock != null )
			fSolibBlock.initializeFrom( configuration );
		fGDBCommandText.setText( gdbCommand );
		fGDBInitText.setText( gdbInit );
		
		String debuggerID = getDebuggerIdentifier();
		fCommandFactoryDescriptors = MIPlugin.getDefault().getCommandFactoryManager().getDescriptors( debuggerID );
		Arrays.sort( fCommandFactoryDescriptors, 
				new Comparator() { 
					public int compare( Object arg0, Object arg1 ) {
						return ((CommandFactoryDescriptor)arg0).getName().compareTo( ((CommandFactoryDescriptor)arg1).getName() );
					}
				} );
		String[] descLabels = new String[fCommandFactoryDescriptors.length];
		String commandFactoryId = MIPlugin.getCommandFactory( configuration );
		int index = -1;
		for( int i = 0; i < fCommandFactoryDescriptors.length; ++i ) {
			descLabels[i] = fCommandFactoryDescriptors[i].getName();
			if ( fCommandFactoryDescriptors[i].getIdentifier().equals( commandFactoryId ) )
				index = i;
		}
		fCommandFactoryCombo.setItems( descLabels );
		if ( index >= 0 ) {
			fCommandFactoryCombo.select( index );
			String[] miVersions = fCommandFactoryDescriptors[index].getMIVersions();
			fProtocolCombo.setItems( miVersions );
			if ( miVersions.length == 0 ) {
				miVersions = new String[] { DEFAULT_MI_VERSION };
			}
			String mi = DEFAULT_MI_VERSION;
			try {
				mi = configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, DEFAULT_MI_VERSION );
			}
			catch( CoreException e ) {
				// use default
			}
			int miIndex = 0;
			for ( int i = 0; i < miVersions.length; ++i ) {
				if ( miVersions[i].equals( mi ) ) {
					miIndex = i;
					break;
				}
			}
			fProtocolCombo.select( miIndex );
		}

		setInitializing( false ); 
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		String str = fGDBCommandText.getText();
		str.trim();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, str );
		str = fGDBInitText.getText();
		str.trim();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, str );
		str = fCommandFactoryCombo.getText();
		int index = fCommandFactoryCombo.indexOf( str );
		str = fCommandFactoryDescriptors[index].getIdentifier();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, str );
		str = fProtocolCombo.getText();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, str );
		if ( fSolibBlock != null )
			fSolibBlock.performApply( configuration );
	}

	public String getName() {
		return MIUIMessages.getString( "StandardGDBDebuggerPage.1" ); //$NON-NLS-1$
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
		tabItem.setText( MIUIMessages.getString( "StandardGDBDebuggerPage.2" ) ); //$NON-NLS-1$
		Composite comp = ControlFactory.createCompositeEx( tabFolder, 1, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		comp.setFont( tabFolder.getFont() );
		tabItem.setControl( comp );
		Composite subComp = ControlFactory.createCompositeEx( comp, 3, GridData.FILL_HORIZONTAL );
		((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false;
		subComp.setFont( tabFolder.getFont() );
		Label label = ControlFactory.createLabel( subComp, MIUIMessages.getString( "StandardGDBDebuggerPage.3" ) ); //$NON-NLS-1$
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
		Button button = createPushButton( subComp, MIUIMessages.getString( "StandardGDBDebuggerPage.4" ), null ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent evt ) {
				handleGDBButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleGDBButtonSelected() {
				FileDialog dialog = new FileDialog( getShell(), SWT.NONE );
				dialog.setText( MIUIMessages.getString( "StandardGDBDebuggerPage.5" ) ); //$NON-NLS-1$
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
		label = ControlFactory.createLabel( subComp, MIUIMessages.getString( "StandardGDBDebuggerPage.6" ) ); //$NON-NLS-1$
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
		button = createPushButton( subComp, MIUIMessages.getString( "StandardGDBDebuggerPage.7" ), null ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent evt ) {
				handleGDBInitButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleGDBInitButtonSelected() {
				FileDialog dialog = new FileDialog( getShell(), SWT.NONE );
				dialog.setText( MIUIMessages.getString( "StandardGDBDebuggerPage.8" ) ); //$NON-NLS-1$
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
		label = ControlFactory.createLabel( subComp, MIUIMessages.getString( "StandardGDBDebuggerPage.9" ), //$NON-NLS-1$
				200, SWT.DEFAULT, SWT.WRAP );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		gd.widthHint = 200;
		label.setLayoutData( gd );
		
		Composite options = ControlFactory.createCompositeEx( subComp, 2, GridData.FILL_HORIZONTAL );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		options.setLayoutData( gd );
		createCommandFactoryCombo( options );
		createProtocolCombo( options );
	}

	public void createSolibTab( TabFolder tabFolder ) {
		TabItem tabItem = new TabItem( tabFolder, SWT.NONE );
		tabItem.setText( MIUIMessages.getString( "StandardGDBDebuggerPage.10" ) ); //$NON-NLS-1$
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

	protected void createCommandFactoryCombo( Composite parent ) {
		Label label = new Label( parent, SWT.NONE );
		label.setText( MIUIMessages.getString( "StandardGDBDebuggerPage.12" ) ); //$NON-NLS-1$
		fCommandFactoryCombo = new Combo( parent, SWT.READ_ONLY | SWT.DROP_DOWN );
		fCommandFactoryCombo.addSelectionListener( new SelectionListener() {

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

	protected void createProtocolCombo( Composite parent ) {
		Label label = new Label( parent, SWT.NONE );
		label.setText( MIUIMessages.getString( "StandardGDBDebuggerPage.11" ) ); //$NON-NLS-1$
		fProtocolCombo = new Combo( parent, SWT.READ_ONLY | SWT.DROP_DOWN );
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

	protected String getCurrentCommandFactoryID() {
		String name = fCommandFactoryCombo.getText();
		for ( int i = 0; i < fCommandFactoryDescriptors.length; ++i ) {
			if ( fCommandFactoryDescriptors[i].getName().equals( name ) ) {
				return fCommandFactoryDescriptors[i].getIdentifier();
			}
		}
		return ""; //$NON-NLS-1$
	}
}
