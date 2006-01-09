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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
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
 */
public class PDOMCLinkage extends PDOMLinkage {

	public PDOMCLinkage(PDOMDatabase pdom, int record) {
		super(pdom, record);
	}

	public PDOMCLinkage(PDOMDatabase pdom) throws CoreException {
		super(pdom, GCCLanguage.ID, "C".toCharArray());
	}

	public static final int CVARIABLE = 1;
	public static final int CFUNCTION = 2;
	public static final int CSTRUCTURE = 3;
	public static final int CFIELD = 4;

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
		if (name == null || name.toCharArray().length == 0)
			return null;
		
		IBinding binding = name.resolveBinding();
		if (binding == null || binding instanceof IProblemBinding)
			return null;

		PDOMBinding pdomBinding = adaptBinding(binding);
		if (pdomBinding == null) {
			PDOMNode parent = getParent(binding);
			
			if (binding instanceof IParameter)
				; // skip parameters
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
			case CVARIABLE:
				if (binding instanceof IVariable)
					pdomBinding = tBinding;
				break;
			case CFUNCTION:
				if (binding instanceof IFunction)
					pdomBinding = tBinding;
				break;
			case CSTRUCTURE:
				if (binding instanceof ICompositeType)
					pdomBinding = tBinding;
				break;
			case CFIELD:
				if (binding instanceof IField)
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage#getBinding(int)
	 */
	public PDOMBinding getBinding(int record) throws CoreException {
		if (record == 0)
			return null;
		
		switch (PDOMBinding.getBindingType(pdom, record)) {
		case CVARIABLE:
			return new PDOMCVariable(pdom, record);
		case CFUNCTION:
			return new PDOMCFunction(pdom, record);
		case CSTRUCTURE:
			return new PDOMCStructure(pdom, record);
		case CFIELD:
			return new PDOMCField(pdom, record);
		}

		return null;
	}

}
