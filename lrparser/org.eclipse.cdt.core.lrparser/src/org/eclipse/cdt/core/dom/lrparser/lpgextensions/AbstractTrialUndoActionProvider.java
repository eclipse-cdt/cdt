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
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.lpgextensions;

import java.util.Collections;
import java.util.List;

import lpg.lpgjavaruntime.IToken;
import lpg.lpgjavaruntime.LexStream;
import lpg.lpgjavaruntime.PrsStream;

/**
 * Base class for parser action classes which support trial, undo and
 * final actions.
 */
public abstract class AbstractTrialUndoActionProvider<ACT, RULE_DATA> extends PrsStream
		implements ITrialUndoActionProvider<RULE_DATA> {
	/**
	 * An action that does nothing.
	 */
	public static final Action<Object, Object> EMPTY_ACTION = new Action<>();

	/**
	 * The parser table interpreter.
	 */
	protected TrialUndoParser btParser;

	public AbstractTrialUndoActionProvider() {
		super();
	}

	public AbstractTrialUndoActionProvider(LexStream lexStream) {
		super(lexStream);
	}

	/**
	 * Actions for reduction rules.
	 */
	protected Action<ACT, RULE_DATA>[] ruleAction;

	protected ACT parserAction;

	public void setParserAction(ACT parserAction) {
		this.parserAction = parserAction;
	}

	/**
	 * The reduction rule which is currently being processed.
	 */
	protected Rule<RULE_DATA> activeRule;

	/**
	 * Returns the number of tokens in the rule being reduced.
	 */
	public int getRuleTokenCount() {
		return activeRule.getEndTokenOffset() - activeRule.getStartTokenOffset() + 1;
	}

	/**
	 * Returns the tokens in the rule being reduced.
	 */
	@SuppressWarnings("unchecked")
	public List<IToken> getRuleTokens() {
		return Collections.unmodifiableList(getTokens().subList(getFirstRealToken(activeRule.getStartTokenOffset()),
				activeRule.getEndTokenOffset() + 1));
	}

	public void backtrack() {
		btParser.backtrack();
	}

	@Override
	public void setActiveRule(Rule<RULE_DATA> rule) {
		activeRule = rule;
	}

	@Override
	public Rule<RULE_DATA> getActiveRule() {
		return activeRule;
	}

	@Override
	public final boolean trialAction(int rule_number) {
		return ruleAction[rule_number].doTrial(this, parserAction);
	}

	@Override
	public final void undoAction(int rule_number) {
		ruleAction[rule_number].doUndo(this, parserAction);
	}

	@Override
	public final void finalAction(int rule_number) {
		//System.out.println("finalAction: " + rule_number); //$NON-NLS-1$
		ruleAction[rule_number].doFinal(this, parserAction);
	}

	public TrialUndoParser getParser() {
		return btParser;
	}

	/**
	 * Returns the offset of the leftmost token of the
	 * rule being reduced.
	 */
	private int getLeftSpan() {
		return getFirstRealToken(activeRule.getStartTokenOffset());
	}

	/**
	 * Returns the leftmost token of the rule being reduced.
	 */
	public IToken getLeftIToken() {
		return super.getIToken(getLeftSpan());
	}

	/**
	 * Returns the offset of the rightmost token of the
	 * rule being reduced.
	 */
	private int getRightSpan() {
		return activeRule.getEndTokenOffset();
	}

	/**
	 * Returns the rightmost token of the rule being reduced.
	 */
	public IToken getRightIToken() {
		return super.getIToken(getRightSpan());
	}

	public static <ACT, RULE_DATA> Action<ACT, RULE_DATA> emptyAction() {
		return new Action<>();
	}

	@SuppressWarnings("unused")
	public static class Action<ACT, RULE_DATA> {

		public void doFinal(ITrialUndoActionProvider<RULE_DATA> provider, ACT action) {
			// convenience method, can be overridden

		}

		public boolean doTrial(ITrialUndoActionProvider<RULE_DATA> provider, ACT action) {
			//System.out.println(provider.getActiveRule());
			//System.out.println(Rules.lookup(provider.getActiveRule().getRuleNumber()));
			return false;
		}

		public void doUndo(ITrialUndoActionProvider<RULE_DATA> provider, ACT action) {
			//System.out.println(provider.getActiveRule() + " - undo");
			// convenience method, can be overridden
		}
	}

	public static class DeclaredAction<ACT, RULE_DATA> extends Action<ACT, RULE_DATA> {
		protected boolean hasUndo = false;

		@Override
		@SuppressWarnings("unused")
		public boolean doTrial(ITrialUndoActionProvider<RULE_DATA> provider, ACT action) {
			return true;
		}
	}

	/**
	 * Action for a null rule
	 */
	static final class NullAction<ACT, RULE_DATA> extends Action<ACT, RULE_DATA> {
		@Override
		@SuppressWarnings("unused")
		public void doFinal(ITrialUndoActionProvider<RULE_DATA> provider, ACT action) {
			// do nothing
		}
	}

	static final class BadAction<ACT, RULE_DATA> extends Action<ACT, RULE_DATA> {
		@Override
		@SuppressWarnings("unused")
		public void doFinal(ITrialUndoActionProvider<RULE_DATA> provider, ACT action) {
			throw new Error(new BadActionException());
		}

		@Override
		@SuppressWarnings("unused")
		public boolean doTrial(ITrialUndoActionProvider<RULE_DATA> provider, ACT action) {
			throw new Error(new BadActionException());
		}

		@Override
		@SuppressWarnings("unused")
		public void doUndo(ITrialUndoActionProvider<RULE_DATA> provider, ACT action) {
			throw new Error(new BadActionException());
		}
	}

	static public class BadActionException extends Exception {
		private static final long serialVersionUID = 1L;
	}
}
