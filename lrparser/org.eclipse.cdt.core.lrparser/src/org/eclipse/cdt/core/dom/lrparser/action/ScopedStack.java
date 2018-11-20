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

package org.eclipse.cdt.core.dom.lrparser.action;

import static org.eclipse.cdt.core.parser.util.CollectionUtils.reverseIterable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A stack that can be "marked", that is the stack can be divided
 * into chunks that can be conveniently processed. There is always at
 * least one open scope.
 *
 *
 * This stack was designed to be used to store AST nodes while
 * the AST is built during the parse, however it is useful for other
 * purposes as well.
 *
 * Some grammar rules have arbitrary length lists on the right side.
 * For example the rule for compound statements (where block_item_list is any
 * number of statements or declarations):
 *
 * compound-statement ::= '{' <openscope-ast> block_item_list '}'
 *
 * There is a problem when trying to build the AST node for the compound statement...
 * you don't know how many block_items are contained in the compound statement, so
 * you don't know how many times to pop the AST stack.
 *
 * One inelegant solution is to count the block-items as they are parsed. This
 * is inelegant because nested compound-statements are allowed so you would
 * have to maintain several counts at the same time.
 *
 * Another solution would be to build the list of block-items as part of the
 * block_item_list rule, but just using this stack is simpler.
 *
 * This class can be used as an AST stack that is implemented as a stack of "AST Scopes".
 * There is a special grammar rule <openscope-ast> that creates a new AST Scope.
 * So, in order to consume all the block_items, all that has to be done is
 * iterate over the topmost scope and then close it when done.
 *
 *
 * @author Mike Kucera
 */
public class ScopedStack<T> {

	private LinkedList<T> topScope;

	// A stack of stacks, used to implement scoping
	private final LinkedList<LinkedList<T>> scopeStack;

	/**
	 * Creates a new ScopedStack with the first scope already open.
	 */
	public ScopedStack() {
		topScope = new LinkedList<>();
		scopeStack = new LinkedList<>();
	}

	/**
	 * Opens a new scope.
	 */
	public void openScope() {
		scopeStack.add(topScope);
		topScope = new LinkedList<>();
	}

	/**
	 * Opens a scope then pushes all the items in the given list.
	 *
	 * @throws NullPointerException if items is null
	 */
	public void openScope(Collection<T> items) {
		openScope();
		for (T item : items)
			push(item);
	}

	/**
	 * Marks the stack then pushes all the items in the given array.
	 *
	 * @throws NullPointerException if items is null
	 */
	public void openScope(T[] items) {
		// looks the same as above but compiles into different bytecode
		openScope();
		for (T item : items)
			push(item);
	}

	/**
	 * Pops all the items in the topmost scope.
	 * The outermost scope cannot be closed.
	 *
	 * @throws NoSuchElementException If the outermost scope is closed.
	 */
	public List<T> closeScope() {
		if (scopeStack.isEmpty())
			throw new NoSuchElementException("cannot close outermost scope"); //$NON-NLS-1$

		List<T> top = topScope;
		topScope = scopeStack.removeLast();
		return top;
	}

	/**
	 * Pushes an item onto the topmost scope.
	 */
	public void push(T o) {
		topScope.add(o);
	}

	/**
	 * @throws NoSuchElementException if the topmost scope is empty
	 */
	public T pop() {
		return topScope.removeLast();
	}

	/**
	 * @throws NoSuchElementException if the topmost scope is empty
	 */
	public T peek() {
		return topScope.getLast();
	}

	/**
	 * Returns the entire top scope as a List.
	 */
	public List<T> topScope() {
		return topScope;
	}

	/**
	 * Returns the next outermost scope.
	 * @throws NoSuchElementException if size() < 2
	 */
	public List<T> outerScope() {
		return scopeStack.getLast();
	}

	public boolean isEmpty() {
		return topScope.isEmpty() && scopeStack.isEmpty();
	}

	/**
	 * Why oh why does java not have reverse iterators?????
	 */
	public void print() {
		final String separator = "----------"; //$NON-NLS-1$
		System.out.println();
		System.out.println('-');

		printScope(topScope);
		System.out.println(separator);

		for (List<T> list : reverseIterable(scopeStack)) {
			printScope(list);
		}

		System.out.println();
	}

	private void printScope(List<T> scope) {
		for (T t : reverseIterable(scope)) {
			System.out.println(t);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (List<T> scope : scopeStack)
			appendScopeContents(sb, scope);
		appendScopeContents(sb, topScope);
		return sb.toString();
	}

	private void appendScopeContents(StringBuilder sb, List<T> scope) {
		sb.append('[');
		boolean first = true;
		for (T t : scope) {
			if (first)
				first = false;
			else
				sb.append(',');
			sb.append(t);
		}
		sb.append(']');
	}

}
