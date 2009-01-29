/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.outline;

import junit.framework.TestSuite;

public class OutlineTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new OutlineTestSuite();
    }
    
    public OutlineTestSuite() {
        super(OutlineTestSuite.class.getName());
		addTest(BasicOutlineTest.suite());
    }
}
