/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.DBString;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMName implements IASTName, IASTFileLocation {

	private final PDOM pdom;
	private final int record;
	
	private static final int FILE_REC_OFFSET     = 0;
	private static final int FILE_PREV_OFFSET 	 = 4;
	private static final int FILE_NEXT_OFFSET	 = 8;
	private static final int BINDING_REC_OFFSET  = 12;
	private static final int BINDING_PREV_OFFSET = 16;
	private static final int BINDING_NEXT_OFFSET = 20;
	private static final int NODE_OFFSET_OFFSET  = 24;
	private static final int NODE_LENGTH_OFFSET  = 28;
	private static final int FLAGS 				 = 32;

	private static final int RECORD_SIZE = 33;

	private static final int IS_DECLARATION = 1;
	private static final int IS_DEFINITION = 2;
	private static final int IS_REFERENCE = 3;
	

	public PDOMName(PDOM pdom, IASTName name, PDOMBinding binding) throws CoreException {
		this.pdom = pdom;
		Database db = pdom.getDB();
		record = db.malloc(RECORD_SIZE);

		// What kind of name are we
		byte flags = 0;
		if (name.isDefinition())
			flags = IS_DEFINITION;
		else if (name.isDeclaration())
			flags = IS_DECLARATION;
		else 
			flags = IS_REFERENCE;
		db.putByte(record + FLAGS, flags);

		// Hook us up to the binding
		if (binding != null) {
			switch (flags) {
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

			db.putInt(record + BINDING_REC_OFFSET, binding.getRecord());
		}
		
		// Hook us up the the liked name list from file
		IASTFileLocation fileloc = name.getFileLocation();
		String filename = fileloc.getFileName();
		PDOMFile pdomFile = pdom.addFile(filename);
		db.putInt(record + FILE_REC_OFFSET, pdomFile.getRecord());
		PDOMName firstName = pdomFile.getFirstName();
		if (firstName != null) {
			db.putInt(record + FILE_NEXT_OFFSET, firstName.getRecord());
			firstName.setPrevInFile(this);
		}
		pdomFile.setFirstName(this);

		db.putInt(record + NODE_OFFSET_OFFSET, fileloc.getNodeOffset());
		db.putInt(record + NODE_LENGTH_OFFSET, fileloc.getNodeLength());
	}
	
	public PDOMName(PDOM pdom, int nameRecord) {
		this.pdom = pdom;
		this.record = nameRecord;
	}
	
	public int getRecord() {
		return record;
	}

	private int getRecField(int offset) throws CoreException {
		return pdom.getDB().getInt(record + offset);
	}
	
	private void setRecField(int offset, int fieldrec) throws CoreException {
		pdom.getDB().putInt(record + offset, fieldrec);
	}
	
	public PDOMBinding getPDOMBinding() throws CoreException {
		int bindingrec = getRecField(BINDING_REC_OFFSET);
		return pdom.getBinding(bindingrec);
	}
	
	public void setBinding(PDOMBinding binding) throws CoreException {
		int bindingrec = binding != null ? binding.getRecord() : 0;
		setRecField(BINDING_REC_OFFSET, bindingrec);
	}

	private PDOMName getNameField(int offset) throws CoreException {
		int namerec = getRecField(offset);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	private void setNameField(int offset, PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
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
	
	public PDOMFile getFile() throws CoreException {
		int filerec = pdom.getDB().getInt(record + FILE_REC_OFFSET);
		return filerec != 0 ? new PDOMFile(pdom, filerec) : null;
	}
	
	public PDOMName getNextInFile() throws CoreException {
		return getNameField(FILE_NEXT_OFFSET);
	}
	
	public void setNextInFile(PDOMName name) throws CoreException {
		setNameField(FILE_NEXT_OFFSET, name);
	}
	
	public PDOMName getPrevInFile() throws CoreException {
		return getNameField(FILE_PREV_OFFSET);
	}

	public void setPrevInFile(PDOMName name) throws CoreException {
		setNameField(FILE_PREV_OFFSET, name);
	}
	
	public IBinding resolveBinding() {
		try {
			int bindingRecord = pdom.getDB().getInt(record + BINDING_REC_OFFSET);
			return pdom.getBinding(bindingRecord);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IBinding getBinding() {
		throw new PDOMNotImplementedError();
	}

	public void setBinding(IBinding binding) {
		throw new PDOMNotImplementedError();
	}

	public IBinding[] resolvePrefix() {
		throw new PDOMNotImplementedError();
	}

	public char[] toCharArray() {
		try {
			Database db = pdom.getDB();
			int bindingRec = db.getInt(record + BINDING_REC_OFFSET);
			PDOMBinding binding = pdom.getBinding(bindingRec);
			return binding != null ? binding.getNameCharArray() : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	private byte getFlags() throws CoreException {
		return pdom.getDB().getByte(record + FLAGS);
	}
	
	public boolean isDeclaration() {
		try {
			byte flags = getFlags();
			return flags == IS_DECLARATION || flags == IS_DEFINITION;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isReference() {
		try {
			byte flags = getFlags();
			return flags == IS_REFERENCE;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isDefinition() {
		try {
			byte flags = getFlags();
			return flags == IS_DEFINITION;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public IASTTranslationUnit getTranslationUnit() {
		// TODO Bug 115367 this is dumb - only need for validation checks
		return new PDOMTranslationUnit();
	}

	public IASTNodeLocation[] getNodeLocations() {
		throw new PDOMNotImplementedError();
	}

	public IASTFileLocation getFileLocation() {
		return this;
	}

	public String getContainingFilename() {
		throw new PDOMNotImplementedError();
	}

	public IASTNode getParent() {
		throw new PDOMNotImplementedError();
	}

	public void setParent(IASTNode node) {
		throw new PDOMNotImplementedError();
	}

	public ASTNodeProperty getPropertyInParent() {
		throw new PDOMNotImplementedError();
	}

	public void setPropertyInParent(ASTNodeProperty property) {
		throw new PDOMNotImplementedError();
	}

	public boolean accept(ASTVisitor visitor) {
		throw new PDOMNotImplementedError();
	}

	public String getRawSignature() {
		throw new PDOMNotImplementedError();
	}

	public int getEndingLineNumber() {
		throw new PDOMNotImplementedError();
	}

	public String getFileName() {
		try {
			PDOMFile file = getFile();
			return file != null ? file.getFileName().getString() : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public int getStartingLineNumber() {
		throw new PDOMNotImplementedError();
	}

	public IASTFileLocation asFileLocation() {
		throw new PDOMNotImplementedError();
	}

	public int getNodeLength() {
		try {
			return pdom.getDB().getInt(record + NODE_LENGTH_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public int getNodeOffset() {
		try {
			return pdom.getDB().getInt(record + NODE_OFFSET_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public void delete() throws CoreException {
		// Delete from the binding chain
		PDOMName prevName = getPrevInBinding();
		PDOMName nextName = getNextInBinding();
		if (prevName != null)
			prevName.setNextInBinding(nextName);
		else {
			switch (getFlags()) {
			case IS_DECLARATION:
				getPDOMBinding().setFirstDeclaration(nextName);
				break;
			case IS_DEFINITION:
				getPDOMBinding().setFirstDefinition(nextName);
				break;
			case IS_REFERENCE:
				getPDOMBinding().setFirstReference(nextName);
				break;
			}
		}
		
		if (nextName != null)
			nextName.setPrevInBinding(prevName);
		
		// Delete our record
		pdom.getDB().free(record);
	}
	
}
