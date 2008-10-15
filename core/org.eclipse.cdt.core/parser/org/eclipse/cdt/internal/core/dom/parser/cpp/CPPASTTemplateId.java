/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * @author jcamelon
 */
public class CPPASTTemplateId extends ASTNode implements ICPPASTTemplateId, IASTAmbiguityParent {
	private IASTName templateName;
    private IASTNode[] templateArguments = null;
    private IBinding binding = null;
	private int fResolutionDepth = 0;

    public CPPASTTemplateId() {
	}

	public CPPASTTemplateId(IASTName templateName) {
		setTemplateName(templateName);
	}

	public IASTName getTemplateName() {
        return templateName;
    }

    public void setTemplateName(IASTName name) {
        templateName = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(TEMPLATE_NAME);
		}
    }

    public void addTemplateArgument(IASTTypeId typeId) {
        templateArguments = (IASTNode[]) ArrayUtil.append(IASTNode.class, templateArguments, typeId);
        if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TEMPLATE_ID_ARGUMENT);
		}
    }

    public void addTemplateArgument(IASTExpression expression) {
        templateArguments = (IASTNode[]) ArrayUtil.append(IASTNode.class, templateArguments, expression);
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(TEMPLATE_ID_ARGUMENT);
		}
    }
    
    public void addTemplateArgument(ICPPASTAmbiguousTemplateArgument ata) {
    	templateArguments = (IASTNode[]) ArrayUtil.append(IASTNode.class, templateArguments, ata);
    	if (ata != null) {
    		ata.setParent(this);
    		ata.setPropertyInParent(TEMPLATE_ID_ARGUMENT);
    	}
    }

    public IASTNode[] getTemplateArguments() {
        if (templateArguments == null) return ICPPASTTemplateId.EMPTY_ARG_ARRAY;
        return (IASTNode[]) ArrayUtil.trim(IASTNode.class, templateArguments);
    }

    public IBinding resolveBinding() {
    	if (binding == null) {
    		// protect for infinite recursion
        	if (++fResolutionDepth > CPPASTName.MAX_RESOLUTION_DEPTH) {
        		binding= new CPPASTName.RecursionResolvingBinding(this);
        	} else {
        		binding = CPPTemplates.createBinding(this);
        	}
    	}

        return binding;
    }

	public IASTCompletionContext getCompletionContext() {
		return null;
	}

    public char[] toCharArray() {
        return templateName.toCharArray();
    }

    @Override
	public String toString() {
        return templateName.toString();
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitNames) {
		    switch(action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        if (templateName != null && !templateName.accept(action)) return false;

        IASTNode[] nodes = getTemplateArguments();
        for (int i = 0; i < nodes.length; i++) {
            if (!nodes[i].accept(action)) return false;
        }
        if (action.shouldVisitNames) {
		    switch(action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	public boolean isDeclaration() {
		return false; //for now this seems to be true
	}

	public boolean isReference() {
		return true; //for now this seems to be true
	}

	public int getRoleForName(IASTName n) {
		if (n == templateName)
			return r_reference;
		return r_unclear;
	}

	public IBinding getBinding() {
		return binding;
	}

	public void setBinding(IBinding binding) {
		this.binding = binding;
		fResolutionDepth = 0;
	}

    public void replace(IASTNode child, IASTNode other) {
        if (templateArguments == null) return;
        for (int i = 0; i < templateArguments.length; ++i) {
            if (child == templateArguments[i]) {
                other.setPropertyInParent(child.getPropertyInParent());
                other.setParent(child.getParent());
                templateArguments[i] = other;
            }
        }
    }

	public int getRoleOfName(boolean allowResolution) {
        IASTNode parent = getParent();
        if (parent instanceof IASTInternalNameOwner) {
        	return ((IASTInternalNameOwner) parent).getRoleForName(this, allowResolution);
        }
        if (parent instanceof IASTNameOwner) {
            return ((IASTNameOwner) parent).getRoleForName(this);
        }
        return IASTNameOwner.r_unclear;
	}

    public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            return role == IASTNameOwner.r_definition;
        }
        return false;
    }

	public void incResolutionDepth() {
		if (binding == null && ++fResolutionDepth > CPPASTName.MAX_RESOLUTION_DEPTH) {
			binding = new CPPASTName.RecursionResolvingBinding(this);
		}
	}

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public IASTName getLastName() {
		return this;
	}
}
