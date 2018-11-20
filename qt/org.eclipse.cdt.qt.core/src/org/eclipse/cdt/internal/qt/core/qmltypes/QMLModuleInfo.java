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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.qt.core.qmljs.IQmlObjectDefinition;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectMember;
import org.eclipse.cdt.qt.core.qmljs.IQmlRootObject;

public class QMLModuleInfo {
	static final String IDENTIFIER = "Module"; //$NON-NLS-1$

	private List<QMLComponentInfo> componentsList = new ArrayList<>();

	QMLModuleInfo(QMLModelBuilder builder, IQmlRootObject obj) {
		if (builder.ensureIdentifier(obj.getIdentifier(), IDENTIFIER)) {
			for (IQmlObjectMember member : obj.getBody().getMembers()) {
				if (builder.ensureNode(member, IQmlObjectDefinition.class)) {
					componentsList.add(new QMLComponentInfo(builder, (IQmlObjectDefinition) member));
				}
			}
		}
		componentsList = Collections.unmodifiableList(componentsList);
	}

	public List<QMLComponentInfo> getComponents() {
		return componentsList;
	}
}
