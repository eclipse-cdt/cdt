/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IScope.ScopeLookupData;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.tag.ITagReader;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBindingComparator;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.db.PDOMExternalReferencesList;
import org.eclipse.cdt.internal.core.pdom.tag.PDOMTaggable;
import org.eclipse.core.runtime.CoreException;

/**
 * Base class for bindings in the PDOM.
 */
public abstract class PDOMBinding extends PDOMNamedNode implements IPDOMBinding {
	public static final PDOMBinding[] EMPTY_PDOMBINDING_ARRAY = {};

	private static final int FIRST_DECL = PDOMNamedNode.RECORD_SIZE; // size 4
	private static final int FIRST_DEF = FIRST_DECL + Database.PTR_SIZE; // size 4
	private static final int FIRST_REF = FIRST_DEF + Database.PTR_SIZE; // size 4
	private static final int LOCAL_TO_FILE = FIRST_REF + Database.PTR_SIZE; // size 4
	private static final int FIRST_EXTREF = LOCAL_TO_FILE + Database.PTR_SIZE; // size 4

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FIRST_EXTREF + +Database.PTR_SIZE;

	private byte hasDeclaration = -1;

	protected PDOMBinding(PDOMLinkage linkage, PDOMNode parent, char[] name) throws CoreException {
		super(linkage, parent, name);
	}

	public PDOMBinding(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(PDOMBinding.class))
			return (T) this;

		// Any PDOMBinding can have a persistent tag.  These tags should be deleted when
		// the PDOMBinding is deleted.  However, PDOMBinding's don't get deleted, so there is no way
		// to trigger deleting of the tags.  If the implementation is changed so that PDOMBindings
		// do get deleted, then it should call:
		// PDOMTagIndex.setTags(getPDOM(), pdomBinding.record, Collections.<ITag>emptyList());
		// to clear out all tags for the binding.
		if (adapter.isAssignableFrom(ITagReader.class))
			return (T) new PDOMTaggable(getPDOM(), getRecord());

		return null;
	}

	/**
	 * Is the binding as the record orphaned, i.e., has no declarations
	 * or references.
	 * Watch out, a binding may also be used in a type (e.g. pointer to class)
	 *
	 * @param pdom
	 * @param record
	 * @return {@code true} if the binding is orphaned.
	 * @throws CoreException
	 */
	public static boolean isOrphaned(PDOM pdom, long record) throws CoreException {
		Database db = pdom.getDB();
		return db.getRecPtr(record + FIRST_DECL) == 0 && db.getRecPtr(record + FIRST_DEF) == 0
				&& db.getRecPtr(record + FIRST_REF) == 0 && db.getRecPtr(record + FIRST_EXTREF) == 0;
	}

	@Override
	public final boolean hasDeclaration() throws CoreException {
		if (hasDeclaration == -1) {
			final Database db = getDB();
			if (db.getRecPtr(record + FIRST_DECL) != 0 || db.getRecPtr(record + FIRST_DEF) != 0) {
				hasDeclaration = 1;
				return true;
			}
			hasDeclaration = 0;
			return false;
		}
		return hasDeclaration != 0;
	}

	public final void addDeclaration(PDOMName name) throws CoreException {
		PDOMName first = getFirstDeclaration();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstDeclaration(name);
	}

	public final void addDefinition(PDOMName name) throws CoreException {
		PDOMName first = getFirstDefinition();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstDefinition(name);
	}

	public final void addReference(PDOMName name) throws CoreException {
		// This needs to filter between the local and external lists because it can be used in
		// contexts that don't know which type of list they are iterating over.  E.g., this is
		// used when deleting names from a PDOMFile.
		if (!getLinkage().equals(name.getLinkage())) {
			new PDOMExternalReferencesList(getPDOM(), record + FIRST_EXTREF).add(name);
			return;
		}

		PDOMName first = getFirstReference();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstReference(name);
	}

	public PDOMName getFirstDeclaration() throws CoreException {
		long namerec = getDB().getRecPtr(record + FIRST_DECL);
		return namerec != 0 ? new PDOMName(getLinkage(), namerec) : null;
	}

	public void setFirstDeclaration(PDOMName name) throws CoreException {
		long namerec = name != null ? name.getRecord() : 0;
		getDB().putRecPtr(record + FIRST_DECL, namerec);
	}

	public PDOMName getFirstDefinition() throws CoreException {
		long namerec = getDB().getRecPtr(record + FIRST_DEF);
		return namerec != 0 ? new PDOMName(getLinkage(), namerec) : null;
	}

	public void setFirstDefinition(PDOMName name) throws CoreException {
		long namerec = name != null ? name.getRecord() : 0;
		getDB().putRecPtr(record + FIRST_DEF, namerec);
	}

	public PDOMName getFirstReference() throws CoreException {
		long namerec = getDB().getRecPtr(record + FIRST_REF);
		return namerec != 0 ? new PDOMName(getLinkage(), namerec) : null;
	}

	public IRecordIterator getDeclarationRecordIterator() throws CoreException {
		Database db = getDB();
		return PDOMName.getNameInBindingRecordIterator(db, db.getRecPtr(record + FIRST_DECL));
	}

	public IRecordIterator getDefinitionRecordIterator() throws CoreException {
		Database db = getDB();
		return PDOMName.getNameInBindingRecordIterator(db, db.getRecPtr(record + FIRST_DEF));
	}

	/**
	 * Returns an iterator over the names in other linkages that reference this binding.  Does
	 * not return null.
	 */
	public IPDOMIterator<PDOMName> getExternalReferences() throws CoreException {
		return new PDOMExternalReferencesList(getPDOM(), record + FIRST_EXTREF).getIterator();
	}

	/**
	 * In most cases the linkage can be found from the linkage of the name.  However, when the
	 * list is being cleared (there is no next), the linkage must be passed in.
	 */
	public void setFirstReference(PDOMLinkage linkage, PDOMName name) throws CoreException {
		if (linkage.equals(getLinkage())) {
			setFirstReference(name);
		} else {
			new PDOMExternalReferencesList(getPDOM(), record + FIRST_EXTREF).setFirstReference(linkage, name);
		}
	}

	private void setFirstReference(PDOMName name) throws CoreException {
		// This needs to filter between the local and external lists because it can be used in
		// contexts that don't know which type of list they are iterating over.  E.g., this is
		// used when deleting names from a PDOMFile.
		if (name != null && !getLinkage().equals(name.getLinkage())) {
			new PDOMExternalReferencesList(getPDOM(), record + FIRST_EXTREF).add(name);
			return;
		}

		// Otherwise put the reference into list of locals.
		long namerec = name != null ? name.getRecord() : 0;
		getDB().putRecPtr(record + FIRST_REF, namerec);
	}

	@Override
	public final PDOMFile getLocalToFile() throws CoreException {
		final long filerec = getLocalToFileRec(getDB(), record);
		return filerec == 0 ? null : new PDOMFile(getLinkage(), filerec);
	}

	public final long getLocalToFileRec() throws CoreException {
		return getLocalToFileRec(getDB(), record);
	}

	public static long getLocalToFileRec(Database db, long record) throws CoreException {
		return db.getRecPtr(record + LOCAL_TO_FILE);
	}

	public final void setLocalToFileRec(long rec) throws CoreException {
		getDB().putRecPtr(record + LOCAL_TO_FILE, rec);
	}

	@Override
	public String getName() {
		try {
			return super.getDBName().getString();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public char[] getNameCharArray() {
		try {
			return super.getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return CharArrayUtils.EMPTY;
	}

	public IIndexScope getParent() {
		try {
			IBinding parent = getParentBinding();
			if (parent instanceof IIndexScope) {
				return (IIndexScope) parent;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return getLinkage().getGlobalScope();
	}

	@Override
	public IIndexScope getScope() {
		// The parent node in the binding hierarchy is the scope.
		try {
			IBinding parent = getParentBinding();
			if (parent instanceof IIndexScope) {
				return (IIndexScope) parent;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return getLinkage().getGlobalScope();
	}

	@Override
	public IIndexFragment getFragment() {
		return getPDOM();
	}

	@Override
	abstract protected int getRecordSize(); // Superclass's implementation is no longer valid

	/** For debug purposes only. */
	@Override
	public final String toString() {
		String name = toStringBase();
		try {
			PDOMFile localToFile = getLocalToFile();
			if (localToFile != null)
				return name + " (local to " + localToFile.getLocation().getURI().getPath() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CoreException e) {
		}
		return name;
	}

	protected String toStringBase() {
		if (this instanceof IType) {
			return ASTTypeUtil.getType((IType) this);
		} else if (this instanceof IFunction) {
			IFunctionType t = null;
			t = ((IFunction) this).getType();
			if (t != null) {
				return getName() + ASTTypeUtil.getParameterTypeString(t);
			} else {
				return getName() + "()"; //$NON-NLS-1$
			}
		}
		return getName();
	}

	/**
	 * For debug purposes only.
	 * @param linkage
	 * @param value
	 * @return String representation of {@code value}.
	 */
	protected static String getConstantNameForValue(PDOMLinkage linkage, int value) {
		Class<? extends PDOMLinkage> c = linkage.getClass();
		Field[] fields = c.getFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				if ((field.getModifiers() & Modifier.STATIC) != 0) {
					if (int.class.equals(field.getType())) {
						int fvalue = field.getInt(null);
						if (fvalue == value)
							return field.getName();
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException e) {
				continue;
			}
		}
		return Integer.toString(value);
	}

	public PDOMName getScopeName() {
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

	@Override
	public String[] getQualifiedName() {
		return new String[] { getName() };
	}

	@Override
	public final boolean isFileLocal() throws CoreException {
		return getDB().getRecPtr(record + LOCAL_TO_FILE) != 0;
	}

	@Override
	public boolean hasDefinition() throws CoreException {
		return getDB().getRecPtr(record + FIRST_DEF) != 0;
	}

	/**
	 * Compares two binding fully qualified names. If b0 has
	 * less segments than b1 then -1 is returned, if b0 has
	 * more segments than b1 then 1 is returned. If the segment
	 * lengths are equal then comparison is lexicographical on each
	 * component name, beginning with the most nested name and working
	 * outward.
	 * If one of the bindings in the hierarchy is file-local it is treated as a different
	 * binding.
	 * The first non-zero comparison is returned as the result.
	 * @param b0
	 * @param b1
	 * @return <ul><li> -1 if b0 &lt; b1
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
				if (cmp == 0) {
					long l1 = b0.getLocalToFileRec();
					long l2 = b1.getLocalToFileRec();
					if (l1 != l2) {
						return l1 < l2 ? -1 : 1;
					}
					b0 = (PDOMBinding) b0.getParentBinding();
					b1 = (PDOMBinding) b1.getParentBinding();
					if (b0 == null || b1 == null) {
						cmp = b0 == b1 ? 0 : (b0 == null ? -1 : 1);
					}
				}
			} while (cmp == 0 && b1 != null && b0 != null);
			return cmp;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return -1;
		}
	}

	/**
	 * Compares two PDOMBinding objects in accordance with
	 * {@link IIndexFragmentBindingComparator#compare(IIndexFragmentBinding, IIndexFragmentBinding)}
	 * @param other
	 * @return comparison result, -1, 0, or 1.
	 */
	public int pdomCompareTo(PDOMBinding other) {
		int cmp = comparePDOMBindingQNs(this, other);
		if (cmp == 0) {
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
		return pdomCompareTo(other) == 0;
	}

	@Override
	public final int getBindingConstant() {
		return getNodeType();
	}

	/**
	 * The binding is reused by a declaration or definition, update the binding, e.g. modifiers,
	 * with the new information.
	 */
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
	}

	@Override
	public final void delete(PDOMLinkage linkage) throws CoreException {
		assert false;
	}

	/**
	 * Bindings may set additional flags for their occurrences
	 * Return a combination of flags defined in {@link PDOMName}.
	 * @since 5.0
	 */
	public int getAdditionalNameFlags(int standardFlags, IASTName name) {
		return 0;
	}

	public final IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, null);
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		return null;
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return getBindings(new ScopeLookupData(name, resolve, prefix));
	}

	public IBinding[] getBindings(ScopeLookupData lookup) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}
}
