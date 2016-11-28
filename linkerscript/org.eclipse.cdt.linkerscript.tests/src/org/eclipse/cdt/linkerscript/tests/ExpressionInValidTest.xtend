package org.eclipse.cdt.linkerscript.tests

import com.itemis.xtext.testing.XtextTest
import org.eclipse.xtext.junit4.InjectWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters
import org.junit.runners.Parameterized.UseParametersRunnerFactory

@RunWith(Parameterized)
@UseParametersRunnerFactory(XtextRunnerParameterizedFactory)
@InjectWith(LinkerScriptInjectorProvider)
class ExpressionInValidTest extends XtextTest {
	@Parameters(name='{0}')
	def static Iterable<? extends Object> data() {
		return newArrayList(
			'2+',
			'1/2', // as GNU ld, divide must be followed by a space
			'(2+2',
			'LENGTH("MEM',
			'LENGTH(MEM',

			'''1+'''
		)
	}

	@Parameter(0)
	public String input;

	@Test
	def void validInputs() {
		// We are not being specific about which errors here
		testParserRuleErrors(input, 'LExpression')
	}
}
