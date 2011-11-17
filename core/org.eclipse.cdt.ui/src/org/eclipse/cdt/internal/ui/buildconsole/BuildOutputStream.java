/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *     Alex Collins (Broadcom Corp.) - Global build console
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import java.io.IOException;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ProblemMarkerInfo;

import org.eclipse.cdt.internal.core.IErrorMarkeredOutputStream;

/**
 * Output stream which put all output to BuildConsolePartitioner
 * and informs it when stream is closed
 */
public class BuildOutputStream extends ConsoleOutputStream implements IErrorMarkeredOutputStream {

	final BuildConsoleStreamDecorator fStream;
	private BuildConsolePartitioner fPartitioner;

	public BuildOutputStream(BuildConsolePartitioner partitioner,
			BuildConsoleStreamDecorator stream) {
		fPartitioner = partitioner;
		if (fPartitioner.getProject() == null)
			// Note: The global console log stream should have been
			// opened by BuildConsoleManager.startGlobalConsole()
			fPartitioner.setStreamAppend();
		else
			fPartitioner.setStreamOpened();
		fStream = stream;
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
		flush();
		fPartitioner.setStreamClosed();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		fPartitioner.appendToDocument(new String(b, off, len), fStream, null);
	}

	@Override
	public void write(String s, ProblemMarkerInfo marker) throws IOException {
		fPartitioner.appendToDocument(s, fStream, marker);

	}

}
