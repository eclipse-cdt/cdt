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

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.CatchByReference;

/**
 * Test for {@see CatchByReference} class
 * 
 */
public class CatchByReferenceTest extends CheckerTestCase {
	@Override
	public boolean isCpp() {
		return true;
	}

	//	void main() {
	//		try {
	//			foo();
	//		} catch (int e) {
	//		}
	//	}
	public void test_int() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {};	
	// void main() {
	//		try {
	//			foo();
	//		} catch (C e) {
	//		}
	//	}
	public void test_class() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

	// class C {};	
	// void main() {
	//		try {
	//			foo();
	//		} catch (C & e) {
	//		}
	//	}
	public void test_class_ref() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {};	
	// void main() {
	//		try {
	//			foo();
	//		} catch (C * e) {
	//		}
	//	}
	public void test_class_point() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// typedef int A;
	// void main() {
	//		try {
	//			foo();
	//		} catch (A e) {
	//		}
	//	}
	public void test_int_typedef() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// typedef int A;
	// typedef A B;
	//	void main() {
	//		try {
	//			foo();
	//		} catch (B e) {
	//		}
	//	}
	public void test_int_typedef2() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	//		try {
	//			foo();
	//		} catch (C e) {
	//		}
	//	}
	public void test_class_unknown() {
		setPreferenceValue(CatchByReference.ER_ID,
				CatchByReference.PARAM_UNKNOWN_TYPE, false);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	//		try {
	//			foo();
	//		} catch (C e) {
	//		}
	//	}
	public void test_class_unknown_on() {
		setPreferenceValue(CatchByReference.ER_ID,
				CatchByReference.PARAM_UNKNOWN_TYPE, true);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// class C {};
	// typedef C B;
	// void main() {
	//		try {
	//			foo();
	//		} catch (B e) {
	//		}
	//	}
	public void test_class_typedef() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6);
	}
}
