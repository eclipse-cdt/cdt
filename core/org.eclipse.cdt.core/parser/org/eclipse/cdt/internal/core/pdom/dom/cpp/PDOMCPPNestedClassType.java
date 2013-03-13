/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNestedClassType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPNestedClassType extends PDOMCPPClassType implements ICPPNestedClassType {

	private static final int VISIBILITY = PDOMCPPClassType.RECORD_SIZE + 0; // byte

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPClassType.RECORD_SIZE + 1;

	public PDOMCPPNestedClassType(PDOMLinkage linkage, PDOMNode parent, ICPPNestedClassType classType) throws CoreException {
		super(linkage, parent, classType);
		setVisibility(classType);
	}

	public PDOMCPPNestedClassType(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_NESTEDCLASSTYPE;
	}

	public void setVisibility(ICPPMember member) throws CoreException {
		getDB().putByte(record + VISIBILITY, (byte) member.getVisibility());
	}

	@Override
	public int getVisibility() {
		try {
			return getDB().getByte(record + VISIBILITY);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPMember.v_public;
		}
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public IType getType() throws DOMException {
		return this;
	}
}
