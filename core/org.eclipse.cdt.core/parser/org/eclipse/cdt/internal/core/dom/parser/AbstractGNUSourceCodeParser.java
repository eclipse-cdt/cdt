/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *    Mike Kucera (IBM) - bug #206952
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTUnaryExpression;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;

/**
 * @author jcamelon
 */
public abstract class AbstractGNUSourceCodeParser implements ISourceCodeParser {
	protected final AbstractParserLogService log;
    protected final IScanner scanner;
    protected final ParserMode mode;

    protected final boolean supportStatementsInExpressions;
    protected final boolean supportTypeOfUnaries;
    protected final boolean supportAlignOfUnaries;
    protected final boolean supportKnRC;
    protected final boolean supportAttributeSpecifiers;
    protected final boolean supportDeclspecSpecifiers;
    protected boolean supportParameterInfoBlock;
    protected final IBuiltinBindingsProvider builtinBindingsProvider;
    
    /**
     *  Marks the beginning of the current declaration. It is important to clear the mark whenever we
     *  enter a nested declaration, in order to avoid holding on to all the tokens.
     */
    protected IToken declarationMark;
    protected IToken currToken;
    protected int eofOffset;
	protected boolean parsePassed = true;
    protected int backtrackCount = 0;
    protected BacktrackException backtrack = new BacktrackException();
    protected ASTCompletionNode completionNode;
	
    protected AbstractGNUSourceCodeParser(IScanner scanner,
            IParserLogService logService, ParserMode parserMode,
            boolean supportStatementsInExpressions,
            boolean supportTypeOfUnaries, boolean supportAlignOfUnaries,
            boolean supportKnRC, boolean supportAttributeSpecifiers,
    		boolean supportDeclspecSpecifiers, 
    		IBuiltinBindingsProvider builtinBindingsProvider) {
        this.scanner = scanner;
        this.log = wrapLogService(logService);
        this.mode = parserMode;
        this.supportStatementsInExpressions = supportStatementsInExpressions;
        this.supportTypeOfUnaries = supportTypeOfUnaries;
        this.supportAlignOfUnaries = supportAlignOfUnaries;
        this.supportKnRC = supportKnRC;
        this.supportAttributeSpecifiers = supportAttributeSpecifiers;
        this.supportDeclspecSpecifiers = supportDeclspecSpecifiers;
        this.builtinBindingsProvider= builtinBindingsProvider;
    }

    public void setSupportParameterInfoBlock(boolean val) {
    	supportParameterInfoBlock= val;
    }
    
    private AbstractParserLogService wrapLogService(IParserLogService logService) {
		if (logService instanceof AbstractParserLogService) {
			return (AbstractParserLogService) logService;
		}
		return new ParserLogServiceWrapper(logService);
	}

    protected final void throwBacktrack(int offset, int length) throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(offset, (length < 0) ? 0 : length);
        throw backtrack;
    }

    public IASTCompletionNode getCompletionNode() {
        return completionNode;
    }

    // Use to create the completion node
    protected ASTCompletionNode createCompletionNode(IToken token) {
    	// the preprocessor may deliver tokens for literals or header-names.
        if(completionNode == null && token != null && token.getType() == IToken.tCOMPLETION) {
        	completionNode = new ASTCompletionNode(token, getTranslationUnit());
        }
        return completionNode;
    }

    /**
     * Look Ahead in the token list to see what is coming.
     * 
     * @param i
     *            How far ahead do you wish to peek?
     * @return the token you wish to observe
     * @throws EndOfFileException
     *             if looking ahead encounters EOF, throw EndOfFile
     */
    protected IToken LA(int i) throws EndOfFileException {

        if (isCancelled) {
            throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
        }

        if (i < 1) // can't go backwards
            return null;
        if (currToken == null)
            currToken = fetchToken();
        IToken retToken = currToken;
        for (; i > 1; --i) {
            retToken = retToken.getNext();
            if (retToken == null)
                retToken = fetchToken();
        }
        return retToken;
    }

    /**
     * Same as {@link #LA(int)}, but returns <code>null</code> when eof is reached.
     */
    protected IToken LAcatchEOF(int i) {
    	try {
    		return LA(i);
    	} catch (EndOfFileException e) {
    		return null;
    	}
    }
    
    /**
     * Look ahead in the token list and return the token type.
     * 
     * @param i
     *            How far ahead do you wish to peek?
     * @return The type of that token
     * @throws EndOfFileException
     *             if looking ahead encounters EOF, throw EndOfFile
     */
    protected int LT(int i) throws EndOfFileException {
        return LA(i).getType();
    }
    
    /**
     * Same as {@link #LT(int)}, but returns <code>0</code> when eof is reached.
     */
    protected int LTcatchEOF(int i) {
    	try {
    		return LT(i);
    	} catch (EndOfFileException e) {
    		return 0;
    	}
    }
    
	protected boolean isOnSameLine(int offset1, int offset2) {
		ILocationResolver lr= (ILocationResolver) getTranslationUnit().getAdapter(ILocationResolver.class);
		IASTFileLocation floc= lr.getMappedFileLocation(offset1, offset2-offset1+1);
		return floc.getFileName().equals(lr.getContainingFilePath(offset1)) &&
			floc.getStartingLineNumber() == floc.getEndingLineNumber();
	}

    protected int calculateEndOffset(IASTNode n) {
        ASTNode node = (ASTNode) n;
        return node.getOffset() + node.getLength();
    }
    
    protected void adjustLength(IASTNode n, IASTNode endNode) {
        final int endOffset= calculateEndOffset(endNode);
        final ASTNode node = (ASTNode) n;
        node.setLength(endOffset-node.getOffset());
    }

    /**
     * Consume the next token available, regardless of the type.
     * 
     * @return The token that was consumed and removed from our buffer.
     * @throws EndOfFileException
     *             If there is no token to consume.
     */
    protected IToken consume() throws EndOfFileException {
        if (currToken == null) {
            currToken = fetchToken();
        }
        
        final IToken lastToken = currToken;
        currToken= lastToken.getNext();
        return lastToken;
    }

    /**
     * Consume the next token available only if the type is as specified.
     * 
     * @param type
     *            The type of token that you are expecting.
     * @return the token that was consumed and removed from our buffer.
     * @throws BacktrackException
     *             If LT(1) != type
     */
    protected IToken consume(int type) throws EndOfFileException, BacktrackException {
    	final IToken la1= LA(1);
        if (la1.getType() != type)
            throwBacktrack(la1);
        	
        return consume();
    }

    /**
     * Consume the next token available only if the type is as specified. In case we
     * reached the end of completion, no token is consumed and the eoc-token returned.
     * 
     * @param type
     *            The type of token that you are expecting.
     * @return the token that was consumed and removed from our buffer.
     * @throws BacktrackException
     *             If LT(1) != type
     */
    protected IToken consumeOrEOC(int type) throws EndOfFileException, BacktrackException {
    	final IToken la1= LA(1);
        final int lt1 = la1.getType();
		if (lt1 != type) {
        	if (lt1 == IToken.tEOC)
        		return la1;
            throwBacktrack(la1);
        }        	
        return consume();
    }

    /**
     * Fetches a token from the scanner.
     * 
     * @return the next token from the scanner
     * @throws EndOfFileException
     *             thrown when the scanner.nextToken() yields no tokens
     */
    protected IToken fetchToken() throws EndOfFileException {
        try {
        	final IToken result= scanner.nextToken();
        	eofOffset= result.getEndOffset();
            return result;
        } catch (OffsetLimitReachedException olre) {
            handleOffsetLimitException(olre);
            // never returns, to make the java-compiler happy:
            return null;
        }
    }

    protected boolean isCancelled = false;

    protected static final int DEFAULT_DESIGNATOR_LIST_SIZE = 4;

    protected static int parseCount = 0;

    protected void handleOffsetLimitException(OffsetLimitReachedException exception) throws EndOfFileException {
        if (mode != ParserMode.COMPLETION_PARSE)
            throw new EndOfFileException();
        createCompletionNode(exception.getFinalToken());
        throw exception;
    }

    protected static final char[] EMPTY_STRING = "".toCharArray(); //$NON-NLS-1$

    /**
     * Mark our place in the buffer so that we could return to it should we have
     * to.
     * 
     * @return The current token.
     * @throws EndOfFileException
     *             If there are no more tokens.
     */
    protected IToken mark() throws EndOfFileException {
        return currToken == null ? currToken = fetchToken() : currToken;
    }

    /**
     * Rollback to a previous point, reseting the queue of tokens.
     * 
     * @param mark
     *            The point that we wish to restore to.
     */
    protected void backup(IToken mark) {
        currToken = mark;
    }

    /**
     * This is the single entry point for setting parsePassed to false
     */
    protected void failParse() {
        parsePassed = false;
    }

    public synchronized void cancel() {
        isCancelled = true;
    }

    /**
     * Parse an identifier.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IToken identifier() throws EndOfFileException, BacktrackException {
        switch (LT(1)) {
        case IToken.tIDENTIFIER:
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            return consume();
        default:
            throw backtrack;
        }

    }

    /**
     * @return Returns the backtrackCount.
     */
    public final int getBacktrackCount() {
        return backtrackCount;
    }

    protected IASTProblem createProblem(BacktrackException bt) {
        IASTProblem result= bt.getProblem();
        if (result == null) {
            result= createProblem(IProblem.SYNTAX_ERROR, bt.getOffset(), bt.getLength());
        }
        return result;
    }

    protected abstract IASTProblem createProblem(int signal, int offset, int length);

    protected void logThrowable(String methodName, Throwable e) {
        if (e != null) {
        	if (log.isTracing()) {
        		StringBuffer buffer = new StringBuffer();
        		buffer.append("Parser: Unexpected throwable in "); //$NON-NLS-1$
        		buffer.append(methodName);
        		buffer.append(":"); //$NON-NLS-1$
        		buffer.append(e.getClass().getName());
        		buffer.append("::"); //$NON-NLS-1$
        		buffer.append(e.getMessage());
        		buffer.append(". w/"); //$NON-NLS-1$
        		buffer.append(scanner.toString());
        		log.traceLog(buffer.toString());
        	}
        	log.traceException(e);
        }
    }

    @Override
	public String toString() {
        return scanner.toString(); 
    }

    /**
     * @param methodName
     * @param e
     */
    protected void logException(String methodName, Exception e) {
        if (!(e instanceof EndOfFileException) && e != null) {
        	if (log.isTracing()) {
        		StringBuffer buffer = new StringBuffer();
        		buffer.append("Parser: Unexpected exception in "); //$NON-NLS-1$
        		buffer.append(methodName);
        		buffer.append(":"); //$NON-NLS-1$
        		buffer.append(e.getClass().getName());
        		buffer.append("::"); //$NON-NLS-1$
        		buffer.append(e.getMessage());
        		buffer.append(". w/"); //$NON-NLS-1$
        		buffer.append(scanner.toString());
        		log.traceLog(buffer.toString());
        		// log.errorLog(buffer.toString());
        	}
        	log.traceException(e);
        }
    }

    protected final void throwBacktrack(IASTProblem problem, IASTNode node) throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(problem, node);
        throw backtrack;
    }
    
    protected final void throwBacktrack(IASTProblem problem) throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(problem);
        throw backtrack;
    }

    public IASTTranslationUnit parse() {
        long startTime = System.currentTimeMillis();
        translationUnit();
        log.traceLog("Parse " //$NON-NLS-1$
                + (++parseCount) + ": " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime) + "ms" //$NON-NLS-1$
                + (parsePassed ? "" : " - parse failure")); //$NON-NLS-1$ //$NON-NLS-2$
        startTime = System.currentTimeMillis();
        resolveAmbiguities();
        log.traceLog("Ambiguity resolution : " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime) + "ms" //$NON-NLS-1$
        ); 
        IASTTranslationUnit result = getTranslationUnit();
        nullifyTranslationUnit();
        return result;
    }

    protected void resolveAmbiguities() {
        final IASTTranslationUnit translationUnit = getTranslationUnit();
		translationUnit.accept(createAmbiguityNodeVisitor()); 
		if (translationUnit instanceof ASTTranslationUnit) {
			((ASTTranslationUnit)translationUnit).cleanupAfterAmbiguityResolution();
		}
    }

    protected abstract ASTVisitor createAmbiguityNodeVisitor();

    protected abstract void nullifyTranslationUnit();

    protected IToken skipOverCompoundStatement() throws BacktrackException,
            EndOfFileException {
        // speed up the parser by skipping the body, simply look for matching brace and return
        consume(IToken.tLBRACE);
        IToken result = null;
        int depth = 1;
        while (depth > 0) {
            result = consume();
            switch (result.getType()) {
            case IToken.tRBRACE:
                --depth;
                break;
            case IToken.tLBRACE:
                ++depth;
                break;
            case IToken.tEOC:
                throw new EndOfFileException();
            }
        }
        return result;
    }

    protected IASTProblemDeclaration skipProblemDeclaration(int offset) {
		failParse();
		declarationMark= null;
    	int endOffset = skipToSemiOrClosingBrace(offset);
		IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
		return createProblemDeclaration(problem);
    }
    
    protected IASTProblemStatement skipProblemStatement(int offset) {
		failParse();
		declarationMark= null;
    	int endOffset = skipToSemiOrClosingBrace(offset);
		IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
		return createProblemStatement(problem);
    }

	private int skipToSemiOrClosingBrace(int offset) {
		failParse();
		declarationMark= null;
    	int depth= 0;
    	int endOffset;
    	loop: try {
    		endOffset= LA(1).getOffset();
			while(true) {
				switch (LT(1)) {
				case IToken.tEOC:
					endOffset= eofOffset;
					break loop;
				case IToken.tSEMI:
					if (depth == 0) {
						endOffset= consume().getEndOffset();  
						break loop;
					}
					break;
				case IToken.tLBRACE:
					++depth;
					break;
				case IToken.tRBRACE:
					if (--depth <= 0) {
						if (depth == 0 || offset == endOffset) {
							endOffset= consume().getEndOffset(); // consume closing brace
						}
						break loop;
					}
					break;
				}
				endOffset= consume().getEndOffset();
			}
		} catch (EndOfFileException e) {
			endOffset= eofOffset;
		}
		return endOffset;
	}

    protected IASTProblemExpression skipProblemConditionInParenthesis(int offset) {
		failParse();
		int compExpr= 0;
		int depth= 0;
		int endOffset= offset;
		loop: try {
			while(true) {
				switch (LT(1)) {
				case IToken.tEOC:
					endOffset= eofOffset;
					break loop;
				case IToken.tSEMI:
				case IToken.tLBRACE:
					if (compExpr == 0) {
						break loop;
					}
					break;
				case IToken.tLPAREN:
					depth++;
					if (LTcatchEOF(2) == IToken.tLBRACE) {
						if (compExpr == 0) {
							compExpr= depth;
						}
						consume();
					} 
					break;
				case IToken.tRPAREN:
					if (--depth < 0) {
						break loop;
					}
					if (depth < compExpr) {
						compExpr= 0;
					}
					break;
				}
				endOffset= consume().getEndOffset();
			}
		} catch (EndOfFileException e) {
			endOffset= eofOffset;
		}
		IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
		return createProblemExpression(problem);
    }

    /**
     * @return TODO
     * @throws BacktrackException
     */
    protected IASTCompoundStatement compoundStatement() throws EndOfFileException, BacktrackException {
        IASTCompoundStatement result = createCompoundStatement();
        if (LT(1) == IToken.tEOC)
            return result;

        final int offset= LA(1).getOffset();
        int endOffset= consume(IToken.tLBRACE).getOffset();

        int stmtOffset= -1;
        while(true) {
        	IToken next= LAcatchEOF(1);
        	if (next == null) {
        		((ASTNode) result).setOffsetAndLength(offset, endOffset-offset);
        		throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, endOffset, 0), result);
        		return null; // hint for java-compiler
        	}
            try {
            	if (next.getType() == IToken.tEOC)
            		break;
            	
            	if (next.getType() == IToken.tRBRACE) {
            		endOffset= consume().getEndOffset();
            		break;
            	}
            		    
            	final int nextOffset = next.getOffset();
        		declarationMark= next;
        		next= null; // don't hold on to the token while parsing namespaces, class bodies, etc.

        		IASTStatement stmt;
        		if (stmtOffset == nextOffset) {
        			// no progress
        			stmt= skipProblemStatement(stmtOffset);
            	} else {
            		stmtOffset= nextOffset;
            		stmt= statement();
            	}
        		result.addStatement(stmt);
        		endOffset= calculateEndOffset(stmt);
            } catch (BacktrackException bt) {
            	IASTStatement stmt= skipProblemStatement(stmtOffset);
        		result.addStatement(stmt);
        		endOffset= calculateEndOffset(stmt);
            } catch (EndOfFileException e) {
            	IASTStatement stmt= skipProblemStatement(stmtOffset);
        		result.addStatement(stmt);
        		endOffset= calculateEndOffset(stmt);
        		break;
            } finally {
            	declarationMark= null;
            }
        }
        ((ASTNode) result).setOffsetAndLength(offset, endOffset-offset);
        return result;
    }

    protected abstract IASTProblemStatement createProblemStatement();
	protected abstract IASTProblemDeclaration createProblemDeclaration();
    protected abstract IASTCompoundStatement createCompoundStatement();

	private IASTProblemDeclaration createProblemDeclaration(IASTProblem problem) {
		IASTProblemDeclaration pd = createProblemDeclaration();
		pd.setProblem(problem);
		((ASTNode) pd).setOffsetAndLength(((ASTNode) problem));
		return pd;
	}

	private IASTProblemStatement createProblemStatement(IASTProblem problem) {
		IASTProblemStatement pstmt = createProblemStatement();
		pstmt.setProblem(problem);
		((ASTNode) pstmt).setOffsetAndLength(((ASTNode) problem));
		return pstmt;
	}

	private IASTProblemExpression createProblemExpression(IASTProblem problem) {
		IASTProblemExpression pexpr = createProblemExpression();
		pexpr.setProblem(problem);
		((ASTNode) pexpr).setOffsetAndLength(((ASTNode) problem));
		return pexpr;
	}

    protected IASTExpression compoundStatementExpression() throws EndOfFileException, BacktrackException {
        int startingOffset = consume().getOffset(); // tLPAREN always
        IASTCompoundStatement compoundStatement = null;
        if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE)
            skipOverCompoundStatement();
        else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                compoundStatement();
            else
                skipOverCompoundStatement();
        } else if (mode == ParserMode.COMPLETE_PARSE)
            compoundStatement = compoundStatement();

        int lastOffset = consume(IToken.tRPAREN).getEndOffset();
        IGNUASTCompoundStatementExpression resultExpression = createCompoundStatementExpression();
        ((ASTNode) resultExpression).setOffsetAndLength(startingOffset, lastOffset - startingOffset);
        if (compoundStatement != null) {
            resultExpression.setCompoundStatement(compoundStatement);
        }

        return resultExpression;
    }

    protected abstract IGNUASTCompoundStatementExpression createCompoundStatementExpression();

    protected IASTExpression possiblyEmptyExpressionList(int endToken) throws BacktrackException, EndOfFileException {
    	IToken la1= LA(1);
    	if (la1.getType() == endToken) {
            IASTExpressionList expressionList = createExpressionList();
            ((ASTNode) expressionList).setOffsetAndLength(la1.getOffset(), 0);
            return expressionList;
    	}
    	return expression();
    }
    
    protected IASTExpression expression() throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        IASTExpression assignmentExpression = assignmentExpression();
        if (LT(1) != IToken.tCOMMA)
            return assignmentExpression;

        IASTExpressionList expressionList = createExpressionList();
        ((ASTNode) expressionList).setOffset(startingOffset);
        expressionList.addExpression(assignmentExpression);

        int lastOffset = 0;
        while (LT(1) == IToken.tCOMMA) {
            consume();
            IASTExpression secondExpression = assignmentExpression();
            expressionList.addExpression(secondExpression);
            lastOffset = calculateEndOffset(secondExpression);
        }
        ((ASTNode) expressionList).setLength(lastOffset - startingOffset);
        return expressionList;
    }

    protected abstract IASTExpressionList createExpressionList();

    protected abstract IASTExpression assignmentExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression relationalExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression multiplicativeExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTTypeId typeId(DeclarationOptions option) throws EndOfFileException;

    protected abstract IASTExpression castExpression() throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression unaryExpression() throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression buildTypeIdExpression(int op,
            IASTTypeId typeId, int startingOffset, int endingOffset);

    protected abstract IASTTranslationUnit getTranslationUnit();

	protected abstract void setupTranslationUnit() throws Exception;

	protected void translationUnit() {
        try {
            setupTranslationUnit();
        } catch (Exception e2) {
            logException("translationUnit::createCompilationUnit()", e2); //$NON-NLS-1$
            return;
        }
        parseTranslationUnit();
    }

	protected void parseTranslationUnit() {
		final IASTTranslationUnit tu= getTranslationUnit();
		int offset= -1;
        while (true) {
            try {
            	IToken next= LAcatchEOF(1);
            	if (next == null || next.getType() == IToken.tEOC)
            		break;
            	          
            	final int nextOffset = next.getOffset();
        		declarationMark= next;
        		next= null; // don't hold on to the token while parsing namespaces, class bodies, etc.

        		if (offset == nextOffset) {
        			// no progress
            		tu.addDeclaration(skipProblemDeclaration(offset));
            	} else {
            		offset= nextOffset;
            		final IASTDeclaration declaration= declaration(DeclarationOptions.GLOBAL);
            		tu.addDeclaration(declaration);
            	}
            } catch (BacktrackException bt) {
            	IASTDeclaration[] decls= problemDeclaration(offset, bt, DeclarationOptions.GLOBAL);
            	for (IASTDeclaration declaration : decls) {
            		tu.addDeclaration(declaration);
            	}
            } catch (EndOfFileException e) {
            	tu.addDeclaration(skipProblemDeclaration(offset));
            	break;
            } catch (OutOfMemoryError oome) {
                logThrowable("translationUnit", oome); //$NON-NLS-1$
                throw oome;
            } catch (Exception e) {
                logException("translationUnit", e); //$NON-NLS-1$
                tu.addDeclaration(skipProblemDeclaration(offset));
            } finally {
            	declarationMark= null;
            }
        }
        ((ASTNode) tu).setLength(eofOffset);
	}

    protected IASTExpression assignmentOperatorExpression(int kind,
            IASTExpression lhs) throws EndOfFileException, BacktrackException {
        consume();
        IASTExpression rhs = assignmentExpression();
        return buildBinaryExpression(kind, lhs, rhs, calculateEndOffset(rhs));
    }

    protected IASTExpression constantExpression() throws BacktrackException,
            EndOfFileException {
        return conditionalExpression();
    }

    protected IASTExpression logicalOrExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = logicalAndExpression();
        while (LT(1) == IToken.tOR) {
            consume();
            IASTExpression secondExpression = logicalAndExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_logicalOr, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    protected IASTExpression logicalAndExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = inclusiveOrExpression();
        while (LT(1) == IToken.tAND) {
            consume();
            IASTExpression secondExpression = inclusiveOrExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_logicalAnd, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    protected IASTExpression inclusiveOrExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = exclusiveOrExpression();
        while (LT(1) == IToken.tBITOR) {
            consume();
            IASTExpression secondExpression = exclusiveOrExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryOr, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    protected IASTExpression exclusiveOrExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = andExpression();
        while (LT(1) == IToken.tXOR) {
            consume();
            IASTExpression secondExpression = andExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryXor, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    protected IASTExpression andExpression() throws EndOfFileException, BacktrackException {
        IASTExpression firstExpression = equalityExpression();
        while (LT(1) == IToken.tAMPER) {
            consume();
            IASTExpression secondExpression = equalityExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryAnd, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    protected IASTExpression equalityExpression() throws EndOfFileException,
            BacktrackException {
        IASTExpression firstExpression = relationalExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tEQUAL:
            case IToken.tNOTEQUAL:
                IToken t = consume();
                int operator = ((t.getType() == IToken.tEQUAL) ? IASTBinaryExpression.op_equals
                        : IASTBinaryExpression.op_notequals);
                IASTExpression secondExpression = relationalExpression();
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    protected IASTExpression buildBinaryExpression(int operator,
            IASTExpression firstExpression, IASTExpression secondExpression,
            int lastOffset) {
        IASTBinaryExpression result = createBinaryExpression();
        result.setOperator(operator);
        int o = ((ASTNode) firstExpression).getOffset();
        ((ASTNode) result).setOffsetAndLength(o, lastOffset - o);
        result.setOperand1(firstExpression);
        result.setOperand2(secondExpression);
        return result;
    }

    protected abstract IASTBinaryExpression createBinaryExpression();

    protected IASTExpression shiftExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = additiveExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tSHIFTL:
            case IToken.tSHIFTR:
                IToken t = consume();
                int operator = t.getType() == IToken.tSHIFTL ? IASTBinaryExpression.op_shiftLeft
                        : IASTBinaryExpression.op_shiftRight;
                IASTExpression secondExpression = additiveExpression();
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    protected IASTExpression additiveExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = multiplicativeExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tPLUS:
            case IToken.tMINUS:
                IToken t = consume();
                int operator = (t.getType() == IToken.tPLUS) ? IASTBinaryExpression.op_plus
                        : IASTBinaryExpression.op_minus;
                IASTExpression secondExpression = multiplicativeExpression();
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    protected IASTExpression conditionalExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = logicalOrExpression();
        if (LT(1) == IToken.tQUESTION) {
            consume();
            IASTExpression secondExpression= null;
            if (LT(1) != IToken.tCOLON) {
            	secondExpression = expression();
            }
            IASTExpression thirdExpression = null;
            
            if (LT(1) != IToken.tEOC) {
                consume(IToken.tCOLON);
                thirdExpression = assignmentExpression();
            }
            
            IASTConditionalExpression result = createConditionalExpression();
            result.setLogicalConditionExpression(firstExpression);
            result.setPositiveResultExpression(secondExpression);
            if (thirdExpression != null) {
                result.setNegativeResultExpression(thirdExpression);
                ((ASTNode) result).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), calculateEndOffset(thirdExpression)
                        - ((ASTNode) firstExpression).getOffset());
            }
            
            return result;
        }
        return firstExpression;
    }

    protected abstract IASTConditionalExpression createConditionalExpression();

    protected IASTExpression buildUnaryExpression(int operator,
            IASTExpression operand, int offset, int lastOffset) {
        IASTUnaryExpression result = createUnaryExpression();
        ((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
        result.setOperator(operator);
        result.setOperand(operand);
        return result;
    }

    protected abstract IASTUnaryExpression createUnaryExpression();

    protected IASTExpression unaryAlignofExpression()
            throws EndOfFileException, BacktrackException {
        int offset = consume().getOffset(); // t___alignof__
        IASTTypeId d = null;
        IASTExpression unaryExpression = null;

        IToken m = mark();
        int lastOffset = 0;
        if (LT(1) == IToken.tLPAREN) {
        	boolean needBack = false;
            try {
                consume();
                d = typeId(DeclarationOptions.TYPEID);
                if (d == null)
                	needBack = true;
                else
                	lastOffset = consume(IToken.tRPAREN).getEndOffset();
            } catch (BacktrackException bt) {
            	needBack = true;
            }
            if (needBack) {
            	backup(m);
            	d = null;
            	unaryExpression = unaryExpression();
            	lastOffset = calculateEndOffset(unaryExpression);
            }
            
        } else {
            unaryExpression = unaryExpression();
            lastOffset = calculateEndOffset(unaryExpression);
        }
        if (d != null & unaryExpression == null)
            return buildTypeIdExpression(IGNUASTTypeIdExpression.op_alignof, d,
                    offset, lastOffset);
        else if (unaryExpression != null && d == null)
            return buildUnaryExpression(IGNUASTUnaryExpression.op_alignOf,
                    unaryExpression, offset, lastOffset);
        return null;
    }

    protected IASTExpression unaryTypeofExpression() throws EndOfFileException,
            BacktrackException {
        int offset = consume().getOffset(); // t_typeof
        IASTTypeId d = null;
        IASTExpression expression = null;

        int lastOffset = 0;
        // prefer expressions over type-ids
        if (LT(1) == IToken.tLPAREN && LT(2) != IToken.tLBRACE) {
        	consume();
            final IToken m = mark();
        	try {
        		expression= expression();
        	}
        	catch (BacktrackException e) {
        		backup(m);
            	d = typeId(DeclarationOptions.TYPEID);
            	if (d == null) 
            		throw e;
        	}
        	lastOffset = consume(IToken.tRPAREN).getEndOffset();
        } else {
            expression = unaryExpression();
            lastOffset = calculateEndOffset(expression);
        }
        if (d != null)
            return buildTypeIdExpression(IGNUASTTypeIdExpression.op_typeof, d, offset, lastOffset);
        
        return buildUnaryExpression(IGNUASTUnaryExpression.op_typeof, expression, offset, lastOffset);
    }

    protected IASTStatement handleFunctionBody() throws BacktrackException, EndOfFileException {
    	declarationMark= null; 
        if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE) {
            IToken curr = LA(1);
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = createCompoundStatement();
            ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last.getEndOffset() - curr.getOffset());
            return cs;
        } else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                return functionBody();
            IToken curr = LA(1);
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = createCompoundStatement();
            ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last.getEndOffset() - curr.getOffset());
            return cs;
        } 

        // full parse
        return functionBody();
    }

    /**
     * Parses a function body.
     * 
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTStatement functionBody() throws EndOfFileException, BacktrackException {
        return compoundStatement();
    }

    protected abstract IASTDeclarator initDeclarator(DeclarationOptions option) throws EndOfFileException, BacktrackException;

    /**
     * @param option the options with which to parse the declaration
     * @throws FoundDeclaratorException encountered EOF while looking ahead
     */
    protected void lookAheadForDeclarator(final DeclarationOptions option) throws FoundDeclaratorException {
        IToken mark = null;
        try {
            mark = mark();
            final IASTDeclarator dtor= initDeclarator(option);
            final IToken la = LA(1);
            if (la == null || la == mark)
            	return;

            if (verifyLookaheadDeclarator(option, dtor, la))
            	throw new FoundDeclaratorException(dtor, la);
        } catch (BacktrackException bte) {
        } catch (EndOfFileException e) {
        } finally {
        	if (mark != null)
        		backup(mark);
        }
    }

	protected abstract boolean verifyLookaheadDeclarator(DeclarationOptions option, IASTDeclarator d, IToken nextToken);

    public static class FoundDeclaratorException extends Exception {
    	private static final long serialVersionUID = 0;
    	
        public IASTDeclSpecifier declSpec;
        public IASTDeclarator declarator;

		public IASTDeclSpecifier altSpec;
		public IASTDeclarator altDeclarator;
        
        public IToken currToken;

        public FoundDeclaratorException(IASTDeclarator d, IToken t) {
            this.declarator = d;
            this.currToken =t;
        }
    }
    
    /**
     * Parse an enumeration specifier, as according to the ANSI specs in C &
     * C++. enumSpecifier: "enum" (name)? "{" (enumerator-list) "}"
     * enumerator-list: enumerator-definition enumerator-list ,
     * enumerator-definition enumerator-definition: enumerator enumerator =
     * constant-expression enumerator: identifier
     * 
     * @throws BacktrackException request a backtrack
     */
    protected IASTEnumerationSpecifier enumSpecifier() throws BacktrackException, EndOfFileException {
        IToken mark = mark();
        IASTName name = null;
        int startOffset = consume().getOffset(); // t_enum
        if (LT(1) == IToken.tIDENTIFIER) {
            name = createName(identifier());
        } else
            name = createName();
        if (LT(1) == IToken.tLBRACE) {

            IASTEnumerationSpecifier result = createEnumerationSpecifier();
            ((ASTNode) result).setOffset(startOffset);
            result.setName(name);

            consume(); // IToken.tLBRACE
            enumLoop: while (true) {
                
                switch (LT(1)) {
                case IToken.tRBRACE:
                case IToken.tEOC:
                    break enumLoop;
                }

                IASTName enumeratorName = null;

                int lastOffset = 0;
                if (LT(1) == IToken.tIDENTIFIER) {
                    enumeratorName = createName(identifier());
                    lastOffset = calculateEndOffset(enumeratorName);
                } else {
                    IToken la = LA(1);
                    throwBacktrack(la.getOffset(), la.getLength());
                    return null; // line is never reached, hint for the parser
                }
                IASTExpression initialValue = null;
                if (LT(1) == IToken.tASSIGN) {
                    consume();
                    initialValue = constantExpression();
                    lastOffset = calculateEndOffset(initialValue);
                }
                IASTEnumerationSpecifier.IASTEnumerator enumerator = null;
                if (LT(1) == IToken.tRBRACE) {
                    enumerator = createEnumerator();
                    enumerator.setName(enumeratorName);
                    ((ASTNode) enumerator).setOffsetAndLength(
                            ((ASTNode) enumeratorName).getOffset(), lastOffset
                                    - ((ASTNode) enumeratorName).getOffset());
                    if (initialValue != null) {
                        enumerator.setValue(initialValue);
                    }
                    result.addEnumerator(enumerator);

                    break;
                }
                
                switch (LT(1)) {
                case IToken.tCOMMA:
                case IToken.tEOC:
                    consume();
                    break;
                default:
                    throwBacktrack(mark.getOffset(), mark.getLength());
                }
                
                enumerator = createEnumerator();
                enumerator.setName(enumeratorName);
                ((ASTNode) enumerator).setOffsetAndLength(
                        ((ASTNode) enumeratorName).getOffset(), lastOffset
                                - ((ASTNode) enumeratorName).getOffset());
                if (initialValue != null) {
                    enumerator.setValue(initialValue);
                }
                result.addEnumerator(enumerator);
            }
            
            int lastOffset = consume().getEndOffset();
            ((ASTNode) result).setLength(lastOffset - startOffset);
            return result;
        }
        // enumSpecifierAbort
        backup(mark);
        throwBacktrack(mark.getOffset(), mark.getLength());
        return null;
    }

    protected abstract IASTStatement statement() throws EndOfFileException, BacktrackException;

    protected abstract IASTEnumerator createEnumerator();

    protected abstract IASTEnumerationSpecifier createEnumerationSpecifier();

    protected abstract IASTName createName();

    protected abstract IASTName createName(IToken token);

    protected IASTExpression condition(boolean followedByParenthesis) throws BacktrackException, EndOfFileException {
    	IToken mark= mark();
    	try {
    		IASTExpression expr= expression();
    		if (!followedByParenthesis)
    			return expr;
    		
    		switch (LT(1)) {
    		case IToken.tEOC:
    		case IToken.tRPAREN:
    			return expr;
    		}
    	} catch (BacktrackException b) {
    		if (!followedByParenthesis)
    			throw b;
    	}
		backup(mark);
		return skipProblemConditionInParenthesis(mark.getOffset());
    }

    public boolean encounteredError() {
        return !parsePassed;
    }

    protected abstract IASTSimpleDeclaration createSimpleDeclaration();

    protected abstract IASTNamedTypeSpecifier createNamedTypeSpecifier();


    protected abstract IASTDeclarationStatement createDeclarationStatement();


    protected abstract IASTExpressionStatement createExpressionStatement();


    protected abstract IASTLabelStatement createLabelStatement();


    protected abstract IASTNullStatement createNullStatement();


    protected abstract IASTGotoStatement createGoToStatement();


    protected abstract IASTReturnStatement createReturnStatement();


    protected abstract IASTContinueStatement createContinueStatement();


    protected abstract IASTBreakStatement createBreakStatement();
    

    protected abstract IASTDoStatement createDoStatement();


    protected abstract IASTWhileStatement createWhileStatement();


    protected abstract IASTIdExpression createIdExpression();


    protected abstract IASTDefaultStatement createDefaultStatement();


    protected abstract IASTCaseStatement createCaseStatement();

    protected abstract IASTDeclaration declaration(DeclarationOptions option) throws BacktrackException, EndOfFileException;

    protected IASTDeclaration[] problemDeclaration(int offset, BacktrackException bt, DeclarationOptions option) {
    	failParse();
    	IASTProblem origProblem= createProblem(bt);
    	
    	// a node was detected by assuming additional tokens (e.g. missing semicolon)
    	IASTNode n= bt.getNodeBeforeProblem();
    	if (n instanceof IASTDeclaration) {
    		declarationMark= null;
    		return new IASTDeclaration[] {(IASTDeclaration) n, createProblemDeclaration(origProblem)};
    	} 
    	
    	if (declarationMark != null) {
    		IASTDeclaration trailingProblem= null;
    		offset= declarationMark.getOffset();
    		
    		// try to skip identifiers (undefined macros?)
    		IASTDeclaration decl= null;
    		int endOffset= 0;
    		loop: while (declarationMark != null && declarationMark.getType() == IToken.tIDENTIFIER) {
    			endOffset= declarationMark.getEndOffset();
    			declarationMark= declarationMark.getNext();
    			if (declarationMark != null) {
    				backup(declarationMark);
    				// avoid creating an empty declaration
    				switch(LTcatchEOF(1)) {
    				case 0: // eof
    				case IToken.tEOC:
    				case IToken.tSEMI:
    				 	break loop;
    				}
    				try {
    					decl= declaration(option);
    					break;
    				} catch (BacktrackException bt2) {
    			    	n= bt.getNodeBeforeProblem();
    			    	if (n instanceof IASTDeclaration) {
    			    		decl= (IASTDeclaration) n;
    			    		trailingProblem= createProblemDeclaration(bt.getProblem());
    			    		break;
    			    	} 
    				} catch (EndOfFileException e) {
    					endOffset= eofOffset;
    					break;
    				}
    			}
    		}
    		declarationMark= null;
    		
    		if (decl != null) {
    			IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
    			IASTDeclaration pd= createProblemDeclaration(problem);
    			if (trailingProblem != null)
    				return new IASTDeclaration[] {pd, decl, trailingProblem};
    			return new IASTDeclaration[] {pd, decl};
    		}
    	}

    	return new IASTDeclaration[] {skipProblemDeclaration(offset)};
    }

	protected IASTDeclaration asmDeclaration() throws EndOfFileException,
            BacktrackException {
        IToken first = consume(); // t_asm
        IToken next= LA(1);
        if (next.getType() == IToken.t_volatile) {
        	consume();
        }
        
        StringBuilder buffer= new StringBuilder();
        asmExpression(buffer);
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        return buildASMDirective(first.getOffset(), buffer.toString(), lastOffset);
    }

	protected IToken asmExpression(StringBuilder content) throws EndOfFileException, BacktrackException {
		IToken t= consume(IToken.tLPAREN);
    	boolean needspace= false;
        int open= 1;
        while (open > 0) {
        	t= consume();
			switch(t.getType()) {
			case IToken.tLPAREN:
				open++;
				break;
        	case IToken.tRPAREN:
        		open--;
        		break;
        	case IToken.tEOC:
        		throw new EndOfFileException();
        	
        	default:
        		if (content != null) {
        			if (needspace) {
        				content.append(' ');
        			}
        			content.append(t.getCharImage());
        			needspace= true;
        		}
        		break;
			}
        }
		return t;
	}

    protected IASTASMDeclaration buildASMDirective(int offset, String assembly,
            int lastOffset) {
        IASTASMDeclaration result = createASMDirective();
        ((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
        result.setAssembly(assembly);
        return result;
    }


    protected abstract IASTASMDeclaration createASMDirective();


    protected IASTExpression buildTypeIdUnaryExpression(int op,
            IASTTypeId typeId, IASTExpression subExpression,
            int startingOffset, int lastOffset) {
        IASTCastExpression result = createCastExpression();
        result.setOperator(op);
        ((ASTNode) result).setOffsetAndLength(startingOffset, lastOffset - startingOffset);
        result.setTypeId(typeId);
        if (subExpression != null) { // which it can be in a completion
            result.setOperand(subExpression);
        }
        
        return result;
    }


    protected abstract IASTCastExpression createCastExpression();

    
    /**
     * There are many ambiguities in C and C++ between expressions and declarations.
     * This method will attempt to parse a statement as both an expression and a declaration,
     * if both parses succeed then an ambiguity node is returned.
     */
    protected IASTStatement parseDeclarationOrExpressionStatement(DeclarationOptions option) throws EndOfFileException, BacktrackException {
        // First attempt to parse an expressionStatement
        // Note: the function style cast ambiguity is handled in expression
        // Since it only happens when we are in a statement
        IToken mark = mark();
        IASTExpressionStatement expressionStatement = null;
        IToken lastTokenOfExpression = null;
        try {
            IASTExpression expression = expression();
            if (LT(1) == IToken.tEOC)
                lastTokenOfExpression = consume();
            else
                lastTokenOfExpression = consume(IToken.tSEMI);
            expressionStatement = createExpressionStatement();
            expressionStatement.setExpression(expression);
            ((ASTNode) expressionStatement).setOffsetAndLength(mark.getOffset(), lastTokenOfExpression.getEndOffset() - mark.getOffset());
        } catch (BacktrackException b) {
        }

        backup(mark);

        // Now attempt to parse a declarationStatement
        IASTDeclarationStatement ds = null;
        try {
            IASTDeclaration d = declaration(option);
            ds = createDeclarationStatement();
            ds.setDeclaration(d);
            ((ASTNode) ds).setOffsetAndLength(((ASTNode) d).getOffset(), ((ASTNode) d).getLength());
        } catch (BacktrackException b) {
            backup(mark);
            if (expressionStatement == null) {
            	throw b;
            }
        }

        if (expressionStatement == null) {
        	return ds;
        }
        if (ds == null) {
        	backup(lastTokenOfExpression); consume();
            return expressionStatement;
        }
        
        // At this point we know we have an ambiguity.
        // Attempt to resolve some ambiguities that are easy to detect.
        
        // A * B = C;
        // foo() = x;
        // These can get parsed as expressions but the lvalue doesn't make sense
        if (expressionStatement.getExpression() instanceof IASTBinaryExpression) {
            IASTBinaryExpression exp = (IASTBinaryExpression) expressionStatement.getExpression();
            if (exp.getOperator() == IASTBinaryExpression.op_assign) {
                IASTExpression lhs = exp.getOperand1();
                if (lhs instanceof IASTBinaryExpression
                        && ((IASTBinaryExpression) lhs).getOperator() == IASTBinaryExpression.op_multiply) {
                    return ds;
                }
                if (lhs instanceof IASTFunctionCallExpression) {
                    return ds;
                }
            }
        }

        final IASTDeclaration declaration = ds.getDeclaration();
		if (declaration instanceof IASTSimpleDeclaration) {
	        final IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
	        IASTDeclSpecifier declspec= simpleDecl.getDeclSpecifier();
	        if (declspec instanceof IASTNamedTypeSpecifier) {
	        	final IASTDeclarator[] declarators = simpleDecl.getDeclarators();

	            // x; 
	            // can be parsed as a named declaration specifier without a declarator
	        	if (declarators.length == 0) {
					backup(lastTokenOfExpression); consume();
					return expressionStatement;
	        	}

	        	// a function call interpreted as declaration: 'func(x);' --> 'func x;'
	        	if (declarators.length == 1) {
	        		IASTName name= ((IASTNamedTypeSpecifier) declspec).getName();
        			final IASTDeclarator dtor= declarators[0];
	        		if (name.contains(declspec)) {
	        			if (dtor.getNestedDeclarator() != null) {
	        				if (dtor instanceof IASTAmbiguousDeclarator == false
	        					&& dtor instanceof IASTArrayDeclarator == false 
	        					&& dtor instanceof IASTFieldDeclarator == false
	        					&& dtor instanceof IASTFunctionDeclarator == false) {
	        					backup(lastTokenOfExpression); consume();
	        					return expressionStatement;
	        				}
	        			}
	        		}
	        		
	        		if (dtor.getName().toCharArray().length == 0 && dtor.getNestedDeclarator() == null) {
	        			throw new Error();
//	        			backup(lastTokenOfExpression); consume();
//	        			return expressionStatement;
	        		}
	        	}
	        }
		}

        // create and return ambiguity node
        IASTAmbiguousStatement statement = createAmbiguousStatement();
        statement.addStatement(expressionStatement);
        statement.addStatement(ds);
        ((ASTNode) statement).setOffsetAndLength((ASTNode) ds);
        return statement;
    }
    
    
    /**
     * Returns true if the given declaration has unspecified type,
     * in this case the type defaults to int and is know as "implicit int".
     */
    protected static boolean isImplicitInt(IASTDeclaration declaration) {
    	if(declaration instanceof IASTSimpleDeclaration) {
    		IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)declaration).getDeclSpecifier();
    		if(declSpec instanceof IASTSimpleDeclSpecifier && 
    		   ((IASTSimpleDeclSpecifier)declSpec).getType() == IASTSimpleDeclSpecifier.t_unspecified) {
    			return true;
    		}
    	}
    	return false;
    }

    
    protected abstract IASTAmbiguousStatement createAmbiguousStatement();

    protected IASTStatement parseLabelStatement() throws EndOfFileException,
            BacktrackException {
        IToken labelName = consume(); // tIDENTIFIER
        consume(); // tCOLON
        IASTStatement nestedStatement = statement();
        int lastOffset = calculateEndOffset( nestedStatement );
        IASTLabelStatement label_statement = createLabelStatement();
        ((ASTNode) label_statement).setOffsetAndLength(labelName.getOffset(),
                lastOffset - labelName.getOffset());
        
        IASTName name = createName(labelName);
        label_statement.setName(name);
        label_statement.setNestedStatement(nestedStatement);
        return label_statement;
    }

    protected IASTStatement parseNullStatement() throws EndOfFileException,
            BacktrackException {
        IToken t = consume(); // tSEMI

        IASTNullStatement null_statement = createNullStatement();
        ((ASTNode) null_statement).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
        return null_statement;
    }

    protected IASTStatement parseGotoStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume().getOffset(); // t_goto
        IToken identifier = consume(IToken.tIDENTIFIER);
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTName goto_label_name = createName(identifier);
        IASTGotoStatement goto_statement = createGoToStatement();
        ((ASTNode) goto_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        goto_statement.setName(goto_label_name);
        return goto_statement;
    }

    protected IASTStatement parseBreakStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume().getOffset(); // t_break
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTBreakStatement break_statement = createBreakStatement();
        ((ASTNode) break_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return break_statement;
    }

    protected IASTStatement parseSwitchBody() throws EndOfFileException, BacktrackException {
		IASTStatement stmt= null;
        if (LT(1) != IToken.tEOC)
        	stmt= statement();
    
        if (stmt instanceof IASTCaseStatement == false) 
        	return stmt;
        
        // bug 105334, switch without compound statement
        IASTCompoundStatement comp= createCompoundStatement();
        ((ASTNode) comp).setOffsetAndLength((ASTNode) stmt);
        comp.addStatement(stmt);

        while (LT(1) != IToken.tEOC && stmt instanceof IASTCaseStatement) {
        	stmt= statement();
        	comp.addStatement(stmt);
        }
        adjustLength(comp, stmt);
		return comp;
	}

    protected IASTStatement parseContinueStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume().getOffset(); // t_continue
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTContinueStatement continue_statement = createContinueStatement();
        ((ASTNode) continue_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return continue_statement;
    }

    protected IASTStatement parseReturnStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset;
        startOffset = consume().getOffset(); // t_return
        IASTExpression result = null;

        // See if there is a return expression
        switch (LT(1)) {
        case IToken.tEOC:
            // We're trying to start one
            IASTName name = createName(LA(1));
            IASTIdExpression idExpr = createIdExpression();
            idExpr.setName(name);
            result = idExpr;
            break;
        case IToken.tSEMI:
            // None
            break;
        default:
            // Yes
            result = expression();
            break;
        }

        int lastOffset = 0;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throwBacktrack(LA(1));
        }

        IASTReturnStatement return_statement = createReturnStatement();
        ((ASTNode) return_statement).setOffsetAndLength(startOffset, lastOffset
                - startOffset);
        if (result != null) {
            return_statement.setReturnValue(result);
        }
        return return_statement;
    }

    protected IASTStatement parseDoStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset(); // t_do
        IASTStatement do_body = statement();

        IASTExpression do_condition = null;
        if (LT(1) != IToken.tEOC) {
            consume(IToken.t_while);
            consume(IToken.tLPAREN);
            do_condition = condition(true);
        }
        
        int lastOffset;
        switch (LT(1)) {
        case IToken.tRPAREN:
        case IToken.tEOC:
            consume();
            break;
        default:
            throw backtrack;
        }
        
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throw backtrack;
        }
        

        IASTDoStatement do_statement = createDoStatement();
        ((ASTNode) do_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        do_statement.setBody(do_body);
        
        if (do_condition != null) {
            do_statement.setCondition(do_condition);
        }
        
        return do_statement;
    }

    protected IASTStatement parseWhileStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTExpression while_condition = condition(true);
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throwBacktrack(LA(1));
        }
        IASTStatement while_body = null;
        if (LT(1) != IToken.tEOC)
            while_body = statement();

        IASTWhileStatement while_statement = createWhileStatement();
        ((ASTNode) while_statement).setOffsetAndLength(startOffset,
                (while_body != null ? calculateEndOffset(while_body) : LA(1).getEndOffset()) - startOffset);
        while_statement.setCondition(while_condition);
        while_statement.setBody(while_body);
        
        return while_statement;
    }

    /**
     * @param result
     */
    protected void reconcileLengths(IASTIfStatement result) {
        if (result == null)
            return;
        IASTIfStatement current = result;
        while (current.getElseClause() instanceof IASTIfStatement)
            current = (IASTIfStatement) current.getElseClause();

        while (current != null) {
            ASTNode r = ((ASTNode) current);
            if (current.getElseClause() != null) {
                ASTNode else_clause = ((ASTNode) current.getElseClause());
                r.setLength(else_clause.getOffset() + else_clause.getLength() - r.getOffset());
            } else {
                ASTNode then_clause = (ASTNode) current.getThenClause();
                if (then_clause != null)
                    r.setLength(then_clause.getOffset() + then_clause.getLength() - r.getOffset());
            }
            if (current.getParent() != null && current.getParent() instanceof IASTIfStatement)
                current = (IASTIfStatement) current.getParent();
            else
                current = null;
        }
    }

    protected abstract IASTProblemExpression createProblemExpression();

    protected IASTStatement parseCompoundStatement() throws EndOfFileException, BacktrackException {
        IASTCompoundStatement compound = compoundStatement();
        return compound;
    }

    protected IASTStatement parseDefaultStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset(); // t_default
        int lastOffset = consume(IToken.tCOLON).getEndOffset();

        IASTDefaultStatement df = createDefaultStatement();
        ((ASTNode) df).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return df;
    }

    protected IASTStatement parseCaseStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset(); // t_case
        IASTExpression caseExpression = constantExpression();
        int lt1 = LT(1);
		if (lt1 == IToken.tELLIPSIS) {
			consume();
        	IASTExpression upperBoundExpression= constantExpression();
        	caseExpression = buildBinaryExpression(IASTBinaryExpression.op_assign,
        			caseExpression, upperBoundExpression, calculateEndOffset(upperBoundExpression));
        	lt1= LT(1);
		}
        int lastOffset = 0;
        switch (lt1) {
        case IToken.tCOLON:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throwBacktrack(LA(1));
        }

        IASTCaseStatement cs = createCaseStatement();
        ((ASTNode) cs).setOffsetAndLength(startOffset, lastOffset - startOffset);
        cs.setExpression(caseExpression);
        return cs;
    }

 
    protected int figureEndOffset(IASTDeclSpecifier declSpec, IASTDeclarator[] declarators) {
        if (declarators.length == 0)
            return calculateEndOffset(declSpec);
        return calculateEndOffset(declarators[declarators.length - 1]);
    }


    protected int figureEndOffset(IASTDeclSpecifier declSpecifier,
            IASTDeclarator declarator) {
        if (declarator == null || ((ASTNode) declarator).getLength() == 0)
            return calculateEndOffset(declSpecifier);
        return calculateEndOffset(declarator);
    }


    protected void throwBacktrack(IToken token) throws BacktrackException {
        throwBacktrack(token.getOffset(), token.getLength());
    }

    protected IASTNode[] parseTypeIdOrUnaryExpression(boolean typeIdWithParentheses) throws EndOfFileException {
    	return parseTypeIdOrUnaryExpression(typeIdWithParentheses, new int[1]);
    }

    protected IASTNode[] parseTypeIdOrUnaryExpression(boolean typeIdWithParentheses, int[] endoffset) throws EndOfFileException {
        IASTTypeId typeId = null;
        
        IToken typeIdLA = null;
        IToken mark = mark();
        try {
            if (typeIdWithParentheses)
                consume(IToken.tLPAREN);
            typeId = typeId(DeclarationOptions.TYPEID);
            if (typeId != null) {	
            	if (typeIdWithParentheses) {
            		switch (LT(1)) {
            		case IToken.tRPAREN:
            		case IToken.tEOC:
            			endoffset[0]= consume().getEndOffset();
                		typeIdLA = LA(1);
            			break;
            		default:
            			typeId = null;
            		}
            	}
            	else {
        			endoffset[0]= calculateEndOffset(typeId);
            		typeIdLA = LA(1);
            	}
            }
        } catch (BacktrackException e) { }
        backup(mark);
        
        IToken unaryExpressionLA = null;
        IASTExpression unaryExpression = null;
        try {
            unaryExpression = unaryExpression(); // throws BacktrackException
            unaryExpressionLA = LA(1);
        } catch (BacktrackException bte) { }

        if (unaryExpression == null && typeId != null) {
            backup(typeIdLA);
            return new IASTNode[] {typeId};
        }
        if (unaryExpression != null && typeId == null) {
            backup(unaryExpressionLA);
            endoffset[0]= calculateEndOffset(unaryExpression);
            return new IASTNode[] {unaryExpression};
        }
        if (unaryExpression != null && typeId != null) {
        	if (typeIdLA == unaryExpressionLA) {
        		return new IASTNode[] {typeId, unaryExpression};
        	}
        	return new IASTNode[] {unaryExpression};
        }
        return IASTNode.EMPTY_NODE_ARRAY;

    }

    protected abstract IASTAmbiguousExpression createAmbiguousExpression();

    protected IASTExpression parseSizeofExpression() throws BacktrackException, EndOfFileException {
        int startingOffset = consume().getOffset(); // t_sizeof
        int[] endoffset= new int[] {0};
        IASTNode[] choice = parseTypeIdOrUnaryExpression(true, endoffset);
        switch (choice.length) {
        case 1:
            if (choice[0] instanceof IASTExpression)
                return buildUnaryExpression(IASTUnaryExpression.op_sizeof,
                        (IASTExpression) choice[0], startingOffset, endoffset[0]);
            else if (choice[0] instanceof IASTTypeId)
                return buildTypeIdExpression(IASTTypeIdExpression.op_sizeof,
                        (IASTTypeId) choice[0], startingOffset, endoffset[0]);
            throwBacktrack(LA(1));
            break;
        case 2:
            IASTAmbiguousExpression ambExpr = createAmbiguousExpression();
            IASTExpression e1 = buildTypeIdExpression(
                    IASTTypeIdExpression.op_sizeof, (IASTTypeId) choice[0],
                    startingOffset, endoffset[0]);
            IASTExpression e2 = buildUnaryExpression(
                    IASTUnaryExpression.op_sizeof, (IASTExpression) choice[1],
                    startingOffset, endoffset[0]);
            ambExpr.addExpression(e1);
            ambExpr.addExpression(e2);
            ((ASTNode) ambExpr).setOffsetAndLength((ASTNode) e2);
            return ambExpr;
        default:
        }
        throwBacktrack(LA(1));
        return null;
    }
    
    /**
     * @throws BacktrackException
     */
    protected IASTStatement forInitStatement(DeclarationOptions option) throws BacktrackException, EndOfFileException {
        if( LT(1) == IToken.tSEMI )
            return parseNullStatement();
        return parseDeclarationOrExpressionStatement(option);
    }

    /**
     * Accept a sequence of __attribute__ or __declspec
     * 
     * @param allowAttrib if true accept any number of __attribute__ 
     * @param allowDeclspec if true accept any number of __declspec
     * @throws BacktrackException
     * @throws EndOfFileException
     */
    protected void __attribute_decl_seq(boolean allowAttrib, boolean allowDeclspec) throws BacktrackException, EndOfFileException {
        while (true) {
        	IToken token = LA(1);
        	if ( allowAttrib && (token.getType() == IGCCToken.t__attribute__)) {
        		__attribute__();
        	} else if (allowDeclspec && (token.getType() == IGCCToken.t__declspec)) {
        		__declspec();
        	} else {
        		break;
        	}
        }
    }

    protected void __attribute__() throws BacktrackException, EndOfFileException {
    	IToken token = LA(1);
    	
    	if (token.getType() == IGCCToken.t__attribute__) {
    		consume();
    		
    		token = LA(1);
    		
    		if (token.getType() == IToken.tLPAREN) {
    			consume();
            	while(true) {
            		token = LA(1);
            		switch(token.getType()) {
            			case IToken.tLPAREN:
            				consume();
                        	boolean ident=false;
                        	boolean comma1=false;
                        	boolean first=true;
            				whileLoop: while(true) {
            					token = LA(1);
            					switch(token.getType()) {
            						case IToken.tIDENTIFIER:
           								if (comma1 || first) {
           									ident=true;
           									first=false;
           								} else {
           									throwBacktrack(token.getOffset(), token.getLength());
           								}
            							consume();
            							break;
            						case IToken.tLPAREN:
            							consume();
            							if (ident) {
            								token = LA(1);
            								// consume the parameters
            								whileLoop2: while(true) {
            									try {
            										expression();
            									} catch (BacktrackException be) {
            										switch(LT(1)) {
	            										case IToken.tCOMMA:
	            											consume();
	            											break;
	            										case IToken.tRPAREN:
	            											consume();
	            											break whileLoop2;
	            										default:
	            											throw be;
            										}
            									}
            								}
            							} else {
            								throwBacktrack(token.getOffset(), token.getLength()); // can't have __attribute((()))
            							}
            							break;
            						case IToken.tRPAREN:
           								consume();
           								break whileLoop;
            						case IToken.tCOMMA:
            							if (ident) {
            								ident=false;
            								comma1=true;
            							}
            							consume();            								
            							break;
            						case IToken.t_const:
            							consume();
            							break;
            						default:
           								throwBacktrack(token.getOffset(), token.getLength());
            							break;
            					}
            				}
            				break;
            			case IToken.tRPAREN: // finished
            				consume();
            				return;
            			default:
            				throwBacktrack(token.getOffset(), token.getLength());
            		}
            	}
    		}
    	}
    }
    
    protected void __declspec() throws BacktrackException, EndOfFileException {
    	IToken token = LA(1);

    	if (token.getType() == IGCCToken.t__declspec) {
    		consume();

    		token = LA(1);

    		if (token.getType() == IToken.tLPAREN) {
    			consume();
    			while(true) {
    				token = LA(1);
    				consume();
    				if (token.getType() == IToken.tRPAREN) {
    					break;
    				}
    			}
    		}
    	}
    }    

    /**
	 * Hook method to support (skip) additional declspec modifiers.
	 * @throws BacktrackException
     * @throws EndOfFileException 
	 */
	protected void handleOtherDeclSpecModifier() throws BacktrackException, EndOfFileException {
		// default action: consume keyword plus optional parenthesised "something"
		consume();

		IToken token = LA(1);

		if (token.getType() == IToken.tLPAREN) {
			consume();
			int openParen= 1;
			while(true) {
				token = LA(1);
				consume();
				if (token.getType() == IToken.tLPAREN) {
					++openParen;
				} else if (token.getType() == IToken.tRPAREN) {
					--openParen;
					if (openParen == 0) {
						break;
					}
				}
			}
		}
	}

    /**
     * In case a cast expression is followed by +/- or & we should avoid it:
     * (a)+1 vs. (int)+1;
     * @since 4.0
     */
	protected boolean avoidCastExpressionByHeuristics() throws EndOfFileException {
		if (LT(1) == IToken.tIDENTIFIER) {
			if (LT(2) == IToken.tRPAREN) {
				switch (LT(3)) {
				case IToken.tPLUS:
				case IToken.tMINUS:
				case IToken.tAMPER:
				case IToken.tSTAR:
		    		return true;
		    	}
			}
		}
		return false;
	}
	
	protected boolean canBeTypeSpecifier() throws EndOfFileException {

		switch (LT(1)) {
		// simple type specifiers:
		case IToken.tIDENTIFIER:
		case IToken.tCOLONCOLON:
        case IToken.t_void:
		case IToken.t_char:
		case IToken.t_wchar_t:
		case IToken.t_bool:
		case IToken.t_short:
        case IToken.t_int:
        case IToken.t_long:
        case IToken.t_float:
        case IToken.t_double:
        case IToken.t__Bool:
        case IToken.t__Complex:
        case IToken.t__Imaginary:
        case IToken.t_signed:
        case IToken.t_unsigned:

        // class-specifier:
        case IToken.t_class:
        case IToken.t_struct:
        case IToken.t_union:

        // enum-specifier:
        case IToken.t_enum:

        // elaborated type specifier: (together with class, struct, union, enum
        case IToken.t_typename:
        	
        // cq-qualifiers
		case IToken.t_const:
        case IToken.t_volatile:
        case IToken.t_restrict:

        // gcc-special
        case IGCCToken.t_typeof:
        case IGCCToken.t__attribute__:

        // content assist
        case IToken.tCOMPLETION:
        	return true;
        	
        default:
        	return false;
		}
	}
	
	protected void skipBrackets(int left, int right) throws EndOfFileException, BacktrackException {
		consume(left);
		int nesting= 0;
		while(true) {
			final int lt1= LT(1);
			if (lt1 == IToken.tEOC)
				throwBacktrack(LA(1));

			consume();
			if (lt1 == left) {
				nesting++;
			} else if (lt1 == right) {
				if (--nesting < 0) {
					return;
				}
			}
		}
	}
}
