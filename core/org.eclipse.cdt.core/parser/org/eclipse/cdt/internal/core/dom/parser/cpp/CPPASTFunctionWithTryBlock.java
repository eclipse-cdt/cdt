/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Represents a function definition contained in a try block.
 * @see ICPPASTFunctionWithTryBlock
 */
public class CPPASTFunctionWithTryBlock extends CPPASTFunctionDefinition implements ICPPASTFunctionWithTryBlock {
    private ICPPASTCatchHandler[] catchHandlers;
    private int catchHandlersPos= -1;
    
    public CPPASTFunctionWithTryBlock() {
	}

	public CPPASTFunctionWithTryBlock(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement) {
		super(declSpecifier, declarator, bodyStatement);
	}
	
	@Override
	public CPPASTFunctionWithTryBlock copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFunctionWithTryBlock copy(CopyStyle style) {
		IASTDeclSpecifier declSpecifier = getDeclSpecifier();
		IASTFunctionDeclarator declarator = getDeclarator();
		IASTStatement bodyStatement = getBody();

		CPPASTFunctionWithTryBlock copy = new CPPASTFunctionWithTryBlock();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy(style));
		copy.setDeclarator(declarator == null ? null : declarator.copy(style));
		copy.setBody(bodyStatement == null ? null : bodyStatement.copy(style));

		for (ICPPASTConstructorChainInitializer initializer : getMemberInitializers()) {
			copy.addMemberInitializer(initializer == null ? null : initializer.copy(style));
		}
		for (ICPPASTCatchHandler handler : getCatchHandlers()) {
			copy.addCatchHandler(handler == null ? null : handler.copy(style));
		}

		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public void addCatchHandler(ICPPASTCatchHandler statement) {
        assertNotFrozen();
    	if (statement != null) {
    		catchHandlers = ArrayUtil.appendAt(ICPPASTCatchHandler.class, catchHandlers, ++catchHandlersPos, statement);
    		statement.setParent(this);
			statement.setPropertyInParent(CATCH_HANDLER);
    	}
    }

    @Override
	public ICPPASTCatchHandler[] getCatchHandlers() {
        if (catchHandlers == null) return ICPPASTCatchHandler.EMPTY_CATCHHANDLER_ARRAY;
        catchHandlers = ArrayUtil.trimAt(ICPPASTCatchHandler.class, catchHandlers, catchHandlersPos);
        return catchHandlers;
    }

    @Override
	protected boolean acceptCatchHandlers(ASTVisitor action) {
    	final ICPPASTCatchHandler[] handlers = getCatchHandlers();
        for (int i= 0; i < handlers.length; i++) {
            if (!handlers[i].accept(action)) 
            	return false;
        }
        return true;
    }
}
