/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * e.g.: int a[]= {1,2,3};
 */
public class CPPASTInitializerList extends ASTNode implements ICPPASTInitializerList, IASTAmbiguityParent {
    
	private IASTInitializerClause [] initializers = null;
    private int initializersPos=-1;
    private int actualSize;
	private boolean fIsPackExpansion;
    
	@Override
	public CPPASTInitializerList copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTInitializerList copy(CopyStyle style) {
		CPPASTInitializerList copy = new CPPASTInitializerList();
		for (IASTInitializerClause initializer : getClauses())
			copy.addClause(initializer == null ? null : initializer.copy(style));
		copy.setOffsetAndLength(this);
		copy.actualSize = getSize();
		copy.fIsPackExpansion = fIsPackExpansion;
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public int getSize() {
		return actualSize;
	}
	
	@Override
	public IASTInitializerClause[] getClauses() {
		if (initializers == null)
			return IASTExpression.EMPTY_EXPRESSION_ARRAY;
		initializers = ArrayUtil.trimAt(IASTInitializerClause.class, initializers, initializersPos);
		return initializers;
	}

	@Override
	@Deprecated
	public IASTInitializer[] getInitializers() {
		IASTInitializerClause[] clauses= getClauses();
		if (clauses.length == 0)
			return IASTInitializer.EMPTY_INITIALIZER_ARRAY;
		
		IASTInitializer[] inits= new IASTInitializer[clauses.length];
		for (int i = 0; i < inits.length; i++) {
			IASTInitializerClause clause= clauses[i]; 
			if (clause instanceof IASTInitializer) {
				inits[i]= (IASTInitializer) clause;
			} else if (clause instanceof IASTExpression) {
				final CPPASTEqualsInitializer initExpr = new CPPASTEqualsInitializer(((IASTExpression) clause).copy());
				initExpr.setParent(this);
				initExpr.setPropertyInParent(NESTED_INITIALIZER);
				inits[i]= initExpr;
			}
		}
		return inits;
	}
    
	@Override
	public void addClause(IASTInitializerClause d) {
        assertNotFrozen();
    	if (d != null) {
    		initializers = ArrayUtil.appendAt( IASTInitializerClause.class, initializers, ++initializersPos, d );
    		d.setParent(this);
			d.setPropertyInParent(NESTED_INITIALIZER);
    	}
    	actualSize++;
    }

	@Override
	@Deprecated
	public void addInitializer(IASTInitializer d) {
        assertNotFrozen();
        if (d instanceof IASTInitializerClause) {
        	addClause((IASTInitializerClause) d);
        } else if (d instanceof IASTEqualsInitializer) {
        	addClause(((IASTEqualsInitializer) d).getInitializerClause());
        } else {
        	addClause(null);
        }
    }
        
    @Override
	public boolean accept( ASTVisitor action ){
		if (action.shouldVisitInitializers) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		IASTInitializerClause[] list = getClauses();
		for (IASTInitializerClause clause : list) {
			if (!clause.accept(action))
				return false;
		}
        
		if (action.shouldVisitInitializers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
    }

	@Override
	public boolean isPackExpansion() {
		return fIsPackExpansion;
	}

	@Override
	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fIsPackExpansion= val;
	}
	
	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (initializers != null) {
			for (int i = 0; i < initializers.length; ++i) {
				if (child == initializers[i]) {
					other.setPropertyInParent(child.getPropertyInParent());
					other.setParent(child.getParent());
					initializers[i] = (IASTInitializerClause) other;
				}
			}
		}
	}
}
