/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CASTTypeId extends ASTNode implements IASTTypeId {
    private IASTDeclSpecifier declSpecifier;
    private IASTDeclarator declarator;

    public CASTTypeId() {
	}

	public CASTTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		setDeclSpecifier(declSpecifier);
		setAbstractDeclarator(declarator);
	}
	
	@Override
	public CASTTypeId copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTTypeId copy(CopyStyle style) {
		CASTTypeId copy = new CASTTypeId();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy(style));
		copy.setAbstractDeclarator(declarator == null ? null : declarator.copy(style));
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
	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        assertNotFrozen();
        this.declSpecifier = declSpec;
        if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    @Override
	public IASTDeclarator getAbstractDeclarator() {
        return declarator;
    }

    @Override
	public void setAbstractDeclarator(IASTDeclarator abstractDeclarator) {
        assertNotFrozen();
        declarator = abstractDeclarator;
        if (abstractDeclarator != null) {
			abstractDeclarator.setParent(this);
			abstractDeclarator.setPropertyInParent(ABSTRACT_DECLARATOR);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitTypeIds) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        
        if (declSpecifier != null && !declSpecifier.accept(action)) return false;
        if (declarator != null && !declarator.accept(action)) return false;

        if (action.shouldVisitTypeIds) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }
}
