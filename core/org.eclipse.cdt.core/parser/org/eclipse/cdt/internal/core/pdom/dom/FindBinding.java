/*******************************************************************************
 * Copyright (c) 2006, 2010 Symbian Software Systems and others.
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
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
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
		protected final PDOMLinkage linkage;
		protected final Database database;

		public DefaultBindingBTreeComparator(PDOMLinkage linkage) {
			this.linkage = linkage;
			this.database= linkage.getDB();
		}

		@Override
		public int compare(long record1, long record2) throws CoreException {
			IString nm1 = PDOMNamedNode.getDBName(database, record1);
			IString nm2 = PDOMNamedNode.getDBName(database, record2);
			int cmp= nm1.compareCompatibleWithIgnoreCase(nm2);
			if (cmp == 0) {
				long t1= PDOMBinding.getLocalToFileRec(database, record1);
				long t2= PDOMBinding.getLocalToFileRec(database, record2);
				if (t1 == t2) {
					t1 = PDOMNode.getNodeType(database, record1);
					t2 = PDOMNode.getNodeType(database, record2);
					if (t1 == t2 && t1 == IIndexBindingConstants.ENUMERATOR) {
						// Allow to insert multiple enumerators into the global index.
						t1= record1;
						t2= record2;
					}
				}
				cmp= t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
			}
			return cmp;
		}
	}

	public static class DefaultFindBindingVisitor implements IBTreeVisitor, IPDOMVisitor {
		protected final PDOMLinkage fLinkage;
		private final char[] fName;
		private final int[] fConstants;
		private final long fLocalToFile;
		protected PDOMBinding fResult;
	
		protected DefaultFindBindingVisitor(PDOMLinkage linkage, char[] name, int[] constants, long localToFile) {
			fLinkage = linkage;
			fName = name;
			fConstants = constants;
			fLocalToFile= localToFile;
		}
		
		// IBTreeVisitor
		@Override
		public int compare(long record) throws CoreException {
			final Database db = fLinkage.getDB();
			IString nm1 = PDOMNamedNode.getDBName(db, record);
			int cmp= nm1.compareCompatibleWithIgnoreCase(fName); 
			if (cmp == 0) {
				long t1= PDOMBinding.getLocalToFileRec(db, record);
				long t2= fLocalToFile;
				cmp= t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
			}
			return cmp;
		}
	
		// IBTreeVisitor
		@Override
		public boolean visit(long record) throws CoreException {
			final PDOMNamedNode nnode = (PDOMNamedNode) fLinkage.getNode(record);
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
				for (int c : fConstants) {
					if (constant == c) {
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
		@Override
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
		@Override
		public void leave(IPDOMNode node) throws CoreException {
		}
	}

	public static class NestedBindingsBTreeComparator extends DefaultBindingBTreeComparator {
		public NestedBindingsBTreeComparator(PDOMLinkage linkage) {
			super(linkage);
		}

		@Override
		public int compare(long record1, long record2) throws CoreException {
			int cmp= super.compare(record1, record2);	// compare names
			if (cmp == 0) {								// any order will do.
				if (record1 < record2) {
					return -1;
				} else if (record1 > record2) {			
					return 1;
				}
			}
			return cmp;
		}
	}

	public static class MacroBTreeComparator implements IBTreeComparator {
		final private Database db;
		
		public MacroBTreeComparator(Database database) {
			db= database;
		}
		@Override
		public int compare(long record1, long record2) throws CoreException {
			return compare(PDOMNamedNode.getDBName(db, record1), PDOMNamedNode.getDBName(db, record2));	// compare names
		}
		private int compare(IString nameInDB, IString nameInDB2) throws CoreException {
			return nameInDB.compareCompatibleWithIgnoreCase(nameInDB2);
		}
	}

	public static PDOMBinding findBinding(BTree btree, final PDOMLinkage linkage, final char[] name, 
			final int[] constants, final long localToFileRec) throws CoreException {
		final DefaultFindBindingVisitor visitor = new DefaultFindBindingVisitor(linkage, name, constants, localToFileRec);
		btree.accept(visitor);
		return visitor.getResult();
	}

	public static PDOMBinding findBinding(IPDOMNode node, final PDOMLinkage linkage, final char[] name, final int[] constants,
			long localToFileRec) throws CoreException {
		final DefaultFindBindingVisitor visitor = new DefaultFindBindingVisitor(linkage, name, constants, localToFileRec);
		try {
			node.accept(visitor);
		} catch (OperationCanceledException e) {
		}
		return visitor.getResult();
	}
}

