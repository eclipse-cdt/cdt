/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Template ids consist of an unqualified name (or operator or conversion name) 
 * and an array of template arguments. 
 */
public class CPPASTTemplateId extends CPPASTNameBase implements ICPPASTTemplateId, IASTAmbiguityParent {
	private IASTName templateName;
    private IASTNode[] templateArguments = null;

    public CPPASTTemplateId() {
	}

	public CPPASTTemplateId(IASTName templateName) {
		setTemplateName(templateName);
	}

	@Override
	public CPPASTTemplateId copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTTemplateId copy(CopyStyle style) {
		CPPASTTemplateId copy = new CPPASTTemplateId(templateName == null ?
				null : templateName.copy(style));
		for (IASTNode arg : getTemplateArguments())
			copy.internalAddTemplateArgument(arg == null ? null : arg.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public char[] getSimpleID() {
		return templateName.getSimpleID();
	}

	@Override
	public char[] getLookupKey() {
		return templateName.getLookupKey();
	}
	
	@Override
	public IASTName getTemplateName() {
        return templateName;
    }

    @Override
	public void setTemplateName(IASTName name) {
        assertNotFrozen();
    	assert !(name instanceof ICPPASTQualifiedName) && !(name instanceof ICPPASTTemplateId);
        templateName = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(TEMPLATE_NAME);
		}
    }
    
    private void internalAddTemplateArgument(IASTNode node) {
		assertNotFrozen();
	    templateArguments = ArrayUtil.append(IASTNode.class, templateArguments, node);
	    if (node != null) {
	    	node.setParent(this);
	    	node.setPropertyInParent(TEMPLATE_ID_ARGUMENT);
 		}
    }

    @Override
	public void addTemplateArgument(IASTTypeId typeId) {
    	internalAddTemplateArgument(typeId);
    }

    @Override
	public void addTemplateArgument(IASTExpression expression) {
    	internalAddTemplateArgument(expression);
    }
    
    @Override
	public void addTemplateArgument(ICPPASTAmbiguousTemplateArgument ata) {
    	internalAddTemplateArgument(ata);
    }

    @Override
	public IASTNode[] getTemplateArguments() {
        if (templateArguments == null) return ICPPASTTemplateId.EMPTY_ARG_ARRAY;
        return ArrayUtil.trim(IASTNode.class, templateArguments);
    }

    @Override
	protected IBinding createIntermediateBinding() {
       return CPPTemplates.createBinding(this);
    }

    @Override
	public char[] toCharArray() {
    	assert sAllowNameComputation;
    	
    	StringBuilder buf= new StringBuilder();
    	buf.append(getTemplateName().toCharArray());
    	buf.append('<');
    	boolean needComma= false;
    	boolean cleanupWhitespace= false;
    	final IASTNode[] args= getTemplateArguments();
    	for (IASTNode arg : args) {
    		if (needComma)
    			buf.append(", "); //$NON-NLS-1$
    		needComma= true;
    		IASTNodeLocation[] nodeLocs = arg.getNodeLocations();
    		if (nodeLocs.length == 1 && nodeLocs[0] instanceof IASTFileLocation) {
    			buf.append(arg.getRawSignature());
    			cleanupWhitespace= true;
    		} else if (arg instanceof IASTExpression) {
    			IValue value= Value.create((IASTExpression) arg, Value.MAX_RECURSION_DEPTH);
    			if (value != Value.UNKNOWN && !Value.isDependentValue(value)) {
        			buf.append(value.getSignature());
    			} else {
    				buf.append(arg.getRawSignature());
    				cleanupWhitespace= true;
    			}
    		} else if (arg instanceof IASTTypeId){
    			IType type= CPPVisitor.createType((IASTTypeId) arg);
    			if (type instanceof ISemanticProblem) {
    				buf.append(arg.getRawSignature());
    			} else {
    				ASTTypeUtil.appendType(type, false, buf);
    			}
    		}
    		if (cleanupWhitespace)
    			WHITESPACE_SEQ.matcher(buf).replaceAll(" "); //$NON-NLS-1$
    	}
    	buf.append('>');
    	final int len= buf.length();
    	final char[] result= new char[len];
    	buf.getChars(0, len, result, 0);
    	return result;
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitNames) {
		    switch (action.visit(this)) {
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
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public boolean isDeclaration() {
		return false; //for now this seems to be true
	}

	@Override
	public boolean isReference() {
		return true; //for now this seems to be true
	}

	@Override
	public int getRoleForName(IASTName n) {
		if (n == templateName)
			return r_reference;
		return r_unclear;
	}

    @Override
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
}
