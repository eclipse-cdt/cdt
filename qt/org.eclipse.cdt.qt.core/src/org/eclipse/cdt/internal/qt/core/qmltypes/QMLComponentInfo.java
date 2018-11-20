/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.qmltypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.qt.core.qmljs.IQmlObjectDefinition;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectMember;
import org.eclipse.cdt.qt.core.qmljs.IQmlPropertyBinding;

public class QMLComponentInfo {
	static final String IDENTIFIER = "Component"; //$NON-NLS-1$

	static final String PROPERTY_NAME = "name"; //$NON-NLS-1$
	static final String PROPERTY_PROTOTYPE = "prototype"; //$NON-NLS-1$
	static final String PROPERTY_DEF_PROPERTY = "defaultProperty"; //$NON-NLS-1$
	static final String PROPERTY_ATTACHED_TYPE = "attachedType"; //$NON-NLS-1$
	static final String PROPERTY_EXPORTS = "exports"; //$NON-NLS-1$
	static final String PROPERTY_EXPORT_REVISIONS = "exportMetaObjectRevisions"; //$NON-NLS-1$

	private String name;
	private String prototype;
	private String defaultProperty;
	private String attachedType;
	private Integer[] exportMetaObjectRevisions;
	private List<QMLExportInfo> exportList = new ArrayList<>();
	private List<QMLPropertyInfo> propertyList = new ArrayList<>();
	private List<QMLMethodInfo> methodList = new ArrayList<>();
	private List<QMLSignalInfo> signalList = new ArrayList<>();
	private List<QMLEnumInfo> enumList = new ArrayList<>();

	protected QMLComponentInfo(QMLModelBuilder builder, IQmlObjectDefinition obj) {
		builder.ensureIdentifier(obj.getIdentifier(), IDENTIFIER);
		for (IQmlObjectMember member : obj.getBody().getMembers()) {
			if (member instanceof IQmlPropertyBinding) {
				IQmlPropertyBinding prop = (IQmlPropertyBinding) member;
				switch (prop.getIdentifier().getName()) {
				case PROPERTY_NAME:
					this.name = builder.getStringBinding(prop);
					break;
				case PROPERTY_PROTOTYPE:
					this.prototype = builder.getStringBinding(prop);
					break;
				case PROPERTY_DEF_PROPERTY:
					this.defaultProperty = builder.getStringBinding(prop);
					break;
				case PROPERTY_ATTACHED_TYPE:
					this.attachedType = builder.getStringBinding(prop);
					break;
				case PROPERTY_EXPORTS:
					String[] exports = builder.getStringArrayBinding(prop);
					for (String exp : exports) {
						this.exportList.add(new QMLExportInfo(builder, exp));
					}
					break;
				case PROPERTY_EXPORT_REVISIONS:
					this.exportMetaObjectRevisions = builder.getIntegerArrayBinding(prop);
					break;
				default:
				}
			} else if (member instanceof IQmlObjectDefinition) {
				IQmlObjectDefinition object = (IQmlObjectDefinition) member;
				switch (object.getIdentifier().getName()) {
				case QMLPropertyInfo.IDENTIFIER:
					this.propertyList.add(new QMLPropertyInfo(builder, object));
					break;
				case QMLMethodInfo.IDENTIFIER:
					this.methodList.add(new QMLMethodInfo(builder, object));
					break;
				case QMLSignalInfo.IDENTIFIER:
					this.signalList.add(new QMLSignalInfo(builder, object));
					break;
				case QMLEnumInfo.IDENTIFIER:
					this.enumList.add(new QMLEnumInfo(builder, object));
					break;
				default:
				}
			} else {
				builder.unexpectedNode(member);
			}
		}
		exportList = Collections.unmodifiableList(exportList);
		propertyList = Collections.unmodifiableList(propertyList);
		methodList = Collections.unmodifiableList(methodList);
		signalList = Collections.unmodifiableList(signalList);
		enumList = Collections.unmodifiableList(enumList);
	}

	public String getName() {
		return name;
	}

	public String getPrototype() {
		return prototype;
	}

	public String getDefaultProperty() {
		return defaultProperty;
	}

	public String getAttachedType() {
		return attachedType;
	}

	public List<QMLExportInfo> getExports() {
		return exportList;
	}

	public Integer[] getExportMetaObjectRevisions() {
		return Arrays.copyOf(exportMetaObjectRevisions, exportMetaObjectRevisions.length);
	}

	public List<QMLPropertyInfo> getProperties() {
		return propertyList;
	}

	public List<QMLMethodInfo> getMethods() {
		return methodList;
	}

	public List<QMLSignalInfo> getSignals() {
		return signalList;
	}

	public List<QMLEnumInfo> getEnums() {
		return enumList;
	}
}
