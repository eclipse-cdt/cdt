/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

import lpg.lpgjavaruntime.IToken;

@SuppressWarnings("restriction")
public abstract class AbstractParserAction {

	/**
	 * Used with very simple optional rules that just say
	 * that some particular token or keyword is optional.
	 * The presence of the PLACE_HOLDER on the stack means that the keyword
	 * was parsed, the presence of null means the keyword wasn't parsed.
	 *
	 * @see BuildASTParserAction#consumePlaceHolder()
	 * @see BuildASTParserAction#consumeEmpty()
	 */
	protected static final Object PLACE_HOLDER = Boolean.TRUE; // any object will do

	/** Provides an interface to the token stream */
	protected final ITokenStream stream;

	/** Stack that holds the intermediate nodes as the AST is being built */
	protected final ScopedStack<Object> astStack;

	/** The completion node, only generated during a completion parse */
	protected ASTCompletionNode completionNode;

	/** Options that change the behavior of the parser actions */
	protected Map<String, String> properties = Collections.emptyMap();

	/**
	 * Completion tokens are represented by different kinds by different parsers.
	 */
	protected abstract boolean isCompletionToken(IToken token);

	protected abstract IASTName createName(char[] image);

	/**
	 * Create a new parser action.
	 * @param tu Root node of the AST, its list of declarations should be empty.
	 * @throws NullPointerException if any of the parameters are null
	 */
	public AbstractParserAction(ITokenStream parser, ScopedStack<Object> astStack) {
		if (parser == null)
			throw new NullPointerException("parser is null"); //$NON-NLS-1$
		if (astStack == null)
			throw new NullPointerException("astStack is null"); //$NON-NLS-1$

		this.stream = parser;
		this.astStack = astStack;
	}

	protected void setOffsetAndLength(IASTNode node) {
		int ruleOffset = stream.getLeftIToken().getStartOffset();
		int ruleLength = stream.getRightIToken().getEndOffset() - ruleOffset;
		((ASTNode) node).setOffsetAndLength(ruleOffset, ruleLength < 0 ? 0 : ruleLength);
	}

	/**
	 * Creates a IASTName node from an identifier token.
	 * If the token is a completion token then it is added to the completion node.
	 */
	protected IASTName createName(IToken token) {
		IASTName name = createName(token.toString().toCharArray()); // TODO, token.toCharArray();
		ParserUtil.setOffsetAndLength(name, token);

		if (isCompletionToken(token))
			addNameToCompletionNode(name, token.toString());

		return name;
	}

	public void setParserProperties(Map<String, String> properties) {
		this.properties = properties == null ? Collections.<String, String>emptyMap() : properties;
	}

	/**
	 * Creates a completion node if one does not yet exist and adds the
	 * given name to it.
	 */
	protected void addNameToCompletionNode(IASTName name, String prefix) {
		if (completionNode == null) {
			completionNode = newCompletionNode(prefix);
		}

		completionNode.addName(name);
	}

	public ASTCompletionNode newCompletionNode(String prefix) {
		return new ASTCompletionNode(prefix);
	}

	/**
	 * Returns the completion node if this is a completion parse, null otherwise.
	 */
	public IASTCompletionNode getASTCompletionNode() {
		return completionNode;
	}

	/**
	 * Returns the parse result.
	 * @return
	 */
	public IASTNode getParseResult() {
		return (IASTNode) astStack.peek();
	}

	/**
	 * Runs the given parser on the given token list.
	 *
	 */
	protected <N extends IASTNode> N runSecondaryParser(ISecondaryParser<N> secondaryParser) {
		return runSecondaryParser(secondaryParser, stream.getRuleTokens());
	}

	/**
	 * Runs the given parser on the tokens that make up the current rule.
	 */
	protected <N extends IASTNode> N runSecondaryParser(ISecondaryParser<N> secondaryParser, List<IToken> tokens) {
		// the secondary parser will alter the token kinds, which will need to be undone
		int[] savedKinds = new int[tokens.size()];

		int i = 0;
		for (IToken token : tokens)
			savedKinds[i++] = token.getKind();

		secondaryParser.setTokens(tokens);
		N result = secondaryParser.parse();

		IASTCompletionNode compNode = secondaryParser.getCompletionNode();
		if (compNode != null) {
			for (IASTName name : compNode.getNames())
				addNameToCompletionNode(name, compNode.getPrefix());
		}

		// restore the token kinds
		i = 0;
		for (IToken token : tokens)
			token.setKind(savedKinds[i++]);

		return result;
	}

	/*************************************************************************************************************
	 * Basic Actions
	 ************************************************************************************************************/

	/**
	 * Method that is called by the special <openscope> production
	 * in order to create a new scope in the AST stack.
	 */
	public void openASTScope() {
		astStack.openScope();
	}

	/**
	 * Place null on the stack.
	 * Usually called for optional element to indicate the element
	 * was not parsed.
	 */
	public void consumeEmpty() {
		astStack.push(null);
	}

	/**
	 * Place a marker on the stack.
	 * Usually used for very simple optional elements to indicate
	 * the element was parsed. Usually the existence of an AST node
	 * on the stack is used instead of the marker, but for simple
	 * cases like an optional keyword this action is useful.
	 */
	public void consumePlaceHolder() {
		astStack.push(PLACE_HOLDER);
	}

	/**
	 * Just pops the stack, useful if you have a rule that generates
	 * a node but you don't need the node.
	 */
	public void consumeIgnore() {
		astStack.pop();
	}

	/**
	 * Gets the current token and places it on the stack for later consumption.
	 */
	public void consumeToken() {
		astStack.push(stream.getRightIToken());
	}
}
