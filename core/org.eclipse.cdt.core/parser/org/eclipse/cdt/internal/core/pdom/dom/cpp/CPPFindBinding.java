/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.dom.FindBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Look up bindings in BTree objects and IPDOMNode objects. This additionally
 * takes into account function/method parameters for overloading.
 */
public class CPPFindBinding extends FindBinding {
	public static PDOMBinding findBinding(BTree btree, final PDOM pdom, final char[]name, final int c2, final IType[] types) throws CoreException {
		final PDOMBinding[] result = new PDOMBinding[1];
		try {
			final int ty2 = PDOMCPPFunction.getSignatureMemento(types);
			btree.accept(new IBTreeVisitor() {
				public int compare(int record) throws CoreException {
					IString nm1 = PDOMNamedNode.getDBName(pdom, record);
					
					int cmp= nm1.compare(name, false);
					cmp= cmp==0 ? nm1.compare(name, true) : cmp;
					
					if(cmp==0) {
						int c1 = PDOMNode.getNodeType(pdom, record);
						cmp = c1 < c2 ? -1 : (c1 > c2 ? 1 : 0);
						if(cmp==0) {
							PDOMBinding binding = pdom.getBinding(record);
							if(binding instanceof PDOMCPPFunction) {
								int ty1 = ((PDOMCPPFunction)binding).getSignatureMemento();
								cmp = ty1 < ty2 ? -1 : (ty1 > ty2 ? 1 : 0);
							}
						}
					}
					return cmp;
				}
				public boolean visit(int record) throws CoreException {
					result[0] = pdom.getBinding(record);
					return false;
				}
			});
		} catch(DOMException de) {
			CCorePlugin.log(de);
		}
		return result[0];
	}


	public static PDOMBinding findBinding(PDOMNode node, final PDOM pdom, final char[]name, final int constant, final IType[] types) {
		final PDOMBinding[] result = new PDOMBinding[1];
		try {
			final int ty2 = PDOMCPPFunction.getSignatureMemento(types);
			node.accept(new IPDOMVisitor() {
				public boolean visit(IPDOMNode binding) throws CoreException {
					if(binding instanceof PDOMNamedNode) {
						PDOMNamedNode nnode = (PDOMNamedNode) binding;
						if(nnode.hasName(name)) {
							if(nnode.getNodeType() == constant) {
								if(binding instanceof PDOMCPPFunction) {
									int ty1 = ((PDOMCPPFunction)binding).getSignatureMemento();
									if(ty1==ty2) {
										result[0] = (PDOMBinding) binding;
										throw new CoreException(Status.OK_STATUS);
									}
								}
							}
						}
					}
					return false;
				}
				public void leave(IPDOMNode node) throws CoreException {}					
			});
		} catch(CoreException ce) {
			if(ce.getStatus().getCode()==IStatus.OK) {
				return result[0];
			} else {
				CCorePlugin.log(ce);
			}
		} catch(DOMException de) {
			CCorePlugin.log(de);
		}
		return null;
	}


	public static PDOMBinding findBinding(BTree btree, PDOMLinkage linkage, IBinding binding) throws CoreException {
		if(binding instanceof IFunction) {
			try {
				IFunctionType type = ((IFunction) binding).getType();
				return findBinding(btree, linkage.getPDOM(), binding.getNameCharArray(), linkage.getBindingType(binding), type.getParameterTypes());
			} catch(DOMException de) {
				CCorePlugin.log(de);
				return null;
			}
		}
		return findBinding(btree, linkage.getPDOM(), binding.getNameCharArray(), new int [] {linkage.getBindingType(binding)});
	}


	public static PDOMBinding findBinding(PDOMNode node, PDOMLinkage linkage, IBinding binding) {
		if(binding instanceof IFunction) {
			try {
				IFunctionType type = ((IFunction) binding).getType();
				return findBinding(node, linkage.getPDOM(), binding.getNameCharArray(), linkage.getBindingType(binding), type.getParameterTypes());
			} catch(DOMException de) {
				CCorePlugin.log(de);
				return null;
			}
		}
		return findBinding(node, linkage.getPDOM(), binding.getNameCharArray(), new int[] {linkage.getBindingType(binding)});
	}

	public static class CPPBindingBTreeComparator extends FindBinding.DefaultBindingBTreeComparator {
		public CPPBindingBTreeComparator(PDOM pdom) {
			super(pdom);
		}
		public int compare(int record1, int record2) throws CoreException {
			int cmp = super.compare(record1, record2);
			if(cmp==0) {
				PDOMBinding binding1 = pdom.getBinding(record1);
				PDOMBinding binding2 = pdom.getBinding(record2);
				if(binding1 instanceof PDOMCPPFunction && binding2 instanceof PDOMCPPFunction) {
					int ty1 = ((PDOMCPPFunction)binding1).getSignatureMemento();
					int ty2 = ((PDOMCPPFunction)binding2).getSignatureMemento();
					cmp = ty1 < ty2 ? -1 : (ty1 > ty2 ? 1 : 0);
				}
			}
			return cmp;
		}
	}
}
