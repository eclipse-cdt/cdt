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
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectLiteralContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectRootContext;
import org.junit.Assert;

public class ExpectedQmlRootObjectLiteral extends ExpectedQmlElement {
	private final String id;
	private final ExpectedQmlElement[] members;

	public ExpectedQmlRootObjectLiteral(String identifier, ExpectedQmlElement... expectedMembers) {
		this.id = identifier;
		this.members = expectedMembers;
	}

	@Override
	public void assertEquals(ParserRuleContext ctx, int index) {
		String baseMsg = getBaseMessage(ctx, index);

		if (!(ctx instanceof QmlObjectRootContext))
			fail("Unable to find Root Object"); //$NON-NLS-1$

		QmlObjectLiteralContext objectLiteral = ((QmlObjectRootContext) ctx).qmlObjectLiteral();
		if (objectLiteral == null)
			fail(baseMsg + " is not a Qml Object Literal"); //$NON-NLS-1$

		// Check the qualified id
		Assert.assertEquals(baseMsg + " has incorrect qualified id", id, objectLiteral.qmlQualifiedId().getText()); //$NON-NLS-1$

		// Check members
		QMLParseTreeUtil.assertMembersEqual(objectLiteral, members);
	}
}