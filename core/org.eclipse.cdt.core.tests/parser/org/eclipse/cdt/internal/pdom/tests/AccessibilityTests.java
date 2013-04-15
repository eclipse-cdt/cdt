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
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Thomas Corbat
 *
 */
public class AccessibilityTests extends PDOMTestBase {

	protected PDOM pdom;

	public static Test suite() {
		return suite(AccessibilityTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("accessibilityTests");
			pdom = (PDOM)CCoreInternals.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}

	public void testVisibilityDefaultMemberFunction() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] functionBindings = getMember("AClass", "defaultMemFun");
		assertEquals(1, functionBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(functionBindings[0]);
		assertAccessibility(ICPPClassType.a_private, accessibility);
	}

	public void testVisibilityPublicMemberFunction() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] functionBindings = getMember("AClass", "publicMemFun");
		assertEquals(1, functionBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(functionBindings[0]);
		assertAccessibility(ICPPClassType.a_public, accessibility);
	}

	public void testVisibilityProtectedMemberFunction() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] functionBindings = getMember("AClass", "protectedMemFun");
		assertEquals(1, functionBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(functionBindings[0]);
		assertAccessibility(ICPPClassType.a_protected, accessibility);
	}

	public void testVisibilityPrivateMemberFunction() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] functionBindings = getMember("AClass", "privateMemFun");
		assertEquals(1, functionBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(functionBindings[0]);
		assertAccessibility(ICPPClassType.a_private, accessibility);
	}

	public void testVisibilityDefaultMemberVariable() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] variableBindings = getMember("AClass", "defaultVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(variableBindings[0]);
		assertAccessibility(ICPPClassType.a_private, accessibility);
	}

	public void testVisibilityPublicMemberVariable() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] variableBindings = getMember("AClass", "publicVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(variableBindings[0]);
		assertAccessibility(ICPPClassType.a_public, accessibility);
	}

	public void testVisibilityProtectedMemberVariable() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] variableBindings = getMember("AClass", "protectedVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(variableBindings[0]);
		assertAccessibility(ICPPClassType.a_protected, accessibility);
	}

	public void testVisibilityPrivateMemberVariable() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] variableBindings = getMember("AClass", "privateVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(variableBindings[0]);
		assertAccessibility(ICPPClassType.a_private, accessibility);
	}

	public void testVisibilityDefaultNestedClass() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] nestedBindings = getMember("AClass", "DefaultNested");
		assertEquals(1, nestedBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(nestedBindings[0]);
		assertAccessibility(ICPPClassType.a_private, accessibility);
	}

	public void testVisibilityPublicNestedClass() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] nestedBindings = getMember("AClass", "PublicNested");
		assertEquals(1, nestedBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(nestedBindings[0]);
		assertAccessibility(ICPPClassType.a_public, accessibility);
	}

	public void testVisibilityProtectedNestedClass() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] nestedBindings = getMember("AClass", "ProtectedNested");
		assertEquals(1, nestedBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(nestedBindings[0]);
		assertAccessibility(ICPPClassType.a_protected, accessibility);
	}

	public void testVisibilityPrivateNestedClass() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] nestedBindings = getMember("AClass", "PrivateNested");
		assertEquals(1, nestedBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(nestedBindings[0]);
		assertAccessibility(ICPPClassType.a_private, accessibility);
	}

	public void testVisibilityImplicitMembers() throws Exception {
		IBinding[] classBindings = getAClass();
		IBinding[] ctorBindings = getMember("AClass", "AClass");
		assertEquals(2, ctorBindings.length);

		ICPPClassType aClass = (ICPPClassType) classBindings[0];
		int accessibility = aClass.getAccessibility(ctorBindings[0]);
		assertAccessibility(ICPPClassType.a_public, accessibility);
		int accessibility2 = aClass.getAccessibility(ctorBindings[1]);
		assertAccessibility(ICPPClassType.a_public, accessibility2);
	}

	public void testTemplateVisibility() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();
		
		IBinding[] ctorBindings = getMember("ATemplate", "ATemplate");
		assertEquals(4, ctorBindings.length);
		
		ICPPClassTemplate aTemplate = (ICPPClassTemplate) aTemplateBindings[0];
		int accessibility = aTemplate.getAccessibility(ctorBindings[0]);
		assertAccessibility(ICPPClassType.a_public, accessibility);
		int accessibility2 = aTemplate.getAccessibility(ctorBindings[1]);
		assertAccessibility(ICPPClassType.a_public, accessibility2);
	}

	public void testTemplateSpecializationVisibility() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();
		
		IBinding[] ctorBindings = getMember("ATemplate", "ATemplate");
		assertEquals(4, ctorBindings.length);
		
		ICPPClassSpecialization aTemplateSpecialization = (ICPPClassSpecialization) aTemplateBindings[1];
		int accessibility = aTemplateSpecialization.getAccessibility(ctorBindings[2]);
		assertAccessibility(ICPPClassType.a_public, accessibility);
		int accessibility2 = aTemplateSpecialization.getAccessibility(ctorBindings[3]);
		assertAccessibility(ICPPClassType.a_public, accessibility2);
	}

	public void testVisibilitySpecializedDefaultVariable() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();
		IBinding[] variableBindings = getMember("ATemplate", "specializedDefaultVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aTemplateSpecialization = (ICPPClassType) aTemplateBindings[1];
		int accessibility = aTemplateSpecialization.getAccessibility(variableBindings[0]);
		assertAccessibility(ICPPClassType.a_private, accessibility);
	}

	public void testVisibilitySpecializedPublicVariable() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();
		IBinding[] variableBindings = getMember("ATemplate", "specializedPublicVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aTemplateSpecialization = (ICPPClassType) aTemplateBindings[1];
		int accessibility = aTemplateSpecialization.getAccessibility(variableBindings[0]);
		assertAccessibility(ICPPClassType.a_public, accessibility);
	}

	public void testVisibilitySpecializedProtectedVariable() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();
		IBinding[] variableBindings = getMember("ATemplate", "specializedProtectedVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aTemplateSpecialization = (ICPPClassType) aTemplateBindings[1];
		int accessibility = aTemplateSpecialization.getAccessibility(variableBindings[0]);
		assertAccessibility(ICPPClassType.a_protected, accessibility);
	}

	public void testVisibilitySpecializedPrivateVariable() throws Exception {
		IBinding[] aTemplateBindings = getATemplate();
		IBinding[] variableBindings = getMember("ATemplate", "specializedPrivateVariable");
		assertEquals(1, variableBindings.length);

		ICPPClassType aTemplateSpecialization = (ICPPClassType) aTemplateBindings[1];
		int accessibility = aTemplateSpecialization.getAccessibility(variableBindings[0]);
		assertAccessibility(ICPPClassType.a_private, accessibility);
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

	private void assertAccessibility(int expected, int actual) {
		String expectedAccessibility = accessibilityName(expected);
		String actualAccessibility = accessibilityName(actual);
		String message = "Expected access specifier:<" + expectedAccessibility + "> but was:<" + actualAccessibility + "> -";
		assertEquals(message, expected, actual);
	}

	private String accessibilityName(int expected) {
		switch(expected){
		case ICPPClassType.a_private:
			return "private";
		case ICPPClassType.a_protected:
			return "protected";
		case ICPPClassType.a_public:
			return "public";
		case ICPPClassType.a_unspecified:
			return "unspecified";
		default:
			return "illegal access specifier";
		}
	}
}
