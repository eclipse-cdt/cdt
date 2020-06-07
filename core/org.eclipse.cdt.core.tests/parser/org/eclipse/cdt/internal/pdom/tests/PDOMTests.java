/*******************************************************************************
 * Copyright (c) 2006, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *     Marc-Andre Laperle
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Doug Schaefer
 */
public class PDOMTests extends TestSuite {

	public static Test suite() {
		TestSuite suite = new PDOMTests();

		suite.addTest(DatabaseTest.suite());
		suite.addTest(DBPropertiesTest.suite());
		suite.addTest(PDOMCBugsTest.suite());
		suite.addTest(PDOMCPPBugsTest.suite());
		suite.addTest(PDOMSearchTest.suite());
		suite.addTest(PDOMLocationTest.suite());
		suite.addTest(PDOMNameTest.suite());
		suite.addTest(PDOMProviderTest.suite());
		suite.addTest(EnumerationTest.suite());
		suite.addTest(ClassTest.suite());
		suite.addTest(TypesTest.suite());
		suite.addTest(IncludesTest.suite());
		suite.addTest(OverloadsWithinSingleTUTest.suite());
		suite.addTest(OverloadsWithinCommonHeaderTest.suite());
		suite.addTest(BTreeTest.suite());
		suite.addTest(PDOMStringSetTest.suite());
		suite.addTest(PDOMTagIndexTest.suite());
		suite.addTest(FilesOnReindexTest.suite());
		suite.addTest(GeneratePDOMApplicationTest.suite());

		suite.addTest(CPPFieldTest.suite());
		suite.addTest(CPPFunctionTest.suite());
		suite.addTest(CPPVariableTest.suite());
		suite.addTest(CPPClassTemplateTest.suite());
		suite.addTest(CPPFunctionTemplateTest.suite());
		suite.addTest(MethodTest.suite());
		suite.addTest(NamespaceTest.suite());
		suite.addTest(ClassMemberVisibilityTest.suite());

		suite.addTest(CFunctionTest.suite());
		suite.addTest(CVariableTest.suite());
		suite.addTest(CCompositeTypeTest.suite());

		suite.addTest(DefDeclTest.suite());
		suite.addTest(RaceCondition157992Test.suite());
		suite.addTest(ChangeConfigurationTest.suite());

		return suite;
	}
}
