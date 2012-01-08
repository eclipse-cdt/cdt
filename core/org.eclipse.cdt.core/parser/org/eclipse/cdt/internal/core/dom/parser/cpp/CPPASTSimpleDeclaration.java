/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTSimpleDeclaration extends ASTNode implements IASTSimpleDeclaration, IASTAmbiguityParent {

    public CPPASTSimpleDeclaration() {
	}

	public CPPASTSimpleDeclaration(IASTDeclSpecifier declSpecifier) {
		setDeclSpecifier(declSpecifier);
	}

	@Override
	public CPPASTSimpleDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTSimpleDeclaration copy(CopyStyle style) {
		CPPASTSimpleDeclaration copy = new CPPASTSimpleDeclaration();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy(style));
		for (IASTDeclarator declarator : getDeclarators())
			copy.addDeclarator(declarator == null ? null : declarator.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    @Override
	public IASTDeclarator[] getDeclarators() {
        if (declarators == null)
        	return IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
        declarators = ArrayUtil.trimAt(IASTDeclarator.class, declarators, declaratorsPos);
        return declarators;
    }
    
    @Override
	public void addDeclarator(IASTDeclarator d) {
        assertNotFrozen();
    	if (d != null) {
    		declarators = ArrayUtil.appendAt(IASTDeclarator.class, declarators, ++declaratorsPos, d);
    		d.setParent(this);
			d.setPropertyInParent(DECLARATOR);
    	}
    }
    
    private IASTDeclarator[] declarators;
    private int declaratorsPos = -1;
    private IASTDeclSpecifier declSpecifier;

    /**
     * @param declSpecifier The declSpecifier to set.
     */
    @Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpecifier) {
        assertNotFrozen();
        this.declSpecifier = declSpecifier;
        if (declSpecifier != null) {
			declSpecifier.setParent(this);
			declSpecifier.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitDeclarations) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        
        if (declSpecifier != null && !declSpecifier.accept(action)) return false;
        IASTDeclarator[] dtors = getDeclarators();
        for (int i = 0; i < dtors.length; i++) {
            if (!dtors[i].accept(action))
            	return false;
        }
        
        if (action.shouldVisitDeclarations) {
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
		IASTDeclarator[] declarators = getDeclarators();
		for (int i = 0; i < declarators.length; i++) {
			if (declarators[i] == child) {
				declarators[i] = (IASTDeclarator)other;
				other.setParent(child.getParent());
	            other.setPropertyInParent(child.getPropertyInParent());
				break;
			}
		}
	}

}
