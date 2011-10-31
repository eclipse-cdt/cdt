/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

public class CPPASTAmbiguousStatement extends ASTAmbiguousNode implements
        IASTAmbiguousStatement {

    private IASTStatement [] stmts = new IASTStatement[2];
    private int stmtsPos=-1;
	private IScope fScope;
	private IASTDeclaration fDeclaration;
    
    public CPPASTAmbiguousStatement(IASTStatement... statements) {
		for(IASTStatement s : statements)
			addStatement(s);
	}

	@Override
	protected void beforeResolution() {
		// Populate containing scope, so that it will not be affected by the alternative 
		// branches.
		fScope= CPPVisitor.getContainingScope(this);
		if (fScope instanceof ICPPASTInternalScope) {
			((ICPPASTInternalScope) fScope).populateCache();
		}
	}
	
	@Override
	protected void beforeAlternative(IASTNode alternative) {
		cleanupScope();
		if (alternative instanceof IASTDeclarationStatement) {
			if (fScope instanceof ICPPASTInternalScope) {
				fDeclaration = ((IASTDeclarationStatement) alternative).getDeclaration();
				CPPSemantics.populateCache((ICPPASTInternalScope) fScope, fDeclaration);
			}
		}
	}
	
	private void cleanupScope() {
		if (fScope instanceof ICPPASTInternalScope && fDeclaration != null) {
			((ICPPASTInternalScope) fScope).removeNestedFromCache(fDeclaration);
		}
	}

	@Override
	protected void afterResolution(ASTVisitor resolver, IASTNode best) {
		beforeAlternative(best);
		fDeclaration= null;
		fScope= null;
	}

    @Override
	public IASTStatement copy() {
    	throw new UnsupportedOperationException();
    }
    
	@Override
	public IASTStatement copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addStatement(IASTStatement s) {
        assertNotFrozen();
    	if (s != null) {
    		stmts = (IASTStatement[]) ArrayUtil.append( IASTStatement.class, stmts, ++stmtsPos, s );
    		s.setParent(this);
			s.setPropertyInParent(STATEMENT);
    	}
    }

    @Override
	public IASTStatement[] getStatements() {
        stmts = (IASTStatement[]) ArrayUtil.removeNullsAfter( IASTStatement.class, stmts, stmtsPos );
    	return stmts;
    }
    
    @Override
	public IASTNode[] getNodes() {
        return getStatements();
    }
}
