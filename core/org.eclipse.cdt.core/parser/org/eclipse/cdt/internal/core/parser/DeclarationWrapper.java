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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTVariable;

/**
 * @author jcamelon
 *
 */
public class DeclarationWrapper
{
	private final IASTScope scope;
	private IASTTypeSpecifier typeSpecifier;
	private List declarators = new ArrayList(); 
	private boolean typeNamed = false;
	private String name = null;
	private boolean volatil = false;
	private boolean virtual = false;
	private boolean typedef = false;
	private boolean staticc = false;
	private boolean register = false;
	private boolean extern = false;
	private boolean explicit = false;
	private boolean constt = false;
	private int startingOffset = 0;
	private boolean auto = false,
		mutable = false,
		friend = false,
		inline = false;
	/**
	 * @param b
	 */
	public void setAuto(boolean b)
	{
		auto = b;
	}
	/**
	 * @return
	 */
	public IASTScope getScope()
	{
		return scope;
	}
	/**
	 * @param scope
	 */
	public DeclarationWrapper(IASTScope scope)
	{
		this.scope = scope;
	}
	/**
	 * @param b
	 */
	public void setTypenamed(boolean b)
	{
		typeNamed = b;
	}
	/**
	 * @param string
	 */
	public void setTypeName(String string)
	{
		name = string;
	}
	private int type = -1;
	/**
	 * @param i
	 */
	public void setType(int i)
	{
		type = i;
	}
	/**
	 * @param b
	 */
	public void setMutable(boolean b)
	{
		mutable = b;
	}
	/**
	 * @param b
	 */
	public void setFriend(boolean b)
	{
		friend = b;
	}
	/**
	 * @param b
	 */
	public void setInline(boolean b)
	{
		inline = b;
	}
	/**
	 * @param b
	 */
	public void setRegister(boolean b)
	{
		register = b;
	}
	/**
	 * @param b
	 */
	public void setStatic(boolean b)
	{
		staticc = b;
	}
	/**
	 * @param b
	 */
	public void setTypedef(boolean b)
	{
		typedef = b;
	}
	/**
	 * @param b
	 */
	public void setVirtual(boolean b)
	{
		virtual = b;
	}
	/**
	 * @param b
	 */
	public void setVolatile(boolean b)
	{
		volatil = b;
	}
	/**
	 * @param b
	 */
	public void setExtern(boolean b)
	{
		extern = b;
	}
	/**
	 * @param b
	 */
	public void setExplicit(boolean b)
	{
		explicit = b;
	}
	/**
	 * @param b
	 */
	public void setConst(boolean b)
	{
		constt = b;
	}
	/**
	 * @return
	 */
	public boolean isAuto()
	{
		return auto;
	}
	/**
	 * @return
	 */
	public boolean isConst()
	{
		return constt;
	}
	/**
	 * @return
	 */
	public boolean isExplicit()
	{
		return explicit;
	}
	/**
	 * @return
	 */
	public boolean isExtern()
	{
		return extern;
	}
	/**
	 * @return
	 */
	public boolean isFriend()
	{
		return friend;
	}
	/**
	 * @return
	 */
	public boolean isInline()
	{
		return inline;
	}
	/**
	 * @return
	 */
	public boolean isMutable()
	{
		return mutable;
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
	public boolean isRegister()
	{
		return register;
	}
	/**
	 * @return
	 */
	public int getStartingOffset()
	{
		return startingOffset;
	}
	/**
	 * @return
	 */
	public boolean isStatic()
	{
		return staticc;
	}
	/**
	 * @return
	 */
	public int getType()
	{
		return type;
	}
	/**
	 * @return
	 */
	public boolean isTypedef()
	{
		return typedef;
	}
	/**
	 * @return
	 */
	public boolean isTypeNamed()
	{
		return typeNamed;
	}
	/**
	 * @return
	 */
	public boolean isVirtual()
	{
		return virtual;
	}
	/**
	 * @return
	 */
	public boolean isVolatile()
	{
		return volatil;
	}
	
	public void addDeclarator( Declarator d )
	{
		declarators.add( d );
	}
	
	public List getDeclarators()
	{
		return Collections.unmodifiableList( declarators );
	}

    /**
     * @return
     */
    public IASTTypeSpecifier getTypeSpecifier()
    {
        return typeSpecifier;
    }

    /**
     * @param specifier
     */
    public void setTypeSpecifier(IASTTypeSpecifier specifier)
    {
        typeSpecifier = specifier;
    }
    
    /**
     * @param requestor
     */
    public List createAndCallbackASTNodes()
    {
        Iterator i = declarators.iterator();
        List l = new ArrayList();  
        while( i.hasNext() )
        	l.add( createAndCallbackASTNode( (Declarator)i.next() ) );
        return l;
    }
    /**
     * @param declarator
     */
    private Object createAndCallbackASTNode(Declarator declarator)
    {
        boolean isWithinClass = ( getScope() instanceof IASTClassSpecifier );
        boolean isFunction = declarator.isFunction(); 
        
        if( isWithinClass && isFunction )
        	return createMethodASTNode( declarator );
        else if( isWithinClass )
        	return createFieldASTNode( declarator );
        else if ( ( ! isWithinClass )&& isFunction )
        	return createFunctionASTNode( declarator );
        else 
        	return createVariableASTNode( declarator ); 
        
    }
    
    /**
     * @param declarator
     * @return
     */
    private IASTMethod createMethodASTNode(Declarator declarator)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @param declarator
     * @return
     */
    private IASTFunction createFunctionASTNode(Declarator declarator)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @param declarator
     * @return
     */
    private IASTField createFieldASTNode(Declarator declarator)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @param declarator
     * @return
     */
    private IASTVariable createVariableASTNode(Declarator declarator)
    {
        // TODO Auto-generated method stub
        return null;
    }
  
}

