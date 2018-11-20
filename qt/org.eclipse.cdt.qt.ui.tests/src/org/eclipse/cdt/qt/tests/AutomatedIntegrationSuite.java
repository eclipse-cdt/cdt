/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.qt.tests;

import org.eclipse.cdt.qt.pro.parser.tests.QtProjectFileModifierTest;
import org.eclipse.cdt.qt.pro.parser.tests.QtProjectFileParserTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomatedIntegrationSuite extends TestSuite {

	public static Test suite() throws Exception {
		return new TestSuite(ASTUtilTests.class, QMakeTests.class, QGadgetTests.class, QObjectTests.class,
				QtContentAssistantTests.class, QtIndexTests.class, QtRegressionTests.class, QmlRegistrationTests.class,
				QtProjectFileModifierTest.class, QtProjectFileParserTest.class);
	}
}
