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

/**
 * A facade for a FunctionalMap that is used just to track typedef
 * declarations.
 *
 * This class acts like a set. No information needs to be associated
 * with a typedef declaration, all we need to know is if the identifier
 * has been declared as a typedef.
 *
 * @author Mike Kucera
 */
public class TypedefSymbolTable {

	/**
	 * Start with EMPTY_TABLE and build up a symbol table using add().
	 */
	public static final TypedefSymbolTable EMPTY_TABLE = new TypedefSymbolTable();

	// the map we are providing a facade for
	private final FunctionalMap<String, Object> map;

	/**
	 * Constructors are private, start with EMPTY_TABLE
	 * and build it up using insert().
	 */
	private TypedefSymbolTable() {
		map = FunctionalMap.emptyMap();
	}

	private TypedefSymbolTable(FunctionalMap<String, Object> newRoot) {
		map = newRoot;
	}

	public TypedefSymbolTable add(String typedefIdent) {
		return new TypedefSymbolTable(map.insert(typedefIdent, null));
	}

	public boolean contains(String typedef) {
		return map.containsKey(typedef);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.size() == 0;
	}

	@Override
	public String toString() {
		return map.toString();
	}

}
