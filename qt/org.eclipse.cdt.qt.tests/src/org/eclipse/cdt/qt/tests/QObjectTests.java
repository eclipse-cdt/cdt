/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.qt.core.index.IQEnum;
import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.IQProperty;
import org.eclipse.cdt.qt.core.index.IQProperty.Attribute;
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

    // #include "junit-QObject.hh"
    // class B : public QObject
    // {
    // Q_OBJECT
    // Q_PROPERTY(bool allowed READ isAllowed)
    // public:
    //     bool isAllowed() const { return false; }
    // };
	public void testOwner() throws Exception {
		loadComment("owner.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj = qtIndex.findQObject(new String[]{ "B" });
		if (!isIndexOk("B", qobj))
			return;
		assertNotNull(qobj);

		Collection<IQProperty> properties = qobj.getProperties().locals();
		assertNotNull(properties);
		assertEquals(1, properties.size());

		IQProperty property = properties.iterator().next();
		assertNotNull(property);
		assertTrue(qobj == property.getOwner());
	}

	// #include "junit-QObject.hh"
	// template <typename T> class T {};
	// class Q : public QObject
	// {
	// Q_OBJECT
	//
	//     bool getProp() const;
	//
	// // strange cases found by grep'ing for Q_PROPERTY in the qt4 headers
	// Q_PROPERTY( bool prop1 READ getProp )
	// Q_PROPERTY( T<bool> prop2 READ getProp )
	// Q_PROPERTY( T<bool *> prop3 READ getProp )
	// Q_PROPERTY( bool *prop4 READ getProp )
	// Q_PROPERTY( bool prop5 )
	// Q_PROPERTY( bool *prop6 )
	//
	// Q_PROPERTY( bool read1 READ readMethod )
	// Q_PROPERTY( bool read2 READ readMethod2 FINAL )
	//
	// // from qtoolbar.h
	// Q_PROPERTY( Namespace::Type  allowedAreas1 )
	// Q_PROPERTY( Qt::ToolBarAreas allowedAreas2 READ allowedAreas WRITE setAllowedAreas
	//             DESIGNABLE (qobject_cast<QMainWindow *>(parentWidget()) != 0)
	//             NOTIFY allowedAreasChanged )
	//
	//     bool readMethod();
	//     bool readMethod2() const { return false; }
	//     Qt::ToolBarAreas allowedAreas() const;
	//     void setAllowedAreas( Qt::ToolBarAreas ) { }
	//     Q_SIGNAL void allowedAreasChanged();
	// };
	public void testQProperties() throws Exception {
		loadComment("q_property.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj = qtIndex.findQObject(new String[]{ "Q" });
		if (!isIndexOk("Q", qobj))
			return;
		assertNotNull(qobj);

		assert_checkQProperties(
			qobj,
			new ExpectedQProperty("bool",      "prop1", Attribute.READ, "getProp"),
			new ExpectedQProperty("T<bool>",   "prop2", Attribute.READ, "getProp"),
			new ExpectedQProperty("T<bool *>", "prop3", Attribute.READ, "getProp"),
			new ExpectedQProperty("bool *",    "prop4", Attribute.READ, "getProp"),
			new ExpectedQProperty("bool",      "prop5"),
			new ExpectedQProperty("bool *",    "prop6"),

			new ExpectedQProperty("bool", "read1", Attribute.READ, "readMethod"),
			new ExpectedQProperty("bool", "read2", Attribute.READ ,"readMethod2", Attribute.FINAL),

			new ExpectedQProperty("Namespace::Type",    "allowedAreas1" ),
			new ExpectedQProperty("Qt::ToolBarAreas",   "allowedAreas2",
							      Attribute.READ,       "allowedAreas",
							      Attribute.WRITE,      "setAllowedAreas",
							      Attribute.DESIGNABLE, "(qobject_cast<QMainWindow *>(parentWidget()) != 0)",
							      Attribute.NOTIFY,     "allowedAreasChanged")
		);
	}

    // #include "junit-QObject.hh"
    // class B : public QObject
    // {
    // Q_OBJECT
    // Q_PROPERTY(bool allowed READ isAllowed)
    // public:
    //     bool isAllowed() const { return false; }
    // };
    // class D1 : public B
    // {
    // Q_OBJECT
    // Q_PROPERTY(bool allowed READ isAllowed_d)
    // public:
    //     bool isAllowed_d() const { return false; }
    // };
	public void testGetOverridden() throws Exception {
		loadComment("getOverridden.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject base_qobj = qtIndex.findQObject(new String[]{ "B" });
		if (!isIndexOk("B", base_qobj))
			return;
		assertNotNull(base_qobj);

		IQObject.IMembers<IQProperty> base_qprops = base_qobj.getProperties();
		assertNotNull(base_qprops);
		assertEquals(1, base_qprops.all().size());
		assertEquals(1, base_qprops.locals().size());
		assertEquals(1, base_qprops.withoutOverrides().size());

		IQObject derived_qobj1 = qtIndex.findQObject(new String[]{ "D1" });
		assertNotNull(derived_qobj1);
		IQObject.IMembers<IQProperty> derived_qprops1 = derived_qobj1.getProperties();
		assertNotNull(derived_qprops1);
		assertEquals(2, derived_qprops1.all().size());
		assertEquals(1, derived_qprops1.locals().size());
		assertEquals(1, derived_qprops1.withoutOverrides().size());
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

	private static class ExpectedQProperty {
		public final String type;
		public final String name;
		Object[] attributes;
		public ExpectedQProperty(String type, String name, Object... attributes) {
			this.type = type;
			this.name = name;
			this.attributes = attributes;
		}
	}

	/**
	 * A utility method for testing Q_PROPERTYs.  The given object is checked for the list of
	 * values.  Only the locally declared properties are checked and the list must be complete.
	 */
	private static void assert_checkQProperties(IQObject qobj, ExpectedQProperty... expectedProperties) throws Exception {

		// this map is used to make sure that all expected attributes are found
		Map<String, ExpectedQProperty> qprops = new HashMap<String, QObjectTests.ExpectedQProperty>();
		for(ExpectedQProperty qprop : expectedProperties)
			if (qprops.containsKey(qprop.name))
				fail("duplicate properties in expected list " + qprop.name);
			else
				qprops.put(qprop.name, qprop);

		for(IQProperty qprop : qobj.getProperties().locals()) {
			ExpectedQProperty expected = qprops.remove(qprop.getName());
			assertNotNull("unexpected or duplicate attribute " + qprop.getName(), expected);
			assertEquals("unexpected type for " + expected.name, expected.type, qprop.getType());
			assertEquals("unexpected type for " + expected.name, expected.name, qprop.getName());

			// make sure that all attributes that were found were expected
			Set<Attribute> allAttrs = new HashSet<Attribute>(Arrays.asList(Attribute.values()));

			for(int i = 0; i < expected.attributes.length; ++i) {
				Attribute attr = (Attribute)expected.attributes[i];

				// make sure the test is valid -- search for each attribute at most once
				assertTrue(allAttrs.remove(attr));

				if (!attr.hasValue)
					assertNotNull("missing " + attr.toString(), attr.valueIn(qprop));
				else if(i >= (expected.attributes.length - 1)
					 || expected.attributes[i + 1] instanceof Attribute)
					fail("INVALID TEST CASE: " + attr + " should have a value, but one was not provided" );
				else {
					Object exp = expected.attributes[++i];
					assertEquals(attr.toString(), exp, attr.valueIn(qprop));
				}
			}

			// make sure there is no value for all other attributes
			for(Attribute attr : allAttrs)
				assertTrue("unexpectedly found value for " + attr, attr.valueIn(qprop) == null);
		}

		// make sure that all expected properties were found
		StringBuilder missingAttrs = new StringBuilder();
		for(String propName : qprops.keySet()) {
			if (missingAttrs.length() > 0)
				missingAttrs.append(", ");
			missingAttrs.append(propName);
		}
		assertTrue("missing properties " + missingAttrs.toString(), missingAttrs.length() == 0);
	}
}
