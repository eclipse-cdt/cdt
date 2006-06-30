/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.viewsupport;

import junit.framework.TestSuite;

public class ViewSupportTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new ViewSupportTestSuite();
    }
    
    public ViewSupportTestSuite() {
        super("View support tests");
        addTestSuite(AsyncViewerTest.class);
    }
}
