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
