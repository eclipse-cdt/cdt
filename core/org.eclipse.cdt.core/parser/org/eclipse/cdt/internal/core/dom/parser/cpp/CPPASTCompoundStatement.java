/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTCompoundStatement extends ASTNode
		implements IASTCompoundStatement, IASTAmbiguityParent {
    private IASTStatement[] statements = new IASTStatement[2];
    private ICPPScope scope;

    @Override
	public CPPASTCompoundStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTCompoundStatement copy(CopyStyle style) {
		CPPASTCompoundStatement copy = new CPPASTCompoundStatement();
		for (IASTStatement statement : getStatements())
			copy.addStatement(statement == null ? null : statement.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

    @Override
	public IASTStatement[] getStatements() {
        if (statements == null)
        	return IASTStatement.EMPTY_STATEMENT_ARRAY;
        return ArrayUtil.trim(IASTStatement.class, statements);
    }

    @Override
	public void addStatement(IASTStatement statement) {
        assertNotFrozen();
        statements = ArrayUtil.append(IASTStatement.class, statements, statement);
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(NESTED_STATEMENT);
		}
    }

    @Override
	public IScope getScope() {
    	if (scope == null)
    		scope = new CPPBlockScope(this);
        return scope;
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitStatements) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        IASTStatement[] s = getStatements();
        for (int i = 0; i < s.length; i++) {
            if (!s[i].accept(action))
            	return false;
        }
        if (action.shouldVisitStatements) {
        	switch (action.leave(this)) {
        		case ASTVisitor.PROCESS_ABORT: return false;
        		case ASTVisitor.PROCESS_SKIP: return true;
        		default: break;
        	}
        }
        return true;
    }
    
    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (statements == null) return;
        for (int i = 0; i < statements.length; ++i) {
            if (statements[i] == child) {
                other.setParent(statements[i].getParent());
                other.setPropertyInParent(statements[i].getPropertyInParent());
                statements[i] = (IASTStatement) other;
            }
        }
    }
}
