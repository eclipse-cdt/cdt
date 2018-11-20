/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.qt.core.QtMethodUtil;
import org.eclipse.cdt.internal.qt.core.index.IQObject.IMember;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQMethod;
import org.eclipse.core.runtime.CoreException;

public class QMethod implements IQMethod {

	private final IQObject owner;
	private final String name;
	private final IQMethod.Kind kind;
	private final Collection<String> signatures;
	private final Long revision;

	public QMethod(IQObject owner, QtPDOMQMethod pdom) throws CoreException {
		this.owner = owner;
		this.name = pdom.getName();
		this.kind = pdom.getKind();
		this.signatures = QtMethodUtil.getDecodedQtMethodSignatures(pdom.getQtEncodedSignatures());
		this.revision = pdom.getRevision();
	}

	@Override
	public boolean isOverride(IMember member) {
		if (!IQMethod.class.isAssignableFrom(member.getClass()))
			return false;

		// Methods override when they have the same name and type.

		IQMethod other = (IQMethod) member;

		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;

		IBinding otherBinding = other.getBinding();
		if (otherBinding == null)
			return getBinding() == null;

		return false;// TODO
		//		if (!ICPPMethod.class.isAssignableFrom(otherBinding.getClass()))
		//			return false;
		//
		//		IType thisType = method.getType();
		//		IType otherType = ((ICPPMethod) otherBinding).getType();
		//		return thisType == null ? otherType == null : thisType.isSameType(otherType);
	}

	@Override
	public IBinding getBinding() {
		return null; // TODO method;
	}

	@Override
	public IQObject getOwner() {
		return owner;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<String> getSignatures() {
		return signatures == null ? Collections.<String>emptyList() : signatures;
	}

	@Override
	public Long getRevision() {
		return revision;
	}

	@Override
	public String toString() {
		return kind.toString() + ' ' + signatures;
	}
}
