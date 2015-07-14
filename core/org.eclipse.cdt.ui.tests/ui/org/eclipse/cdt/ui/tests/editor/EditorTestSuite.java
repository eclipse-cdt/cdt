/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.editor;

import junit.framework.TestSuite;

/**
 * Tests for functionality in the package org.eclipse.cdt.internal.ui.editor. 
 */
public class EditorTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new EditorTestSuite();
    }
    
    public EditorTestSuite() {
        super(EditorTestSuite.class.getName());
		addTest(SourceHeaderPartnerFinderTest.suite());
    }
}
