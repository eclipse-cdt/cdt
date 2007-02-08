/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
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
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMBinding extends PDOMNamedNode implements IIndexFragmentBinding {

	private static final int FIRST_DECL_OFFSET   = PDOMNamedNode.RECORD_SIZE +  0; // size 4
	private static final int FIRST_DEF_OFFSET    = PDOMNamedNode.RECORD_SIZE + 4; // size 4
	private static final int FIRST_REF_OFFSET    = PDOMNamedNode.RECORD_SIZE + 8; // size 4
	
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 12;
	
	protected PDOMBinding(PDOM pdom, PDOMNode parent, char[] name) throws CoreException {
		super(pdom, parent, name);
	}
	
	public PDOMBinding(PDOM pdom, int record) {
		super(pdom, record);
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == PDOMBinding.class)
			return this;
		else
			return null;
	}
	
	/**
	 * Is the binding as the record orphaned, i.e., has no declarations
	 * or references.
	 * 
	 * @param pdom
	 * @param record
	 * @return
	 * @throws CoreException
	 */
	public static boolean isOrphaned(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		return db.getInt(record + FIRST_DECL_OFFSET) == 0
			&& db.getInt(record + FIRST_DEF_OFFSET) == 0
			&& db.getInt(record + FIRST_REF_OFFSET) == 0;
	}
	
	public int getRecord() {
		return record;
	}

	public boolean hasDeclarations() throws CoreException {
		Database db = pdom.getDB();
		return db.getInt(record + FIRST_DECL_OFFSET) != 0
			|| db.getInt(record + FIRST_DEF_OFFSET) != 0;
	}
	
	public void addDeclaration(PDOMName name) throws CoreException {
		PDOMName first = getFirstDeclaration();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstDeclaration(name);
	}
	
	public void addDefinition(PDOMName name) throws CoreException {
		PDOMName first = getFirstDefinition();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstDefinition(name);
	}
	
	public void addReference(PDOMName name) throws CoreException {
		PDOMName first = getFirstReference();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstReference(name);
	}
	
	public PDOMName getFirstDeclaration() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_DECL_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstDeclaration(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_DECL_OFFSET, namerec);
	}
	
	public PDOMName getFirstDefinition() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_DEF_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstDefinition(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_DEF_OFFSET, namerec);
	}
	
	public PDOMName getFirstReference() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_REF_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstReference(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_REF_OFFSET, namerec);
	}
	
	public String getName() {
		try {
			return super.getDBName().getString();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return ""; //$NON-NLS-1$
	}

	public char[] getNameCharArray() {
		try {
			return super.getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new char[0];
	}
	
	public IScope getParent() throws DOMException {
		try {
			IBinding parent = getParentBinding();
			if(parent instanceof IScope) {
				return (IScope) parent;
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		return null;
	}
	
	public final IScope getScope() throws DOMException {
		try {
			IBinding parent = getParentBinding(); 
			if(parent instanceof IScope) {
				return (IScope) parent;
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		return null;
	}
	
	public IIndexBinding getParentBinding() throws CoreException {
		PDOMNode parent= getParentNode();
		if (parent instanceof IIndexBinding) {
			return (IIndexBinding) parent;
		}
		return null;
	}
	
	public IIndexFragment getFragment() {
		return pdom;
	}

	abstract protected int getRecordSize(); // superclass's implementation is no longer valid
	
	public String toString() {
		return getName() + " " + getNodeType();  //$NON-NLS-1$
	}
	
	/**
     * Convenience method to shorten subclass file length
     */
	protected final void fail() { throw new PDOMNotImplementedError(); }

	public boolean mayHaveChildren() {
		return false;
	}
	
	public IName getScopeName() throws DOMException {
		try {
			PDOMName name = getFirstDefinition();
			if (name == null)
				name = getFirstDeclaration();
			return name;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	final public String[] getQualifiedName() {
		List result = new ArrayList();
		try {
			PDOMNode node = this;
			while (node != null) {
				if (node instanceof PDOMBinding) {							
					result.add(0, ((PDOMBinding)node).getName());
				}
				node = node.getParentNode();
			}
			return (String[]) result.toArray(new String[result.size()]);
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			return null;
		}
	}
	
	final public boolean isFileLocal() throws CoreException {
		return getParentNode() instanceof PDOMFileLocalScope;
	}
}
