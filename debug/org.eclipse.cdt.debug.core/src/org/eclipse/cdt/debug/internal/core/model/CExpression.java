/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.ICValue;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 17, 2002
 */
public class CExpression extends CVariable 
						 implements IExpression
{
	/**
	 * Constructor for CExpression.
	 * @param target
	 */
	public CExpression( CDebugTarget target, ICDIExpression cdiExpression )
	{
		super( target, cdiExpression );
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
}
