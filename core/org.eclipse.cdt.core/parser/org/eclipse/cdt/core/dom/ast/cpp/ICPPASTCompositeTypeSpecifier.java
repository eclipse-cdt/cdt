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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author jcamelon
 */
public interface ICPPASTCompositeTypeSpecifier extends IASTCompositeTypeSpecifier,
        ICPPASTDeclSpecifier {

    public static final int k_class = IASTCompositeTypeSpecifier.k_last + 1;
    public static final int k_last = k_class;
    
    public static final ASTNodeProperty VISIBILITY_LABEL = new ASTNodeProperty( "Visibility Label"); //$NON-NLS-1$
    public static final ASTNodeProperty BASE_SPECIFIER = new ASTNodeProperty( "Base Specifier"); //$NON-NLS-1$
    
    public static interface ICPPASTBaseSpecifier extends IASTNode
    {
    	public static final ICPPASTBaseSpecifier[] EMPTY_BASESPECIFIER_ARRAY = new ICPPASTBaseSpecifier[0];
    	
        public boolean isVirtual();
        public void setVirtual( boolean value );
        
        public static final int v_public = 1;
        public static final int v_protected = 2;
        public static final int v_private = 3;
        
        public int getVisibility();
        public void setVisibility( int visibility );
        
        public static final ASTNodeProperty NAME = new ASTNodeProperty( "BaseSpec Name"); //$NON-NLS-1$
        public IASTName getName();
        public void setName( IASTName name );
    }
    
    public ICPPASTBaseSpecifier[] getBaseSpecifiers();
    public void addBaseSpecifier( ICPPASTBaseSpecifier baseSpec );
    
}
