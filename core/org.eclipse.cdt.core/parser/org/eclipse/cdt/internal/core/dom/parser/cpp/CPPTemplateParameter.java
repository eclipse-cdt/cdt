/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Base implementation for template parameter bindings in the AST.
 */
public abstract class CPPTemplateParameter extends PlatformObject implements ICPPTemplateParameter, ICPPInternalBinding {
	private IASTName [] declarations;
	private final int position;
	
	public CPPTemplateParameter(IASTName name) {
		declarations = new IASTName[] {name};
		
		int pos= -1;
		int nesting= -1;
		ICPPASTTemplateParameter tp= null;
		for (IASTNode node= name.getParent(); node != null; node= node.getParent()) {
			ICPPASTTemplateParameter[] tps= null;
			if (node instanceof ICPPASTTemplateParameter) {
				tp= (ICPPASTTemplateParameter) node;
			} else if (node instanceof ICPPASTTemplateDeclaration) {
				if (++nesting == 0) {
					tps= ((ICPPASTTemplateDeclaration) node).getTemplateParameters();
				}
			} else if (node instanceof ICPPASTTemplatedTypeTemplateParameter) {
				if (++nesting == 0) {
					tps= ((ICPPASTTemplatedTypeTemplateParameter) node).getTemplateParameters();
				}
			}
			
			if (pos == -1 && tps != null && tp != null) {
				for (int i = 0; i < tps.length; i++) {
					if (tps[i] == tp) {
						pos= i;
						break;
					}
				}
			}
		}
		if (nesting < 0)
			nesting= 0;
		if (pos < 0)
			pos= 0;
		
		position= (nesting << 16) + pos;
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
	public String getName() {
		return declarations[0].toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return declarations[0].toCharArray();
	}

	
	public int getParameterPosition() {
		return position;
	}

	public IASTName getPrimaryDeclaration () {
		return declarations[0];
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope(getPrimaryDeclaration());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
	 */
	public String[] getQualifiedName() {
		return new String[] { getName() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
	 */
	public char[][] getQualifiedNameCharArray() {
		return new char [][] {getNameCharArray() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
	 */
	public boolean isGloballyQualified() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDeclarations()
	 */
	public IASTNode[] getDeclarations() {
		return declarations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDefinition()
	 */
	public IASTNode getDefinition() {
		if (declarations != null && declarations.length > 0)
			return declarations[0];
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		addDeclaration(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		IASTName name = (IASTName) node;
		if (declarations == null) {
	        declarations = new IASTName[] { name };
		} else {
	        if (declarations.length > 0 && declarations[0] == node)
	            return;
			//keep the lowest offset declaration in [0]
			if (declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset()) {
				declarations = (IASTName[]) ArrayUtil.prepend(IASTName.class, declarations, name);
			} else {
				declarations = (IASTName[]) ArrayUtil.append(IASTName.class, declarations, name);
			}
	    }
	}
	public void removeDeclaration(IASTNode node) {
		ArrayUtil.remove(declarations, node);
	}
	
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public String toString() {
		return getName();
	}	
	
	public IBinding getOwner() throws DOMException {
		if (declarations == null || declarations.length == 0)
			return null;
		
		IASTNode node= declarations[0];
		while (node instanceof ICPPASTTemplateParameter == false) {
			if (node == null)
				return null;
			
			node= node.getParent();
		}
		
		return CPPTemplates.getContainingTemplate((ICPPASTTemplateParameter) node);
	}
}
