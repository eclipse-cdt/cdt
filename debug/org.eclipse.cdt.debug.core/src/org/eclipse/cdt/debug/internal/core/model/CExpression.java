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
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 17, 2002
 */
public class CExpression extends CModificationVariable 
						 implements IExpression
{
	/**
	 * Constructor for CExpression.
	 * @param target
	 * @param cdiExpression
	 */
	public CExpression( CDebugTarget target, ICDIExpression cdiExpression )
	{
		super( target, cdiExpression );
		fFormat = CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT );
	}

	/**
	 * Constructor for CExpression.
	 * @param target
	 * @param cdiExpression
	 */
	public CExpression( CDebugTarget target, ICDIVariableObject cdiVariableObject )
	{
		super( target, cdiVariableObject );
		fFormat = CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IExpression#getExpressionText()
	 */
	public String getExpressionText()
	{
		try
		{
			return getName();
		}
		catch( DebugException e )
		{
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IExpression#getValue()
	 */
	public IValue getValue()
	{
		try
		{
			return super.getValue();
		}
		catch( DebugException e )
		{
		}
		return null;
	}

	public void dispose()
	{
		super.dispose();
		try {
			ICDIExpression cdiExpression = getCDIExpression();
			if ( cdiExpression != null ) {
					getCDISession().getExpressionManager().destroyExpression( cdiExpression );
			}
		}
		catch( CDIException e ) {
			DebugPlugin.log( e );
		}
	}
	
	protected ICDIExpression getCDIExpression() throws CDIException
	{
		ICDIVariable var = getCDIVariable();
		return ( var instanceof ICDIExpression ) ? (ICDIExpression)var : null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events )
	{
		for (int i = 0; i < events.length; i++)
		{
			ICDIEvent event = events[i];
			if ( event instanceof ICDIResumedEvent )
			{
				if ( event.getSource() instanceof ICDITarget && getCDITarget().equals( event.getSource() ) )
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
				break;
			}
		}
		super.handleDebugEvents(events);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEnabled()
	 */
	public boolean isEnabled()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable()
	{
		return false;
	}
}
