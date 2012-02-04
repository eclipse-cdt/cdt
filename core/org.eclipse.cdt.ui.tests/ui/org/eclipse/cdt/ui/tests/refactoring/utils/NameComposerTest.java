/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.util.NameComposer;

public class NameComposerTest extends TestCase {
	private static final int CAPITALIZATION_ORIGINAL = PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL;
	private static final int CAPITALIZATION_UPPER_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_UPPER_CASE;
	private static final int CAPITALIZATION_LOWER_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CASE;
	private static final int CAPITALIZATION_CAMEL_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE;
	private static final int CAPITALIZATION_LOWER_CAMEL_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE;
	
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
	
	public void testTrimFieldName() {
		assertEquals("f", StubUtility.trimFieldName("f_"));
		assertEquals("F", StubUtility.trimFieldName("F_"));
		assertEquals("oo", StubUtility.trimFieldName("F_oo"));
		assertEquals("o", StubUtility.trimFieldName("f_o"));
		
		assertEquals("M", StubUtility.trimFieldName("a_M_"));
		assertEquals("bs", StubUtility.trimFieldName("a_bs_"));
		assertEquals("foo_bar", StubUtility.trimFieldName("foo_bar"));
		assertEquals("foo_bar", StubUtility.trimFieldName("foo_bar_"));
		
		assertEquals("foo_b", StubUtility.trimFieldName("foo_b_"));
		
		assertEquals("foo", StubUtility.trimFieldName("foo"));
		assertEquals("foo", StubUtility.trimFieldName("_foo"));
		assertEquals("bar", StubUtility.trimFieldName("_f_bar"));
		
		assertEquals("f", StubUtility.trimFieldName("f__"));
		assertEquals("f", StubUtility.trimFieldName("__f"));
		assertEquals("O__b", StubUtility.trimFieldName("fO__b"));
		assertEquals("Oo", StubUtility.trimFieldName("fOo"));
		assertEquals("O", StubUtility.trimFieldName("fO"));
		assertEquals("MyStatic", StubUtility.trimFieldName("sMyStatic"));
		assertEquals("MyMember", StubUtility.trimFieldName("mMyMember"));
		
		assertEquals("8", StubUtility.trimFieldName("_8"));
		
		assertEquals("8bar", StubUtility.trimFieldName("_8bar_"));
		assertEquals("8bar_8", StubUtility.trimFieldName("_8bar_8"));
		assertEquals("8bAr", StubUtility.trimFieldName("_8bAr"));
		assertEquals("b8Ar", StubUtility.trimFieldName("_b8Ar"));
		
		assertEquals("Id", StubUtility.trimFieldName("Id"));
		assertEquals("ID", StubUtility.trimFieldName("ID"));
		assertEquals("IDS", StubUtility.trimFieldName("IDS"));
		assertEquals("ID", StubUtility.trimFieldName("bID"));
		assertEquals("Id", StubUtility.trimFieldName("MId"));
		assertEquals("IdA", StubUtility.trimFieldName("IdA"));
	}
}
