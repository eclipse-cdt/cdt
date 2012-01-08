/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Base implementation for template parameter bindings in the AST.
 */
public abstract class CPPTemplateParameter extends PlatformObject 
		implements ICPPTemplateParameter, ICPPInternalBinding, ICPPTwoPhaseBinding {
	private IASTName[] declarations;
	private final int fParameterID;
	
	public CPPTemplateParameter(IASTName name) {
		declarations = new IASTName[] {name};
		fParameterID= computeParameterID(name);
	}

	private int computeParameterID(IASTName name) {
		int nesting= 0;
		ICPPASTTemplateParameter tp= null;
		ICPPASTTemplateParameter[] tps= null;
		for (IASTNode node= name.getParent(); node != null; node= node.getParent()) {
			if (tp == null && node instanceof ICPPASTTemplateParameter) {
				tp= (ICPPASTTemplateParameter) node;
			} else if (node instanceof ICPPASTInternalTemplateDeclaration) {
				final ICPPASTInternalTemplateDeclaration tdecl= (ICPPASTInternalTemplateDeclaration) node;
				nesting+= tdecl.getNestingLevel();
				if (tps == null) {
					tps= tdecl.getTemplateParameters();
				}
				break;
			} else if (node instanceof ICPPASTTemplatedTypeTemplateParameter) {
				nesting++;
				if (tps == null) {
					tps= ((ICPPASTTemplatedTypeTemplateParameter) node).getTemplateParameters();
				}
			}
		}
		int pos= 0;
		if (tps != null && tp != null) {
			for (int i = 0; i < tps.length; i++) {
				if (tps[i] == tp) {
					pos= i;
					break;
				}
			}
		}

		return (nesting << 16) + (pos & 0xffff);
	}

	@Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
        }
        return t;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	@Override
	public final String getName() {
		return new String(getNameCharArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	@Override
	public final char[] getNameCharArray() {
		// Search for the first declaration that has a name.
		for (IASTName decl : declarations) {
			if (decl == null)
				break;
			
			final char[] result= decl.getSimpleID();
			if (result.length > 0)
				return result;
		}
		return CharArrayUtils.EMPTY;
	}

	@Override
	public int getParameterID() {
		return fParameterID;
	}

	@Override
	public short getParameterPosition() {
		return (short) fParameterID;
	}

	@Override
	public short getTemplateNestingLevel() {
		return (short) (fParameterID >> 16);
	}

	public IASTName getPrimaryDeclaration () {
		return declarations[0];
	}
	
	private ICPPASTTemplateParameter getASTTemplateParameter() {
		IASTNode node= declarations[0];
		while (node != null && !(node instanceof ICPPASTTemplateParameter))
			node= node.getParent();
		assert node != null;
		return (ICPPASTTemplateParameter) node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(getPrimaryDeclaration());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
	 */
	@Override
	public String[] getQualifiedName() {
		return new String[] { getName() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
	 */
	@Override
	public char[][] getQualifiedNameCharArray() {
		return new char[][] {getNameCharArray() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
	 */
	@Override
	public boolean isGloballyQualified() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDeclarations()
	 */
	@Override
	public IASTName[] getDeclarations() {
		return declarations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDefinition()
	 */
	@Override
	public IASTNode getDefinition() {
		if (declarations != null && declarations.length > 0)
			return declarations[0];
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	@Override
	public void addDefinition(IASTNode node) {
		addDeclaration(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	@Override
	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		IASTName name = (IASTName) node;
		if (declarations == null) {
	        declarations = new IASTName[] { name };
		} else {
	        if (declarations.length > 0 && declarations[0] == node)
	            return;
			// keep the lowest offset declaration in [0]
			if (declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset()) {
				declarations = ArrayUtil.prepend(IASTName.class, declarations, name);
			} else {
				declarations = ArrayUtil.append(IASTName.class, declarations, name);
			}
	    }
	}
	
	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public String toString() {
		return getName();
	}	
	
	@Override
	public IBinding getOwner() {
		if (declarations == null || declarations.length == 0)
			return null;
		
		IASTNode node= declarations[0];
		while (!(node instanceof ICPPASTTemplateParameter)) {
			if (node == null)
				return null;
			
			node= node.getParent();
		}
		
		return CPPTemplates.getContainingTemplate((ICPPASTTemplateParameter) node);
	}
	
	@Override
	public IBinding resolveFinalBinding(CPPASTNameBase name) {
		// check if the binding has been updated.
		IBinding current= name.getPreBinding();
		if (current != this)
			return current;
		
		ICPPTemplateDefinition template= CPPTemplates.getContainingTemplate(getASTTemplateParameter());
		if (template instanceof ICPPInternalTemplate) {
			return ((ICPPInternalTemplate) template).resolveTemplateParameter(this);
		}

		// problem finding the containing template
		if (template == null) {
			return this;
		}
		
		ICPPTemplateParameter[] params = template.getTemplateParameters();
		final int pos= getParameterPosition();
		if (pos < params.length) 
			return params[pos];
		return new ProblemBinding(getPrimaryDeclaration(), IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND);
	}
}
