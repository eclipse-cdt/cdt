package org.eclipse.cdt.ui.tests;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.text.PartitionTokenScannerTest;
import org.eclipse.cdt.ui.tests.text.contentassist.*;
import org.eclipse.cdt.ui.tests.text.contentassist.failedtests.*;
import org.eclipse.cdt.ui.tests.textmanipulation.TextBufferTest;



/**
 * Test all areas of the UI.
 */
public class AutomatedSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new AutomatedSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public AutomatedSuite() {
		
		// Success Tests
		addTest(PartitionTokenScannerTest.suite());
		addTest(TextBufferTest.suite());
		// completion tests
		addTest(CompletionTest_FieldType_Prefix.suite());
		addTest(CompletionTest_FieldType_NoPrefix.suite());
		addTest(CompletionTest_FieldType_NoPrefix2.suite());
		addTest(CompletionTest_VariableType_Prefix.suite());
		addTest(CompletionTest_VariableType_NoPrefix.suite());
		addTest(CompletionTest_ArgumentType_NoPrefix_Bug50642.suite());
		addTest(CompletionTest_ArgumentType_NoPrefix2_Bug50642.suite());
		addTest(CompletionTest_ArgumentType_Prefix_Bug50642.suite());
		addTest(CompletionTest_ArgumentType_NoPrefix2_Bug50642.suite());
		addTest(CompletionTest_StatementStart_Prefix.suite());
		addTest(CompletionTest_StatementStart_NoPrefix.suite());
		addTest(CompletionTest_SingleName_Prefix.suite());
		addTest(CompletionTest_SingleName_Prefix2.suite());
		addTest(CompletionTest_SingleName_NoPrefix.suite());
		addTest(CompletionTest_MemberReference_Dot_Prefix.suite());
		addTest(CompletionTest_MemberReference_Dot_NoPrefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_Prefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_Prefix2.suite());
		addTest(CompletionTest_MemberReference_Arrow_NoPrefix.suite());
		
		// Failed Tests
		addTest(CompletionFailedTest_ScopedReference_NoPrefix_Bug50152.suite());
		addTest(CompletionFailedTest_ScopedReference_Prefix_Bug50152.suite());
		addTest(CompletionFailedTest_NamespaceRef_NoPrefix_Bug50471.suite());		
		addTest(CompletionFailedTest_NamespaceRef_Prefix_Bug50471.suite());		
		addTest(CompletionFailedTest_MacroRef_NoPrefix_Bug50487.suite());		
		addTest(CompletionFailedTest_MacroRef_Prefix_Bug50487.suite());
		addTest(CompletionFailedTest_ClassReference_NoPrefix_Bug50621.suite());
		addTest(CompletionFailedTest_ClassReference_Prefix_Bug50621.suite());
		addTest(CompletionFailedTest_ExceptionReference_NoPrefix_Bug50640.suite());
		addTest(CompletionFailedTest_ExceptionReference_Prefix_Bug50640.suite());
	}
	
}

