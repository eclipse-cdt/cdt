package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 *
 * Enter type comment.
 * 
 * @since: Oct 2, 2002
 */
public class CGlobalVariable extends CModificationVariable
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
		if ( fValue == null )
		{
			fValue = CValueFactory.createGlobalValue( this, getCurrentValue() );
		}
		return fValue;
	}
}
