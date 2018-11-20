/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class XlcExtensionsTestSuite extends TestSuite {

	public static Test suite() {
		return new TestSuite() {
			{
				addTestSuite(VectorExtensionsTest.class);
				addTestSuite(XlcExtensionsTest.class);
			}
		};
	}
}