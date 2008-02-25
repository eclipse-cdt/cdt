/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Norbert Ploett (Siemens AG)
 *******************************************************************************/
/*
 * Created on May 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.core.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.cdescriptor.tests.CDescriptorTests;
import org.eclipse.cdt.core.internal.errorparsers.tests.ErrorParserTests;
import org.eclipse.cdt.core.internal.tests.PositionTrackerTests;
import org.eclipse.cdt.core.internal.tests.StringBuilderTest;
import org.eclipse.cdt.core.language.AllLanguageTests;
import org.eclipse.cdt.core.model.tests.AllCoreTests;
import org.eclipse.cdt.core.model.tests.BinaryTests;
import org.eclipse.cdt.core.model.tests.ElementDeltaTests;
import org.eclipse.cdt.core.model.tests.WorkingCopyTests;
import org.eclipse.cdt.core.parser.tests.ParserTestSuite;
import org.eclipse.cdt.core.tests.templateengine.AllTemplateEngineTests;
import org.eclipse.cdt.internal.index.tests.IndexTests;
import org.eclipse.cdt.internal.pdom.tests.PDOMTests;

/**
 * @author vhirsl
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AutomatedIntegrationSuite extends TestSuite {

	public AutomatedIntegrationSuite() {}
	
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
		
		// Add all success tests
		suite.addTest(CDescriptorTests.suite());
		suite.addTest(ErrorParserTests.suite());
		suite.addTest(ParserTestSuite.suite());
		suite.addTest(AllCoreTests.suite());
		suite.addTest(BinaryTests.suite());
		suite.addTest(ElementDeltaTests.suite());
		suite.addTest(WorkingCopyTests.suite());
        suite.addTest(PositionTrackerTests.suite());
        suite.addTest(StringBuilderTest.suite());
        suite.addTest(AllLanguageTests.suite());
				
		// Add in PDOM tests
		suite.addTest(PDOMTests.suite());
		suite.addTest(IndexTests.suite());
	
        suite.addTest(AllTemplateEngineTests.suite());

		return suite;
	}
	
}
