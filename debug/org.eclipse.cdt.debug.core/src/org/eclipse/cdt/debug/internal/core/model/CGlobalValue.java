package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.debug.core.DebugException;

/**
 *
 * Enter type comment.
 * 
 * @since: Oct 2, 2002
 */
public class CGlobalValue extends CValue
{
	private Boolean fHasChildren = null;


	/**
	 * Constructor for CGlobalValue.
	 * @param parent
	 * @param cdiValue
	 */
	public CGlobalValue( CVariable parent, ICDIValue cdiValue )
	{
		super( parent, cdiValue );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException
	{
		if ( fHasChildren == null )
		{
			fHasChildren = new Boolean( super.hasVariables() );
		}
		return fHasChildren.booleanValue();
	}
}
