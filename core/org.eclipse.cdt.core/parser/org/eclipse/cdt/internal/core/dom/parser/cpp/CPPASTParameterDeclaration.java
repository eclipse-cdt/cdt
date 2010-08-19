/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Function parameter or non-type template parameter declaration.
 */
public class CPPASTParameterDeclaration extends ASTNode implements ICPPASTParameterDeclaration, IASTAmbiguityParent {

    private IASTDeclSpecifier fDeclSpec;
    private ICPPASTDeclarator fDeclarator;
    
    public CPPASTParameterDeclaration() {
	}

	public CPPASTParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		setDeclSpecifier(declSpec);
		setDeclarator(declarator);
	}
	
	public boolean isParameterPack() {
		return fDeclarator != null && CPPVisitor.findInnermostDeclarator(fDeclarator).declaresParameterPack();
	}

	public CPPASTParameterDeclaration copy() {
		CPPASTParameterDeclaration copy = new CPPASTParameterDeclaration();
		copy.setDeclSpecifier(fDeclSpec == null ? null : fDeclSpec.copy());
		copy.setDeclarator(fDeclarator == null ? null : fDeclarator.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public IASTDeclSpecifier getDeclSpecifier() {
        return fDeclSpec;
    }

    public ICPPASTDeclarator getDeclarator() {
        return fDeclarator;
    }

    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        assertNotFrozen();
        this.fDeclSpec = declSpec;
        if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    public void setDeclarator(IASTDeclarator declarator) {
        assertNotFrozen();
        if (declarator instanceof ICPPASTDeclarator) {
        	fDeclarator = (ICPPASTDeclarator) declarator;
			declarator.setParent(this);
			declarator.setPropertyInParent(DECLARATOR);
		} else {
			fDeclarator= null;
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
		if (action.shouldVisitParameterDeclarations) {
			switch (action.visit((IASTParameterDeclaration) this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
		if (fDeclSpec != null && !fDeclSpec.accept(action))
			return false;
		if (fDeclarator != null && !fDeclarator.accept(action))
			return false;
        
		if (action.shouldVisitParameterDeclarations &&
				action.leave((IASTParameterDeclaration) this) == ASTVisitor.PROCESS_ABORT) {
			return false;
		}
        return true;
    }
    
	public void replace(IASTNode child, IASTNode other) {
        if (child == fDeclarator) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            fDeclarator= (ICPPASTDeclarator) other;
        }
	}
}
