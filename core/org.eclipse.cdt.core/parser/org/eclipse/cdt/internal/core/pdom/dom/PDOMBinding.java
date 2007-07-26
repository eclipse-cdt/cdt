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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBindingComparator;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
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
		if (adapter.isAssignableFrom(PDOMBinding.class))
			return this;

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

	public boolean hasDeclaration() throws CoreException {
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
	
	public final IScope getScope() {
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
				if (node instanceof PDOMBinding && !(node instanceof ICPPTemplateInstance)) {							
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
	

	public boolean hasDefinition() throws CoreException {
		return getFirstDefinition()!=null;
	}

	/**
	 * Compares two binding fully qualified names. If b0 has
     * less segments than b1 then -1 is returned, if b0 has 
     * more segments than b1 then 1 is returned. If the segment
     * lengths are equal then comparison is lexographical on each
     * component name, beginning with the most nested name and working
     * outward. The first non-zero comparison is returned as the result.
	 * @param b0
	 * @param b1
	 * @return<ul><li> -1 if b0 &lt; b1
	 * <li> 0 if b0 == b1
	 * <li> 1 if b0 &gt; b1
	 * </ul>
	 * @throws CoreException
	 */
	private static int comparePDOMBindingQNs(PDOMBinding b0, PDOMBinding b1) {
		try {
			int cmp = 0; 
			do {
				IString s0 = b0.getDBName(), s1 = b1.getDBName();
				cmp = s0.compare(s1, true);
				if(cmp==0) {
					b0 = (PDOMBinding) b0.getParentBinding();
					b1 = (PDOMBinding) b1.getParentBinding();
					if(b0==null || b1==null) {
						cmp = b0==b1 ? 0 : (b0==null ? -1 : 1);
					}
				}
			} while(cmp==0 && b1!=null && b0!=null);
			return cmp;
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			return -1;
		}
	}

	/**
	 * Compares two PDOMBinding objects in accordance with 
	 * {@link IIndexFragmentBindingComparator#compare(IIndexFragmentBinding, IIndexFragmentBinding)}
	 * @param other
	 * @return
	 */
	public int pdomCompareTo(PDOMBinding other) {
		int cmp = comparePDOMBindingQNs(this, other);
		if(cmp==0) {
			int t1 = getNodeType();
			int t2 = other.getNodeType();
			return t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
		}
		return cmp;
	}
	
	/**
     * Returns whether pdomCompareTo returns zero
     */
	public final boolean pdomEquals(PDOMBinding other) {
		return pdomCompareTo(other)==0;
	}
	
	public final int getBindingConstant() {
		return getNodeType();
	}

	/**
	 * The binding is reused by a declaration or definition, we may need to update modifiers.
	 * @throws CoreException 
	 */
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
	}
	
	final public void delete(PDOMLinkage linkage) throws CoreException {
		assert false;
	}
}