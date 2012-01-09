/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CPPASTNamespaceAlias extends ASTNode implements ICPPASTNamespaceAlias {
    private IASTName alias;
    private IASTName qualifiedName;
    
	public CPPASTNamespaceAlias(IASTName alias, IASTName qualifiedName) {
		setAlias(alias);
		setMappingName(qualifiedName);
	}

	@Override
	public CPPASTNamespaceAlias copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTNamespaceAlias copy(CopyStyle style) {
		CPPASTNamespaceAlias copy = new CPPASTNamespaceAlias(
				alias == null ? null : alias.copy(style),
				qualifiedName == null ? null : qualifiedName.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
	@Override
	public IASTName getAlias() {
        return alias;
    }

    @Override
	public void setAlias(IASTName name) {
        assertNotFrozen();
        this.alias = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ALIAS_NAME);
		}
    }

    @Override
	public IASTName getMappingName() {
        return qualifiedName;
    }

    @Override
	public void setMappingName(IASTName qualifiedName) {
        assertNotFrozen();
        this.qualifiedName = qualifiedName;
        if (qualifiedName != null) {
			qualifiedName.setParent(this);
			qualifiedName.setPropertyInParent(MAPPING_NAME);
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
        
        if (alias != null && !alias.accept(action)) return false;
        if (qualifiedName != null && !qualifiedName.accept(action)) return false;
        
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
	public int getRoleForName(IASTName n) {
		if (alias == n) return r_definition;
		if (qualifiedName == n) return r_reference;
		return r_unclear;
	}
}
