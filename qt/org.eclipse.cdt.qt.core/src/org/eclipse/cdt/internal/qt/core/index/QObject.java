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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMProperty;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQEnum;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQMethod;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQObject;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQmlRegistration;
import org.eclipse.core.runtime.CoreException;

public class QObject implements IQObject {

	private final String name;
	private final QtPDOMQObject pdomQObject;
	private final List<IQObject> bases;
	private final IQObject.IMembers<IQMethod> slots;
	private final IQObject.IMembers<IQMethod> signals;
	private final IQObject.IMembers<IQMethod> invokables;
	private final IQObject.IMembers<IQProperty> properties;
	private final List<IQmlRegistration> qmlRegistrations;
	private final List<IQEnum> enums;
	private final Map<String, String> classInfos;

	public QObject(QtIndexImpl qtIndex, CDTIndex cdtIndex, QtPDOMQObject pdomQObject) throws CoreException {
		this.name = pdomQObject.getName();
		this.pdomQObject = pdomQObject;

		List<IQMethod> baseSlots = new ArrayList<>();
		List<IQMethod> baseSignals = new ArrayList<>();
		List<IQMethod> baseInvokables = new ArrayList<>();
		List<IQProperty> baseProps = new ArrayList<>();

		this.bases = new ArrayList<>();
		for (QtPDOMQObject base : pdomQObject.findBases()) {
			QObject baseQObj = new QObject(qtIndex, cdtIndex, base);
			this.bases.add(baseQObj);
			baseSlots.addAll(baseQObj.getSlots().all());
			baseSignals.addAll(baseQObj.getSignals().all());
			baseInvokables.addAll(baseQObj.getInvokables().all());
			baseProps.addAll(baseQObj.getProperties().all());
		}

		this.classInfos = pdomQObject.getClassInfos();

		List<IQMethod> slots = new ArrayList<>();
		List<IQMethod> signals = new ArrayList<>();
		List<IQMethod> invokables = new ArrayList<>();
		for (QtPDOMQMethod pdom : pdomQObject.getChildren(QtPDOMQMethod.class))
			switch (pdom.getKind()) {
			case Slot:
				slots.add(new QMethod(this, pdom));
				break;
			case Signal:
				signals.add(new QMethod(this, pdom));
				break;
			case Invokable:
				invokables.add(new QMethod(this, pdom));
				break;
			case Unspecified:
				break;
			}

		this.slots = QObjectMembers.create(slots, baseSlots);
		this.signals = QObjectMembers.create(signals, baseSignals);
		this.invokables = QObjectMembers.create(invokables, baseInvokables);

		this.enums = new ArrayList<>();
		for (QtPDOMQEnum pdom : pdomQObject.getChildren(QtPDOMQEnum.class))
			this.enums.add(new QEnum(pdom.getName(), pdom.isFlag(), pdom.getEnumerators()));

		List<IQProperty> props = new ArrayList<>();
		for (QtPDOMProperty pdom : pdomQObject.getChildren(QtPDOMProperty.class)) {
			QProperty qProp = new QProperty(this, pdom.getType(), pdom.getName());
			for (QtPDOMProperty.Attribute attr : pdom.getAttributes())
				qProp.setAttribute(attr.attr, attr.value);
			props.add(qProp);
		}
		this.properties = QObjectMembers.create(props, baseProps);

		this.qmlRegistrations = new ArrayList<>();
		for (QtPDOMQmlRegistration pdom : QtPDOMQmlRegistration.findFor(pdomQObject))
			this.qmlRegistrations.add(QmlRegistration.create(qtIndex, pdom));
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
	public IMembers<IQMethod> getSlots() {
		return slots;
	}

	@Override
	public IMembers<IQMethod> getSignals() {
		return signals;
	}

	@Override
	public IMembers<IQMethod> getInvokables() {
		return invokables;
	}

	@Override
	public IQObject.IMembers<IQProperty> getProperties() {
		return properties;
	}

	@Override
	public Collection<IQmlRegistration> getQmlRegistrations() {
		return qmlRegistrations;
	}

	@Override
	public String getClassInfo(String key) {
		String value = classInfos.get(key);
		if (value != null)
			return value;

		for (IQObject base : bases) {
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
