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

		AnonymousTypes_CompletionTest.class, //
		ArgumentType_Prefix_CompletionTest.class, //

		ArgumentType_Prefix2_CompletionTest.class, //
		ClassReference_NoPrefix_CompletionTest.class, //

		ClassReference_Prefix_CompletionTest.class, //
		ConstructorReference_CompletionTest.class, //

		ExceptionReference_NoPrefix_CompletionTest.class, //
		ExceptionReference_Prefix_CompletionTest.class, //

		FieldType_NoPrefix_CompletionTest.class, //
		FieldType_NoPrefix2_CompletionTest.class, //

		FieldType_Prefix_CompletionTest.class, //
		FunctionReference_Prefix_CompletionTest.class, //

		MacroRef_NoPrefix_CompletionTest.class, //
		MacroRef_Prefix_CompletionTest.class, //

		MemberReference_Arrow_NoPrefix_CompletionTest.class, //
		MemberReference_Arrow_NoPrefix2_CompletionTest.class, //

		MemberReference_Arrow_NoPrefix3_CompletionTest.class, //
		MemberReference_Arrow_Prefix_CompletionTest.class, //

		MemberReference_Arrow_Prefix2_CompletionTest.class, //
		MemberReference_Dot_NoPrefix_CompletionTest.class, //

		MemberReference_Dot_Prefix_CompletionTest.class, //
		NamespaceRef_NoPrefix_CompletionTest.class, //

		NamespaceRef_Prefix_CompletionTest.class, //
		NewTypeReference_NoPrefix_CompletionTest.class, //

		NewTypeReference_Prefix_CompletionTest.class, //
		ScopedReference_NonCodeScope_CompletionTest.class, //

		ScopedReference_NoPrefix_CompletionTest.class, //
		ScopedReference_Prefix_CompletionTest.class, //

		SingleName_Method_NoPrefix_CompletionTest.class, //
		SingleName_Method_Prefix_CompletionTest.class, //

		SingleName_NoPrefix_CompletionTest.class, //
		SingleName_Prefix_CompletionTest.class, //

		SingleName_Prefix2_CompletionTest.class, //
		TypeDef_NoPrefix_CompletionTest.class, //

		TypeDef_Prefix_CompletionTest.class, //
		TypeRef_NoPrefix_CompletionTest.class, //
		TypeRef_Prefix_CompletionTest.class, //

		VariableType_NestedPrefix_CompletionTest.class, //
		VariableType_NoPrefix_CompletionTest.class, //

		VariableType_Prefix_CompletionTest.class, //

		CompletionTests.class, //
		HelpProposalTests.class, //
		PlainC_CompletionTests.class, //
		ParameterHintTests.class, //

		CPPParameterGuessingTests.class, //
		CParameterGuessingTests.class, //

		ShowCamelCasePreferenceTest.class, //

		TemplateProposalTest.class, //

})
public class ContentAssist2TestSuite {
}
