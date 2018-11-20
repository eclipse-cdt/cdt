/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.qmltypes;

import org.eclipse.cdt.qt.core.qmljs.IQmlObjectDefinition;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectMember;
import org.eclipse.cdt.qt.core.qmljs.IQmlPropertyBinding;

public class QMLParameterInfo {
	static final String IDENTIFIER = "Parameter"; //$NON-NLS-1$

	static final String PROPERTY_NAME = "name"; //$NON-NLS-1$
	static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$

	private String name;
	private String type;

	QMLParameterInfo(QMLModelBuilder builder, IQmlObjectDefinition obj) {
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
}
