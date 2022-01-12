/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.memory.tests;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.memory.transport.ExportRequest;
import org.eclipse.cdt.debug.core.memory.transport.ImportRequest;
import org.eclipse.cdt.debug.internal.core.memory.transport.RAWBinaryExport;
import org.eclipse.cdt.debug.internal.core.memory.transport.RAWBinaryImport;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

public final class RAWBinaryTransportTest {

	private final BigInteger base;
	private final BigInteger start;

	public RAWBinaryTransportTest() {
		base = new BigInteger("1000"); //$NON-NLS-1$
		start = new BigInteger("0"); //$NON-NLS-1$
	}

	@Test
	public void transport0ffff() throws CoreException, IOException {
		BigInteger end = start.add(new BigInteger("0ffff", 16)); //$NON-NLS-1$
		transport("memory_0ffff.bin", end); //$NON-NLS-1$
	}

	@Test
	public void transport10000() throws CoreException, IOException {
		BigInteger end = start.add(new BigInteger("10000", 16)); //$NON-NLS-1$
		transport("memory_10000.bin", end); //$NON-NLS-1$
	}

	@Test
	public void transport10001() throws CoreException, IOException {
		BigInteger end = start.add(new BigInteger("10001", 16)); //$NON-NLS-1$
		transport("memory_10001.bin", end); //$NON-NLS-1$
	}

	private void transport(String name, BigInteger end) throws CoreException, IOException {
		EmulateMemory memory = new EmulateMemory(BigInteger.valueOf(1), base);
		CollectScrolls scroll = new CollectScrolls();
		File input = new InputFile(name).get();
		new RAWBinaryImport(input, new ImportRequest(base, start, memory), scroll)//
				.run(new NullProgressMonitor());
		File output = new OutputFile(name).get();
		new RAWBinaryExport(output, new ExportRequest(start, end, BigInteger.ONE, memory))//
				.run(new NullProgressMonitor());
		Assert.assertArrayEquals(read(input), read(output));
		Assert.assertEquals(Arrays.asList(start), scroll.collected());
	}

	private byte[] read(File file) throws IOException {
		return Files.readAllBytes(Paths.get(file.toString()));
	}

}
