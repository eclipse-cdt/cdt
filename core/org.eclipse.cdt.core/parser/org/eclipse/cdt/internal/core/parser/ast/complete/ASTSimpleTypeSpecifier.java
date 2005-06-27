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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;

/**
 * @author jcamelon
 *
 */
public class ASTSimpleTypeSpecifier extends ASTNode implements IASTSimpleTypeSpecifier
{
    private final List refs;
    private ISymbol symbol;
    private final boolean isTypename;
    private final char[] name;

    /**
     * @param s
     * @param b
     * @param string
     */
    public ASTSimpleTypeSpecifier(ISymbol s, boolean b, char[] string, List references )
    {
    	this.symbol = s; 
    	this.isTypename = b; 
    	this.name = string;
    	this.refs = references;
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#getType()
     */
    public Type getType()
    {
        if( symbol.getType() == ITypeInfo.t_int )
        	return IASTSimpleTypeSpecifier.Type.INT;
        if( symbol.getType() == ITypeInfo.t_double )
        	return IASTSimpleTypeSpecifier.Type.DOUBLE; 
        if( symbol.getType() == ITypeInfo.t_float )
        	return IASTSimpleTypeSpecifier.Type.FLOAT;
        if( symbol.getType() == ITypeInfo.t_bool )
			return IASTSimpleTypeSpecifier.Type.BOOL;
		if( symbol.getType() == ITypeInfo.t_type )
			return IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
		if( symbol.getType() == ITypeInfo.t_char )
			return IASTSimpleTypeSpecifier.Type.CHAR;
		if( symbol.getType() == ITypeInfo.t_void )
			return IASTSimpleTypeSpecifier.Type.VOID;
		if( symbol.getType() == ITypeInfo.t_wchar_t)
			return IASTSimpleTypeSpecifier.Type.WCHAR_T;
		if( symbol.getType() == ITypeInfo.t__Bool )
			return IASTSimpleTypeSpecifier.Type._BOOL;
			
        return IASTSimpleTypeSpecifier.Type.UNSPECIFIED;
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#getTypename()
     */
    public String getTypename()
    {
        return String.valueOf(name);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isLong()
     */
    public boolean isLong()
    {
        return symbol.getTypeInfo().checkBit( ITypeInfo.isLong );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isShort()
     */
    public boolean isShort()
    {
		return symbol.getTypeInfo().checkBit( ITypeInfo.isShort );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isSigned()
     */
    public boolean isSigned()
    {
        return symbol.getTypeInfo().checkBit( ITypeInfo.isSigned);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isUnsigned()
     */
    public boolean isUnsigned()
    {
		return symbol.getTypeInfo().checkBit( ITypeInfo.isUnsigned );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isTypename()
     */
    public boolean isTypename()
    {
        return isTypename;
    }
    /**
     * @return
     */
    public ISymbol getSymbol()
    {
        return symbol;
    }
    
    public List getReferences()
    {
    	return (refs == null ) ? Collections.EMPTY_LIST : refs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#getTypeSpecifier()
     */
    public IASTTypeSpecifier getTypeSpecifier()
    {
        return (IASTTypeSpecifier)getSymbol().getTypeSymbol().getASTExtension().getPrimaryDeclaration();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isComplex()
     */
    public boolean isComplex()
    {
		return symbol.getTypeInfo().checkBit( ITypeInfo.isComplex );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isImaginary()
     */
    public boolean isImaginary()
    {
		return symbol.getTypeInfo().checkBit( ITypeInfo.isImaginary );		        
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#releaseReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void releaseReferences() {
		if( refs == null || refs.isEmpty() ) return;
		refs.clear();
	}
}
