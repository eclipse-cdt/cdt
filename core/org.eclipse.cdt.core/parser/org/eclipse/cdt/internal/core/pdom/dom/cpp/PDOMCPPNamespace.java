/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.dom.bid.ILocalBindingIdentity;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.FindBindingsInBTree;
import org.eclipse.cdt.internal.core.pdom.dom.FindEquivalentBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPNamespace extends PDOMCPPBinding
		implements ICPPNamespace, ICPPNamespaceScope {

	private static final int INDEX_OFFSET = PDOMBinding.RECORD_SIZE + 0;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;

	public PDOMCPPNamespace(PDOM pdom, PDOMNode parent, IASTName name) throws CoreException {
		super(pdom, parent, name);
	}

	public PDOMCPPNamespace(PDOM pdom, int record) throws CoreException {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPPNAMESPACE;
	}
	
	public BTree getIndex() throws CoreException {
		return new BTree(pdom.getDB(), record + INDEX_OFFSET, getLinkageImpl().getIndexComparator());
	}

	public void accept(final IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		getIndex().accept(new IBTreeVisitor() {
			public int compare(int record) throws CoreException {
				return 1;
			}
			public boolean visit(int record) throws CoreException {
				PDOMBinding binding = pdom.getBinding(record);
				if (binding != null) {
					if (visitor.visit(binding))
						binding.accept(visitor);
					visitor.leave(binding);
				}
				return true;
			}
		});
	}
	
	public void addChild(PDOMNode child) throws CoreException {
		getIndex().insert(child.getRecord());
	}

	public ICPPNamespaceScope getNamespaceScope() throws DOMException {
		return this;
	}

	public IASTNode[] getUsingDirectives() throws DOMException {
		// TODO
		return new IASTNode[0];
	}

	// mstodo this method currently does not get called, we could try to remove it.
	// an alternative an appropriate method in  CPPSemantics. This implementation is not
	// correct for sure.
	public IBinding[] find(String name) throws DOMException {
		try {
			FindBindingsInBTree visitor = new FindBindingsInBTree(getLinkageImpl(), name.toCharArray());
			getIndex().accept(visitor);
			return visitor.getBinding();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IBinding[0];
		}
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		try {
			if (name instanceof ICPPASTQualifiedName) {
				IASTName lastName = ((ICPPASTQualifiedName)name).getLastName();
				return lastName != null ? lastName.resolveBinding() : null;
			}
			IASTNode parent = name.getParent();
			if (parent instanceof ICPPASTQualifiedName) {
				IASTName[] names = ((ICPPASTQualifiedName)parent).getNames();
				if (name == names[names.length - 1]) {
					parent = parent.getParent();
				} else {
					IASTName nsname = null;
					for (int i = 0; i < names.length - 2; ++i) {
						if (name != names[i])
							nsname = names[i];
					}
					// make sure we're the namespace they're talking about
					if (nsname != null && !equals(pdom.findBinding(nsname)))
						return null;
					
					// Look up the name
					FindBindingsInBTree visitor = new FindBindingsInBTree(getLinkageImpl(), name.toCharArray(),
							new int[] {
								PDOMCPPLinkage.CPPCLASSTYPE,
								PDOMCPPLinkage.CPPNAMESPACE,
								PDOMCPPLinkage.CPPFUNCTION,
								PDOMCPPLinkage.CPPVARIABLE
							});
					getIndex().accept(visitor);
					IBinding[] bindings = visitor.getBinding();
					return bindings.length > 0 ? bindings[0] : null;
				}
			}
			if (parent instanceof IASTIdExpression) {
				// reference
				IASTNode eParent = parent.getParent();
				if (eParent instanceof IASTFunctionCallExpression) {
					IType[] types = ((PDOMCPPLinkage)getLinkage()).getTypes(
							((IASTFunctionCallExpression)eParent).getParameterExpression()
							);
					ILocalBindingIdentity bid = new CPPBindingIdentity.Holder(
						new String(name.toCharArray()),
						PDOMCPPLinkage.CPPFUNCTION,
						types);
					FindEquivalentBinding feb = new FindEquivalentBinding(getLinkageImpl(), bid);
					getIndex().accept(feb);
					return feb.getResult();
				} else {
					FindBindingsInBTree visitor = new FindBindingsInBTree(getLinkageImpl(), name.toCharArray(), 
							(name.getParent() instanceof ICPPASTQualifiedName
									&& ((ICPPASTQualifiedName)name.getParent()).getLastName() != name)
								? PDOMCPPLinkage.CPPNAMESPACE : PDOMCPPLinkage.CPPVARIABLE);
					getIndex().accept(visitor);
					IBinding[] bindings = visitor.getBinding();
					return bindings.length > 0
								? bindings[0]
					           : null;
				}
			} else if (parent instanceof IASTNamedTypeSpecifier) {
				FindBindingsInBTree visitor = new FindBindingsInBTree(getLinkageImpl(), name.toCharArray(), PDOMCPPLinkage.CPPCLASSTYPE);
				getIndex().accept(visitor);
				IBinding[] bindings = visitor.getBinding();
				return bindings.length > 0
					? bindings[0]
					: null;
			}
			return null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IScope getParent() throws DOMException {
		// TODO
		return null;
	}

	public boolean isFullyCached() throws DOMException {
		return true;
	}

	public boolean mayHaveChildren() {
		return true;
	}

	public IBinding[] getMemberBindings() throws DOMException {fail(); return null;}
	public IName getScopeName() throws DOMException {fail(); return null;}
	public void addUsingDirective(IASTNode directive) throws DOMException {fail();}
}
