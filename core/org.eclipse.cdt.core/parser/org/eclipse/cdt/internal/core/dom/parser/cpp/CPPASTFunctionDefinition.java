/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Models a function definition without a try-block. If used for a constructor definition it may contain
 * member initializers.
 */
public class CPPASTFunctionDefinition extends ASTNode implements
        ICPPASTFunctionDefinition, IASTAmbiguityParent {

    private IASTDeclSpecifier declSpecifier;
    private IASTFunctionDeclarator declarator;
    private IASTStatement bodyStatement;
    private ICPPASTConstructorChainInitializer[] memInits = null;
    private int memInitPos= -1;
    private boolean fDeleted= false;
    private boolean fDefaulted= false;


    public CPPASTFunctionDefinition() {
	}

	public CPPASTFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement) {
		setDeclSpecifier(declSpecifier);
		setDeclarator(declarator);
		setBody(bodyStatement);
	}
	
	public CPPASTFunctionDefinition copy() {
		CPPASTFunctionDefinition copy = new CPPASTFunctionDefinition();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy());
		
		if(declarator != null) {
			IASTDeclarator outer = ASTQueries.findOutermostDeclarator(declarator);
			outer = outer.copy();
			copy.setDeclarator((IASTFunctionDeclarator)ASTQueries.findTypeRelevantDeclarator(outer));
		}	
		
		copy.setBody(bodyStatement == null ? null : bodyStatement.copy());
		
		for(ICPPASTConstructorChainInitializer initializer : getMemberInitializers())
			copy.addMemberInitializer(initializer == null ? null : initializer.copy());
		
		copy.fDefaulted= fDefaulted;
		copy.fDeleted= fDeleted;
		copy.setOffsetAndLength(this);
		return copy;
	}

	public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        assertNotFrozen();
        declSpecifier = declSpec;
        if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    public IASTFunctionDeclarator getDeclarator() {
        return declarator;
    }

    public void setDeclarator(IASTFunctionDeclarator declarator) {
        assertNotFrozen();
        this.declarator = declarator;
        if (declarator != null) {
        	IASTDeclarator outerDtor= ASTQueries.findOutermostDeclarator(declarator);
        	outerDtor.setParent(this);
        	outerDtor.setPropertyInParent(DECLARATOR);
		}
    }

    public IASTStatement getBody() {
        return bodyStatement;
    }

    public void setBody(IASTStatement statement) {
        assertNotFrozen();
        bodyStatement = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(FUNCTION_BODY);
		} 
    }

    
	public void addMemberInitializer(ICPPASTConstructorChainInitializer initializer) {
        assertNotFrozen();
    	if (initializer != null) {
    		memInits= ArrayUtil.appendAt(ICPPASTConstructorChainInitializer.class, memInits, ++memInitPos, initializer);
    		initializer.setParent(this);
			initializer.setPropertyInParent(MEMBER_INITIALIZER);
    	}
	}

	public ICPPASTConstructorChainInitializer[] getMemberInitializers() {
        if (memInits == null) 
        	return ICPPASTConstructorChainInitializer.EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY;
        
        return memInits= ArrayUtil.trimAt(
        		ICPPASTConstructorChainInitializer.class, memInits, memInitPos);
	}

	public IScope getScope() {
		return ((ICPPASTFunctionDeclarator)declarator).getFunctionScope();
	}

	public boolean isDefaulted() {
		return fDefaulted;
	}

	public boolean isDeleted() {
		return fDeleted;
	}

	public void setIsDefaulted(boolean isDefaulted) {
		assertNotFrozen();
		fDefaulted= isDefaulted;
	}

	public void setIsDeleted(boolean isDeleted) {
		assertNotFrozen();
		fDeleted= isDeleted;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (declSpecifier != null && !declSpecifier.accept(action))
			return false;

		final IASTDeclarator outerDtor = ASTQueries.findOutermostDeclarator(declarator);
		if (outerDtor != null && !outerDtor.accept(action))
			return false;

		final ICPPASTConstructorChainInitializer[] chain = getMemberInitializers();
		for (ICPPASTConstructorChainInitializer memInit : chain) {
			if (!memInit.accept(action))
				return false;
		}

		if (bodyStatement != null && !bodyStatement.accept(action))
			return false;

		if (!acceptCatchHandlers(action))
			return false;

		if (action.shouldVisitDeclarations && action.leave(this) == ASTVisitor.PROCESS_ABORT) 
			return false;

		return true;
	}

    /**
     * Allows subclasses to visit catch handlers, returns whether the visit should continue.
     */
	protected boolean acceptCatchHandlers(ASTVisitor action) {
		return true;
	}

	public void replace(IASTNode child, IASTNode other) {
        if( bodyStatement == child ) 
        {
            other.setPropertyInParent( bodyStatement.getPropertyInParent() );
            other.setParent( bodyStatement.getParent() );
            bodyStatement = (IASTStatement) other;
        }
    }
}
