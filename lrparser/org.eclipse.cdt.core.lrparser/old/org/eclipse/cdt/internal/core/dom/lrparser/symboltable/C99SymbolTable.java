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
package org.eclipse.cdt.internal.core.dom.lrparser.symboltable;


import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.IC99Binding;

/**
 * A facade for a FunctionalMap to make it behave like 
 * a symbol table for C99.
 * 
 * In particular we need to be able to lookup identifiers based both
 * on the String representation of the identifier and its "namespace".
 * 
 * @author Mike Kucera
 */
public class C99SymbolTable {
	
	/**
	 * Adapter objects are used as the keys. The trick here is to implement
	 * compareTo() in such a way that identifiers are separated by their namespace.
	 */
	private static class Key implements Comparable<Key> {
		private final String ident;
		private final CNamespace namespace;
		
		public Key(CNamespace namespace, String ident) {
			if(namespace == null || ident == null)
				throw new NullPointerException();
			
			this.ident = ident;
			this.namespace = namespace;
		}

		@Override
		public int compareTo(Key x) {
			// this separates namespaces in the symbol table
			int c = namespace.compareTo(x.namespace);
			// only if the namespace is the same do we check the identifier
			return (c == 0) ? ident.compareTo(x.ident) : c;
		}
		
		@Override public String toString() {
			return ident + "::" + namespace;//$NON-NLS-1$
		}
	}
	
	/**
	 * Start with EMPTY_TABLE and build up a symbol table using insert().
	 */
	public static final C99SymbolTable EMPTY_TABLE = new C99SymbolTable();
	
	
	// the map we are providing a facade for
	private final FunctionalMap<Key,IC99Binding> map;
	
	
	/**
	 * Constructors are private, start with EMPTY_TABLE 
	 * and build it up using insert().
	 */
	private C99SymbolTable() {
		map = FunctionalMap.emptyMap();
	}
	
	private C99SymbolTable(FunctionalMap<Key,IC99Binding> newRoot) {
		map = newRoot;
	}
	
	/**
	 * Returns a new symbol table that contains the given mapping.
	 * @throws NullPointerException if the namespace or key is null.
	 */
	public C99SymbolTable insert(CNamespace ns, String key, IC99Binding binding) {
		return new C99SymbolTable(map.insert(new Key(ns, key), binding));
	}

	/**
	 * Looks up the binding given its namespace and identifier.
	 * @return null If there is no binding corresponding to the key.
	 * @throws NullPointerException if the namespace or key is null.
	 */
	public IC99Binding lookup(CNamespace ns, String key) {
		return map.lookup(new Key(ns, key));
	}
	
	public int size() {
		return map.size();
	}
	
	public boolean isEmpty() {
		return map.size() == 0;
	}
	
	@Override public String toString() {
		return map.toString();
	}
	
//	void printStructure() {
//		map.printStructure();
//	}
	
}
