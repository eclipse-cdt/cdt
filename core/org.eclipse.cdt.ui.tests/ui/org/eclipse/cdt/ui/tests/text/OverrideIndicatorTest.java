/*******************************************************************************
 * Copyright (c) 2015 Patrick Hofer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Hofer - Initial API and implementation
 *     Tomasz Wesolowski - more tests
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import org.eclipse.cdt.internal.ui.editor.OverrideIndicatorManager;
import org.eclipse.cdt.internal.ui.editor.OverrideIndicatorManager.OverrideIndicator;
import org.eclipse.cdt.ui.tests.AnnotationTestCase;
import org.eclipse.jface.text.source.Annotation;

/**
 * Test for {@see OverrideIndicatorManager} class.
 */
public class OverrideIndicatorTest extends AnnotationTestCase {
	private Integer expectedAnnotationType;

	public OverrideIndicatorTest() {
		super();
		testedAnnotationId = OverrideIndicator.ANNOTATION_TYPE_ID;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		//enableProblems(NonVirtualDestructor.ER_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public boolean checkAnnotationType(Annotation annotation) {
		if (annotation instanceof OverrideIndicator) {
			if (expectedAnnotationType != null) {
				OverrideIndicator oi = (OverrideIndicator) annotation;
				return expectedAnnotationType == oi.getIndicationType();
			}
		}
		return true;
	}

	protected void checkImplementsAnnotationLines(Object... args) {
		expectedAnnotationType = OverrideIndicatorManager.ANNOTATION_IMPLEMENTS;
		try {
			checkAnnotationLines(args);
		} finally {
			expectedAnnotationType = null;
		}
	}

	protected void checkOverridesAnnotationLines(Object... args) {
		expectedAnnotationType = OverrideIndicatorManager.ANNOTATION_OVERRIDES;
		try {
			checkAnnotationLines(args);
		} catch (Exception e) {
			expectedAnnotationType = null;
		}
	}

	protected void checkShadowsAnnotationLines(Object... args) {
		expectedAnnotationType = OverrideIndicatorManager.ANNOTATION_SHADOWS;
		try {
			checkAnnotationLines(args);
		} catch (Exception e) {
			expectedAnnotationType = null;
		}
	}

	// class A {
	//   virtual void vm() = 0;
	// };
	//
	// class B : public A {
	//   virtual void vm() {};
	// };
	public void testSimpleImplementedAnnotation() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkImplementsAnnotationLines(6);
	}

	// class A {
	//   virtual void vm() {};
	// };
	//
	// class B : public A {
	//   virtual void vm() {};
	// };
	public void testSimpleOverridenAnnotation() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkOverridesAnnotationLines(6);
	}

	// class A {
	//   void m(void) {};
	// };
	//
	// class B : public A {
	//   void m(int param) {};
	// };
	public void testSimpleShadowedAnnotation() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkShadowsAnnotationLines(6);
	}

	// class A {
	//   virtual void m(void) {};
	// };
	//
	// class B : public A {
	//   void m(int param) {};
	// };
	public void testShadowedVirtualAnnotation1() throws Exception {
		loadCodeAndRun(getAboveComment());
		// Non-virtual shadowing virtual
		checkShadowsAnnotationLines(6);
	}

	// class A {
	//   void m(void) {};
	// };
	//
	// class B : public A {
	//   virtual void m(int param) {};
	// };
	public void testShadowedVirtualAnnotation2() throws Exception {
		loadCodeAndRun(getAboveComment());
		// Virtual shadowing non-virtual
		checkShadowsAnnotationLines(6);
	}

	// class A {
	//   virtual void m(void) {};
	// };
	//
	// class B : public A {
	//   virtual void m(int param) {};
	// };
	public void testShadowedVirtualAnnotation3() throws Exception {
		loadCodeAndRun(getAboveComment());
		// Virtual shadowing virtual
		checkShadowsAnnotationLines(6);
	}

	//	struct X {
	//		virtual ~X();
	//		virtual void foo() const;
	//	};
	//	struct Y : X {
	//		void foo();
	//	};
	public void testShadowedConstByNonConst() throws Exception {
		loadCodeAndRun(getAboveComment());
		// CV-qualifiers produce different overloads
		checkShadowsAnnotationLines(6);
	}

	//	struct X {
	//		virtual ~X();
	//		virtual void foo();
	//	};
	//	struct Y : X {
	//		virtual void foo() volatile;
	//	};
	public void testShadowedNonVolatileByVolatile() throws Exception {
		loadCodeAndRun(getAboveComment());
		// CV-qualifiers produce different overloads
		checkShadowsAnnotationLines(6);
	}

	// class I1 {
	//   virtual void vm1(void) = 0;
	// };
	//
	// class I2 {
	//   virtual void vm2(void) = 0;
	// };
	//
	// class D : I1, I2 {
	//  public:
	//   virtual void vm1(void) {};
	//   virtual void vm2(void) {};
	// };
	public void testAnnotationsWithMultipleInheritance() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkImplementsAnnotationLines(11, 12);
	}

	// class I1 {
	//   virtual void vm1(void) = 0;
	// };
	//
	// class I2 {
	//   virtual void vm2(void) = 0;
	// };
	//
	// class D : I2, I1 {
	//  public:
	//   virtual void vm1(void) {};
	//   virtual void vm2(void) {};
	// };
	public void testAnnotationsWithMultipleInheritanceReverseOrder() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkImplementsAnnotationLines(11, 12);
	}

	// class I1 {
	//   virtual void vm1(void) = 0;
	// };
	//
	// class I2 {
	//   virtual void vm2(void) = 0;
	// };
	//
	// class B1 {
	//  public:
	//   void m1(void) {};
	//   virtual void vm3(void) {};
	//   virtual ~B1();
	// };
	//
	// class B2 : public B1 {
	//  public:
	//   void m2(void) {};
	// };
	//
	// class D : public B2, I1, I2 {
	//  public:
	//   void m1(void) {};     // line 23
	//   void m2(int param) {};
	//   virtual void vm1(void) {};
	//   virtual void vm2(void) {};
	//   virtual void vm3(void) {};
	//   virtual void vm4(void) {};
	// };
	public void testAnnotationsInClassHierarchy() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkShadowsAnnotationLines(23, 24);
		checkImplementsAnnotationLines(25, 26);
		checkOverridesAnnotationLines(27);
	}

	//	struct Foo {
	//	  virtual void waldo(int) = 0;
	//	  virtual void waldo(float) = 0;
	//	};
	//
	//	struct Bar : Foo {
	//	  void waldo(int) override;
	//	  void waldo(float) override;
	//	};
	public void testMultipleOverloadsOverridden_479142() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkImplementsAnnotationLines(7, 8);
	}
}
