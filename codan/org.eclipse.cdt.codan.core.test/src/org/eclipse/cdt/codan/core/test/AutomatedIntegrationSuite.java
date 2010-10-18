/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.codan.core.internal.checkers.AssignmentInConditionCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.AssignmentToItselfCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.CatchByReferenceTest;
import org.eclipse.cdt.codan.core.internal.checkers.ReturnCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.ReturnStyleCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.StatementHasNoEffectCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.SuggestedParenthesisCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.SuspiciousSemicolonCheckerTest;
import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.SuggestedParenthesisQuickFixTest;

public class AutomatedIntegrationSuite extends TestSuite {
	public AutomatedIntegrationSuite() {
	}

	public AutomatedIntegrationSuite(Class theClass, String name) {
		super(theClass, name);
	}

	public AutomatedIntegrationSuite(Class theClass) {
		super(theClass);
	}

	public AutomatedIntegrationSuite(String name) {
		super(name);
	}

	public static Test suite() {
		final AutomatedIntegrationSuite suite = new AutomatedIntegrationSuite();
		// checkers
		suite.addTestSuite(StatementHasNoEffectCheckerTest.class);
		suite.addTestSuite(SuggestedParenthesisCheckerTest.class);
		suite.addTestSuite(ReturnCheckerTest.class);
		suite.addTestSuite(CatchByReferenceTest.class);
		suite.addTestSuite(AssignmentInConditionCheckerTest.class);
		suite.addTestSuite(AssignmentToItselfCheckerTest.class);
		suite.addTestSuite(ReturnStyleCheckerTest.class);
		suite.addTestSuite(SuspiciousSemicolonCheckerTest.class);
		// framework
		suite.addTest(CodanFastTestSuite.suite());
		// quick fixes
		suite.addTestSuite(SuggestedParenthesisQuickFixTest.class);
		return suite;
	}
}
