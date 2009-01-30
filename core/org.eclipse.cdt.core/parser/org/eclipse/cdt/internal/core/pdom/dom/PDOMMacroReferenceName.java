/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Represents declarations, definitions and references to bindings, except for macros.
 */
public final class PDOMMacroReferenceName implements IIndexFragmentName, IASTFileLocation {
	private final PDOMLinkage linkage;
	private final int record;
	
	private static final int FILE_REC_OFFSET     = 0;
	private static final int FILE_NEXT_OFFSET	 = 4;
	private static final int CONTAINER_REC_OFFSET  = 8;
	private static final int CONTAINER_PREV_OFFSET = 12;
	private static final int CONTAINER_NEXT_OFFSET = 16;
	private static final int NODE_OFFSET_OFFSET  = 20; 
	private static final int NODE_LENGTH_OFFSET  = 24; 

	private static final int RECORD_SIZE = 26;	

	public PDOMMacroReferenceName(PDOMLinkage linkage, IASTName name, PDOMFile file,
			PDOMMacroContainer container) throws CoreException {
		this.linkage = linkage;
		Database db = linkage.getDB();
		record = db.malloc(RECORD_SIZE);
		
		db.putInt(record + CONTAINER_REC_OFFSET, container.getRecord());
		db.putInt(record + FILE_REC_OFFSET, file.getRecord());

		// Record our location in the file
		IASTFileLocation fileloc = name.getFileLocation();
		db.putInt(record + NODE_OFFSET_OFFSET, fileloc.getNodeOffset());
		db.putShort(record + NODE_LENGTH_OFFSET, (short) fileloc.getNodeLength());
		container.addReference(this);
	}

	public PDOMMacroReferenceName(PDOMLinkage linkage, int nameRecord) {
		this.linkage = linkage;
		this.record = nameRecord;
	}
	
	public int getRecord() {
		return record;
	}

	private int getRecField(int offset) throws CoreException {
		return linkage.getDB().getInt(record + offset);
	}

	private void setRecField(int offset, int fieldrec) throws CoreException {
		linkage.getDB().putInt(record + offset, fieldrec);
	}

	public PDOMMacroContainer getContainer() throws CoreException {
		int bindingrec = getRecField(CONTAINER_REC_OFFSET);
		return new PDOMMacroContainer(linkage, bindingrec);
	}

	private PDOMMacroReferenceName getNameField(int offset) throws CoreException {
		int namerec = getRecField(offset);
		return namerec != 0 ? new PDOMMacroReferenceName(linkage, namerec) : null;
	}

	private void setNameField(int offset, PDOMMacroReferenceName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		setRecField(offset, namerec);
	}

	PDOMMacroReferenceName getPrevInContainer() throws CoreException {
		return getNameField(CONTAINER_PREV_OFFSET);
	}

	void setPrevInContainer(PDOMMacroReferenceName name) throws CoreException {
		setNameField(CONTAINER_PREV_OFFSET, name);
	}

	public PDOMMacroReferenceName getNextInContainer() throws CoreException {
		return getNameField(CONTAINER_NEXT_OFFSET);
	}
	
	void setNextInContainer(PDOMMacroReferenceName name) throws CoreException {
		setNameField(CONTAINER_NEXT_OFFSET, name);
	}
	
	public PDOMFile getFile() throws CoreException {
		int filerec = linkage.getDB().getInt(record + FILE_REC_OFFSET);
		return filerec != 0 ? new PDOMFile(linkage, filerec) : null;
	}
	
	PDOMMacroReferenceName getNextInFile() throws CoreException {
		return getNameField(FILE_NEXT_OFFSET);
	}
	
	void setNextInFile(PDOMMacroReferenceName name) throws CoreException {
		setNameField(FILE_NEXT_OFFSET, name);
	}
		
	/**
	 * @deprecated use {@link #getSimpleID()}.
	 */
	@Deprecated
	public char[] toCharArray() {
		return getSimpleID();
	}
	
	public char[] getSimpleID() {
		try {
			return getContainer().getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return CharArrayUtils.EMPTY;
		}
	}

	@Override
	public String toString() {
		return new String(getSimpleID());
	}
	
	public boolean isBaseSpecifier() throws CoreException {
		return false;
	}
	
	public boolean couldBePolymorphicMethodCall() throws CoreException {
		return false;
	}

	public boolean isReadAccess() throws CoreException {
		return false;
	}

	public boolean isWriteAccess() throws CoreException {
		return false;
	}

	public boolean isDeclaration() {
		return false;
	}

	public boolean isReference() {
		return true;
	}

	public boolean isDefinition() {
		return false;
	}

	public IASTFileLocation getFileLocation() {
		return this;
	}

	public String getFileName() {
		try {
			IIndexFile file = getFile();
			if (file == null) {
				return null;
			}
			// We need to specify what this method can return to know
			// how to implement this. Existing implementations return
			// the absolute path, so here we attempt to do the same.
			IPath location = IndexLocationFactory.getAbsolutePath(file.getLocation());
			return location != null ? location.toOSString() : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public int getStartingLineNumber() {
		return 0;
	}

	public int getEndingLineNumber() {
		return 0;
	}

	public IASTFileLocation asFileLocation() {
		return this;
	}

	public int getNodeLength() {
		try {
			return (linkage.getDB().getShort(record + NODE_LENGTH_OFFSET)) & 0xffff;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public int getNodeOffset() {
		try {
			return linkage.getDB().getInt(record + NODE_OFFSET_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public void delete() throws CoreException {
		// Delete from the binding chain
		PDOMMacroReferenceName prevName = getPrevInContainer();
		PDOMMacroReferenceName nextName = getNextInContainer();
		if (prevName != null) {
			prevName.setNextInContainer(nextName);
		} else {
			getContainer().setFirstReference(nextName);
		}

		if (nextName != null)
			nextName.setPrevInContainer(prevName);

		// Delete our record
		linkage.getDB().free(record);
	}

	public IIndexFragment getIndexFragment() {
		return linkage.getPDOM();
	}

	public IIndexName[] getEnclosedNames() throws CoreException {
		return IIndexName.EMPTY_ARRAY;
	}

	public IIndexFragmentBinding getBinding() throws CoreException {
		return getContainer();
	}

	public IIndexName getEnclosingDefinition() throws CoreException {
		return null;
	}
}
