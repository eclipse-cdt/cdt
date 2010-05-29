/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The content of the <code>Shared Libraries</code> tab of the <code>GDBDebuggerPage</code>.
 */
public class GDBSolibBlock extends Observable implements IMILaunchConfigurationComponent, Observer {

	private IMILaunchConfigurationComponent fSolibSearchPathBlock;

	private Button fAutoSoLibButton;

	private Button fUseSolibForAppButton;
	
	private Composite fControl;
	
	private boolean fAutoSolib = false;

	// Bug 314536 and Bug 314554
	// This option will make the DSF-GDB launch fail, and on Linux we have a problem where it will hang
	// Since this option was added for multi-process but it has not been completed for Linux yet, just hide it completely for now
	private boolean fUseSolibForApp = false;

	public GDBSolibBlock( IMILaunchConfigurationComponent solibSearchBlock, boolean autoSolib, boolean stopOnSolibEvents ) {
		super();
		fSolibSearchPathBlock = solibSearchBlock;
		fAutoSolib = autoSolib;
	}

	public void createControl( Composite parent ) {
		Composite subComp = ControlFactory.createCompositeEx( parent, 1, GridData.FILL_HORIZONTAL );
		((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout)subComp.getLayout()).marginHeight = 0;
		((GridLayout)subComp.getLayout()).marginWidth = 0;
		if ( fSolibSearchPathBlock != null ) {
			fSolibSearchPathBlock.createControl( subComp );
			if ( fSolibSearchPathBlock instanceof Observable )
				((Observable)fSolibSearchPathBlock).addObserver( this );
		}
		if ( fAutoSolib ) {
			fAutoSoLibButton = ControlFactory.createCheckBox( subComp, LaunchUIMessages.getString( "GDBSolibBlock.0" ) ); //$NON-NLS-1$
			fAutoSoLibButton.addSelectionListener( new SelectionAdapter() {

				@Override
				public void widgetSelected( SelectionEvent e ) {
					updateButtons();
					changed();
				}
			} );
		}
		if ( fUseSolibForApp ) {
			fUseSolibForAppButton = ControlFactory.createCheckBox( subComp, LaunchUIMessages.getString( "GDBSolibBlock.2" ) ); //$NON-NLS-1$
			fUseSolibForAppButton.addSelectionListener( new SelectionAdapter() {

				@Override
				public void widgetSelected( SelectionEvent e ) {
					updateButtons();
					changed();
				}
			} );
		}
		fControl = subComp;
	}

	public void initializeFrom( ILaunchConfiguration configuration ) {
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.initializeFrom( configuration );
		try {
			if ( fAutoSoLibButton != null )
				fAutoSoLibButton.setSelection( configuration.getAttribute( IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IGDBLaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT ) );
			if ( fUseSolibForAppButton != null )
				fUseSolibForAppButton.setSelection( configuration.getAttribute( IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP, IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT ) );
			initializeButtons( configuration );
			updateButtons();
		}
		catch( CoreException e ) {
		}
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.performApply( configuration );
		try {
			@SuppressWarnings("unchecked")
			Map<String, Boolean> attrs = configuration.getAttributes();
			
			if ( fAutoSoLibButton != null )
				attrs.put( IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, Boolean.valueOf( fAutoSoLibButton.getSelection() ) );
			if ( fUseSolibForAppButton != null )
				attrs.put( IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP, Boolean.valueOf( fUseSolibForAppButton.getSelection() ) );
			configuration.setAttributes( attrs );
		}
		catch( CoreException e ) {
		}
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.setDefaults( configuration );
		configuration.setAttribute( IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IGDBLaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT );
		configuration.setAttribute( IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP, IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT );
	}

	protected void updateButtons() {
	}

	public void dispose() {
		deleteObservers();
		if ( fSolibSearchPathBlock != null ) {
			if ( fSolibSearchPathBlock instanceof Observable )
				((Observable)fSolibSearchPathBlock).deleteObserver( this );
			fSolibSearchPathBlock.dispose();
		}
	}

	public void update( Observable o, Object arg ) {
		changed();
	}

	protected void changed() {
		setChanged();
		notifyObservers();
	}

	protected void initializeButtons( ILaunchConfiguration configuration ) {
		try {
			boolean enable = !ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE.equals( configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, "" ) ); //$NON-NLS-1$
			if ( fAutoSoLibButton != null )
				fAutoSoLibButton.setEnabled( enable );
			if ( fUseSolibForAppButton != null )
				fUseSolibForAppButton.setEnabled( enable );
		}
		catch( CoreException e ) {
		}
	}

	public Control getControl() {
		return fControl;
	}

	public boolean isValid( ILaunchConfiguration launchConfig ) {
		// TODO Auto-generated method stub
		return false;
	}
}
