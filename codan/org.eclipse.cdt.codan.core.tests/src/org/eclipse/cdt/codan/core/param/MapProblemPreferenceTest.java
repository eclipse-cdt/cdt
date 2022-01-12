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

import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType;

import junit.framework.TestCase;

/**
 * Test for BasicProblemPreference
 */
public class MapProblemPreferenceTest extends TestCase {
	private static final String PAR1 = "aaa";
	private static final String PAR2 = "bbb";
	private MapProblemPreference map;
	private String key = "map";
	private MapProblemPreference map2;

	@Override
	protected void setUp() throws Exception {
		map = new MapProblemPreference(key, "My Value");
		map2 = new MapProblemPreference(key, "My Value2");
	}

	/**
	 * @param parval
	 * @return
	 */
	protected BasicProblemPreference addPar(String key, Object parval) {
		BasicProblemPreference str = makePar(key, parval);
		map.addChildDescriptor(str);
		return str;
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
		String value = map.exportValue();
		assertEquals("{" + str.getKey() + "=>42.5}", value);
	}

	public void testImportValue() {
		addPar(PAR1, "xxx");
		String value = map.exportValue();
		BasicProblemPreference str2 = new BasicProblemPreference(PAR1, PAR1);
		map2.addChildDescriptor(str2);
		map2.importValue(value);
		assertEquals("xxx", map2.getChildValue(PAR1));
	}

	public void testImportValueSpec() {
		BasicProblemPreference str = addPar(PAR1, "a=b");
		String value = map.exportValue();
		BasicProblemPreference str2 = new BasicProblemPreference(PAR1, PAR1);
		map2.addChildDescriptor(str2);
		map2.importValue(value);
		assertEquals(str.getValue(), map2.getChildValue(PAR1));
	}

	public void testImportValue2() {
		addPar(PAR1, "a=b");
		BasicProblemPreference p2 = addPar(PAR2, "2,\"2");
		String value = map.exportValue();
		map = new MapProblemPreference(key, "My Value");
		addPar(PAR1, null);
		addPar(PAR2, null);
		map.importValue(value);
		assertEquals("a=b", map.getChildValue(PAR1));
		assertEquals(p2.getValue(), map.getChildValue(PAR2));
	}
}
