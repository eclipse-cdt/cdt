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
package org.eclipse.cdt.core.dom.lrparser.action.c99;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.IC99Binding;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.IC99Scope;


/**
 * Used to compute binding resolution during the parse.
 * 
 * Imperative style symbol table with destructive update.
 * 
 * Consists of two data structures, a hash table for fast lookup
 * of bindings given their names, and a stack used to keep track
 * of scopes.
 * 
 * @deprecated Use FunctionalSymbolTable now that undo actions are needed
 * 
 * @author Mike Kucera
 */
@Deprecated public class ImperativeSymbolTable {
	
	private static final int TABLE_SIZE = 256;
	
	private Bucket[] table = new Bucket[TABLE_SIZE];
	
	private LinkedList<SymbolScope> scopeStack = new LinkedList<SymbolScope>();
	
	
	
	/**
	 * Represents a scope in the C language.
	 */
	private static class SymbolScope {
		
		/** 
		 * List of buckets that have been modified in the current scope.
		 * When the scope is closed these buckets are popped, returning the 
		 * symbol table to the state it was in before the scope was opened.
		 */
		List<Integer> modifiedBuckets = new ArrayList<Integer>();
		
		/**
		 * List of inner scopes that have been closed.
		 */
		List<IC99Scope> innerScopes = new ArrayList<IC99Scope>();
	}
	
	
	/**
	 * A bucket object used to hold elements in the hash table.
	 */
	private static class Bucket {
		String key;
		CNamespace namespace;
		IC99Binding binding;
		Bucket next;
		
		Bucket(Bucket next, CNamespace namespace, String key, IC99Binding binding) {
			this.key = key;
			this.namespace = namespace;
			this.binding = binding;
			this.next = next;
		}
	}
	
	
	public ImperativeSymbolTable() {
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
	public void put(CNamespace namespace, String ident, IC99Binding b) {		
		int index = index(ident);
		table[index] = new Bucket(table[index], namespace, ident, b);
		
		SymbolScope scope = scopeStack.getLast();
		scope.modifiedBuckets.add(index);
	}
	
	
	/**
	 * Special version of put that adds the binding to the scope that contains
	 * the current scope. 
	 * 
	 * This is here because the scope for a function body is opened before
	 * the function binding is created.
	 */
	public void putInOuterScope(CNamespace namespace, String ident, IC99Binding b) {
		LinkedList<Bucket> poppedBindings = new LinkedList<Bucket>();
		SymbolScope scope = scopeStack.removeLast();
		
		for(int index : scope.modifiedBuckets) {
			Bucket bucket = table[index];
			poppedBindings.add(bucket);
			table[index] = bucket.next;
		}
		
		put(namespace, ident, b);
		
		for(int index : scope.modifiedBuckets) {
			Bucket bucket = poppedBindings.removeFirst();
			bucket.next = table[index];
			table[index] = bucket;
		}
		
		scopeStack.add(scope);
	}
	

	/**
	 * Returns the binding associated with the given identifier, or
	 * null if there is none.
	 * 
	 * @param mask A bit mask used to identify the namespace of the identifier.
	 */
	public IC99Binding get(CNamespace namespace, String ident) {
		Bucket b = table[index(ident)];
		while(b != null) {
			if(namespace == b.namespace && ident.equals(b.key))
				return b.binding;
			b = b.next;
		}
		return null;
	}
	

	List<IC99Scope> getInnerScopes() {
		return scopeStack.getLast().innerScopes;
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
	public void closeScope(IC99Scope bindingScope) {		
		SymbolScope poppedScope = scopeStack.removeLast(); // pop the scopeStack
		
		for(IC99Scope innerScope : poppedScope.innerScopes) {
			innerScope.setParent(bindingScope);
		}
		
		if(!scopeStack.isEmpty()) { // would be empty if the global scope was popped
			SymbolScope outerScope = scopeStack.getLast();
			outerScope.innerScopes.add(bindingScope);
		}
			
		// pop each bucket that was modified in the scope
		for(int index : poppedScope.modifiedBuckets) {
			Bucket bucket = table[index];
			bucket.binding.setScope(bindingScope);
			table[index] = bucket.next;
		}
	}
	
	
	public String toString() {
		StringBuilder buff = new StringBuilder("[");
		for(Bucket b : table) {
			while(b != null) {
				buff.append("<").append(b.key).append(": ").append(b.binding).append(">, ");
				b = b.next;
			}
		}
		return buff.append("]").toString();
	}
}
