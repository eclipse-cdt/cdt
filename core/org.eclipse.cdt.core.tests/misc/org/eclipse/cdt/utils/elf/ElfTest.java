/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.utils.elf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.cdt.utils.elf.Elf.Section;
import org.eclipse.cdt.utils.elf.Elf.Symbol;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Elf reader test
 * @author Matthew Khouzam
 */
public class ElfTest extends TestCase {

	private static final String ARCH_X86_64 = "x86-64";
	private static final Map<String, String> PATHS = Collections.singletonMap(ARCH_X86_64,
			"resources/elf/unit_test/simple-x86-64");
	private final Map<String, Elf> elves = new HashMap<>();

	public static Test suite() {
		return new TestSuite(ElfTest.class);
	}

	public ElfTest() throws IOException {
		for (Entry<String, String> path : PATHS.entrySet()) {
			elves.put(path.getKey(), new Elf(path.getValue()));
		}
	}

	public void testGetSectionsX8664() throws IOException {
		Elf elf = elves.get(ARCH_X86_64);
		assertEquals("Number of sections", 36, elf.getSections().length);
		Section sectionByName = elf.getSectionByName(".symtab");
		assertNotNull(sectionByName);
		assertEquals("symbol table", ".symtab", sectionByName.toString());
		assertEquals("binary address", "0x0000000000000000", sectionByName.sh_addr.toHexAddressString());
		assertEquals("sh_name", 1, sectionByName.sh_name);
		sectionByName = elf.getSectionByName(".dynsym");
		assertNotNull(sectionByName);
		assertEquals("dynamic symbols", ".dynsym", sectionByName.toString());
		assertEquals("binary address", "0x00000000004002b8", sectionByName.sh_addr.toHexAddressString());
		assertEquals("sh_name", 78L, sectionByName.sh_name);
	}

	/**
	 * Test getting symbols
	 * @throws IOException
	 */
	public void testGetSymbolsX8664() throws IOException {
		Elf elf = elves.get(ARCH_X86_64);
		Section sectionByName = elf.getSectionByName(".symtab");
		assertNotNull(sectionByName);
		// never call Elf#LoadSymbols before this point
		assertNull(elf.getSymbols());
		elf.loadSymbols();
		Symbol[] symbols = elf.getSymbols();
		assertNotNull(symbols);
		assertEquals("Symbol Comparisson", Arrays.asList("", "", "", "", "", "", "crtstuff.c", "simple.c", "crtstuff.c",
				"", "_ITM_deregisterTMCloneTable", "__libc_start_main@@GLIBC_2.2.5", "__gmon_start__",
				"_Jv_RegisterClasses", "_ITM_registerTMCloneTable", "", "", "", "", "", "", "", "", "", "", "", "_init",
				"", "", "", "_start", "deregister_tm_clones", "register_tm_clones", "__do_global_dtors_aux",
				"frame_dummy", "function", "main", "__libc_csu_init", "__libc_csu_fini", "", "_fini", "",
				"_IO_stdin_used", "", "", "__FRAME_END__", "", "__frame_dummy_init_array_entry", "",
				"__do_global_dtors_aux_fini_array_entry", "", "__JCR_LIST__", "__JCR_END__", "", "_DYNAMIC", "", "",
				"_GLOBAL_OFFSET_TABLE_", "", "data_start", "__data_start", "__dso_handle", "", "completed.7594",
				"_edata", "__bss_start", "__TMC_END__", "_end"),
				Arrays.asList(symbols).stream().map(Symbol::toString).collect(Collectors.toList()));
		Symbol symbol = null;
		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i].toString().equals("main")) {
				symbol = symbols[i];
				break;
			}
		}
		assertNotNull(symbol);
		assertEquals("Main address", "0x00000000004004e4", symbol.st_value.toHexAddressString());
	}

}
