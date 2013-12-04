/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.index;

import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.IQObject.IMember;

public abstract class AbstractQField implements IQObject.IMember {

	private final IQObject owner;
	protected String name;

	/**
	 * Scan the given field and extracts the strings defining the attributes of the
	 * field.  Returns false if the expansion parameter, does not represent a Q_PROPERTY,
	 * does not have related information, or if the information does not match the
	 * expected format.
	 */
	protected abstract boolean scanDefn(String expansionParam);

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

		// I haven't been able to find Qt documentation describing how Q_PROPERY is overridden,
		// but the docs suggest it is just by name.

		AbstractQField other = (AbstractQField) member;
		return name == null ? other.name == null : name.equals(other.name);
	}
}
