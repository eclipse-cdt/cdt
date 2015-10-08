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
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlHeaderItemContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlProgramContext;

public class ExpectedQmlProgram extends ExpectedQmlElement {

	private final ExpectedQmlElement[] expectedElements;

	public ExpectedQmlProgram(ExpectedQmlElement... expectedElements) {
		this.expectedElements = expectedElements;
	}

	@Override
	public void assertEquals(ParserRuleContext context, int index) {
		if (!(context instanceof QmlProgramContext))
			fail("Qml Program was null"); //$NON-NLS-1$

		QmlProgramContext program = (QmlProgramContext) context;
		int i = 0;

		// Check the header items
		for (QmlHeaderItemContext headerItem : program.qmlHeaderItem()) {
			expectedElements[i].assertEquals(headerItem, i);
			i++;
		}

		// check the root object
		if (program.qmlObjectRoot().qmlObjectLiteral() != null)
			expectedElements[i].assertEquals(program.qmlObjectRoot(), i);
	}

}
