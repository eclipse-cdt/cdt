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

public class QProperty extends AbstractQField implements IQProperty {

	private String type;
	private final String[] values = new String[Attribute.values().length];

	public QProperty(IQObject owner, String type, String name) {
		super(owner);
		this.type = type;
		this.name = name;
	}

	public void setAttribute(IQProperty.Attribute attr, String value) {
		values[attr.ordinal()] = (value == null ? "" : value);
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue(Attribute attr) {
		return values[attr.ordinal()];
	}

	@Override
	public String getReadMethodName() {
		return Attribute.READ.valueIn(this);
	}

	@Override
	public String getWriteMethodName() {
		return Attribute.WRITE.valueIn(this);
	}

	@Override
	public String getResetMethodName() {
		return Attribute.RESET.valueIn(this);
	}

	@Override
	public String getNotifyMethodName() {
		return Attribute.NOTIFY.valueIn(this);
	}

	@Override
	public Long getRevision() {
		String revision = Attribute.REVISION.valueIn(this);
		if (revision != null)
			try {
				return Long.valueOf(revision);
			} catch (NumberFormatException e) {
				// This is a problem with the user's C++ code, there is no need to log this exception,
				// just ignore the value.
			}

		return null;
	}

	@Override
	public String getDesignable() {
		return Attribute.DESIGNABLE.valueIn(this);
	}

	@Override
	public String getScriptable() {
		return Attribute.SCRIPTABLE.valueIn(this);
	}

	@Override
	public String getStored() {
		return Attribute.STORED.valueIn(this);
	}

	@Override
	public String getUser() {
		return Attribute.USER.valueIn(this);
	}

	@Override
	public boolean isConstant() {
		return Attribute.CONSTANT.valueIn(this) != null;
	}

	@Override
	public boolean isFinal() {
		return Attribute.FINAL.valueIn(this) != null;
	}
}
