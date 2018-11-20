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
	STRUCT_TAG, // structs, unions, enums
	IDENTIFIER; // all other identifiers

}
