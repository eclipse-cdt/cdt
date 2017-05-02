/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.tests;

import org.eclipse.cdt.codan.core.internal.checkers.AbstractClassInstantiationCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.AssignmentInConditionCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.AssignmentToItselfCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.CaseBreakCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.CatchByReferenceTest;
import org.eclipse.cdt.codan.core.internal.checkers.ClassMembersInitializationCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.CommentCheckerLineTests;
import org.eclipse.cdt.codan.core.internal.checkers.CommentCheckerNestedTests;
import org.eclipse.cdt.codan.core.internal.checkers.FormatStringCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.NonVirtualDestructorCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.ProblemBindingCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.ReturnCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.ReturnStyleCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.StatementHasNoEffectCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.SuggestedParenthesisCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.SuspiciousSemicolonCheckerTest;
import org.eclipse.cdt.codan.core.internal.checkers.UnusedSymbolInFileScopeCheckerTest;
import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.AssignmentInConditionQuickFixTest;
import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.CaseBreakQuickFixBreakTest;
import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.CaseBreakQuickFixCommentTest;
import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.CaseBreakQuickFixFallthroughAttributeTest;
import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.CatchByReferenceQuickFixTest;
import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.CreateLocalVariableQuickFixTest;
import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.SuggestedParenthesisQuickFixTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AutomatedIntegrationSuite extends TestSuite {
	public AutomatedIntegrationSuite() {
	}

	public AutomatedIntegrationSuite(Class<? extends TestCase> theClass, String name) {
		super(theClass, name);
	}

	public AutomatedIntegrationSuite(Class<? extends TestCase> theClass) {
		super(theClass);
	}

	public AutomatedIntegrationSuite(String name) {
		super(name);
	}

	public static Test suite() {
		final AutomatedIntegrationSuite suite = new AutomatedIntegrationSuite();
		// checkers
		suite.addTestSuite(AbstractClassInstantiationCheckerTest.class);
		suite.addTestSuite(AssignmentInConditionCheckerTest.class);
		suite.addTestSuite(AssignmentToItselfCheckerTest.class);
		suite.addTestSuite(CaseBreakCheckerTest.class);
		suite.addTestSuite(CatchByReferenceTest.class);
		suite.addTestSuite(ClassMembersInitializationCheckerTest.class);
		suite.addTestSuite(FormatStringCheckerTest.class);
		suite.addTestSuite(NonVirtualDestructorCheckerTest.class);
		suite.addTestSuite(ProblemBindingCheckerTest.class);
		suite.addTestSuite(ReturnCheckerTest.class);
		suite.addTestSuite(ReturnStyleCheckerTest.class);
		suite.addTestSuite(StatementHasNoEffectCheckerTest.class);
		suite.addTestSuite(SuggestedParenthesisCheckerTest.class);
		suite.addTestSuite(SuspiciousSemicolonCheckerTest.class);
		suite.addTestSuite(UnusedSymbolInFileScopeCheckerTest.class);
		suite.addTestSuite(CommentCheckerLineTests.class);
		suite.addTestSuite(CommentCheckerNestedTests.class);
		// framework
		suite.addTest(CodanFastTestSuite.suite());
		// quick fixes
		suite.addTestSuite(CreateLocalVariableQuickFixTest.class);
		suite.addTestSuite(SuggestedParenthesisQuickFixTest.class);
		suite.addTestSuite(CatchByReferenceQuickFixTest.class);
		suite.addTestSuite(CaseBreakQuickFixBreakTest.class);
		suite.addTestSuite(CaseBreakQuickFixCommentTest.class);
		suite.addTestSuite(CaseBreakQuickFixFallthroughAttributeTest.class);
		suite.addTestSuite(AssignmentInConditionQuickFixTest.class);
		return suite;
	}
}
