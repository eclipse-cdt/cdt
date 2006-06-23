/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * This class represents a collection of symbols that can be linked together at
 * link time. These are generally global symbols specific to a given language.
 */
public abstract class PDOMLinkage extends PDOMNamedNode {

	protected static final class MatchBinding implements IBTreeVisitor {
			private final PDOM pdom;
			private final List bindings;
			private final Pattern pattern;
			
			public MatchBinding(PDOM pdom, Pattern pattern, List bindings) {
				this.pdom = pdom;
				this.bindings = bindings;
				this.pattern = pattern;
			}
			
			public boolean visit(int record) throws CoreException {
				if (record == 0)
					return true;
				
				// TODO of course do something smarter here
				PDOMBinding binding = pdom.getBinding(record);
				if (binding != null) {
					Matcher matcher = pattern.matcher(binding.getName());
					if (matcher.matches())
						bindings.add(binding);
				}
				return true;
			}
			public int compare(int record) throws CoreException {
				return 1;
			}
		}

	// record offsets
	private static final int ID_OFFSET   = PDOMNamedNode.RECORD_SIZE + 0;
	private static final int NEXT_OFFSET = PDOMNamedNode.RECORD_SIZE + 4;
	private static final int INDEX_OFFSET = PDOMNamedNode.RECORD_SIZE + 8;
	
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 12;
	
	// node types
	protected static final int LINKAGE = 0; // special one for myself
	static final int POINTER_TYPE = 1;
	static final int QUALIFIER_TYPE = 2;
	
	protected static final int LAST_NODE_TYPE = QUALIFIER_TYPE;
	
	public PDOMLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected PDOMLinkage(PDOM pdom, String languageId, char[] name) throws CoreException {
		super(pdom, null, name);
		Database db = pdom.getDB();

		// id
		db.putInt(record + ID_OFFSET, db.newString(languageId).getRecord());
		
		pdom.insertLinkage(this);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public static IString getId(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		int namerec = db.getInt(record + ID_OFFSET);
		return db.getString(namerec);
	}
	
	public abstract ILanguage getLanguage();

	public static int getNextLinkageRecord(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + NEXT_OFFSET);
	}
	
	public PDOMLinkage getNextLinkage() throws CoreException {
		return pdom.getLinkage(pdom.getDB().getInt(record + NEXT_OFFSET));
	}
	
	public void setNext(int nextrec) throws CoreException {
		pdom.getDB().putInt(record + NEXT_OFFSET, nextrec);
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
				}
				return true;
			};
		});
	}
	
	public PDOMLinkage getLinkage() throws CoreException {
		return this;
	}

	protected void addChild(PDOMNamedNode child) throws CoreException {
		getIndex().insert(child.getRecord(), child.getIndexComparator());
	}
	
	public PDOMNode getNode(int record) throws CoreException {
		switch (PDOMNode.getNodeType(pdom, record)) {
		case POINTER_TYPE:
			return new PDOMPointerType(pdom, record);
		case QUALIFIER_TYPE:
			return new PDOMQualifierType(pdom, record);
		}
		return null;
	}
	
	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof IPointerType)
			return new PDOMPointerType(pdom, parent, (IPointerType)type);
		else if (type instanceof IQualifierType)
			return new PDOMQualifierType(pdom, parent, (IQualifierType)type);
		else
			return null;
	}

	public abstract PDOMBinding addName(IASTName name, PDOMFile file) throws CoreException;
	
	public abstract PDOMBinding adaptBinding(IBinding binding) throws CoreException;
	
	public abstract IBinding resolveBinding(IASTName name) throws CoreException;

	public abstract void findBindings(Pattern pattern, List bindings) throws CoreException;
	
}
