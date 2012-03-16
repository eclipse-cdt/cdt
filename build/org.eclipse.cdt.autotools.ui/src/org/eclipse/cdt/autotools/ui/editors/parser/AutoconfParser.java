/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc., (c) 2008 Nokia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Ed Swartz (Nokia) - refactoring
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.autotools.ui.editors.AcInitElement;
import org.eclipse.cdt.autotools.ui.editors.AutoconfEditorMessages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


/**
 * Tokenizing autoconf parser, based on original work by Jeff Johnston 
 * @author eswartz
 */
public class AutoconfParser {
	
	public static final String MISSING_SPECIFIER = "MissingSpecifier"; //$NON-NLS-1$
	public static final String INVALID_SPECIFIER = "InvalidSpecifier"; //$NON-NLS-1$
	public static final String INVALID_TERMINATION = "InvalidTermination"; //$NON-NLS-1$
	public static final String UNTERMINATED_CONSTRUCT = "UnterminatedConstruct"; //$NON-NLS-1$
	public static final String MISSING_CONDITION = "MissingCondition"; //$NON-NLS-1$
	public static final String INVALID_ELIF = "InvalidElif"; //$NON-NLS-1$
	public static final String INVALID_ELSE = "InvalidElse"; //$NON-NLS-1$
	public static final String INVALID_FI = "InvalidFi"; //$NON-NLS-1$
	public static final String INVALID_DONE = "InvalidDone"; //$NON-NLS-1$
	public static final String INVALID_ESAC = "InvalidEsac"; //$NON-NLS-1$
	public static final String INVALID_DO = "InvalidDo"; //$NON-NLS-1$
	public static final String INVALID_THEN = "InvalidThen"; //$NON-NLS-1$
	public static final String INVALID_IN = "InvalidIn"; //$NON-NLS-1$
	public static final String IMPROPER_CASE_CONDITION = "ImproperCaseCondition"; //$NON-NLS-1$
	public static final String UNTERMINATED_CASE_CONDITION = "UnterminatedCaseCondition"; //$NON-NLS-1$
	public static final String UNTERMINATED_INLINE_DOCUMENT = "UnterminatedInlineDocument"; //$NON-NLS-1$
	public static final String INCOMPLETE_INLINE_MARKER="IncompleteInlineMarker"; //$NON-NLS-1$
	public static final String MISSING_INLINE_MARKER="MissingInlineMarker"; //$NON-NLS-1$
	public static final String UNMATCHED_RIGHT_PARENTHESIS = "UnmatchedRightParenthesis"; //$NON-NLS-1$
	public static final String UNMATCHED_LEFT_PARENTHESIS = "UnmatchedLeftParenthesis"; //$NON-NLS-1$
	
	private IAutoconfErrorHandler errorHandler;
	private IAutoconfMacroValidator macroValidator;
	private AutoconfTokenizer tokenizer;
	private IAutoconfMacroDetector macroDetector;
	
	private static final String M4_BUILTINS =
		"define undefine defn pushdef popdef indir builtin ifdef ifelse shift reverse cond " + //$NON-NLS-1$
		"dumpdef traceon traceoff debugmode debugfile dnl changequote changecom changeword " + //$NON-NLS-1$
		"m4wrap " + //$NON-NLS-1$
		"include sinclude divert undivert divnum len index regexp substr translit patsubst " + //$NON-NLS-1$
		"format incr decr eval syscmd esyscmd sysval mkstemp maketemp errprint m4exit " + //$NON-NLS-1$
		"__file__ __line__ __program__ "; //$NON-NLS-1$
	
	private static List<String> m4builtins = new ArrayList<String>();
	static {
		m4builtins.addAll(Arrays.asList(M4_BUILTINS.split(" "))); //$NON-NLS-1$
	}
	
	/**
	 * Create a parser for autoconf-style sources.  
	 * @param errorHandler
	 * @param macroDetector
	 * @param macroValidator
	 */
	public AutoconfParser(IAutoconfErrorHandler errorHandler,
			IAutoconfMacroDetector macroDetector,
			IAutoconfMacroValidator macroValidator) {
		this.errorHandler = errorHandler;
		this.macroDetector = macroDetector;
		this.macroValidator = macroValidator;
	}

	/**
	 * Parse the given document and produce an AutoconfElement tree
	 * @param document
	 * @return element tree
	 */
	public AutoconfElement parse(IDocument document) {
		return parse(document, true);
	}

	/**
	 * Parse the given document and produce an AutoconfElement tree,
	 * and control whether the initial quoting style is m4 style (`...')
	 * or autoconf style ([ ... ]).
	 * @param errorHandler
	 * @param macroValidator
	 * @param document
	 * @param useAutoconfQuotes
	 * @return element tree
	 */
	public AutoconfElement parse(IDocument document, boolean useAutoconfQuotes) {
		this.tokenizer = new AutoconfTokenizer(document, errorHandler);
		if (useAutoconfQuotes)
			tokenizer.setM4Quote("[", "]"); //$NON-NLS-1$ //$NON-NLS-2$
		
		AutoconfElement root = new AutoconfRootElement();
		Token eof = parseTopLevel(root);
		
		root.setStartOffset(0);
		root.setDocument(document);
		
		setSourceEnd(root, eof);
		
		return root;
	}


	static class BlockEndCondition extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Token token;

		public BlockEndCondition(Token token) {
			this.token = token;
		}
		
		public Token getToken() {
			return token;
		}
		
	}

	static class ExprEndCondition extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Token token;

		public ExprEndCondition(Token token) {
			this.token = token;
		}
		
		public Token getToken() {
			return token;
		}
		
	}

	/**
	 * Parse individual top-level nodes: divide text into macro calls
	 * and recognized shell constructs.  Anything else is not accounted for.
	 * @param parent
	 * @return
	 */
	protected Token parseTopLevel(AutoconfElement parent) {
		while (true) {
			try {
				parseStatement(parent);
			} catch (BlockEndCondition e) {
				// don't terminate here; we may have constructs closed too early
				Token token = tokenizer.peekToken();
				if (token.getType() == ITokenConstants.EOF)
					return token; 
			}
		}
	}
	
	/**
	 * Parse a block of nodes, which starts with an expression and contains
	 * subnodes.  Divide text into macro calls and recognized shell constructs.  
	 * Anything else is not accounted for.
	 * @param parent
	 */
	protected void parseBlock(AutoconfElement parent, Token open, AutoconfElement block) throws BlockEndCondition {
		parent.addChild(block);
		
		setSourceStart(block, open);
		
		// get the expression part
		Token token;
		try {
			token = parseBlockExpression(open, block);
		} catch (BlockEndCondition e) {
			setSourceEndBefore(block, e.getToken());
			throw e;
		}
		
		// parse the block proper
		if (token.getType() != ITokenConstants.EOF) {
			while (true) {
				try {
					parseStatement(block);
				} catch (BlockEndCondition e) {
					setSourceEnd(block, e.getToken());
					return;
				}
			}
		} else {
			setSourceEnd(block, token);
		}
	}

	private Token parseBlockExpression(Token open, AutoconfElement block) throws BlockEndCondition {
		Token token;
		try {
			if (block instanceof AutoconfIfElement 
					|| block instanceof AutoconfElifElement 
					|| block instanceof AutoconfCaseElement 
					|| block instanceof AutoconfWhileElement) {
				token = parseExpression(block);
			} else if (block instanceof AutoconfForElement) {
				token = parseForExpression(block);
			} else {
				// no expression
				return open;
			}
			block.setVar(getTokenSpanTextBetween(open, token));
		} catch (BlockEndCondition e) {
			// oops, premature end
			setSourceEnd(block, e.getToken());
			throw e;
		}
	
		// check for expected token
		while (true) {
			token = tokenizer.readToken();
			if (token.getType() == ITokenConstants.EOF)
				break;
			if (token.getType() != ITokenConstants.EOL)
				break;
		}
		
		if (token.getType() == ITokenConstants.SH_DO) {
			checkBlockValidity(block, token, 
					new Class[] { AutoconfForElement.class, AutoconfWhileElement.class },
					INVALID_DO);
		}
		else if (token.getType() == ITokenConstants.SH_THEN) {
			checkBlockValidity(block, token, 
					new Class[] { AutoconfIfElement.class, AutoconfElifElement.class },
					INVALID_THEN);
		} 
		else {
			String exp;
			if (block instanceof AutoconfIfElement || block instanceof AutoconfElifElement)
				exp = "then";
			else
				exp = "do";
			
			handleError(block, token, AutoconfEditorMessages.getFormattedString(MISSING_SPECIFIER, exp));

			// assume we're still in the block...
			tokenizer.unreadToken(token);
		}
		return token;
	}
	
	/**
	 * Parse a case statement.  Scoop up statements into case conditional blocks.
	 * <pre>
	 * 'case' EXPR 'in' 
	 * 		{ EXPR ')'  { STMTS } ';;' }
	 * 'esac'
	 * </pre>
	 * @param parent
	 * @return
	 */
	protected void parseCaseBlock(AutoconfElement parent, Token open, AutoconfElement block) throws BlockEndCondition {
		parent.addChild(block);
		
		setSourceStart(block, open);
		
		// get the case expression, terminating at 'in'
		Token token;
		try {
			token = parseCaseExpression(block);
		} catch (BlockEndCondition e) {
			// oops, premature end
			setSourceEnd(block, e.getToken());
			throw e;
		}
		
		block.setVar(getTokenSpanTextBetween(open, token));
		
		// now get the statements, which are themselves blocks... just read statements
		// that terminate with ';;' and stuff those into blocks.
		
		while (true) {
			AutoconfCaseConditionElement condition = new AutoconfCaseConditionElement();

			// skip EOLs and get the first "real" token
			while (true) {
				token = tokenizer.readToken();
				setSourceStart(condition, token);
				if (token.getType() == ITokenConstants.EOF)
					break;
				if (token.getType() == ITokenConstants.EOL)
					continue;
				break;
			}
			
			if (token.getType() == ITokenConstants.SH_ESAC) {
				break;
			}
			
			try {
				Token start = token;
				token = parseCaseExpression(condition);
				condition.setName(getTokenSpanTextFromUpTo(start, token));
				
				while (true) {
					parseStatement(condition);
				}
			} catch (BlockEndCondition e) {
				setSourceEnd(condition, e.getToken());
				
				if (condition.getSource().length() > 0)
					block.addChild(condition);
				
				if (e.getToken().getType() != ITokenConstants.SH_CASE_CONDITION_END) {
					token = e.getToken();
					break;
				}
			}
		}
		
		setSourceEnd(block, token);
		
		if (token.getType() != ITokenConstants.SH_ESAC) {
			handleError(parent, token, AutoconfEditorMessages.getFormattedString(UNTERMINATED_CONSTRUCT, block.getName()));
		}
	}

	private String getTokenSpanTextBetween(Token open, Token close) {
		int startOffset = open.getOffset() + open.getLength();
		int endOffset = close.getOffset();
		if (open.getDocument() != close.getDocument())
			return open.getText();
		
		String text;
		try {
			text = open.getDocument().get(startOffset, endOffset - startOffset).trim();
		} catch (BadLocationException e) {
			text = open.getText();
			// TODO: error
		}
		
		return text;
	}

	private String getTokenSpanTextFromUpTo(Token open, Token close) {
		int startOffset = open.getOffset();
		int endOffset = close.getOffset();
		if (open.getDocument() != close.getDocument())
			return open.getText();
		
		String text;
		try {
			text = open.getDocument().get(startOffset, endOffset - startOffset).trim();
		} catch (BadLocationException e) {
			text = open.getText();
			// TODO: error
		}
		
		return text;
	}
	private void setSourceStart(AutoconfElement block, Token open) {
		int offset = open.getOffset();
		block.setDocument(open.getDocument());
		block.setStartOffset(offset);
	}

	private void setSourceEnd(AutoconfElement block, Token close) {
		int offset = close.getOffset() + close.getLength();
		if (block.getDocument() != null && block.getDocument() != close.getDocument())
			throw new IllegalStateException();
		block.setDocument(close.getDocument());
		block.setEndOffset(offset);
	}

	private void setSourceEndBefore(AutoconfElement block, Token close) {
		int offset = close.getOffset();
		if (block.getDocument() != null && block.getDocument() != close.getDocument())
			throw new IllegalStateException();
		block.setDocument(close.getDocument());
		block.setEndOffset(offset);
	}

	/**
	 * Parse a single statement (macro call or a shell construct).
	 * This can recursively invoke parseBlock() or parseStatement() to populate the tree.
	 * Whenever a token terminates a block, we check its validity and then throw BlockEndCondition.
	 * @param parent the parent into which to add statements.  The type of this element is used
	 * to validate the legality of block closing tokens.
	 */
	protected void parseStatement(AutoconfElement parent) throws BlockEndCondition {
	
		boolean atStart = true;
		
		while (true) {
			Token token = tokenizer.readToken();
			
			switch (token.getType()) {
			// 0. Check EOF
			case ITokenConstants.EOF:
				AutoconfElement element = parent;
				while (element != null && !(element instanceof AutoconfRootElement)) {
					handleError(element, token, AutoconfEditorMessages.getFormattedString(UNTERMINATED_CONSTRUCT, element.getName()));
					element = element.getParent();
				}
				throw new BlockEndCondition(token);
			
			
			// 1. Check for end of statement
			case ITokenConstants.EOL:
			case ITokenConstants.SEMI:
				return;
			
			
			// 2. Check macro expansions
			case ITokenConstants.WORD:
				checkMacro(parent, token);
				atStart = false;
				break;
			
			// Check for shell constructs.  These should appear at the start of a line
			// or after a semicolon.  If they don't,  just report an error and continue, 
			// to be tolerant of our own lax parsing.
			
			// 3.a) Check dollar variables
			case ITokenConstants.SH_DOLLAR:
				// skip the next token
				atStart = false;
				token = tokenizer.readToken();
				continue;
				
			// 3.b) Look for if/else/elif/fi constructs,
			// being tolerant of nesting problems by allowing
			// stranded else/elif nodes but reporting errors.
			case ITokenConstants.SH_IF:
				checkLineStart(parent, token, atStart);
				parseBlock(parent, token, new AutoconfIfElement());
				break;
				
			case ITokenConstants.SH_ELIF:
				checkLineStart(parent, token, atStart);
				checkBlockValidity(
						parent, token, 
						new Class[] { AutoconfIfElement.class, AutoconfElifElement.class },
						INVALID_ELIF);
				parseBlock(parent, token, new AutoconfElifElement());
				token = tokenizer.peekToken();
				throw new BlockEndCondition(token);
			
			case ITokenConstants.SH_ELSE:
				checkLineStart(parent, token, atStart);
				checkBlockValidity(
						parent, token, 
						new Class[] { AutoconfIfElement.class, AutoconfElifElement.class },
						INVALID_ELSE);
				parseBlock(parent, token, new AutoconfElseElement());
				token = tokenizer.peekToken();
				throw new BlockEndCondition(token);
			
			case ITokenConstants.SH_FI:
				checkLineStart(parent, token, atStart);
				checkBlockValidity(
						parent, token, 
						new Class[] { AutoconfIfElement.class, AutoconfElifElement.class, AutoconfElseElement.class },
						INVALID_FI);
				throw new BlockEndCondition(token);
			
				
			// 4. Look for for/while loops
			case ITokenConstants.SH_FOR:
				checkLineStart(parent, token, atStart);
				parseBlock(parent, token, new AutoconfForElement());
				break;
				
			case ITokenConstants.SH_WHILE:
				checkLineStart(parent, token, atStart);
				parseBlock(parent, token, new AutoconfWhileElement());
				break;
				
			case ITokenConstants.SH_UNTIL:
				checkLineStart(parent, token, atStart);
				parseBlock(parent, token, new AutoconfUntilElement());
				break;
				
			case ITokenConstants.SH_SELECT:
				checkLineStart(parent, token, atStart);
				parseBlock(parent, token, new AutoconfSelectElement());
				break;
				
			case ITokenConstants.SH_DONE:
				checkLineStart(parent, token, atStart);
				checkBlockValidity(
						parent, token, 
						new Class[] { AutoconfForElement.class, AutoconfWhileElement.class,
								AutoconfUntilElement.class, AutoconfSelectElement.class },
						INVALID_DONE);
				throw new BlockEndCondition(token);
			
			// 5. Look for case statements
			case ITokenConstants.SH_CASE:
				checkLineStart(parent, token, atStart);
				parseCaseBlock(parent, token, new AutoconfCaseElement());
				break;
				
			case ITokenConstants.SH_CASE_CONDITION_END:
				checkBlockValidity(
						parent, token, 
						new Class[] { AutoconfCaseConditionElement.class },
						IMPROPER_CASE_CONDITION);
				throw new BlockEndCondition(token);
			
			case ITokenConstants.SH_ESAC:
				checkLineStart(parent, token, atStart);
				checkBlockValidity(
						parent, token, 
						// note: we don't strictly recurse here, so accept either parent
						new Class[] { AutoconfCaseElement.class, AutoconfCaseConditionElement.class },
						INVALID_ESAC);
				throw new BlockEndCondition(token);
			
			
			// 6. Check for HERE documents
			case ITokenConstants.SH_HERE:
			case ITokenConstants.SH_HERE_DASH:

				parseHERE(parent, token);
				break;
			}
		}
	}

	private void checkLineStart(AutoconfElement parent, Token token, boolean atStart) {
		if (!atStart) {
			handleError(parent, token, AutoconfEditorMessages.getFormattedString(INVALID_TERMINATION, token.getText()));
		}
	}

	/**
	 * Parse the Here document, whose control token is provided (SH_HERE or SH_HERE_DASH).
	 * Contents are thrown away except for any macro calls.
	 * @param parent
	 * @param controlToken
	 */
	private void parseHERE(AutoconfElement parent, Token controlToken) {
		Token token = tokenizer.readToken();
		if (token.getType() == ITokenConstants.EOL || token.getType() == ITokenConstants.EOF) {
			handleError(parent, token,
					AutoconfEditorMessages.getString(INCOMPLETE_INLINE_MARKER));
			
		} else {
			String hereTag = token.getText();
		
			boolean atEOL = false;
			while (true) {
				token = tokenizer.readToken();
				if (token.getType() == ITokenConstants.EOF) {
					handleError(parent, token, AutoconfEditorMessages.getFormattedString(UNTERMINATED_CONSTRUCT, parent.getName()));
					break;
				} else if (token.getType() == ITokenConstants.EOL) {
					atEOL = true;
				} else {
					if (atEOL && token.getText().equals(hereTag)) {
						// only the end if it is also followed by EOL without any whitespace
						Token eol = tokenizer.readToken();
						if (eol.getType() == ITokenConstants.EOL && eol.getOffset() == token.getOffset() + token.getLength()) {
							break;
						}
					}
					if (token.getType() == ITokenConstants.WORD) {
						checkMacro(parent, token);
					}
					atEOL = false;
				}
			}
		}
	}

	/**
	 * Parse through a single expression up to a semicolon or newline.
	 * Add a macro call to the element or just return upon finding the desired token.
	 * Whenever a token terminates the expression, we check its validity and return the final token
	 * Throw {@link BlockEndCondition} if an unexpected token was found.
	 * @param parent the parent into which to add statements.  The type of this element is used
	 * to validate the legality of block closing tokens.
	 */
	protected Token parseExpression(AutoconfElement parent) throws BlockEndCondition {
	
		while (true) {
			Token token = tokenizer.readToken();
			
			// 0. Ignore comments (tokenizer skipped them!)
			
			switch (token.getType()) {
			// 1. Check EOF
			case ITokenConstants.EOF:
				throw new BlockEndCondition(token);
		
			// 2. Check macro expansions
			case ITokenConstants.WORD:
				token = checkMacro(parent, token);
				break;
			
			// 3. Check expression terminators
			case ITokenConstants.SEMI:
			case ITokenConstants.EOL:
				return token;

			// 4. Handle variables
			case ITokenConstants.SH_DOLLAR:
				token = tokenizer.readToken();
				break;
				
			case ITokenConstants.SH_IN:
				// in 'for' or 'select, an 'in' may occur before 'do'
				if (!(parent instanceof AutoconfForElement)
						&& !(parent instanceof AutoconfSelectElement)) 
					return token;
				// fall through
				
			// 5. Abort on unexpected tokens
			case ITokenConstants.SH_DO:
			case ITokenConstants.SH_THEN:
				handleError(parent, token, AutoconfEditorMessages.getFormattedString(INVALID_SPECIFIER, token.getText()));
				tokenizer.unreadToken(token);
				// close enough...
				return token;
				
			case ITokenConstants.SH_ESAC:
			case ITokenConstants.SH_CASE:
			case ITokenConstants.SH_CASE_CONDITION_END:
			case ITokenConstants.SH_FOR:
			case ITokenConstants.SH_IF:
			case ITokenConstants.SH_ELIF:
			case ITokenConstants.SH_ELSE:
			case ITokenConstants.SH_FI:
			case ITokenConstants.SH_DONE:
				handleError(parent, token, AutoconfEditorMessages.getFormattedString(UNTERMINATED_CONSTRUCT, parent.getName()));
				tokenizer.unreadToken(token);
				throw new BlockEndCondition(token);
			}
		}
	}

	/**
	 * Parse through a single expression up to 'do'.
	 * Add a macro call to the element or just return upon finding the desired token.
	 * Whenever a token terminates the expression, we check its validity and then throw ExprEndCondition.
	 * Throw {@link BlockEndCondition} if an unexpected token was found.
	 * @param parent the parent into which to add statements.  The type of this element is used
	 * to validate the legality of block closing tokens.
	 */
	protected Token parseForExpression(AutoconfElement parent) throws BlockEndCondition {
		while (true) {
			Token token = tokenizer.readToken();
			
			// 0. Ignore comments (tokenizer skipped them!)
			
			// 1. Check EOF
			if (token.getType() == ITokenConstants.EOF) {
				throw new BlockEndCondition(token);
			}
		
			// 2. Check macro expansions
			else if (token.getType() == ITokenConstants.WORD) {
				token = checkMacro(parent, token);
			}
			
			// 3. Check expression terminators -- not ';' here, but 'do'
			else if (token.getType() == ITokenConstants.SH_DO) {
				tokenizer.unreadToken(token);
				return tokenizer.peekToken();
			}
			
			// 4. Abort on unexpected tokens
			else switch (token.getType()) {
			case ITokenConstants.SH_THEN:
				handleError(parent, token, AutoconfEditorMessages.getFormattedString(INVALID_SPECIFIER, token.getText()));
				tokenizer.unreadToken(token);
				// close enough...
				//throw new ExprEndCondition(token);
				return token;
				
			case ITokenConstants.SH_ESAC:
			case ITokenConstants.SH_CASE:
			case ITokenConstants.SH_CASE_CONDITION_END:
			case ITokenConstants.SH_FOR:
			case ITokenConstants.SH_IF:
			case ITokenConstants.SH_ELIF:
			case ITokenConstants.SH_ELSE:
			case ITokenConstants.SH_FI:
			case ITokenConstants.SH_DONE:
				handleError(parent, token, AutoconfEditorMessages.getFormattedString(UNTERMINATED_CONSTRUCT, parent.getName()));
				tokenizer.unreadToken(token);
				throw new BlockEndCondition(token);
			}
		}
	}

	/**
	 * Parse through a single expression up to 'in'.
	 * Add a macro call to the element or just return upon finding the desired token.
	 * Whenever a token terminates the expression, we check its validity and then throw ExprEndCondition.
	 * Throw {@link BlockEndCondition} if an unexpected token was found.
	 * @param parent the parent into which to add statements.  The type of this element is used
	 * to validate the legality of block closing tokens.
	 */
	protected Token parseCaseExpression(AutoconfElement parent) throws BlockEndCondition {
		while (true) {
			Token token = tokenizer.readToken();
			
			// 0. Ignore comments (tokenizer skipped them!)
			
			// 1. Check EOF
			if (token.getType() == ITokenConstants.EOF) {
				throw new BlockEndCondition(token);
			}
		
			// 2. Check macro expansions
			else if (token.getType() == ITokenConstants.WORD) {
				token = checkMacro(parent, token);
			}
			
			// 3. Check expression terminators
			else if (parent instanceof AutoconfCaseElement && token.getType() == ITokenConstants.SH_IN) {
				return token;
			}
			else if (parent instanceof AutoconfCaseConditionElement && token.getType() == ITokenConstants.RPAREN) {
				return token;
			}
			
			// 4. Abort on unexpected tokens
			else switch (token.getType()) {
			case ITokenConstants.SEMI:
			case ITokenConstants.SH_IN:
			case ITokenConstants.RPAREN:
			case ITokenConstants.SH_DO:
			case ITokenConstants.SH_THEN:
				if (parent instanceof AutoconfCaseElement)
					handleError(parent, token, AutoconfEditorMessages.getString(INVALID_IN));
				else
					handleError(parent, token, AutoconfEditorMessages.getString(IMPROPER_CASE_CONDITION));
				// close enough...
				return token;
				
			case ITokenConstants.SH_ESAC:
			case ITokenConstants.SH_CASE:
			case ITokenConstants.SH_CASE_CONDITION_END:
			case ITokenConstants.SH_FOR:
			case ITokenConstants.SH_IF:
			case ITokenConstants.SH_ELIF:
			case ITokenConstants.SH_ELSE:
			case ITokenConstants.SH_FI:
			case ITokenConstants.SH_DONE:
				handleError(parent, token, AutoconfEditorMessages.getFormattedString(UNTERMINATED_CONSTRUCT, parent.getName()));
				tokenizer.unreadToken(token);
				throw new BlockEndCondition(token);
			}
		}
	}
	
	/**
	 * Check a given close block token against the current parent by checking that
	 * the parent's class is one of classes,  If a match happens,
	 * optionally push back the token if it will be used to parse new statements in the parent.
	 * Report an error if the parent is not one of the expected kinds.
	 * @param parent
	 * @param token
	 * @param classes
	 */
	private void checkBlockValidity(
			AutoconfElement parent, 
			Token token,
			Class<?>[] classes,
			String errorMessage) {
		for (int i = 0; i < classes.length; i++) {
			if (classes[i].isInstance(parent)) {
				return;
			}
		}
		
		// not a match
		handleError(parent, token, 
				AutoconfEditorMessages.getFormattedString(errorMessage, parent.getName(), token.getText()));
	}

	/**
	 * Creates the appropriate macro type object depending on the
	 * macro name.
	 * 
	 * @return
	 */
	private AutoconfMacroElement createMacroElement (String name){
		if (name.equals("AC_INIT")){ //$NON-NLS-1$
			return new AcInitElement(name);
		}
		return new AutoconfMacroElement(name);
	}

	/**
	 * Check whether the given token is part of a macro, and parse it as such
	 * if necessary.  
	 * @param parent
	 * @param token
	 * @return Token last read for the macro call
	 */
	private Token checkMacro(AutoconfElement parent, Token token) {
		String name = token.getText();
		
		boolean hasArguments = tokenizer.peekToken().getType() == ITokenConstants.LPAREN;
		if (macroDetector != null && macroDetector.isMacroIdentifier(name)) {
			// ok
		} else if (m4builtins.contains(name)) {
			// all of these except dnl take arguments
			if (!name.equals("dnl") && !hasArguments) //$NON-NLS-1$
				return token;
		} else {
			return token;
		}
		
		AutoconfMacroElement macro = createMacroElement(name);
		token = parseMacro(macro, token);					
		
		// handle special macros here
		if ("dnl".equals(name)) { //$NON-NLS-1$
			// consume to EOL
			while (true) {
				token = tokenizer.readToken();
				if (token.getType() == ITokenConstants.EOF || token.getType() == ITokenConstants.EOL)
					break;
			}
			
			// ignore macro entirely
			macro = null;
		} else if ("changequote".equals(name)) { //$NON-NLS-1$
			// change quote delimiters
			validateMacroParameterCount(macro, token, 2);
			
			// GNU behavior for invalid argument count
			String parm0 = "`"; //$NON-NLS-1$
			String parm1 = "'"; //$NON-NLS-1$
			if (macro.getParameterCount() >= 1)
				parm0 = macro.getParameter(0);
			if (macro.getParameterCount() >= 2)
				parm1 = macro.getParameter(1);
			
			tokenizer.setM4Quote(parm0, parm1);
		}  else if ("changecom".equals(name)) { //$NON-NLS-1$
			// change comment delimiters
			validateMacroParameterCount(macro, token, 2);
			
			// GNU behavior for invalid argument count
			String parm0 = "#"; //$NON-NLS-1$
			String parm1 = "\n"; //$NON-NLS-1$
			if (macro.getParameterCount() >= 1)
				parm0 = macro.getParameter(0);
			if (macro.getParameterCount() >= 2)
				parm1 = macro.getParameter(1);
			
			tokenizer.setM4Comment(parm0, parm1);
		}
		
		if (macro != null) {
			parent.addChild(macro);
		}
		
		// now validate that the macro is properly terminated
		if (!(parent instanceof AutoconfMacroArgumentElement) 
				&& !(parent instanceof AutoconfMacroElement)
				&& !(parent instanceof AutoconfForElement)) {
			Token peek = tokenizer.peekToken();
			if (peek.getType() == ITokenConstants.RPAREN) {
				handleError(macro, peek, AutoconfEditorMessages.getString(UNMATCHED_RIGHT_PARENTHESIS)); 
			}
		}
		
		return token;
	}

	private void validateMacroParameterCount(AutoconfMacroElement macro, Token token, int count) {
		if (macro.getParameterCount() < count) {
			handleError(macro, token, AutoconfEditorMessages.getFormattedString("M4MacroArgsTooFew", 
					macro.getName(), Integer.valueOf(2))); //$NON-NLS-1$ 
		} else if (macro.getParameterCount() > count) {
			handleError(macro, token, AutoconfEditorMessages.getFormattedString("M4MacroArgsTooMany", 
					macro.getName(), Integer.valueOf(2))); //$NON-NLS-1$
		}
	}

	/**
	 * Start parsing a macro call at a suspected macro expansion location.
	 * @param macro
	 * @param line the line containing the start of the 
	 * @param parent
	 * @return last token parsed
	 */
	protected Token parseMacro(AutoconfMacroElement macro, Token macroName) {
		setSourceStart(macro, macroName);
		
		// parse any arguments
		tokenizer.setM4Context(true);
		
		Token token = tokenizer.readToken();
		if (token.getType() == ITokenConstants.LPAREN) {
			token = parseMacroArguments(macro, token);
			setSourceEnd(macro, token);
		} else {
			tokenizer.unreadToken(token);
			setSourceEnd(macro, macroName);
		}
		
		tokenizer.setM4Context(false);
		
		// validate macro arguments?
		if (macroValidator != null) {
			try {
				macroValidator.validateMacroCall(macro);
			} catch (ParseException e) {
				errorHandler.handleError(e);
			} catch (InvalidMacroException e) {
				handleError(e);
			}
		}
		
		return token;
	}

	/**
	 * Parse the arguments for the given macro.  These are not interpreted as shell
	 * constructs but just as text with possibly more macro expansions inside.
	 * @param macro
	 * @return final token (')')
	 */
	protected Token parseMacroArguments(AutoconfMacroElement macro, Token lparen) {
		Token argStart = null;
		Token argEnd = null;
		Token token;
		
		// When parsing, we want to ignore the whitespace around the arguments.
		// So, instead of taking the source range "between" a parenthesis and a comma,
		// track the exact tokens forming the start and end of an argument, defaulting
		// to the borders of the parentheses and commas if no text is included.
		
		StringBuffer argBuffer = new StringBuffer();
		AutoconfMacroArgumentElement arg = new AutoconfMacroArgumentElement();

		while (true) {
			token = tokenizer.readToken();
			
			if (token.getType() == ITokenConstants.EOL) {
				if (argBuffer.length() > 0)
					argBuffer.append(token.getText());
				continue;
			}
			
			if (token.getType() == ITokenConstants.COMMA 
					|| token.getType() == ITokenConstants.RPAREN
					|| token.getType() == ITokenConstants.EOF) {

				arg.setName(argBuffer.toString());
				argBuffer.setLength(0);
				
				if (argStart != null && argEnd != null) {
					setSourceStart(arg, argStart);
					setSourceEnd(arg, argEnd);
				} else if (argEnd != null) {
					setSourceStart(arg, argStart);
					setSourceEndBefore(arg, token);
				} else {
					// empty argument
					setSourceStart(arg, token);
					setSourceEndBefore(arg, token);
				}
				
				macro.addChild(arg);
				
				if (token.getType() != ITokenConstants.COMMA)
					break;
				
				argStart = null;
				argEnd = null;
				
				arg = new AutoconfMacroArgumentElement();

			} else {
				if (argStart == null) {
					argStart = token;
				}
				argEnd = token;
				
				if (argBuffer.length() > 0 && token.followsSpace())
					argBuffer.append(' ');
				argBuffer.append(token.getText());
				
				// handle nested macro calls in arguments
				if (token.getType() == ITokenConstants.WORD) {
					argEnd = checkMacro(arg, token);
				}
			}
		}
		
		if (token.getType() != ITokenConstants.RPAREN) {
			handleError(macro, token, AutoconfEditorMessages.getString(UNMATCHED_LEFT_PARENTHESIS)); 
		}
		
		// note: moved 15-char truncation to AutoconfLabelProvider
		AutoconfElement[] children = macro.getChildren();
		if (children.length > 0)
			macro.setVar(children[0].getVar());
		
		return token;
	}
	

	
	protected void handleError(AutoconfElement element, Token token, String message) {
		handleMessage(element, token, message, IMarker.SEVERITY_ERROR);
	}
	protected void handleWarning(AutoconfElement element, Token token, String message) {
		handleMessage(element, token, message, IMarker.SEVERITY_WARNING);
	}
	protected void handleMessage(AutoconfElement element, Token token, String message, int severity) {
		if (errorHandler != null) {
			int lineNumber = 0;
			int startColumn = 0;
			int endColumn = 0;
			try {
				lineNumber = token.getDocument().getLineOfOffset(token.getOffset());
				int lineOffs = token.getDocument().getLineOffset(lineNumber);
				startColumn = token.getOffset() - lineOffs;
				endColumn = startColumn + token.getLength();
			} catch (BadLocationException e) {
				// Don't care if we blow up trying to issue marker
			}
			errorHandler.handleError(new ParseException(
					message,
					token.getOffset(),
					token.getOffset() + token.getLength(),
					lineNumber, 
					startColumn, endColumn,
					severity));
		}
	}

	/**
	 * Figure out the error location and create a marker and message for it.
	 * @param exception
	 */
	protected void handleError (InvalidMacroException exception) {
		AutoconfElement element = exception.getBadElement();
		
		if (errorHandler != null) {
			int lineNumber = 0;
			int startColumn = 0;
			int endColumn = 0;
			try {
				lineNumber = element.getDocument().getLineOfOffset(element.getStartOffset());
				int lineOffs = element.getDocument().getLineOffset(lineNumber);
				startColumn = element.getStartOffset() - lineOffs;
				endColumn = startColumn + element.getLength();
			} catch (BadLocationException e) {
				// Don't care if we blow up trying to issue marker
			}
			errorHandler.handleError(new ParseException(
					exception.getMessage(),
					element.getStartOffset(),
					element.getEndOffset(),
					lineNumber, 
					startColumn, endColumn,
					IMarker.SEVERITY_ERROR));
		}
	}

	public IAutoconfErrorHandler getErrorHandler() {
		return errorHandler;
	}
}

