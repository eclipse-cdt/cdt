/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hansruedi Patzen (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf;

import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.utils.elf.parser.ElfBinaryExecutable;
import org.eclipse.cdt.utils.elf.parser.ElfBinaryShared;
import org.eclipse.cdt.utils.elf.parser.ElfParser;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ElfParserTest extends TestCase {

	ElfParser elfParser;

	public static Test suite() {
		return new TestSuite(ElfParserTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		elfParser = new ElfParser();
		super.setUp();
	}

	private byte[] readHints(final IPath path) throws IOException {
		int bytesToRead = elfParser.getHintBufferSize();
		byte[] hints = new byte[bytesToRead];
		try (FileInputStream is = new FileInputStream(path.toFile())) {
			is.read(hints);
		}
		return hints;
	}

	/*
	 * The most probable cause for failing one of the following tests
	 *
	 * testLE64DYNwithInterpELFoutsideHintsError_Bug512822
	 * testLE32DYNwithInterpELFoutsideHintsError_Bug512822
	 *
	 * is that the hint buffer size has been changed and the test binaries
	 * are too small and need to be updated to reflect this change as well.
	 *
	 * The original files have been manually created using nasm. See the
	 * *.asm files inside resources/elf/{inside_hints,outside_hints}/ for
	 * further details.
	 */
	public void testLE64DYNwithInterpELFoutsideHintsError_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/le64");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertFalse("Binary should not be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testLE32DYNwithInterpELFoutsideHintsError_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/le32");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertFalse("Binary should not be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testLE64DYNwithInterpELFinsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/inside_hints/le64");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertTrue("Binary should be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testLE32DYNwithInterpELFinsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/inside_hints/le32");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertTrue("Binary should be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testLE64DYNwithInterpELFoutsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/le64");
		IBinaryFile binary = elfParser.getBinary(readHints(path), path);
		assertTrue("Binary should be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testLE32DYNwithInterpELFoutsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/le32");
		IBinaryFile binary = elfParser.getBinary(readHints(path), path);
		assertTrue("Binary should be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testLE32DYNnoInterpELFinsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/inside_hints/le32lib");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertTrue("Binary should be a library", binary instanceof ElfBinaryShared);
	}

	public void testLE64DYNnoInterpELFinsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/inside_hints/le64lib");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertTrue("Binary should be a library", binary instanceof ElfBinaryShared);
	}

	public void testLE32DYNnoInterpELFoutsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/le32lib");
		IBinaryFile binary = elfParser.getBinary(readHints(path), path);
		assertTrue("Binary should be a library", binary instanceof ElfBinaryShared);
	}

	public void testLE64DYNnoInterpELFoutsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/le64lib");
		IBinaryFile binary = elfParser.getBinary(readHints(path), path);
		assertTrue("Binary should be a library", binary instanceof ElfBinaryShared);
	}

	public void testBE64DYNwithInterpELFinsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/inside_hints/be64");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertTrue("Binary should be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testBE32DYNwithInterpELFinsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/inside_hints/be32");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertTrue("Binary should be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testBE64DYNwithInterpELFoutsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/be64");
		IBinaryFile binary = elfParser.getBinary(readHints(path), path);
		assertTrue("Binary should be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testBE32DYNwithInterpELFoutsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/be32");
		IBinaryFile binary = elfParser.getBinary(readHints(path), path);
		assertTrue("Binary should be an executable", binary instanceof ElfBinaryExecutable);
	}

	public void testBE32DYNnoInterpELFinsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/inside_hints/be32lib");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertTrue("Binary should be a library", binary instanceof ElfBinaryShared);
	}

	public void testBE64DYNnoInterpELFinsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/inside_hints/be64lib");
		IBinaryFile binary = elfParser.getBinary(readHints(path), new Path(""));
		assertTrue("Binary should be a library", binary instanceof ElfBinaryShared);
	}

	public void testBE32DYNnoInterpELFoutsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/be32lib");
		IBinaryFile binary = elfParser.getBinary(readHints(path), path);
		assertTrue("Binary should be a library", binary instanceof ElfBinaryShared);
	}

	public void testBE64DYNnoInterpELFoutsideHints_Bug512822() throws CModelException, IOException {
		IPath path = new Path("resources/elf/outside_hints/be64lib");
		IBinaryFile binary = elfParser.getBinary(readHints(path), path);
		assertTrue("Binary should be a library", binary instanceof ElfBinaryShared);
	}
}
