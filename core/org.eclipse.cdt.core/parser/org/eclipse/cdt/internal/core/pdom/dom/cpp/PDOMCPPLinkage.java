/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespaceAlias;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMember;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPLinkage extends PDOMLinkage {

	public PDOMCPPLinkage(PDOMDatabase pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPLinkage(PDOMDatabase pdom)
			throws CoreException {
		super(pdom, GPPLanguage.ID, "C++".toCharArray());
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	// Binding types
	public static final int CPPVARIABLE = 1;
	public static final int CPPFUNCTION = 2;
	public static final int CPPCLASSTYPE = 3;
	public static final int CPPFIELD = 4;
	public static final int CPPMETHOD = 5;
	public static final int CPPNAMESPACE = 6;
	public static final int CPPNAMESPACEALIAS = 7;

	public PDOMNode getParent(IBinding binding) throws CoreException {
		PDOMNode parent = this;
		IScope scope = binding.getScope();
		if (scope != null) {
			IASTName scopeName = scope.getScopeName();
			if (scopeName != null) {
				IBinding scopeBinding = scopeName.resolveBinding();
				PDOMBinding scopePDOMBinding = adaptBinding(scopeBinding);
				if (scopePDOMBinding != null)
					parent = scopePDOMBinding;
			}
		}
		return parent;
	}
	
	public PDOMBinding addName(IASTName name) throws CoreException {
		if (name == null || name.toCharArray().length == 0 
				|| name instanceof ICPPASTQualifiedName)
			return null;
		
		IBinding binding = name.resolveBinding();
		if (binding == null || binding instanceof IProblemBinding)
			// Can't tell what it is
			return null;
		
		PDOMBinding pdomBinding = adaptBinding(binding);
		if (pdomBinding == null) {
			PDOMNode parent = getParent(binding);

			if (binding instanceof CPPField && parent instanceof PDOMCPPClassType)
				pdomBinding = new PDOMCPPField(pdom, (PDOMCPPClassType)parent, name);
			else if (binding instanceof CPPVariable) {
				if (!(binding.getScope() instanceof CPPBlockScope))
					pdomBinding = new PDOMCPPVariable(pdom, parent, name);
			} else if (binding instanceof CPPMethod && parent instanceof PDOMCPPClassType) {
				pdomBinding = new PDOMCPPMethod(pdom, (PDOMCPPClassType)parent, name);
			} else if (binding instanceof CPPFunction) {
				pdomBinding = new PDOMCPPFunction(pdom, parent, name);
			} else if (binding instanceof CPPClassType) {
				pdomBinding = new PDOMCPPClassType(pdom, parent, name);
			} else if (binding instanceof CPPNamespaceAlias) {
				pdomBinding = new PDOMCPPNamespaceAlias(pdom, parent, name);
			} else if (binding instanceof CPPNamespace) {
				pdomBinding = new PDOMCPPNamespace(pdom, parent, name);
			}
		}
		
		// Add in the name
		if (pdomBinding != null)
			new PDOMName(pdom, name, pdomBinding);
			
		return pdomBinding;
	}

	private static final class FindBinding extends PDOMNode.NodeVisitor {
		PDOMBinding pdomBinding;
		final int desiredType;
		public FindBinding(PDOMDatabase pdom, char[] name, int desiredType) {
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
			if (tBinding.getBindingType() != desiredType)
				// wrong type, try again
				return true;
			
			// got it
			pdomBinding = tBinding;
			return false;
		}
	}

	protected int getBindingType(IBinding binding) {
		if (binding instanceof ICPPVariable)
			return CPPVARIABLE;
		else if (binding instanceof ICPPFunction)
			return CPPFUNCTION;
		else if (binding instanceof ICPPClassType)
			return CPPCLASSTYPE;
		else if (binding instanceof ICPPField)
			return CPPFIELD;
		else if (binding instanceof ICPPMethod)
			return CPPMETHOD;
		else if (binding instanceof ICPPNamespaceAlias)
			return CPPNAMESPACEALIAS;
		else if (binding instanceof ICPPNamespace)
			return CPPNAMESPACE;
		else
			return 0;
	}
	
	public PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding == null || binding instanceof IProblemBinding)
			return null;
		
		if (binding instanceof PDOMBinding)
			return (PDOMBinding)binding;
		
		PDOMNode parent = getParent(binding);
		if (parent == this) {
			FindBinding visitor = new FindBinding(pdom, binding.getNameCharArray(), getBindingType(binding));
			getIndex().visit(visitor);
			return visitor.pdomBinding;
		} else if (parent instanceof PDOMMemberOwner) {
			PDOMMemberOwner owner = (PDOMMemberOwner)parent;
			PDOMMember[] members = owner.findMembers(binding.getNameCharArray());
			if (members.length > 0)
				return members[0];
		}
		return null;
	}
	
	public PDOMBinding getBinding(int record) throws CoreException {
		if (record == 0)
			return null;
		
		switch (PDOMBinding.getBindingType(pdom, record)) {
		case CPPVARIABLE:
			return new PDOMCPPVariable(pdom, record);
		case CPPFUNCTION:
			return new PDOMCPPFunction(pdom, record);
		case CPPCLASSTYPE:
			return new PDOMCPPClassType(pdom, record);
		case CPPFIELD:
			return new PDOMCPPField(pdom, record);
		case CPPMETHOD:
			return new PDOMCPPMethod(pdom, record);
		case CPPNAMESPACE:
			return new PDOMCPPNamespace(pdom, record);
		case CPPNAMESPACEALIAS:
			return new PDOMCPPNamespaceAlias(pdom, record);
		}
		
		return null;
	}
	
	public PDOMBinding resolveBinding(IASTName name) throws CoreException {
		if (name instanceof ICPPASTQualifiedName) {
			IASTName lastName = ((ICPPASTQualifiedName)name).getLastName();
			return lastName != null ? resolveBinding(lastName) : null;
		}
		IASTNode parent = name.getParent();
		if (parent instanceof ICPPASTQualifiedName)
			parent = parent.getParent();
		if (parent instanceof IASTIdExpression) {
			// reference
			IASTNode eParent = parent.getParent();
			if (eParent instanceof IASTFunctionCallExpression) {
				FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CPPFUNCTION);
				getIndex().visit(visitor);
				return visitor.pdomBinding;
			} else {
				FindBinding visitor = new FindBinding(pdom, name.toCharArray(), 
						(name.getParent() instanceof ICPPASTQualifiedName
								&& ((ICPPASTQualifiedName)name.getParent()).getLastName() != name)
							? CPPNAMESPACE : CPPVARIABLE);
				getIndex().visit(visitor);
				return visitor.pdomBinding;
			}
		} else if (parent instanceof IASTNamedTypeSpecifier) {
			FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CPPCLASSTYPE);
			getIndex().visit(visitor);
			return visitor.pdomBinding;
		} else if (parent instanceof ICPPASTNamespaceAlias) {
			FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CPPNAMESPACE);
			getIndex().visit(visitor);
			return visitor.pdomBinding;
		}
		
		return null;
	}
	
}
