/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.IProblem;


/**
 * Scanner2Tests ported to use the CPreprocessor
 */
public class PreprocessorBugsTests extends PreprocessorTestsBase {
	
	public static TestSuite suite() {
		return suite(PreprocessorBugsTests.class);
	}

	// #define NOP(x)        x
	// #define CPUINC(cpu)   <NOP(reg)NOP(cpu).sfr>
	// #include CPUINC(xag4)
	public void testMacroInInclusion_Bug122891() throws Exception {
		initializeScanner();
		validateEOF();
		validateProblem(0, IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, "regxag4.sfr");
		validateProblemCount(1);
	}
	
	//	#define FUNKY(x) __##x##__
	//	#define __foo__ 127
	//
	//	#if FUNKY(foo) == 0x7f
	//	#define MSG "hello"
	//	#else
	//	#define MSG "goodbye"
	//	#endif
	//  MSG
	public void testTokenPaste_Bug210344() throws Exception {
		initializeScanner();
		validateString("hello");
		validateEOF();
		validateProblemCount(0);
	}
}
