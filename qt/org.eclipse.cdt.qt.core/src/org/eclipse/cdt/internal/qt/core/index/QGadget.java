/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.internal.qt.core.pdom.AbstractQtPDOMClass;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQEnum;
import org.eclipse.core.runtime.CoreException;

public class QGadget implements IQGadget {

	private final String name;
	private final List<IQEnum> enums;

	public QGadget(QtIndexImpl qtIndex, CDTIndex cdtIndex, AbstractQtPDOMClass pdomQGadget) throws CoreException {
		this.name = pdomQGadget.getName();

		this.enums = new ArrayList<>();
		for (QtPDOMQEnum pdom : pdomQGadget.getChildren(QtPDOMQEnum.class))
			this.enums.add(new QEnum(pdom.getName(), pdom.isFlag(), pdom.getEnumerators()));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<IQEnum> getEnums() {
		return enums;
	}
}
