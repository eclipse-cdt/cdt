/*******************************************************************************
 * Copyright (c) 2011, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import static org.eclipse.cdt.internal.corext.codemanipulation.StubUtility.trimFieldName;
import static org.eclipse.cdt.internal.ui.util.NameComposer.createByExample;

import org.eclipse.cdt.internal.ui.util.NameComposer;
import org.eclipse.cdt.ui.PreferenceConstants;

import junit.framework.TestCase;

public class NameComposerTest extends TestCase {
	private static final int CAPITALIZATION_ORIGINAL = PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL;
	private static final int CAPITALIZATION_UPPER_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_UPPER_CASE;
	private static final int CAPITALIZATION_LOWER_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CASE;
	private static final int CAPITALIZATION_CAMEL_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE;
	private static final int CAPITALIZATION_LOWER_CAMEL_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE;

	private NameComposer assertCreatedByExample(String seedName, String composedName, int defaultCapitalization,
			String defaultDelimiter) {
		NameComposer composer = createByExample(seedName, composedName, defaultCapitalization, defaultDelimiter);
		assertNotNull("Failed to create a name composer for \"" + seedName + "\", \"" + composedName + "\".", composer);
		assertEquals(composedName, composer.compose(seedName));
		return composer;
	}

	public void testCompose() {
		NameComposer composer = new NameComposer(CAPITALIZATION_ORIGINAL, "", "", ".h");
		assertEquals("MyClass.h", composer.compose("MyClass"));
		composer = new NameComposer(CAPITALIZATION_LOWER_CASE, "-", "", ".cc");
		assertEquals("my-class.cc", composer.compose("MyClass"));
		composer = new NameComposer(CAPITALIZATION_UPPER_CASE, "_", "", "");
		assertEquals("MY_CONSTANT", composer.compose("MyConstant"));
		composer = new NameComposer(CAPITALIZATION_CAMEL_CASE, "", "get", "");
		assertEquals("getMyField", composer.compose("myField"));
		assertEquals("getMyField", composer.compose("my_field_"));
		composer = new NameComposer(CAPITALIZATION_LOWER_CAMEL_CASE, "", "", "");
		assertEquals("myField", composer.compose("MyField"));
		composer = new NameComposer(CAPITALIZATION_LOWER_CASE, "_", "", "_");
		assertEquals("my_field_", composer.compose("MyField"));
		composer = new NameComposer(CAPITALIZATION_ORIGINAL, "_", "", "");
		assertEquals("red_Green_blue", composer.compose("_red_Green_blue"));
		composer = new NameComposer(CAPITALIZATION_CAMEL_CASE, "", "", "");
		assertEquals("RgbValue", composer.compose("RGBValue"));
		composer = new NameComposer(CAPITALIZATION_ORIGINAL, "_", "", "");
		assertEquals("RGB_Value", composer.compose("RGBValue"));
	}

	public void testCreateByExample() {
		NameComposer composer = assertCreatedByExample("MyName", "prefix_my-name_suffix", CAPITALIZATION_ORIGINAL, "");
		assertEquals("prefix_einstein-podolsky-rosen_suffix", composer.compose("EinsteinPodolskyRosen"));
		assertCreatedByExample("MyName", "PREFIX-MYNAME-SUFFIX", CAPITALIZATION_ORIGINAL, "_");
		assertNull(createByExample("Class", "Classic", CAPITALIZATION_ORIGINAL, ""));
	}

	public void testTrimFieldName() {
		assertEquals("f", trimFieldName("f_"));
		assertEquals("F", trimFieldName("F_"));
		assertEquals("oo", trimFieldName("F_oo"));
		assertEquals("o", trimFieldName("f_o"));

		assertEquals("M", trimFieldName("a_M_"));
		assertEquals("bs", trimFieldName("a_bs_"));
		assertEquals("foo_bar", trimFieldName("foo_bar"));
		assertEquals("foo_bar", trimFieldName("foo_bar_"));

		assertEquals("foo_b", trimFieldName("foo_b_"));

		assertEquals("foo", trimFieldName("foo"));
		assertEquals("foo", trimFieldName("_foo"));
		assertEquals("bar", trimFieldName("_f_bar"));

		assertEquals("f", trimFieldName("f__"));
		assertEquals("f", trimFieldName("__f"));
		assertEquals("O__b", trimFieldName("fO__b"));
		assertEquals("Oo", trimFieldName("fOo"));
		assertEquals("O", trimFieldName("fO"));
		assertEquals("MyStatic", trimFieldName("sMyStatic"));
		assertEquals("MyMember", trimFieldName("mMyMember"));

		assertEquals("8", trimFieldName("_8"));

		assertEquals("8bar", trimFieldName("_8bar_"));
		assertEquals("8bar_8", trimFieldName("_8bar_8"));
		assertEquals("8bAr", trimFieldName("_8bAr"));
		assertEquals("b8Ar", trimFieldName("_b8Ar"));

		assertEquals("Id", trimFieldName("Id"));
		assertEquals("ID", trimFieldName("ID"));
		assertEquals("IDS", trimFieldName("IDS"));
		assertEquals("ID", trimFieldName("bID"));
		assertEquals("Id", trimFieldName("MId"));
		assertEquals("IdA", trimFieldName("IdA"));
	}
}
