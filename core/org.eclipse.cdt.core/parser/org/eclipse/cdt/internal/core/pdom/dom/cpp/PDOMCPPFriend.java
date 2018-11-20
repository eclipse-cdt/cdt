/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

class PDOMCPPFriend extends PDOMNode {
	private static final int FRIEND_SPECIFIER = PDOMNode.RECORD_SIZE + 0;
	private static final int NEXT_FRIEND = PDOMNode.RECORD_SIZE + 4;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 8;

	public PDOMCPPFriend(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMCPPFriend(PDOMLinkage linkage, PDOMName friendSpec) throws CoreException {
		super(linkage, null);

		long friendrec = friendSpec != null ? friendSpec.getRecord() : 0;
		linkage.getDB().putRecPtr(record + FRIEND_SPECIFIER, friendrec);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FRIEND_DECLARATION;
	}

	public PDOMName getSpecifierName() throws CoreException {
		long rec = getDB().getRecPtr(record + FRIEND_SPECIFIER);
		if (rec != 0)
			return new PDOMName(getLinkage(), rec);
		return null;
	}

	public IBinding getFriendSpecifier() {
		PDOMName friendSpecName;
		try {
			friendSpecName = getSpecifierName();
			if (friendSpecName != null) {
				return friendSpecName.getBinding();
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public void setNextFriend(PDOMCPPFriend nextFriend) throws CoreException {
		long rec = nextFriend != null ? nextFriend.getRecord() : 0;
		getDB().putRecPtr(record + NEXT_FRIEND, rec);
	}

	public PDOMCPPFriend getNextFriend() throws CoreException {
		long rec = getDB().getRecPtr(record + NEXT_FRIEND);
		return rec != 0 ? new PDOMCPPFriend(getLinkage(), rec) : null;
	}

	public void delete() throws CoreException {
		getDB().free(record);
	}
}
