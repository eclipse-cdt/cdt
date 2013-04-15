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
import org.eclipse.cdt.core.parser.tests.VisibilityHelper;
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
		IBinding[] classBindings = getAClass();
		IBinding[] functionBindings = getMember("AClass", "defaultMemFun");
		assertEquals(1, functionBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(functionBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//  public:
	//    void publicMemFun();
	//  };
	public void testVisibilityPublicMemberFunction() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] functionBindings = getMember("AClass", "publicMemFun");
		assertEquals(1, functionBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(functionBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility);
	}

	//  class AClass {
	//  protected:
	//    void protectedMemFun();
	//  };
	public void testVisibilityProtectedMemberFunction() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] functionBindings = getMember("AClass", "protectedMemFun");
		assertEquals(1, functionBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(functionBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_protected, visibility);
	}

	//  class AClass {
	//  private:
	//    void privateMemFun();
	//  };
	public void testVisibilityPrivateMemberFunction() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] functionBindings = getMember("AClass", "privateMemFun");
		assertEquals(1, functionBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(functionBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//    int defaultVariable;
	//  };
	public void testVisibilityDefaultMemberVariable() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] variableBindings = getMember("AClass", "defaultVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(variableBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//  public:
	//    int publicVariable;
	//  };
	public void testVisibilityPublicMemberVariable() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] variableBindings = getMember("AClass", "publicVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(variableBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility);
	}

	//  class AClass {
	//  protected:
	//    int protectedVariable;
	//  };
	public void testVisibilityProtectedMemberVariable() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] variableBindings = getMember("AClass", "protectedVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(variableBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_protected, visibility);
	}

	//  class AClass {
	//  private:
	//    int privateVariable;
	//  };
	public void testVisibilityPrivateMemberVariable() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] variableBindings = getMember("AClass", "privateVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(variableBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//    class DefaultNested {};
	//  };
	public void testVisibilityDefaultNestedClass() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] nestedBindings = getMember("AClass", "DefaultNested");
		assertEquals(1, nestedBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(nestedBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//  public:
	//    class PublicNested {};
	//  };
	public void testVisibilityPublicNestedClass() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] nestedBindings = getMember("AClass", "PublicNested");
		assertEquals(1, nestedBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(nestedBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility);
	}

	//  class AClass {
	//  protected:
	//    class ProtectedNested {};
	//  };
	public void testVisibilityProtectedNestedClass() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] nestedBindings = getMember("AClass", "ProtectedNested");
		assertEquals(1, nestedBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(nestedBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_protected, visibility);
	}

	//  class AClass {
	//  private:
	//    class PrivateNested {};
	//  };
	public void testVisibilityPrivateNestedClass() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] nestedBindings = getMember("AClass", "PrivateNested");
		assertEquals(1, nestedBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(nestedBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_private, visibility);
	}

	//  class AClass {
	//  };
	public void testVisibilityImplicitClassMembers() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] ctorBindings = getMember("AClass", "AClass");
		assertEquals(2, ctorBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int visibility = aClass.getVisibility(ctorBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility);
		int visibility2 = aClass.getVisibility(ctorBindings[1]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility2);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//  };
	public void testVisibilityImplicitTemplateMembers() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();

		IBinding[] ctorBindings = getMember("ATemplate", "ATemplate");
		assertEquals(4, ctorBindings.length);

		ICPPClassTemplate aTemplate = (ICPPClassTemplate) aTemplateBindings[0];
		int visibility = aTemplate.getVisibility(ctorBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility);
		int visibility2 = aTemplate.getVisibility(ctorBindings[1]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility2);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//  };
	public void testTemplateSpecializationImplicitMemberVisibility() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();

		IBinding[] ctorBindings = getMember("ATemplate", "ATemplate");
		assertEquals(4, ctorBindings.length);

		ICPPClassSpecialization aTemplateSpecialization = (ICPPClassSpecialization) aTemplateBindings[1];
		int visibility = aTemplateSpecialization.getVisibility(ctorBindings[2]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility);
		int visibility2 = aTemplateSpecialization.getVisibility(ctorBindings[3]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility2);
	}

	//  template<typename T>
	//  class ATemplate {
	//  };
	//  template<>
	//  class ATemplate<int> {
	//    int specializedDefaultVariable;
	//  };
	public void testVisibilitySpecializedDefaultVariable() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();
		IBinding[] variableBindings = getMember("ATemplate", "specializedDefaultVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aTemplateSpecialization = (ICPPClassType) aTemplateBindings[1];
		int visibility = aTemplateSpecialization.getVisibility(variableBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_private, visibility);
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
		IBinding[] aTemplateBindings = getATemplate();
		IBinding[] variableBindings = getMember("ATemplate", "specializedPublicVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aTemplateSpecialization = (ICPPClassType) aTemplateBindings[1];
		int visibility = aTemplateSpecialization.getVisibility(variableBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_public, visibility);
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
		IBinding[] aTemplateBindings = getATemplate();
		IBinding[] variableBindings = getMember("ATemplate", "specializedProtectedVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aTemplateSpecialization = (ICPPClassType) aTemplateBindings[1];
		int visibility = aTemplateSpecialization.getVisibility(variableBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_protected, visibility);
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
		IBinding[] aTemplateBindings = getATemplate();
		IBinding[] variableBindings = getMember("ATemplate", "specializedPrivateVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aTemplateSpecialization = (ICPPClassType) aTemplateBindings[1];
		int visibility = aTemplateSpecialization.getVisibility(variableBindings[0]);
		VisibilityHelper.assertVisibility(ICPPClassType.v_private, visibility);
	}

	private IBinding[] getATemplate() throws CoreException {
		char[][] templateName= {"ATemplate".toCharArray()};
		IBinding[] aTemplateBindings= pdom.findBindings(templateName, IndexFilter.ALL, npm());
		assertEquals(2, aTemplateBindings.length);
		assertInstance(aTemplateBindings[0], ICPPClassTemplate.class);
		assertInstance(aTemplateBindings[1], ICPPClassSpecialization.class);
		return aTemplateBindings;
	}

	private IBinding[] getMember(String parentName, String memberName) throws CoreException {
		char[][] functionName= {parentName.toCharArray(), memberName.toCharArray()};
		IBinding[] functionBindings= pdom.findBindings(functionName, IndexFilter.ALL, npm());
		return functionBindings;
	}

	private IBinding[] getAClass() throws CoreException {
		char[][] className= {"AClass".toCharArray()};
		IBinding[] classBindings= pdom.findBindings(className, IndexFilter.ALL, npm());
		assertEquals(1, classBindings.length);
		assertInstance(classBindings[0], ICPPClassType.class);
		return classBindings;
	}
}
