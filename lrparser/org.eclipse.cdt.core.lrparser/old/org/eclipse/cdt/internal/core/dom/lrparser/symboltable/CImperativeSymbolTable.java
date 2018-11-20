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
package org.eclipse.cdt.internal.core.dom.lrparser.symboltable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Used to compute binding resolution during the parse.
 *
 * Imperative style symbol table with destructive update.
 *
 * Consists of two data structures, a hash table for fast lookup
 * of bindings given their names, and a stack used to keep track
 * of scopes.
 *
 *
 * @author Mike Kucera
 */
public class CImperativeSymbolTable {

	private static final int TABLE_SIZE = 256;

	private Bucket[] table = new Bucket[TABLE_SIZE];

	private LinkedList<SymbolScope> scopeStack = new LinkedList<>();

	/**
	 * Represents a scope in the C language.
	 */
	private static class SymbolScope {

		/**
		 * List of buckets that have been modified in the current scope.
		 * When the scope is closed these buckets are popped, returning the
		 * symbol table to the state it was in before the scope was opened.
		 */
		List<Integer> modifiedBuckets = new ArrayList<>();
	}

	/**
	 * A bucket object used to hold elements in the hash table.
	 */
	private static class Bucket {
		String key;
		CNamespace namespace;
		IBinding binding;
		Bucket next;

		Bucket(Bucket next, CNamespace namespace, String key, IBinding binding) {
			this.key = key;
			this.namespace = namespace;
			this.binding = binding;
			this.next = next;
		}
	}

	public CImperativeSymbolTable() {
		openScope(); // open the global scope
		// TODO populate the global scope with built-ins
	}

	/**
	 * Hashes a key into an index in the hash table.
	 */
	private int index(String key) {
		return Math.abs(key.hashCode() % TABLE_SIZE);
	}

	/**
	 * Adds a binding to the symbol table in the current scope.
	 *
	 * @param mask A bit mask used to identify the namespace of the identifier.
	 */
	public void put(CNamespace namespace, String ident, IBinding b) {
		int index = index(ident);
		table[index] = new Bucket(table[index], namespace, ident, b);

		SymbolScope scope = scopeStack.getLast();
		scope.modifiedBuckets.add(index);
	}

	/**
	 * Returns the binding associated with the given identifier, or
	 * null if there is none.
	 *
	 * @param mask A bit mask used to identify the namespace of the identifier.
	 */
	public IBinding get(CNamespace namespace, String ident) {
		Bucket b = table[index(ident)];
		while (b != null) {
			if (namespace == b.namespace && ident.equals(b.key))
				return b.binding;
			b = b.next;
		}
		return null;
	}

	/**
	 * Opens a new inner scope for identifiers.
	 *
	 * If an identifier is added that already exists in an outer scope
	 * then it will be shadowed.
	 */
	public void openScope() {
		scopeStack.add(new SymbolScope());
	}

	/**
	 * Remove all the symbols defined in the scope that is being closed.
	 *
	 * @param scope An IScope object that will be used to represent this scope.
	 * @throws SymbolTableException If the global scope has already been closed or if bindingScope is null.
	 */
	public void closeScope() {
		SymbolScope poppedScope = scopeStack.removeLast(); // pop the scopeStack

		// pop each bucket that was modified in the scope
		for (int index : poppedScope.modifiedBuckets)
			table[index] = table[index].next;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder('[');
		for (Bucket b : table) {
			while (b != null) {
				buff.append('<').append(b.key).append(": ").append(b.binding).append(">, ");
				b = b.next;
			}
		}
		return buff.append(']').toString();
	}
}
