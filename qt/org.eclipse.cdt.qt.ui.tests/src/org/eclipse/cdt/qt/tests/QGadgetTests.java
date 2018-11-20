/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.qt.tests;

import java.util.Collection;

import org.eclipse.cdt.internal.qt.core.index.IQEnum;
import org.eclipse.cdt.internal.qt.core.index.IQGadget;
import org.eclipse.cdt.internal.qt.core.index.QtIndex;

public class QGadgetTests extends BaseQtTestCase {

	// #include "junit-QObject.hh"
	// class G
	// {
	// Q_GADGET
	// };
	public void testFindQGadget() throws Exception {
		loadComment("qgadget.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQGadget qgadget = qtIndex.findQGadget(new String[] { "G" });
		if (!isIndexOk("G", qgadget))
			return;
		assertNotNull(qgadget);

		assertEquals("G", qgadget.getName());
		assertNotNull(qgadget.getEnums());
		assertEquals(0, qgadget.getEnums().size());
	}

	// #include "junit-QObject.hh"
	// class G0
	// {
	// Q_GADGET
	// public:
	//     enum EB { eb0 = 0xff };
	// };
	// class G
	// {
	// Q_GADGET
	//
	// enum E0 { e0a, e0b };
	//
	// Q_ENUMS( E0 G0::EB )
	// Q_ENUMS( E1 )
	//
	// enum E1 { e1a, e1b = 2 };
	// };
	public void testEnums() throws Exception {
		loadComment("qgadget_enums.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQGadget qgadget = qtIndex.findQGadget(new String[] { "G" });
		if (!isIndexOk("G", qgadget))
			return;
		assertNotNull(qgadget);

		Collection<IQEnum> qEnums = qgadget.getEnums();
		assertNotNull(qEnums);
		assertEquals(3, qEnums.size());
		for (IQEnum qEnum : qEnums) {
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
			} else if ("E1".equals(name)) {
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
			} else if ("G0::EB".equals(name)) {
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
	// class G
	// {
	// Q_GADGET
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
		loadComment("qgadget_flags.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQGadget qgadget = qtIndex.findQGadget(new String[] { "G" });
		if (!isIndexOk("G", qgadget))
			return;
		assertNotNull(qgadget);

		Collection<IQEnum> qEnums = qgadget.getEnums();
		assertNotNull(qEnums);
		assertEquals(2, qEnums.size());

		for (IQEnum qEnum : qEnums) {
			assertNotNull(qEnum);
			assertTrue(qEnum.isFlag());
			if ("Flag".equals(qEnum.getName())) {
				Collection<IQEnum.Enumerator> enumerators = qEnum.getEnumerators();
				assertNotNull(enumerators);
				assertEquals(1, enumerators.size());
				assertEquals("e0", enumerators.iterator().next().getName());
			} else if ("Flag2".equals(qEnum.getName())) {
				Collection<IQEnum.Enumerator> enumerators = qEnum.getEnumerators();
				assertNotNull(enumerators);
				assertEquals(1, enumerators.size());
				assertEquals("e2", enumerators.iterator().next().getName());
			} else
				fail("unexpected Q_FLAGS " + qEnum.getName());
		}
	}
}
