/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser;

/**
 * @author jcamelon
 *
 */
public class PointerOperator
{
	private String name;
    private boolean isConst = false;
	private boolean isVolatile = false;
	
	public static class Type
	{
		private final int type;
        public static final Type REFERENCE = new Type( 1 );
		public static final Type POINTER   = new Type( 2 );
		public static final Type NAMED = new Type( 3 );
		
		private Type( int type )
		{
			this.type = type; 
		}
	}
	
	private Type type; 
	
	public PointerOperator( Type t )
	{
		this.type = t;
	}
    /**
     * @return
     */
    public Type getType()
    {
        return type;
    }

    /**
     * @return
     */
    public boolean isConst()
    {
        return isConst;
    }

    /**
     * @return
     */
    public boolean isVolatile()
    {
        return isVolatile;
    }

    /**
     * @param b
     */
    public void setConst(boolean b)
    {
        isConst = b;
    }

    /**
     * @param b
     */
    public void setVolatile(boolean b)
    {
        isVolatile = b;
    }
    /**
     * @param string
     */
    public void setName(String string)
    {
        name = string;
    }

    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

}
