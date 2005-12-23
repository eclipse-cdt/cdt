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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
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

	public PDOMNode getParent(IBinding binding) throws CoreException {
		PDOMNode parent = this;
		IScope scope = binding.getScope();
		if (scope != null) {
			IASTName scopeName = scope.getScopeName();
			if (scopeName != null) {
				IBinding scopeBinding = scopeName.resolveBinding();
				PDOMBinding scopePDOMBinding = (PDOMBinding)scopeBinding.getAdapter(PDOMBinding.class);
				if (scopePDOMBinding != null)
					parent = scopePDOMBinding;
			}
		}
		return parent;
	}
	
	public PDOMBinding addName(IASTName name) throws CoreException {
		if (name == null)
			return null;
		
		IBinding binding = name.resolveBinding();
		if (binding == null)
			// Can't tell what it is
			return null;
		
		PDOMBinding pdomBinding = (PDOMBinding)binding.getAdapter(PDOMBinding.class);
		if (pdomBinding == null) {
			PDOMNode parent = getParent(binding);

			if (binding instanceof PDOMBinding)
				pdomBinding = (PDOMBinding)binding;
			else if (binding instanceof CPPField)
				pdomBinding = new PDOMCPPField(pdom, (PDOMCPPClassType)parent, name);
			else if (binding instanceof CPPVariable) {
				if (!(binding.getScope() instanceof CPPBlockScope))
					pdomBinding = new PDOMCPPVariable(pdom, parent, name);
			} else if (binding instanceof CPPMethod) {
				; // TODO
			} else if (binding instanceof CPPFunction) {
				pdomBinding = new PDOMCPPFunction(pdom, parent, name);
			} else if (binding instanceof CPPClassType) {
				pdomBinding = new PDOMCPPClassType(pdom, parent, name);
			}
		}
		
		// Add in the name
		if (pdomBinding != null && name.getFileLocation() != null)
			new PDOMName(pdom, name, pdomBinding);
			
		return pdomBinding;
	}

	private static final class FindBinding extends PDOMNode.NodeVisitor {
		private final IBinding binding;
		public PDOMBinding pdomBinding;
		public FindBinding(PDOMDatabase pdom, IBinding binding) {
			super(pdom, binding.getNameCharArray());
			this.binding = binding;
		}
		public boolean visit(int record) throws CoreException {
			if (record == 0)
				return true;
			PDOMBinding tBinding = pdom.getBinding(record);
			if (!tBinding.hasName(name))
				return false;
			switch (tBinding.getBindingType()) {
			case CPPVARIABLE:
				if (binding instanceof ICPPVariable)
					pdomBinding = tBinding;
				break;
			case CPPFUNCTION:
				if (binding instanceof ICPPFunction)
					pdomBinding = tBinding;
				break;
			case CPPCLASSTYPE:
				if (binding instanceof ICPPClassType)
					pdomBinding = tBinding;
				break;
			case CPPFIELD:
				if (binding instanceof ICPPField)
					pdomBinding = tBinding;
				break;
			}
			return pdomBinding == null;
		}
	}
	
	public PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		PDOMNode parent = getParent(binding);
		if (parent == this) {
			FindBinding visitor = new FindBinding(pdom, binding);
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
		}
		
		return null;
	}
}
