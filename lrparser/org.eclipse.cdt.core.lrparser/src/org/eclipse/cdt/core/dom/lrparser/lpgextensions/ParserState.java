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

import java.util.LinkedList;

import lpg.lpgjavaruntime.ConfigurationStack;
import lpg.lpgjavaruntime.IntTuple;
import lpg.lpgjavaruntime.TokenStream;

class ParserState {
	private static final int STACK_INCREMENT = 1024;

	public int lastToken;
	public int currentAction;
	public IntTuple tokens;
	public int actionStack[];
	public int stateStackTop;
	public int[] stateStack;
	public int actionCount;
	public int totalCommits;

	public int[] parserLocationStack;
	public int[] undoStack;
	
	// Error recovery
	public int[] locationStack;
	public int repair_token;

	/**
	 * The number of trial actions that have been executed since the last backtrackable point was encountered.
	 */
	public int trialActionCount;
	
	/**
	 * A stack that contains the number of trial actions that were executed at different backtrackable points.
	 */
	public LinkedList<Integer> trialActionStack;

	/**
	 * Trial actions that have been executed but not yet committed.
	 */
	@SuppressWarnings("rawtypes")
	public LinkedList pendingCommits;

	public ConfigurationStack configurationStack;

	public int act;

	public int curtok;

	public ParserState(int startState, TokenStream tokStream) {
		reallocateStateStack();
		stateStackTop = 0;
		stateStack[0] = startState;

		//
		// The tuple tokens will eventually contain the sequence
		// of tokens that resulted in a successful parse. We leave
		// it up to the "Stream" implementer to define the predecessor
		// of the first token as he sees fit.
		//
		tokStream.reset(); // Position at first token.
		tokens = new IntTuple(tokStream.getStreamLength());
		tokens.add(tokStream.getPrevious(tokStream.peek()));
	}

	public void allocateOtherStacks() {
		locationStack = new int[stateStack.length];
	}

	public void reallocateStateStack() {
		int old_stack_length = (stateStack == null ? 0 : stateStack.length), stack_length = old_stack_length + STACK_INCREMENT;
		if (stateStack == null)
			stateStack = new int[stack_length];
		else
			System.arraycopy(stateStack, 0, stateStack = new int[stack_length], 0, old_stack_length);
		return;
	}

	//
	// Allocate or reallocate all the stacks. Their sizes should always be the
	// same.
	//
	public void reallocateOtherStacks(int start_token_index) {
		// assert(stateStack != null);
		if (this.actionStack == null) {
			this.actionStack = new int[stateStack.length];
			locationStack = new int[stateStack.length];

			actionStack[0] = 0;
			undoStack = new int[stateStack.length];
			
			locationStack[0] = start_token_index;
			
			parserLocationStack = new int[stateStack.length];
			parserLocationStack[0] = start_token_index;
			
			
		} else if (this.actionStack.length < stateStack.length) {
			int old_length = this.actionStack.length;

			System.arraycopy(this.actionStack, 0, this.actionStack = new int[stateStack.length], 0, old_length);
			System.arraycopy(this.undoStack, 0, this.undoStack = new int[stateStack.length], 0, old_length);
			System.arraycopy(locationStack, 0, locationStack = new int[stateStack.length], 0, old_length);
		}
		return;
	}
	
	
	@SuppressWarnings("nls")
	public void dumpState() {
		System.out.print(curtok);
		System.out.print("\t");
		System.out.print(act);
		System.out.print("\t");
		dump(stateStack, stateStackTop);
		System.out.print("\t");
		dump(parserLocationStack, stateStackTop);
		System.out.println();
	}
	
	@SuppressWarnings("nls")
	private void dump(int[] array, int limit) {
		System.out.print("[");
		for (int i = 0; i < limit; i++) {
			if (i > 0) {
				System.out.print(", ");
			}
			System.out.print(array[i]);
		}
		System.out.print("]");
	}
}
