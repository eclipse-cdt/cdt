/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMember;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public class PDOMCLinkage extends PDOMLinkage {

	public PDOMCLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCLinkage(PDOM pdom) throws CoreException {
		super(pdom, GCCLanguage.ID, "C".toCharArray());
	}
	
	public int getNodeType() {
		return LINKAGE;
	}

	public static final int CVARIABLE = PDOMLinkage.LAST_NODE_TYPE + 1;
	public static final int CFUNCTION = PDOMLinkage.LAST_NODE_TYPE + 2;
	public static final int CSTRUCTURE = PDOMLinkage.LAST_NODE_TYPE + 3;
	public static final int CFIELD = PDOMLinkage.LAST_NODE_TYPE + 4;

	public ILanguage getLanguage() {
		return new GCCLanguage();
	}
	
	public PDOMNode getParent(IBinding binding) throws CoreException {
		IScope scope = binding.getScope();
		if (scope == null)
			return null;
		
		IASTNode scopeNode = scope.getPhysicalNode();
		if (scopeNode instanceof IASTCompoundStatement)
			return null;
		else if (scopeNode instanceof IASTTranslationUnit)
			return this;
		else {
			IASTName scopeName = scope.getScopeName();
			if (scopeName != null) {
				IBinding scopeBinding = scopeName.resolveBinding();
				PDOMBinding scopePDOMBinding = adaptBinding(scopeBinding);
				if (scopePDOMBinding != null)
					return scopePDOMBinding;
			}
		}
			
		return null;
	}
	
	public PDOMBinding addName(IASTName name, PDOMFile file) throws CoreException {
		if (name == null)
			return null;
		
		char[] namechars = name.toCharArray();
		if (namechars == null || name.toCharArray().length == 0)
			return null;
		
		IBinding binding = name.resolveBinding();
		if (binding == null || binding instanceof IProblemBinding)
			// can't tell what it is
			return null;

		if (binding instanceof IParameter)
			// skip parameters
			return null;
	
		PDOMBinding pdomBinding = adaptBinding(binding);
		if (pdomBinding == null) {
			PDOMNode parent = getParent(binding);
			if (parent == null)
				return null;
			
			if (binding instanceof IParameter)
				return null; // skip parameters
			else if (binding instanceof IField) { // must be before IVariable
				if (parent instanceof PDOMMemberOwner)
					pdomBinding = new PDOMCField(pdom, (PDOMMemberOwner)parent, name);
			} else if (binding instanceof IVariable)
				pdomBinding = new PDOMCVariable(pdom, parent, name);
			else if (binding instanceof IFunction)
				pdomBinding = new PDOMCFunction(pdom, parent, name);
			else if (binding instanceof ICompositeType)
				pdomBinding = new PDOMCStructure(pdom, parent, name);
		}
		
		if (pdomBinding != null)
			new PDOMName(pdom, name, file, pdomBinding);
		
		return pdomBinding;
	}

	private static final class FindBinding extends PDOMNamedNode.NodeFinder {
		PDOMBinding pdomBinding;
		final int desiredType;
		public FindBinding(PDOM pdom, char[] name, int desiredType) {
			super(pdom, name);
			this.desiredType = desiredType;
		}
		public boolean visit(int record) throws CoreException {
			if (record == 0)
				return true;
			PDOMBinding tBinding = pdom.getBinding(record);
			if (!tBinding.hasName(name))
				// no more bindings with our desired name
				return false;
			if (tBinding.getNodeType() != desiredType)
				// wrong type, try again
				return true;
			
			// got it
			pdomBinding = tBinding;
			return false;
		}
	}

	protected int getBindingType(IBinding binding) {
		if (binding instanceof IVariable)
			return CVARIABLE;
		else if (binding instanceof IFunction)
			return CFUNCTION;
		else if (binding instanceof ICompositeType)
			return CSTRUCTURE;
		else if (binding instanceof IField)
			return CFIELD;
		else
			return 0;
	}
	
	public PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding instanceof PDOMBinding)
			return (PDOMBinding)binding;
		
		PDOMNode parent = getParent(binding);
		if (parent == this) {
			FindBinding visitor = new FindBinding(pdom, binding.getNameCharArray(), getBindingType(binding));
			getIndex().accept(visitor);
			return visitor.pdomBinding;
		} else if (parent instanceof PDOMMemberOwner) {
			PDOMMemberOwner owner = (PDOMMemberOwner)parent;
			PDOMMember[] members = owner.findMembers(binding.getNameCharArray());
			if (members.length > 0)
				return members[0];
		}
		return null;
	}

	public PDOMNode getNode(int record) throws CoreException {
		if (record == 0)
			return null;
		
		switch (PDOMNode.getNodeType(pdom, record)) {
		case CVARIABLE:
			return new PDOMCVariable(pdom, record);
		case CFUNCTION:
			return new PDOMCFunction(pdom, record);
		case CSTRUCTURE:
			return new PDOMCStructure(pdom, record);
		case CFIELD:
			return new PDOMCField(pdom, record);
		}

		return super.getNode(record);
	}

	public IBinding resolveBinding(IASTName name) throws CoreException {
		IASTNode parent = name.getParent();
		if (parent instanceof IASTIdExpression) {
			// reference
			IASTNode eParent = parent.getParent();
			if (eParent instanceof IASTFunctionCallExpression) {
				FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CFUNCTION);
				getIndex().accept(visitor);
				return visitor.pdomBinding;
			} else {
				FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CVARIABLE);
				getIndex().accept(visitor);
				return visitor.pdomBinding;
			}
		} else if (parent instanceof ICASTElaboratedTypeSpecifier) {
			FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CSTRUCTURE);
			getIndex().accept(visitor);
			return visitor.pdomBinding;
		}
		return null;
	}
	
	public void findBindings(String pattern, List bindings) throws CoreException {
		MatchBinding visitor = new MatchBinding(pdom, pattern, bindings);
		getIndex().accept(visitor);
	}
	
	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
