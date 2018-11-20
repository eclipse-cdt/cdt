/*******************************************************************************
 * Copyright (c) 2006, 2017 Siemens AG and others.
 *
 * This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Norbert Ploett - Initial implementation
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Mohamed Azab (Mentor Graphics) - Bug 438549. Add mechanism for parameter guessing.
 *     Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This suite bundles all tests for the CContentAssistProcessor
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({

		CompletionTest_AnonymousTypes.class, CompletionTest_ArgumentType_Prefix.class,
		CompletionTest_ArgumentType_Prefix2.class, CompletionTest_ClassReference_NoPrefix.class,
		CompletionTest_ClassReference_Prefix.class, CompletionTest_ConstructorReference.class,
		CompletionTest_ExceptionReference_NoPrefix.class, CompletionTest_ExceptionReference_Prefix.class,
		CompletionTest_FieldType_NoPrefix.class, CompletionTest_FieldType_NoPrefix2.class,
		CompletionTest_FieldType_Prefix.class, CompletionTest_FunctionReference_Prefix.class,
		CompletionTest_MacroRef_NoPrefix.class, CompletionTest_MacroRef_Prefix.class,
		CompletionTest_MemberReference_Arrow_NoPrefix.class, CompletionTest_MemberReference_Arrow_NoPrefix2.class,
		CompletionTest_MemberReference_Arrow_NoPrefix3.class, CompletionTest_MemberReference_Arrow_Prefix.class,
		CompletionTest_MemberReference_Arrow_Prefix2.class, CompletionTest_MemberReference_Dot_NoPrefix.class,
		CompletionTest_MemberReference_Dot_Prefix.class, CompletionTest_NamespaceRef_NoPrefix.class,
		CompletionTest_NamespaceRef_Prefix.class, CompletionTest_NewTypeReference_NoPrefix.class,
		CompletionTest_NewTypeReference_Prefix.class, CompletionTest_ScopedReference_NonCodeScope.class,
		CompletionTest_ScopedReference_NoPrefix.class, CompletionTest_ScopedReference_Prefix.class,
		CompletionTest_SingleName_Method_NoPrefix.class, CompletionTest_SingleName_Method_Prefix.class,
		CompletionTest_SingleName_NoPrefix.class, CompletionTest_SingleName_Prefix.class,
		CompletionTest_SingleName_Prefix2.class, CompletionTest_TypeDef_NoPrefix.class,
		CompletionTest_TypeDef_Prefix.class, CompletionTest_TypeRef_NoPrefix.class, CompletionTest_TypeRef_Prefix.class,
		CompletionTest_VariableType_NestedPrefix.class, CompletionTest_VariableType_NoPrefix.class,
		CompletionTest_VariableType_Prefix.class,

		CompletionTests.class, HelpProposalTests.class, CompletionTests_PlainC.class, ParameterHintTests.class,
		CPPParameterGuessingTests.class, CParameterGuessingTests.class,

		ShowCamelCasePreferenceTest.class,

		TemplateProposalTest.class,

})
public class ContentAssist2TestSuite {
}
