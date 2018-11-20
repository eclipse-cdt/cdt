/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
package org.eclipse.cdt.core.parser.upc.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomatedIntegrationSuite extends TestSuite {

	public static Test suite() {
		return new TestSuite() {
			{
				addTestSuite(UPCCommentTests.class);
				addTestSuite(UPCCompletionBasicTest.class);
				addTestSuite(UPCCompletionParseTest.class);
				addTestSuite(UPCDOMLocationMacroTests.class);
				addTestSuite(UPCDOMLocationTests.class);
				addTestSuite(UPCDOMPreprocessorInformationTest.class);
				addTestSuite(UPCKnRTests.class);
				addTestSuite(UPCSelectionParseTest.class);
				addTestSuite(UPCCSpecTests.class);
				addTestSuite(UPCTests.class);
				addTestSuite(UPCLanguageExtensionTests.class);
				addTestSuite(UPCDigraphTrigraphTests.class);
				addTestSuite(UPCGCCTests.class);
				addTestSuite(UPCUtilOldTests.class);
				addTestSuite(UPCUtilTests.class);
				addTestSuite(UPCCompleteParser2Tests.class);
				addTestSuite(UPCTaskParserTest.class);
			}
		};
	}
}
