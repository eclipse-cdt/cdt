/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
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
package org.eclipse.cdt.codan.core.internal.checkers;

import java.io.File;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.StatementHasNoEffectChecker;
import org.eclipse.core.resources.IMarker;

/**
 * Test for {@see StatementHasNoEffectChecker} class
 */
public class StatementHasNoEffectCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(StatementHasNoEffectChecker.ER_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	// int main() {
	// int a;
	// +a; // error here on line 3
	// }
	public void testUnaryExpression() throws Exception {
		checkSampleAbove();
	}

	// int main() {
	// int a,b;
	//
	// b+a; // error here on line 4
	// }
	public void testBinaryExpression() throws Exception {
		checkSampleAbove();
	}

	// int main() {
	// int a,b;
	//
	// a=b+a; // no error here
	// }
	public void testNormalAssignment() throws Exception {
		checkSampleAbove();
	}

	// int main() {
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
	public void testFalsePositives() throws Exception {
		checkSampleAbove();
	}

	// int main() {
	// int a;
	// a; // error here on line 3
	// }
	public void testIdExpression() throws Exception {
		checkSampleAbove();
	}

	// int main() {
	// int a=({foo();a;}); // no error here on line 2
	// char *p=({char s[]="Some string";&s[0];}); // no error here on line 3
	// }
	public void testGNUExpressionCompoundStmtFalsePositives() throws Exception {
		checkSampleAbove();
	}

	// int main() {
	// int z=({int a=0; +a; a;}) // error here on line 2
	// }
	public void testGNUExpressionCompoundStmtInside() throws Exception {
		checkSampleAbove();
	}

	// int main() {
	// int a;
	// +a; // error here on line 3
	// }

	// void foo() {
	// int a;
	//
	// +a; // error here on line 4
	// }
	public void test2FilesUnaryExpression() throws Exception {
		/* This test is using two files */
		CharSequence[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkErrorLine(f1, 3);
		checkErrorLine(f2, 4);
	}

	// int main() {
	// 	for (a=b;a;a=a->next);
	// }
	public void testForTestExpression() throws Exception {
		checkSampleAbove();
	}

	// void main() {
	// bool a;
	// class c {};
	// c z;
	// 	 (a = z.foo(1)) || (a = z.foo(2));
	// }
	public void testLazyEvalHack() throws Exception {
		checkSampleAboveCpp();
	}

	// int main() {
	// A a,b;
	//
	// b+=a; // no error here on line 4
	// }
	public void testOverloadedBinaryExpression() throws Exception {
		checkSampleAboveCpp();
	}

	//#define FUNC(a) a
	// int main() {
	// int a;
	//   FUNC(a); // error by default
	// }
	public void testInMacro() throws Exception {
		IProblemPreference macro = getPreference(StatementHasNoEffectChecker.ER_ID,
				StatementHasNoEffectChecker.PARAM_MACRO_ID);
		macro.setValue(Boolean.TRUE);
		checkSampleAbove();
	}

	//#define FUNC(a) a
	// int main() {
	// int x;
	//   FUNC(x); //  error
	// }
	public void testMessageInMacro() throws Exception {
		loadCodeAndRun(getAboveComment());
		IMarker m = checkErrorLine(4);
		assertMessageMatch("'FUNC(x)'", m); //$NON-NLS-1$
	}

	//#define FUNC(a) a
	// int main() {
	// int a;
	//   FUNC(a); // no error if macro exp turned off
	// }
	public void testInMacroParamOff() throws Exception {
		IProblemPreference macro = getPreference(StatementHasNoEffectChecker.ER_ID,
				StatementHasNoEffectChecker.PARAM_MACRO_ID);
		macro.setValue(Boolean.FALSE);
		checkSampleAbove();
	}

	// int main() {
	// int a;
	// +a; // error here on line 3
	// }
	public void testMessage() throws Exception {
		loadCodeAndRun(getAboveComment());
		IMarker m = checkErrorLine(3);
		assertMessageMatch("'\\+a'", m); //$NON-NLS-1$
	}

	//	class S {
	//		int operator*();  // may have side effect
	//	};
	//
	//	int main() {
	//		S s;
	//		*s;
	//	}
	public void testOverloadedOperator_bug399146() throws Exception {
		checkSampleAboveCpp();
	}
}
