/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;

/**
 * @author jcamelon
 */
public class CPPASTCompoundStatement extends ASTAttributeOwner
		implements ICPPASTCompoundStatement, IASTAmbiguityParent {
    private IASTStatement[] statements = new IASTStatement[2];
    private ICPPScope scope;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

    @Override
	public CPPASTCompoundStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTCompoundStatement copy(CopyStyle style) {
		CPPASTCompoundStatement copy = new CPPASTCompoundStatement();
		for (IASTStatement statement : getStatements()) {
			if (statement == null)
				break;
			copy.addStatement(statement.copy(style));
		}
		return copy(copy, style);
	}

    @Override
	public IASTStatement[] getStatements() {
    	statements = ArrayUtil.trim(statements);
        return statements;
    }

    @Override
	public void addStatement(IASTStatement statement) {
        assertNotFrozen();
        statements = ArrayUtil.append(statements, statement);
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
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = DestructorCallCollector.getLocalVariablesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
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

        if (!acceptByAttributeSpecifiers(action)) return false;
        for (IASTStatement statement : statements) {
        	if (statement == null)
        		break;
            if (!statement.accept(action))
            	return false;
        }

        if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
        	return false;

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
        for (int i = 0; i < statements.length; ++i) {
            if (statements[i] == child) {
                other.setParent(statements[i].getParent());
                other.setPropertyInParent(statements[i].getPropertyInParent());
                statements[i] = (IASTStatement) other;
            }
        }
    }
}
