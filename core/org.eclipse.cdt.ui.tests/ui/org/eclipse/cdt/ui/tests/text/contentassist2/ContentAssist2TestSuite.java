/*******************************************************************************
 * Copyright (c) 2006, 2007 Siemens AG and others.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Norbert Ploett - Initial implementation
 * Bryan Wilkinson (QNX)
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This suite bundles all tests for the CContentAssistProcessor
 */
public class ContentAssist2TestSuite extends TestSuite {

	public static Test suite()  {
		return new ContentAssist2TestSuite() ;
	}
	
	public ContentAssist2TestSuite()  {
		super(ContentAssist2TestSuite.class.getName());
		
		addTest(CompletionTest_AnonymousTypes.suite());
		addTest(CompletionTest_ArgumentType_Prefix.suite());
		addTest(CompletionTest_ArgumentType_Prefix2.suite());
		addTest(CompletionTest_ClassReference_NoPrefix.suite());
		addTest(CompletionTest_ClassReference_Prefix.suite());
		addTest(CompletionTest_ConstructorReference.suite());
		addTest(CompletionTest_ExceptionReference_NoPrefix.suite());
		addTest(CompletionTest_ExceptionReference_Prefix.suite());
		addTest(CompletionTest_FieldType_NoPrefix.suite());
		addTest(CompletionTest_FieldType_NoPrefix2.suite());
		addTest(CompletionTest_FieldType_Prefix.suite());
		addTest(CompletionTest_FunctionReference_Prefix.suite());
		addTest(CompletionTest_MacroRef_NoPrefix.suite());
		addTest(CompletionTest_MacroRef_Prefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_NoPrefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_NoPrefix2.suite());
		addTest(CompletionTest_MemberReference_Arrow_NoPrefix3.suite());
		addTest(CompletionTest_MemberReference_Arrow_Prefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_Prefix2.suite());
		addTest(CompletionTest_MemberReference_Dot_NoPrefix.suite());
		addTest(CompletionTest_MemberReference_Dot_Prefix.suite());
		addTest(CompletionTest_NamespaceRef_NoPrefix.suite());
		addTest(CompletionTest_NamespaceRef_Prefix.suite());
		addTest(CompletionTest_NewTypeReference_NoPrefix.suite());
		addTest(CompletionTest_NewTypeReference_Prefix.suite());
		addTest(CompletionTest_ScopedReference_NonCodeScope.suite());
		addTest(CompletionTest_ScopedReference_NoPrefix.suite());
		addTest(CompletionTest_ScopedReference_Prefix.suite());
		addTest(CompletionTest_SingleName_Method_NoPrefix.suite());
		addTest(CompletionTest_SingleName_Method_Prefix.suite());
		addTest(CompletionTest_SingleName_NoPrefix.suite());
		addTest(CompletionTest_SingleName_Prefix.suite());
		addTest(CompletionTest_SingleName_Prefix2.suite());
		addTest(CompletionTest_TypeDef_NoPrefix.suite());
		addTest(CompletionTest_TypeDef_Prefix.suite());
		addTest(CompletionTest_TypeRef_NoPrefix.suite());
		addTest(CompletionTest_TypeRef_Prefix.suite());
		addTest(CompletionTest_VariableType_NestedPrefix.suite());
		addTest(CompletionTest_VariableType_NoPrefix.suite());
		addTest(CompletionTest_VariableType_Prefix.suite());
		
		addTest(CompletionTests.suite());
		addTest(CompletionTests_PlainC.suite());
		addTest(ParameterHintTests.suite());
		
		addTest(ShowCamelCasePreferenceTest.suite());
		
		addTest(TemplateProposalTest.suite());
	}
}
