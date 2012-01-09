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
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;

/**
 * @author jcamelon
 */
public class CPPASTElaboratedTypeSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTElaboratedTypeSpecifier, IASTInternalNameOwner {

    private int kind;
    private IASTName name;
    
    public CPPASTElaboratedTypeSpecifier() {
	}

	public CPPASTElaboratedTypeSpecifier(int kind, IASTName name) {
		this.kind = kind;
		setName(name);
	}

	@Override
	public CPPASTElaboratedTypeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTElaboratedTypeSpecifier copy(CopyStyle style) {
		CPPASTElaboratedTypeSpecifier copy = new CPPASTElaboratedTypeSpecifier(kind, name == null
				? null : name.copy(style));
		copyBaseDeclSpec(copy);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public int getKind() {
        return kind;
    }

    @Override
	public void setKind(int value) {
        assertNotFrozen();
        this.kind = value;
    }

    @Override
	public IASTName getName() {
        return name;
    }

    @Override
	public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(TYPE_NAME);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitDeclSpecifiers) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
        if (name != null) if (!name.accept(action)) return false;
        if (action.shouldVisitDeclSpecifiers) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public int getRoleForName(IASTName n) {
		return getRoleForName(n, true);
	}
	
	@Override
	public int getRoleForName(IASTName n, boolean allowResolution) {
		if (n != name) return r_unclear;
		
		IASTNode parent = getParent();
		if (parent instanceof IASTSimpleDeclaration) {
			IASTDeclarator[] dtors = ((IASTSimpleDeclaration)parent).getDeclarators(); 
			if (dtors.length == 0)
				return r_declaration;
		}
		
		// 7.1.5.3.2: check for simple form <class-key> <identifier>, then it may be a declaration
		final int kind= getKind();
		if (kind == k_class || kind == k_union || kind == k_struct) {
			if (name instanceof ICPPASTQualifiedName == false 
					&& name instanceof ICPPASTTemplateId == false) {
				IBinding binding = allowResolution ? name.resolveBinding() : name.getBinding();
				if (binding != null) {
					if (binding instanceof ICPPInternalBinding) {
						IASTNode[] decls = ((ICPPInternalBinding)binding).getDeclarations();
						if (ArrayUtil.contains(decls, name)) 
							return r_declaration;
					}
					return r_reference;
				}
				// resolution is not allowed.
				return r_unclear;
			}
		}
		return r_reference;
	}
}
