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
import java.util.List;

import org.eclipse.cdt.qt.core.qmljs.IQmlObjectDefinition;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectMember;
import org.eclipse.cdt.qt.core.qmljs.IQmlPropertyBinding;

public class QMLMethodInfo {
	static final String IDENTIFIER = "Method"; //$NON-NLS-1$

	static final String PROPERTY_NAME = "name"; //$NON-NLS-1$ s
	static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$
	static final String PROPERTY_REVISION = "revision"; //$NON-NLS-1$

	private String name;
	private String type;
	private int revision;
	private List<QMLParameterInfo> parameterList = new ArrayList<>();

	QMLMethodInfo(QMLModelBuilder builder, IQmlObjectDefinition obj) {
		if (builder.ensureIdentifier(obj.getIdentifier(), IDENTIFIER)) {
			for (IQmlObjectMember member : obj.getBody().getMembers()) {
				if (member instanceof IQmlPropertyBinding) {
					IQmlPropertyBinding prop = (IQmlPropertyBinding) member;
					switch (prop.getIdentifier().getName()) {
					case PROPERTY_NAME:
						this.name = builder.getStringBinding(prop);
						break;
					case PROPERTY_TYPE:
						this.type = builder.getStringBinding(prop);
						break;
					case PROPERTY_REVISION:
						this.revision = builder.getIntegerBinding(prop);
						break;
					default:
					}
				} else if (member instanceof IQmlObjectDefinition) {
					this.parameterList.add(new QMLParameterInfo(builder, (IQmlObjectDefinition) member));
				} else {
					builder.unexpectedNode(member);
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
}
