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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.net.URI;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMName implements IIndexFragmentName, IASTFileLocation {

	private final PDOM pdom;
	private final int record;
	
	private static final int FILE_REC_OFFSET     = 0;
	private static final int FILE_NEXT_OFFSET	 = 4;
	private static final int CALLER_REC_OFFSET   = 8;
	private static final int BINDING_REC_OFFSET  = 12;
	private static final int BINDING_PREV_OFFSET = 16;
	private static final int BINDING_NEXT_OFFSET = 20;
	private static final int NODE_OFFSET_OFFSET  = 24;
	private static final int NODE_LENGTH_OFFSET  = 28; // short
	private static final int FLAGS 				 = 30; // byte

	private static final int RECORD_SIZE = 31;

	private static final int IS_DECLARATION = 1;
	private static final int IS_DEFINITION = 2;
	private static final int IS_REFERENCE = 3;
	private static final int DECL_DEF_REF_MASK= 3;
	private static final int IS_INHERITANCE_SPEC = 4;

	

	public PDOMName(PDOM pdom, IASTName name, PDOMFile file, PDOMBinding binding, PDOMName caller) throws CoreException {
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
		
		db.putInt(record + FILE_REC_OFFSET, file.getRecord());
		if (caller != null) {
			db.putInt(record + CALLER_REC_OFFSET, caller.getRecord());
		}

		// Record our location in the file
		IASTFileLocation fileloc = name.getFileLocation();
		db.putInt(record + NODE_OFFSET_OFFSET, fileloc.getNodeOffset());
		db.putShort(record + NODE_LENGTH_OFFSET, (short) fileloc.getNodeLength());
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
	
	public IIndexFile getFile() throws CoreException {
		int filerec = pdom.getDB().getInt(record + FILE_REC_OFFSET);
		return filerec != 0 ? new PDOMFile(pdom, filerec) : null;
	}

	public IIndexName getEnclosingDefinition() throws CoreException {
		int namerec = getEnclosingDefinitionRecord();
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}

	int getEnclosingDefinitionRecord() throws CoreException {
		return pdom.getDB().getInt(record + CALLER_REC_OFFSET);
	}
	
	public PDOMName getNextInFile() throws CoreException {
		return getNameField(FILE_NEXT_OFFSET);
	}
	
	public void setNextInFile(PDOMName name) throws CoreException {
		setNameField(FILE_NEXT_OFFSET, name);
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

	public String toString() {
		try {
			Database db = pdom.getDB();
			int bindingRec = db.getInt(record + BINDING_REC_OFFSET);
			PDOMBinding binding = pdom.getBinding(bindingRec);
			return binding != null ? binding.getName() : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	private byte getFlags(int mask) throws CoreException {
		return (byte) (pdom.getDB().getByte(record + FLAGS) & mask);
	}

	public void setIsBaseSpecifier(boolean val) throws CoreException {
		byte flags= getFlags(0xff);
		if (val) 
			flags |= IS_INHERITANCE_SPEC;
		else
			flags &= ~IS_INHERITANCE_SPEC;
		pdom.getDB().putByte(record + FLAGS, flags);
	}

	public boolean isBaseSpecifier() throws CoreException {
		return getFlags(IS_INHERITANCE_SPEC) == IS_INHERITANCE_SPEC;
	}
	
	public boolean isDeclaration() {
		try {
			byte flags = getFlags(DECL_DEF_REF_MASK);
			return flags == IS_DECLARATION || flags == IS_DEFINITION;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isReference() {
		try {
			byte flags = getFlags(DECL_DEF_REF_MASK);
			return flags == IS_REFERENCE;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isDefinition() {
		try {
			byte flags = getFlags(DECL_DEF_REF_MASK);
			return flags == IS_DEFINITION;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public IASTFileLocation getFileLocation() {
		return this;
	}

	public String getFileName() {
		try {
			PDOMFile file = (PDOMFile) getFile();
			if(file!=null) {
				/*
				 * We need to spec. what this method can return to know
				 * how to implement this. Existing implmentations return
				 * the absolute path, so here we attempt to do the same.
				 */
				URI uri = file.getLocation().getURI();
				if ("file".equals(uri.getScheme())) //$NON-NLS-1$
					return uri.getSchemeSpecificPart();
			}
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
			return pdom.getDB().getShort(record + NODE_LENGTH_OFFSET);
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
			switch (getFlags(DECL_DEF_REF_MASK)) {
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

	public IIndexFragment getIndexFragment() {
		return pdom;
	}

	public IIndexFragmentBinding getBinding() throws CoreException {
		return getPDOMBinding();
	}

	public IIndexName[] getEnclosedNames() throws CoreException {
		ArrayList result= new ArrayList();
		PDOMName name= getNextInFile();
		while (name != null) {
			if (name.getEnclosingDefinitionRecord() == record) {
				result.add(name);
			}
			name= name.getNextInFile();
		}
		return (IIndexName[]) result.toArray(new IIndexName[result.size()]);
	}
}
