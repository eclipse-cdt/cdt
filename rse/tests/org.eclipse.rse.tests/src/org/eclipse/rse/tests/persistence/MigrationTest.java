/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * Martin Oberhuber (Wind River) - [240729] More flexible disabling of testcases
 *********************************************************************************/

package org.eclipse.rse.tests.persistence;

import org.eclipse.rse.tests.core.RSECoreTestCase;

/**
 *
 */
public class MigrationTest extends RSECoreTestCase {

	public MigrationTest(String name) {
		super(name);
	}

	public void testProfileMigration() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		// create a new profile
		// set its persistence manager to PM1
		// populate the profile
		// migrate profile to PM2
		// test for migration
		// ensure that the old profile has been locked
	}

}
