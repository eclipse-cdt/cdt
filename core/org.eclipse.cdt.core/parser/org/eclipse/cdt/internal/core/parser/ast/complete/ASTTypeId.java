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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTTypeId implements IASTTypeId
{
    private final boolean isTypename;
    private final boolean isUnsigned;
    private final boolean isSigned;
    private final boolean isShort;
    private final boolean isLong;
    private final boolean isVolatile;
    private final boolean isConst;
    private final char[] signature;
    private ITokenDuple tokenDuple;
    private final List arrayModifiers;
    private final char[] typeName;
    private final List pointerOps;
    private final Type kind;
    private List references = null; 
    private ISymbol symbol;

    /**
     * 
     */
    public ASTTypeId( Type kind, ITokenDuple duple, List pointerOps, List arrayMods, char[] signature, 
		boolean isConst, boolean isVolatile, boolean isUnsigned, boolean isSigned, boolean isShort, boolean isLong, boolean isTypeName )
    {
 		typeName = ( duple == null ) ? "".toCharArray() : duple.toCharArray() ; //$NON-NLS-1$
 		this.tokenDuple = duple;
 		this.kind = kind;
 		this.pointerOps = pointerOps; 
 		this.arrayModifiers = arrayMods;
 		this.signature = signature; 
		this.isConst = isConst;
		this.isVolatile =  isVolatile;
		this.isUnsigned = isUnsigned;
		this.isSigned = isSigned;
		this.isShort = isShort;
		this.isLong = isLong;
		this.isTypename  = isTypeName;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#getKind()
     */
    public Type getKind()
    {
        return kind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#getType()
     */
    public String getTypeOrClassName()
    {
        return String.valueOf(typeName);
    }
    public char[] getTypeOrClassNameCharArray(){
    	return typeName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#getPointerOperators()
     */
    public Iterator getPointerOperators()
    {
        return pointerOps.iterator();
    }
    public List getPointerOperatorsList(){
        return pointerOps;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#getArrayModifiers()
     */
    public Iterator getArrayModifiers()
    {
        return arrayModifiers.iterator();
    }
    public List getArrayModifiersList(){
        return arrayModifiers;
    }
    
    public List getReferences()
    {
    	return (references == null ) ? Collections.EMPTY_LIST : references;
    }
        
    public ITokenDuple getTokenDuple()
    {
    	return tokenDuple;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#getFullSignature()
     */
    public String getFullSignature()
    {
        return String.valueOf(signature);
    }
    
    public char[] getFullSignatureCharArray(){
    	return signature;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#createTypeSymbol(org.eclipse.cdt.core.parser.ast.IASTFactory)
     */
    public ISymbol getTypeSymbol()
    {
        return symbol;
    }
    
    public void setTypeSymbol( ISymbol symbol )
    {
    	this.symbol = symbol;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#isConst()
     */
    public boolean isConst()
    {
        return isConst;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#isVolatile()
     */
    public boolean isVolatile()
    {        
        return isVolatile;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#isLong()
     */
    public boolean isLong()
    {
        return isLong;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#isShort()
     */
    public boolean isShort()
    {
        return isShort;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#isSigned()
     */
    public boolean isSigned()
    {
        return isSigned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#isUnsigned()
     */
    public boolean isUnsigned()
    {
        return isUnsigned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#isTypename()
     */
    public boolean isTypename()
    {
        return isTypename;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
   		Parser.processReferences(references, requestor);
   		references = null;
   		if( tokenDuple != null )
   			tokenDuple.acceptElement( requestor );
   		
    	List arrayMods = getArrayModifiersList();
    	int size = arrayMods.size();
    	for( int i = 0; i < size; i++ )
    	{
    		((IASTArrayModifier)arrayMods.get(i)).acceptElement(requestor);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
    /**
     * @param list
     */
    public void addReferences(List list )
    {
    	if( references == null )
    		references = new ArrayList( list.size() );
    	for( int i = 0; i < list.size(); ++i )
    		references.add( list.get(i) );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
  		if( tokenDuple != null )
   			tokenDuple.freeReferences( );
   		
		if( references == null || references.isEmpty() ) return;
		references.clear();
	}


}
