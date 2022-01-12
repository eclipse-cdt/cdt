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

import org.eclipse.cdt.internal.qt.core.index.IQObject.IMember;

public abstract class AbstractQField implements IQObject.IMember {

	private final IQObject owner;
	protected String name;

	protected AbstractQField(IQObject owner) {
		this.owner = owner;
	}

	@Override
	public IQObject getOwner() {
		return owner;
	}

	@Override
	public boolean isOverride(IMember member) {
		if (!AbstractQField.class.isAssignableFrom(member.getClass()))
			return false;

		// I haven't been able to find Qt documentation describing how things like
		// Q_PROPERY are overridden, but the docs suggest it is just by name.

		AbstractQField other = (AbstractQField) member;
		return name == null ? other.name == null : name.equals(other.name);
	}
}
