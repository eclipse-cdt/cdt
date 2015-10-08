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
package org.eclipse.cdt.qt.core.qml.tests.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlAttributeContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectLiteralContext;

public abstract class ExpectedQmlElement {
	public abstract void assertEquals(ParserRuleContext context, int index);

	protected String getParentQualifiedId(ParserRuleContext ctx) {
		ParserRuleContext parent = ctx.getParent();
		while (parent != null) {
			if (parent instanceof QmlAttributeContext)
				return ((QmlAttributeContext) parent).qmlQualifiedId().getText();
			else if (parent instanceof QmlObjectLiteralContext)
				return ((QmlObjectLiteralContext) parent).qmlQualifiedId().getText();
			parent = parent.getParent();
		}
		return null;
	}

	protected String getBaseMessage(ParserRuleContext ctx, int index) {
		String parentId = getParentQualifiedId(ctx);
		if (parentId == null) {
			parentId = "QmlProgram"; //$NON-NLS-1$
		}
		return "['" + parentId + "':member #" + index + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
