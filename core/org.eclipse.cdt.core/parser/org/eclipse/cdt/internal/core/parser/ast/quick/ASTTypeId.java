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
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTTypeId implements IASTTypeId
{ 
    private final boolean isVolatile;
    private final boolean isUnsigned;
    private final boolean isTypeName;
    private final boolean isSigned;
    private final boolean isShort;
    private final boolean isLong;
    private final boolean isConst;
    private final Type kind;
    private final char[] name;
    private final List pointerOps;
    private final List arrayMods;
	private final char[] completeSignature;
    /**
     * @param kind
     * @param string
     * @param pointerOps
     * @param arrayMods
     */
    public ASTTypeId(Type kind, char[] string, List pointerOps, List arrayMods, boolean isConst, boolean isVolatile, 
    	boolean isUnsigned, boolean isSigned, boolean isShort, boolean isLong, boolean isTypeName, char[] completeSignature)
    {
       this.kind = kind; 
       this.name = string; 
       this.pointerOps = pointerOps;
       this.arrayMods = arrayMods;
	   this.isVolatile =isVolatile; 
	   this.isUnsigned =isUnsigned;  
	   this.isTypeName =isTypeName;  
	   this.isSigned =isSigned;  
	   this.isShort =isShort;  
	   this.isLong =isLong;  
	   this.isConst =isConst;
	   this.completeSignature = completeSignature;
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
        return String.valueOf(name);
    }
    public char[] getTypeOrClassNameCharArray(){
    	return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#getPointerOperators()
     */
    public Iterator getPointerOperators()
    {
        return pointerOps.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#getArrayModifiers()
     */
    public Iterator getArrayModifiers()
    {
        return arrayMods.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#getFullSignature()
     */
    public String getFullSignature() 
    {
        return String.valueOf(completeSignature);
    }
    public char[] getFullSignatureCharArray(){
    	return completeSignature;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#createTypeSymbol(org.eclipse.cdt.core.parser.ast.IASTFactory)
     */
    public ISymbol getTypeSymbol() throws ASTNotImplementedException
    {
        throw new ASTNotImplementedException(); 
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
        return isTypeName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTypeId#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences(IReferenceManager manager) {
		// do nothing
	}
}
