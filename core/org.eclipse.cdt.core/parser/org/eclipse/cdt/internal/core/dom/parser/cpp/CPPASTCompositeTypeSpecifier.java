/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

/**
 * @author jcamelon
 */
public class CPPASTCompositeTypeSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTCompositeTypeSpecifier {

    private int k;
    private IASTName n;
    private IScope scope;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#getUnpreprocessedSignature()
     */
    public String getUnpreprocessedSignature() {
       return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier#getBaseSpecifiers()
     */
    public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
        if( baseSpecs == null ) return ICPPASTBaseSpecifier.EMPTY_BASESPECIFIER_ARRAY;
        removeNullBaseSpecs();
        return baseSpecs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier#addBaseSpecifier(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier)
     */
    public void addBaseSpecifier(ICPPASTBaseSpecifier baseSpec) {
        if( baseSpecs == null )
        {
            baseSpecs = new ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[ DEFAULT_DECLARATIONS_LIST_SIZE ];
            currentIndex2 = 0;
        }
        if( baseSpecs.length == currentIndex )
        {
            ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier [] old = baseSpecs;
            baseSpecs = new ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                baseSpecs[i] = old[i];
        }
        baseSpecs[ currentIndex2++ ] = baseSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getKey()
     */
    public int getKey() {
        return k;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#setKey(int)
     */
    public void setKey(int key) {
        k = key;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getName()
     */
    public IASTName getName() {
        return n;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.n = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getMembers()
     */
    public IASTDeclaration[] getMembers() {
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

    private int currentIndex = 0;    
    private IASTDeclaration [] declarations = null;
    private static final int DEFAULT_DECLARATIONS_LIST_SIZE = 4;

    private void removeNullBaseSpecs() {
        int nullCount = 0; 
        for( int i = 0; i < baseSpecs.length; ++i )
            if( baseSpecs[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier [] old = baseSpecs;
        int newSize = old.length - nullCount;
        baseSpecs = new ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[ newSize ];
        for( int i = 0; i < newSize; ++i )
            baseSpecs[i] = old[i];
        currentIndex2 = newSize;
    }

    private int currentIndex2 = 0;    
    private ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier [] baseSpecs = null;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getScope()
     */
    public IScope getScope() {
    	if( scope == null )
    		scope = new CPPClassScope( this );
    	
        return scope;
    }

}
