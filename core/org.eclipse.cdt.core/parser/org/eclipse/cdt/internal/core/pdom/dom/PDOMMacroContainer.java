/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexMacroContainer;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * A container collecting definitions and references for macros.
 * @since 5.0
 */
public class PDOMMacroContainer extends PDOMNamedNode implements IIndexMacroContainer, IPDOMBinding {
	private static final int FIRST_DEF_OFFSET    = PDOMNamedNode.RECORD_SIZE + 0; // size 4
	private static final int FIRST_REF_OFFSET    = PDOMNamedNode.RECORD_SIZE + 4; // size 4
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 8;

	public PDOMMacroContainer(PDOMLinkage linkage, char[] name) throws CoreException {
		super(linkage, linkage, name);
	}
	
	PDOMMacroContainer(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}
		
	@Override
	public int getNodeType() {
		return IIndexBindingConstants.MACRO_CONTAINER;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public boolean isOrphaned() throws CoreException {
		Database db = getDB();
		return db.getRecPtr(record + FIRST_DEF_OFFSET) == 0
			&& db.getRecPtr(record + FIRST_REF_OFFSET) == 0;
	}

	public void addDefinition(PDOMMacro name) throws CoreException {
		PDOMMacro first = getFirstDefinition();
		if (first != null) {
			first.setPrevInContainer(name);
			name.setNextInContainer(first);
		}
		setFirstDefinition(name);
	}
	
	public void addReference(PDOMMacroReferenceName name) throws CoreException {
		PDOMMacroReferenceName first = getFirstReference();
		if (first != null) {
			first.setPrevInContainer(name);
			name.setNextInContainer(first);
		}
		setFirstReference(name);
	}
	
	public PDOMMacro getFirstDefinition() throws CoreException {
		long namerec = getDB().getRecPtr(record + FIRST_DEF_OFFSET);
		return namerec != 0 ? new PDOMMacro(getLinkage(), namerec) : null;
	}
	
	void setFirstDefinition(PDOMMacro macro) throws CoreException {
		long namerec = macro != null ? macro.getRecord() : 0;
		getDB().putRecPtr(record + FIRST_DEF_OFFSET, namerec);
	}
	
	public PDOMMacroReferenceName getFirstReference() throws CoreException {
		long namerec = getDB().getRecPtr(record + FIRST_REF_OFFSET);
		return namerec != 0 ? new PDOMMacroReferenceName(getLinkage(), namerec) : null;
	}
	
	void setFirstReference(PDOMMacroReferenceName nextName) throws CoreException {
		long namerec = nextName != null ? nextName.getRecord() : 0;
		getDB().putRecPtr(record + FIRST_REF_OFFSET, namerec);
	}

	public IIndexMacro[] getDefinitions() throws CoreException {
		PDOMMacro macro;
		List<PDOMMacro> macros= new ArrayList<PDOMMacro>();
		for (macro= getFirstDefinition(); macro != null; macro= macro.getNextInContainer()) {
			macros.add(macro);
		}
		return macros.toArray(new IIndexMacro[macros.size()]);
	}

	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		if (linkage != null) {
			linkage.removeMacroContainer(this);
		}
		super.delete(linkage);
	}

	@Override
	public int getBindingConstant() {
		return IIndexBindingConstants.MACRO_CONTAINER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.IIndexFragmentBinding#getFragment()
	 */
	@Override
	public IIndexFragment getFragment() {
		return getPDOM();
	}

	@Override
	public IIndexScope getScope() {
		return null;
	}

	@Override
	public boolean hasDeclaration() throws CoreException {
		return false;
	}

	@Override
	public boolean hasDefinition() throws CoreException {
		return getDB().getRecPtr(record + FIRST_DEF_OFFSET) != 0;
	}

	@Override
	public IIndexFile getLocalToFile() throws CoreException {
		return null;
	}

	@Override
	public String[] getQualifiedName() {
		return new String[]{getName()};
	}

	@Override
	public boolean isFileLocal() throws CoreException {
		return false;
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

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(PDOMMacroContainer.class)) {
			return this;
		}
		return null;
	}
}
