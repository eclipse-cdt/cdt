/*******************************************************************************
 * Copyright (c) 2006, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.dom.FindBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Look up bindings in BTree objects and IPDOMNode objects. This additionally
 * takes into account function/method parameters as well as template
 * specialization arguments for overloading.
 */
public class CPPFindBinding extends FindBinding {
	
	public static class CPPBindingBTreeComparator extends FindBinding.DefaultBindingBTreeComparator {
		public CPPBindingBTreeComparator(PDOM pdom) {
			super(pdom);
		}
		@Override
		public int compare(int record1, int record2) throws CoreException {
			int cmp = super.compare(record1, record2);
			if (cmp == 0) {
				PDOMBinding binding1 = pdom.getBinding(record1);
				PDOMBinding binding2 = pdom.getBinding(record2);
				if (binding1 instanceof IPDOMOverloader && binding2 instanceof IPDOMOverloader) {
					int ty1 = ((IPDOMOverloader) binding1).getSignatureHash();
					int ty2 = ((IPDOMOverloader) binding2).getSignatureHash();
					cmp = ty1 < ty2 ? -1 : (ty1 > ty2 ? 1 : 0);
				}
			}
			return cmp;
		}
	}

	public static class CPPFindBindingVisitor extends FindBinding.DefaultFindBindingVisitor {
		private final int fConstant;
		private final int fSigHash;
		public CPPFindBindingVisitor(PDOM pdom, char[] name, int constant, int hash, int localToFile) {
			super(pdom, name, new int[] {constant}, localToFile);
			fConstant= constant;
			fSigHash= hash;
		}
		
		@Override
		public int compare(int record) throws CoreException {
			int cmp= super.compare(record);
			if (cmp == 0) {
				int c1 = PDOMNode.getNodeType(fPdom, record);
				int c2= fConstant;
				if (c1 == c2) {
					PDOMBinding binding = fPdom.getBinding(record);
					if (binding instanceof IPDOMOverloader) {
						c1 = ((IPDOMOverloader) binding).getSignatureHash();
						c2= fSigHash;
					}
				}
				cmp = c1 < c2 ? -1 : (c1 > c2 ? 1 : 0);
			}
			return cmp;
		}

		@Override
		public boolean visit(int record) throws CoreException {
			fResult= fPdom.getBinding(record);
			return false;
		}

		@Override
		protected boolean matches(PDOMBinding binding) throws CoreException {
			if (super.matches(binding)) {
				if (binding instanceof IPDOMOverloader) {
					int ty1 = ((IPDOMOverloader) binding).getSignatureHash();
					return fSigHash == ty1;
				}
			}
			return false;
		}
	}

	public static PDOMBinding findBinding(BTree btree, final PDOM pdom, final char[] name,
			final int c2, final int ty2, int localToFileRec) throws CoreException {
		CPPFindBindingVisitor visitor= new CPPFindBindingVisitor(pdom, name, c2, ty2, localToFileRec);
		btree.accept(visitor);
		return visitor.getResult();
	}

	public static PDOMBinding findBinding(PDOMNode node, PDOM pdom, char[] name, int constant,
			int sigHash, int localToFileRec) throws CoreException {
		CPPFindBindingVisitor visitor= new CPPFindBindingVisitor(pdom, name, constant, sigHash,
				localToFileRec);
		try {
			node.accept(visitor);
		} catch (OperationCanceledException e) {
		}
		return visitor.getResult();
	}

	public static PDOMBinding findBinding(BTree btree, PDOMLinkage linkage, IBinding binding,
			int localToFileRec) throws CoreException {	
		Integer hash= 0;
		try {
			hash = IndexCPPSignatureUtil.getSignatureHash(binding);
		} catch (DOMException e) {
		}
		if (hash != null) {
			return findBinding(btree, linkage.getPDOM(), binding.getNameCharArray(),
					linkage.getBindingType(binding), hash.intValue(), localToFileRec);
		}
		return findBinding(btree, linkage.getPDOM(), binding.getNameCharArray(),
				new int[] {linkage.getBindingType(binding)}, localToFileRec);
	}

	public static PDOMBinding findBinding(PDOMNode node, PDOMLinkage linkage, IBinding binding,
			int localToFileRec) throws CoreException {
		Integer hash = null;
		try {
			hash = IndexCPPSignatureUtil.getSignatureHash(binding);
		} catch (DOMException e) {
		}
		if (hash != null) {
			return findBinding(node, linkage.getPDOM(), binding.getNameCharArray(),
					linkage.getBindingType(binding), hash.intValue(), localToFileRec);
		}
		return findBinding(node, linkage.getPDOM(), binding.getNameCharArray(),
				new int[] {linkage.getBindingType(binding)}, localToFileRec);
	}
}
