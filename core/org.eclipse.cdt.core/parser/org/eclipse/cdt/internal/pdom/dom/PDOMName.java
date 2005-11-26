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
package org.eclipse.cdt.internal.pdom.dom;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope2;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMName implements IASTName, IASTFileLocation {

	private final PDOMDatabase pdom;
	private final int record;
	
	private static final int FILE_REC_OFFSET = 0 * Database.INT_SIZE;
	private static final int FILE_PREV_OFFSET = 1 * Database.INT_SIZE;
	private static final int FILE_NEXT_OFFSET = 2 * Database.INT_SIZE;
	private static final int BINDING_REC_OFFET = 3 * Database.INT_SIZE;
	private static final int BINDING_PREV_OFFSET = 4 * Database.INT_SIZE;
	private static final int BINDING_NEXT_OFFSET = 5 * Database.INT_SIZE;
	private static final int NODE_OFFSET_OFFSET = 6 * Database.INT_SIZE;
	private static final int NODE_LENGTH_OFFSET = 7 * Database.INT_SIZE;
	
	private static final int RECORD_SIZE = 8 * Database.INT_SIZE;

	public PDOMName(PDOMDatabase pdom, IASTName name, PDOMBinding binding) throws CoreException {
		try {
			this.pdom = pdom;
			Database db = pdom.getDB();
			record = db.malloc(RECORD_SIZE);
			
			// Hook us up to the binding
			if (binding != null) {
				db.putInt(record + BINDING_REC_OFFET, binding.getRecord());
				if (name.isDeclaration())
					binding.addDeclaration(this);
			}
			
			// Hook us up the the liked name list from file
			IASTFileLocation fileloc = name.getFileLocation();
			String filename = fileloc.getFileName();
			PDOMFile pdomFile = PDOMFile.insert(pdom, filename);
			db.putInt(record + FILE_REC_OFFSET, pdomFile.getRecord());
			int firstName = pdomFile.getFirstName();
			if (firstName != 0) {
				db.putInt(record + FILE_NEXT_OFFSET, firstName);
				db.putInt(firstName + FILE_PREV_OFFSET, record);
			}
			pdomFile.setFirstName(record);
			
			db.putInt(record + NODE_OFFSET_OFFSET, fileloc.getNodeOffset());
			db.putInt(record + NODE_LENGTH_OFFSET, fileloc.getNodeLength());
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					CCorePlugin.PLUGIN_ID, 0, "Failed to allocate name", e));
		}
	}
	
	public PDOMName(PDOMDatabase pdom, int nameRecord) throws IOException {
		this.pdom = pdom;
		this.record = nameRecord;
	}
	
	public int getRecord() {
		return record;
	}
	
	public void setBinding(PDOMBinding binding) throws IOException {
		pdom.getDB().putInt(record + BINDING_REC_OFFET, binding.getRecord());
	}

	public void setPrevInBinding(PDOMName prevName) throws IOException {
		pdom.getDB().putInt(record + BINDING_PREV_OFFSET, prevName.getRecord());
	}
	
	public void setNextInBinding(PDOMName nextName) throws IOException {
		pdom.getDB().putInt(record + BINDING_NEXT_OFFSET, nextName.getRecord());
	}
	
	public IBinding resolveBinding() {
		try {
			int bindingRecord = pdom.getDB().getInt(record + BINDING_REC_OFFET);
			return new PDOMBinding(pdom, bindingRecord);
		} catch (IOException e) {
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
			int bindingRec = db.getInt(record + BINDING_REC_OFFET);
			if (bindingRec == 0)
				return null;
			
			return new PDOMBinding(pdom, bindingRec).getNameCharArray();
		} catch (IOException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isDeclaration() {
		throw new PDOMNotImplementedError();
	}

	public boolean isReference() {
		throw new PDOMNotImplementedError();
	}

	public boolean isDefinition() {
		throw new PDOMNotImplementedError();
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
			return new PDOMFile(pdom, pdom.getDB().getInt(record + FILE_REC_OFFSET)).getFileName();
		} catch (IOException e) {
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
		} catch (IOException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public int getNodeOffset() {
		try {
			return pdom.getDB().getInt(record + NODE_OFFSET_OFFSET);
		} catch (IOException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public IScope2 getScope(IASTNode child, ASTNodeProperty childProperty) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
