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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.tests.VisibilityAsserts;
import org.eclipse.core.runtime.CoreException;

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

	//  class AClass {
	//    void defaultMemFun();
	//  };
	public void testVisibilityDefaultMemberFunction() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "defaultMemFun", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//  public:
	//    void publicMemFun();
	//  };
	public void testVisibilityPublicMemberFunction() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "publicMemFun", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility);
	}

	//  class AClass {
	//  protected:
	//    void protectedMemFun();
	//  };
	public void testVisibilityProtectedMemberFunction() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "protectedMemFun", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_protected, visibility);
	}

	//  class AClass {
	//  private:
	//    void privateMemFun();
	//  };
	public void testVisibilityPrivateMemberFunction() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "privateMemFun", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//    int defaultVariable;
	//  };
	public void testVisibilityDefaultMemberVariable() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "defaultVariable", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//  public:
	//    int publicVariable;
	//  };
	public void testVisibilityPublicMemberVariable() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "publicVariable", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility);
	}

	//  class AClass {
	//  protected:
	//    int protectedVariable;
	//  };
	public void testVisibilityProtectedMemberVariable() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "protectedVariable", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_protected, visibility);
	}

	//  class AClass {
	//  private:
	//    int privateVariable;
	//  };
	public void testVisibilityPrivateMemberVariable() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "privateVariable", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//    class DefaultNested {};
	//  };
	public void testVisibilityDefaultNestedClass() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "DefaultNested", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//  public:
	//    class PublicNested {};
	//  };
	public void testVisibilityPublicNestedClass() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "PublicNested", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility);
	}

	//  class AClass {
	//  protected:
	//    class ProtectedNested {};
	//  };
	public void testVisibilityProtectedNestedClass() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "ProtectedNested", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_protected, visibility);
	}

	//  class AClass {
	//  private:
	//    class PrivateNested {};
	//  };
	public void testVisibilityPrivateNestedClass() throws Exception {
		int visibility = getMemberVisibility(getAClass(), "PrivateNested", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//  };
	public void testVisibilityImplicitClassMembers() throws Exception {
		ICPPClassType classBinding = getAClass();
		int visibility = getMemberVisibility(classBinding, "AClass", 2, 0);
		int visibility2 = getMemberVisibility(classBinding, "AClass", 2, 1);

		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility2);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//  };
	public void testVisibilityImplicitTemplateMembers() throws Exception {
		ICPPClassType aTemplateBinding = getATemplate(0);
		int visibility = getMemberVisibility(aTemplateBinding, "ATemplate", 4, 0);
		int visibility2 = getMemberVisibility(aTemplateBinding, "ATemplate", 4, 1);

		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility2);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//  };
	public void testTemplateSpecializationImplicitMemberVisibility() throws Exception {
		ICPPClassType aTemplateBinding = getATemplate(1);
		int visibility = getMemberVisibility(aTemplateBinding, "ATemplate", 4, 2);
		int visibility2 = getMemberVisibility(aTemplateBinding, "ATemplate", 4, 3);

		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility2);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//    int specializedDefaultVariable;
	//  };
	public void testVisibilitySpecializedDefaultVariable() throws Exception {
		int visibility = getMemberVisibility(getATemplate(1), "specializedDefaultVariable", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//  public:
	//    int specializedPublicVariable;
	//  };
	public void testVisibilitySpecializedPublicVariable() throws Exception {
		int visibility = getMemberVisibility(getATemplate(1), "specializedPublicVariable", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_public, visibility);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//  protected:
	//    int specializedProtectedVariable;
	//  };
	public void testVisibilitySpecializedProtectedVariable() throws Exception {
		int visibility = getMemberVisibility(getATemplate(1), "specializedProtectedVariable", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_protected, visibility);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//  private:
	//    int specializedPrivateVariable;
	//  };
	public void testVisibilitySpecializedPrivateVariable() throws Exception {
		int visibility = getMemberVisibility(getATemplate(1), "specializedPrivateVariable", 1, 0);
		VisibilityAsserts.assertVisibility(ICPPClassType.v_private, visibility);
	}

	private ICPPClassType getATemplate(int templateIndex) throws CoreException {
		char[][] templateName= {"ATemplate".toCharArray()};
		IBinding[] aTemplateBindings= pdom.findBindings(templateName, IndexFilter.ALL, npm());
		assertEquals(2, aTemplateBindings.length);
		assertInstance(aTemplateBindings[0], ICPPClassTemplate.class);
		assertInstance(aTemplateBindings[1], ICPPClassSpecialization.class);
		ICPPClassType aTemplate = (ICPPClassType) aTemplateBindings[templateIndex];
		return aTemplate;
	}

	private IBinding[] getMember(String parentName, String memberName, int expectedMemberCount) throws CoreException {
		char[][] qualifiedMemberName = {parentName.toCharArray(), memberName.toCharArray()};
		IBinding[] memberBindings = pdom.findBindings(qualifiedMemberName, IndexFilter.ALL, npm());
		assertEquals(expectedMemberCount, memberBindings.length);
		return memberBindings;
	}

	private ICPPClassType getAClass() throws CoreException {
		char[][] className = {"AClass".toCharArray()};
		IBinding[] classBindings = pdom.findBindings(className, IndexFilter.ALL, npm());
		assertEquals(1, classBindings.length);
		assertInstance(classBindings[0], ICPPClassType.class);
		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		return aClass;
	}

	private int getMemberVisibility(ICPPClassType classBinding, String memberName, int expectedMemberCount, int memberIndex) throws CoreException {
		IBinding[] memberBindings = getMember(classBinding.getName(), memberName, expectedMemberCount);
		return classBinding.getVisibility(memberBindings[memberIndex]);
	}
}
