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
		suite.addTest(DBPropertiesTests.suite());
		suite.addTest(PDOMCBugsTest.suite());
		suite.addTest(PDOMCPPBugsTest.suite());
		suite.addTest(PDOMSearchTest.suite());
		suite.addTest(PDOMLocationTests.suite());
		suite.addTest(PDOMNameTests.suite());
		suite.addTest(PDOMProviderTests.suite());
		suite.addTest(EnumerationTests.suite());
		suite.addTest(ClassTests.suite());
		suite.addTest(TypesTests.suite());
		suite.addTest(IncludesTests.suite());
		suite.addTest(OverloadsWithinSingleTUTests.suite());
		suite.addTest(OverloadsWithinCommonHeaderTests.suite());
		suite.addTest(BTreeTests.suite());
		suite.addTest(PDOMStringSetTests.suite());
		suite.addTest(PDOMTagIndexTests.suite());
		suite.addTest(FilesOnReindexTests.suite());
		suite.addTest(GeneratePDOMApplicationTest.suite());

		suite.addTest(CPPFieldTests.suite());
		suite.addTest(CPPFunctionTests.suite());
		suite.addTest(CPPVariableTests.suite());
		suite.addTest(CPPClassTemplateTests.suite());
		suite.addTest(CPPFunctionTemplateTests.suite());
		suite.addTest(MethodTests.suite());
		suite.addTest(NamespaceTests.suite());
		suite.addTest(ClassMemberVisibilityTests.suite());

		suite.addTest(CFunctionTests.suite());
		suite.addTest(CVariableTests.suite());
		suite.addTest(CCompositeTypeTests.suite());

		suite.addTest(DefDeclTests.suite());
		suite.addTest(RaceCondition157992Test.suite());
		suite.addTest(ChangeConfigurationTests.suite());

		return suite;
	}
}
