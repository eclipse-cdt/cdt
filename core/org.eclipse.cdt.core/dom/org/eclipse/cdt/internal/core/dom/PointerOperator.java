/**********************************************************************
 * Created on Mar 23, 2003
 *
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.dom;

/**
 * @author jcamelon
 *
 */
public class PointerOperator {

	public final static int t_undefined = 0;
	public final static int t_pointer = 1; 
	public final static int t_reference = 2;
    public final static int t_pointer_to_member = 3;  
	private int type = t_undefined; 
	
	/**
	 * @return int
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	private boolean isConst = false; 
	private boolean isVolatile = false; 
	
	/**
	 * @return boolean
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * @return boolean
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}
	
	public PointerOperator( Declarator decl )
	{
		ownerDeclarator = decl;
	}
	
	private Declarator ownerDeclarator = null; 
	/**
	 * @return Declarator
	 */
	public Declarator getOwnerDeclarator() {
		return ownerDeclarator;
	}
    

    // This is not a complete name, it is something like A::B::, i.e. ends with ::
    private String nameSpecifier = null;
    
    /**
     * @return Class name specifier for pointers to members
     */
    public String getNameSpecifier() {
        return nameSpecifier;
    }

    /**
     * Sets the class name specifier for pointers to members.
     * @param name The name specifier to set
     */
    public void setNameSpecifier(String name) {
        this.nameSpecifier = name;
    }
}
