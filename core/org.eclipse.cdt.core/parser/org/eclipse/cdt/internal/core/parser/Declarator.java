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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;

/**
 * @author jcamelon
 *
 */
public class Declarator implements IParameterCollection 
{
	private final DeclarationWrapper owner1;
	private final Declarator owner2;
	private String name; 
	private IASTInitializerClause initializerClause;
	private List ptrOps = new ArrayList();
	private List parameters = new ArrayList();  
	
	private int nameStartOffset, nameEndOffset; 

    public Declarator( DeclarationWrapper owner )
	{
		this.owner1 = owner;
		owner2 = null; 
	}
	
	public Declarator( Declarator owner )
	{
		owner2 = owner;
		owner1 = null;
	}
    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return
     */
    public int getNameEndOffset()
    {
        return nameEndOffset;
    }

    /**
     * @return
     */
    public int getNameStartOffset()
    {
        return nameStartOffset;
    }

    /**
     * @return
     */
    public DeclarationWrapper getOwner()
    {
        return owner1;
    }

    /**
     * @param string
     */
    public void setName(String string)
    {
        name = string;
    }

    /**
     * @param i
     */
    public void setNameEndOffset(int i)
    {
        nameEndOffset = i;
    }

    /**
     * @param i
     */
    public void setNameStartOffset(int i)
    {
        nameStartOffset = i;
    }

    /**
     * @return
     */
    public Declarator getOwnerDeclarator()
    {
        return owner2;
    }

    /**
     * @return
     */
    public List getPtrOps()
    {
        return Collections.unmodifiableList( ptrOps );
    }

	public void addPtrOp( PointerOperator ptrOp )
	{
		ptrOps.add( ptrOp ); 
	}
    /**
     * @return
     */
    public List getParameters()
    {
        return parameters;
    }

	public void addParameter( DeclarationWrapper param )
	{
		parameters.add( param );
	}
    /**
     * @return
     */
    public IASTInitializerClause getInitializerClause()
    {
        return initializerClause;
    }

    /**
     * @param expression
     */
    public void setInitializerClause(IASTInitializerClause expression)
    {
        initializerClause = expression;
    }

}
