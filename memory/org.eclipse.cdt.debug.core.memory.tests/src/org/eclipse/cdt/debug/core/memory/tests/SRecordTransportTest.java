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
import java.util.List;

import org.eclipse.cdt.debug.core.memory.transport.ExportRequest;
import org.eclipse.cdt.debug.core.memory.transport.ImportRequest;
import org.eclipse.cdt.debug.internal.core.memory.transport.SRecordExport;
import org.eclipse.cdt.debug.internal.core.memory.transport.SRecordImport;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

public final class SRecordTransportTest {

	private final BigInteger base;
	private final BigInteger start;

	public SRecordTransportTest() {
		base = new BigInteger("1000"); //$NON-NLS-1$
		start = new BigInteger("00601060", 16); //$NON-NLS-1$
	}

	@Test
	public void transport0ffff() throws CoreException, IOException {
		BigInteger end = start.add(new BigInteger("0ffff", 16)); //$NON-NLS-1$
		transport("memory_0ffff.srec", end); //$NON-NLS-1$
	}

	@Test
	public void transport10000() throws CoreException, IOException {
		BigInteger end = start.add(new BigInteger("10000", 16)); //$NON-NLS-1$
		transport("memory_10000.srec", end); //$NON-NLS-1$
	}

	@Test
	public void transport10001() throws CoreException, IOException {
		BigInteger end = start.add(new BigInteger("10001", 16)); //$NON-NLS-1$
		transport("memory_10001.srec", end); //$NON-NLS-1$
	}

	private void transport(String name, BigInteger end) throws CoreException, IOException {
		EmulateMemory memory = new EmulateMemory(BigInteger.valueOf(1), base);
		CollectScrolls scroll = new CollectScrolls();
		File input = new InputFile(name).get();
		File output = new OutputFile(name).get();
		new SRecordImport(input, new ImportRequest(base, start, memory), scroll, true)//
				.run(new NullProgressMonitor());
		new SRecordExport(output, new ExportRequest(start, end, BigInteger.ONE, memory))//
				.run(new NullProgressMonitor());
		Assert.assertArrayEquals(read(input), read(output));
		Assert.assertEquals(Arrays.asList(start), scroll.collected());

		scroll = new CollectScrolls();
		new SRecordImport(input, new ImportRequest(base, start, memory), scroll, false)//
				.run(new NullProgressMonitor());
		new SRecordExport(output, new ExportRequest(start, end, BigInteger.ONE, memory))//
				.run(new NullProgressMonitor());
		Assert.assertArrayEquals(read(input), read(output));
		Assert.assertEquals(Arrays.asList(start), scroll.collected());
	}

	private String[] read(File file) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(file.toString()));
		return lines.toArray(new String[lines.size()]);
	}

}
