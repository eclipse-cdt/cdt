/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text.selection;

import junit.framework.TestSuite;

public class SelectionTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new SelectionTestSuite();
    }
    
    public SelectionTestSuite() {
        super(SelectionTestSuite.class.getName());
        
        // selection tests
        addTest(ResolveBindingTests.suite());
        addTest(CPPSelectionTestsNoIndexer.suite());
		addTest(CSelectionTestsNoIndexer.suite());
		addTest(CPPSelectionTestsFastIndexer.suite());
		addTest(CSelectionTestsFastIndexer.suite());
    }
}
