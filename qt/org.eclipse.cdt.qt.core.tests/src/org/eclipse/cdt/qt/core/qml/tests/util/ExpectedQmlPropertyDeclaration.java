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

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlMemberContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlPropertyDeclarationContext;
import org.junit.Assert;

public class ExpectedQmlPropertyDeclaration extends ExpectedQmlElement {
	private final boolean readOnly;
	private final String id;
	private final String type;
	private final String value;

	public ExpectedQmlPropertyDeclaration(boolean readOnly, String qualifiedId, String declaredType, String value) {
		this.readOnly = readOnly;
		this.id = qualifiedId;
		this.type = declaredType;
		this.value = value;
	}

	@Override
	public void assertEquals(ParserRuleContext ctx, int index) {
		String baseMsg = getBaseMessage(ctx, index);

		if (!(ctx instanceof QmlMemberContext))
			fail(baseMsg + " is not a Qml Property Declaration"); //$NON-NLS-1$

		QmlPropertyDeclarationContext propertyDeclaration = ((QmlMemberContext) ctx).qmlPropertyDeclaration();
		if (propertyDeclaration == null)
			fail(baseMsg + " is not a Qml Property Declaration"); //$NON-NLS-1$

		// 'readonly'?
		boolean isPropertyReadonly = propertyDeclaration.READONLY() != null;
		Assert.assertEquals(baseMsg + " readonly is incorrect", readOnly, isPropertyReadonly); //$NON-NLS-1$

		// 'property' <qmlPropertyType>
		Assert.assertEquals(
				baseMsg + " has incorrect type", //$NON-NLS-1$
				type,
				propertyDeclaration.qmlPropertyType().getText());

		// <qmlIdentifier>
		Assert.assertEquals(
				baseMsg + " has incorrect identifier", //$NON-NLS-1$
				id,
				propertyDeclaration.qmlIdentifier().getText());

		// (':' <singleExpression>)?
		if (propertyDeclaration.singleExpression() != null) {
			Assert.assertEquals(
					baseMsg + " has incorrect value", //$NON-NLS-1$
					value,
					propertyDeclaration.singleExpression().getText());
		} else if (value != null) {
			Assert.assertEquals(
					baseMsg + " has incorrect value", //$NON-NLS-1$
					value,
					new String());
		}
	}
}