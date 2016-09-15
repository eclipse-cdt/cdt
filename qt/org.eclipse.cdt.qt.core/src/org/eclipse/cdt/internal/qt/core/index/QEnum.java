/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IValue;

public class QEnum implements IQEnum {

	private final String name;
	private final boolean isFlag;
	private final List<IQEnum.Enumerator> enumerators;

	public QEnum(String name, boolean isFlag, List<IEnumerator> enumerators) {
		this.name = name;
		this.isFlag = isFlag;
		this.enumerators = new ArrayList<IQEnum.Enumerator>(enumerators.size());
		for (IEnumerator enumerator : enumerators)
			this.enumerators.add(new Enumerator(enumerator));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isFlag() {
		return isFlag;
	}

	@Override
	public Collection<IQEnum.Enumerator> getEnumerators() {
		return enumerators;
	}

	private static class Enumerator implements IQEnum.Enumerator {

		private final String name;
		private final Long ordinal;

		public Enumerator(IEnumerator enumerator) {
			this.name = enumerator.getName();

			IValue val = enumerator.getValue();
			this.ordinal = val == null ? null : val.numberValue().longValue();
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Long getOrdinal() {
			return ordinal;
		}
	}
}
