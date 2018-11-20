/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
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
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.core.index.IIndexBinding;

import junit.framework.TestSuite;

public class IndexCPPVariableTemplateResolutionTest extends IndexBindingResolutionTestBase {

	public static class SingleProject extends IndexCPPVariableTemplateResolutionTest {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	public static class ProjectWithDepProj extends IndexCPPVariableTemplateResolutionTest {
		public ProjectWithDepProj() {
			setStrategy(new ReferencedProject(true));
		}

		public static TestSuite suite() {
			return suite(ProjectWithDepProj.class);
		}
	}

	public static void addTests(TestSuite suite) {
		suite.addTest(SingleProject.suite());
		suite.addTest(ProjectWithDepProj.suite());
	}

	public IndexCPPVariableTemplateResolutionTest() {
		setStrategy(new ReferencedProject(true));
	}

	// template<typename T> constexpr T pi = T(3);

	// int f(){ return pi<int>; };
	public void testVariableTemplateReference() {
		checkBindings();
		ICPPVariableTemplate pi = getBindingFromASTName("pi", 0);
		ICPPVariableInstance piOfInt = getBindingFromASTName("pi<int>", 0);

		assertEquals(pi, piOfInt.getSpecializedBinding());
	}

	// struct S {
	//   template<typename T> static constexpr T pi = T(3);
	// };

	// int f(){ return S::pi<int>; };
	public void testFieldTemplateReference() {
		checkBindings();
		ICPPFieldTemplate pi = getBindingFromASTName("pi", 0);
		ICPPVariableInstance piOfInt = getBindingFromASTName("pi<int>", 0);

		assertEquals(pi, piOfInt.getSpecializedBinding());
	}

	// template<typename T> constexpr T pi = T(3);
	// template constexpr int pi<int>;

	// int f(){ return pi<int>; }
	public void testExplicitVariableInstance() {
		checkBindings();
		ICPPVariableTemplate pi = getBindingFromASTName("pi", 0);
		ICPPVariableInstance piOfInt = getBindingFromASTName("pi<int>", 0, ICPPVariableInstance.class,
				IIndexBinding.class);

		assertEquals(pi, piOfInt.getSpecializedBinding());
	}

	// struct S {
	//   template<typename T> static constexpr T pi = T(3);
	// };
	// template constexpr double S::pi<double>;

	// double f(){ return S::pi<double>; }
	public void testExplicitFieldInstance() {
		checkBindings();
		ICPPFieldTemplate pi = getBindingFromASTName("pi", 0);
		ICPPVariableInstance piOfDouble = getBindingFromASTName("pi<double>", 0, ICPPVariableInstance.class,
				ICPPField.class, IIndexBinding.class);

		assertEquals(pi, piOfDouble.getSpecializedBinding());
	}

	// template<typename T> constexpr T pi = T(3);
	// template<> constexpr int pi<int> = 4;

	// int f(){ return pi<int>; }
	public void testVariableSpecialization() {
		checkBindings();
		ICPPVariableTemplate pi = getBindingFromASTName("pi", 0);
		ICPPVariableInstance piOfInt = getBindingFromASTName("pi<int>", 0, ICPPVariableInstance.class,
				IIndexBinding.class);

		assertEquals(pi, piOfInt.getSpecializedBinding());
	}

	// struct S {
	//   template<typename T> static constexpr T pi = T(3);
	// };
	// template<> constexpr double S::pi<double> = 4;

	// double f(){ return S::pi<double>; }
	public void testFieldSpecialization() {
		checkBindings();
		ICPPFieldTemplate pi = getBindingFromASTName("pi", 0);
		ICPPVariableInstance piOfDouble = getBindingFromASTName("pi<double>", 0, ICPPVariableInstance.class,
				ICPPField.class, IIndexBinding.class);

		assertEquals(pi, piOfDouble.getSpecializedBinding());
	}

	// struct S {
	//   template<typename T> static constexpr T pi = T(3);
	// };

	// template<> constexpr double S::pi<double> = 4;
	public void testFieldSpecializationInRef() {
		checkBindings();
		ICPPVariableInstance piOfDouble = getBindingFromASTName("pi<double>", 0, ICPPVariableInstance.class,
				ICPPField.class);
	}

	// template<typename T, int I> T c = T(I);
	// template<int I> float c<float, I> = float(I);

	// float f() { return c<float, 100>; }
	public void testVariableTemplatePartialSpecialization() {
		checkBindings();
		ICPPVariableTemplate c = getBindingFromASTName("c", 0);

		ICPPVariableInstance cOfFloat = getBindingFromASTName("c<float, 100>", 0, ICPPVariableInstance.class);

		assertInstance(cOfFloat.getSpecializedBinding(), ICPPVariableTemplatePartialSpecialization.class,
				IIndexBinding.class);

		assertEquals(c,
				((ICPPVariableTemplatePartialSpecialization) cOfFloat.getSpecializedBinding()).getPrimaryTemplate());
	}

	// template<typename T> T c = T(1);
	// template<typename T> T* c<T*> = T(10);

	// float f() { return c<int*>; }
	public void testVariableTemplatePartialSpecialization2() {
		checkBindings();
		ICPPVariableTemplate c = getBindingFromASTName("c", 0);

		ICPPVariableInstance cOfIntPtr = getBindingFromASTName("c<int*>", 0, ICPPVariableInstance.class);

		assertInstance(cOfIntPtr.getSpecializedBinding(), ICPPVariableTemplatePartialSpecialization.class,
				IIndexBinding.class);

		assertEquals(c,
				((ICPPVariableTemplatePartialSpecialization) cOfIntPtr.getSpecializedBinding()).getPrimaryTemplate());
	}

	// struct S {
	//   template<typename T, int I> static constexpr T c = T(I);
	// };
	// template<int I> constexpr float S::c<float, I> = float(I);

	// float f() { return S::c<float, 100>; }
	public void testFieldTemplatePartialSpecialization() {
		checkBindings();
		ICPPVariableTemplate c = getBindingFromASTName("c", 0);

		ICPPVariableInstance cOfIntPtr = getBindingFromASTName("c<float, 100>", 0, ICPPVariableInstance.class,
				ICPPField.class);

		assertInstance(cOfIntPtr.getSpecializedBinding(), ICPPVariableTemplatePartialSpecialization.class,
				IIndexBinding.class, ICPPField.class);

		assertEquals(c.getClass(), ((ICPPVariableTemplatePartialSpecialization) cOfIntPtr.getSpecializedBinding())
				.getPrimaryTemplate().getClass());
	}

	//  template <typename T>
	//  constexpr bool templ = true;

	//  struct A {};
	//  constexpr bool waldo = templ<A>;
	public void testStorageOfUninstantiatedValue_bug486671() {
		checkBindings();
		IVariable waldo = getBindingFromASTName("waldo", 5);
		assertVariableValue(waldo, 1);
	}
}
