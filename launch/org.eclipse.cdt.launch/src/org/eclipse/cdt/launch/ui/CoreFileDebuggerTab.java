/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.launch.ui; 

import java.util.Map;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

public class CoreFileDebuggerTab extends CDebuggerTab {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl( Composite parent ) {
		Composite comp = new Composite( parent, SWT.NONE );
		setControl( comp );
		WorkbenchHelp.setHelp( getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB );
		GridLayout topLayout = new GridLayout( 2, false );
		comp.setLayout( topLayout );
		createDebuggerCombo( comp );
		createDebuggerGroup( comp );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults( ILaunchConfigurationWorkingCopy config ) {
		super.setDefaults( config );
		config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.launch.ui.CDebuggerTab#initializeCommonControls(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	protected void initializeCommonControls( ILaunchConfiguration config ) {
		// no common controls for this tab
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply( ILaunchConfigurationWorkingCopy config ) {
		if ( getDebugConfig() != null ) {
			config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, getDebugConfig().getID() );
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if ( dynamicTab == null ) {
				config.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map)null );
			}
			else {
				dynamicTab.performApply( config );
			}
		}
	}

	protected void loadDebuggerComboBox( ILaunchConfiguration config, String selection ) {
		fDCombo.removeAll();
		ICDebugConfiguration[] debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		String projectPlatform = getProjectPlatform( config );
		int x = 0;
		int selndx = -1;
		for( int i = 0; i < debugConfigs.length; i++ ) {
			if ( debugConfigs[i].supportsMode( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE ) ) {
				if ( validatePlatform( config, debugConfigs[i] ) ) {
					fDCombo.add( debugConfigs[i].getName() );
					fDCombo.setData( Integer.toString( x ), debugConfigs[i] );
					// select first exact matching debugger for platform or requested selection
					String debuggerPlatform = debugConfigs[i].getPlatform();
					if ( (selndx == -1 && debuggerPlatform.equalsIgnoreCase( projectPlatform )) || selection.equals( debugConfigs[i].getID() ) ) {
						selndx = x;
					}
					x++;
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on tab
		setInitializeDefault( selection.equals( "" ) ? true : false ); //$NON-NLS-1$
		fDCombo.select( selndx == -1 ? 0 : selndx );
		//The behaviour is undefined for if the callbacks should be triggered for this,
		//so to avoid unnecessary confusion, we force an update.
		handleDebuggerChanged();
		getControl().getParent().layout( true );
	}

	protected boolean validatePlatform( ILaunchConfiguration config, ICDebugConfiguration debugConfig ) {
		String projectPlatform = getProjectPlatform( config );
		String debuggerPlatform = debugConfig.getPlatform();
		return ( debuggerPlatform.equals( "*" ) || debuggerPlatform.equalsIgnoreCase( projectPlatform ) ); //$NON-NLS-1$
	}

	private String getProjectPlatform( ILaunchConfiguration config ) {
		ICElement ce = getContext( config, null );
		String projectPlatform = "*"; //$NON-NLS-1$
		if ( ce != null ) {
			try {
				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription( ce.getCProject().getProject(), false );
				if ( descriptor != null ) {
					projectPlatform = descriptor.getPlatform();
				}
			}
			catch( Exception e ) {
			}
		}
		return projectPlatform;
	}
}
