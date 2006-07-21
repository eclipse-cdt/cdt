/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public class PDOMCTypedef extends PDOMBinding implements ITypedef {

	private static final int TYPE = PDOMBinding.RECORD_SIZE + 0;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;
	
	public PDOMCTypedef(PDOM pdom, PDOMNode parent, IASTName name, ITypedef typedef)
			throws CoreException {
		super(pdom, parent, name);
		
		IType type = typedef.getType();
		PDOMNode typeNode = parent.getLinkage().addType(this, type);
		if (typeNode != null)
			pdom.getDB().putInt(record + TYPE, typeNode.getRecord());
	}

	public PDOMCTypedef(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCLinkage.CTYPEDEF;
	}

	public IType getType() throws DOMException {
		try {
			int typeRec = pdom.getDB().getInt(record + TYPE);
			return (IType)getLinkage().getNode(typeRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isSameType(IType type) {
		throw new PDOMNotImplementedError();
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}
	
}
