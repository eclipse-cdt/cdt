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

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;

/**
 * @author jcamelon
 *
 */
public class Declarator implements IParameterCollection 
{
	private boolean isFunction;
    private boolean hasFunctionBody;
    private IASTExpression constructorExpression;
    private boolean pureVirtual = false;
    private final DeclarationWrapper owner1;
	private final Declarator owner2;
	private Declarator ownedDeclarator = null; 
	private String name = ""; 
	private IASTInitializerClause initializerClause = null;
	private List ptrOps = new ArrayList();
	private List parameters = new ArrayList();
	private List arrayModifiers = new ArrayList();
	private List constructorMemberInitializers = new ArrayList();  
	private IASTExceptionSpecification exceptionSpecification = null;
	private IASTExpression bitFieldExpression = null;
	private boolean isConst = false; 
	private boolean isVolatile = false;
	private boolean isKandR = false;  
	
	
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

    /**
     * @return
     */
    public Declarator getOwnedDeclarator()
    {
        return ownedDeclarator;
    }

    /**
     * @param declarator
     */
    public void setOwnedDeclarator(Declarator declarator)
    {
        ownedDeclarator = declarator;
    }
    
    public void setName( ITokenDuple duple )
    {
		setName( duple.toString() );
		setNameStartOffset( duple.getFirstToken().getOffset());
		setNameEndOffset( duple.getLastToken().getEndOffset());
    }

    /**
     * @return
     */
    public IASTExceptionSpecification getExceptionSpecification()
    {
        return exceptionSpecification;
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
     * @param specification
     */
    public void setExceptionSpecification(IASTExceptionSpecification specification)
    {
        exceptionSpecification = specification;
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
     * @return
     */
    public boolean isKandR()
    {
        return isKandR;
    }

    /**
     * @param b
     */
    public void setKandR(boolean b)
    {
        isKandR = b;
    }

    /**
     * @param b
     */
    public void setPureVirtual(boolean b)
    {
     	pureVirtual = b;
    }

    /**
     * @return
     */
    public boolean isPureVirtual()
    {
        return pureVirtual;
    }

    /**
     * @param arrayMod
     */
    public void addArrayModifier(IASTArrayModifier arrayMod)
    {
		arrayModifiers.add( arrayMod );        
    }

    /**
     * @return
     */
    public List getArrayModifiers()
    {
        return arrayModifiers;
    }

    /**
     * @return
     */
    public IASTExpression getBitFieldExpression()
    {
        return bitFieldExpression;
    }

    /**
     * @param expression
     */
    public void setBitFieldExpression(IASTExpression expression)
    {
        bitFieldExpression = expression;
    }

    /**
     * @param astExpression
     */
    public void setConstructorExpression(IASTExpression astExpression)
    {
        constructorExpression = astExpression;
    }

    /**
     * @return
     */
    public IASTExpression getConstructorExpression()
    {
        return constructorExpression;
    }

    /**
     * @param initializer
     */
    public void addConstructorMemberInitializer(IASTConstructorMemberInitializer initializer)
    {
        constructorMemberInitializers.add( initializer );
    }

    /**
     * @return
     */
    public List getConstructorMemberInitializers()
    {
        return constructorMemberInitializers;
    }

    /**
     * @param b
     */
    public void hasFunctionBody(boolean b)
    {
    	hasFunctionBody = true;
    }

    /**
     * @return
     */
    public boolean isFunction()
    {
        return isFunction;
    }

    /**
     * @param b
     */
    public void setIsFunction(boolean b)
    {
        isFunction = b;
    }

}
