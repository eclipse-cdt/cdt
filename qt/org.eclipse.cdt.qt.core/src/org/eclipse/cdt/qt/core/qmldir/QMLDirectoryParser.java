/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmldir;

import java.io.InputStream;
import java.util.Stack;

import org.eclipse.cdt.internal.qt.core.location.SourceLocation;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirAST;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirASTNode;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirClassnameCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirCommentCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirDependsCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirDesignerSupportedCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirInternalCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirModuleCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirPluginCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirResourceCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirSingletonCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirSyntaxError;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirTypeInfoCommand;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirVersion;
import org.eclipse.cdt.internal.qt.core.qmldir.QDirWord;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryLexer.Token;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryLexer.TokenType;

/**
 * Converts an <code>InputStream</code> representing a qmldir file into an Abstract Syntax Tree. Uses the {@link QMLDirectoryLexer}
 * under the hood to match tokens which it then uses to construct the AST. Also, a <code>QMLDirectoryParser</code> has the ability
 * to skip over syntax errors and include them in its AST rather than returning upon the first error.
 */
public class QMLDirectoryParser {
	/**
	 * An exception thrown when a <code>QMLDirectoryParser</code> encounters a syntax error. This class stores information on the
	 * offending token as well as the node the parser was working on before it failed (if available).
	 */
	public static class SyntaxError extends RuntimeException {
		private static final long serialVersionUID = 6608815552297970623L;

		private final IQDirASTNode incompleteNode;
		private final Token offendingToken;

		/**
		 * Creates a new <code>SyntaxError</code>.
		 *
		 * @param node
		 *            the incomplete working node
		 * @param token
		 *            the offending token
		 * @param message
		 *            the message to display
		 */
		public SyntaxError(IQDirASTNode node, Token token, String message) {
			super(message);
			this.incompleteNode = node;
			this.offendingToken = token;
		}

		/**
		 * Gets the token that caused the parser to fail.
		 *
		 * @return the offending token
		 */
		public Token getOffendingToken() {
			return offendingToken;
		}

		/**
		 * Gets the last node that the parser was working on before it failed or null if that information isn't present.
		 *
		 * @return the incomplete node or <code>null</code> if not available
		 */
		public IQDirASTNode getIncompleteNode() {
			return incompleteNode;
		}
	}

	private final QMLDirectoryLexer lexer;
	private final Stack<QDirASTNode> workingNodes;
	private Token tok;

	/**
	 * Initializes a new <code>QMLDirectoryParser</code> capable of parsing an <code>InputStream</code> and returning an AST.
	 */
	public QMLDirectoryParser() {
		this.lexer = new QMLDirectoryLexer();
		this.workingNodes = new Stack<>();
	}

	/**
	 * Parses the given <code>InputStream</code> into an Abstract Syntax Tree. This is a helper method equivalent to
	 * <code>parse(input, true)</code>. That is, the parser will attempt to recover once it hits an error and include an
	 * {@link IQDirSyntaxError} node in the AST.
	 *
	 * @param input
	 *            the input to parse
	 * @return the Abstract Syntax Tree representing the input
	 * @see QMLDirectoryParser#parse(InputStream, boolean)
	 */
	public IQDirAST parse(InputStream input) {
		return parse(input, true);
	}

	/**
	 * Parses the given <code>InputStream</code> into an Abstract Syntax Tree. If <code>tolerateErrors</code> is <code>true</code>,
	 * any syntax errors will be included in the AST as a separate {@link IQDirSyntaxErrorCommand}. The parser will then attempt to
	 * recover by jumping to the next line and continue parsing. A value of </code>false</code> tells the parser to throw a
	 * {@link SyntaxError} on the first problem it encounters.
	 *
	 * @param input
	 *            the input to parse
	 * @param tolerateErrors
	 *            whether or not the parser should be error tolerant
	 * @return the Abstract Syntax Tree representing the input
	 */
	public IQDirAST parse(InputStream input, boolean tolerateErrors) {
		// Clear out any leftover state
		this.lexer.setInput(input);
		this.workingNodes.clear();

		QDirAST ast = new QDirAST();
		nextToken();
		while (tok.getType() != TokenType.EOF) {
			try {
				switch (tok.getType()) {
				case MODULE:
					ast.addCommand(parseModuleCommand());
					break;
				case SINGLETON:
					ast.addCommand(parseSingletonCommand());
					break;
				case INTERNAL:
					ast.addCommand(parseInternalCommand());
					break;
				case WORD:
					ast.addCommand(parseResourceCommand());
					break;
				case PLUGIN:
					ast.addCommand(parsePluginCommand());
					break;
				case CLASSNAME:
					ast.addCommand(parseClassnameCommand());
					break;
				case TYPEINFO:
					ast.addCommand(parseTypeInfoCommand());
					break;
				case DEPENDS:
					ast.addCommand(parseDependsCommand());
					break;
				case DESIGNERSUPPORTED:
					ast.addCommand(parseDesignerSupportedCommand());
					break;
				case COMMENT:
					ast.addCommand(parseCommentCommand());
					break;
				case COMMAND_END:
					// This is just an empty line that should be ignored
					nextToken();
					break;
				default:
					throw unexpectedToken();
				}
			} catch (SyntaxError e) {
				if (!tolerateErrors) {
					throw e;
				}
				// Add the syntax error to the AST and jump to the next line
				QDirSyntaxError errNode = new QDirSyntaxError(e);
				markStart(errNode);
				IQDirASTNode node = e.getIncompleteNode();
				if (node != null) {
					errNode.setLocation((SourceLocation) node.getLocation());
					errNode.setStart(node.getStart());
					errNode.setEnd(node.getEnd());
				}
				while (!eat(TokenType.COMMAND_END) && !eat(TokenType.EOF)) {
					nextToken();
				}
				markEnd();
				ast.addCommand(errNode);
			}
		}
		return ast;
	}

	private void nextToken() {
		nextToken(true);
	}

	private void nextToken(boolean skipWhitespace) {
		tok = lexer.nextToken(skipWhitespace);
	}

	private void markStart(QDirASTNode node) {
		workingNodes.push(node);
		node.setStart(tok.getStart());
		node.setLocation(new SourceLocation());
		node.getLocation().setStart(tok.getLocation().getStart());
	}

	private void markEnd() {
		QDirASTNode node = workingNodes.pop();
		node.setEnd(tok.getEnd());
		node.getLocation().setEnd(tok.getLocation().getEnd());
	}

	private boolean eat(TokenType type) {
		if (tok.getType() == type) {
			nextToken();
			return true;
		}
		return false;
	}

	private SyntaxError syntaxError(String message) {
		return new SyntaxError(workingNodes.peek(), tok, message + " " + tok.getLocation().getStart().toString()); //$NON-NLS-1$
	}

	private SyntaxError unexpectedToken() {
		String tokenText = tok.getText();
		if (tok.getType() == TokenType.EOF) {
			tokenText = "EOF"; //$NON-NLS-1$
		}
		return syntaxError("Unexpected token '" + tokenText + "'"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void expect(TokenType type) {
		if (tok.getType() != type) {
			throw unexpectedToken();
		}
		nextToken();
	}

	private void expectCommandEnd() {
		// Allow EOF to be substituted for COMMAND_END
		if (tok.getType() == TokenType.EOF) {
			nextToken();
			return;
		}
		if (tok.getType() != TokenType.COMMAND_END) {
			throw syntaxError("Expected token '\\n' or 'EOF', but saw '" + tok.getText() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		nextToken();
	}

	private QDirModuleCommand parseModuleCommand() {
		QDirModuleCommand node = new QDirModuleCommand();
		markStart(node);
		expect(TokenType.MODULE);
		if (tok.getType() == TokenType.WORD) {
			node.setModuleIdentifier(parseWord());
			expectCommandEnd();
			markEnd();
			return node;
		}
		throw unexpectedToken();
	}

	private QDirSingletonCommand parseSingletonCommand() {
		QDirSingletonCommand node = new QDirSingletonCommand();
		markStart(node);
		expect(TokenType.SINGLETON);
		if (tok.getType() == TokenType.WORD) {
			node.setTypeName(parseWord());
			if (tok.getType() == TokenType.DECIMAL) {
				node.setInitialVersion(parseVersion());
				if (tok.getType() == TokenType.WORD) {
					node.setFile(parseWord());
					expectCommandEnd();
					markEnd();
					return node;
				}
			}
		}
		throw unexpectedToken();
	}

	private QDirInternalCommand parseInternalCommand() {
		QDirInternalCommand node = new QDirInternalCommand();
		markStart(node);
		expect(TokenType.INTERNAL);
		if (tok.getType() == TokenType.WORD) {
			node.setTypeName(parseWord());
			if (tok.getType() == TokenType.WORD) {
				node.setFile(parseWord());
				expectCommandEnd();
				markEnd();
				return node;
			}
		}
		throw unexpectedToken();
	}

	private QDirResourceCommand parseResourceCommand() {
		QDirResourceCommand node = new QDirResourceCommand();
		markStart(node);
		if (tok.getType() == TokenType.WORD) {
			node.setResourceIdentifier(parseWord());
			if (tok.getType() == TokenType.DECIMAL) {
				node.setInitialVersion(parseVersion());
				if (tok.getType() == TokenType.WORD) {
					node.setFile(parseWord());
					expectCommandEnd();
					markEnd();
					return node;
				}
			}
		}
		throw unexpectedToken();
	}

	private QDirPluginCommand parsePluginCommand() {
		QDirPluginCommand node = new QDirPluginCommand();
		markStart(node);
		expect(TokenType.PLUGIN);
		if (tok.getType() == TokenType.WORD) {
			node.setName(parseWord());
			if (tok.getType() == TokenType.WORD) {
				node.setPath(parseWord());
			}
			expectCommandEnd();
			markEnd();
			return node;
		}
		throw unexpectedToken();
	}

	private QDirClassnameCommand parseClassnameCommand() {
		QDirClassnameCommand node = new QDirClassnameCommand();
		markStart(node);
		expect(TokenType.CLASSNAME);
		if (tok.getType() == TokenType.WORD) {
			node.setIdentifier(parseWord());
			expectCommandEnd();
			markEnd();
			return node;
		}
		throw unexpectedToken();
	}

	private QDirTypeInfoCommand parseTypeInfoCommand() {
		QDirTypeInfoCommand node = new QDirTypeInfoCommand();
		markStart(node);
		expect(TokenType.TYPEINFO);
		if (tok.getType() == TokenType.WORD) {
			node.setFile(parseWord());
			expectCommandEnd();
			markEnd();
			return node;
		}
		throw unexpectedToken();
	}

	private QDirDependsCommand parseDependsCommand() {
		QDirDependsCommand node = new QDirDependsCommand();
		markStart(node);
		expect(TokenType.DEPENDS);
		if (tok.getType() == TokenType.WORD) {
			node.setModuleIdentifier(parseWord());
			if (tok.getType() == TokenType.DECIMAL) {
				node.setInitialVersion(parseVersion());
				expectCommandEnd();
				markEnd();
				return node;
			}
		}
		throw unexpectedToken();
	}

	private QDirDesignerSupportedCommand parseDesignerSupportedCommand() {
		QDirDesignerSupportedCommand node = new QDirDesignerSupportedCommand();
		markStart(node);
		expect(TokenType.DESIGNERSUPPORTED);
		expectCommandEnd();
		markEnd();
		return node;
	}

	private QDirCommentCommand parseCommentCommand() {
		QDirCommentCommand node = new QDirCommentCommand();
		markStart(node);
		if (tok.getType() == TokenType.COMMENT) {
			node.setText(tok.getText());
			nextToken();
			expectCommandEnd();
			markEnd();
			return node;
		}
		throw unexpectedToken();
	}

	private QDirVersion parseVersion() {
		QDirVersion node = new QDirVersion();
		markStart(node);
		if (tok.getType() == TokenType.DECIMAL) {
			node.setVersionString(tok.getText());
			nextToken();
			markEnd();
			return node;
		}
		throw unexpectedToken();
	}

	private QDirWord parseWord() {
		QDirWord node = new QDirWord();
		markStart(node);
		if (tok.getType() == TokenType.WORD) {
			node.setText(tok.getText());
			nextToken();
			markEnd();
			return node;
		}
		throw unexpectedToken();
	}
}
