/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.pda.service.command;

import java.io.File;

import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class Test1 extends CommandControlTestsBase {

	@BeforeClass
	public static void setProgram() {
		File programFile = PDAPlugin.getFileInPlugin(new Path("samples/example.pda"));
		fProgram = programFile.getPath();
	}

	@Test
	public void testRun() throws Throwable {
		sendCommand("vmresume");
		expectOutput("\"hello\"");
		expectOutput("\"barfoo\"");
		expectOutput("\"first\"");
		expectOutput("\"second\"");
		expectOutput("12");
		expectOutput("11");
		expectOutput("10");
		expectOutput("\"barfoo\"");
		expectOutput("\"first\"");
		expectOutput("\"second\"");
		expectOutput("\"end\"");
	}
}
