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
package org.eclipse.cdt.internal.core.parser2.cpp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

/**
 * @author jcamelon
 */
public class CPPASTCompositeTypeSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTCompositeTypeSpecifier {

    private int k;
    private IASTName n;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier#getBaseSpecifiers()
     */
    public List getBaseSpecifiers() {
        if( baseSpecs == null ) return Collections.EMPTY_LIST;
        removeNullBaseSpecs();
        return Arrays.asList( baseSpecs );
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
        if( declarations.length == currentIndex )
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
    public List getMembers() {
        if( declarations == null ) return Collections.EMPTY_LIST;
        removeNullDeclarations();
        return Arrays.asList( declarations );

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

}
