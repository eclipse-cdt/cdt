/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		- Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_1.MIRegistersTest_7_1;
import org.eclipse.core.runtime.Platform;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIRegistersTest_7_2 extends MIRegistersTest_7_1 {
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_2);
	}
	
	// GDB's list of registers is different with GDB 7.2
	@Override
	protected List<String> get_X86_REGS() {
		List<String>  list = new LinkedList<String>(Arrays.asList("eax","ecx","edx","ebx","esp","ebp","esi","edi","eip","eflags",
																  "cs","ss","ds","es","fs","gs","st0","st1","st2","st3",
																  "st4","st5","st6","st7","fctrl","fstat","ftag","fiseg","fioff","foseg",
																  "fooff","fop","xmm0","xmm1","xmm2","xmm3","xmm4","xmm5","xmm6","xmm7",
																  "mxcsr",/*"","","","","","","","",*/"orig_eax",
																  "al","cl","dl","bl","ah","ch","dh","bh","ax","cx",
																  "dx","bx",/*"",*/"bp","si","di","mm0","mm1","mm2","mm3",
																  "mm4","mm5","mm6","mm7"));
		// On Windows, gdb doesn't report "orig_eax" as a register. Apparently it does on Linux
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
    		list.remove("orig_eax");
	    }
		return list;
	}

}
