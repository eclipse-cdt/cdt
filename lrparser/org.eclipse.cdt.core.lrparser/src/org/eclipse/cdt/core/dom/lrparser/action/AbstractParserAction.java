package org.eclipse.cdt.core.dom.lrparser.action;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

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
	protected final IParserActionTokenProvider parser;
	
	/** Stack that holds the intermediate nodes as the AST is being built */
	protected final ScopedStack<Object> astStack;
	
	/** The completion node, only generated during a completion parse */
	protected ASTCompletionNode completionNode;
	
	/** The root node is created outside the parser because it is also needed by the preprocessor */
	protected final IASTTranslationUnit tu;
	
	/** Options that change the behavior of the parser actions */
	protected Set<IParser.Options> options = EnumSet.noneOf(IParser.Options.class);
	
	
	
	
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
	public AbstractParserAction(IParserActionTokenProvider parser, IASTTranslationUnit tu, ScopedStack<Object> astStack) {
		if(parser == null)
			throw new NullPointerException("parser is null"); //$NON-NLS-1$
		if(tu == null)
			throw new NullPointerException("tu is null"); //$NON-NLS-1$
		if(astStack == null)
			throw new NullPointerException("astStack is null"); //$NON-NLS-1$
		
		this.parser = parser;
		this.tu = tu;
		this.astStack = astStack;
	}
	
	

	protected void setOffsetAndLength(IASTNode node) {
		int ruleOffset = parser.getLeftIToken().getStartOffset();
		int ruleLength = parser.getRightIToken().getEndOffset() - ruleOffset;
		((ASTNode)node).setOffsetAndLength(ruleOffset, ruleLength < 0 ? 0 : ruleLength);
	}
	
	/**
	 * Creates a IASTName node from an identifier token.
	 * If the token is a completion token then it is added to the completion node.
	 */
	protected IASTName createName(IToken token) {
		IASTName name = createName(token.toString().toCharArray()); // TODO, token.toCharArray();
		ParserUtil.setOffsetAndLength(name, token); 
		
		if(isCompletionToken(token))
			addNameToCompletionNode(name, token.toString());
		
		return name;
	}
	
	public void setParserOptions(Set<IParser.Options> options) {
		this.options = options == null ? EnumSet.noneOf(IParser.Options.class) : options;
	}
	
	/**
	 * Creates a completion node if one does not yet exist and adds the 
	 * given name to it.
	 */
	protected void addNameToCompletionNode(IASTName name, String prefix) {
		if(completionNode == null) {
			prefix = (prefix == null || prefix.length() == 0) ? null : prefix;
			completionNode = newCompletionNode(prefix, tu);
		}
		
		completionNode.addName(name);
	}
	
	public ASTCompletionNode newCompletionNode(String prefix, IASTTranslationUnit tu) {
		return new ASTCompletionNode((prefix == null || prefix.length() == 0) ? null : prefix, tu);
	}
	
	/**
	 * Used to combine completion nodes from secondary parsers into
	 * the main completion node.
	 */
	protected void addNameToCompletionNode(IASTCompletionNode node) {
		if(node == null)
			return;
		
		for(IASTName name : node.getNames())
			addNameToCompletionNode(name, node.getPrefix());
	}
	
	
	/**
	 * Returns the completion node if this is a completion parse, null otherwise.
	 */
	public IASTCompletionNode getASTCompletionNode() {
		return completionNode;
	}
	
	
	/**
	 * Runs the given parser on the given token list.
	 * 
	 */
	protected IASTNode runSecondaryParser(IParser secondaryParser) {
		return runSecondaryParser(secondaryParser, parser.getRuleTokens());
	}
	
	
	/**
	 * Runs the given parser on the tokens that make up the current rule.
	 */
	protected IASTNode runSecondaryParser(IParser secondaryParser, List<IToken> tokens) { 
		// the secondary parser will alter the token kinds, which will need to be undone
		int[] savedKinds = new int[tokens.size()];
		
		int i = 0;
		for(IToken token : tokens)
			savedKinds[i++] = token.getKind();
		
		secondaryParser.setTokens(tokens);
		
		// need to pass tu because any new completion nodes need to be linked directly to the root
		IASTCompletionNode compNode = secondaryParser.parse(tu, options);
		addNameToCompletionNode(compNode);
		IASTNode result = secondaryParser.getSecondaryParseResult();
		
		// restore the token kinds
		i = 0;
		for(IToken token : tokens)
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
		astStack.push(parser.getRightIToken());
	}
}
