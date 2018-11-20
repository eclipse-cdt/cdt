/*******************************************************************************
 * Copyright (c) 2005, 2014 QNX Software Systems and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public final class PDOMName implements IIndexFragmentName {
	private final PDOMLinkage linkage;
	private final long record;

	private static final int FILE_REC_OFFSET = 0;
	private static final int FILE_NEXT_OFFSET = 4;
	private static final int CALLER_REC_OFFSET = 8;
	private static final int BINDING_REC_OFFSET = 12;
	private static final int BINDING_PREV_OFFSET = 16;
	private static final int BINDING_NEXT_OFFSET = 20;
	private static final int NODE_OFFSET_OFFSET = 24; // 3-byte unsigned int (sufficient for files <= 16mb)
	private static final int NODE_LENGTH_OFFSET = 27; // short (sufficient for names <= 32k)
	private static final int FLAGS = 29;

	private static final int RECORD_SIZE = 30; // 30 yields a 32-byte block. (31 would trigger a 40-byte block)

	public static final int IS_DECLARATION = 0x01;
	public static final int IS_DEFINITION = 0x02;
	public static final int IS_REFERENCE = IS_DECLARATION | IS_DEFINITION;
	public static final int DECL_DEF_REF_MASK = IS_DECLARATION | IS_DEFINITION | IS_REFERENCE;
	public static final int INHERIT_FRIEND_INLINE_MASK = 0x0C;
	public static final int IS_INHERITANCE_SPEC = 0x04;
	public static final int IS_FRIEND_SPEC = 0x08;
	public static final int IS_INLINE_NAMESPACE = 0x0C;
	public static final int COULD_BE_POLYMORPHIC_METHOD_CALL = 0x10;
	public static final int READ_ACCESS = 0x20;
	public static final int WRITE_ACCESS = 0x40;
	// Whether this name is a potential match for its binding, rather than an exact match.
	// Potential matches are recorded in the index so that we can e.g. offer best-effort
	// navigation even in the presence of errors in the code (or an incomplete project
	// configuration), but they are annotated as such so exact matches can be preferred
	// where appropriate.
	public static final int IS_POTENTIAL_MATCH = 0x80;
	// Note: There is no room in the flags byte for more flags. If more flags are
	//       needed, the flag byte needs to be expanded to a short.

	public PDOMName(PDOMLinkage linkage, IASTName name, PDOMFile file, PDOMBinding binding, PDOMName caller,
			boolean isPotentialMatch) throws CoreException {
		this.linkage = linkage;
		Database db = linkage.getDB();
		record = db.malloc(RECORD_SIZE);

		// What kind of name are we
		int flags = getRoleOfName(name);

		if (isPotentialMatch) {
			flags |= IS_POTENTIAL_MATCH;
		}

		flags |= binding.getAdditionalNameFlags(flags, name);
		db.putByte(record + FLAGS, (byte) flags);

		// Hook us up to the binding
		switch (flags & DECL_DEF_REF_MASK) {
		case IS_DEFINITION:
			binding.addDefinition(this);
			break;
		case IS_DECLARATION:
			binding.addDeclaration(this);
			break;
		case IS_REFERENCE:
			binding.addReference(this);
			break;
		}

		db.putRecPtr(record + BINDING_REC_OFFSET, binding.getRecord());

		db.putRecPtr(record + FILE_REC_OFFSET, file.getRecord());
		if (caller != null) {
			db.putRecPtr(record + CALLER_REC_OFFSET, caller.getRecord());
		}

		// Record our location in the file
		IASTFileLocation fileloc = name.getFileLocation();
		db.put3ByteUnsignedInt(record + NODE_OFFSET_OFFSET, fileloc.getNodeOffset());
		db.putShort(record + NODE_LENGTH_OFFSET, (short) fileloc.getNodeLength());
	}

	private int getRoleOfName(IASTName name) {
		if (name.isDefinition()) {
			return IS_DEFINITION;
		}
		if (name.isDeclaration()) {
			return IS_DECLARATION;
		}
		return IS_REFERENCE;
	}

	public PDOMName(PDOMLinkage linkage, long nameRecord) {
		this.linkage = linkage;
		this.record = nameRecord;
	}

	public PDOMLinkage getLinkage() {
		return linkage;
	}

	public long getRecord() {
		return record;
	}

	private long getRecField(int offset) throws CoreException {
		return linkage.getDB().getRecPtr(record + offset);
	}

	private void setRecField(int offset, long fieldrec) throws CoreException {
		linkage.getDB().putRecPtr(record + offset, fieldrec);
	}

	public PDOM getPDOM() {
		return linkage.getPDOM();
	}

	@Override
	public PDOMBinding getBinding() throws CoreException {
		long bindingrec = getRecField(BINDING_REC_OFFSET);
		return linkage.getBinding(bindingrec);
	}

	public void setBinding(PDOMBinding binding) throws CoreException {
		long bindingrec = binding != null ? binding.getRecord() : 0;
		setRecField(BINDING_REC_OFFSET, bindingrec);
	}

	private PDOMName getNameField(int offset) throws CoreException {
		long namerec = getRecField(offset);
		return namerec != 0 ? new PDOMName(linkage, namerec) : null;
	}

	private void setNameField(int offset, PDOMName name) throws CoreException {
		long namerec = name != null ? name.getRecord() : 0;
		setRecField(offset, namerec);
	}

	public PDOMName getPrevInBinding() throws CoreException {
		return getNameField(BINDING_PREV_OFFSET);
	}

	public void setPrevInBinding(PDOMName name) throws CoreException {
		setNameField(BINDING_PREV_OFFSET, name);
	}

	public PDOMName getNextInBinding() throws CoreException {
		return getNameField(BINDING_NEXT_OFFSET);
	}

	public void setNextInBinding(PDOMName name) throws CoreException {
		setNameField(BINDING_NEXT_OFFSET, name);
	}

	@Override
	public PDOMFile getFile() throws CoreException {
		long filerec = linkage.getDB().getRecPtr(record + FILE_REC_OFFSET);
		return filerec != 0 ? new PDOMFile(linkage, filerec) : null;
	}

	public long getFileRecord() throws CoreException {
		return linkage.getDB().getRecPtr(record + FILE_REC_OFFSET);
	}

	void setFile(PDOMFile file) throws CoreException {
		linkage.getDB().putRecPtr(record + FILE_REC_OFFSET, file != null ? file.getRecord() : 0);
	}

	public static long getFileRecord(Database db, long record) throws CoreException {
		return db.getRecPtr(record + FILE_REC_OFFSET);
	}

	@Override
	public IIndexName getEnclosingDefinition() throws CoreException {
		long namerec = getEnclosingDefinitionRecord();
		return namerec != 0 ? new PDOMName(linkage, namerec) : null;
	}

	long getEnclosingDefinitionRecord() throws CoreException {
		return linkage.getDB().getRecPtr(record + CALLER_REC_OFFSET);
	}

	public PDOMName getNextInFile() throws CoreException {
		return getNameField(FILE_NEXT_OFFSET);
	}

	public void setNextInFile(PDOMName name) throws CoreException {
		setNameField(FILE_NEXT_OFFSET, name);
	}

	/**
	 * @deprecated use {@link #getSimpleID()}, instead.
	 */
	@Override
	@Deprecated
	public char[] toCharArray() {
		return getSimpleID();
	}

	@Override
	public char[] getSimpleID() {
		try {
			PDOMBinding binding = getBinding();
			return binding != null ? binding.getNameCharArray() : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return CharArrayUtils.EMPTY;
		}
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PDOMName) {
			PDOMName other = (PDOMName) obj;
			return record == other.record && linkage.equals(other.linkage);
		}

		return super.equals(obj);
	}

	@Override
	public final int hashCode() {
		return linkage.hashCode() + (int) (41 * record);
	}

	@Override
	public String toString() {
		return new String(getSimpleID());
	}

	private int getFlags(int mask) throws CoreException {
		return linkage.getDB().getByte(record + FLAGS) & mask;
	}

	public void setIsFriendSpecifier() throws CoreException {
		int flags = linkage.getDB().getByte(record + FLAGS) & 0xff;
		flags |= IS_FRIEND_SPEC;
		linkage.getDB().putByte(record + FLAGS, (byte) flags);
	}

	public void setIsBaseSpecifier() throws CoreException {
		int flags = linkage.getDB().getByte(record + FLAGS) & 0xff;
		flags |= IS_INHERITANCE_SPEC;
		linkage.getDB().putByte(record + FLAGS, (byte) flags);
	}

	public void setIsInlineNamespace() throws CoreException {
		int flags = linkage.getDB().getByte(record + FLAGS) & 0xff;
		flags |= IS_INLINE_NAMESPACE;
		linkage.getDB().putByte(record + FLAGS, (byte) flags);
	}

	public boolean isFriendSpecifier() throws CoreException {
		return getFlags(INHERIT_FRIEND_INLINE_MASK) == IS_FRIEND_SPEC;
	}

	@Override
	public boolean isBaseSpecifier() throws CoreException {
		return getFlags(INHERIT_FRIEND_INLINE_MASK) == IS_INHERITANCE_SPEC;
	}

	@Override
	public boolean isInlineNamespaceDefinition() throws CoreException {
		return getFlags(INHERIT_FRIEND_INLINE_MASK) == IS_INLINE_NAMESPACE;
	}

	@Override
	public boolean couldBePolymorphicMethodCall() throws CoreException {
		return getFlags(COULD_BE_POLYMORPHIC_METHOD_CALL) == COULD_BE_POLYMORPHIC_METHOD_CALL;
	}

	@Override
	public boolean isPotentialMatch() throws CoreException {
		return getFlags(IS_POTENTIAL_MATCH) == IS_POTENTIAL_MATCH;
	}

	@Override
	public boolean isReadAccess() throws CoreException {
		return getFlags(READ_ACCESS) == READ_ACCESS;
	}

	@Override
	public boolean isWriteAccess() throws CoreException {
		return getFlags(WRITE_ACCESS) == WRITE_ACCESS;
	}

	@Override
	public boolean isDeclaration() {
		try {
			int flags = getFlags(DECL_DEF_REF_MASK);
			return flags == IS_DECLARATION || flags == IS_DEFINITION;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	@Override
	public boolean isReference() {
		try {
			int flags = getFlags(DECL_DEF_REF_MASK);
			return flags == IS_REFERENCE;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	@Override
	public boolean isDefinition() {
		try {
			int flags = getFlags(DECL_DEF_REF_MASK);
			return flags == IS_DEFINITION;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	@Override
	public IASTFileLocation getFileLocation() {
		try {
			IIndexFile file = getFile();
			if (file == null) {
				return null;
			}
			// We need to specify what this method can return to know
			// how to implement this. Existing implementations return
			// the absolute path, so here we attempt to do the same.
			IPath location = IndexLocationFactory.getAbsolutePath(file.getLocation());
			if (location == null) {
				return null;
			}
			String filename = location.toOSString();
			return new PDOMASTFileLocation(filename, getNodeOffset(), getNodeLength());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	@Override
	public int getNodeLength() {
		try {
			return (linkage.getDB().getShort(record + NODE_LENGTH_OFFSET)) & 0xffff;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	@Override
	public int getNodeOffset() {
		try {
			return linkage.getDB().get3ByteUnsignedInt(record + NODE_OFFSET_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public void delete() throws CoreException {
		// Delete from the binding chain
		PDOMName prevName = getPrevInBinding();
		PDOMName nextName = getNextInBinding();
		if (prevName != null) {
			prevName.setNextInBinding(nextName);
		} else {
			switch (getFlags(DECL_DEF_REF_MASK)) {
			case IS_DECLARATION:
				getBinding().setFirstDeclaration(nextName);
				break;
			case IS_DEFINITION:
				getBinding().setFirstDefinition(nextName);
				break;
			case IS_REFERENCE:
				getBinding().setFirstReference(getLinkage(), nextName);
				break;
			}
		}

		if (nextName != null)
			nextName.setPrevInBinding(prevName);

		// Delete our record
		linkage.getDB().free(record);
	}

	@Override
	public IIndexFragment getIndexFragment() {
		return linkage.getPDOM();
	}

	@Override
	public IIndexName[] getEnclosedNames() throws CoreException {
		ArrayList<PDOMName> result = new ArrayList<>();
		PDOMName name = getNextInFile();
		while (name != null) {
			if (name.getEnclosingDefinitionRecord() == record) {
				result.add(name);
			}
			name = name.getNextInFile();
		}
		return result.toArray(new PDOMName[result.size()]);
	}

	/**
	 * Returns an iterator over names in binding. This is a lighter weight alternative to
	 * the {@link #getNextInBinding()} method.
	 */
	public static IRecordIterator getNameInBindingRecordIterator(Database db, long nameRecord) {
		if (nameRecord == 0)
			return IRecordIterator.EMPTY;
		return new NameInBindingRecordIterator(db, nameRecord);
	}

	/**
	 * Iterator over PDOMName records in a binding.
	 */
	private static class NameInBindingRecordIterator implements IRecordIterator {
		final Database db;
		long nameRecord;

		public NameInBindingRecordIterator(Database db, long nameRecord) {
			this.db = db;
			this.nameRecord = nameRecord;
		}

		@Override
		public long next() throws CoreException {
			long record = nameRecord;
			if (record != 0)
				nameRecord = db.getRecPtr(record + BINDING_NEXT_OFFSET);
			return record;
		}
	}
}
