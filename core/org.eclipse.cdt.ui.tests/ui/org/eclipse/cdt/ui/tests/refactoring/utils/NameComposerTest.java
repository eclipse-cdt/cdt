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

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.util.NameComposer;

public class NameComposerTest extends TestCase {
	private static final int CAPITALIZATION_ORIGINAL = PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL;
	private static final int CAPITALIZATION_UPPER_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_UPPER_CASE;
	private static final int CAPITALIZATION_LOWER_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CASE;
	private static final int CAPITALIZATION_CAMEL_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE;
	private static final int CAPITALIZATION_LOWER_CAMEL_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE;
	
	public void testTrimFieldName() {
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
}
