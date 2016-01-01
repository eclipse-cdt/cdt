/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Vladimir Prus (vladimir@codesourcery.com) - bug 156114: GDB options layout 
 *     problem
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.cdt.utils.Platform;
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
import org.eclipse.swt.widgets.Control;
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

	protected Button fVerboseModeButton;
	protected Button fBreakpointsFullPath;

	private IMILaunchConfigurationComponent fSolibBlock;

	private CommandFactoryDescriptor[] fCommandFactoryDescriptors;

	private boolean fIsInitializing = false;
	
	private static boolean gdb64ExistsIsCached = false;
	
	private static boolean cachedGdb64Exists;

	@Override
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

	@Override
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, defaultGdbCommand(configuration));
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, MIPlugin.getDefault().getCommandFactoryManager().getDefaultDescriptor( getDebuggerIdentifier() ).getIdentifier() );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT );
		if ( fSolibBlock != null )
			fSolibBlock.setDefaults( configuration );
	}
	
	protected String defaultGdbCommand(ILaunchConfiguration configuration) {
		String gdbCommand = null;

		if (Platform.getOS().equals(Platform.OS_LINUX) &&
				Platform.getOSArch().equals("ppc64")) { //$NON-NLS-1$
			// On SLES 9 and 10 for ppc64 arch, there is a separate
			// 64-bit capable gdb called gdb64.  It can
			// also debug 32-bit executables, so let's see if it exists.
			if (!gdb64ExistsIsCached) {
				Process unameProcess;
				int interruptedRetryCount = 5;

				String cmd[] = {"gdb64", "--version"}; //$NON-NLS-1$ //$NON-NLS-2$

				gdb64ExistsIsCached = true;

				while (interruptedRetryCount >= 0) {
					try {
						unameProcess = Runtime.getRuntime().exec(cmd);
						int exitStatus = unameProcess.waitFor();

						cachedGdb64Exists = (exitStatus == 0);
						break;
					} catch (IOException e) {
						cachedGdb64Exists = false;
						break;
					} catch (InterruptedException e) {
						// Never should get here, really.  The chances of the command being interrupted
						// are very small
						cachedGdb64Exists = false;
						interruptedRetryCount--;
					}
				}
			}
			if (cachedGdb64Exists) {
				gdbCommand = "gdb64"; //$NON-NLS-1$
			} else {
				gdbCommand = IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT;
			}
		} else {
			gdbCommand = IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT;
		}
		return gdbCommand;
	}

	
	@Override
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

	@Override
	public void initializeFrom( ILaunchConfiguration configuration ) {
		setInitializing( true );
		String gdbCommand = defaultGdbCommand(configuration);
		String gdbInit = IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT;
		try {
			gdbCommand = configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, defaultGdbCommand(configuration));
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
					@Override
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
		if ( index < 0 ) {
			index = 0;
		}
		
		//It may be the case that we can't match up any identifier with any installed debuggers associated 
		//with this debuggerID (ie fCommandFactoryDescriptors.length == 0) for example when importing a 
		//launch from different environments that use CDT debugging.  In this case we try and soldier on
		//using the defaults as much as is realistic.
		String[] miVersions = new String[0];
		if(index < fCommandFactoryDescriptors.length) {
			fCommandFactoryCombo.select( index );
			miVersions = fCommandFactoryDescriptors[index].getMIVersions();
		}
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
		boolean verboseMode = IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT;
		try {
			verboseMode = configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, IMILaunchConfigurationConstants.DEBUGGER_VERBOSE_MODE_DEFAULT );
		}
		catch( CoreException e ) {
			// use default
		}
		fVerboseModeButton.setSelection( verboseMode );
		fBreakpointsFullPath.setSelection(getBreakpointsWithFullNameAttribute(configuration));
		// We've populated combos, which affects their preferred size, and so must relayout things.
		Control changed[] = { fCommandFactoryCombo, fProtocolCombo };
		((Composite) getControl()).layout( changed );

		setInitializing( false ); 
	}
	protected boolean getBreakpointsWithFullNameAttribute( ILaunchConfiguration config ) {
		boolean result = IMILaunchConfigurationConstants.DEBUGGER_FULLPATH_BREAKPOINTS_DEFAULT; 
		try {
			return config.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, result );
		}
		catch( CoreException e ) {
			// use default
		}
		return result;
	}
	@Override
	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		String str = fGDBCommandText.getText();
		str.trim();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, str );
		str = fGDBInitText.getText();
		str.trim();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, str );
		str = fCommandFactoryCombo.getText();
		int index = fCommandFactoryCombo.indexOf( str );
		str = ( index < 0 ) ? "" : fCommandFactoryDescriptors[index].getIdentifier(); //$NON-NLS-1$
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY, str );
		str = fProtocolCombo.getText();
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, str );
		if ( fSolibBlock != null )
			fSolibBlock.performApply( configuration );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_VERBOSE_MODE, fVerboseModeButton.getSelection() );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_FULLPATH_BREAKPOINTS, fBreakpointsFullPath.getSelection() );
	}

	@Override
	public String getName() {
		return MIUIMessages.getString( "StandardGDBDebuggerPage.1" ); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getShell()
	 */
	@Override
	protected Shell getShell() {
		return super.getShell();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
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

			@Override
			public void modifyText( ModifyEvent evt ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
		} );
		Button button = createPushButton( subComp, MIUIMessages.getString( "StandardGDBDebuggerPage.4" ), null ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			@Override
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

			@Override
			public void modifyText( ModifyEvent evt ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
		} );
		button = createPushButton( subComp, MIUIMessages.getString( "StandardGDBDebuggerPage.7" ), null ); //$NON-NLS-1$
		button.addSelectionListener( new SelectionAdapter() {

			@Override
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
		createVerboseModeButton( subComp );
		createBreakpointFullPathName(subComp);
		// fit options into 3-grid one per line
		GridData gd1 = new GridData();
		gd1.horizontalSpan = 3;
		fVerboseModeButton.setLayoutData(gd1);
		GridData gd2 = new GridData();
		gd2.horizontalSpan = 3;
		fBreakpointsFullPath.setLayoutData(gd2);
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
	@Override
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
	@Override
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

			@Override
			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
			
			@Override
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

			@Override
			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
			
			@Override
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

	protected void createVerboseModeButton( Composite parent ) {
		fVerboseModeButton = createCheckButton( parent, MIUIMessages.getString( "StandardGDBDebuggerPage.13" ) ); //$NON-NLS-1$
		fVerboseModeButton.addSelectionListener( new SelectionListener() {

			@Override
			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
			
			@Override
			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
		} );
	}
	protected void createBreakpointFullPathName( Composite parent ) {
		fBreakpointsFullPath = createCheckButton( parent, MIUIMessages.getString( "StandardGDBDebuggerPage.14" ) ); //$NON-NLS-1$

		fBreakpointsFullPath.addSelectionListener( new SelectionListener() {

			@Override
			public void widgetDefaultSelected( SelectionEvent e ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
			
			@Override
			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() )
					updateLaunchConfigurationDialog();
			}
		} );
	}
}
