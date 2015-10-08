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

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlMemberContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectLiteralContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectRootContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlProgramContext;

/**
 * Various utilities for testing a parse tree created by the <code>QMLParser</code>.
 */
public class QMLParseTreeUtil {

	public static QmlObjectLiteralContext getRootObjectLiteral(QmlProgramContext program) {
		if (program == null)
			fail("Qml program was null"); //$NON-NLS-1$

		QmlObjectRootContext rootObject = program.qmlObjectRoot();
		if (rootObject == null)
			fail("Unable to find Qml Object Root in parse tree"); //$NON-NLS-1$

		QmlObjectLiteralContext rootObjectLiteral = rootObject.qmlObjectLiteral();
		if (rootObjectLiteral == null)
			fail("Unable to find Qml Root Object Literal in parse tree"); //$NON-NLS-1$

		return rootObjectLiteral;
	}

	public static void assertEquals(ParserRuleContext actual, ExpectedQmlElement expected) {
		expected.assertEquals(actual, 0);
	}

	public static void assertMembersEqual(QmlObjectLiteralContext ctx, ExpectedQmlElement... expectedMembers) {
		List<QmlMemberContext> actualMembers = ctx.qmlMembers().qmlMember();
		for (int i = 0; i < expectedMembers.length; i++) {
			ExpectedQmlElement expected = expectedMembers[i];
			try {
				QmlMemberContext actual = actualMembers.get(i);
				expected.assertEquals(actual, i);
			} catch (IndexOutOfBoundsException e) {
				fail("['" + ctx.qmlQualifiedId().getText() + "'] Qml Member at index " + i + " was not found"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}
}
