/*******************************************************************************
 * Copyright (c) 2020 Sergey Vladimirov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers.metrics;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.metrics.NestedBlockDepthChecker;

/**
 * Test for {@link NestedBlockDepthChecker} class
 */
public class NestedBlockDepthCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = NestedBlockDepthChecker.ER_NESTED_BLOCK_DEPTH_EXCEEDED_ID;

	@Override
	public boolean isCpp() {
		return false;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	//void foo() {
	//  if (true == false)
	//    if (true == false)
	//      if (true == false)
	//        if (true == false)
	//          if (true == false);
	//}
	public void testFiveBlocks() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//void foo() {
	//  if (true == false);
	//}
	public void testOneBlock() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//void foo() {
	//  if (true == false)
	//    if (true == false)
	//      if (true == false)
	//        if (true == false)
	//          if (true == false)
	//            if (true == false);
	//}
	public void testSixNestedBlocks() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(7);
	}

	//void foo() {
	//  if (true == false);
	//  if (true == false);
	//  if (true == false);
	//  if (true == false);
	//  if (true == false);
	//  if (true == false);
	//}
	public void testSixNonNestedBlocks() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

}
