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
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Look up bindings in BTree objects and IPDOMNode objects
 */
public class FindBinding {
	public static class DefaultBindingBTreeComparator implements IBTreeComparator {
		protected PDOM pdom;
		public DefaultBindingBTreeComparator(PDOM pdom) {
			this.pdom = pdom;
		}
		public int compare(int record1, int record2) throws CoreException {
			IString nm1 = PDOMNamedNode.getDBName(pdom, record1);
			IString nm2 = PDOMNamedNode.getDBName(pdom, record2);
			int cmp= nm1.compareCompatibleWithIgnoreCase(nm2);
			if(cmp == 0) {
				int t1= PDOMBinding.getLocalToFileRec(pdom, record1);
				int t2= PDOMBinding.getLocalToFileRec(pdom, record2);
				if (t1 == t2) {
					t1 = PDOMNode.getNodeType(pdom, record1);
					t2 = PDOMNode.getNodeType(pdom, record2);
				}
				cmp= t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
			}
			return cmp;
		}
	}

	public static class DefaultFindBindingVisitor implements IBTreeVisitor, IPDOMVisitor {
		protected final PDOM fPdom;
		private final char[] fName;
		private final int[] fConstants;
		private final int fLocalToFile;
		protected PDOMBinding fResult;
	
		protected DefaultFindBindingVisitor(PDOM pdom, char[] name, int[] constants, int localToFile) {
			fPdom = pdom;
			fName = name;
			fConstants = constants;
			fLocalToFile= localToFile;
		}
		// IBTreeVisitor
		public int compare(int record) throws CoreException {
			IString nm1 = PDOMNamedNode.getDBName(fPdom, record);
			int cmp= nm1.compareCompatibleWithIgnoreCase(fName); 
			if(cmp == 0) {
				int t1= PDOMBinding.getLocalToFileRec(fPdom, record);
				int t2= fLocalToFile;
				cmp= t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
			}
			return cmp;
		}
	
		// IBTreeVisitor
		public boolean visit(int record) throws CoreException {
			final PDOMNamedNode nnode = (PDOMNamedNode) PDOMNode.getLinkage(fPdom, record).getNode(record);
			if (nnode instanceof PDOMBinding) {
				final PDOMBinding binding = (PDOMBinding) nnode;
				if (matches(binding)) {
					fResult= binding;
					return false;
				}
			}
			return true;
		}
		
		protected boolean matches(PDOMBinding nnode) throws CoreException {
			if (nnode.hasName(fName)) {
				int constant = nnode.getNodeType();
				for(int i=0; i<fConstants.length; i++) {
					if(constant==fConstants[i]) {
						return true;
					}
				}
			}
			return false;
		}

		public PDOMBinding getResult() {
			return fResult;
		}
		// IPDOMVisitor
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof PDOMBinding) {
				final PDOMBinding nnode = (PDOMBinding) node;
				if (matches(nnode)) {
					fResult= nnode;
					throw new OperationCanceledException();
				}
			}
			return false; /* do not visit children of node */
		}
		// IPDOMVisitor
		public void leave(IPDOMNode node) throws CoreException {
		}
	}

	public static class NestedBindingsBTreeComparator extends DefaultBindingBTreeComparator implements IBTreeComparator {
		protected PDOMLinkage linkage;
		
		public NestedBindingsBTreeComparator(PDOMLinkage linkage) {
			super(linkage.pdom);
			this.linkage= linkage;
		}
		public int compare(int record1, int record2) throws CoreException {
			int cmp= super.compare(record1, record2);	// compare names
			if (cmp==0) {								// any order will do.
				if (record1 < record2) {
					return -1;
				}
				else if (record1 > record2) {			
					return 1;
				}
			}
			return cmp;
		}
	}

	public static class MacroBTreeComparator implements IBTreeComparator {
		final private PDOM fPDom;
		
		public MacroBTreeComparator(PDOM pdom) {
			fPDom= pdom;
		}
		public int compare(int record1, int record2) throws CoreException {
			int cmp= compare(PDOMMacro.getNameInDB(fPDom, record1), PDOMMacro.getNameInDB(fPDom, record2));	// compare names
			if (cmp==0) {								// any order will do.
				if (record1 < record2) {
					return -1;
				}
				else if (record1 > record2) {			
					return 1;
				}
			}
			return cmp;
		}
		private int compare(IString nameInDB, IString nameInDB2) throws CoreException {
			return nameInDB.compareCompatibleWithIgnoreCase(nameInDB2);
		}
	}

	public static PDOMBinding findBinding(BTree btree, final PDOM pdom, final char[]name, final int[] constants, 
			final int localToFileRec) throws CoreException {
		final DefaultFindBindingVisitor visitor = new DefaultFindBindingVisitor(pdom, name, constants, localToFileRec);
		btree.accept(visitor);
		return visitor.getResult();
	}


	public static PDOMBinding findBinding(IPDOMNode node, final PDOM pdom, final char[]name, final int[] constants,
			int localToFileRec) throws CoreException {
		final DefaultFindBindingVisitor visitor = new DefaultFindBindingVisitor(pdom, name, constants, localToFileRec);
		try {
			node.accept(visitor);
		} catch (OperationCanceledException e) {
		}
		return visitor.getResult();
	}
}

