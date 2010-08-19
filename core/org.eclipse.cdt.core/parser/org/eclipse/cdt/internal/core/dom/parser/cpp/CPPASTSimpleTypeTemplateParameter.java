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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Template type parameter as in <code>template &lttypename T&gt class X;</code>
 */
public class CPPASTSimpleTypeTemplateParameter extends ASTNode implements ICPPASTSimpleTypeTemplateParameter {

    private IASTName fName;
    private IASTTypeId fTypeId;
    private boolean fUsesKeywordClass;
    private boolean fIsParameterPack;

    public CPPASTSimpleTypeTemplateParameter() {
	}

	public CPPASTSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId) {
		fUsesKeywordClass= type == st_class;
		setName(name);
		setDefaultType(typeId);
	}
	
	public CPPASTSimpleTypeTemplateParameter copy() {
		CPPASTSimpleTypeTemplateParameter copy = new CPPASTSimpleTypeTemplateParameter();
		copy.fUsesKeywordClass = fUsesKeywordClass;
		copy.fIsParameterPack= fIsParameterPack;
		copy.setName(fName == null ? null : fName.copy());
		copy.setDefaultType(fTypeId == null ? null : fTypeId.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

	
	public boolean isParameterPack() {
		return fIsParameterPack;
	}

	public void setIsParameterPack(boolean val) {
		assertNotFrozen();
		fIsParameterPack= val;
	}

	public int getParameterType() {
        return fUsesKeywordClass ? st_class : st_typename;
    }

    public void setParameterType(int value) {
        assertNotFrozen();
        fUsesKeywordClass = value == st_class;
    }

    public IASTName getName() {
        return fName;
    }

    public void setName(IASTName name) {
        assertNotFrozen();
        this.fName = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(PARAMETER_NAME);
		}
    }

    public IASTTypeId getDefaultType() {
        return fTypeId;
    }

    public void setDefaultType(IASTTypeId typeId) {
        assertNotFrozen();
        this.fTypeId = typeId;
        if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(DEFAULT_TYPE);
		}
    }
    
    @Override
	public boolean accept(ASTVisitor action) {
    	if (action.shouldVisitTemplateParameters) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default : break;
	        }
		}
        
		if (fName != null && !fName.accept(action))
			return false;
		if (fTypeId != null && !fTypeId.accept(action))
			return false;
        
		if (action.shouldVisitTemplateParameters && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

    	return true;
    }

	public int getRoleForName(IASTName n) {
		if (n == fName)
			return r_declaration;
		return r_unclear;
	}

	@Override
	public String toString() {
		return getName().toString();
	}
}
