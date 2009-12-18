/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * e.g.: int a[]= {1,2,3};
 */
public class CPPASTInitializerList extends ASTNode implements ICPPASTInitializerList {
    private IASTInitializer [] initializers = null;
    private int initializersPos=-1;
    private int actualLength;
	private boolean fIsPackExpansion;
    
	public CPPASTInitializerList copy() {
		CPPASTInitializerList copy = new CPPASTInitializerList();
		for(IASTInitializer initializer : getInitializers())
			copy.addInitializer(initializer == null ? null : initializer.copy());
		copy.setOffsetAndLength(this);
		copy.actualLength= getSize();
		copy.fIsPackExpansion= fIsPackExpansion;
		return copy;
	}
	
	public int getSize() {
		return actualLength;
	}
	
	public IASTInitializer[] getInitializers() {
		if (initializers == null)
			return IASTInitializer.EMPTY_INITIALIZER_ARRAY;
		
		initializers = ArrayUtil.trimAt(IASTInitializer.class, initializers, initializersPos);
		return initializers;
	}
    
	public void addInitializer(IASTInitializer d) {
		assertNotFrozen();
		if (d != null) {
			initializers = (IASTInitializer[]) ArrayUtil.append(IASTInitializer.class, initializers,
					++initializersPos, d);
			d.setParent(this);
			d.setPropertyInParent(NESTED_INITIALIZER);
		}
		actualLength++;
	}
        
    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitInitializers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        IASTInitializer [] list = getInitializers();
        for ( int i = 0; i < list.length; i++ ) {
            if( !list[i].accept( action ) ) return false;
        }
        
        if( action.shouldVisitInitializers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
	public boolean isPackExpansion() {
		return fIsPackExpansion;
	}

	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fIsPackExpansion= val;
	}

}
