/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.debug.core.DebugException;
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
	public CExpression( CDebugTarget target, ICDIVariable cdiVariable )
	{
		super( target, cdiVariable );
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
	}
	
	protected ICDIExpression getCDIExpression()
	{
		return (ICDIExpression)getCDIVariable();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
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
		}
		super.handleDebugEvent(event);
	}
}
