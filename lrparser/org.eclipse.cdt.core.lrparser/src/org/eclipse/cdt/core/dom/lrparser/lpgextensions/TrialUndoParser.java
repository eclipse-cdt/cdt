/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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

import java.util.LinkedList;

import lpg.lpgjavaruntime.BadParseException;
import lpg.lpgjavaruntime.BadParseSymFileException;
import lpg.lpgjavaruntime.ConfigurationElement;
import lpg.lpgjavaruntime.ConfigurationStack;
import lpg.lpgjavaruntime.Monitor;
import lpg.lpgjavaruntime.NotBacktrackParseTableException;
import lpg.lpgjavaruntime.ParseTable;
import lpg.lpgjavaruntime.TokenStream;

public class TrialUndoParser {
	private Monitor monitor = null;
	private int START_STATE, NUM_RULES, LA_STATE_OFFSET, EOFT_SYMBOL, ERROR_SYMBOL, ACCEPT_ACTION, ERROR_ACTION;

	private TokenStream tokStream;
	private ParseTable prs;
	private ITrialUndoActionProvider actionProvider;
	private boolean skipTokens = false; // true if error productions are used to
										// skip tokens

	private ParserState state;

	/**
	 * Signals that a backtrack was requested by a trial action.
	 */
	private boolean backtrackRequested;

	//
	// Override the getToken function in Stacks.
	//
	public final int getToken(int i) {
		return state.tokens.get(state.parserLocationStack[state.stateStackTop + (i - 1)]);
	}

	public int getTokenOffset() {
		return state.parserLocationStack[state.stateStackTop];
	}

	public final int getCurrentRule() {
		return state.currentAction;
	}

	public final int getFirstToken() {
		return tokStream.getFirstErrorToken(getToken(1));
	}

	public final int getFirstToken(int i) {
		return tokStream.getFirstErrorToken(getToken(i));
	}

	public final int getLastToken() {
		return tokStream.getLastErrorToken(state.lastToken);
	}

	public final int getLastToken(int i) {
		int l = (i >= prs.rhs(state.currentAction) ? state.lastToken
				: state.tokens.get(state.locationStack[state.stateStackTop + i] - 1));
		return tokStream.getLastErrorToken(l);
	}

	public TrialUndoParser(TokenStream tokStream, ParseTable prs, ITrialUndoActionProvider ra)
			throws BadParseSymFileException, NotBacktrackParseTableException {
		this.tokStream = tokStream;
		this.prs = prs;
		this.actionProvider = ra;

		START_STATE = prs.getStartState();
		NUM_RULES = prs.getNumRules();
		LA_STATE_OFFSET = prs.getLaStateOffset();
		EOFT_SYMBOL = prs.getEoftSymbol();
		ERROR_SYMBOL = prs.getErrorSymbol();
		ACCEPT_ACTION = prs.getAcceptAction();
		ERROR_ACTION = prs.getErrorAction();

		if (!prs.isValidForParser())
			throw new BadParseSymFileException();
		if (!prs.getBacktrack())
			throw new NotBacktrackParseTableException();
	}

	public TrialUndoParser(Monitor monitor, TokenStream tokStream, ParseTable prs, ITrialUndoActionProvider ra)
			throws BadParseSymFileException, NotBacktrackParseTableException {
		this(tokStream, prs, ra);
		this.monitor = monitor;
	}

	//
	// Parse without attempting any Error token recovery
	//
	public Object parse() throws BadParseException {
		// without an argument parse() will ignore error productions
		return parse(0);
	}

	//
	// Parse input allowing up to max_error_count Error token recoveries.
	// When max_error_count is 0, no Error token recoveries occur.
	// When max_error is > 0, it limits the number of Error token recoveries.
	// When max_error is < 0, the number of error token recoveries is unlimited.
	// Also, such recoveries only require one token to be parsed beyond the
	// recovery point.
	// (normally two tokens beyond the recovery point must be parsed)
	// Thus, a negative max_error_count should be used when error productions
	// are used to
	// skip tokens.
	//
	public Object parse(int max_error_count) throws BadParseException {
		state = new ParserState(START_STATE, tokStream);
		skipTokens = max_error_count < 0;

		state.pendingCommits = new LinkedList();
		backtrackRequested = false;
		state.trialActionCount = 0;

		// Next "start" token
		state.repair_token = 0;

		int start_token_index = tokStream.peek();
		int start_action_index = 0;

		// Last commit point
		int[] temp_stack = new int[1];
		temp_stack[0] = START_STATE;

		state.reallocateOtherStacks(start_token_index);

		int initial_error_token = backtrackParse(state.repair_token);
		for (int error_token = initial_error_token, count = 0; error_token != 0; error_token = backtrackParse(
				state.repair_token), count++) {
			if (count == max_error_count)
				throw new BadParseException(initial_error_token);
			state.actionCount = start_action_index;
			tokStream.reset(start_token_index);
			state.stateStackTop = temp_stack.length - 1;
			System.arraycopy(temp_stack, 0, state.stateStack, 0, temp_stack.length);
			state.reallocateOtherStacks(start_token_index);

			backtrackParseUpToError(state.repair_token, error_token);

			for (state.stateStackTop = findRecoveryStateIndex(
					state.stateStackTop); state.stateStackTop >= 0; state.stateStackTop = findRecoveryStateIndex(
							state.stateStackTop - 1)) {
				int recovery_token = state.tokens.get(state.locationStack[state.stateStackTop] - 1);
				state.repair_token = errorRepair((recovery_token >= start_token_index ? recovery_token : error_token),
						error_token);
				if (state.repair_token != 0)
					break;
			}

			if (state.stateStackTop < 0)
				throw new BadParseException(initial_error_token);

			temp_stack = new int[state.stateStackTop + 1];
			System.arraycopy(state.stateStack, 0, temp_stack, 0, temp_stack.length);

			start_action_index = state.actionCount;
			start_token_index = tokStream.peek();
		}

		if (state.repair_token != 0)
			state.tokens.add(state.repair_token);
		int t;
		for (t = start_token_index; tokStream.getKind(t) != EOFT_SYMBOL; t = tokStream.getNext(t))
			state.tokens.add(t);
		state.tokens.add(t);

		return null;
	}

	//
	// Process reductions and continue...
	//
	private int process_backtrack_reductions(int act) {
		do {
			state.stateStackTop -= (prs.rhs(act) - 1);
			trialAction(act);
			if (backtrackRequested) {
				backtrackRequested = false;
				return ERROR_ACTION;
			}
			act = prs.ntAction(state.stateStack[state.stateStackTop], prs.lhs(act));
		} while (act <= NUM_RULES);
		return act;
	}

	//
	// Process reductions and continue...
	//
	private int process_repair_reductions(int act) {
		do {
			System.out.println("process_repair_reductions: " + act);
			state.stateStackTop -= (prs.rhs(act) - 1);
			act = prs.ntAction(state.stateStack[state.stateStackTop], prs.lhs(act));
		} while (act <= NUM_RULES);
		return act;
	}

	//
	// Parse the input until either the parse completes successfully or
	// an error is encountered. This function returns an integer that
	// represents the last action that was executed by the parser. If
	// the parse was succesful, then the tuple "action" contains the
	// successful sequence of actions that was executed.
	//
	private int backtrackParse(int initial_token) {
		//
		// Allocate configuration stack.
		//
		state.configurationStack = new ConfigurationStack(prs);
		state.trialActionStack = new LinkedList<>();
		state.trialActionStack.add(Integer.valueOf(state.trialActionCount));

		//
		// Keep parsing until we successfully reach the end of file or
		// an error is encountered. The list of actions executed will
		// be stored in the "action" tuple.
		//
		int error_token = 0;
		int maxStackTop = state.stateStackTop;
		int start_token = tokStream.peek();
		state.curtok = (initial_token > 0 ? initial_token : tokStream.getToken());
		int current_kind = tokStream.getKind(state.curtok);
		state.act = tAction(state.stateStack[state.stateStackTop], current_kind);

		//
		// The main driver loop
		//
		for (;;) {
			//
			// if the parser needs to stop processing,
			// it may do so here.
			//
			if (monitor != null && monitor.isCancelled())
				return 0;

			state.parserLocationStack[state.stateStackTop] = state.curtok;

			if (state.act <= NUM_RULES) {
				state.actionCount++;
				state.stateStackTop--;
				state.act = process_backtrack_reductions(state.act);
			} else if (state.act > ERROR_ACTION) {
				state.actionCount++;
				state.lastToken = state.curtok;
				state.curtok = tokStream.getToken();
				current_kind = tokStream.getKind(state.curtok);
				state.act = process_backtrack_reductions(state.act - ERROR_ACTION);
			} else if (state.act < ACCEPT_ACTION) {
				state.actionCount++;
				state.lastToken = state.curtok;
				state.curtok = tokStream.getToken();
				current_kind = tokStream.getKind(state.curtok);
			}

			if (state.act == ERROR_ACTION) {
				error_token = (error_token > state.curtok ? error_token : state.curtok);

				ConfigurationElement configuration = state.configurationStack.pop();
				if (configuration == null)
					state.act = ERROR_ACTION;
				else {
					boolean shouldPop = prs.baseAction(configuration.conflict_index) == 0;
					undoActions(shouldPop);
					state.actionCount = configuration.action_length;
					state.act = configuration.act;
					state.curtok = configuration.curtok;
					current_kind = tokStream.getKind(state.curtok);
					tokStream.reset(state.curtok == initial_token ? start_token : tokStream.getNext(state.curtok));
					state.stateStackTop = configuration.stack_top;
					configuration.retrieveStack(state.stateStack);
					continue;
				}
				break;
			}
			if (state.act > ACCEPT_ACTION && state.act != ERROR_ACTION) {
				if (state.configurationStack.findConfiguration(state.stateStack, state.stateStackTop, state.curtok))
					state.act = ERROR_ACTION;
				else {
					state.configurationStack.push(state.stateStack, state.stateStackTop, state.act + 1, state.curtok,
							state.actionCount);
					state.trialActionStack.add(Integer.valueOf(state.trialActionCount));
					state.act = prs.baseAction(state.act);
					maxStackTop = state.stateStackTop > maxStackTop ? state.stateStackTop : maxStackTop;
				}
				continue;
			} else if (state.act == ACCEPT_ACTION) {
				break;
			}
			try {
				state.stateStack[++state.stateStackTop] = state.act;
			} catch (IndexOutOfBoundsException e) {
				state.reallocateStateStack();
				state.stateStack[state.stateStackTop] = state.act;
			}

			state.act = tAction(state.act, current_kind);
		}

		// System.out.println("****Number of configurations: " +
		// configuration_stack.configurationSize());
		// System.out.println("****Number of elements in stack tree: " +
		// configuration_stack.numStateElements());
		// System.out.println("****Number of elements in stacks: " +
		// configuration_stack.stacksSize());
		// System.out.println("****Number of actions: " + action.size());
		// System.out.println("****Max Stack Size = " + maxStackTop);
		// System.out.flush();
		return (state.act == ERROR_ACTION ? error_token : 0);
	}

	private void backtrackParseUpToError(int initial_token, int error_token) {
		//
		// Allocate configuration stack.
		//
		state.configurationStack = new ConfigurationStack(prs);
		state.trialActionStack = new LinkedList<>();
		state.trialActionStack.add(Integer.valueOf(state.trialActionCount));

		//
		// Keep parsing until we successfully reach the end of file or
		// an error is encountered. The list of actions executed will
		// be stored in the "action" tuple.
		//

		// tokStream.reset(initial_token);
		int start_token = tokStream.peek();
		state.curtok = (initial_token > 0 ? initial_token : tokStream.getToken());
		int current_kind = tokStream.getKind(state.curtok);
		state.act = tAction(state.stateStack[state.stateStackTop], current_kind);

		state.tokens.add(state.curtok);
		state.locationStack[state.stateStackTop] = state.tokens.size();
		state.actionStack[state.stateStackTop] = state.actionCount;
		state.undoStack[state.stateStackTop] = state.trialActionCount;

		for (;;) {
			//
			// if the parser needs to stop processing,
			// it may do so here.
			//
			if (monitor != null && monitor.isCancelled())
				return;

			state.parserLocationStack[state.stateStackTop] = state.curtok;

			if (state.act <= NUM_RULES) {
				state.actionCount++;
				state.stateStackTop--;
				state.act = process_backtrack_reductions(state.act);
			} else if (state.act > ERROR_ACTION) {
				state.actionCount++;
				state.lastToken = state.curtok;
				state.curtok = tokStream.getToken();
				current_kind = tokStream.getKind(state.curtok);
				state.tokens.add(state.curtok);
				state.act = process_backtrack_reductions(state.act - ERROR_ACTION);
			} else if (state.act < ACCEPT_ACTION) {
				state.actionCount++;
				state.lastToken = state.curtok;
				state.curtok = tokStream.getToken();
				current_kind = tokStream.getKind(state.curtok);
				state.tokens.add(state.curtok);
			} else if (state.act == ERROR_ACTION) {
				if (state.curtok != error_token) {
					ConfigurationElement configuration = state.configurationStack.pop();
					if (configuration == null)
						state.act = ERROR_ACTION;
					else {
						boolean shouldPop = prs.baseAction(configuration.conflict_index) == 0;
						undoActions(shouldPop);
						state.actionCount = configuration.action_length;
						state.act = configuration.act;
						int next_token_index = configuration.curtok;
						state.tokens.reset(next_token_index);
						state.curtok = state.tokens.get(next_token_index - 1);
						current_kind = tokStream.getKind(state.curtok);
						tokStream.reset(state.curtok == initial_token ? start_token : tokStream.getNext(state.curtok));
						state.stateStackTop = configuration.stack_top;
						configuration.retrieveStack(state.stateStack);
						state.locationStack[state.stateStackTop] = state.tokens.size();
						state.actionStack[state.stateStackTop] = state.actionCount;
						state.undoStack[state.stateStackTop] = state.trialActionCount;
						continue;
					}
				}
				break;
			} else if (state.act > ACCEPT_ACTION) {
				if (state.configurationStack.findConfiguration(state.stateStack, state.stateStackTop,
						state.tokens.size()))
					state.act = ERROR_ACTION;
				else {
					state.configurationStack.push(state.stateStack, state.stateStackTop, state.act + 1,
							state.tokens.size(), state.actionCount);
					state.trialActionStack.add(Integer.valueOf(state.trialActionCount));
					state.act = prs.baseAction(state.act);
				}
				continue;
			} else
				break; // assert(act == ACCEPT_ACTION);

			state.stateStack[++state.stateStackTop] = state.act; // no need
																	// to check
																	// if out of
																	// bounds
			state.locationStack[state.stateStackTop] = state.tokens.size();
			state.actionStack[state.stateStackTop] = state.actionCount;
			state.undoStack[state.stateStackTop] = state.trialActionCount;
			state.act = tAction(state.act, current_kind);
		}

		// assert(curtok == error_token);

		return;
	}

	private boolean repairable(int error_token) {
		//
		// Allocate configuration stack.
		//
		ConfigurationStack configuration_stack = new ConfigurationStack(prs);

		//
		// Keep parsing until we successfully reach the end of file or
		// an error is encountered. The list of actions executed will
		// be stored in the "action" tuple.
		//
		int start_token = tokStream.peek();
		int final_token = tokStream.getStreamLength(); // unreachable
		int curtok = 0;
		int current_kind = ERROR_SYMBOL;
		int act = tAction(state.stateStack[state.stateStackTop], current_kind);

		for (;;) {
			if (act <= NUM_RULES) {
				state.stateStackTop--;
				act = process_repair_reductions(act);
			} else if (act > ERROR_ACTION) {
				curtok = tokStream.getToken();
				if (curtok > final_token)
					return true;
				current_kind = tokStream.getKind(curtok);
				act = process_repair_reductions(act - ERROR_ACTION);
			} else if (act < ACCEPT_ACTION) {
				curtok = tokStream.getToken();
				if (curtok > final_token)
					return true;
				current_kind = tokStream.getKind(curtok);
			} else if (act == ERROR_ACTION) {
				ConfigurationElement configuration = configuration_stack.pop();
				if (configuration == null)
					act = ERROR_ACTION;
				else {
					state.stateStackTop = configuration.stack_top;
					configuration.retrieveStack(state.stateStack);
					act = configuration.act;
					curtok = configuration.curtok;
					if (curtok == 0) {
						current_kind = ERROR_SYMBOL;
						tokStream.reset(start_token);
					} else {
						current_kind = tokStream.getKind(curtok);
						tokStream.reset(tokStream.getNext(curtok));
					}
					continue;
				}
				break;
			} else if (act > ACCEPT_ACTION) {
				if (configuration_stack.findConfiguration(state.stateStack, state.stateStackTop, curtok))
					act = ERROR_ACTION;
				else {
					configuration_stack.push(state.stateStack, state.stateStackTop, act + 1, curtok, 0);
					act = prs.baseAction(act);
				}
				continue;
			} else
				break; // assert(act == ACCEPT_ACTION);
			try {
				//
				// We consider a configuration to be acceptable for recovery
				// if we are able to consume enough symbols in the remaining
				// tokens to reach another potential recovery point past the
				// original error token.
				//
				if ((curtok > error_token) && (final_token == tokStream.getStreamLength())) {
					//
					// If the ERROR_SYMBOL is a valid Action Adjunct in the
					// state
					// "act" then we set the terminating token as the successor
					// of
					// the current token. I.e., we have to be able to parse at
					// least
					// two tokens past the re-synch point before we claim
					// victory.
					//
					if (recoverableState(act))
						final_token = skipTokens ? curtok : tokStream.getNext(curtok);
				}

				state.stateStack[++state.stateStackTop] = act;
			} catch (IndexOutOfBoundsException e) {
				state.reallocateStateStack();
				state.stateStack[state.stateStackTop] = act;
			}

			act = tAction(act, current_kind);
		}

		//
		// If we can reach the end of the input successfully, we claim victory.
		//
		return (act == ACCEPT_ACTION);
	}

	private boolean recoverableState(int state) {
		for (int k = prs.asi(state); prs.asr(k) != 0; k++) {
			if (prs.asr(k) == ERROR_SYMBOL)
				return true;
		}
		return false;
	}

	private int findRecoveryStateIndex(int start_index) {
		int i;
		for (i = start_index; i >= 0; i--) {
			//
			// If the ERROR_SYMBOL is an Action Adjunct in state stateStack[i]
			// then chose i as the index of the state to recover on.
			//
			if (recoverableState(state.stateStack[i]))
				break;
		}

		if (i >= 0) // if a recoverable state, remove null reductions, if any.
		{
			int k;
			for (k = i - 1; k >= 0; k--) {
				if (state.locationStack[k] != state.locationStack[i])
					break;
			}
			i = k + 1;
		}

		return i;
	}

	private int errorRepair(int recovery_token, int error_token) {
		int temp_stack[] = new int[state.stateStackTop + 1];
		System.arraycopy(state.stateStack, 0, temp_stack, 0, temp_stack.length);
		for (; tokStream.getKind(recovery_token) != EOFT_SYMBOL; recovery_token = tokStream.getNext(recovery_token)) {
			System.out.println("recovery token: " + tokStream.getKind(recovery_token)); //$NON-NLS-1$
			tokStream.reset(recovery_token);
			if (repairable(error_token))
				break;
			state.stateStackTop = temp_stack.length - 1;
			System.arraycopy(temp_stack, 0, state.stateStack, 0, temp_stack.length);
		}

		if (tokStream.getKind(recovery_token) == EOFT_SYMBOL) {
			tokStream.reset(recovery_token);
			if (!repairable(error_token)) {
				state.stateStackTop = temp_stack.length - 1;
				System.arraycopy(temp_stack, 0, state.stateStack, 0, temp_stack.length);
				return 0;
			}
		}

		state.stateStackTop = temp_stack.length - 1;
		System.arraycopy(temp_stack, 0, state.stateStack, 0, temp_stack.length);

		undoActions(state.undoStack[state.stateStackTop]);
		tokStream.reset(recovery_token);
		state.tokens.reset(state.locationStack[state.stateStackTop] - 1);
		state.actionCount = state.actionStack[state.stateStackTop];
		state.trialActionCount = state.undoStack[state.stateStackTop];

		return tokStream.makeErrorToken(state.tokens.get(state.locationStack[state.stateStackTop] - 1),
				tokStream.getPrevious(recovery_token), error_token, ERROR_SYMBOL);
	}

	private int tAction(int act, int sym) {
		act = prs.tAction(act, sym);
		if (act > LA_STATE_OFFSET) {
			int next_token = tokStream.peek();
			act = prs.lookAhead(act - LA_STATE_OFFSET, tokStream.getKind(next_token));
			while (act > LA_STATE_OFFSET) {
				next_token = tokStream.getNext(next_token);
				act = prs.lookAhead(act - LA_STATE_OFFSET, tokStream.getKind(next_token));
			}
		}
		return act;
	}

	private void trialAction(int action) {
		int start = getTokenOffset();
		int end = getLastToken();
		Rule rule = new Rule(action, start, end);

		actionProvider.setActiveRule(rule);
		boolean saveAction = actionProvider.trialAction(action);
		if (backtrackRequested) {
			return;
		}
		if (saveAction) {
			state.trialActionCount++;
			state.pendingCommits.add(rule);
		}
	}

	/**
	 * Performs the undo actions (in reverse order) for the corresponding trial
	 * actions that have been executed since the last backtrackable point.
	 */
	private void undoActions(boolean shouldPop) {
		int oldTrialActionCount;
		if (state.trialActionStack.size() == 0) {
			oldTrialActionCount = 0;
		} else {
			oldTrialActionCount = state.trialActionStack.getLast();
			if (shouldPop) {
				state.trialActionStack.removeLast();
			}
		}
		safeUndoActions(oldTrialActionCount);

		// needs to be a real checked exception if we ever decide to implement commits
		//assert (state.trialActionCount == 0 && oldTrialActionCount == 0) || oldTrialActionCount != state.trialActionCount : "Went back in time too far";
	}

	private void safeUndoActions(int total) {
		assert total >= 0 : "Tried to go back in time but the door was already shut."; //$NON-NLS-1$
		undoActions(total);
	}

	private void undoActions(int total) {
		while (state.trialActionCount > total) {
			Rule action = ((Rule) state.pendingCommits.removeLast());
			actionProvider.setActiveRule(action);
			actionProvider.undoAction(action.getRuleNumber());
			state.trialActionCount--;
		}
	}

	public void backtrack() {
		backtrackRequested = true;
	}

	public void commit() {
		while (state.pendingCommits.size() > 0) {
			Rule activeRule = (Rule) state.pendingCommits.removeFirst();
			actionProvider.setActiveRule(activeRule);
			actionProvider.finalAction(activeRule.getRuleNumber());
			state.totalCommits++;
		}
	}
}
