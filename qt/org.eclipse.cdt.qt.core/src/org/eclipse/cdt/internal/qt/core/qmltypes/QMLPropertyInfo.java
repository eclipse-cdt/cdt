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

import org.eclipse.cdt.qt.core.qmljs.IQmlObjectDefinition;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectMember;
import org.eclipse.cdt.qt.core.qmljs.IQmlPropertyBinding;

public class QMLPropertyInfo {
	static final String IDENTIFIER = "Property"; //$NON-NLS-1$

	static final String PROPERTY_NAME = "name"; //$NON-NLS-1$
	static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$
	static final String PROPERTY_READONLY = "isReadonly"; //$NON-NLS-1$
	static final String PROPERTY_POINTER = "isPointer"; //$NON-NLS-1$
	static final String PROPERTY_LIST = "isList"; //$NON-NLS-1$
	static final String PROPERTY_REVISION = "revision"; //$NON-NLS-1$

	private String name;
	private String type;
	private boolean readonly = false;
	private boolean pointer = false;
	private boolean list = false;
	private int revision;

	QMLPropertyInfo(QMLModelBuilder builder, IQmlObjectDefinition obj) {
		if (builder.ensureIdentifier(obj.getIdentifier(), IDENTIFIER)) {
			for (IQmlObjectMember member : obj.getBody().getMembers()) {
				if (builder.ensureNode(member, IQmlPropertyBinding.class)) {
					IQmlPropertyBinding prop = (IQmlPropertyBinding) member;
					switch (prop.getIdentifier().getName()) {
					case PROPERTY_NAME:
						this.name = builder.getStringBinding(prop);
						break;
					case PROPERTY_TYPE:
						this.type = builder.getStringBinding(prop);
						break;
					case PROPERTY_READONLY:
						this.readonly = builder.getBooleanBinding(prop);
						break;
					case PROPERTY_POINTER:
						this.pointer = builder.getBooleanBinding(prop);
						break;
					case PROPERTY_LIST:
						this.list = builder.getBooleanBinding(prop);
						break;
					case PROPERTY_REVISION:
						this.revision = builder.getIntegerBinding(prop);
						break;
					default:
					}
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getRevision() {
		return revision;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public boolean isPointer() {
		return pointer;
	}

	public boolean isList() {
		return list;
	}
}
