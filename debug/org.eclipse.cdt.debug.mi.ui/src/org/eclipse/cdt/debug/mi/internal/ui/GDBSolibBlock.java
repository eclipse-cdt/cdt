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

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
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

/**
 * Enter type comment.
 * 
 * @since Sep 3, 2003
 */
public class GDBSolibBlock extends Observable implements Observer 
{
	private SolibSearchPathBlock fSolibSearchPathBlock;
	private Button fAutoSoLibButton;
	private Button fStopOnSolibEventsButton;
	private Composite fControl;

	public GDBSolibBlock()
	{
		super();
	}

	public void createBlock( Composite parent, boolean solibPath, boolean autoSolib, boolean stopOnSolibEvents )
	{
		Composite subComp = ControlFactory.createCompositeEx( parent, 1, GridData.FILL_HORIZONTAL );
		((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false; 
		((GridLayout)subComp.getLayout()).marginHeight = 0; 
		((GridLayout)subComp.getLayout()).marginWidth = 0; 

		if ( solibPath )
		{
			fSolibSearchPathBlock = new SolibSearchPathBlock();
			fSolibSearchPathBlock.createBlock( subComp );
			fSolibSearchPathBlock.addObserver( this );
		}
				
		if ( autoSolib )
		{	
			fAutoSoLibButton = ControlFactory.createCheckBox( subComp, MIUIMessages.getString( "GDBSolibBlock.0" ) ); //$NON-NLS-1$
			fAutoSoLibButton.addSelectionListener( 
										new SelectionAdapter()
											{
												public void widgetSelected( SelectionEvent e )
												{
													updateButtons();
													changed();
												}
											} );
		}

		if ( stopOnSolibEvents )
		{
			fStopOnSolibEventsButton = ControlFactory.createCheckBox( subComp, MIUIMessages.getString( "GDBSolibBlock.1" ) ); //$NON-NLS-1$
			fStopOnSolibEventsButton.addSelectionListener( 
										new SelectionAdapter()
											{
												public void widgetSelected( SelectionEvent e )
												{
													updateButtons();
													changed();
												}
											} );
		}
		setControl( subComp );
	}

	public void initializeFrom( ILaunchConfiguration configuration )
	{
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.initializeFrom( configuration );
		try
		{
			if ( fAutoSoLibButton != null )
				fAutoSoLibButton.setSelection( configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IMILaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT ) );
			if ( fStopOnSolibEventsButton != null )
				fStopOnSolibEventsButton.setSelection( configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, IMILaunchConfigurationConstants.DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT ) );
			initializeButtons( configuration );
			updateButtons();
		}
		catch( CoreException e )
		{
			return;
		}
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration )
	{
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.performApply( configuration );		
		try
		{
			Map attrs = configuration.getAttributes();
			if ( fAutoSoLibButton != null )
				attrs.put( IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, new Boolean( fAutoSoLibButton.getSelection() ) );
			if ( fStopOnSolibEventsButton != null )
				attrs.put( IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, new Boolean( fStopOnSolibEventsButton.getSelection() ) );
			configuration.setAttributes( attrs );
		}
		catch( CoreException e )
		{
		}
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
	{
		if ( fSolibSearchPathBlock != null )
			fSolibSearchPathBlock.setDefaults( configuration );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, 
									IMILaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT );
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, 
									IMILaunchConfigurationConstants.DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT );
	}

	protected void updateButtons()
	{
	}

	public void dispose()
	{
		deleteObservers();
		if ( fSolibSearchPathBlock != null )
		{
			fSolibSearchPathBlock.deleteObserver( this );
			fSolibSearchPathBlock.dispose();
		}
	}

	public void disable()
	{
		if ( fAutoSoLibButton != null )
			fAutoSoLibButton.setEnabled( false );
		if ( fStopOnSolibEventsButton != null )
			fStopOnSolibEventsButton.setEnabled( false );
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg )
	{
		changed();
	}

	protected void changed()
	{
		setChanged();
		notifyObservers();
	}

	public Composite getControl()
	{
		return fControl;
	}

	protected void setControl( Composite composite )
	{
		fControl = composite;
	}

	protected void initializeButtons( ILaunchConfiguration configuration )
	{
		try
		{
			boolean enable = !ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE.equals( configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, "" ) ); //$NON-NLS-1$
			if ( fAutoSoLibButton != null )
				fAutoSoLibButton.setSelection( enable );
			if ( fStopOnSolibEventsButton != null )
				fStopOnSolibEventsButton.setSelection( enable );
		}
		catch( CoreException e )
		{
		}
	}
}

