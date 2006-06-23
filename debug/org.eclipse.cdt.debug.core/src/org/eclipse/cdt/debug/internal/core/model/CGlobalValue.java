/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.debug.core.DebugException;

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
			fHasChildren = Boolean.valueOf( super.hasVariables() );
		}
		return fHasChildren.booleanValue();
	}
}
