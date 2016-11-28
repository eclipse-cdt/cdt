package org.eclipse.cdt.linkerscript.tests

import com.google.inject.Inject
import com.itemis.xtext.testing.XtextTest
import java.io.StringReader
import java.util.List
import java.util.Map
import org.eclipse.cdt.linkerscript.linkerScript.LExpression
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.GrammarUtil
import org.eclipse.xtext.IGrammarAccess
import org.eclipse.xtext.ParserRule
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.parser.IParseResult
import org.eclipse.xtext.parser.IParser
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.junit.runners.Parameterized.UseParametersRunnerFactory

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

@RunWith(Parameterized)
@UseParametersRunnerFactory(XtextRunnerParameterizedFactory)
@InjectWith(LinkerScriptInjectorProvider)
class ExpressionReducerTest extends XtextTest {
	@Inject
	private IGrammarAccess grammar;
	@Inject
	private IParser parser;

	public String input;
	public Long expected;

	private LExpressionReducer reducer = new LExpressionReducerImpl();

	@Parameters(name='{index}: {0}')
	def static Iterable<List<? extends Object>> data() {
		return newArrayList(
			#['1', 1L],
			#['1+2', 3L],
			#['LENGTH(MEM)', 100L, #{'MEM' -> 100L}],
			#['ALIGN(101, 2)', 102L],
			#['''1''', 1L]
		)
	}

	new(List<? extends Object> parameters) {
		input = parameters.get(0) as String
		expected = parameters.get(1) as Long
		if (parameters.size() > 2) {
			reducer.memorySizesMap.putAll(parameters.get(2) as Map<String, Long>)
		}
		if (parameters.size() > 3) {
			reducer.variableValuesMap.putAll(parameters.get(3) as Map<String, Long>)
		}
	}

	// TODO: Add to xtext-test-utils
	def <T extends EObject> T parse(String textToParse, String ruleName) {

		val parserRule = GrammarUtil.findRuleForName(grammar.getGrammar(), ruleName) as ParserRule;

		if (parserRule == null) {
			fail("\n\nCould not find ParserRule " + ruleName + "\n\n");
		}

		val IParseResult result = parser.parse(parserRule, new StringReader(textToParse));
		assertThat(result.getSyntaxErrors(), is(emptyIterable()))
		assertThat(result.hasSyntaxErrors, is(false))
		return result.rootASTElement as T;
	}

	@Test
	def void validInputs() {
		val LExpression expression = parse(input, 'LExpression')
		val reduced = reducer.reduceToLong(expression).orElse(null)
		assertThat(reduced, is(expected))
	}
}
