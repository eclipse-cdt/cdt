/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPBase implements ICPPBase {

	private static final int BASECLASS_SPECIFIER = 0;
	private static final int NEXTBASE = 4;
	private static final int FLAGS = 8;
	
	protected static final int RECORD_SIZE = 9;
	
	protected final PDOM pdom;
	protected final int record;
	
	public PDOMCPPBase(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public PDOMCPPBase(PDOM pdom, PDOMName baseClassSpec, boolean isVirtual, int visibility) throws CoreException {
		this.pdom = pdom;
		Database db = pdom.getDB();
		this.record = db.malloc(RECORD_SIZE);
		
		int baserec = baseClassSpec != null ? baseClassSpec.getRecord() : 0;
		db.putInt(record + BASECLASS_SPECIFIER, baserec);
		
		byte flags = (byte)(visibility | (isVirtual ? 4 : 0));
		db.putByte(record + FLAGS, flags);
	}

	public int getRecord() {
		return record;
	}
	
	public void setNextBase(PDOMCPPBase nextBase) throws CoreException {
		int rec = nextBase != null ? nextBase.getRecord() : 0;
		pdom.getDB().putInt(record + NEXTBASE, rec);
	}
	
	public PDOMCPPBase getNextBase() throws CoreException {
		int rec = pdom.getDB().getInt(record + NEXTBASE);
		return rec != 0 ? new PDOMCPPBase(pdom, rec) : null;
	}
	
	private int getFlags() throws CoreException {
		return pdom.getDB().getByte(record + FLAGS);
	}

	public PDOMName getBaseClassSpecifierImpl() {
		try {
			int rec = pdom.getDB().getInt(record + BASECLASS_SPECIFIER);
			if (rec != 0) {
				return new PDOMName(pdom, rec);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public IBinding getBaseClass() {
		try {
			PDOMName name= getBaseClassSpecifierImpl();
			if (name != null) {
				PDOMBinding b = name.getPDOMBinding();
		    	while( b instanceof PDOMCPPTypedef && ((PDOMCPPTypedef)b).getType() instanceof PDOMBinding ){
					b = (PDOMBinding) ((PDOMCPPTypedef)b).getType();
		    	}
		    	return b;
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
		pdom.getDB().free(record);
	}
}
