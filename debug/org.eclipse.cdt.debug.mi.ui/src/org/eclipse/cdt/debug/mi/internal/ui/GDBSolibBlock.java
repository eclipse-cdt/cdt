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

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.ui.IMILaunchConfigurationComponent;
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

	private Button fStopOnSolibEventsButton;
	
	private Composite fControl;
	
	private boolean fAutoSolib = false;

	private boolean fStopOnSolibEvents = false;

	public GDBSolibBlock( IMILaunchConfigurationComponent solibSearchBlock, boolean autoSolib, boolean stopOnSolibEvents ) {
		super();
		fSolibSearchPathBlock = solibSearchBlock;
		fAutoSolib = autoSolib;
		fStopOnSolibEvents = stopOnSolibEvents;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#createControl(org.eclipse.swt.widgets.Composite)
	 */
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
			fAutoSoLibButton = ControlFactory.createCheckBox( subComp, MIUIMessages.getString( "GDBSolibBlock.0" ) ); //$NON-NLS-1$
			fAutoSoLibButton.addSelectionListener( new SelectionAdapter() {

				public void widgetSelected( SelectionEvent e ) {
					updateButtons();
					changed();
				}
			} );
		}
		if ( fStopOnSolibEvents ) {
			fStopOnSolibEventsButton = ControlFactory.createCheckBox( subComp, MIUIMessages.getString( "GDBSolibBlock.1" ) ); //$NON-NLS-1$
			fStopOnSolibEventsButton.addSelectionListener( new SelectionAdapter() {

				public void widgetSelected( SelectionEvent e ) {
					updateButtons();
					changed();
				}
			} );
		}
		fControl = subComp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom( ILaunchConfiguration configuration ) {
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.initializeFrom( configuration );
		try {
			if ( fAutoSoLibButton != null )
				fAutoSoLibButton.setSelection( configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IMILaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT ) );
			if ( fStopOnSolibEventsButton != null )
				fStopOnSolibEventsButton.setSelection( configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, IMILaunchConfigurationConstants.DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT ) );
			initializeButtons( configuration );
			updateButtons();
		}
		catch( CoreException e ) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.performApply( configuration );
		try {
			Map attrs = configuration.getAttributes();
			if ( fAutoSoLibButton != null )
				attrs.put( IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, Boolean.valueOf( fAutoSoLibButton.getSelection() ) );
			if ( fStopOnSolibEventsButton != null )
				attrs.put( IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, Boolean.valueOf( fStopOnSolibEventsButton.getSelection() ) );
			configuration.setAttributes( attrs );
		}
		catch( CoreException e ) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.setDefaults( configuration );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IMILaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, IMILaunchConfigurationConstants.DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT );
	}

	protected void updateButtons() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#dispose()
	 */
	public void dispose() {
		deleteObservers();
		if ( fSolibSearchPathBlock != null ) {
			if ( fSolibSearchPathBlock instanceof Observable )
				((Observable)fSolibSearchPathBlock).deleteObserver( this );
			fSolibSearchPathBlock.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
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
			if ( fStopOnSolibEventsButton != null )
				fStopOnSolibEventsButton.setEnabled( enable );
		}
		catch( CoreException e ) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#getControl()
	 */
	public Control getControl() {
		return fControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid( ILaunchConfiguration launchConfig ) {
		// TODO Auto-generated method stub
		return false;
	}
}
