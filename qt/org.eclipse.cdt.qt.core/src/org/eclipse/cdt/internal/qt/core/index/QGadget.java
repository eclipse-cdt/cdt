/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQEnum;
import org.eclipse.cdt.internal.qt.core.pdom.AbstractQtPDOMClass;
import org.eclipse.core.runtime.CoreException;

public class QGadget implements IQGadget {

	private final String name;
	private final List<IQEnum> enums;

	public QGadget(QtIndexImpl qtIndex, CDTIndex cdtIndex, AbstractQtPDOMClass pdomQGadget) throws CoreException {
		this.name = pdomQGadget.getName();

		this.enums = new ArrayList<IQEnum>();
		for(QtPDOMQEnum pdom : pdomQGadget.getChildren(QtPDOMQEnum.class))
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
