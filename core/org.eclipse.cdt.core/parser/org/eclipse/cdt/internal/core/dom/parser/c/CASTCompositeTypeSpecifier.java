/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;

/**
 * @author jcamelon
 */
public class CASTCompositeTypeSpecifier extends CASTBaseDeclSpecifier implements
        ICASTCompositeTypeSpecifier {

    private int key;
    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getKey()
     */
    public int getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#setKey(int)
     */
    public void setKey(int key) {
        this.key = key;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    private int currentIndex = 0;
    private IASTDeclaration [] declarations = null;
    private IScope scope = null;
    private static final int DEFAULT_DECLARATIONS_LIST_SIZE = 4;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getMembers()
     */
    public IASTDeclaration [] getMembers() {
        if( declarations == null ) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        removeNullDeclarations();
        return declarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#addMemberDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void addMemberDeclaration(IASTDeclaration declaration) {
        if( declarations == null )
        {
            declarations = new IASTDeclaration[ DEFAULT_DECLARATIONS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( declarations.length == currentIndex )
        {
            IASTDeclaration [] old = declarations;
            declarations = new IASTDeclaration[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                declarations[i] = old[i];
        }
        declarations[ currentIndex++ ] = declaration;
    }
    
    /**
     * @param decls2
     */
    private void removeNullDeclarations() {
        int nullCount = 0; 
        for( int i = 0; i < declarations.length; ++i )
            if( declarations[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTDeclaration [] old = declarations;
        int newSize = old.length - nullCount;
        declarations = new IASTDeclaration[ newSize ];
        for( int i = 0; i < newSize; ++i )
            declarations[i] = old[i];
        currentIndex = newSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getScope()
     */
    public IScope getScope() {
        if( scope == null )
            scope = new CCompositeTypeScope( this );
        return scope;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#getUnpreprocessedSignature()
     */
    public String getUnpreprocessedSignature() {
       return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
    }

}
