/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.text.contentassist;

import junit.framework.TestSuite;

public class ContentAssistTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new ContentAssistTestSuite();
    }
    
    public ContentAssistTestSuite() {
        super("Tests in package org.eclipse.cdt.ui.tests.text.contentassist");
        
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
		addTest(CompletionTest_FunctionReference_Prefix.suite());
		addTest(CompletionTest_ScopedReference_NoPrefix.suite());
		addTest(CompletionTest_ScopedReference_Prefix.suite());
		addTest(CompletionTest_ScopedReference_NonCodeScope.suite());
		addTest(CompletionTest_MacroRef_NoPrefix.suite());		
		addTest(CompletionTest_MacroRef_Prefix.suite());
		addTest(CompletionTest_FunctionReference_NoPrefix.suite());
		addTest(CompletionTest_ConstructorReference.suite());
		addTest(CompletionTest_TypeDef_NoPrefix.suite());
		addTest(CompletionTest_VariableType_NestedPrefix.suite());
		
		addTest( ContentAssistTests.suite() );
        
		// Failed Tests
		addTest(CompletionFailedTest_MemberReference_Arrow_Prefix2.suite());
    }
}
