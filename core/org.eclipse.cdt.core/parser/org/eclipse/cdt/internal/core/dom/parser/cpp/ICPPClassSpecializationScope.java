/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

/**
 * Composite scope of a class specialization. Supports creating instances for bindings found
 * in the scope of the specialized class template.
 *
 * @since 5.0
 */
public interface ICPPClassSpecializationScope extends ICPPClassScope {
	/**
	 * Returns the class that was specialized to get this scope.
	 */
	ICPPClassType getOriginalClassType();

	/**
	 * The specialized class.
	 */
	ICPPClassSpecialization getClassType();

	/**
	 * Computes the bases via the original class.
	 */
	ICPPBase[] getBases() throws DOMException;

	/**
	 * Computes the methods via the original class.
	 */
	ICPPMethod[] getDeclaredMethods() throws DOMException;

	/**
	 * Computes the fields via the original class.
	 */
	ICPPField[] getDeclaredFields() throws DOMException;

	/**
	 * Computes the friends via the original class.
	 */
	IBinding[] getFriends() throws DOMException;

	/**
	 * Computes the nested classes via the original class.
	 */
	ICPPClassType[] getNestedClasses() throws DOMException;
}
