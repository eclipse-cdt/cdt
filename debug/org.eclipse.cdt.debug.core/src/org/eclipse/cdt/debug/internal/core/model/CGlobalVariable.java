/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 *
 * Enter type comment.
 * 
 * @since: Oct 2, 2002
 */
public class CGlobalVariable extends CModificationVariable implements ICGlobalVariable
{
	/**
	 * Constructor for CGlobalVariable.
	 * @param parent
	 * @param cdiVariable
	 */
	public CGlobalVariable( CDebugElement parent, ICDIVariable cdiVariable )
	{
		super( parent, cdiVariable );
	}

	/**
	 * Returns the current value of this variable. The value
	 * is cached.
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException
	{
		if ( !isEnabled() )
			return fDisabledValue;
		if ( fValue == null )
		{
			ICDIValue cdiValue = getCurrentValue();
			if ( cdiValue instanceof ICDIArrayValue )
			{
				ICDIVariable var = null;
				try
				{
					var = getCDIVariable();
				}
				catch( CDIException e )
				{
					requestFailed( "", e ); //$NON-NLS-1$
				}
				int[] dims = getType().getArrayDimensions();
				if ( dims.length > 0 && dims[0] > 0 )
					fValue = CValueFactory.createArrayValue( this, var, 0, dims[0] - 1 );
			}
			else
				fValue = CValueFactory.createGlobalValue( this, getCurrentValue() );
		}
		return fValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events )
	{
		super.handleDebugEvents( events );
		for (int i = 0; i < events.length; i++)
		{
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if (source == null)
				continue;
	
			if ( source.getTarget().equals( getCDITarget() ) )
			{
				if ( event instanceof ICDIResumedEvent )
				{
					try
					{
						setChanged( false );
					}
					catch( DebugException e )
					{
						CDebugCorePlugin.log( e );
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.CVariable#dispose()
	 */
	public void dispose() {
		if ( getShadow() != null )
			getShadow().dispose();
		try {
			getCDISession().getVariableManager().destroyVariable( getCDIVariable() );
		}
		catch( CDIException e ) {
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return true;
	}
}
