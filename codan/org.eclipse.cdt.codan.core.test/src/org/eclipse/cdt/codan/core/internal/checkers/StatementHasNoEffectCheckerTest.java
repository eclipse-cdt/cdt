/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.StatementHasNoEffectChecker;
import org.eclipse.core.resources.IMarker;

/**
 * Test for {@see StatementHasNoEffectChecker} class
 * 
 */
public class StatementHasNoEffectCheckerTest extends CheckerTestCase {
	// main() {
	// int a;
	// +a; // error here on line 3
	// }
	public void testUnaryExpression() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// main() {
	// int a,b;
	//
	// b+a; // error here on line 4
	// }
	public void testBinaryExpression() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// main() {
	// int a,b;
	//
	// a=b+a; // no error here
	// }
	public void testNormalAssignment() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// main() {
	// int a,b;
	//
	// (a=b); // no errors here
	// a+=b;
	// a<<=b;
	// a-=b;
	// a++;
	// b--;
	// --a;
	// ++b;
	// a%=2;
	// a>>=2;
	// }
	public void testFalsePositives() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// main() {
	// int a;
	// a; // error here on line 3
	// }
	public void testIdExpression() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// main() {
	// int a=({foo();a;}); // no error here on line 2
	// }
	public void testGNUExpressionCompoundStmt() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	/* first file */
	// main() {
	// int a;
	// +a; // error here on line 3
	// }
	/* second file */
	// foo() {
	// int a;
	//
	// +a; // error here on line 4
	// }
	/* this test is using two files */
	public void test2FilesUnaryExpression() throws IOException {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkErrorLine(f1, 3);
		checkErrorLine(f2, 4);
	}

	// main() {
	// 	for (a=b;a;a=a->next);
	// }
	public void testForTestExpression() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// bool a;
	// class c {};
	// c z;
	// 	 (a = z.foo(1)) || (a = z.foo(2));
	// }
	public void testLazyEvalHack() {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// main() {
	// A a,b;
	//
	// b+=a; // error here on line 4
	// }
	public void testOverloadedBinaryExpression() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//#define FUNC(a) a
	// main() {
	// int a;
	//   FUNC(a); // error by default 
	// }
	@SuppressWarnings("restriction")
	public void testInMacro() {
		IProblemPreference macro = getPreference(
				StatementHasNoEffectChecker.ER_ID,
				StatementHasNoEffectChecker.PARAM_MACRO_ID);
		macro.setValue(Boolean.TRUE);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	//#define FUNC(a) a
	// main() {
	// int x;
	//   FUNC(x); //  error
	// }
	public void testMessageInMacro() throws IOException {
		loadCodeAndRun(getAboveComment());
		IMarker m = checkErrorLine(4);
		assertMessageMatch("'FUNC(x)'", m); //$NON-NLS-1$
	}

	//#define FUNC(a) a
	// main() {
	// int a;
	//   FUNC(a); // no error if macro exp turned off
	// }
	@SuppressWarnings("restriction")
	public void testInMacroParamOff() {
		IProblemPreference macro = getPreference(
				StatementHasNoEffectChecker.ER_ID,
				StatementHasNoEffectChecker.PARAM_MACRO_ID);
		macro.setValue(Boolean.FALSE);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// main() {
	// int a;
	// +a; // error here on line 3
	// }
	public void testMessage() throws IOException {
		loadCodeAndRun(getAboveComment());
		IMarker m = checkErrorLine(3);
		assertMessageMatch("'\\+a'", m); //$NON-NLS-1$
	}
}
