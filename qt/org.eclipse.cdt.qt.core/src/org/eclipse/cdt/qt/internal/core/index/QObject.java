/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.qt.core.index.IQEnum;
import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.internal.core.pdom.QtPDOMQEnum;
import org.eclipse.cdt.qt.internal.core.pdom.QtPDOMQObject;
import org.eclipse.core.runtime.CoreException;

public class QObject implements IQObject {

	private final String name;
	private final QtPDOMQObject pdomQObject;
	private final List<IQObject> bases;
	private final List<IQEnum> enums;

	public QObject(QtIndexImpl qtIndex, CDTIndex cdtIndex, QtPDOMQObject pdomQObject) throws CoreException {
		this.name = pdomQObject.getName();
		this.pdomQObject = pdomQObject;

		this.bases = new ArrayList<IQObject>();
		for(QtPDOMQObject base : pdomQObject.findBases())
			this.bases.add(new QObject(qtIndex, cdtIndex, base));

		this.enums = new ArrayList<IQEnum>();
		for(IField field : pdomQObject.getFields())
			if (field instanceof QtPDOMQEnum) {
				QtPDOMQEnum qEnum = (QtPDOMQEnum) field;
				this.enums.add(new QEnum(field.getName(), qEnum.isFlag(), qEnum.getEnumerators()));
			}
	}

	@Override
	public IBinding getBinding() {
		return pdomQObject;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<IQObject> getBases() {
		return bases;
	}

	@Override
	public Collection<IQEnum> getEnums() {
		return enums;
	}
}
