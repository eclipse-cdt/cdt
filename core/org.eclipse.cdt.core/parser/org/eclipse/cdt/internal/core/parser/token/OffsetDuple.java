/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import org.eclipse.cdt.core.parser.IOffsetDuple;

/**
 * @author jcamelon
 *
 */
public class OffsetDuple implements IOffsetDuple
{
	private final int lineFloor, lineCeiling;
    /**
     * @param floor
     * @param ceiling
     */
    public OffsetDuple(int floor, int ceiling)
    {
        lineFloor = floor; 
        lineCeiling = ceiling;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IOffsetDuple#getCeilingOffset()
     */
    public int getCeilingOffset()
    {
        return lineCeiling;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IOffsetDuple#getFloorOffset()
     */
    public int getFloorOffset()
    {
        return lineFloor;
    }
}
