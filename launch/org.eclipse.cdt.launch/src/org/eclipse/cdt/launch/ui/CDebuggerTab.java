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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

public class CDebuggerTab extends AbstractCDebuggerTab {

	public class AdvancedDebuggerOptionsDialog extends Dialog {

		private Button fVarBookKeeping;

		private Map fAttributes;

		/** 
		 * Constructor for AdvancedDebuggerOptionsDialog. 
		 */
		public AdvancedDebuggerOptionsDialog( Shell parentShell, Map attributes ) {
			super( parentShell );
			fAttributes = attributes;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea( Composite parent ) {
			Composite composite = (Composite)super.createDialogArea( parent );
			fVarBookKeeping = new Button( composite, SWT.CHECK );
			fVarBookKeeping.setText( LaunchUIPlugin.getResourceString( "CDebuggerTab.Automatically_track_values_of_variables" ) ); //$NON-NLS-1$
			initialize();
			return composite;
		}

		private Map getAttributes() {
			return fAttributes;
		}

		protected void okPressed() {
			saveValues();
			super.okPressed();
		}

		private void initialize() {
			Map attr = getAttributes();
			Object varBookkeeping = attr.get( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING );
			fVarBookKeeping.setSelection( ( varBookkeeping instanceof Boolean ) ? !((Boolean)varBookkeeping).booleanValue() : true );
		}

		private void saveValues() {
			Map attr = getAttributes();
			Boolean varBookkeeping = ( fVarBookKeeping.getSelection() ) ? Boolean.FALSE : Boolean.TRUE;
			attr.put( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, varBookkeeping );
			updateLaunchConfigurationDialog();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell( Shell newShell ) {
			super.configureShell( newShell );
			newShell.setText( LaunchUIPlugin.getResourceString( "CDebuggerTab.Advanced_Options_Dialog_Title" ) ); //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#close()
		 */
		public boolean close() {
			fAttributes.clear();
			return super.close();
		}
	}

	protected Combo fDCombo;
	protected Button fAdvancedButton;
	protected Button fStopInMain;
	protected Button fAttachButton;

	private Map fAdvancedAttributes = new HashMap( 5 );

	private boolean fPageUpdated;

	private boolean fIsInitializing = false;

	public void createControl( Composite parent ) {
		Composite comp = new Composite( parent, SWT.NONE );
		setControl( comp );
		WorkbenchHelp.setHelp( getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB );
		GridLayout layout = new GridLayout( 2, true );
		comp.setLayout( layout );
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
		gd.grabExcessHorizontalSpace = true;
		comp.setLayoutData( gd );

		createDebuggerCombo( comp );
		createAttachButton( comp );
		createOptionsComposite( comp );
		createDebuggerGroup( comp );
	}

	protected void loadDebuggerComboBox( ILaunchConfiguration config, String selection ) {
		ICDebugConfiguration[] debugConfigs;
		String configPlatform = getPlatform( config );
		fDCombo.removeAll();
		debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		Arrays.sort( debugConfigs, new Comparator() {

			public int compare( Object o1, Object o2 ) {
				ICDebugConfiguration ic1 = (ICDebugConfiguration)o1;
				ICDebugConfiguration ic2 = (ICDebugConfiguration)o2;
				return ic1.getName().compareTo( ic2.getName() );
			}
		} );
		int selndx = -1;
		int x = 0;
		for( int i = 0; i < debugConfigs.length; i++ ) {
			if ( debugConfigs[i].supportsMode( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN ) || debugConfigs[i].supportsMode( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH ) ) {
				String debuggerPlatform = debugConfigs[i].getPlatform();
				if ( validatePlatform( config, debugConfigs[i] ) ) {
					fDCombo.add( debugConfigs[i].getName() );
					fDCombo.setData( Integer.toString( x ), debugConfigs[i] );
					// select first exact matching debugger for platform or requested selection
					if ( (selndx == -1 && debuggerPlatform.equalsIgnoreCase( configPlatform )) || selection.equals( debugConfigs[i].getID() ) ) {
						selndx = x;
					}
					x++;
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on tab
		setInitializeDefault( selection.equals( "" ) ? true : false ); //$NON-NLS-1$
		fPageUpdated = false;
		fDCombo.select( selndx == -1 ? 0 : selndx );
		//The behaviour is undefined for if the callbacks should be triggered for this,
		//so force page update if needed.
		if ( !fPageUpdated ) {
			updateComboFromSelection();
		}
		fPageUpdated = false;
		getControl().getParent().layout( true );
	}

	protected void updateComboFromSelection() {
		fPageUpdated = true;
		handleDebuggerChanged();
		initializeCommonControls( getLaunchConfigurationWorkingCopy() );
		updateLaunchConfigurationDialog();
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy config ) {
		super.setDefaults( config );
		config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT );
		config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false );
		config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
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
		if ( fAttachButton.getSelection() ) {
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
		if ( debugConfig == null ) {
			setErrorMessage( LaunchUIPlugin.getResourceString( "CDebuggerTab.No_debugger_available" ) ); //$NON-NLS-1$
			return false;
		}
		if ( fAttachButton != null ) {
			String mode = ( fAttachButton.getSelection() ) ? ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH : ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
			if ( !debugConfig.supportsMode( mode ) ) {
				setErrorMessage( MessageFormat.format( LaunchUIPlugin.getResourceString( "CDebuggerTab.Mode_not_supported" ), new String[] { mode } ) ); //$NON-NLS-1$
				return false;
			}
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
			setErrorMessage( LaunchUIPlugin.getResourceString( "CDebuggerTab.No_debugger_available" ) ); //$NON-NLS-1$
			return false;
		}
		if ( !validatePlatform( config, debugConfig ) || !validateCPU( config, debugConfig ) ) {
			setErrorMessage( LaunchUIPlugin.getResourceString( "CDebuggerTab.CPU_is_not_supported" ) ); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * Return the class that implements <code>ILaunchConfigurationTab</code> that is registered against the debugger id of the currently selected debugger.
	 */
	protected ICDebugConfiguration getConfigForCurrentDebugger() {
		int selectedIndex = fDCombo.getSelectionIndex();
		return (ICDebugConfiguration)fDCombo.getData( Integer.toString( selectedIndex ) );
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	protected void createDebuggerCombo( Composite parent ) {
		Composite comboComp = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( 2, false );
		comboComp.setLayout( layout );
		Label dlabel = new Label( comboComp, SWT.NONE );
		dlabel.setText( LaunchUIPlugin.getResourceString( "Launch.common.DebuggerColon" ) ); //$NON-NLS-1$
		fDCombo = new Combo( comboComp, SWT.DROP_DOWN | SWT.READ_ONLY );
		fDCombo.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent e ) {
				if ( !isInitializing() ) {
					setInitializeDefault( true );
					updateComboFromSelection();
				}
			}
		} );
	}

	protected void createAttachButton( Composite parent ) {
		Composite attachComp = new Composite( parent, SWT.NONE );
		GridLayout attachLayout = new GridLayout();
		attachLayout.marginHeight = 0;
		attachLayout.marginWidth = 0;
		attachComp.setLayout( attachLayout );
		fAttachButton = createCheckButton( attachComp, LaunchUIPlugin.getResourceString( "CDebuggerTab.Attach_to_running_process" ) ); //$NON-NLS-1$
		fAttachButton.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					fStopInMain.setSelection( !fAttachButton.getSelection() );
					fStopInMain.setEnabled( !fAttachButton.getSelection() );
					updateLaunchConfigurationDialog();
				}
			}
		} );
	}

	protected void createOptionsComposite( Composite parent ) {
		Composite optionsComp = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( 2, true );
		optionsComp.setLayout( layout );
		optionsComp.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false, 2, 1 ) );

		fStopInMain = createCheckButton( optionsComp, LaunchUIPlugin.getResourceString( "CDebuggerTab.Stop_at_main_on_startup" ) ); //$NON-NLS-1$
		fStopInMain.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent e ) {
				if ( !isInitializing() ) {
					updateLaunchConfigurationDialog();
				}
			}
		} );

		fAdvancedButton = createPushButton( optionsComp, LaunchUIPlugin.getResourceString( "CDebuggerTab.Advanced" ), null ); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		PixelConverter pc = new PixelConverter( parent );
		data.widthHint = pc.convertHorizontalDLUsToPixels( IDialogConstants.BUTTON_WIDTH );
		fAdvancedButton.setLayoutData( data );
		fAdvancedButton.addSelectionListener( new SelectionAdapter() {
			
			public void widgetSelected( SelectionEvent e ) {
				Dialog dialog = new AdvancedDebuggerOptionsDialog( getShell(), getAdvancedAttributes() );
				dialog.open();
			}
		} );
	}

	protected void createDebuggerGroup( Composite parent ) {
		Group debuggerGroup = new Group( parent, SWT.SHADOW_ETCHED_IN );
		debuggerGroup.setText( LaunchUIPlugin.getResourceString( "CDebuggerTab.Debugger_Options" ) ); //$NON-NLS-1$
		setDynamicTabHolder( debuggerGroup );
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		getDynamicTabHolder().setLayout( tabHolderLayout );
		GridData gd = new GridData( GridData.FILL_BOTH );
		gd.horizontalSpan = 2;
		getDynamicTabHolder().setLayoutData( gd );
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
	}

	private void applyAdvancedAttributes( ILaunchConfigurationWorkingCopy config ) {
		Map attr = getAdvancedAttributes();
		Object varBookkeeping = attr.get( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING );
		if ( varBookkeeping instanceof Boolean )
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, ((Boolean)varBookkeeping).booleanValue() );
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
		ICDebugConfiguration debugConfig = getConfigForCurrentDebugger();
		try {
			String mode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
			fAttachButton.setEnabled( debugConfig != null && debugConfig.supportsMode( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH ) );
			if ( fAttachButton.isEnabled() )
				fAttachButton.setSelection( mode.equals( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH ) );
			fStopInMain.setEnabled( debugConfig != null && !fAttachButton.getSelection() );
			if ( fStopInMain.isEnabled() )
				fStopInMain.setSelection( ( fAttachButton.getSelection() ) ? false : config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT ) );
			initializeAdvancedAttributes( config );
		}
		catch( CoreException e ) {
		}
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	private void setInitializing( boolean isInitializing ) {
		fIsInitializing = isInitializing;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.launch.internal.ui.AbstractCDebuggerTab#setInitializeDefault(boolean)
	 */
	protected void setInitializeDefault( boolean init ) {
		super.setInitializeDefault( init );
	}
}