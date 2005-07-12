/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

public class CDebuggerTab extends AbstractCDebuggerTab {

	public class AdvancedDebuggerOptionsDialog extends Dialog {

		private Button fVarBookKeeping;

		private Button fRegBookKeeping;

		/**
		 * Constructor for AdvancedDebuggerOptionsDialog.
		 */
		protected AdvancedDebuggerOptionsDialog( Shell parentShell ) {
			super( parentShell );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea( Composite parent ) {
			Composite composite = (Composite)super.createDialogArea( parent );
			Group group = new Group( composite, SWT.NONE );
			group.setText( LaunchMessages.getString( "CDebuggerTab.Automatically_track_values_of" ) ); //$NON-NLS-1$
			GridLayout layout = new GridLayout();
			group.setLayout( layout );
			group.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
			fVarBookKeeping = new Button( group, SWT.CHECK );
			fVarBookKeeping.setText( LaunchMessages.getString( "CDebuggerTab.Variables" ) ); //$NON-NLS-1$
			fRegBookKeeping = new Button( group, SWT.CHECK );
			fRegBookKeeping.setText( LaunchMessages.getString( "CDebuggerTab.Registers" ) ); //$NON-NLS-1$
			initialize();
			return composite;
		}

		protected void okPressed() {
			saveValues();
			super.okPressed();
		}

		private void initialize() {
			Map attr = getAdvancedAttributes();
			Object varBookkeeping = attr.get( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING );
			fVarBookKeeping.setSelection( ( varBookkeeping instanceof Boolean ) ? !((Boolean)varBookkeeping).booleanValue() : true );
			Object regBookkeeping = attr.get( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING );
			fRegBookKeeping.setSelection( ( regBookkeeping instanceof Boolean ) ? !((Boolean)regBookkeeping).booleanValue() : true );
		}

		private void saveValues() {
			Map attr = getAdvancedAttributes();
			Boolean varBookkeeping = ( fVarBookKeeping.getSelection() ) ? Boolean.FALSE : Boolean.TRUE;
			attr.put( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, varBookkeeping );
			Boolean regBookkeeping = ( fRegBookKeeping.getSelection() ) ? Boolean.FALSE : Boolean.TRUE;
			attr.put( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, regBookkeeping );
			updateLaunchConfigurationDialog();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell( Shell newShell ) {
			super.configureShell( newShell );
			newShell.setText( LaunchMessages.getString( "CDebuggerTab.Advanced_Options_Dialog_Title" ) ); //$NON-NLS-1$
		}
	}

	final protected boolean fAttachMode;

	protected Button fAdvancedButton;
	protected Button fStopInMain;
	protected Button fAttachButton;

	private Map fAdvancedAttributes = new HashMap( 5 );

	public CDebuggerTab(boolean attachMode) {
		fAttachMode = attachMode;
	}
	
	
	public void createControl( Composite parent ) {
		Composite comp = new Composite( parent, SWT.NONE );
		setControl( comp );
		WorkbenchHelp.setHelp( getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB );
		GridLayout layout = new GridLayout( 2, true );
		comp.setLayout( layout );
		GridData gd = new GridData( GridData.BEGINNING, GridData.CENTER, true, false );
		comp.setLayoutData( gd );

		createDebuggerCombo( comp, ( fAttachMode ) ? 1 : 2 );
		createOptionsComposite( comp );
		createDebuggerGroup( comp, 2 );
	}

	protected void loadDebuggerComboBox( ILaunchConfiguration config, String selection ) {
		ICDebugConfiguration[] debugConfigs;
		String configPlatform = getPlatform( config );
		debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		Arrays.sort( debugConfigs, new Comparator() {

			public int compare( Object o1, Object o2 ) {
				ICDebugConfiguration ic1 = (ICDebugConfiguration)o1;
				ICDebugConfiguration ic2 = (ICDebugConfiguration)o2;
				return ic1.getName().compareTo( ic2.getName() );
			}
		} );
		List  list = new ArrayList();
		String mode;
		if (fAttachMode) {
			mode = ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH;
		} else {
			mode = ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN;  
		}
		String defaultSelection = selection;
		for( int i = 0; i < debugConfigs.length; i++ ) {
			if ( debugConfigs[i].supportsMode( mode )) { 
				String debuggerPlatform = debugConfigs[i].getPlatform();
				if ( validatePlatform( config, debugConfigs[i] ) ) {
					list.add( debugConfigs[i] );
					// select first exact matching debugger for platform or requested selection
					if ( (defaultSelection.equals("") && debuggerPlatform.equalsIgnoreCase( configPlatform ))) { //$NON-NLS-1$
						defaultSelection = debugConfigs[i].getID();
					}
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on tab
		setInitializeDefault( selection.equals( "" ) ? true : false ); //$NON-NLS-1$
		loadDebuggerCombo((ICDebugConfiguration[])list.toArray(new ICDebugConfiguration[list.size()]), defaultSelection);
	}

	protected void updateComboFromSelection() {
		super.updateComboFromSelection();
		initializeCommonControls( getLaunchConfiguration() );
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy config ) {
		super.setDefaults( config );
		if (fAttachMode) {
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH );
		} else {
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT );
		}
		config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false );
		config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false );
	}

	public void initializeFrom( ILaunchConfiguration config ) {
		setInitializing( true );
		super.initializeFrom( config );
		try {
			String id = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, "" ); //$NON-NLS-1$
			loadDebuggerComboBox( config, id );
			initializeCommonControls( config );
		}
		catch( CoreException e ) {
		}
		setInitializing( false );
	}

	public void performApply( ILaunchConfigurationWorkingCopy config ) {
		super.performApply( config );
		if ( fAttachMode ) {
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH );
		}
		else {
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, fStopInMain.getSelection() );
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
		}
		applyAdvancedAttributes( config );
	}

	public boolean isValid( ILaunchConfiguration config ) {
		if ( !validateDebuggerConfig( config ) ) {
			return false;
		}
		ICDebugConfiguration debugConfig = getDebugConfig();
		String mode = fAttachMode ? ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH : ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
		if ( !debugConfig.supportsMode( mode ) ) {
			setErrorMessage( MessageFormat.format( LaunchMessages.getString( "CDebuggerTab.Mode_not_supported" ), new String[] { mode } ) ); //$NON-NLS-1$
			return false;
		}
		if ( super.isValid( config ) == false ) {
			return false;
		}
		return true;
	}
	
	protected boolean validatePlatform( ILaunchConfiguration config, ICDebugConfiguration debugConfig ) {
		String configPlatform = getPlatform( config );
		String debuggerPlatform = debugConfig.getPlatform();
		return ( debuggerPlatform.equals( "*" ) || debuggerPlatform.equalsIgnoreCase( configPlatform ) ); //$NON-NLS-1$
	}

	protected boolean validateCPU( ILaunchConfiguration config, ICDebugConfiguration debugConfig ) {
		ICElement ce = getContext( config, null );
		String projectCPU = ICDebugConfiguration.CPU_NATIVE;
		if ( ce != null ) {
			if ( ce instanceof IBinary ) {
				IBinary bin = (IBinary)ce;
				projectCPU = bin.getCPU();
			}
		}
		return debugConfig.supportsCPU( projectCPU );
	}

	protected boolean validateDebuggerConfig( ILaunchConfiguration config ) {
		ICDebugConfiguration debugConfig = getDebugConfig();
		if ( debugConfig == null ) {
			setErrorMessage( LaunchMessages.getString( "CDebuggerTab.No_debugger_available" ) ); //$NON-NLS-1$
			return false;
		}
		if ( !validatePlatform( config, debugConfig ) ) {
			setErrorMessage( LaunchMessages.getString( "CDebuggerTab.Platform_is_not_supported" ) ); //$NON-NLS-1$
			return false;
		}
		if (!validateCPU( config, debugConfig ) ) {
			setErrorMessage( LaunchMessages.getString( "CDebuggerTab.CPU_is_not_supported" ) ); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	protected void createOptionsComposite( Composite parent ) {
		Composite optionsComp = new Composite( parent, SWT.NONE );

		if (fAttachMode == true) {
			GridLayout layout = new GridLayout( 1, false );
			optionsComp.setLayout( layout );
			optionsComp.setLayoutData( new GridData( GridData.BEGINNING, GridData.CENTER, true, false, 1, 1 ) );
		} else {
			GridLayout layout = new GridLayout( 2, false );
			optionsComp.setLayout( layout );
			optionsComp.setLayoutData( new GridData( GridData.BEGINNING, GridData.CENTER, true, false, 2, 1 ) );
			fStopInMain = createCheckButton( optionsComp, LaunchMessages.getString( "CDebuggerTab.Stop_at_main_on_startup" ) ); //$NON-NLS-1$
			GridData data = new GridData();
			data.horizontalAlignment = GridData.BEGINNING;
			fStopInMain.setLayoutData( data );
			fStopInMain.addSelectionListener( new SelectionAdapter() {
				
				public void widgetSelected( SelectionEvent e ) {
					if ( !isInitializing() ) {
						updateLaunchConfigurationDialog();
					}
				}
			} );
		}
		fAdvancedButton = createPushButton( optionsComp, LaunchMessages.getString( "CDebuggerTab.Advanced" ), null ); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		PixelConverter pc = new PixelConverter( parent );
		data.widthHint = pc.convertHorizontalDLUsToPixels( IDialogConstants.BUTTON_WIDTH );
		fAdvancedButton.setLayoutData( data );
		fAdvancedButton.addSelectionListener( new SelectionAdapter() {
			
			public void widgetSelected( SelectionEvent e ) {
				Dialog dialog = new AdvancedDebuggerOptionsDialog( getShell() );
				dialog.open();
			}
		} );
	}

	protected Map getAdvancedAttributes() {
		return fAdvancedAttributes;
	}

	private void initializeAdvancedAttributes( ILaunchConfiguration config ) {
		Map attr = getAdvancedAttributes();
		try {
			Boolean varBookkeeping = ( config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false ) ) ? Boolean.TRUE : Boolean.FALSE;
			attr.put( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, varBookkeeping );
		}
		catch( CoreException e ) {
		}
		try {
			Boolean regBookkeeping = ( config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false ) ) ? Boolean.TRUE : Boolean.FALSE;
			attr.put( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, regBookkeeping );
		}
		catch( CoreException e ) {
		}
	}

	private void applyAdvancedAttributes( ILaunchConfigurationWorkingCopy config ) {
		Map attr = getAdvancedAttributes();
		Object varBookkeeping = attr.get( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING );
		if ( varBookkeeping instanceof Boolean )
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, ((Boolean)varBookkeeping).booleanValue() );
		Object regBookkeeping = attr.get( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING );
		if ( regBookkeeping instanceof Boolean )
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, ((Boolean)regBookkeeping).booleanValue() );
	}

	protected Shell getShell() {
		return super.getShell();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		getAdvancedAttributes().clear();
		super.dispose();
	}

	protected void initializeCommonControls( ILaunchConfiguration config ) {
		try {
			if (!fAttachMode) {
				fStopInMain.setSelection( config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT ) );
			}
			initializeAdvancedAttributes( config );
		}
		catch( CoreException e ) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab#setInitializeDefault(boolean)
	 */
	protected void setInitializeDefault( boolean init ) {
		super.setInitializeDefault( init );
	}
}