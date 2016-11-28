/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.tests

import com.itemis.xtext.testing.XtextTest
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(LinkerScriptInjectorProvider)
class LinkerScriptParseErrorTest extends XtextTest {

	@Test
	def void noViableInputLengthCall() {
		testParserRuleErrors('''LENGTH(0)''', 'LExpression', "no viable alternative at input '0'")
		testParserRuleErrors('''LENGTH(+)''', 'LExpression', "no viable alternative at input '+'")
		testParserRuleErrors('''LENGTH(LENGTH)''', 'LExpression', "no viable alternative at input 'LENGTH'")
		testParserRuleErrors('''LENGTH(0+)''', 'LExpression', "no viable alternative at input '0'", "extraneous input '+'")
	}

}
