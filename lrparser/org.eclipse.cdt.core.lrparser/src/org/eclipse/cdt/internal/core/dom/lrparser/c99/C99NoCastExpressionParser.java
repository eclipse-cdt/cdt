/*******************************************************************************
* Copyright (c) 2006, 2015 IBM Corporation and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.lrparser.c99;

import lpg.lpgjavaruntime.*;

import java.util.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.ITokenCollector;
import org.eclipse.cdt.core.dom.lrparser.CPreprocessorAdapter;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.lpgextensions.FixedBacktrackingParser;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.index.IIndex;

import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;

import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99BuildASTParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99SecondaryParserFactory;

public class C99NoCastExpressionParser extends PrsStream implements RuleAction, ITokenStream, ITokenCollector,
		IParser<IASTExpression>, ISecondaryParser<IASTExpression> {
	private static ParseTable prs = new C99NoCastExpressionParserprs();
	private FixedBacktrackingParser btParser;

	public FixedBacktrackingParser getParser() {
		return btParser;
	}

	private void setResult(Object object) {
		btParser.setSym1(object);
	}

	public Object getRhsSym(int i) {
		return btParser.getSym(i);
	}

	public int getRhsTokenIndex(int i) {
		return btParser.getToken(i);
	}

	public IToken getRhsIToken(int i) {
		return super.getIToken(getRhsTokenIndex(i));
	}

	public int getRhsFirstTokenIndex(int i) {
		return btParser.getFirstToken(i);
	}

	public IToken getRhsFirstIToken(int i) {
		return super.getIToken(getRhsFirstTokenIndex(i));
	}

	public int getRhsLastTokenIndex(int i) {
		return btParser.getLastToken(i);
	}

	public IToken getRhsLastIToken(int i) {
		return super.getIToken(getRhsLastTokenIndex(i));
	}

	public int getLeftSpan() {
		return btParser.getFirstToken();
	}

	@Override
	public IToken getLeftIToken() {
		return super.getIToken(getLeftSpan());
	}

	public int getRightSpan() {
		return btParser.getLastToken();
	}

	@Override
	public IToken getRightIToken() {
		return super.getIToken(getRightSpan());
	}

	public int getRhsErrorTokenIndex(int i) {
		int index = btParser.getToken(i);
		IToken err = super.getIToken(index);
		return (err instanceof ErrorToken ? index : 0);
	}

	public ErrorToken getRhsErrorIToken(int i) {
		int index = btParser.getToken(i);
		IToken err = super.getIToken(index);
		return (ErrorToken) (err instanceof ErrorToken ? err : null);
	}

	public C99NoCastExpressionParser(LexStream lexStream) {
		super(lexStream);

		try {
			super.remapTerminalSymbols(orderedTerminalSymbols(), C99NoCastExpressionParserprs.EOFT_SYMBOL);
		} catch (NullExportedSymbolsException e) {
		} catch (NullTerminalSymbolsException e) {
		} catch (UnimplementedTerminalsException e) {
			java.util.ArrayList unimplemented_symbols = e.getSymbols();
			System.out.println("The Lexer will not scan the following token(s):");
			for (int i = 0; i < unimplemented_symbols.size(); i++) {
				Integer id = (Integer) unimplemented_symbols.get(i);
				System.out.println("    " + C99NoCastExpressionParsersym.orderedTerminalSymbols[id.intValue()]);
			}
			System.out.println();
		} catch (UndefinedEofSymbolException e) {
			throw new Error(new UndefinedEofSymbolException("The Lexer does not implement the Eof symbol "
					+ C99NoCastExpressionParsersym.orderedTerminalSymbols[C99NoCastExpressionParserprs.EOFT_SYMBOL]));
		}
	}

	@Override
	public String[] orderedTerminalSymbols() {
		return C99NoCastExpressionParsersym.orderedTerminalSymbols;
	}

	public String getTokenKindName(int kind) {
		return C99NoCastExpressionParsersym.orderedTerminalSymbols[kind];
	}

	public int getEOFTokenKind() {
		return C99NoCastExpressionParserprs.EOFT_SYMBOL;
	}

	public PrsStream getParseStream() {
		return this;
	}

	//
	// Report error message for given error_token.
	//
	public final void reportErrorTokenMessage(int error_token, String msg) {
		int firsttok = super.getFirstErrorToken(error_token), lasttok = super.getLastErrorToken(error_token);
		String location = super.getFileName() + ':'
				+ (firsttok > lasttok ? (super.getEndLine(lasttok) + ":" + super.getEndColumn(lasttok))
						: (super.getLine(error_token) + ":" + super.getColumn(error_token) + ":"
								+ super.getEndLine(error_token) + ":" + super.getEndColumn(error_token)))
				+ ": ";
		super.reportError((firsttok > lasttok ? ParseErrorCodes.INSERTION_CODE : ParseErrorCodes.SUBSTITUTION_CODE),
				location, msg);
	}

	public void parser() {
		parser(null, 0);
	}

	public void parser(Monitor monitor) {
		parser(monitor, 0);
	}

	public void parser(int error_repair_count) {
		parser(null, error_repair_count);
	}

	public void parser(Monitor monitor, int error_repair_count) {
		try {
			btParser = new FixedBacktrackingParser(monitor, this, prs, this);
		} catch (NotBacktrackParseTableException e) {
			throw new Error(new NotBacktrackParseTableException(
					"Regenerate C99NoCastExpressionParserprs.java with -BACKTRACK option"));
		} catch (BadParseSymFileException e) {
			throw new Error(
					new BadParseSymFileException("Bad Parser Symbol File -- C99NoCastExpressionParsersym.java"));
		}

		try {
			btParser.parse(error_repair_count);
		} catch (BadParseException e) {
			reset(e.error_token); // point to error token
			DiagnoseParser diagnoseParser = new DiagnoseParser(this, prs);
			diagnoseParser.diagnose(e.error_token);
		}
	}

	private C99BuildASTParserAction action;
	private IASTCompletionNode compNode;

	public C99NoCastExpressionParser(IScanner scanner, IDOMTokenMap tokenMap,
			IBuiltinBindingsProvider builtinBindingsProvider, IIndex index, Map<String, String> properties) {
		initActions(properties);
		action.initializeTranslationUnit(scanner, builtinBindingsProvider, index);
		CPreprocessorAdapter.runCPreprocessor(scanner, this, tokenMap);
	}

	private void initActions(Map<String, String> properties) {
		ScopedStack<Object> astStack = new ScopedStack<Object>();

		action = new C99BuildASTParserAction(this, astStack, CNodeFactory.getDefault(),
				C99SecondaryParserFactory.getDefault());
		action.setParserProperties(properties);

	}

	@Override
	public void addToken(IToken token) {
		token.setKind(mapKind(token.getKind())); // TODO does mapKind need to be called?
		super.addToken(token);
	}

	@Override
	public IASTExpression parse() {
		// this has to be done, or... kaboom!
		setStreamLength(getSize());

		final int errorRepairCount = -1; // -1 means full error handling
		parser(null, errorRepairCount); // do the actual parse
		super.resetTokenStream(); // allow tokens to be garbage collected

		compNode = action.getASTCompletionNode(); // the completion node may be null
		return (IASTExpression) action.getParseResult();
	}

	@Override
	public IASTCompletionNode getCompletionNode() {
		return compNode;
	}

	// uncomment this method to use with backtracking parser
	@Override
	public List<IToken> getRuleTokens() {
		return getTokens().subList(getLeftSpan(), getRightSpan() + 1);
	}

	@Override
	public String[] getOrderedTerminalSymbols() {
		return C99NoCastExpressionParsersym.orderedTerminalSymbols;
	}

	@Override
	@SuppressWarnings("nls")
	public String getName() {
		return "C99NoCastExpressionParser";
	}

	private ITokenMap tokenMap = null;

	@Override
	public void setTokens(List<IToken> tokens) {
		resetTokenStream();
		addToken(new Token(null, 0, 0, 0)); // dummy token
		for (IToken token : tokens) {
			token.setKind(tokenMap.mapKind(token.getKind()));
			addToken(token);
		}
		addToken(new Token(null, 0, 0, C99NoCastExpressionParsersym.TK_EOF_TOKEN));
	}

	public C99NoCastExpressionParser(ITokenStream stream, Map<String, String> properties) { // constructor for creating secondary parser
		initActions(properties);
		tokenMap = new TokenMap(C99NoCastExpressionParsersym.orderedTerminalSymbols,
				stream.getOrderedTerminalSymbols());
	}

	@Override
	public void ruleAction(int ruleNumber) {
		switch (ruleNumber) {

		//
		// Rule 1:  <openscope-ast> ::= $Empty
		//
		case 1: {
			action.openASTScope();
			break;
		}

		//
		// Rule 2:  <empty> ::= $Empty
		//
		case 2: {
			action.consumeEmpty();
			break;
		}

		//
		// Rule 13:  literal ::= integer
		//
		case 13: {
			action.consumeExpressionLiteral(IASTLiteralExpression.lk_integer_constant);
			break;
		}

		//
		// Rule 14:  literal ::= floating
		//
		case 14: {
			action.consumeExpressionLiteral(IASTLiteralExpression.lk_float_constant);
			break;
		}

		//
		// Rule 15:  literal ::= charconst
		//
		case 15: {
			action.consumeExpressionLiteral(IASTLiteralExpression.lk_char_constant);
			break;
		}

		//
		// Rule 16:  literal ::= stringlit
		//
		case 16: {
			action.consumeExpressionLiteral(IASTLiteralExpression.lk_string_literal);
			break;
		}

		//
		// Rule 18:  primary_expression ::= primary_expression_id
		//
		case 18: {
			action.consumeExpressionID();
			break;
		}

		//
		// Rule 19:  primary_expression ::= ( expression )
		//
		case 19: {
			action.consumeExpressionBracketed();
			break;
		}

		//
		// Rule 22:  postfix_expression ::= postfix_expression [ expression ]
		//
		case 22: {
			action.consumeExpressionArraySubscript();
			break;
		}

		//
		// Rule 23:  postfix_expression ::= postfix_expression ( expression_list_opt )
		//
		case 23: {
			action.consumeExpressionFunctionCall();
			break;
		}

		//
		// Rule 24:  postfix_expression ::= postfix_expression . member_name
		//
		case 24: {
			action.consumeExpressionFieldReference(false);
			break;
		}

		//
		// Rule 25:  postfix_expression ::= postfix_expression -> member_name
		//
		case 25: {
			action.consumeExpressionFieldReference(true);
			break;
		}

		//
		// Rule 26:  postfix_expression ::= postfix_expression ++
		//
		case 26: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_postFixIncr);
			break;
		}

		//
		// Rule 27:  postfix_expression ::= postfix_expression --
		//
		case 27: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_postFixDecr);
			break;
		}

		//
		// Rule 28:  postfix_expression ::= ( type_id ) initializer_list
		//
		case 28: {
			action.consumeExpressionTypeIdInitializer();
			break;
		}

		//
		// Rule 33:  unary_expression ::= ++ unary_expression
		//
		case 33: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_prefixIncr);
			break;
		}

		//
		// Rule 34:  unary_expression ::= -- unary_expression
		//
		case 34: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_prefixDecr);
			break;
		}

		//
		// Rule 35:  unary_expression ::= & cast_expression
		//
		case 35: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_amper);
			break;
		}

		//
		// Rule 36:  unary_expression ::= * cast_expression
		//
		case 36: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_star);
			break;
		}

		//
		// Rule 37:  unary_expression ::= + cast_expression
		//
		case 37: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_plus);
			break;
		}

		//
		// Rule 38:  unary_expression ::= - cast_expression
		//
		case 38: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_minus);
			break;
		}

		//
		// Rule 39:  unary_expression ::= ~ cast_expression
		//
		case 39: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_tilde);
			break;
		}

		//
		// Rule 40:  unary_expression ::= ! cast_expression
		//
		case 40: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_not);
			break;
		}

		//
		// Rule 41:  unary_expression ::= sizeof unary_expression
		//
		case 41: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_sizeof);
			break;
		}

		//
		// Rule 42:  unary_expression ::= sizeof ( type_id )
		//
		case 42: {
			action.consumeExpressionTypeId(IASTTypeIdExpression.op_sizeof);
			break;
		}

		//
		// Rule 45:  multiplicative_expression ::= multiplicative_expression * cast_expression
		//
		case 45: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiply);
			break;
		}

		//
		// Rule 46:  multiplicative_expression ::= multiplicative_expression / cast_expression
		//
		case 46: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_divide);
			break;
		}

		//
		// Rule 47:  multiplicative_expression ::= multiplicative_expression % cast_expression
		//
		case 47: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_modulo);
			break;
		}

		//
		// Rule 49:  additive_expression ::= additive_expression + multiplicative_expression
		//
		case 49: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_plus);
			break;
		}

		//
		// Rule 50:  additive_expression ::= additive_expression - multiplicative_expression
		//
		case 50: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_minus);
			break;
		}

		//
		// Rule 52:  shift_expression ::= shift_expression << additive_expression
		//
		case 52: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftLeft);
			break;
		}

		//
		// Rule 53:  shift_expression ::= shift_expression >> additive_expression
		//
		case 53: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftRight);
			break;
		}

		//
		// Rule 55:  relational_expression ::= relational_expression < shift_expression
		//
		case 55: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_lessThan);
			break;
		}

		//
		// Rule 56:  relational_expression ::= relational_expression > shift_expression
		//
		case 56: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_greaterThan);
			break;
		}

		//
		// Rule 57:  relational_expression ::= relational_expression <= shift_expression
		//
		case 57: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_lessEqual);
			break;
		}

		//
		// Rule 58:  relational_expression ::= relational_expression >= shift_expression
		//
		case 58: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_greaterEqual);
			break;
		}

		//
		// Rule 60:  equality_expression ::= equality_expression == relational_expression
		//
		case 60: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_equals);
			break;
		}

		//
		// Rule 61:  equality_expression ::= equality_expression != relational_expression
		//
		case 61: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_notequals);
			break;
		}

		//
		// Rule 63:  and_expression ::= and_expression & equality_expression
		//
		case 63: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryAnd);
			break;
		}

		//
		// Rule 65:  exclusive_or_expression ::= exclusive_or_expression ^ and_expression
		//
		case 65: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryXor);
			break;
		}

		//
		// Rule 67:  inclusive_or_expression ::= inclusive_or_expression | exclusive_or_expression
		//
		case 67: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryOr);
			break;
		}

		//
		// Rule 69:  logical_and_expression ::= logical_and_expression && inclusive_or_expression
		//
		case 69: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_logicalAnd);
			break;
		}

		//
		// Rule 71:  logical_or_expression ::= logical_or_expression || logical_and_expression
		//
		case 71: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_logicalOr);
			break;
		}

		//
		// Rule 73:  conditional_expression ::= logical_or_expression ? expression : assignment_expression
		//
		case 73: {
			action.consumeExpressionConditional();
			break;
		}

		//
		// Rule 75:  assignment_expression ::= unary_expression = assignment_expression
		//
		case 75: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_assign);
			break;
		}

		//
		// Rule 76:  assignment_expression ::= unary_expression *= assignment_expression
		//
		case 76: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiplyAssign);
			break;
		}

		//
		// Rule 77:  assignment_expression ::= unary_expression /= assignment_expression
		//
		case 77: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_divideAssign);
			break;
		}

		//
		// Rule 78:  assignment_expression ::= unary_expression %= assignment_expression
		//
		case 78: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_moduloAssign);
			break;
		}

		//
		// Rule 79:  assignment_expression ::= unary_expression += assignment_expression
		//
		case 79: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_plusAssign);
			break;
		}

		//
		// Rule 80:  assignment_expression ::= unary_expression -= assignment_expression
		//
		case 80: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_minusAssign);
			break;
		}

		//
		// Rule 81:  assignment_expression ::= unary_expression <<= assignment_expression
		//
		case 81: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftLeftAssign);
			break;
		}

		//
		// Rule 82:  assignment_expression ::= unary_expression >>= assignment_expression
		//
		case 82: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftRightAssign);
			break;
		}

		//
		// Rule 83:  assignment_expression ::= unary_expression &= assignment_expression
		//
		case 83: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryAndAssign);
			break;
		}

		//
		// Rule 84:  assignment_expression ::= unary_expression ^= assignment_expression
		//
		case 84: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryXorAssign);
			break;
		}

		//
		// Rule 85:  assignment_expression ::= unary_expression |= assignment_expression
		//
		case 85: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryOrAssign);
			break;
		}

		//
		// Rule 88:  expression_list ::= <openscope-ast> expression_list_actual
		//
		case 88: {
			action.consumeExpressionList();
			break;
		}

		//
		// Rule 90:  expression_list_opt ::= $Empty
		//
		case 90: {
			action.consumeEmpty();
			break;
		}

		//
		// Rule 100:  statement ::= ERROR_TOKEN
		//
		case 100: {
			action.consumeStatementProblem();
			break;
		}

		//
		// Rule 101:  labeled_statement ::= identifier_token : statement
		//
		case 101: {
			action.consumeStatementLabeled();
			break;
		}

		//
		// Rule 102:  labeled_statement ::= case constant_expression : statement
		//
		case 102: {
			action.consumeStatementCase();
			break;
		}

		//
		// Rule 103:  labeled_statement ::= default : statement
		//
		case 103: {
			action.consumeStatementDefault();
			break;
		}

		//
		// Rule 104:  compound_statement ::= { }
		//
		case 104: {
			action.consumeStatementCompoundStatement(false);
			break;
		}

		//
		// Rule 105:  compound_statement ::= { <openscope-ast> block_item_list }
		//
		case 105: {
			action.consumeStatementCompoundStatement(true);
			break;
		}

		//
		// Rule 109:  block_item ::= declaration
		//
		case 109: {
			action.consumeStatementDeclarationWithDisambiguation();
			break;
		}

		//
		// Rule 110:  expression_statement ::= ;
		//
		case 110: {
			action.consumeStatementNull();
			break;
		}

		//
		// Rule 111:  expression_statement ::= expression_in_statement ;
		//
		case 111: {
			action.consumeStatementExpression();
			break;
		}

		//
		// Rule 112:  selection_statement ::= if ( expression ) statement
		//
		case 112: {
			action.consumeStatementIf(false);
			break;
		}

		//
		// Rule 113:  selection_statement ::= if ( expression ) statement else statement
		//
		case 113: {
			action.consumeStatementIf(true);
			break;
		}

		//
		// Rule 114:  selection_statement ::= switch ( expression ) statement
		//
		case 114: {
			action.consumeStatementSwitch();
			break;
		}

		//
		// Rule 116:  expression_opt ::= $Empty
		//
		case 116: {
			action.consumeEmpty();
			break;
		}

		//
		// Rule 117:  iteration_statement ::= do statement while ( expression ) ;
		//
		case 117: {
			action.consumeStatementDoLoop(true);
			break;
		}

		//
		// Rule 118:  iteration_statement ::= do statement
		//
		case 118: {
			action.consumeStatementDoLoop(false);
			break;
		}

		//
		// Rule 119:  iteration_statement ::= while ( expression ) statement
		//
		case 119: {
			action.consumeStatementWhileLoop();
			break;
		}

		//
		// Rule 120:  iteration_statement ::= for ( expression_opt ; expression_opt ; expression_opt ) statement
		//
		case 120: {
			action.consumeStatementForLoop();
			break;
		}

		//
		// Rule 121:  iteration_statement ::= for ( declaration expression_opt ; expression_opt ) statement
		//
		case 121: {
			action.consumeStatementForLoop();
			break;
		}

		//
		// Rule 122:  jump_statement ::= goto identifier_token ;
		//
		case 122: {
			action.consumeStatementGoto();
			break;
		}

		//
		// Rule 123:  jump_statement ::= continue ;
		//
		case 123: {
			action.consumeStatementContinue();
			break;
		}

		//
		// Rule 124:  jump_statement ::= break ;
		//
		case 124: {
			action.consumeStatementBreak();
			break;
		}

		//
		// Rule 125:  jump_statement ::= return ;
		//
		case 125: {
			action.consumeStatementReturn(false);
			break;
		}

		//
		// Rule 126:  jump_statement ::= return expression ;
		//
		case 126: {
			action.consumeStatementReturn(true);
			break;
		}

		//
		// Rule 127:  declaration ::= declaration_specifiers ;
		//
		case 127: {
			action.consumeDeclarationSimple(false);
			break;
		}

		//
		// Rule 128:  declaration ::= declaration_specifiers <openscope-ast> init_declarator_list ;
		//
		case 128: {
			action.consumeDeclarationSimple(true);
			break;
		}

		//
		// Rule 129:  declaration_specifiers ::= <openscope-ast> simple_declaration_specifiers
		//
		case 129: {
			action.consumeDeclarationSpecifiersSimple();
			break;
		}

		//
		// Rule 130:  declaration_specifiers ::= <openscope-ast> struct_or_union_declaration_specifiers
		//
		case 130: {
			action.consumeDeclarationSpecifiersStructUnionEnum();
			break;
		}

		//
		// Rule 131:  declaration_specifiers ::= <openscope-ast> elaborated_declaration_specifiers
		//
		case 131: {
			action.consumeDeclarationSpecifiersStructUnionEnum();
			break;
		}

		//
		// Rule 132:  declaration_specifiers ::= <openscope-ast> enum_declaration_specifiers
		//
		case 132: {
			action.consumeDeclarationSpecifiersStructUnionEnum();
			break;
		}

		//
		// Rule 133:  declaration_specifiers ::= <openscope-ast> typdef_name_declaration_specifiers
		//
		case 133: {
			action.consumeDeclarationSpecifiersTypedefName();
			break;
		}

		//
		// Rule 158:  init_declarator ::= complete_declarator = initializer
		//
		case 158: {
			action.consumeDeclaratorWithInitializer(true);
			break;
		}

		//
		// Rule 160:  storage_class_specifier ::= storage_class_specifier_token
		//
		case 160: {
			action.consumeToken();
			break;
		}

		//
		// Rule 166:  simple_type_specifier ::= simple_type_specifier_token
		//
		case 166: {
			action.consumeToken();
			break;
		}

		//
		// Rule 179:  type_name_specifier ::= identifier_token
		//
		case 179: {
			action.consumeToken();
			break;
		}

		//
		// Rule 180:  struct_or_union_specifier ::= struct_or_union struct_or_union_specifier_hook { <openscope-ast> struct_declaration_list_opt }
		//
		case 180: {
			action.consumeTypeSpecifierComposite(false);
			break;
		}

		//
		// Rule 181:  struct_or_union_specifier ::= struct_or_union struct_or_union_specifier_hook identifier_token struct_or_union_specifier_suffix_hook { <openscope-ast> struct_declaration_list_opt }
		//
		case 181: {
			action.consumeTypeSpecifierComposite(true);
			break;
		}

		//
		// Rule 186:  elaborated_specifier ::= struct elaborated_specifier_hook identifier_token
		//
		case 186: {
			action.consumeTypeSpecifierElaborated(IASTCompositeTypeSpecifier.k_struct);
			break;
		}

		//
		// Rule 187:  elaborated_specifier ::= union elaborated_specifier_hook identifier_token
		//
		case 187: {
			action.consumeTypeSpecifierElaborated(IASTCompositeTypeSpecifier.k_union);
			break;
		}

		//
		// Rule 188:  elaborated_specifier ::= enum elaborated_specifier_hook identifier_token
		//
		case 188: {
			action.consumeTypeSpecifierElaborated(IASTElaboratedTypeSpecifier.k_enum);
			break;
		}

		//
		// Rule 194:  struct_declaration ::= specifier_qualifier_list <openscope-ast> struct_declarator_list ;
		//
		case 194: {
			action.consumeStructDeclaration(true);
			break;
		}

		//
		// Rule 195:  struct_declaration ::= specifier_qualifier_list ;
		//
		case 195: {
			action.consumeStructDeclaration(false);
			break;
		}

		//
		// Rule 196:  struct_declaration ::= ERROR_TOKEN
		//
		case 196: {
			action.consumeDeclarationProblem();
			break;
		}

		//
		// Rule 202:  struct_declarator ::= : constant_expression
		//
		case 202: {
			action.consumeBitField(false);
			break;
		}

		//
		// Rule 203:  struct_declarator ::= declarator : constant_expression
		//
		case 203: {
			action.consumeBitField(true);
			break;
		}

		//
		// Rule 204:  enum_specifier ::= enum enum_specifier_hook { <openscope-ast> enumerator_list_opt comma_opt }
		//
		case 204: {
			action.consumeTypeSpecifierEnumeration(false);
			break;
		}

		//
		// Rule 205:  enum_specifier ::= enum enum_specifier_hook identifier_token { <openscope-ast> enumerator_list_opt comma_opt }
		//
		case 205: {
			action.consumeTypeSpecifierEnumeration(true);
			break;
		}

		//
		// Rule 211:  enumerator ::= identifier_token
		//
		case 211: {
			action.consumeEnumerator(false);
			break;
		}

		//
		// Rule 212:  enumerator ::= identifier_token = constant_expression
		//
		case 212: {
			action.consumeEnumerator(true);
			break;
		}

		//
		// Rule 213:  type_qualifier ::= type_qualifier_token
		//
		case 213: {
			action.consumeToken();
			break;
		}

		//
		// Rule 217:  function_specifier ::= inline
		//
		case 217: {
			action.consumeToken();
			break;
		}

		//
		// Rule 219:  declarator ::= <openscope-ast> pointer_seq direct_declarator
		//
		case 219: {
			action.consumeDeclaratorWithPointer(true);
			break;
		}

		//
		// Rule 224:  basic_direct_declarator ::= declarator_id_name
		//
		case 224: {
			action.consumeDirectDeclaratorIdentifier();
			break;
		}

		//
		// Rule 225:  basic_direct_declarator ::= ( declarator )
		//
		case 225: {
			action.consumeDirectDeclaratorBracketed();
			break;
		}

		//
		// Rule 226:  declarator_id_name ::= identifier
		//
		case 226: {
			action.consumeIdentifierName();
			break;
		}

		//
		// Rule 227:  array_direct_declarator ::= basic_direct_declarator array_modifier
		//
		case 227: {
			action.consumeDirectDeclaratorArrayDeclarator(true);
			break;
		}

		//
		// Rule 228:  array_direct_declarator ::= array_direct_declarator array_modifier
		//
		case 228: {
			action.consumeDirectDeclaratorArrayDeclarator(true);
			break;
		}

		//
		// Rule 230:  function_direct_declarator ::= basic_direct_declarator ( <openscope-ast> parameter_type_list )
		//
		case 230: {
			action.consumeDirectDeclaratorFunctionDeclarator(true, true);
			break;
		}

		//
		// Rule 231:  function_direct_declarator ::= basic_direct_declarator ( )
		//
		case 231: {
			action.consumeDirectDeclaratorFunctionDeclarator(true, false);
			break;
		}

		//
		// Rule 233:  function_declarator ::= <openscope-ast> pointer_seq function_direct_declarator
		//
		case 233: {
			action.consumeDeclaratorWithPointer(true);
			break;
		}

		//
		// Rule 234:  knr_direct_declarator ::= basic_direct_declarator ( <openscope-ast> identifier_list )
		//
		case 234: {
			action.consumeDirectDeclaratorFunctionDeclaratorKnR();
			break;
		}

		//
		// Rule 236:  knr_function_declarator ::= <openscope-ast> pointer_seq knr_direct_declarator
		//
		case 236: {
			action.consumeDeclaratorWithPointer(true);
			break;
		}

		//
		// Rule 237:  identifier_list ::= identifier
		//
		case 237: {
			action.consumeIdentifierKnR();
			break;
		}

		//
		// Rule 238:  identifier_list ::= identifier_list , identifier
		//
		case 238: {
			action.consumeIdentifierKnR();
			break;
		}

		//
		// Rule 239:  array_modifier ::= [ ]
		//
		case 239: {
			action.consumeDirectDeclaratorArrayModifier(false);
			break;
		}

		//
		// Rule 240:  array_modifier ::= [ <openscope-ast> array_modifier_type_qualifiers ]
		//
		case 240: {
			action.consumeDirectDeclaratorModifiedArrayModifier(false, false, true, false);
			break;
		}

		//
		// Rule 241:  array_modifier ::= [ assignment_expression ]
		//
		case 241: {
			action.consumeDirectDeclaratorArrayModifier(true);
			break;
		}

		//
		// Rule 242:  array_modifier ::= [ <openscope-ast> array_modifier_type_qualifiers assignment_expression ]
		//
		case 242: {
			action.consumeDirectDeclaratorModifiedArrayModifier(false, false, true, true);
			break;
		}

		//
		// Rule 243:  array_modifier ::= [ static assignment_expression ]
		//
		case 243: {
			action.consumeDirectDeclaratorModifiedArrayModifier(true, false, false, true);
			break;
		}

		//
		// Rule 244:  array_modifier ::= [ static <openscope-ast> array_modifier_type_qualifiers assignment_expression ]
		//
		case 244: {
			action.consumeDirectDeclaratorModifiedArrayModifier(true, false, true, true);
			break;
		}

		//
		// Rule 245:  array_modifier ::= [ <openscope-ast> array_modifier_type_qualifiers static assignment_expression ]
		//
		case 245: {
			action.consumeDirectDeclaratorModifiedArrayModifier(true, false, true, true);
			break;
		}

		//
		// Rule 246:  array_modifier ::= [ * ]
		//
		case 246: {
			action.consumeDirectDeclaratorModifiedArrayModifier(false, true, false, false);
			break;
		}

		//
		// Rule 247:  array_modifier ::= [ <openscope-ast> array_modifier_type_qualifiers * ]
		//
		case 247: {
			action.consumeDirectDeclaratorModifiedArrayModifier(false, true, true, false);
			break;
		}

		//
		// Rule 249:  pointer_seq ::= pointer_hook * pointer_hook
		//
		case 249: {
			action.consumePointer();
			break;
		}

		//
		// Rule 250:  pointer_seq ::= pointer_seq pointer_hook * pointer_hook
		//
		case 250: {
			action.consumePointer();
			break;
		}

		//
		// Rule 251:  pointer_seq ::= pointer_hook * pointer_hook <openscope-ast> type_qualifier_list
		//
		case 251: {
			action.consumePointerTypeQualifierList();
			break;
		}

		//
		// Rule 252:  pointer_seq ::= pointer_seq pointer_hook * pointer_hook <openscope-ast> type_qualifier_list
		//
		case 252: {
			action.consumePointerTypeQualifierList();
			break;
		}

		//
		// Rule 256:  parameter_type_list ::= parameter_list
		//
		case 256: {
			action.consumeEmpty();
			break;
		}

		//
		// Rule 257:  parameter_type_list ::= parameter_list , ...
		//
		case 257: {
			action.consumePlaceHolder();
			break;
		}

		//
		// Rule 258:  parameter_type_list ::= ...
		//
		case 258: {
			action.consumePlaceHolder();
			break;
		}

		//
		// Rule 261:  parameter_declaration ::= declaration_specifiers complete_parameter_declarator
		//
		case 261: {
			action.consumeParameterDeclaration();
			break;
		}

		//
		// Rule 262:  parameter_declaration ::= declaration_specifiers
		//
		case 262: {
			action.consumeParameterDeclarationWithoutDeclarator();
			break;
		}

		//
		// Rule 265:  type_id ::= specifier_qualifier_list
		//
		case 265: {
			action.consumeTypeId(false);
			break;
		}

		//
		// Rule 266:  type_id ::= specifier_qualifier_list abstract_declarator
		//
		case 266: {
			action.consumeTypeId(true);
			break;
		}

		//
		// Rule 268:  abstract_declarator ::= <openscope-ast> pointer_seq
		//
		case 268: {
			action.consumeDeclaratorWithPointer(false);
			break;
		}

		//
		// Rule 269:  abstract_declarator ::= <openscope-ast> pointer_seq direct_abstract_declarator
		//
		case 269: {
			action.consumeDeclaratorWithPointer(true);
			break;
		}

		//
		// Rule 273:  basic_direct_abstract_declarator ::= ( abstract_declarator )
		//
		case 273: {
			action.consumeDirectDeclaratorBracketed();
			break;
		}

		//
		// Rule 274:  array_direct_abstract_declarator ::= array_modifier
		//
		case 274: {
			action.consumeDirectDeclaratorArrayDeclarator(false);
			break;
		}

		//
		// Rule 275:  array_direct_abstract_declarator ::= array_direct_abstract_declarator array_modifier
		//
		case 275: {
			action.consumeDirectDeclaratorArrayDeclarator(true);
			break;
		}

		//
		// Rule 276:  array_direct_abstract_declarator ::= basic_direct_abstract_declarator array_modifier
		//
		case 276: {
			action.consumeDirectDeclaratorArrayDeclarator(true);
			break;
		}

		//
		// Rule 277:  function_direct_abstract_declarator ::= ( )
		//
		case 277: {
			action.consumeDirectDeclaratorFunctionDeclarator(false, false);
			break;
		}

		//
		// Rule 278:  function_direct_abstract_declarator ::= basic_direct_abstract_declarator ( )
		//
		case 278: {
			action.consumeDirectDeclaratorFunctionDeclarator(true, false);
			break;
		}

		//
		// Rule 279:  function_direct_abstract_declarator ::= ( <openscope-ast> parameter_type_list )
		//
		case 279: {
			action.consumeDirectDeclaratorFunctionDeclarator(false, true);
			break;
		}

		//
		// Rule 280:  function_direct_abstract_declarator ::= basic_direct_abstract_declarator ( <openscope-ast> parameter_type_list )
		//
		case 280: {
			action.consumeDirectDeclaratorFunctionDeclarator(true, true);
			break;
		}

		//
		// Rule 281:  initializer ::= assignment_expression
		//
		case 281: {
			action.consumeInitializer();
			break;
		}

		//
		// Rule 282:  initializer ::= initializer_list
		//
		case 282: {
			action.consumeInitializer();
			break;
		}

		//
		// Rule 283:  initializer_list ::= start_initializer_list { <openscope-ast> initializer_seq comma_opt } end_initializer_list
		//
		case 283: {
			action.consumeInitializerList();
			break;
		}

		//
		// Rule 284:  initializer_list ::= { <openscope-ast> }
		//
		case 284: {
			action.consumeInitializerList();
			break;
		}

		//
		// Rule 285:  start_initializer_list ::= $Empty
		//
		case 285: {
			action.initializerListStart();
			break;
		}

		//
		// Rule 286:  end_initializer_list ::= $Empty
		//
		case 286: {
			action.initializerListEnd();
			break;
		}

		//
		// Rule 291:  designated_initializer ::= <openscope-ast> designation = initializer
		//
		case 291: {
			action.consumeInitializerDesignated();
			break;
		}

		//
		// Rule 295:  designator_base ::= [ constant_expression ]
		//
		case 295: {
			action.consumeDesignatorArray();
			break;
		}

		//
		// Rule 296:  designator_base ::= . identifier_token
		//
		case 296: {
			action.consumeDesignatorField();
			break;
		}

		//
		// Rule 297:  designator ::= [ constant_expression ]
		//
		case 297: {
			action.consumeDesignatorArray();
			break;
		}

		//
		// Rule 298:  designator ::= . identifier_token
		//
		case 298: {
			action.consumeDesignatorField();
			break;
		}

		//
		// Rule 299:  translation_unit ::= external_declaration_list
		//
		case 299: {
			action.consumeTranslationUnit();
			break;
		}

		//
		// Rule 300:  translation_unit ::= $Empty
		//
		case 300: {
			action.consumeTranslationUnit();
			break;
		}

		//
		// Rule 305:  external_declaration ::= ;
		//
		case 305: {
			action.consumeDeclarationEmpty();
			break;
		}

		//
		// Rule 306:  external_declaration ::= ERROR_TOKEN
		//
		case 306: {
			action.consumeDeclarationProblem();
			break;
		}

		//
		// Rule 310:  function_definition ::= <openscope-ast> function_declarator function_body
		//
		case 310: {
			action.consumeFunctionDefinition(false);
			break;
		}

		//
		// Rule 311:  function_definition ::= declaration_specifiers <openscope-ast> knr_function_declarator <openscope-ast> declaration_list compound_statement
		//
		case 311: {
			action.consumeFunctionDefinitionKnR();
			break;
		}

		//
		// Rule 312:  normal_function_definition ::= declaration_specifiers <openscope-ast> function_declarator function_body
		//
		case 312: {
			action.consumeFunctionDefinition(true);
			break;
		}

		//
		// Rule 313:  function_body ::= { }
		//
		case 313: {
			action.consumeStatementCompoundStatement(false);
			break;
		}

		//
		// Rule 314:  function_body ::= { <openscope-ast> block_item_list }
		//
		case 314: {
			action.consumeStatementCompoundStatement(true);
			break;
		}

		//
		// Rule 316:  no_cast_start ::= ERROR_TOKEN
		//
		case 316: {
			action.consumeEmpty();
			break;
		}

		default:
			break;
		}
		return;
	}
}
