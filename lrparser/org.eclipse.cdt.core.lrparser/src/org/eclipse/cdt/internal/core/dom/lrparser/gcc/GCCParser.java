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

package org.eclipse.cdt.internal.core.dom.lrparser.gcc;

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

import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99BuildASTParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99SecondaryParserFactory;

import org.eclipse.cdt.core.dom.lrparser.action.gnu.GNUBuildASTParserAction;

import org.eclipse.cdt.core.dom.lrparser.action.gnu.GCCBuildASTParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.gnu.GCCSecondaryParserFactory;

public class GCCParser extends PrsStream
		implements RuleAction, ITokenStream, ITokenCollector, IParser<IASTTranslationUnit>

{
	private static ParseTable prs = new GCCParserprs();
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

	public GCCParser(LexStream lexStream) {
		super(lexStream);

		try {
			super.remapTerminalSymbols(orderedTerminalSymbols(), GCCParserprs.EOFT_SYMBOL);
		} catch (NullExportedSymbolsException e) {
		} catch (NullTerminalSymbolsException e) {
		} catch (UnimplementedTerminalsException e) {
			java.util.ArrayList unimplemented_symbols = e.getSymbols();
			System.out.println("The Lexer will not scan the following token(s):");
			for (int i = 0; i < unimplemented_symbols.size(); i++) {
				Integer id = (Integer) unimplemented_symbols.get(i);
				System.out.println("    " + GCCParsersym.orderedTerminalSymbols[id.intValue()]);
			}
			System.out.println();
		} catch (UndefinedEofSymbolException e) {
			throw new Error(new UndefinedEofSymbolException("The Lexer does not implement the Eof symbol "
					+ GCCParsersym.orderedTerminalSymbols[GCCParserprs.EOFT_SYMBOL]));
		}
	}

	@Override
	public String[] orderedTerminalSymbols() {
		return GCCParsersym.orderedTerminalSymbols;
	}

	public String getTokenKindName(int kind) {
		return GCCParsersym.orderedTerminalSymbols[kind];
	}

	public int getEOFTokenKind() {
		return GCCParserprs.EOFT_SYMBOL;
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
			throw new Error(new NotBacktrackParseTableException("Regenerate GCCParserprs.java with -BACKTRACK option"));
		} catch (BadParseSymFileException e) {
			throw new Error(new BadParseSymFileException("Bad Parser Symbol File -- GCCParsersym.java"));
		}

		try {
			btParser.parse(error_repair_count);
		} catch (BadParseException e) {
			reset(e.error_token); // point to error token
			DiagnoseParser diagnoseParser = new DiagnoseParser(this, prs);
			diagnoseParser.diagnose(e.error_token);
		}
	}

	private GCCBuildASTParserAction action;
	private IASTCompletionNode compNode;

	public GCCParser(IScanner scanner, IDOMTokenMap tokenMap, IBuiltinBindingsProvider builtinBindingsProvider,
			IIndex index, Map<String, String> properties) {
		initActions(properties);
		action.initializeTranslationUnit(scanner, builtinBindingsProvider, index);
		CPreprocessorAdapter.runCPreprocessor(scanner, this, tokenMap);
	}

	private void initActions(Map<String, String> properties) {
		ScopedStack<Object> astStack = new ScopedStack<Object>();

		action = new GCCBuildASTParserAction(this, astStack, CNodeFactory.getDefault(),
				GCCSecondaryParserFactory.getDefault());
		action.setParserProperties(properties);

		gnuAction = new GNUBuildASTParserAction(this, astStack, CNodeFactory.getDefault());
		gnuAction.setParserProperties(properties);

	}

	@Override
	public void addToken(IToken token) {
		token.setKind(mapKind(token.getKind())); // TODO does mapKind need to be called?
		super.addToken(token);
	}

	@Override
	public IASTTranslationUnit parse() {
		// this has to be done, or... kaboom!
		setStreamLength(getSize());

		final int errorRepairCount = -1; // -1 means full error handling
		parser(null, errorRepairCount); // do the actual parse
		super.resetTokenStream(); // allow tokens to be garbage collected

		compNode = action.getASTCompletionNode(); // the completion node may be null
		return (IASTTranslationUnit) action.getParseResult();
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
		return GCCParsersym.orderedTerminalSymbols;
	}

	@Override
	@SuppressWarnings("nls")
	public String getName() {
		return "GCCParser";
	}

	private GNUBuildASTParserAction gnuAction;

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
		// Rule 44:  cast_expression ::= ( type_id ) cast_expression
		//
		case 44: {
			action.consumeExpressionCast(IASTCastExpression.op_cast);
			break;
		}

		//
		// Rule 46:  multiplicative_expression ::= multiplicative_expression * cast_expression
		//
		case 46: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiply);
			break;
		}

		//
		// Rule 47:  multiplicative_expression ::= multiplicative_expression / cast_expression
		//
		case 47: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_divide);
			break;
		}

		//
		// Rule 48:  multiplicative_expression ::= multiplicative_expression % cast_expression
		//
		case 48: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_modulo);
			break;
		}

		//
		// Rule 50:  additive_expression ::= additive_expression + multiplicative_expression
		//
		case 50: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_plus);
			break;
		}

		//
		// Rule 51:  additive_expression ::= additive_expression - multiplicative_expression
		//
		case 51: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_minus);
			break;
		}

		//
		// Rule 53:  shift_expression ::= shift_expression << additive_expression
		//
		case 53: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftLeft);
			break;
		}

		//
		// Rule 54:  shift_expression ::= shift_expression >> additive_expression
		//
		case 54: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftRight);
			break;
		}

		//
		// Rule 56:  relational_expression ::= relational_expression < shift_expression
		//
		case 56: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_lessThan);
			break;
		}

		//
		// Rule 57:  relational_expression ::= relational_expression > shift_expression
		//
		case 57: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_greaterThan);
			break;
		}

		//
		// Rule 58:  relational_expression ::= relational_expression <= shift_expression
		//
		case 58: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_lessEqual);
			break;
		}

		//
		// Rule 59:  relational_expression ::= relational_expression >= shift_expression
		//
		case 59: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_greaterEqual);
			break;
		}

		//
		// Rule 61:  equality_expression ::= equality_expression == relational_expression
		//
		case 61: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_equals);
			break;
		}

		//
		// Rule 62:  equality_expression ::= equality_expression != relational_expression
		//
		case 62: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_notequals);
			break;
		}

		//
		// Rule 64:  and_expression ::= and_expression & equality_expression
		//
		case 64: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryAnd);
			break;
		}

		//
		// Rule 66:  exclusive_or_expression ::= exclusive_or_expression ^ and_expression
		//
		case 66: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryXor);
			break;
		}

		//
		// Rule 68:  inclusive_or_expression ::= inclusive_or_expression | exclusive_or_expression
		//
		case 68: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryOr);
			break;
		}

		//
		// Rule 70:  logical_and_expression ::= logical_and_expression && inclusive_or_expression
		//
		case 70: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_logicalAnd);
			break;
		}

		//
		// Rule 72:  logical_or_expression ::= logical_or_expression || logical_and_expression
		//
		case 72: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_logicalOr);
			break;
		}

		//
		// Rule 74:  conditional_expression ::= logical_or_expression ? expression : assignment_expression
		//
		case 74: {
			action.consumeExpressionConditional();
			break;
		}

		//
		// Rule 76:  assignment_expression ::= unary_expression = assignment_expression
		//
		case 76: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_assign);
			break;
		}

		//
		// Rule 77:  assignment_expression ::= unary_expression *= assignment_expression
		//
		case 77: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiplyAssign);
			break;
		}

		//
		// Rule 78:  assignment_expression ::= unary_expression /= assignment_expression
		//
		case 78: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_divideAssign);
			break;
		}

		//
		// Rule 79:  assignment_expression ::= unary_expression %= assignment_expression
		//
		case 79: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_moduloAssign);
			break;
		}

		//
		// Rule 80:  assignment_expression ::= unary_expression += assignment_expression
		//
		case 80: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_plusAssign);
			break;
		}

		//
		// Rule 81:  assignment_expression ::= unary_expression -= assignment_expression
		//
		case 81: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_minusAssign);
			break;
		}

		//
		// Rule 82:  assignment_expression ::= unary_expression <<= assignment_expression
		//
		case 82: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftLeftAssign);
			break;
		}

		//
		// Rule 83:  assignment_expression ::= unary_expression >>= assignment_expression
		//
		case 83: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftRightAssign);
			break;
		}

		//
		// Rule 84:  assignment_expression ::= unary_expression &= assignment_expression
		//
		case 84: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryAndAssign);
			break;
		}

		//
		// Rule 85:  assignment_expression ::= unary_expression ^= assignment_expression
		//
		case 85: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryXorAssign);
			break;
		}

		//
		// Rule 86:  assignment_expression ::= unary_expression |= assignment_expression
		//
		case 86: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryOrAssign);
			break;
		}

		//
		// Rule 89:  expression_list ::= <openscope-ast> expression_list_actual
		//
		case 89: {
			action.consumeExpressionList();
			break;
		}

		//
		// Rule 91:  expression_list_opt ::= $Empty
		//
		case 91: {
			action.consumeEmpty();
			break;
		}

		//
		// Rule 101:  statement ::= ERROR_TOKEN
		//
		case 101: {
			action.consumeStatementProblem();
			break;
		}

		//
		// Rule 102:  labeled_statement ::= identifier_token : statement
		//
		case 102: {
			action.consumeStatementLabeled();
			break;
		}

		//
		// Rule 103:  labeled_statement ::= case constant_expression : statement
		//
		case 103: {
			action.consumeStatementCase();
			break;
		}

		//
		// Rule 104:  labeled_statement ::= default : statement
		//
		case 104: {
			action.consumeStatementDefault();
			break;
		}

		//
		// Rule 105:  compound_statement ::= { }
		//
		case 105: {
			action.consumeStatementCompoundStatement(false);
			break;
		}

		//
		// Rule 106:  compound_statement ::= { <openscope-ast> block_item_list }
		//
		case 106: {
			action.consumeStatementCompoundStatement(true);
			break;
		}

		//
		// Rule 110:  block_item ::= declaration
		//
		case 110: {
			action.consumeStatementDeclarationWithDisambiguation();
			break;
		}

		//
		// Rule 111:  expression_statement ::= ;
		//
		case 111: {
			action.consumeStatementNull();
			break;
		}

		//
		// Rule 112:  expression_statement ::= expression_in_statement ;
		//
		case 112: {
			action.consumeStatementExpression();
			break;
		}

		//
		// Rule 113:  selection_statement ::= if ( expression ) statement
		//
		case 113: {
			action.consumeStatementIf(false);
			break;
		}

		//
		// Rule 114:  selection_statement ::= if ( expression ) statement else statement
		//
		case 114: {
			action.consumeStatementIf(true);
			break;
		}

		//
		// Rule 115:  selection_statement ::= switch ( expression ) statement
		//
		case 115: {
			action.consumeStatementSwitch();
			break;
		}

		//
		// Rule 117:  expression_opt ::= $Empty
		//
		case 117: {
			action.consumeEmpty();
			break;
		}

		//
		// Rule 118:  iteration_statement ::= do statement while ( expression ) ;
		//
		case 118: {
			action.consumeStatementDoLoop(true);
			break;
		}

		//
		// Rule 119:  iteration_statement ::= do statement
		//
		case 119: {
			action.consumeStatementDoLoop(false);
			break;
		}

		//
		// Rule 120:  iteration_statement ::= while ( expression ) statement
		//
		case 120: {
			action.consumeStatementWhileLoop();
			break;
		}

		//
		// Rule 121:  iteration_statement ::= for ( expression_opt ; expression_opt ; expression_opt ) statement
		//
		case 121: {
			action.consumeStatementForLoop();
			break;
		}

		//
		// Rule 122:  iteration_statement ::= for ( declaration expression_opt ; expression_opt ) statement
		//
		case 122: {
			action.consumeStatementForLoop();
			break;
		}

		//
		// Rule 123:  jump_statement ::= goto identifier_token ;
		//
		case 123: {
			action.consumeStatementGoto();
			break;
		}

		//
		// Rule 124:  jump_statement ::= continue ;
		//
		case 124: {
			action.consumeStatementContinue();
			break;
		}

		//
		// Rule 125:  jump_statement ::= break ;
		//
		case 125: {
			action.consumeStatementBreak();
			break;
		}

		//
		// Rule 126:  jump_statement ::= return ;
		//
		case 126: {
			action.consumeStatementReturn(false);
			break;
		}

		//
		// Rule 127:  jump_statement ::= return expression ;
		//
		case 127: {
			action.consumeStatementReturn(true);
			break;
		}

		//
		// Rule 128:  declaration ::= declaration_specifiers ;
		//
		case 128: {
			action.consumeDeclarationSimple(false);
			break;
		}

		//
		// Rule 129:  declaration ::= declaration_specifiers <openscope-ast> init_declarator_list ;
		//
		case 129: {
			action.consumeDeclarationSimple(true);
			break;
		}

		//
		// Rule 130:  declaration_specifiers ::= <openscope-ast> simple_declaration_specifiers
		//
		case 130: {
			action.consumeDeclarationSpecifiersSimple();
			break;
		}

		//
		// Rule 131:  declaration_specifiers ::= <openscope-ast> struct_or_union_declaration_specifiers
		//
		case 131: {
			action.consumeDeclarationSpecifiersStructUnionEnum();
			break;
		}

		//
		// Rule 132:  declaration_specifiers ::= <openscope-ast> elaborated_declaration_specifiers
		//
		case 132: {
			action.consumeDeclarationSpecifiersStructUnionEnum();
			break;
		}

		//
		// Rule 133:  declaration_specifiers ::= <openscope-ast> enum_declaration_specifiers
		//
		case 133: {
			action.consumeDeclarationSpecifiersStructUnionEnum();
			break;
		}

		//
		// Rule 134:  declaration_specifiers ::= <openscope-ast> typdef_name_declaration_specifiers
		//
		case 134: {
			action.consumeDeclarationSpecifiersTypedefName();
			break;
		}

		//
		// Rule 159:  init_declarator ::= complete_declarator = initializer
		//
		case 159: {
			action.consumeDeclaratorWithInitializer(true);
			break;
		}

		//
		// Rule 161:  storage_class_specifier ::= storage_class_specifier_token
		//
		case 161: {
			action.consumeToken();
			break;
		}

		//
		// Rule 167:  simple_type_specifier ::= simple_type_specifier_token
		//
		case 167: {
			action.consumeToken();
			break;
		}

		//
		// Rule 180:  type_name_specifier ::= identifier_token
		//
		case 180: {
			action.consumeToken();
			break;
		}

		//
		// Rule 181:  struct_or_union_specifier ::= struct_or_union struct_or_union_specifier_hook { <openscope-ast> struct_declaration_list_opt }
		//
		case 181: {
			action.consumeTypeSpecifierComposite(false);
			break;
		}

		//
		// Rule 182:  struct_or_union_specifier ::= struct_or_union struct_or_union_specifier_hook identifier_token struct_or_union_specifier_suffix_hook { <openscope-ast> struct_declaration_list_opt }
		//
		case 182: {
			action.consumeTypeSpecifierComposite(true);
			break;
		}

		//
		// Rule 187:  elaborated_specifier ::= struct elaborated_specifier_hook identifier_token
		//
		case 187: {
			action.consumeTypeSpecifierElaborated(IASTCompositeTypeSpecifier.k_struct);
			break;
		}

		//
		// Rule 188:  elaborated_specifier ::= union elaborated_specifier_hook identifier_token
		//
		case 188: {
			action.consumeTypeSpecifierElaborated(IASTCompositeTypeSpecifier.k_union);
			break;
		}

		//
		// Rule 189:  elaborated_specifier ::= enum elaborated_specifier_hook identifier_token
		//
		case 189: {
			action.consumeTypeSpecifierElaborated(IASTElaboratedTypeSpecifier.k_enum);
			break;
		}

		//
		// Rule 195:  struct_declaration ::= specifier_qualifier_list <openscope-ast> struct_declarator_list ;
		//
		case 195: {
			action.consumeStructDeclaration(true);
			break;
		}

		//
		// Rule 196:  struct_declaration ::= specifier_qualifier_list ;
		//
		case 196: {
			action.consumeStructDeclaration(false);
			break;
		}

		//
		// Rule 197:  struct_declaration ::= ERROR_TOKEN
		//
		case 197: {
			action.consumeDeclarationProblem();
			break;
		}

		//
		// Rule 203:  struct_declarator ::= : constant_expression
		//
		case 203: {
			action.consumeBitField(false);
			break;
		}

		//
		// Rule 204:  struct_declarator ::= declarator : constant_expression
		//
		case 204: {
			action.consumeBitField(true);
			break;
		}

		//
		// Rule 205:  enum_specifier ::= enum enum_specifier_hook { <openscope-ast> enumerator_list_opt comma_opt }
		//
		case 205: {
			action.consumeTypeSpecifierEnumeration(false);
			break;
		}

		//
		// Rule 206:  enum_specifier ::= enum enum_specifier_hook identifier_token { <openscope-ast> enumerator_list_opt comma_opt }
		//
		case 206: {
			action.consumeTypeSpecifierEnumeration(true);
			break;
		}

		//
		// Rule 212:  enumerator ::= identifier_token
		//
		case 212: {
			action.consumeEnumerator(false);
			break;
		}

		//
		// Rule 213:  enumerator ::= identifier_token = constant_expression
		//
		case 213: {
			action.consumeEnumerator(true);
			break;
		}

		//
		// Rule 214:  type_qualifier ::= type_qualifier_token
		//
		case 214: {
			action.consumeToken();
			break;
		}

		//
		// Rule 218:  function_specifier ::= inline
		//
		case 218: {
			action.consumeToken();
			break;
		}

		//
		// Rule 220:  declarator ::= <openscope-ast> pointer_seq direct_declarator
		//
		case 220: {
			action.consumeDeclaratorWithPointer(true);
			break;
		}

		//
		// Rule 225:  basic_direct_declarator ::= declarator_id_name
		//
		case 225: {
			action.consumeDirectDeclaratorIdentifier();
			break;
		}

		//
		// Rule 226:  basic_direct_declarator ::= ( declarator )
		//
		case 226: {
			action.consumeDirectDeclaratorBracketed();
			break;
		}

		//
		// Rule 227:  declarator_id_name ::= identifier
		//
		case 227: {
			action.consumeIdentifierName();
			break;
		}

		//
		// Rule 228:  array_direct_declarator ::= basic_direct_declarator array_modifier
		//
		case 228: {
			action.consumeDirectDeclaratorArrayDeclarator(true);
			break;
		}

		//
		// Rule 229:  array_direct_declarator ::= array_direct_declarator array_modifier
		//
		case 229: {
			action.consumeDirectDeclaratorArrayDeclarator(true);
			break;
		}

		//
		// Rule 231:  function_direct_declarator ::= basic_direct_declarator ( <openscope-ast> parameter_type_list )
		//
		case 231: {
			action.consumeDirectDeclaratorFunctionDeclarator(true, true);
			break;
		}

		//
		// Rule 232:  function_direct_declarator ::= basic_direct_declarator ( )
		//
		case 232: {
			action.consumeDirectDeclaratorFunctionDeclarator(true, false);
			break;
		}

		//
		// Rule 234:  function_declarator ::= <openscope-ast> pointer_seq function_direct_declarator
		//
		case 234: {
			action.consumeDeclaratorWithPointer(true);
			break;
		}

		//
		// Rule 235:  knr_direct_declarator ::= basic_direct_declarator ( <openscope-ast> identifier_list )
		//
		case 235: {
			action.consumeDirectDeclaratorFunctionDeclaratorKnR();
			break;
		}

		//
		// Rule 237:  knr_function_declarator ::= <openscope-ast> pointer_seq knr_direct_declarator
		//
		case 237: {
			action.consumeDeclaratorWithPointer(true);
			break;
		}

		//
		// Rule 238:  identifier_list ::= identifier
		//
		case 238: {
			action.consumeIdentifierKnR();
			break;
		}

		//
		// Rule 239:  identifier_list ::= identifier_list , identifier
		//
		case 239: {
			action.consumeIdentifierKnR();
			break;
		}

		//
		// Rule 240:  array_modifier ::= [ ]
		//
		case 240: {
			action.consumeDirectDeclaratorArrayModifier(false);
			break;
		}

		//
		// Rule 241:  array_modifier ::= [ <openscope-ast> array_modifier_type_qualifiers ]
		//
		case 241: {
			action.consumeDirectDeclaratorModifiedArrayModifier(false, false, true, false);
			break;
		}

		//
		// Rule 242:  array_modifier ::= [ assignment_expression ]
		//
		case 242: {
			action.consumeDirectDeclaratorArrayModifier(true);
			break;
		}

		//
		// Rule 243:  array_modifier ::= [ <openscope-ast> array_modifier_type_qualifiers assignment_expression ]
		//
		case 243: {
			action.consumeDirectDeclaratorModifiedArrayModifier(false, false, true, true);
			break;
		}

		//
		// Rule 244:  array_modifier ::= [ static assignment_expression ]
		//
		case 244: {
			action.consumeDirectDeclaratorModifiedArrayModifier(true, false, false, true);
			break;
		}

		//
		// Rule 245:  array_modifier ::= [ static <openscope-ast> array_modifier_type_qualifiers assignment_expression ]
		//
		case 245: {
			action.consumeDirectDeclaratorModifiedArrayModifier(true, false, true, true);
			break;
		}

		//
		// Rule 246:  array_modifier ::= [ <openscope-ast> array_modifier_type_qualifiers static assignment_expression ]
		//
		case 246: {
			action.consumeDirectDeclaratorModifiedArrayModifier(true, false, true, true);
			break;
		}

		//
		// Rule 247:  array_modifier ::= [ * ]
		//
		case 247: {
			action.consumeDirectDeclaratorModifiedArrayModifier(false, true, false, false);
			break;
		}

		//
		// Rule 248:  array_modifier ::= [ <openscope-ast> array_modifier_type_qualifiers * ]
		//
		case 248: {
			action.consumeDirectDeclaratorModifiedArrayModifier(false, true, true, false);
			break;
		}

		//
		// Rule 250:  pointer_seq ::= pointer_hook * pointer_hook
		//
		case 250: {
			action.consumePointer();
			break;
		}

		//
		// Rule 251:  pointer_seq ::= pointer_seq pointer_hook * pointer_hook
		//
		case 251: {
			action.consumePointer();
			break;
		}

		//
		// Rule 252:  pointer_seq ::= pointer_hook * pointer_hook <openscope-ast> type_qualifier_list
		//
		case 252: {
			action.consumePointerTypeQualifierList();
			break;
		}

		//
		// Rule 253:  pointer_seq ::= pointer_seq pointer_hook * pointer_hook <openscope-ast> type_qualifier_list
		//
		case 253: {
			action.consumePointerTypeQualifierList();
			break;
		}

		//
		// Rule 257:  parameter_type_list ::= parameter_list
		//
		case 257: {
			action.consumeEmpty();
			break;
		}

		//
		// Rule 258:  parameter_type_list ::= parameter_list , ...
		//
		case 258: {
			action.consumePlaceHolder();
			break;
		}

		//
		// Rule 259:  parameter_type_list ::= ...
		//
		case 259: {
			action.consumePlaceHolder();
			break;
		}

		//
		// Rule 262:  parameter_declaration ::= declaration_specifiers complete_parameter_declarator
		//
		case 262: {
			action.consumeParameterDeclaration();
			break;
		}

		//
		// Rule 263:  parameter_declaration ::= declaration_specifiers
		//
		case 263: {
			action.consumeParameterDeclarationWithoutDeclarator();
			break;
		}

		//
		// Rule 266:  type_id ::= specifier_qualifier_list
		//
		case 266: {
			action.consumeTypeId(false);
			break;
		}

		//
		// Rule 267:  type_id ::= specifier_qualifier_list abstract_declarator
		//
		case 267: {
			action.consumeTypeId(true);
			break;
		}

		//
		// Rule 269:  abstract_declarator ::= <openscope-ast> pointer_seq
		//
		case 269: {
			action.consumeDeclaratorWithPointer(false);
			break;
		}

		//
		// Rule 270:  abstract_declarator ::= <openscope-ast> pointer_seq direct_abstract_declarator
		//
		case 270: {
			action.consumeDeclaratorWithPointer(true);
			break;
		}

		//
		// Rule 274:  basic_direct_abstract_declarator ::= ( abstract_declarator )
		//
		case 274: {
			action.consumeDirectDeclaratorBracketed();
			break;
		}

		//
		// Rule 275:  array_direct_abstract_declarator ::= array_modifier
		//
		case 275: {
			action.consumeDirectDeclaratorArrayDeclarator(false);
			break;
		}

		//
		// Rule 276:  array_direct_abstract_declarator ::= array_direct_abstract_declarator array_modifier
		//
		case 276: {
			action.consumeDirectDeclaratorArrayDeclarator(true);
			break;
		}

		//
		// Rule 277:  array_direct_abstract_declarator ::= basic_direct_abstract_declarator array_modifier
		//
		case 277: {
			action.consumeDirectDeclaratorArrayDeclarator(true);
			break;
		}

		//
		// Rule 278:  function_direct_abstract_declarator ::= ( )
		//
		case 278: {
			action.consumeDirectDeclaratorFunctionDeclarator(false, false);
			break;
		}

		//
		// Rule 279:  function_direct_abstract_declarator ::= basic_direct_abstract_declarator ( )
		//
		case 279: {
			action.consumeDirectDeclaratorFunctionDeclarator(true, false);
			break;
		}

		//
		// Rule 280:  function_direct_abstract_declarator ::= ( <openscope-ast> parameter_type_list )
		//
		case 280: {
			action.consumeDirectDeclaratorFunctionDeclarator(false, true);
			break;
		}

		//
		// Rule 281:  function_direct_abstract_declarator ::= basic_direct_abstract_declarator ( <openscope-ast> parameter_type_list )
		//
		case 281: {
			action.consumeDirectDeclaratorFunctionDeclarator(true, true);
			break;
		}

		//
		// Rule 282:  initializer ::= assignment_expression
		//
		case 282: {
			action.consumeInitializer();
			break;
		}

		//
		// Rule 283:  initializer ::= initializer_list
		//
		case 283: {
			action.consumeInitializer();
			break;
		}

		//
		// Rule 284:  initializer_list ::= start_initializer_list { <openscope-ast> initializer_seq comma_opt } end_initializer_list
		//
		case 284: {
			action.consumeInitializerList();
			break;
		}

		//
		// Rule 285:  initializer_list ::= { <openscope-ast> }
		//
		case 285: {
			action.consumeInitializerList();
			break;
		}

		//
		// Rule 286:  start_initializer_list ::= $Empty
		//
		case 286: {
			action.initializerListStart();
			break;
		}

		//
		// Rule 287:  end_initializer_list ::= $Empty
		//
		case 287: {
			action.initializerListEnd();
			break;
		}

		//
		// Rule 292:  designated_initializer ::= <openscope-ast> designation = initializer
		//
		case 292: {
			action.consumeInitializerDesignated();
			break;
		}

		//
		// Rule 296:  designator_base ::= [ constant_expression ]
		//
		case 296: {
			action.consumeDesignatorArray();
			break;
		}

		//
		// Rule 297:  designator_base ::= . identifier_token
		//
		case 297: {
			action.consumeDesignatorField();
			break;
		}

		//
		// Rule 298:  designator ::= [ constant_expression ]
		//
		case 298: {
			action.consumeDesignatorArray();
			break;
		}

		//
		// Rule 299:  designator ::= . identifier_token
		//
		case 299: {
			action.consumeDesignatorField();
			break;
		}

		//
		// Rule 300:  translation_unit ::= external_declaration_list
		//
		case 300: {
			action.consumeTranslationUnit();
			break;
		}

		//
		// Rule 301:  translation_unit ::= $Empty
		//
		case 301: {
			action.consumeTranslationUnit();
			break;
		}

		//
		// Rule 306:  external_declaration ::= ;
		//
		case 306: {
			action.consumeDeclarationEmpty();
			break;
		}

		//
		// Rule 307:  external_declaration ::= ERROR_TOKEN
		//
		case 307: {
			action.consumeDeclarationProblem();
			break;
		}

		//
		// Rule 311:  function_definition ::= <openscope-ast> function_declarator function_body
		//
		case 311: {
			action.consumeFunctionDefinition(false);
			break;
		}

		//
		// Rule 312:  function_definition ::= declaration_specifiers <openscope-ast> knr_function_declarator <openscope-ast> declaration_list compound_statement
		//
		case 312: {
			action.consumeFunctionDefinitionKnR();
			break;
		}

		//
		// Rule 313:  normal_function_definition ::= declaration_specifiers <openscope-ast> function_declarator function_body
		//
		case 313: {
			action.consumeFunctionDefinition(true);
			break;
		}

		//
		// Rule 314:  function_body ::= { }
		//
		case 314: {
			action.consumeStatementCompoundStatement(false);
			break;
		}

		//
		// Rule 315:  function_body ::= { <openscope-ast> block_item_list }
		//
		case 315: {
			action.consumeStatementCompoundStatement(true);
			break;
		}

		//
		// Rule 332:  attribute_parameter ::= assignment_expression
		//
		case 332: {
			action.consumeIgnore();
			break;
		}

		//
		// Rule 343:  extended_asm_declaration ::= asm volatile_opt ( extended_asm_param_seq ) ;
		//
		case 343: {
			gnuAction.consumeDeclarationASM();
			break;
		}

		//
		// Rule 354:  unary_expression ::= __alignof__ unary_expression
		//
		case 354: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_alignOf);
			break;
		}

		//
		// Rule 355:  unary_expression ::= __alignof__ ( type_id )
		//
		case 355: {
			action.consumeExpressionTypeId(IASTTypeIdExpression.op_alignof);
			break;
		}

		//
		// Rule 356:  unary_expression ::= typeof unary_expression
		//
		case 356: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_typeof);
			break;
		}

		//
		// Rule 357:  unary_expression ::= typeof ( type_id )
		//
		case 357: {
			action.consumeExpressionTypeId(IASTTypeIdExpression.op_typeof);
			break;
		}

		//
		// Rule 358:  relational_expression ::= relational_expression >? shift_expression
		//
		case 358: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_max);
			break;
		}

		//
		// Rule 359:  relational_expression ::= relational_expression <? shift_expression
		//
		case 359: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_min);
			break;
		}

		//
		// Rule 360:  conditional_expression ::= logical_or_expression ? <empty> : assignment_expression
		//
		case 360: {
			action.consumeExpressionConditional();
			break;
		}

		//
		// Rule 361:  primary_expression ::= ( compound_statement )
		//
		case 361: {
			gnuAction.consumeCompoundStatementExpression();
			break;
		}

		//
		// Rule 362:  labeled_statement ::= case case_range_expression : statement
		//
		case 362: {
			action.consumeStatementCase();
			break;
		}

		//
		// Rule 363:  case_range_expression ::= constant_expression ... constant_expression
		//
		case 363: {
			action.consumeExpressionBinaryOperator(IASTBinaryExpression.op_assign);
			break;
		}

		//
		// Rule 367:  typeof_type_specifier ::= typeof unary_expression
		//
		case 367: {
			action.consumeExpressionUnaryOperator(IASTUnaryExpression.op_typeof);
			break;
		}

		//
		// Rule 368:  typeof_type_specifier ::= typeof ( type_id )
		//
		case 368: {
			action.consumeExpressionTypeId(IASTTypeIdExpression.op_typeof);
			break;
		}

		//
		// Rule 369:  declaration_specifiers ::= <openscope-ast> typeof_declaration_specifiers
		//
		case 369: {
			action.consumeDeclarationSpecifiersTypeof();
			break;
		}

		//
		// Rule 385:  field_name_designator ::= identifier_token :
		//
		case 385: {
			action.consumeDesignatorFieldGCC();
			break;
		}

		//
		// Rule 386:  array_range_designator ::= [ constant_expression ... constant_expression ]
		//
		case 386: {
			action.consumeDesignatorArrayRange();
			break;
		}

		//
		// Rule 387:  designated_initializer ::= <openscope-ast> field_name_designator initializer
		//
		case 387: {
			action.consumeInitializerDesignated();
			break;
		}

		//
		// Rule 388:  block_item ::= normal_function_definition
		//
		case 388: {
			action.consumeStatementDeclaration();
			break;
		}

		default:
			break;
		}
		return;
	}
}
