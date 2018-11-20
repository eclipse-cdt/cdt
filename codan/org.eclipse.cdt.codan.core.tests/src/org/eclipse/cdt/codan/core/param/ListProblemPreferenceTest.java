/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.util.Arrays;

import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType;

import junit.framework.TestCase;

/**
 * Test for BasicProblemPreference
 */
public class ListProblemPreferenceTest extends TestCase {
	private static final String PAR1 = "0";
	private static final String PAR2 = "1";
	private ListProblemPreference list;
	private String key = "list";
	private ListProblemPreference list2;

	@Override
	protected void setUp() throws Exception {
		list = new ListProblemPreference(key, "My Value");
		list2 = new ListProblemPreference(key, "My Value2");
	}

	/**
	 * @param parval
	 * @return
	 */
	protected BasicProblemPreference addPar(String key, Object parval) {
		BasicProblemPreference str = makePar(key, parval);
		list.addChildDescriptor(str);
		return (BasicProblemPreference) list.getChildDescriptor(key);
	}

	/**
	 * @param parval
	 * @param parval
	 * @return
	 */
	protected BasicProblemPreference makePar(String key, Object parval) {
		BasicProblemPreference str = new BasicProblemPreference(key, key);
		if (parval != null) {
			str.setValue(parval);
			str.setType(PreferenceType.typeOf(parval));
		}
		return str;
	}

	public void testExportValueStr() {
		BasicProblemPreference str = addPar(PAR1, "42.5");
		String value = list.exportValue();
		assertEquals("(42.5)", value);
	}

	public void testImportValue() {
		addPar(PAR1, "xxx");
		String value = list.exportValue();
		BasicProblemPreference str2 = new BasicProblemPreference(PAR1, PAR1);
		list2.addChildDescriptor(str2);
		list2.importValue(value);
		assertEquals("xxx", list2.getChildValue(PAR1));
	}

	public void testImportValueSpec() {
		BasicProblemPreference str = addPar(PAR1, "a=b");
		String value = list.exportValue();
		BasicProblemPreference str2 = new BasicProblemPreference(PAR1, PAR1);
		list2.addChildDescriptor(str2);
		list2.importValue(value);
		assertEquals(str.getValue(), list2.getChildValue(PAR1));
	}

	public void testImportValue2() {
		addPar(PAR1, "a=b");
		BasicProblemPreference p2 = addPar(PAR2, "2,\"2");
		String value = list.exportValue();
		list = new ListProblemPreference(key, "My Value");
		addPar(PAR1, null);
		addPar(PAR2, null);
		list.importValue(value);
		assertEquals("a=b", list.getChildValue(PAR1));
		assertEquals(p2.getValue(), list.getChildValue(PAR2));
	}

	public void testImportValue2_nosec() {
		addPar(PAR1, "a=b");
		BasicProblemPreference p2 = addPar(PAR2, "2' 2\"");
		String value = list.exportValue();
		list = new ListProblemPreference(key, "My Value");
		addPar(PAR1, null);
		list.importValue(value);
		assertEquals("a=b", list.getChildValue(PAR1));
		assertEquals(p2.getValue(), list.getChildValue(PAR2));
	}

	public void testGetValue() {
		list.setChildDescriptor(new BasicProblemPreference("#", "Value"));
		String x[] = { "a", "b" };
		list.addChildValue(x[0]);
		list.addChildValue(x[1]);
		Object[] values = list.getValues();
		assertTrue(Arrays.deepEquals(x, values));
	}

	public void testSetValue() {
		list.setChildDescriptor(new BasicProblemPreference("#", "Value"));
		String x[] = { "a", "b" };
		list.setValue(x);
		Object[] values = list.getValues();
		assertTrue(Arrays.deepEquals(x, values));
	}

	public void testSetValueImport() {
		list.setChildDescriptor(new BasicProblemPreference("#", "Value"));
		String x[] = { "a", "b" };
		list.setValue(x);
		list.importValue("(x)");
		Object[] values = list.getValues();
		assertEquals(1, values.length);
		assertEquals("x", values[0]);
	}
}
