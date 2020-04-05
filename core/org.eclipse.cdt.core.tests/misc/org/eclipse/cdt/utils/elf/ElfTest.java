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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.utils.elf.Elf.Section;
import org.eclipse.cdt.utils.elf.Elf.Symbol;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Elf reader test
 * @author Matthew Khouzam
 */
@RunWith(Parameterized.class)
public class ElfTest {

	private static final String SYMTAB_NAME = ".symtab";

	@Parameters(name = "{0}")
	public static Collection<Object[]> primeNumbers() {
		return Arrays.asList(new Object[][] {
				{ "BE32", "resources/elf/unit_test/simple-be32.elf", 35, "0x00000000", "0x100001a8", 75, "0x10000518" },
				{ "BE64", "resources/elf/unit_test/simple-be64.elf", 34, "0x0000000000000000", "0x0000000010000240", 69,
						"0x000000001001fea0" },
				{ "LE32", "resources/elf/unit_test/simple-le32.elf", 36, "0x00000000", "0x080481cc", 70, "0x080483e5" },
				{ "LE64", "resources/elf/unit_test/simple-le64.elf", 36, "0x0000000000000000", "0x00000000004002b8", 68,
						"0x00000000004004e4" }, });
	}

	private static final Collection<String> functions = Arrays.asList("", "crtstuff.c", "simple.c", "crtstuff.c",
			"_ITM_deregisterTMCloneTable", "__gmon_start__", "_Jv_RegisterClasses", "_ITM_registerTMCloneTable",
			"_init", "_start", "deregister_tm_clones", "register_tm_clones", "__do_global_dtors_aux", "frame_dummy",
			"function", "main", "__libc_csu_init", "__libc_csu_fini", "_fini", "_IO_stdin_used", "__FRAME_END__",
			"__JCR_LIST__", "__JCR_END__", "_DYNAMIC", "data_start", "__data_start", "__dso_handle", "_edata",
			"__bss_start", "__TMC_END__", "_end");

	private final String arch;
	private final Elf elf;
	private final int nbSections;
	private final String symtabBaseAddress;
	private final String dynsymBaseAddress;
	private final int nbSymbols;
	private final String mainAddress;

	public ElfTest(String architecture, String path, int sections, String symBaseAddress, String dynBaseAddress,
			int symbolCount, String mainAddr) throws IOException {
		nbSections = sections;
		elf = new Elf(path);
		arch = architecture;
		symtabBaseAddress = symBaseAddress;
		dynsymBaseAddress = dynBaseAddress;
		nbSymbols = symbolCount;
		mainAddress = mainAddr;
	}

	/**
	 * Test getting the sections
	 * @throws IOException
	 */
	@Test
	public void testGetSections() throws IOException {
		assertEquals(arch + ": " + "Number of sections", nbSections, elf.getSections().length);
		Section sectionByName = elf.getSectionByName(SYMTAB_NAME);
		assertNotNull(sectionByName);
		assertEquals(arch + ": " + "symbol table", SYMTAB_NAME, sectionByName.toString());
		assertEquals(arch + ": " + "binary address", symtabBaseAddress, sectionByName.sh_addr.toHexAddressString());
		assertEquals(arch + ": " + "sh_name", 1, sectionByName.sh_name);
		sectionByName = elf.getSectionByName(".dynsym");
		assertNotNull(sectionByName);
		assertEquals(arch + ": " + "dynamic symbols", ".dynsym", sectionByName.toString());
		assertEquals(arch + ": " + "binary address", dynsymBaseAddress, sectionByName.sh_addr.toHexAddressString());
		assertEquals(arch + ": " + "sh_name", 78L, sectionByName.sh_name);
	}

	/**
	 * Test getting symbols, this loads the symbols so it modifies the state of elf.
	 * @throws IOException
	 */
	@Test
	public void testGetSymbols() throws IOException {
		Section sectionByName = elf.getSectionByName(SYMTAB_NAME);
		assertNotNull(sectionByName);
		// never call Elf#LoadSymbols before this point
		assertNull(arch + ": " + "Null symbols", elf.getSymbols());
		elf.loadSymbols();
		Symbol[] symbols = elf.getSymbols();
		assertNotNull(arch + ": " + "Symbols are set", symbols);
		assertEquals(nbSymbols, symbols.length);
		List<String> functionList = Arrays.asList(symbols).stream().map(Symbol::toString).collect(Collectors.toList());
		for (String function : functions) {
			assertTrue(arch + ": " + "Symbols does not contain \"" + function + '"', functionList.contains(function));
		}
		Symbol symbol = null;
		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i].toString().equals("main")) {
				symbol = symbols[i];
				break;
			}
		}
		assertNotNull(symbol);
		assertEquals(arch + ": " + "Main address", mainAddress, symbol.st_value.toHexAddressString());
	}

}
