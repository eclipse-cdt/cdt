/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import static org.eclipse.cdt.core.parser.tests.VisibilityAsserts.assertVisibility;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.core.runtime.CoreException;

import junit.framework.Test;

/**
 * @author Thomas Corbat
 *
 * Tests for ensuring the PDOM contains the correct visibility information for class members.
 */
public class ClassMemberVisibilityTests extends PDOMInlineCodeTestBase {

	public static Test suite() {
		return suite(ClassMemberVisibilityTests.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		setUpSections(1);
	}

	// class A {
	//   void defaultMemFun();
	// public:
	//   void publicMemFun();
	// protected:
	//   void protectedMemFun();
	// private:
	//   void privateMemFun();
	// };
	public void testVisibilityDefaultMemberFunction() throws Exception {
		IBinding[] defaultFunction = findQualifiedPossiblyImplicit(pdom, "A::defaultMemFun");
		assertVisibility(ICPPClassType.v_private, getMemberVisibility(defaultFunction[0]));

		IBinding[] publicFunction = findQualifiedPossiblyImplicit(pdom, "A::publicMemFun");
		assertVisibility(ICPPClassType.v_public, getMemberVisibility(publicFunction[0]));

		IBinding[] protectedFunction = findQualifiedPossiblyImplicit(pdom, "A::protectedMemFun");
		assertVisibility(ICPPClassType.v_protected, getMemberVisibility(protectedFunction[0]));

		IBinding[] privateFunction = findQualifiedPossiblyImplicit(pdom, "A::privateMemFun");
		assertVisibility(ICPPClassType.v_private, getMemberVisibility(privateFunction[0]));
	}

	// class A {
	//   int defaultVariable;
	// public:
	//   int publicVariable;
	// protected:
	//   int protectedVariable;
	// private:
	//   int privateVariable;
	// };
	public void testVisibilityDefaultMemberVariable() throws Exception {
		IBinding[] defaultVariable = findQualifiedPossiblyImplicit(pdom, "A::defaultVariable");
		assertVisibility(ICPPClassType.v_private, getMemberVisibility(defaultVariable[0]));

		IBinding[] publicVariable = findQualifiedPossiblyImplicit(pdom, "A::publicVariable");
		assertVisibility(ICPPClassType.v_public, getMemberVisibility(publicVariable[0]));

		IBinding[] protectedVariable = findQualifiedPossiblyImplicit(pdom, "A::protectedVariable");
		assertVisibility(ICPPClassType.v_protected, getMemberVisibility(protectedVariable[0]));

		IBinding[] privateVariable = findQualifiedPossiblyImplicit(pdom, "A::privateVariable");
		assertVisibility(ICPPClassType.v_private, getMemberVisibility(privateVariable[0]));
	}

	// class A {
	//   class DefaultNested {};
	// public:
	//   class PublicNested {};
	// protected:
	//   class ProtectedNested {};
	// private:
	//   class PrivateNested {};
	// };
	public void testVisibilityDefaultNestedClass() throws Exception {
		IBinding[] defaultNested = findQualifiedPossiblyImplicit(pdom, "A::DefaultNested");
		assertVisibility(ICPPClassType.v_private, getMemberVisibility(defaultNested[0]));

		IBinding[] publicNested = findQualifiedPossiblyImplicit(pdom, "A::PublicNested");
		assertVisibility(ICPPClassType.v_public, getMemberVisibility(publicNested[0]));

		IBinding[] protectedNested = findQualifiedPossiblyImplicit(pdom, "A::ProtectedNested");
		assertVisibility(ICPPClassType.v_protected, getMemberVisibility(protectedNested[0]));

		IBinding[] privateNested = findQualifiedPossiblyImplicit(pdom, "A::PrivateNested");
		assertVisibility(ICPPClassType.v_private, getMemberVisibility(privateNested[0]));
	}

	// class A {
	// };
	public void testVisibilityImplicitClassMembers() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom, "A::A");

		assertVisibility(ICPPClassType.v_public, getMemberVisibility(memberBindings[0]));
		assertVisibility(ICPPClassType.v_public, getMemberVisibility(memberBindings[1]));
	}

	// template<typename T>
	// class Tpl {
	// };
	// template<>
	// class Tpl<int> {
	// };
	public void testVisibilityImplicitTemplateMembers() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom, "Tpl::Tpl");

		assertVisibility(ICPPClassType.v_public, getMemberVisibility(memberBindings[0]));
		assertVisibility(ICPPClassType.v_public, getMemberVisibility(memberBindings[1]));
		assertVisibility(ICPPClassType.v_public, getMemberVisibility(memberBindings[2]));
		assertVisibility(ICPPClassType.v_public, getMemberVisibility(memberBindings[3]));

	}

	// template<typename T>
	// class Tpl {
	// };
	// template<>
	// class Tpl<int> {
	//   int specializedDefaultVariable;
	// };
	public void testVisibilitySpecializedDefaultVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom, "Tpl::specializedDefaultVariable");
		assertVisibility(ICPPClassType.v_private, getMemberVisibility(memberBindings[0]));
	}

	// template<typename T>
	// class Tpl {
	// };
	// template<>
	// class Tpl<int> {
	// public:
	//   int specializedPublicVariable;
	// };
	public void testVisibilitySpecializedPublicVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom, "Tpl::specializedPublicVariable");
		assertVisibility(ICPPClassType.v_public, getMemberVisibility(memberBindings[0]));
	}

	// template<typename T>
	// class Tpl {
	// };
	// template<>
	// class Tpl<int> {
	// protected:
	//   int specializedProtectedVariable;
	// };
	public void testVisibilitySpecializedProtectedVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom, "Tpl::specializedProtectedVariable");
		assertVisibility(ICPPClassType.v_protected, getMemberVisibility(memberBindings[0]));
	}

	// template<typename T>
	// class Tpl {
	// };
	// template<>
	// class Tpl<int> {
	// private:
	//   int specializedPrivateVariable;
	// };
	public void testVisibilitySpecializedPrivateVariable() throws Exception {
		IBinding[] memberBinding = findQualifiedPossiblyImplicit(pdom, "Tpl::specializedPrivateVariable");
		assertVisibility(ICPPClassType.v_private, getMemberVisibility(memberBinding[0]));
	}

	private int getMemberVisibility(IBinding memberBinding) throws CoreException {
		IBinding owner = memberBinding.getOwner();
		assertInstance(owner, ICPPClassType.class);
		ICPPClassType classBinding = (ICPPClassType) owner;
		return classBinding.getVisibility(memberBinding);
	}
}
