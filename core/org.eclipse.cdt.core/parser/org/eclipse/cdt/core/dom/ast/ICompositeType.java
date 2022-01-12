/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for all composite types: classes, structs and unions.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICompositeType extends IBinding, IType {
	public static final int k_struct = IASTCompositeTypeSpecifier.k_struct;
	public static final int k_union = IASTCompositeTypeSpecifier.k_union;

	/**
	 * Returns the type of the composite, {@link #k_struct}, {@link #k_union},
	 * or {@link org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#k_class}.
	 */
	public int getKey();

	/**
	 * Returns whether the type is anonymous or not. A type for which objects or
	 * pointers are declared is not considered an anonymous type.
	 * <pre>
	 * struct Outer {
	 *    struct {int a;}; // anonymous
	 *    struct {int b;} c; // not anonymous
	 * }
	 * </pre>
	 * @since 5.1
	 */
	boolean isAnonymous();

	/**
	 * Returns the fields for this type.
	 *
	 * @return List of IField
	 */
	public IField[] getFields();

	/**
	 * Returns the field that matches name, or <code>null</code> if there is no such field.
	 *
	 * @param name
	 */
	public IField findField(String name);

	/**
	 * Returns the IScope object that is associated with this composite type.
	 */
	public IScope getCompositeScope();
}
