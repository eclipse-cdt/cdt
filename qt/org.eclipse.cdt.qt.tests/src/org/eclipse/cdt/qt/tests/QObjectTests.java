/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.tests;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.qt.core.index.IQEnum;
import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.QtIndex;

public class QObjectTests extends BaseQtTestCase {

	// #include "junit-QObject.hh"
	// class T {};
	// class B1 : public QObject {Q_OBJECT};
	// class B2 : public QObject {Q_OBJECT};
	// class B3 : public QObject {Q_OBJECT};
	// class D1 : public B1, public B2, private B3, public T {Q_OBJECT};
	// class D2 : public T,  public QObject {};
	public void testGetBases() throws Exception {
		loadComment("bases.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj_B1 = qtIndex.findQObject(new String[]{ "B1" });
		if (!isIndexOk("B1", qobj_B1))
			return;
		IQObject qobj_D1 = qtIndex.findQObject(new String[]{ "D1" });
		assertNotNull(qobj_B1);
		assertNotNull(qobj_D1);

		Collection<IQObject> d1_bases = qobj_D1.getBases();
		assertNotNull(d1_bases);
		assertEquals(2, d1_bases.size());
		Iterator<IQObject> iterator = d1_bases.iterator();
		assertEquals(qobj_B1.getName(), iterator.next().getName());
		assertEquals("B2", iterator.next().getName());

		// D2 is not a QObject because it doesn't expand the Q_OBJECT macro
		IQObject qobj_D2 = qtIndex.findQObject(new String[]{ "D2" });
		assertNull(qobj_D2);
	}

    // #include "junit-QObject.hh"
    // class B : public QObject
    // {
    // Q_OBJECT
    // Q_CLASSINFO( "key1", "value1" )
    // Q_CLASSINFO( "key2", "value\"2" )
    // public:
    //     bool isAllowed() const { return false; }
    // };
    // class D : public B
    // {
    // Q_OBJECT
    // Q_CLASSINFO( "key2", "overridden value" )
    // public:
    //     bool isAllowed() const { return false; }
    // };
	public void testClassInfos() throws Exception {
		loadComment("classinfos.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj_b = qtIndex.findQObject(new String[]{ "B" });
		if (!isIndexOk("B", qobj_b))
			return;
		assertNotNull(qobj_b);
		assertEquals("value1", qobj_b.getClassInfo("key1"));
		assertEquals("value\\\"2", qobj_b.getClassInfo("key2"));

		IQObject qobj_d = qtIndex.findQObject(new String[]{ "D" });
		assertNotNull(qobj_d);
		assertEquals("value1", qobj_d.getClassInfo("key1")); // inherited
		assertEquals("overridden value", qobj_d.getClassInfo("key2"));
	}

	// #include "junit-QObject.hh"
	// template <typename T> class QList {};
	// class QString {};
	// class Q0 : public QObject
	// {
	// Q_OBJECT
	// public:
	//     enum EB { eb0 = 0xff };
	// };
	// class Q : public QObject
	// {
	// Q_OBJECT
	//
	// enum E0 { e0a, e0b };
	//
	// Q_ENUMS( E0 Q0::EB )
	// Q_ENUMS( E1 )
	//
	// enum E1 { e1a, e1b = 2 };
	// };
	public void testEnums() throws Exception {
		loadComment("qenums.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj = qtIndex.findQObject(new String[]{ "Q" });
		if (!isIndexOk("Q", qobj))
			return;
		assertNotNull(qobj);

		Collection<IQEnum> qEnums = qobj.getEnums();
		assertNotNull(qEnums);
		assertEquals(3, qEnums.size());
		for(IQEnum qEnum : qEnums) {
			String name = qEnum.getName();
			assertFalse(qEnum.isFlag());
			if ("E0".equals(name)) {
				Collection<IQEnum.Enumerator> enumerators = qEnum.getEnumerators();
				assertNotNull(enumerators);
				assertEquals(2, enumerators.size());
				for (IQEnum.Enumerator enumerator : enumerators) {
					Long ordinal = enumerator.getOrdinal();
					if (Long.valueOf(0).equals(ordinal))
						assertEquals("e0a", enumerator.getName());
					else if (Long.valueOf(1).equals(ordinal))
						assertEquals("e0b", enumerator.getName());
					else
						fail("unexpected " + name + "::" + enumerator.getName() + " = " + String.valueOf(ordinal));
				}
			} else if("E1".equals(name)) {
				Collection<IQEnum.Enumerator> enumerators = qEnum.getEnumerators();
				assertNotNull(enumerators);
				assertEquals(2, enumerators.size());
				for (IQEnum.Enumerator enumerator : enumerators) {
					Long ordinal = enumerator.getOrdinal();
					if (Long.valueOf(0).equals(ordinal))
						assertEquals("e1a", enumerator.getName());
					else if (Long.valueOf(2).equals(ordinal))
						assertEquals("e1b", enumerator.getName());
					else
						fail("unexpected " + name + "::" + enumerator.getName() + " = " + String.valueOf(ordinal));
				}
			} else if("Q0::EB".equals(name)) {
				Collection<IQEnum.Enumerator> enumerators = qEnum.getEnumerators();
				assertNotNull(enumerators);
				assertEquals(1, enumerators.size());
				for (IQEnum.Enumerator enumerator : enumerators) {
					Long ordinal = enumerator.getOrdinal();
					if (Long.valueOf(255).equals(ordinal))
						assertEquals("eb0", enumerator.getName());
					else
						fail("unexpected " + name + "::" + enumerator.getName() + " = " + String.valueOf(ordinal));
				}
			} else {
				fail("unexpected Q_ENUM " + name);
			}
		}
	}

	// #include "junit-QObject.hh"
	// template <typename T> class QList {};
	// class Q : public QObject
	// {
	// Q_OBJECT
	// enum Enum { e0 };
	// Q_DECLARE_FLAGS(Flag, Enum)
	// Q_FLAGS(Flag);
	// enum Enum2 { e2 };
	// Q_FLAGS(Flag2);
	// Q_DECLARE_FLAGS(Flag2, Enum2)
	// Q_DECLARE_FLAGS(Flag2b, Enum2)
	// enum Enum3 { e3 };
	// Q_DECLARE_FLAGS(Flag3, Enum3)
	// };
	public void testFlags() throws Exception {
		loadComment("qflags.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj = qtIndex.findQObject(new String[]{ "Q" });
		if (!isIndexOk("Q", qobj))
			return;
		assertNotNull(qobj);

		Collection<IQEnum> qEnums = qobj.getEnums();
		assertNotNull(qEnums);
		assertEquals(2, qEnums.size());

		for(IQEnum qEnum : qEnums) {
			assertNotNull(qEnum);
			assertTrue(qEnum.isFlag());
			if ("Flag".equals(qEnum.getName())) {
				Collection<IQEnum.Enumerator> enumerators = qEnum.getEnumerators();
				assertNotNull(enumerators);
				assertEquals(1, enumerators.size());
				assertEquals("e0", enumerators.iterator().next().getName());
			} else if("Flag2".equals(qEnum.getName())) {
				Collection<IQEnum.Enumerator> enumerators = qEnum.getEnumerators();
				assertNotNull(enumerators);
				assertEquals(1, enumerators.size());
				assertEquals("e2", enumerators.iterator().next().getName());
			} else
				fail("unexpected Q_FLAGS " + qEnum.getName());
		}
	}

//	// namespace A {
//	//   int a;
//	//   namespace B {
//	//     int b;
//	//     namespace C {
//	//       int c;
//	//     }
//	//     namespace A {
//	//       int a;
//	//     }
//	//   }
//	// }
//	public void testQualifiedNameLookup() throws Exception {
//		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
//
//		IScope scope = tu.getScope();
//		assertNotNull(scope);
//
//		IBinding[] bindings = QtASTVisitor.findBindingsForQualifiedName(scope, "  A::a");
//		assertNotNull(bindings);
//		assertEquals(1, bindings.length);
//		IBinding a = bindings[0];
//		assertEquals("a", a.getName());
//
//		bindings = QtASTVisitor.findBindingsForQualifiedName(scope, "A::B::b	");
//		assertNotNull(bindings);
//		assertEquals(1, bindings.length);
//		IBinding b = bindings[0];
//		assertEquals("b", b.getName());
//
//		bindings = QtASTVisitor.findBindingsForQualifiedName(scope, "A::	B  ::C::c");
//		assertNotNull(bindings);
//		assertEquals(1, bindings.length);
//		IBinding c = bindings[0];
//		assertEquals("c", c.getName());
//
//		// From the level of c, there should be two A::a (::A::a and ::A::B::A::a).
//		IScope scopeC = c.getScope();
//		assertNotNull(scopeC);
//		bindings = QtASTVisitor.findBindingsForQualifiedName(scopeC, "A::a");
//		assertNotNull(bindings);
//		assertEquals(2, bindings.length);
//
//		// From the level of c, there should be only one ::A::a.
//		assertNotNull(scopeC);
//		bindings = QtASTVisitor.findBindingsForQualifiedName(scopeC, "::A::a");
//		assertNotNull(bindings);
//		assertEquals(1, bindings.length);
//	}
}
