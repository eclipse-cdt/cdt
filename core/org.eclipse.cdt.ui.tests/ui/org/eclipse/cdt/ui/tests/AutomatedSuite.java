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
		addTest(CompletionTest_ArgumentType_NoPrefix.suite());
		addTest(CompletionTest_ArgumentType_NoPrefix2.suite());
		addTest(CompletionTest_ArgumentType_Prefix.suite());
		addTest(CompletionTest_ArgumentType_NoPrefix2.suite());
		addTest(CompletionTest_SingleName_Method_Prefix.suite());
		addTest(CompletionTest_SingleName_Method_NoPrefix.suite());
		addTest(CompletionTest_SingleName_Prefix.suite());
		addTest(CompletionTest_SingleName_Prefix2.suite());
		addTest(CompletionTest_SingleName_NoPrefix.suite());
		addTest(CompletionTest_MemberReference_Dot_Prefix.suite());
		addTest(CompletionTest_MemberReference_Dot_NoPrefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_Prefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_Prefix2.suite());
		addTest(CompletionTest_MemberReference_Arrow_NoPrefix.suite());
		addTest(CompletionTest_NamespaceRef_Prefix.suite());
		addTest(CompletionTest_NamespaceRef_NoPrefix.suite());
		addTest(CompletionTest_TypeRef_NoPrefix.suite());		
		addTest(CompletionTest_TypeRef_Prefix.suite());		
		addTest(CompletionTest_ClassReference_NoPrefix.suite());
		addTest(CompletionTest_ClassReference_Prefix.suite());
		addTest(CompletionTest_NewTypeReference_NoPrefix.suite());
		addTest(CompletionTest_NewTypeReference_Prefix.suite());
		addTest(CompletionTest_ExceptionReference_NoPrefix.suite());
		addTest(CompletionTest_ExceptionReference_Prefix.suite());
		addTest(CompletionTest_SingleName_Parameter.suite());
		
		// Failed Tests
		addTest(CompletionFailedTest_ScopedReference_NoPrefix_Bug50152.suite());
		addTest(CompletionFailedTest_ScopedReference_Prefix_Bug50152.suite());
		addTest(CompletionFailedTest_MacroRef_NoPrefix_Bug50487.suite());		
		addTest(CompletionFailedTest_MacroRef_Prefix_Bug50487.suite());
		addTest(CompletionFailedTest_FunctionReference_Bug50807.suite());
		addTest(CompletionFailedTest_ConstructorReference_Bug50808.suite());
	}
	
}

