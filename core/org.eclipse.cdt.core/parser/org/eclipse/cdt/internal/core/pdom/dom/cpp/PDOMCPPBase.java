/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBase;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPBase implements ICPPBase, ICPPInternalBase {
	static final int CLASS_DEFINITION = 0;
	private static final int BASECLASS_TYPE = CLASS_DEFINITION + Database.PTR_SIZE;
	private static final int NEXTBASE = BASECLASS_TYPE + Database.TYPE_SIZE;
	private static final int FLAGS = NEXTBASE + Database.PTR_SIZE;
	
	protected static final int RECORD_SIZE = FLAGS + 1;
	
	private final PDOMLinkage linkage;
	private final long record;
	
	private IType fCachedBaseClass;
	
	public PDOMCPPBase(PDOMLinkage linkage, long record) {
		this.linkage = linkage;
		this.record = record;
	}
	
	public PDOMCPPBase(PDOMLinkage linkage, ICPPBase base, PDOMName classDefName) throws CoreException {
		Database db = linkage.getDB();
		this.linkage = linkage;
		this.record = db.malloc(RECORD_SIZE);
		db.putRecPtr(record + CLASS_DEFINITION, classDefName.getRecord());
		linkage.storeType(record+BASECLASS_TYPE, base.getBaseClassType());
		
		byte flags = (byte)(base.getVisibility() | (base.isVirtual() ? 4 : 0));
		db.putByte(record + FLAGS, flags);
	}

	private Database getDB() {
		return linkage.getDB();
	}

	public long getRecord() {
		return record;
	}
	
	public void setNextBase(PDOMCPPBase nextBase) throws CoreException {
		long rec = nextBase != null ? nextBase.getRecord() : 0;
		getDB().putRecPtr(record + NEXTBASE, rec);
	}
	
	public PDOMCPPBase getNextBase() throws CoreException {
		long rec = getDB().getRecPtr(record + NEXTBASE);
		return rec != 0 ? new PDOMCPPBase(linkage, rec) : null;
	}
	
	private int getFlags() throws CoreException {
		return getDB().getByte(record + FLAGS);
	}

	@Override
	public PDOMName getBaseClassSpecifierName() {
		return null;
	}
	
	@Override
	public PDOMName getClassDefinitionName() {
		try {
			long rec = getDB().getRecPtr(record + CLASS_DEFINITION);
			if (rec != 0) {
				return new PDOMName(linkage, rec);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	@Override
	public IType getBaseClassType() {
		if (fCachedBaseClass == null) {
			try {
				fCachedBaseClass= linkage.loadType(record + BASECLASS_TYPE);
			} catch (CoreException e) {
				fCachedBaseClass= new ProblemType(ISemanticProblem.TYPE_NOT_PERSISTED);
			}
		}
		return fCachedBaseClass;
	}

	@Override
	public IBinding getBaseClass() {
		IType type= getBaseClassType();
		type = getNestedType(type, TDEF);
		if (type instanceof IBinding)
			return (IBinding) type;
		return null;
	}

	@Override
	public int getVisibility() {
		try {
			return getFlags() & 0x3;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
		
	}

	@Override
	public boolean isVirtual() {
		try {
			return (getFlags() & 0x4) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public void delete() throws CoreException {
		getDB().free(record);
	}
	
	@Override
	public void setBaseClass(IBinding binding) {
		throw new UnsupportedOperationException(); 
	}
	@Override
	public void setBaseClass(IType binding) {
		throw new UnsupportedOperationException(); 
	}
	
	@Override
	public ICPPBase clone() {
		return new PDOMCPPBaseClone(this);
	}
	
	private static class PDOMCPPBaseClone implements ICPPBase, ICPPInternalBase {
		private final ICPPBase base;
		private IType baseClass = null;
		
		public PDOMCPPBaseClone(ICPPBase base) {
			this.base = base;
		}
		@Override
		public IBinding getBaseClass() {
			IType type= getBaseClassType();
			type = getNestedType(type, TDEF);
			if (type instanceof IBinding)
				return (IBinding) type;
			return null;
		}
		@Override
		public IType getBaseClassType() {
			if (baseClass == null) {
				baseClass=  base.getBaseClassType();
			}
			return baseClass;
		}
		
		@Override @Deprecated
		public IName getBaseClassSpecifierName() {
			return base.getBaseClassSpecifierName();
		}
		@Override
		public IName getClassDefinitionName() {
			return base.getClassDefinitionName();
		}
		
		@Override
		public int getVisibility() {
			return base.getVisibility();
		}
		@Override
		public boolean isVirtual() {
			return base.isVirtual();
		}
		@Override
		public void setBaseClass(IBinding binding) {
			if (binding instanceof IType)
				baseClass = (IType) binding;
		}
		@Override
		public void setBaseClass(IType binding) {
			baseClass = binding;
		}
		@Override
		public ICPPBase clone() {
			return new PDOMCPPBaseClone(this);
		}
	}
}
