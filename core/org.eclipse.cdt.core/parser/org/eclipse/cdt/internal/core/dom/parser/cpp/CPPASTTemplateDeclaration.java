/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Represents a template declaration.
 */
public class CPPASTTemplateDeclaration extends ASTNode
		implements ICPPASTInternalTemplateDeclaration, IASTAmbiguityParent {
    private boolean exported;
    private byte isAssociatedWithLastName= -1;
    private short nestingLevel= -1;
    private IASTDeclaration declaration;
    private ICPPTemplateScope templateScope;
    private ICPPASTTemplateParameter[] parameters;
    private int parametersPos= -1;
    
    public CPPASTTemplateDeclaration() {
	}

	public CPPASTTemplateDeclaration(IASTDeclaration declaration) {
		setDeclaration(declaration);
	}

	@Override
	public CPPASTTemplateDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTTemplateDeclaration copy(CopyStyle style) {
		CPPASTTemplateDeclaration copy = new CPPASTTemplateDeclaration();
		copy.setDeclaration(declaration == null ? null : declaration.copy(style));
		copy.exported = exported;
		for (ICPPASTTemplateParameter param : getTemplateParameters())
			copy.addTemplateParameter(param == null ? null : param.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public boolean isExported() {
        return exported;
    }

    @Override
	public void setExported(boolean value) {
        assertNotFrozen();
        exported = value;
    }

    @Override
	public IASTDeclaration getDeclaration() {
        return declaration;
    }

    @Override
	public void setDeclaration(IASTDeclaration declaration) {
        assertNotFrozen();
        this.declaration = declaration;
        if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(OWNED_DECLARATION);
		}
    }

    @Override
	public ICPPASTTemplateParameter[] getTemplateParameters() {
        if (parameters == null) return ICPPASTTemplateParameter.EMPTY_TEMPLATEPARAMETER_ARRAY;
        parameters = ArrayUtil.trimAt(ICPPASTTemplateParameter.class, parameters, parametersPos);
        return parameters;
    }

    @Override
	public void addTemplateParameter(ICPPASTTemplateParameter parm) {
        assertNotFrozen();
    	if (parm != null) {
    		parameters = ArrayUtil.appendAt(ICPPASTTemplateParameter.class, parameters, ++parametersPos, parm);
    		parm.setParent(this);
			parm.setPropertyInParent(PARAMETER);
    	}
    }

	@Deprecated
	@Override
	public void addTemplateParamter(ICPPASTTemplateParameter param) {
		addTemplateParameter(param);
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
        
        ICPPASTTemplateParameter[] params = getTemplateParameters();
        for (int i = 0; i < params.length; i++) {
            if (!params[i].accept(action)) return false;
        }
        
        if (declaration != null && !declaration.accept(action)) return false;
        
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
	public ICPPTemplateScope getScope() {
		if (templateScope == null)
			templateScope = new CPPTemplateScope(this);
		return templateScope;
	}
    
    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (declaration == child) {
            other.setParent(child.getParent());
            other.setPropertyInParent(child.getPropertyInParent());
            declaration = (IASTDeclaration) other;
        }
    }

	@Override
	public short getNestingLevel() {
		if (nestingLevel == -1) {
			CPPTemplates.associateTemplateDeclarations(this);
		}
		assert nestingLevel != -1;
		return nestingLevel;
	}

	@Override
	public boolean isAssociatedWithLastName() {
		if (isAssociatedWithLastName == -1)
			CPPTemplates.associateTemplateDeclarations(this);
			
		assert isAssociatedWithLastName != -1;
		return isAssociatedWithLastName != 0;
	}

	@Override
	public void setAssociatedWithLastName(boolean value) {
		isAssociatedWithLastName= value ? (byte) 1 : (byte) 0;
	}

	@Override
	public void setNestingLevel(short level) {
		assert level >= 0;
		nestingLevel= level;
	}
}
