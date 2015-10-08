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
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectLiteralContext;
import org.junit.Assert;

public class ExpectedQmlObjectLiteral extends ExpectedQmlElement {
	private final String id;
	private final ExpectedQmlElement[] members;

	public ExpectedQmlObjectLiteral(String identifier, ExpectedQmlElement... expectedMembers) {
		this.id = identifier;
		this.members = expectedMembers;
	}

	@Override
	public void assertEquals(ParserRuleContext ctx, int index) {
		String baseMsg = getBaseMessage(ctx, index);

		QmlObjectLiteralContext objectLiteralCtx = null;
		if (ctx instanceof QmlObjectLiteralContext)
			objectLiteralCtx = (QmlObjectLiteralContext) ctx;
		else if (ctx instanceof QmlMemberContext)
			objectLiteralCtx = ((QmlMemberContext) ctx).qmlObjectLiteral();

		if (objectLiteralCtx == null)
			fail(baseMsg + " is not a Qml Object Literal"); //$NON-NLS-1$

		// Check the qualified id
		Assert.assertEquals(baseMsg + " has incorrect qualified id", id, objectLiteralCtx.qmlQualifiedId().getText()); //$NON-NLS-1$

		// Check members
		QMLParseTreeUtil.assertMembersEqual(objectLiteralCtx, members);
	}
}