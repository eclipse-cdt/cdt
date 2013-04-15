/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.core.runtime.CoreException;

import static org.eclipse.cdt.core.parser.tests.VisibilityAsserts.assertVisibility;

/**
 * @author Thomas Corbat
 *
 * Tests for ensuring the PDOM contains the correct visibility information for class members.
 */
public class ClassMemberVisibilityTests extends PDOMInlineCodeTestBase {

	public static Test suite() {
		return suite(ClassMemberVisibilityTests.class);
	}

	public void setUp() throws Exception {
		super.setUp();
		setUpSections(1);
	}

	// class AClass {
	//   void defaultMemFun();
	// };
	public void testVisibilityDefaultMemberFunction() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::defaultMemFun");
		assertVisibility(ICPPClassType.v_private,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// public:
	//   void publicMemFun();
	// };
	public void testVisibilityPublicMemberFunction() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::publicMemFun");
		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// protected:
	//   void protectedMemFun();
	// };
	public void testVisibilityProtectedMemberFunction() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::protectedMemFun");
		assertVisibility(ICPPClassType.v_protected,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// private:
	//   void privateMemFun();
	// };
	public void testVisibilityPrivateMemberFunction() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::privateMemFun");
		assertVisibility(ICPPClassType.v_private,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	//   int defaultVariable;
	// };
	public void testVisibilityDefaultMemberVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::defaultVariable");
		assertVisibility(ICPPClassType.v_private,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// public:
	//   int publicVariable;
	// };
	public void testVisibilityPublicMemberVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::publicVariable");
		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// protected:
	//   int protectedVariable;
	// };
	public void testVisibilityProtectedMemberVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::protectedVariable");
		assertVisibility(ICPPClassType.v_protected,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// private:
	//   int privateVariable;
	// };
	public void testVisibilityPrivateMemberVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::privateVariable");
		assertVisibility(ICPPClassType.v_private,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	//   class DefaultNested {};
	// };
	public void testVisibilityDefaultNestedClass() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::DefaultNested");
		assertVisibility(ICPPClassType.v_private,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// public:
	//   class PublicNested {};
	// };
	public void testVisibilityPublicNestedClass() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::PublicNested");
		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// protected:
	//   class ProtectedNested {};
	// };
	public void testVisibilityProtectedNestedClass() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::ProtectedNested");
		assertVisibility(ICPPClassType.v_protected,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// private:
	//   class PrivateNested {};
	// };
	public void testVisibilityPrivateNestedClass() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::PrivateNested");
		assertVisibility(ICPPClassType.v_private,
				getMemberVisibility(memberBindings[0]));
	}

	// class AClass {
	// };
	public void testVisibilityImplicitClassMembers() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"AClass::AClass");

		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[0]));

		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[1]));
	}

	// template<typename T>
	// class ATemplate {
	// };
	// template<>
	// class ATemplate<int> {
	// };
	public void testVisibilityImplicitTemplateMembers() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"ATemplate::ATemplate");

		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[0]));

		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[1]));
	}

	// template<typename T>
	// class ATemplate {
	// };
	// template<>
	// class ATemplate<int> {
	// };
	public void testTemplateSpecializationImplicitMemberVisibility()
			throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"ATemplate::ATemplate");

		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[2]));

		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[3]));

	}

	// template<typename T>
	// class ATemplate {
	// };
	// template<>
	// class ATemplate<int> {
	//   int specializedDefaultVariable;
	// };
	public void testVisibilitySpecializedDefaultVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"ATemplate::specializedDefaultVariable");
		assertVisibility(ICPPClassType.v_private,
				getMemberVisibility(memberBindings[0]));
	}

	// template<typename T>
	// class ATemplate {
	// };
	// template<>
	// class ATemplate<int> {
	// public:
	//   int specializedPublicVariable;
	// };
	public void testVisibilitySpecializedPublicVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"ATemplate::specializedPublicVariable");
		assertVisibility(ICPPClassType.v_public,
				getMemberVisibility(memberBindings[0]));
	}

	// template<typename T>
	// class ATemplate {
	// };
	// template<>
	// class ATemplate<int> {
	// protected:
	//   int specializedProtectedVariable;
	// };
	public void testVisibilitySpecializedProtectedVariable() throws Exception {
		IBinding[] memberBindings = findQualifiedPossiblyImplicit(pdom,
				"ATemplate::specializedProtectedVariable");
		assertVisibility(ICPPClassType.v_protected,
				getMemberVisibility(memberBindings[0]));
	}

	// template<typename T>
	// class ATemplate {
	// };
	// template<>
	// class ATemplate<int> {
	// private:
	//   int specializedPrivateVariable;
	// };
	public void testVisibilitySpecializedPrivateVariable() throws Exception {
		IBinding[] memberBinding = findQualifiedPossiblyImplicit(pdom,
				"ATemplate::specializedPrivateVariable");
		assertVisibility(ICPPClassType.v_private,
				getMemberVisibility(memberBinding[0]));
	}

	private int getMemberVisibility(IBinding memberBinding) throws CoreException {
		IBinding owner = memberBinding.getOwner();
		assertInstance(owner, ICPPClassType.class);
		ICPPClassType classBinding = (ICPPClassType) owner;
		return classBinding.getVisibility(memberBinding);
	}
}
