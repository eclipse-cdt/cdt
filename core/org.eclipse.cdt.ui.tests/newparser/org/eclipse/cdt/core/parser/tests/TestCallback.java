package org.eclipse.cdt.core.parser.tests;

import junit.framework.Assert;

import org.eclipse.cdt.internal.core.newparser.IParserCallback;
import org.eclipse.cdt.internal.core.newparser.Token;

/**
 * @author dschaefe
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestCallback extends Assert implements IParserCallback {

	private int state = 0;
	private int numStates;
	
	public static class StateData extends Assert {
		public int id;
		public String data;
		
		public StateData(int id, String data) {
			this.id = id;
			this.data = data;
		}
		
		public void validate(int id, String data) {
			assertEquals(id, this.id);
			assertEquals(data, this.data);
		}
	}
	
	private final StateData [] stateData;
	
	public TestCallback(StateData [] stateData) {
		this.stateData = stateData;
		numStates = stateData.length;
	}

	private void validate(int id, String data) {
		assertTrue(state < numStates);
		stateData[state].validate(id, data);
		++state;
	}
	
	public void endTest() {
		assertEquals(numStates, state);
	}
	
	public static final int translationUnitBegin = 0;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitBegin()
	 */
	public void translationUnitBegin() {
		fail();
	}

	public static final int translationUnitEnd = 1;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitEnd()
	 */
	public void translationUnitEnd() {
		fail();
	}

	public static final int inclusionBegin = 2;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionBegin(java.lang.String)
	 */
	public void inclusionBegin(String includeFile) {
		fail();
	}

	public static final int inclusionEnd = 3;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd() {
		fail();
	}

	public static final int macro = 4;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(java.lang.String)
	 */
	public void macro(String macroName) {
		fail();
	}

	public static final int simpleDeclarationBegin = 5;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationBegin(Token firstToken) {
		fail();
	}

	public static final int simpleDeclarationEnd = 6;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationEnd() {
		fail();
	}

	public static final int declSpecifier = 7;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declSpecifier(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclSpecifier(Token specifier) {
		fail();
	}

	public static final int declaratorBegin = 8;
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorBegin()
	 */
	public void declaratorBegin() {
		fail();
	}

	public static final int declaratorId = 9;
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void declaratorId() {
		fail();
	}

	public static final int argumentsBegin = 10;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsBegin()
	 */
	public void argumentsBegin() {
		fail();
	}

	public static final int argumentsEnd = 11;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsEnd()
	 */
	public void argumentsEnd() {
		fail();
	}

	public static final int declaratorEnd = 12;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorEnd()
	 */
	public void declaratorEnd() {
		fail();
	}

	public static final int functionBodyBegin = 13;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyBegin()
	 */
	public void functionBodyBegin() {
		fail();
	}

	public static final int functionBodyEnd = 14;
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyEnd()
	 */
	public void functionBodyEnd() {
		fail();
	}

	public static final int classBegin = 15;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classBegin(java.lang.String, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void classSpecifierBegin(Token classKey) {
		fail();
	}

	public static final int classEnd = 16;
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classEnd()
	 */
	public void classSpecifierEnd() {
		fail();
	}

	public static final int expressionOperator = 17;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionOperator(Token operator) throws Exception {
		validate(expressionOperator, operator.getImage());
	}

	public static final int expressionTerminal = 18;	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionTerminal(Token terminal) throws Exception {
		validate(expressionTerminal, terminal.getImage());
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classSpecifierName()
	 */
	public void classSpecifierName() {
	}

}
