/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import junit.framework.TestCase;

import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType;

/**
 * Test for BasicProblemPreference
 */
public class MapProblemPreferenceTest extends TestCase {
	private static final String PAR1 = "aaa"; //$NON-NLS-1$
	private static final String PAR2 = "bbb"; //$NON-NLS-1$
	private MapProblemPreference map;
	private String key = "map"; //$NON-NLS-1$
	private MapProblemPreference map2;

	@Override
	protected void setUp() throws Exception {
		map = new MapProblemPreference(key, "My Value"); //$NON-NLS-1$
		map2 = new MapProblemPreference(key, "My Value2"); //$NON-NLS-1$
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
		BasicProblemPreference str = addPar(PAR1, "42.5"); //$NON-NLS-1$
		String value = map.exportValue();
		assertEquals("{" + str.getKey() + "=>42.5}", value); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testImportValue() {
		addPar(PAR1, "xxx"); //$NON-NLS-1$
		String value = map.exportValue();
		BasicProblemPreference str2 = new BasicProblemPreference(PAR1, PAR1);
		map2.addChildDescriptor(str2);
		map2.importValue(value);
		assertEquals("xxx", map2.getChildValue(PAR1)); //$NON-NLS-1$
	}

	public void testImportValueSpec() {
		BasicProblemPreference str = addPar(PAR1, "a=b"); //$NON-NLS-1$
		String value = map.exportValue();
		BasicProblemPreference str2 = new BasicProblemPreference(PAR1, PAR1);
		map2.addChildDescriptor(str2);
		map2.importValue(value);
		assertEquals(str.getValue(), map2.getChildValue(PAR1));
	}

	public void testImportValue2() {
		addPar(PAR1, "a=b"); //$NON-NLS-1$
		BasicProblemPreference p2 = addPar(PAR2, "2,\"2"); //$NON-NLS-1$
		String value = map.exportValue();
		map = new MapProblemPreference(key, "My Value"); //$NON-NLS-1$
		addPar(PAR1, null);
		addPar(PAR2, null);
		map.importValue(value);
		assertEquals("a=b", map.getChildValue(PAR1)); //$NON-NLS-1$
		assertEquals(p2.getValue(), map.getChildValue(PAR2));
	}
}
