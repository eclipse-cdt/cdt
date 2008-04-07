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

/**
 * The C language has 4 namespaces for identifiers.
 * This enum represents three of them, the "member" namespace 
 * is represented by IStructure.getFields().
 * 
 * The symbol table uses these to mark identifiers and keep
 * the namespaces separate.
 * 
 * @author Mike Kucera
 */
public enum CNamespace {

	GOTO_LABEL, // goto labels
	STRUCT_TAG,// structs, unions, enums
	IDENTIFIER; // all other identifiers

}
