/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of statement nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 * 
 * @see Scribe
 * @see IASTStatement
 * @author Emanuel Graf IFS
 */
public class StatementWriter extends NodeWriter {
	private static final String DEFAULT = "default:"; //$NON-NLS-1$
	private static final String CASE = "case "; //$NON-NLS-1$
	private static final String WHILE = "while ("; //$NON-NLS-1$
	private static final String TRY = "try "; //$NON-NLS-1$
	private static final String CATCH = "catch ("; //$NON-NLS-1$
	private static final String RETURN = "return"; //$NON-NLS-1$
	private static final String GOTO = "goto "; //$NON-NLS-1$
	private static final String CONTINUE = "continue"; //$NON-NLS-1$
	private static final String BREAK = "break"; //$NON-NLS-1$
	private static final String ELSE = "else"; //$NON-NLS-1$
	private static final String IF = "if ("; //$NON-NLS-1$
	private static final String FOR = "for ("; //$NON-NLS-1$
	private static final String DO_WHILE = " while ("; //$NON-NLS-1$
	private static final String DO = "do"; //$NON-NLS-1$
	private static final String SWITCH_BRACKET = "switch ("; //$NON-NLS-1$
	private boolean compoundNoNewLine = false;
	private boolean switchIsNew;
	private boolean decrementIndentationLevelOneMore = false;
	private final DeclarationWriter declWriter;

	public StatementWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
		declWriter = new DeclarationWriter(scribe, visitor, commentMap);
	}
	
	/**
	 * Prints a statement.
	 * 
	 * @param statement the statement
	 * @param newLine if true print a newline if statement usually have one.
	 * @return {@link ASTVisitor#PROCESS_SKIP}
	 */
	protected int writeStatement(IASTStatement statement, boolean newLine) {
		if (statement instanceof IASTAmbiguousStatement) {
			//TODO HSR Leo test
			statement.accept(visitor);
			newLine = false;
		} else if (statement instanceof IASTExpressionStatement) {
			writeExpressionStatement((IASTExpressionStatement) statement);
			//usually newLine
		} else if (statement instanceof IASTDeclarationStatement) {
			writeDeclarationStatement((IASTDeclarationStatement) statement);
			newLine = false;
		} else if (statement instanceof IASTNullStatement) {
			writeNullStatement((IASTNullStatement)statement);
//			usually newLine
		} else if (statement instanceof IASTReturnStatement) {
			writeReturnStatement((IASTReturnStatement)statement);
//			usually newLine
		} else if (statement instanceof IASTGotoStatement) {
			writeGotoStatement((IASTGotoStatement) statement);
//			usually newLine
		} else if (statement instanceof IASTLabelStatement) {
			writeLabelStatement((IASTLabelStatement) statement);
			newLine = false;
		} else if (statement instanceof IASTCaseStatement) {
			writeCaseStatement((IASTCaseStatement) statement);
//			usually newLine			
		} else if (statement instanceof IASTDefaultStatement) {
			writeDefaultStatement((IASTDefaultStatement)statement);
		} else if (statement instanceof IASTContinueStatement) {
			writeContinueStatement((IASTContinueStatement)statement);
//			usually newLine
		} else if (statement instanceof IASTCompoundStatement) {
			writeCompoundStatement((IASTCompoundStatement) statement);
			if (compoundNoNewLine) {
				newLine = false;
				compoundNoNewLine = false;
			}
		} else if (statement instanceof IASTBreakStatement) {
			writeBreakStatement((IASTBreakStatement) statement);
//			usually newLine
		} else if (statement instanceof IASTSwitchStatement) {
			writeSwitchStatement((IASTSwitchStatement) statement);
			newLine = false;
		} else if (statement instanceof IASTIfStatement) {
			writeIfStatement((IASTIfStatement) statement);			
			newLine = false;
		} else if (statement instanceof IASTWhileStatement) {
			writeWhileStatement((IASTWhileStatement) statement);
			newLine = false;
		} else if (statement instanceof IASTForStatement) {
			writeForStatement((IASTForStatement) statement);
			newLine = false;
		} else if (statement instanceof ICPPASTRangeBasedForStatement) {
			writeForStatement((ICPPASTRangeBasedForStatement) statement);
			newLine = false;
		} else if (statement instanceof IASTDoStatement) {
			writeDoStatement((IASTDoStatement) statement);
			newLine = true;
		} else if (statement instanceof ICPPASTTryBlockStatement) {
			writeTryBlockStatement((ICPPASTTryBlockStatement) statement);
			newLine = false;
		} else if (statement instanceof ICPPASTCatchHandler) {
			writeCatchHandler((ICPPASTCatchHandler) statement);
			newLine = false;
		} else if (statement instanceof IASTProblemStatement) {
			throw new ProblemRuntimeException((IASTProblemStatement)statement);
		} 
		
		writeTrailingComments(statement, newLine);			

		return ASTVisitor.PROCESS_SKIP;
	}

	private void writeDoStatement(IASTDoStatement doStatement) {
		nextCompoundNoNewLine();
		
		scribe.print(DO);
		writeBodyStatement(doStatement.getBody(), true);
		scribe.print(DO_WHILE);
		doStatement.getCondition().accept(visitor);
		scribe.print(')');
		scribe.printSemicolon();
	}

	private void writeForStatement(IASTForStatement forStatement) {
		scribe.noNewLines();
		scribe.print(FOR);
		writeStatement(forStatement.getInitializerStatement(),false);
		if (forStatement instanceof ICPPASTForStatement) {
			ICPPASTForStatement cppForStatment = (ICPPASTForStatement) forStatement;
			IASTDeclaration cppConditionDeclaration = cppForStatment.getConditionDeclaration();
			if (cppConditionDeclaration == null) {
				visitNodeIfNotNull(cppForStatment.getConditionExpression());
				scribe.printSemicolon();
			} else {
				cppConditionDeclaration.accept(visitor);
			}
		} else {
			if (forStatement.getConditionExpression() != null) {
				forStatement.getConditionExpression().accept(visitor);
				scribe.printSemicolon();
			}
		}
		
		visitNodeIfNotNull(forStatement.getIterationExpression());
		scribe.print(')');
		scribe.newLines();
		nextCompoundNoNewLine();
		writeBodyStatement(forStatement.getBody(), false);
	}

	private void writeForStatement(ICPPASTRangeBasedForStatement forStatment) {
		scribe.noNewLines();
		scribe.print(FOR);
		writeDeclarationWithoutSemicolon(forStatment.getDeclaration());
		scribe.print(COLON_SPACE);
		visitNodeIfNotNull(forStatment.getInitializerClause());
		scribe.print(')');
		scribe.newLines();
		nextCompoundNoNewLine();
		writeBodyStatement(forStatment.getBody(), false);
	}

	private void writeIfStatement(IASTIfStatement ifStatement) {
		scribe.print(IF);
		scribe.noNewLines();
		if (ifStatement instanceof ICPPASTIfStatement) {
			ICPPASTIfStatement cppIfStatment = (ICPPASTIfStatement) ifStatement;

			if (cppIfStatment.getConditionDeclaration() == null) {
				cppIfStatment.getConditionExpression().accept(visitor);
			} else {
				writeDeclarationWithoutSemicolon(cppIfStatment.getConditionDeclaration());
			}
		} else {
			ifStatement.getConditionExpression().accept(visitor);
		}
		
		scribe.print(')');
		scribe.newLines();
		nextCompoundNoNewLine();
		IASTStatement elseClause = ifStatement.getElseClause();
		writeBodyStatement(ifStatement.getThenClause(), elseClause != null);
		
		if (elseClause != null) {
			scribe.print(ELSE);
			nextCompoundNoNewLine();
			writeBodyStatement(elseClause, false);
		}
	}

	protected void writeDeclarationWithoutSemicolon(IASTDeclaration declaration) {
		declWriter.writeDeclaration(declaration, false);
	}

	private void writeBreakStatement(IASTBreakStatement statement) {
		scribe.print(BREAK);
		scribe.printSemicolon();
	}

	private void writeContinueStatement(IASTContinueStatement statement) {
		scribe.print(CONTINUE);
		scribe.printSemicolon();
	}

	private void writeLabelStatement(IASTLabelStatement labelStatement) {
		labelStatement.getName().accept(visitor);
		scribe.print(':');
		scribe.newLine();			
		labelStatement.getNestedStatement().accept(visitor);
	}

	private void writeGotoStatement(IASTGotoStatement gotoStatement) {
		scribe.print(GOTO);
		gotoStatement.getName().accept(visitor);
		scribe.printSemicolon();
	}

	private void writeReturnStatement(IASTReturnStatement returnStatement) {
		scribe.noNewLines();
		scribe.print(RETURN);
		IASTExpression returnValue = returnStatement.getReturnValue();
		if (returnValue != null) {
			scribe.printSpaces(1);
			returnValue.accept(visitor);
		}
		scribe.newLines();
		scribe.printSemicolon();
	}

	private void writeNullStatement(IASTNullStatement nullStmt) {
		scribe.printSemicolon();
	}
	
	private void writeDeclarationStatement(IASTDeclarationStatement decStmt) {
		decStmt.getDeclaration().accept(visitor);
	}

	private void writeExpressionStatement(IASTExpressionStatement expStmt) {
		expStmt.getExpression().accept(visitor);
		scribe.printSemicolon();
	}

	private void writeCatchHandler(ICPPASTCatchHandler catchStatement) {
		scribe.print(CATCH);
		if (catchStatement.isCatchAll()) {
			scribe.print(VAR_ARGS);
		} else {
			scribe.noSemicolon();
			scribe.noNewLines();
			catchStatement.getDeclaration().accept(visitor);
			scribe.newLines();
		}
		scribe.print(')');
		writeBodyStatement(catchStatement.getCatchBody(), true);
	}

	private void writeTryBlockStatement(ICPPASTTryBlockStatement tryStatement) {
		scribe.print(TRY);
		tryStatement.getTryBody().accept(visitor);
		for (ICPPASTCatchHandler catchStatement : tryStatement.getCatchHandlers()) {
			writeStatement(catchStatement, false);
		}
	}

	private void writeWhileStatement(IASTWhileStatement whileStatment) {
		scribe.print(WHILE);
		scribe.noNewLines();
		if (whileStatment instanceof ICPPASTWhileStatement) {
			ICPPASTWhileStatement cppWhileStatment = (ICPPASTWhileStatement) whileStatment;
			if (cppWhileStatment.getConditionDeclaration() == null) {
				cppWhileStatment.getCondition().accept(visitor);
			} else {
				writeDeclarationWithoutSemicolon(cppWhileStatment.getConditionDeclaration());
			}		
		} else {
			whileStatment.getCondition().accept(visitor);
		}
		scribe.print(')');
		scribe.newLines();
		nextCompoundNoNewLine();
		writeBodyStatement(whileStatment.getBody(), false);
	}

	private void writeCaseStatement(IASTCaseStatement caseStatement) {
		nextCompoundIndentationLevelOneMore();
		
		if (!switchIsNew) {
			scribe.decrementIndentationLevel();
		}
		scribe.print(CASE);
		caseStatement.getExpression().accept(visitor);
		scribe.print(':');
		scribe.incrementIndentationLevel();
		switchIsNew = false;
	}

	private void writeSwitchStatement(IASTSwitchStatement switchStatement) {
		switchIsNew = true;
		
		scribe.print(SWITCH_BRACKET);
		scribe.noNewLines();
		if (switchStatement instanceof ICPPASTSwitchStatement) {
			ICPPASTSwitchStatement cppSwitchStatement = (ICPPASTSwitchStatement) switchStatement;
			if (cppSwitchStatement.getControllerDeclaration() == null) {
				cppSwitchStatement.getControllerExpression().accept(visitor);
			} else {
				declWriter.writeDeclaration(cppSwitchStatement.getControllerDeclaration(), false);
			}
		} else {
			switchStatement.getControllerExpression().accept(visitor);
		}
		scribe.print(')');
		scribe.newLines();
		nextCompoundNoNewLine();
		writeBodyStatement(switchStatement.getBody(), false);
		
		switchIsNew = false;
	}

	private void writeDefaultStatement(IASTDefaultStatement defaultStatement) {
		nextCompoundIndentationLevelOneMore();
		
		if (!switchIsNew) {
			scribe.decrementIndentationLevel();
		}
		scribe.print(DEFAULT);
		scribe.incrementIndentationLevel();
		switchIsNew = false;
	}
	
	private void writeCompoundStatement(IASTCompoundStatement compoundStatement) {
		scribe.printLBrace();
		scribe.newLine();
		for (IASTStatement statements : getNestedStatements(compoundStatement)) {
			statements.accept(visitor);
		}
		
		if (hasFreestandingComments(compoundStatement)) {
			writeFreestandingComments(compoundStatement);			
		}
		
		if (decrementIndentationLevelOneMore) {
			scribe.decrementIndentationLevel();
			decrementIndentationLevelOneMore = false;
		}
		scribe.printRBrace();
	}

	protected IASTStatement[] getNestedStatements(IASTCompoundStatement compoundStatement) {
		return compoundStatement.getStatements();
	}	
	
	protected void writeBodyStatement(IASTStatement statement, boolean isDoStatement) {
		if (statement instanceof IASTCompoundStatement) {
			//TODO hsr existiert noch eine methode
			statement.accept(visitor);
			if (!isDoStatement) {
				scribe.newLine();
			}
			compoundNoNewLine = false;
		} else if (statement instanceof IASTNullStatement) {
			statement.accept(visitor);
			scribe.newLine();
		} else {
			scribe.incrementIndentationLevel();	
			scribe.newLine();	
			statement.accept(visitor);
			scribe.decrementIndentationLevel();	
		}
	}

	/**
	 * Write no new Line after the next Compound-Statement 
	 */
	protected void nextCompoundNoNewLine() {
		compoundNoNewLine = true;
	}
	
	/**
	 * Indent one time more at the end (before the closing Brackets) 
	 * of a Compound-Statement 
	 */
	protected void nextCompoundIndentationLevelOneMore() {
		decrementIndentationLevelOneMore = true;
	}

	protected int writeMixedStatement(IASTStatement statement) {
		String code = statement.getRawSignature();
		scribe.println(code);
		return ASTVisitor.PROCESS_SKIP;
	}
}
