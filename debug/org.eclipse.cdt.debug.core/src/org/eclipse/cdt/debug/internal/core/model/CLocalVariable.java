/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 9, 2002
 */
public class CLocalVariable extends CModificationVariable
{
	public CLocalVariable( CDebugElement parent, ICDIVariable cdiVariable )
	{
		super( parent, cdiVariable );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#supportsCasting()
	 */
	public boolean supportsCasting()
	{
		boolean enabled = false;
		try
		{
			IValue value = getValue();
			if ( value instanceof ICValue )
			{
				switch( ((ICValue)value).getType() )
				{
					case ICValue.TYPE_SIMPLE:
					case ICValue.TYPE_POINTER:
					case ICValue.TYPE_CHAR:
						enabled = true;
						break;
				}
			}	
		}
		catch( DebugException e )
		{
			logError( e );
		}
		return enabled;
	}	
}
