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
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlAttributeContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlMemberContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectLiteralContext;
import org.junit.Assert;

public class ExpectedQmlAttribute extends ExpectedQmlElement {

	private final String id;
	private final String value;
	private final ExpectedQmlElement[] expectedQmlElements;

	public ExpectedQmlAttribute(String qualifiedId, String value) {
		this.id = qualifiedId;
		this.value = value;
		this.expectedQmlElements = null;
	}

	public ExpectedQmlAttribute(String qualifiedId, ExpectedQmlElement... expectedElements) {
		this.id = qualifiedId;
		this.value = null;
		this.expectedQmlElements = expectedElements;
	}

	@Override
	public void assertEquals(ParserRuleContext ctx, int index) {
		String baseMsg = getBaseMessage(ctx, index);

		if (!(ctx instanceof QmlMemberContext))
			fail(baseMsg + " is not a Qml Attribute"); //$NON-NLS-1$

		QmlAttributeContext attribute = ((QmlMemberContext) ctx).qmlAttribute();
		if (attribute == null)
			fail(baseMsg + " is not a Qml Attribute"); //$NON-NLS-1$

		// <qmlQualifiedId>
		Assert.assertEquals(
				baseMsg + " has incorrect qualified id", //$NON-NLS-1$
				id,
				attribute.qmlQualifiedId().getText());

		// ':' (<singleExpression>|<qmlObjectLiteral>|<qmlMembers>)
		if (expectedQmlElements != null) {
			assertMembersEqual(attribute, expectedQmlElements, baseMsg);
		} else {
			if (attribute.singleExpression() != null) {
				Assert.assertEquals(
						baseMsg + " has incorrect value", //$NON-NLS-1$
						value,
						attribute.singleExpression().getText());
			} else if (attribute.qmlMembers() != null) {
				Assert.assertEquals(
						baseMsg + " has incorrect value", //$NON-NLS-1$
						value,
						attribute.qmlMembers().getText());
			} else if (value != null) {
				Assert.assertEquals(
						baseMsg + " has incorrect value", //$NON-NLS-1$
						value,
						new String());
			}
		}
	}

	private void assertMembersEqual(QmlAttributeContext context, ExpectedQmlElement[] expectedElements, String baseMsg) {
		int index = 0;

		QmlObjectLiteralContext objectLiteral = context.qmlObjectLiteral();
		if (objectLiteral != null) {
			expectedElements[index].assertEquals(objectLiteral, index);
			index++;
		}

		if (context.qmlMembers() != null) {
			for (QmlMemberContext member : context.qmlMembers().qmlMember()) {
				expectedElements[index].assertEquals(member, index);
				index++;
			}
		}

		if (index < expectedElements.length - 1) {
			fail(baseMsg + " not enough members to check against expected elements"); //$NON-NLS-1$
		}
	}
}