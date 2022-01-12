/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for all the names in the index. These constitute either a declaration or a reference.
 *
 * @since 4.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIndexName extends IName {
	IIndexName[] EMPTY_ARRAY = {};

	/**
	 * Returns the file the name belongs to.
	 * @throws CoreException
	 */
	public IIndexFile getFile() throws CoreException;

	/**
	 * Returns the character offset of the location of the name.
	 */
	public int getNodeOffset();

	/**
	 * Returns the length of the name.
	 */
	public int getNodeLength();

	/**
	 * Returns the name of the definition that contains this name. May return {@code null}.
	 * Currently this is implemented for function and method definitions, only.
	 */
	public IIndexName getEnclosingDefinition() throws CoreException;

	/**
	 * Returns the names of the references contained in this definition.
	 * Returns {@code null}, if the name is not a definition.
	 *
	 * Currently the method works with function definitions, only.
	 */
	public IIndexName[] getEnclosedNames() throws CoreException;

	/**
	 * Returns whether a declaration is a base-class specifier.
	 */
	public boolean isBaseSpecifier() throws CoreException;

	/**
	 * Returns whether this name potentially denotes a polymorphic method call. This is the case
	 * when the name is not qualified and denotes a method call and the method is accessed via a
	 * pointer or a reference to an object.
	 * <p>
	 * No checks are performed whether the method is actually virtual or not.
	 */
	public boolean couldBePolymorphicMethodCall() throws CoreException;

	/**
	 * Returns whether this name is a potential match for its binding, rather than an exact match.
	 * An example of a potential match might be a function definition that does match a
	 * declaration exactly in signature.
	 * @since 6.5
	 */
	public boolean isPotentialMatch() throws CoreException;

	/**
	 * Returns whether this name specifies an inline namespace.
	 * @since 5.3
	 */
	public boolean isInlineNamespaceDefinition() throws CoreException;

	/**
	 * Returns whether this name is a read-reference to a variable or field.
	 * The notion of a read-reference may not strictly reflect what your compiler generates,
	 * heuristics may be used.
	 * <p>
	 * For pointers and arrays the access to the pointer itself is tracked, rather than considering
	 * the target of the pointer.
	 */
	public boolean isReadAccess() throws CoreException;

	/**
	 * Returns whether this name is a write-reference to a variable or field.
	 * The notion of a write-reference may not strictly reflect what your compiler generates,
	 * heuristics may be used.
	 * <p>
	 * For pointers and arrays the access to the pointer itself is tracked, rather than considering
	 * the target of the pointer.
	 */
	public boolean isWriteAccess() throws CoreException;
}
