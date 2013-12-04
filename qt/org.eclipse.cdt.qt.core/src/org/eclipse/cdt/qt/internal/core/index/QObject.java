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
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.qt.core.index.IQEnum;
import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.IQProperty;
import org.eclipse.cdt.qt.internal.core.pdom.QtPDOMProperty;
import org.eclipse.cdt.qt.internal.core.pdom.QtPDOMQEnum;
import org.eclipse.cdt.qt.internal.core.pdom.QtPDOMQObject;
import org.eclipse.core.runtime.CoreException;

public class QObject implements IQObject {

	private final String name;
	private final QtPDOMQObject pdomQObject;
	private final List<IQObject> bases;
	private final IQObject.IMembers<IQProperty> properties;
	private final List<IQEnum> enums;
	private final Map<String, String> classInfos;

	public QObject(QtIndexImpl qtIndex, CDTIndex cdtIndex, QtPDOMQObject pdomQObject) throws CoreException {
		this.name = pdomQObject.getName();
		this.pdomQObject = pdomQObject;

		List<IQProperty> baseProps = new ArrayList<IQProperty>();

		this.bases = new ArrayList<IQObject>();
		for(QtPDOMQObject base : pdomQObject.findBases()) {
			QObject baseQObj = new QObject(qtIndex, cdtIndex, base);
			this.bases.add(baseQObj);
			baseProps.addAll(baseQObj.getProperties().all());
		}

		this.classInfos = pdomQObject.getClassInfos();

		this.enums = new ArrayList<IQEnum>();
		for(QtPDOMQEnum pdom : pdomQObject.getFields(QtPDOMQEnum.class))
			this.enums.add(new QEnum(pdom.getName(), pdom.isFlag(), pdom.getEnumerators()));

		List<IQProperty> props = new ArrayList<IQProperty>();
		for(QtPDOMProperty pdom : pdomQObject.getFields(QtPDOMProperty.class)) {
			QProperty qProp = new QProperty(this, pdom.getTypeStr(), pdom.getName());
			for(QtPDOMProperty.Attribute attr : pdom.getAttributes())
				qProp.setAttribute(attr.attr, attr.value);
			props.add(qProp);
		}
		this.properties = QObjectMembers.create(props, baseProps);
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
	public IQObject.IMembers<IQProperty> getProperties() {
		return properties;
	}

	@Override
	public String getClassInfo(String key) {
		String value = classInfos.get(key);
		if (value != null)
			return value;

		for(IQObject base : bases) {
			value = base.getClassInfo(key);
			if (value != null)
				return value;
		}

		return null;
	}

	@Override
	public Collection<IQEnum> getEnums() {
		return enums;
	}
}
