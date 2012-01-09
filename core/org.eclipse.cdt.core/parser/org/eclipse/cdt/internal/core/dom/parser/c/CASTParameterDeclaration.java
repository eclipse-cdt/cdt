/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTParameterDeclaration extends ASTNode implements IASTParameterDeclaration, IASTAmbiguityParent {
	
    private IASTDeclSpecifier declSpec;
    private IASTDeclarator declarator;

    public CASTParameterDeclaration() {
	}

	public CASTParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		setDeclSpecifier(declSpec);
		setDeclarator(declarator);
	}

	@Override
	public CASTParameterDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTParameterDeclaration copy(CopyStyle style) {
		CASTParameterDeclaration copy = new CASTParameterDeclaration();
		copy.setDeclSpecifier(declSpec == null ? null : declSpec.copy(style));
		copy.setDeclarator(declarator == null ? null : declarator.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
        return declSpec;
    }

    @Override
	public IASTDeclarator getDeclarator() {
        return declarator;
    }

    @Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        assertNotFrozen();
        this.declSpec = declSpec;
        if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    @Override
	public void setDeclarator(IASTDeclarator declarator) {
        assertNotFrozen();
        this.declarator = declarator;
        if (declarator != null) {
			declarator.setParent(this);
			declarator.setPropertyInParent(DECLARATOR);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitParameterDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( declSpec != null ) if( !declSpec.accept( action ) ) return false;
        if( declarator != null ) if( !declarator.accept( action ) ) return false;    
        if( action.shouldVisitParameterDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	@Override
	public void replace(IASTNode child, IASTNode other) {
        if (child == declarator) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            declarator= (IASTDeclarator) other;
        }
	}
}
