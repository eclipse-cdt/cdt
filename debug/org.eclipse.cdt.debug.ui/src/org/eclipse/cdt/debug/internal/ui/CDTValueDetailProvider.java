/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.swt.widgets.Display;

/**
 * Enter type comment.
 * 
 * @since Jun 4, 2003
 */
public class CDTValueDetailProvider
{
	//The shared instance.
	private static CDTValueDetailProvider fInstance = null;

	public static CDTValueDetailProvider getDefault()
	{
		if ( fInstance == null )
		{
			fInstance = new CDTValueDetailProvider();
		}
		return fInstance;
	}

	public void computeDetail( final IValue value, final IValueDetailListener listener )
	{
		if ( value instanceof ICValue )
		{
			Display.getCurrent().asyncExec( new Runnable()
												{
													public void run()
													{
														listener.detailComputed( value, ((ICValue)value).evaluateAsExpression() );
													}
												} );
		}
	}
}
