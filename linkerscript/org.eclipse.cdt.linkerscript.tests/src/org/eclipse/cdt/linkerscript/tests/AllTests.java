/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FullLinkerScriptFilesTest.class, NumbersTest.class, LinkerScriptParsingTest.class,
		LinkerScriptParseErrorTest.class, ExpressionValidTest.class, ExpressionInValidTest.class,
		ExpressionReducerTest.class, EditSectionTest.class, SerializerTest.class, FormatterTest.class })
public class AllTests {

}
