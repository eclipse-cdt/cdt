/*******************************************************************************
 * Copyright (c) 2010 Gil Barash 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Gil Barash  - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class CaseBreakChecker extends AbstractIndexAstChecker implements ICheckerWithPreferences {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.CaseBreakProblem"; //$NON-NLS-1$
	public static final String PARAM_LAST_CASE = "last_case_param"; //$NON-NLS-1$
	public static final String PARAM_EMPTY_CASE = "empty_case_param"; //$NON-NLS-1$
	public static final String PARAM_NO_BREAK_COMMENT = "no_break_comment"; //$NON-NLS-1$
	public static final String DEFAULT_NO_BREAK_COMMENT = "no break"; //$NON-NLS-1$
	private Boolean _checkLastCase; // Should we check the last case in the switch?
	private Boolean _checkEmptyCase; // Should we check an empty case (a case without any statements within it)
	private String _noBreakComment; // The comment suppressing this warning 

	/**
	 * This visitor looks for "switch" statements and invokes "SwitchVisitor" on
	 * them
	 */
	class SwitchFindingVisitor extends ASTVisitor {
		protected IASTStatement _switchStatement; // The "switch" we're visiting (used by inheriting classes to avoid re-visiting the same "switch" again)

		SwitchFindingVisitor() {
			shouldVisitStatements = true;
			_switchStatement = null;
		}

		/**
		 * @param statement
		 * @return true iff the statement is directly under the "switch" and not
		 *         in the scope of some loop statement, such as "while".
		 */
		private boolean doesStatementAffectThisSwitch(IASTStatement statement) {
			IASTNode parent = statement.getParent();
			if (parent == _switchStatement)
				return true;
			if (parent instanceof IASTCompoundStatement)
				return doesStatementAffectThisSwitch((IASTCompoundStatement) parent);
			return false;
		}

		/**
		 * @param statement
		 * @return true iff the statement is on of:
		 *         - "break" (checks that the break actually exists the
		 *         "switch")
		 *         - "return"
		 *         - "continue"
		 *         - "goto" (does not check that the goto actually exists the
		 *         switch)
		 *         - "thorw"
		 *         - "exit"
		 */
		protected boolean isBreakOrExitStatement(IASTStatement statement) {
			CxxAstUtils utils = CxxAstUtils.getInstance();
			return (statement instanceof IASTBreakStatement && doesStatementAffectThisSwitch(statement))
					|| statement instanceof IASTReturnStatement || statement instanceof IASTContinueStatement
					|| statement instanceof IASTGotoStatement || utils.isThrowStatement(statement) || utils.isExitStatement(statement);
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTSwitchStatement) {
				// Are we already visiting this statement?
				if (_switchStatement == null || !statement.equals(_switchStatement)) {
					SwitchVisitor switch_visitor = new SwitchVisitor(statement);
					statement.accept(switch_visitor);
					return PROCESS_SKIP;
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	/**
	 * This visitor visits a switch statement and checks the end of each
	 * "case" statement (to see that it ends with a "break").
	 * Because it extends SwitchFindingVisitor is would also check nested
	 * "switch"s
	 */
	class SwitchVisitor extends SwitchFindingVisitor {
		private IASTStatement _prev_case_stmnt;
		private boolean _first_case_statement;
		private int _prev_break_stmnt_offset;
		private int _prev_normal_stmnt_offset;
		private int _prev_case_stmnt_offset;

		SwitchVisitor(IASTStatement switch_statement) {
			shouldVisitStatements = true;
			_first_case_statement = true;
			_switchStatement = switch_statement;
			_prev_break_stmnt_offset = 0;
			_prev_normal_stmnt_offset = 0;
			_prev_case_stmnt_offset = 0;
			_prev_case_stmnt = null;
		}

		/**
		 * @return Is this an "empty" case (i.e. with no statements in it)
		 */
		private boolean isEmptyCase() {
			return _prev_case_stmnt_offset > _prev_normal_stmnt_offset;
		}

		/**
		 * @return Was a "break" statement the last statement in this case
		 */
		private boolean breakFoundPrevious() {
			return _prev_normal_stmnt_offset < _prev_break_stmnt_offset && _prev_case_stmnt_offset < _prev_break_stmnt_offset;
		}

		/**
		 * Check the last case we've visited
		 * 
		 * @param comment The comment ending this case (may be NULL)
		 * @param lastOne true if it am actual Last statement
		 */
		private void checkPreviousCase(IASTComment comment, boolean lastOne) {
			if (comment != null) {
				String str = getTrimmedComment(comment);
				if (str.equalsIgnoreCase(_noBreakComment))
					return;
			}
			if (_prev_case_stmnt == null)
				return; // This is an empty switch
			if (breakFoundPrevious())
				return; // There was a "break" before the current statement 
			if (lastOne == true || !isEmptyCase() || _checkEmptyCase) {
				reportProblem(ER_ID, _prev_case_stmnt, (Object) null);
			}
		}

		/**
		 * @param comment
		 * @return
		 */
		public String getTrimmedComment(IASTComment comment) {
			String str = new String(comment.getComment());
			if (comment.isBlockComment())
				str = str.substring(2, str.length() - 2);
			else
				str = str.substring(2);
			str = str.trim();
			return str;
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement) {
				if (_first_case_statement) {
					/*
					 * This is the first "case", i.e. the beginning of the
					 * "switch"
					 */
					_first_case_statement = false;
				} else {
					/*
					 * This is not the 1st "case", meaning that a previous case
					 * has just ended,
					 * Let's check that case and see how it ended...
					 */
					IASTComment comment = getLeadingComment(statement);
					/*
					 * 'comment' is the last comment found in this case (after
					 * the last statement in this "case"
					 */
					checkPreviousCase(comment, false);
				}
				/* Update variables with the new opened "case" */
				_prev_case_stmnt_offset = statement.getFileLocation().getNodeOffset();
				_prev_case_stmnt = statement;
			} else if (isBreakOrExitStatement(statement)) { // A relevant "break" statement
				_prev_break_stmnt_offset = statement.getFileLocation().getNodeOffset();
			} else { // a non-switch related statement
				_prev_normal_stmnt_offset = statement.getFileLocation().getNodeOffset();
			}
			return super.visit(statement); // This would handle nested "switch"s
		}

		/**
		 * @param statement
		 * @return
		 */
		public IASTComment getLeadingComment(IASTStatement statement) {
			return CxxAstUtils.getInstance().getLeadingComment(statement);
		}


		/**
		 * @param statement
		 * @return
		 */
		public IASTComment getFreestandingComment(IASTStatement statement) {
			return CxxAstUtils.getInstance().getFreestandingComment(statement);
		}

		@Override
		public int leave(IASTStatement statement) {
			/*
			 * Are we leaving the "switch" altogether? (we need to see how the
			 * last "case" ended)
			 */
			if (_checkLastCase && statement instanceof IASTCompoundStatement && statement.getParent() == _switchStatement) {
				IASTComment comment = getFreestandingComment(statement);
				/*
				 * 'comment' is the last comment found in this case (after
				 * the
				 * last statement in this "case"
				 */
				checkPreviousCase(comment, true);
			}
			return super.leave(statement);
		}
	}

	/************************************************
	 * "CaseBreakChecker" functions...
	 ************************************************/
	public CaseBreakChecker() {
	}

	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_NO_BREAK_COMMENT, CheckersMessages.CaseBreakChecker_DefaultNoBreakCommentDescription,
				DEFAULT_NO_BREAK_COMMENT);
		addPreference(problem, PARAM_LAST_CASE, CheckersMessages.CaseBreakChecker_LastCaseDescription, Boolean.TRUE);
		addPreference(problem, PARAM_EMPTY_CASE, CheckersMessages.CaseBreakChecker_EmptyCaseDescription, Boolean.FALSE);
	}

	public void processAst(IASTTranslationUnit ast) {
		_checkLastCase = (Boolean) getPreference(getProblemById(ER_ID, getFile()), PARAM_LAST_CASE);
		_checkEmptyCase = (Boolean) getPreference(getProblemById(ER_ID, getFile()), PARAM_EMPTY_CASE);
		_noBreakComment = (String) getPreference(getProblemById(ER_ID, getFile()), PARAM_NO_BREAK_COMMENT);
		SwitchFindingVisitor visitor = new SwitchFindingVisitor();
		ast.accept(visitor);
	}
}
