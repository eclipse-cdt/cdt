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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBase;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPBase implements ICPPBase, ICPPInternalBase {

	private static final int BASECLASS_SPECIFIER = 0;
	private static final int NEXTBASE = 4;
	private static final int FLAGS = 8;
	
	protected static final int RECORD_SIZE = 9;
	
	private final PDOMLinkage linkage;
	private final long record;
	
	private PDOMBinding fCachedBaseClass;
	
	public PDOMCPPBase(PDOMLinkage linkage, long record) {
		this.linkage = linkage;
		this.record = record;
	}
	
	public PDOMCPPBase(PDOMLinkage linkage, PDOMName baseClassSpec, boolean isVirtual, int visibility) throws CoreException {
		this.linkage = linkage;
		Database db = getDB();
		this.record = db.malloc(RECORD_SIZE);
		
		long baserec = baseClassSpec != null ? baseClassSpec.getRecord() : 0;
		db.putRecPtr(record + BASECLASS_SPECIFIER, baserec);
		
		byte flags = (byte)(visibility | (isVirtual ? 4 : 0));
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

	public PDOMName getBaseClassSpecifierName() {
		try {
			long rec = getDB().getRecPtr(record + BASECLASS_SPECIFIER);
			if (rec != 0) {
				return new PDOMName(linkage, rec);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	public IBinding getBaseClass() {
		if (fCachedBaseClass != null)
			return fCachedBaseClass;
		
		try {
			PDOMName name= getBaseClassSpecifierName();
			if (name != null) {
				PDOMBinding b = name.getBinding();
		    	while( b instanceof PDOMCPPTypedef && ((PDOMCPPTypedef)b).getType() instanceof PDOMBinding ){
					b = (PDOMBinding) ((PDOMCPPTypedef)b).getType();
		    	}
		    	return fCachedBaseClass= b;
			}				
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public int getVisibility() {
		try {
			return getFlags() & 0x3;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
		
	}

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
	
	public void setBaseClass(IBinding binding) {
		throw new PDOMNotImplementedError();
	}
	
	@Override
	public ICPPBase clone() {
		return new PDOMCPPBaseClone(this);
	}
	
	private static class PDOMCPPBaseClone implements ICPPBase, ICPPInternalBase {
		private ICPPBase base;
		private IBinding baseClass = null;
		
		public PDOMCPPBaseClone(ICPPBase base) {
			this.base = base;
		}
		public IBinding getBaseClass() {
			if (baseClass == null) {
				return base.getBaseClass();
			}
			return baseClass;
		}
		public IName getBaseClassSpecifierName() {
			return base.getBaseClassSpecifierName();
		}
		public int getVisibility() {
			return base.getVisibility();
		}
		public boolean isVirtual() {
			return base.isVirtual();
		}
		public void setBaseClass(IBinding binding) {
			baseClass = binding;
		}
		@Override
		public ICPPBase clone() {
			return new PDOMCPPBaseClone(this);
		}
	}
}
