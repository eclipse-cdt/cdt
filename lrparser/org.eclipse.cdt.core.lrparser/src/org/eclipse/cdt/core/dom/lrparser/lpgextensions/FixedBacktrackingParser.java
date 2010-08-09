/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.lpgextensions;

import lpg.lpgjavaruntime.BadParseException;
import lpg.lpgjavaruntime.BadParseSymFileException;
import lpg.lpgjavaruntime.ConfigurationElement;
import lpg.lpgjavaruntime.ConfigurationStack;
import lpg.lpgjavaruntime.IntTuple;
import lpg.lpgjavaruntime.Monitor;
import lpg.lpgjavaruntime.NotBacktrackParseTableException;
import lpg.lpgjavaruntime.ParseTable;
import lpg.lpgjavaruntime.PrsStream;
import lpg.lpgjavaruntime.RuleAction;
import lpg.lpgjavaruntime.Stacks;
import lpg.lpgjavaruntime.TokenStream;

public class FixedBacktrackingParser extends Stacks
{
    private Monitor monitor = null;
    private int START_STATE,
                NUM_RULES,
                LA_STATE_OFFSET,
                EOFT_SYMBOL,
                ERROR_SYMBOL,
                ACCEPT_ACTION,
                ERROR_ACTION;

    private int lastToken,
                currentAction;
    private TokenStream tokStream;
    private ParseTable prs;
    private RuleAction ra;
    private IntTuple action = new IntTuple(1 << 10),
                     tokens;
    private int actionStack[];
    private boolean skipTokens = false;	// true if error productions are used to skip tokens

    //
    // Override the getToken function in Stacks.
    //
    public final int getToken(int i)
    {
        return tokens.get(locationStack[stateStackTop + (i - 1)]);
    }

    public final int getCurrentRule()     { return currentAction; }
    public final int getFirstToken()      { return tokStream.getFirstErrorToken(getToken(1)); }
    public final int getFirstToken(int i) { return tokStream.getFirstErrorToken(getToken(i)); }
    public final int getLastToken()       { return tokStream.getLastErrorToken(lastToken); }
    public final int getLastToken(int i)  { int l = (i >= prs.rhs(currentAction)
                                                        ? lastToken
                                                        : tokens.get(locationStack[stateStackTop + i] - 1));
                                            return tokStream.getLastErrorToken(l);
                                          }

    public FixedBacktrackingParser(TokenStream tokStream, ParseTable prs, RuleAction ra) throws BadParseSymFileException,
                                                                                           NotBacktrackParseTableException
    {
        this.tokStream = (PrsStream) tokStream;
        this.prs = prs;
        this.ra = ra;

        START_STATE = prs.getStartState();
        NUM_RULES = prs.getNumRules();
        LA_STATE_OFFSET = prs.getLaStateOffset();
        EOFT_SYMBOL = prs.getEoftSymbol();
        ERROR_SYMBOL = prs.getErrorSymbol();
        ACCEPT_ACTION = prs.getAcceptAction();
        ERROR_ACTION = prs.getErrorAction();

        if (! prs.isValidForParser()) throw new BadParseSymFileException();
        if (! prs.getBacktrack()) throw new NotBacktrackParseTableException();
    }

    public FixedBacktrackingParser(Monitor monitor, TokenStream tokStream, ParseTable prs, RuleAction ra) throws BadParseSymFileException,
                                                                                                            NotBacktrackParseTableException
    {
        this(tokStream, prs, ra);
        this.monitor = monitor;
    }

    //
    // Allocate or reallocate all the stacks. Their sizes should always be the same.
    //
    public void reallocateOtherStacks(int start_token_index)
    {
        // assert(super.stateStack != null);
        if (this.actionStack == null)
        {
            this.actionStack = new int[super.stateStack.length];
            super.locationStack = new int[super.stateStack.length];
            super.parseStack = new Object[super.stateStack.length];

            actionStack[0] = 0;
            locationStack[0] = start_token_index;
        }
        else if (this.actionStack.length < super.stateStack.length)
        {
            int old_length = this.actionStack.length;

            System.arraycopy(this.actionStack, 0, this.actionStack = new int[super.stateStack.length], 0, old_length);
            System.arraycopy(super.locationStack, 0, super.locationStack = new int[super.stateStack.length], 0, old_length);
            System.arraycopy(super.parseStack, 0, super.parseStack = new Object[super.stateStack.length], 0, old_length);
        }
        return;
    }

    //
    // Parse without attempting any Error token recovery
    //
    public Object parse() throws BadParseException
    {
        // without an argument parse() will ignore error productions
    	return parse(0);
    }

    //
    // Parse input allowing up to max_error_count Error token recoveries.
    // When max_error_count is 0, no Error token recoveries occur.
    // When max_error is > 0, it limits the number of Error token recoveries.
    // When max_error is < 0, the number of error token recoveries is unlimited.
    // Also, such recoveries only require one token to be parsed beyond the recovery point.
    // (normally two tokens beyond the recovery point must be parsed)
    // Thus, a negative max_error_count should be used when error productions are used to 
    // skip tokens.
    //
    public Object parse(int max_error_count) throws BadParseException
    {
        action.reset();
        tokStream.reset(); // Position at first token.
        reallocateStateStack();
        stateStackTop = 0;
        stateStack[0] = START_STATE;
        skipTokens = max_error_count < 0;

        //
        // The tuple tokens will eventually contain the sequence 
        // of tokens that resulted in a successful parse. We leave
        // it up to the "Stream" implementer to define the predecessor
        // of the first token as he sees fit.
        //
        tokens = new IntTuple(tokStream.getStreamLength());
        tokens.add(tokStream.getPrevious(tokStream.peek()));

        int repair_token = 0,
            start_token_index = tokStream.peek(),
            start_action_index = action.size(), // obviously 0
            temp_stack[] = new int[1];
        temp_stack[0] = START_STATE;
        int initial_error_token = backtrackParse(repair_token);
        for (int error_token = initial_error_token, count = 0;
             error_token != 0;
             error_token = backtrackParse(repair_token), count++)
        {
            if (count == max_error_count)
                throw new BadParseException(initial_error_token);
            action.reset(start_action_index);
            tokStream.reset(start_token_index);
            stateStackTop = temp_stack.length - 1;
            System.arraycopy(temp_stack, 0, stateStack, 0, temp_stack.length);
            reallocateOtherStacks(start_token_index);

            backtrackParseUpToError(repair_token, error_token);

            for (stateStackTop = findRecoveryStateIndex(stateStackTop);
                 stateStackTop >= 0;
                 stateStackTop = findRecoveryStateIndex(stateStackTop - 1))
            {
                int recovery_token = tokens.get(locationStack[stateStackTop] - 1);
                repair_token = errorRepair((recovery_token >= start_token_index ? recovery_token : error_token), error_token);
                if (repair_token != 0)
                    break;
            }

            if (stateStackTop < 0)
                throw new BadParseException(initial_error_token);

            temp_stack = new int[stateStackTop + 1];
            System.arraycopy(stateStack, 0, temp_stack, 0, temp_stack.length);

            start_action_index = action.size();
            start_token_index = tokStream.peek();
        }

        if (repair_token != 0)
            tokens.add(repair_token);
        int t;
        for (t = start_token_index; tokStream.getKind(t) != EOFT_SYMBOL; t = tokStream.getNext(t))
            tokens.add(t);
        tokens.add(t);

        return parseActions();
    }

    //
    // Process reductions and continue...
    //
    private final void process_reductions()
    {
        do
        {
            stateStackTop -= (prs.rhs(currentAction) - 1);
            ra.ruleAction(currentAction);
            currentAction = prs.ntAction(stateStack[stateStackTop], prs.lhs(currentAction));
        } while(currentAction <= NUM_RULES);
        return;
    }

    //
    // Now do the final parse of the input based on the actions in
    // the list "action" and the sequence of tokens in list "tokens".
    //
    private Object parseActions() throws BadParseException
    {
        int ti = -1,
            curtok;
        lastToken = tokens.get(++ti);
        curtok = tokens.get(++ti);
        allocateOtherStacks();

        //
        // Reparse the input...
        //
        stateStackTop = -1;
        currentAction = START_STATE;
        for (int i = 0; i < action.size(); i++)
        {
            //
            // if the parser needs to stop processing,
            // it may do so here.
            //
            if (monitor != null && monitor.isCancelled())
                return null;

            stateStack[++stateStackTop] = currentAction;
            locationStack[stateStackTop] = ti;

            currentAction = action.get(i);
            if (currentAction <= NUM_RULES)  // a reduce action?
            {
                stateStackTop--; // make reduction look like shift-reduction
                process_reductions();
            }
            else                   // a shift or shift-reduce action
            {
                lastToken = curtok;
                curtok = tokens.get(++ti);
                if (currentAction > ERROR_ACTION) // a shift-reduce action?
                {
                    currentAction -= ERROR_ACTION;
                    process_reductions();
                }
            }
        }

        return parseStack[0];
    }

    //
    // Process reductions and continue...
    //
    private int process_backtrack_reductions(int act)
    {
        do
        {
            stateStackTop -= (prs.rhs(act) - 1);
            act = prs.ntAction(stateStack[stateStackTop], prs.lhs(act));
        } while(act <= NUM_RULES);

        return act;
    }

    //
    // Parse the input until either the parse completes successfully or
    // an error is encountered. This function returns an integer that
    // represents the last action that was executed by the parser. If
    // the parse was succesful, then the tuple "action" contains the
    // successful sequence of actions that was executed.
    //
    private int backtrackParse(int initial_token)
    {
        //
        // Allocate configuration stack.
        //
        ConfigurationStack configuration_stack = new ConfigurationStack(prs);

        //
        // Keep parsing until we successfully reach the end of file or
        // an error is encountered. The list of actions executed will
        // be stored in the "action" tuple.
        //
        int error_token = 0,
            maxStackTop = stateStackTop,
            start_token = tokStream.peek(),
            curtok = (initial_token > 0 ? initial_token : tokStream.getToken()),
            current_kind = tokStream.getKind(curtok),
            act = tAction(stateStack[stateStackTop], current_kind);

        int count = 0;

        //
        // The main driver loop
        //
        for (;;)
        {
        	count++;
            //
            // if the parser needs to stop processing,
            // it may do so here.
            //
            if (monitor != null && monitor.isCancelled())
                return 0;

            if (act <= NUM_RULES)
            {
                action.add(act); // save this reduce action
                stateStackTop--;
                act = process_backtrack_reductions(act);
            }
            else if (act > ERROR_ACTION)
            {
                action.add(act);     // save this shift-reduce action
                curtok = tokStream.getToken();
                current_kind = tokStream.getKind(curtok);
                act = process_backtrack_reductions(act - ERROR_ACTION);
            }
            else if (act < ACCEPT_ACTION)
            {
                action.add(act);    // save this shift action
                curtok = tokStream.getToken();
                current_kind = tokStream.getKind(curtok);
            }
            else if (act == ERROR_ACTION)
            {
                error_token = (error_token > curtok ? error_token : curtok);

                ConfigurationElement configuration = configuration_stack.pop();
                if (configuration == null)
                     act = ERROR_ACTION;
                else
                {
                    action.reset(configuration.action_length);
                    act = configuration.act;
                    curtok = configuration.curtok;
                    current_kind = tokStream.getKind(curtok);
                    tokStream.reset(curtok == initial_token
                                            ? start_token
                                            : tokStream.getNext(curtok));
                    stateStackTop = configuration.stack_top;
                    configuration.retrieveStack(stateStack);
                    continue;
                }
                break;
            }
            else if (act > ACCEPT_ACTION)
            {
                if (configuration_stack.findConfiguration(stateStack, stateStackTop, curtok))
                    act = ERROR_ACTION;
                else
                {
                    configuration_stack.push(stateStack, stateStackTop, act + 1, curtok, action.size());
                    act = prs.baseAction(act);
                    maxStackTop = stateStackTop > maxStackTop ? stateStackTop : maxStackTop;
                }
                continue;
            }
            else break; // assert(act == ACCEPT_ACTION);
            try
            {
                stateStack[++stateStackTop] = act;
            }
            catch(IndexOutOfBoundsException e)
            {
                reallocateStateStack();
                stateStack[stateStackTop] = act;
            }

            act = tAction(act, current_kind);
        }

        //System.out.println("****Number of configurations: " + configuration_stack.configurationSize());
        //System.out.println("****Number of elements in stack tree: " + configuration_stack.numStateElements());
        //System.out.println("****Number of elements in stacks: " + configuration_stack.stacksSize());
        //System.out.println("****Number of actions: " + action.size());
        //System.out.println("****Max Stack Size = " + maxStackTop);
        //System.out.flush();
        System.out.println("The backtrace parser count is: " + count );

        return (act == ERROR_ACTION ? error_token : 0);
    }

    private void backtrackParseUpToError(int initial_token, int error_token)
    {
        //
        // Allocate configuration stack.
        //
        ConfigurationStack configuration_stack = new ConfigurationStack(prs);

        //
        // Keep parsing until we successfully reach the end of file or
        // an error is encountered. The list of actions executed will
        // be stored in the "action" tuple.
        //
        int start_token = tokStream.peek(),
            curtok = (initial_token > 0 ? initial_token : tokStream.getToken()),
            current_kind = tokStream.getKind(curtok),
            act = tAction(stateStack[stateStackTop], current_kind);

        tokens.add(curtok);
        locationStack[stateStackTop] = tokens.size();
        actionStack[stateStackTop] = action.size();

        for (;;)
        {
            //
            // if the parser needs to stop processing,
            // it may do so here.
            //
            if (monitor != null && monitor.isCancelled())
                return;

            if (act <= NUM_RULES)
            {
                action.add(act); // save this reduce action
                stateStackTop--;
                act = process_backtrack_reductions(act);
            }
            else if (act > ERROR_ACTION)
            {
                action.add(act);     // save this shift-reduce action
                curtok = tokStream.getToken();
                current_kind = tokStream.getKind(curtok);
                tokens.add(curtok);
                act = process_backtrack_reductions(act - ERROR_ACTION);
            }
            else if (act < ACCEPT_ACTION)
            {
                action.add(act);     // save this shift action
                curtok = tokStream.getToken();
                current_kind = tokStream.getKind(curtok);
                tokens.add(curtok);
            }
            else if (act == ERROR_ACTION)
            {
                if (curtok != error_token)
                {
                    ConfigurationElement configuration = configuration_stack.pop();
                    if (configuration == null)
                        act = ERROR_ACTION;
                    else
                    {
                        action.reset(configuration.action_length);
                        act = configuration.act;
                        int next_token_index = configuration.curtok;
                        tokens.reset(next_token_index);
                        curtok = tokens.get(next_token_index - 1);
                        current_kind = tokStream.getKind(curtok);
                        tokStream.reset(curtok == initial_token
                                                ? start_token
                                                : tokStream.getNext(curtok));
                        stateStackTop = configuration.stack_top;
                        configuration.retrieveStack(stateStack);
                        locationStack[stateStackTop] = tokens.size();
                        actionStack[stateStackTop] = action.size();
                        continue;
                    }
                }
                break;
            }
            else if (act > ACCEPT_ACTION)
            {
                if (configuration_stack.findConfiguration(stateStack, stateStackTop, tokens.size()))
                    act = ERROR_ACTION;
                else
                {
                    configuration_stack.push(stateStack, stateStackTop, act + 1, tokens.size(), action.size());
                    act = prs.baseAction(act);
                }
                continue;
            }
            else break; // assert(act == ACCEPT_ACTION);

            stateStack[++stateStackTop] = act; // no need to check if out of bounds
            locationStack[stateStackTop] = tokens.size();
            actionStack[stateStackTop] = action.size();
            act = tAction(act, current_kind);
        }

        // assert(curtok == error_token);

        return;
    }

    private boolean repairable(int error_token)
    {
        //
        // Allocate configuration stack.
        //
        ConfigurationStack configuration_stack = new ConfigurationStack(prs);

        //
        // Keep parsing until we successfully reach the end of file or
        // an error is encountered. The list of actions executed will
        // be stored in the "action" tuple.
        //
        int start_token = tokStream.peek(),
            final_token = tokStream.getStreamLength(), // unreachable
            curtok = 0,
            current_kind = ERROR_SYMBOL,
            act = tAction(stateStack[stateStackTop], current_kind);

        for (;;)
        {
            if (act <= NUM_RULES)
            {
                stateStackTop--;
                act = process_backtrack_reductions(act);
            }
            else if (act > ERROR_ACTION)
            {
                curtok = tokStream.getToken();
                if (curtok > final_token) return true;
                current_kind = tokStream.getKind(curtok);
                act = process_backtrack_reductions(act - ERROR_ACTION);
            }
            else if (act < ACCEPT_ACTION)
            {
                curtok = tokStream.getToken();
                if (curtok > final_token) return true;
                current_kind = tokStream.getKind(curtok);
            }
            else if (act == ERROR_ACTION)
            {
                ConfigurationElement configuration = configuration_stack.pop();
                if (configuration == null)
                     act = ERROR_ACTION;
                else
                {
                    stateStackTop = configuration.stack_top;
                    configuration.retrieveStack(stateStack);
                    act = configuration.act;
                    curtok = configuration.curtok;
                    if (curtok == 0)
                    {
                        current_kind = ERROR_SYMBOL;
                        tokStream.reset(start_token);
                    }
                    else
                    {
                        current_kind = tokStream.getKind(curtok);
                        tokStream.reset(tokStream.getNext(curtok));
                    }
                    continue;
                }
                break;
            }
            else if (act > ACCEPT_ACTION)
            {
                if (configuration_stack.findConfiguration(stateStack, stateStackTop, curtok))
                    act = ERROR_ACTION;
                else
                {
                    configuration_stack.push(stateStack, stateStackTop, act + 1, curtok, 0);
                    act = prs.baseAction(act);
                }
                continue;
            }
            else break; // assert(act == ACCEPT_ACTION);
            try
            {
                //
                // We consider a configuration to be acceptable for recovery
                // if we are able to consume enough symbols in the remainining
                // tokens to reach another potential recovery point past the
                // original error token.
                //
                if ((curtok > error_token) && (final_token == tokStream.getStreamLength()))
                {
                    //
                    // If the ERROR_SYMBOL is a valid Action Adjunct in the state
                    // "act" then we set the terminating token as the successor of
                    // the current token. I.e., we have to be able to parse at least
                    // two tokens past the resynch point before we claim victory.
                    //
                    if (recoverableState(act))
                        final_token = skipTokens ? curtok : tokStream.getNext(curtok);
                }

                stateStack[++stateStackTop] = act;
            }
            catch(IndexOutOfBoundsException e)
            {
                reallocateStateStack();
                stateStack[stateStackTop] = act;
            }

            act = tAction(act, current_kind);
        }

        //
        // If we can reach the end of the input successfully, we claim victory.
        //
        return (act == ACCEPT_ACTION);
    }

    private boolean recoverableState(int state)
    {
        for (int k = prs.asi(state); prs.asr(k) != 0; k++)
        {
           if (prs.asr(k) == ERROR_SYMBOL)
                return true;
        }
        return false;
    }

    private int findRecoveryStateIndex(int start_index)
    {
        int i;
        for (i = start_index; i >= 0; i--)
        {
            //
            // If the ERROR_SYMBOL is an Action Adjunct in state stateStack[i]
            // then chose i as the index of the state to recover on.
            //
            if (recoverableState(stateStack[i]))
                break;
        }

        if (i >= 0) // if a recoverable state, remove null reductions, if any.
        {
            int k;
            for (k = i - 1; k >= 0; k--)
            {
                if (locationStack[k] != locationStack[i])
                    break;
            }
            i = k + 1;
        }

        return i;
    }

    private int errorRepair(int recovery_token, int error_token)
    {
        int temp_stack[] = new int[stateStackTop + 1];
        System.arraycopy(stateStack, 0, temp_stack, 0, temp_stack.length);
        for (;
             tokStream.getKind(recovery_token) != EOFT_SYMBOL;
             recovery_token = tokStream.getNext(recovery_token))
        {
            tokStream.reset(recovery_token);
            if (repairable(error_token))
                break;
            stateStackTop = temp_stack.length - 1;
            System.arraycopy(temp_stack, 0, stateStack, 0, temp_stack.length);
        }

        if (tokStream.getKind(recovery_token) == EOFT_SYMBOL)
        {
            tokStream.reset(recovery_token);
            if (! repairable(error_token))
            {
                stateStackTop = temp_stack.length - 1;
                System.arraycopy(temp_stack, 0, stateStack, 0, temp_stack.length);
                return 0;
            }
        }

        //
        //
        //
        stateStackTop = temp_stack.length - 1;
        System.arraycopy(temp_stack, 0, stateStack, 0, temp_stack.length);
        tokStream.reset(recovery_token);
        tokens.reset(locationStack[stateStackTop] - 1);
        action.reset(actionStack[stateStackTop]);

        return tokStream.makeErrorToken(tokens.get(locationStack[stateStackTop] - 1),
                                        tokStream.getPrevious(recovery_token),
                                        error_token,
                                        ERROR_SYMBOL);
    }

    private int tAction(int act, int sym)
    {
        act = prs.tAction(act, sym);
        if (act > LA_STATE_OFFSET)
        {
            int next_token = tokStream.peek();
            act = prs.lookAhead(act - LA_STATE_OFFSET, tokStream.getKind(next_token));
            while(act > LA_STATE_OFFSET)
            {
                next_token = tokStream.getNext(next_token);
                act = prs.lookAhead(act - LA_STATE_OFFSET, tokStream.getKind(next_token));
            }
        }
        return act;
    }
}
