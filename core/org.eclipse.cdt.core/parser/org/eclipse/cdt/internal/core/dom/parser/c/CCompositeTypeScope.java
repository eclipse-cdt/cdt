/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

/**
 * Implementation of scope for structs and unions.
 */
public class CCompositeTypeScope extends CScope implements ICCompositeTypeScope {
    public CCompositeTypeScope( ICASTCompositeTypeSpecifier compTypeSpec ){
        super( compTypeSpec, EScopeKind.eClassType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope#getBinding(char[])
     */
    @Override
	public IBinding getBinding( char[] name ) {
        return super.getBinding( NAMESPACE_TYPE_OTHER, name );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    @Override
	public IBinding[] find( String name ) {
        CollectNamesAction action = new CollectNamesAction( name.toCharArray() );
        getPhysicalNode().accept( action );
        
        IASTName [] names = action.getNames();
        IBinding [] result = null;
        for (IASTName astName : names) {
            IBinding b = astName.resolveBinding();
            if( b == null ) continue;
            try {
                if( b.getScope() == this )
                    result = ArrayUtil.append( IBinding.class, result, b );
            } catch ( DOMException e ) {
            }
        }
            
        return ArrayUtil.trim( IBinding.class, result );
    }

	@Override
	public ICompositeType getCompositeType() {
		ICASTCompositeTypeSpecifier compSpec = (ICASTCompositeTypeSpecifier) getPhysicalNode();
		IBinding binding = compSpec.getName().resolveBinding();
		if (binding instanceof ICompositeType)
			return (ICompositeType) binding;

		return new CStructure.CStructureProblem(compSpec.getName(), ISemanticProblem.BINDING_NO_CLASS, compSpec.getName().toCharArray() );
	}
	
	@Override
	protected void doPopulateCache() {
		ICASTCompositeTypeSpecifier compSpec = (ICASTCompositeTypeSpecifier) getPhysicalNode();
		ICASTCompositeTypeSpecifier[] specStack = null;
		int stackIdx = -1;
		IASTDeclaration[] members = compSpec.getMembers();
		while (members != null) {
			int size = members.length;
			for (int i = 0; i < size; i++) {
				IASTNode node = members[i];
				if (node instanceof IASTSimpleDeclaration) {
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration) node).getDeclarators();
					for (IASTDeclarator declarator : declarators) {
						IASTName dtorName = ASTQueries.findInnermostDeclarator(declarator).getName();
						ASTInternal.addName(this, dtorName);
					}
					// anonymous structures and unions
					if (declarators.length == 0 && ((IASTSimpleDeclaration) node).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) {
						ICASTCompositeTypeSpecifier declSpec = (ICASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) node).getDeclSpecifier();
						IASTName n = declSpec.getName();
						if (n.toCharArray().length == 0) {
							specStack = ArrayUtil.append(ICASTCompositeTypeSpecifier.class, specStack, declSpec);
						}
					}
				}
			}
			if (specStack != null && ++stackIdx < specStack.length && specStack[stackIdx] != null) {
				members = specStack[stackIdx].getMembers();
			} else {
				members = null;
			}
		}
	}
}
