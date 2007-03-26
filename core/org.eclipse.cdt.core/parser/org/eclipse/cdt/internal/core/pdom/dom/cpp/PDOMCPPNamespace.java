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

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.LinkedList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.FindBindingsInBTree;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPNamespace extends PDOMBinding
		implements ICPPNamespace, ICPPNamespaceScope {

	private static final int INDEX_OFFSET = PDOMBinding.RECORD_SIZE + 0;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;

	private String[] qualifiedName;
	
	public PDOMCPPNamespace(PDOM pdom, PDOMNode parent, IASTName name) throws CoreException {
		super(pdom, parent, name);
	}

	public PDOMCPPNamespace(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPPNAMESPACE;
	}
	
	public BTree getIndex() throws CoreException {
		return new BTree(pdom.getDB(), record + INDEX_OFFSET);
	}

	public void accept(final IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		getIndex().accept(new IBTreeVisitor() {
			public int compare(int record) throws CoreException {
				return 1;
			};
			public boolean visit(int record) throws CoreException {
				PDOMBinding binding = pdom.getBinding(record);
				if (binding != null) {
					if (visitor.visit(binding))
						binding.accept(visitor);
					visitor.leave(binding);
				}
				return true;
			};
		});
	}
	
	public void addChild(PDOMNamedNode child) throws CoreException {
		getIndex().insert(child.getRecord(), child.getIndexComparator());
	}

	public String[] getQualifiedName() throws DOMException {
		if (qualifiedName == null) {
			LinkedList namelist = new LinkedList();
			namelist.addFirst(getName());
	
			try {
				PDOMNode parent = getParentNode();
				while (parent instanceof PDOMCPPNamespace) {
					namelist.addFirst(((PDOMCPPNamespace)parent).getName());
					parent = parent.getParentNode();
				}
			} catch (CoreException e) {
			}
		
			qualifiedName = (String[])namelist.toArray(new String[namelist.size()]);
		}
		
		return qualifiedName;
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IBinding[] getMemberBindings() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPNamespaceScope getNamespaceScope() throws DOMException {
		return this;
	}

	public void addUsingDirective(IASTNode directive) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IASTNode[] getUsingDirectives() throws DOMException {
		// TODO
		return new IASTNode[0];
	}

	public void addBinding(IBinding binding) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public void addName(IASTName name) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IBinding[] find(String name) throws DOMException {
		try {
			FindBindingsInBTree visitor = new FindBindingsInBTree(pdom, name.toCharArray());
			getIndex().accept(visitor);
			return visitor.getBinding();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IBinding[0];
		}
	}

	public void flushCache() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		try {
			if (name instanceof ICPPASTQualifiedName) {
				String[] myname = getQualifiedName();
				IASTName[] names = ((ICPPASTQualifiedName)name).getNames();
				if (myname.length != names.length - 1 || names.length < 2)
					return null;
				for (int i = 0; i < myname.length; ++i)
					if (!myname[i].equals(new String(names[i].toCharArray())))
						return null;
				name = names[names.length - 2]; // or myname.length - 1
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
					if (nsname != null && !equals(pdom.resolveBinding(nsname)))
						return null;
					
					// Look up the name
					FindBindingsInBTree visitor = new FindBindingsInBTree(pdom, name.toCharArray(),
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
					FindBindingsInBTree visitor = new FindBindingsInBTree(pdom, name.toCharArray(), PDOMCPPLinkage.CPPFUNCTION);
					getIndex().accept(visitor);
					IBinding[] bindings = visitor.getBinding();
					return bindings.length > 0 ? bindings[0] : null;
				} else {
					FindBindingsInBTree visitor = new FindBindingsInBTree(pdom, name.toCharArray(), 
							(name.getParent() instanceof ICPPASTQualifiedName
									&& ((ICPPASTQualifiedName)name.getParent()).getLastName() != name)
								? PDOMCPPLinkage.CPPNAMESPACE : PDOMCPPLinkage.CPPVARIABLE);
					getIndex().accept(visitor);
					IBinding[] bindings = visitor.getBinding();
					return bindings.length > 0 ? bindings[0] : null;
				}
			} else if (parent instanceof IASTNamedTypeSpecifier) {
				FindBindingsInBTree visitor = new FindBindingsInBTree(pdom, name.toCharArray(), PDOMCPPLinkage.CPPCLASSTYPE);
				getIndex().accept(visitor);
				IBinding[] bindings = visitor.getBinding();
				return bindings.length > 0 ? bindings[0] : null;
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

	public IASTNode getPhysicalNode() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IASTName getScopeName() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isFullyCached() throws DOMException {
		return true;
	}

	public void removeBinding(IBinding binding) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public void setFullyCached(boolean b) throws DOMException {
		throw new PDOMNotImplementedError();
	}
	
}
