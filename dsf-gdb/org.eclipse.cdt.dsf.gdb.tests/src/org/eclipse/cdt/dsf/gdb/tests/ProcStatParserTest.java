/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 396268)
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Split the dsf-gdb tests to a plug-in and fragment pair
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.dsf.gdb.internal.ProcStatCoreLoads;
import org.eclipse.cdt.dsf.gdb.internal.ProcStatParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcStatParserTest {

	final static String stat_t0 = "cpu  27599070 16857 1627173 178832624 958471 10 21253 0 0 0\n"
			+ "cpu0 7076626 3073 420740 44122942 620655 7 19123 0 0 0\n"
			+ "cpu1 6839475 2644 480003 44885633 53738 2 1200 0 0 0\n"
			+ "cpu2 6861775 9347 337505 44860715 195008 0 573 0 0 0\n"
			+ "cpu3 6821192 1792 388924 44963332 89067 0 355 0 0 0\n"
			+ "intr 255054962 1785 9 0 0 0 0 0 0 1 393 0 0 125 0 0 0 1861780 5056689 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3138534 3946219 2295808 199 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"
			+ "ctxt 406954066\n" + "btime 1357642511\n" + "processes 187587\n" + "procs_running 2\n"
			+ "procs_blocked 0\n"
			+ "softirq 187777133 0 82842161 104536 3977894 3827626 0 3881246 12353598 94844 80695228";
	final static String stat_t0_file = "/tmp/stat_t0";

	final static String stat_t1 = "cpu  27599216 16857 1627190 178835528 958483 10 21255 0 0 0\n"
			+ "cpu0 7076664 3073 420751 44123650 620668 7 19125 0 0 0\n"
			+ "cpu1 6839509 2644 480004 44886368 53738 2 1200 0 0 0\n"
			+ "cpu2 6861813 9347 337507 44861445 195008 0 573 0 0 0\n"
			+ "cpu3 6821229 1792 388926 44964063 89067 0 355 0 0 0\n"
			+ "intr 255057230 1785 9 0 0 0 0 0 0 1 393 0 0 125 0 0 0 1861874 5056997 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3138618 3946264 2295808 199 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"
			+ "ctxt 406958462\n" + "btime 1357642511\n" + "processes 187593\n" + "procs_running 5\n"
			+ "procs_blocked 0\n"
			+ "softirq 187779126 0 82842674 104538 3977978 3827690 0 3881346 12353760 94845 80696295";
	final static String stat_t1_file = "/tmp/stat_t1";

	final static String stat_t2 = "cpu  27602962 16857 1627282 178835528 958483 10 21256 0 0 0\n"
			+ "cpu0 7077593 3073 420781 44123650 620668 7 19126 0 0 0\n"
			+ "cpu1 6840413 2644 480060 44886368 53738 2 1200 0 0 0\n"
			+ "cpu2 6862773 9347 337507 44861445 195008 0 573 0 0 0\n"
			+ "cpu3 6822181 1792 388933 44964063 89067 0 355 0 0 0\n"
			+ "intr 255070028 1785 9 0 0 0 0 0 0 1 393 0 0 125 0 0 0 1861998 5057533 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3138674 3946472 2295808 199 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"
			+ "ctxt 407001757\n" + "btime 1357642511\n" + "processes 187607\n" + "procs_running 5\n"
			+ "procs_blocked 0\n"
			+ "softirq 187794229 0 82852274 104540 3978034 3827918 0 3881474 12354181 94845 80700963";
	final static String stat_t2_file = "/tmp/stat_t2";

	// to trigger exception upon parsing
	final static String stat_wrong_content = "cpu  27602962 16857 1627282 178835528 958483 10 21256 0 0 0\n"
			+ "cpu0 AAAAAAA 3073 420781 44123650 620668 7 19126 0 0 0\n"
			+ "cpu1 AAAAAAA 2644 480060 44886368 53738 2 1200 0 0 0\n"
			+ "cpu2 AAAAAAA 9347 337507 44861445 195008 0 573 0 0 0\n"
			+ "cpu3 AAAAAAA 1792 388933 44964063 89067 0 355 0 0 0\n"
			+ "intr 255070028 1785 9 0 0 0 0 0 0 1 393 0 0 125 0 0 0 1861998 5057533 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3138674 3946472 2295808 199 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"
			+ "ctxt 407001757\n" + "btime 1357642511\n" + "processes 187607\n" + "procs_running 5\n"
			+ "procs_blocked 0\n"
			+ "softirq 187794229 0 82852274 104540 3978034 3827918 0 3881474 12354181 94845 80700963";
	final static String stat_wrong_content_file = "/tmp/stat_wrong_content";

	@BeforeClass
	public static void init_once() {
		// generate test input files once at beginning of tests
		writeStr2File(stat_t0, stat_t0_file);
		writeStr2File(stat_t1, stat_t1_file);
		writeStr2File(stat_t2, stat_t2_file);
		writeStr2File(stat_wrong_content, stat_wrong_content_file);
	}

	@AfterClass
	public static void cleanup() {
		// cleanup at end of tests
		new File(stat_t0_file).delete();
		new File(stat_t1_file).delete();
		new File(stat_t2_file).delete();
		new File(stat_wrong_content_file).delete();
	}

	// testcases

	@Test
	public void testProcStatParse1() throws Exception {
		ProcStatParser procStatParser = new ProcStatParser();
		procStatParser.parseStatFile(stat_t0_file);
		procStatParser.parseStatFile(stat_t1_file);
		ProcStatCoreLoads load = procStatParser.getCpuLoad();

		int l0 = (int) load.getLoad("0").floatValue();
		assertEquals(8, l0);
		int l1 = (int) load.getLoad("1").floatValue();
		assertEquals(4, l1);
		int l2 = (int) load.getLoad("2").floatValue();
		assertEquals(5, l2);
		int l3 = (int) load.getLoad("3").floatValue();
		assertEquals(5, l3);
	}

	@Test
	public void testProcStatParse2() throws Exception {
		ProcStatParser procStatParser = new ProcStatParser();
		procStatParser.parseStatFile(stat_t1_file);
		procStatParser.parseStatFile(stat_t2_file);
		ProcStatCoreLoads load = procStatParser.getCpuLoad();

		int l0 = (int) load.getLoad("0").floatValue();
		assertEquals(100, l0);
		int l1 = (int) load.getLoad("1").floatValue();
		assertEquals(100, l1);
		int l2 = (int) load.getLoad("2").floatValue();
		assertEquals(100, l2);
		int l3 = (int) load.getLoad("3").floatValue();
		assertEquals(100, l3);
	}

	@Test
	public void testProcStatParse3() throws Exception {
		ProcStatParser procStatParser = new ProcStatParser();
		procStatParser.parseStatFile(stat_t0_file);
		procStatParser.parseStatFile(stat_t2_file);
		ProcStatCoreLoads load = procStatParser.getCpuLoad();

		int l0 = (int) load.getLoad("0").floatValue();
		assertEquals(59, l0);
		int l1 = (int) load.getLoad("1").floatValue();
		assertEquals(57, l1);
		int l2 = (int) load.getLoad("2").floatValue();
		assertEquals(57, l2);
		int l3 = (int) load.getLoad("3").floatValue();
		assertEquals(57, l3);
	}

	@Test
	public void testProcStatParseOneSetOfCounters() throws Exception {
		ProcStatParser procStatParser = new ProcStatParser();
		procStatParser.parseStatFile(stat_t0_file);
		ProcStatCoreLoads load = procStatParser.getCpuLoad();

		int l0 = (int) load.getLoad("0").floatValue();
		assertEquals(15, l0);
		int l1 = (int) load.getLoad("1").floatValue();
		assertEquals(14, l1);
		int l2 = (int) load.getLoad("2").floatValue();
		assertEquals(14, l2);
		int l3 = (int) load.getLoad("3").floatValue();
		assertEquals(13, l3);
	}

	@Test(expected = FileNotFoundException.class)
	public void testStatFileDoesNotExist() throws Exception {
		ProcStatParser procStatParser = new ProcStatParser();
		// read non-existing stat file
		procStatParser.parseStatFile("/file/does/not/exist");
	}

	@Test(expected = NumberFormatException.class)
	public void testStatFileDoesntParse() throws Exception {
		ProcStatParser procStatParser = new ProcStatParser();
		// read non-existing stat file
		procStatParser.parseStatFile(stat_wrong_content_file);
	}

	// util functions

	private static void writeStr2File(String str, String fileName) {
		FileWriter fileWriter = null;
		File f = new File(fileName);
		try {
			fileWriter = new FileWriter(f);
			fileWriter.write(str);
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
