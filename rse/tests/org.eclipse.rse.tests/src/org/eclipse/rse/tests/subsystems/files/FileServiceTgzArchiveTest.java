/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Johnson Ma (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

/**
 * this class is used to test tgz and .tar.gz archive function
 */
public class FileServiceTgzArchiveTest extends FileServiceArchiveBaseTest {

	public FileServiceTgzArchiveTest(String name) {
		super(name);
		//-test-author-:JohnsonMa:9
		tarSourceFileName1 = "source.tar.gz";
		tarSourceFileName2 = "mynewtar.tgz";
		tarSourceForOpenTest = "tarSourceForOpen.TAR.gz";
		testName = "dummy.tGz";
	}

}
