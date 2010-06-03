/*******************************************************************************
 *  Copyright (c) 2009, 2010 Nokia and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      Ken Ryall (Nokia) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.executables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ISourceFilesProvider;
import org.eclipse.cdt.internal.core.model.BinaryParserConfig;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

public class StandardSourceFilesProvider extends PlatformObject implements ISourceFilesProvider {

	public IBinaryFile createBinaryFile(Executable executable) {
		CModelManager factory = CModelManager.getDefault();

		IResource resource = executable.getResource();
		IPath path = executable.getPath();

		if (resource != null && resource instanceof IFile)
			return factory.createBinaryFile((IFile) resource);

		BinaryParserConfig[] parsers = factory.getBinaryParser(executable.getProject());
		if (parsers.length == 0) {
			return null;
		}

		if (!Executable.isExecutableFile(executable.getPath()))
			return null;

		File f = new File(path.toOSString());
		if (f.length() == 0) {
			return null;
		}

		int hints = 0;

		for (int i = 0; i < parsers.length; i++) {
			IBinaryParser parser = null;
			try {
				parser = parsers[i].getBinaryParser();
				if (parser.getHintBufferSize() > hints) {
					hints = parser.getHintBufferSize();
				}
			} catch (CoreException e) {
			}
		}
		byte[] bytes = new byte[hints];
		if (hints > 0) {
			InputStream is = null;
			try {
				is = new FileInputStream(path.toFile());
				int count = 0;
				// Make sure we read up to 'hints' bytes if we possibly can
				while (count < hints) {
					int bytesRead = is.read(bytes, count, hints - count);
					if (bytesRead < 0)
						break;
					count += bytesRead;
				}
				if (count > 0 && count < bytes.length) {
					byte[] array = new byte[count];
					System.arraycopy(bytes, 0, array, 0, count);
					bytes = array;
				}
			} catch (IOException e) {
				return null;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		for (int i = 0; i < parsers.length; i++) {
			try {
				IBinaryParser parser = parsers[i].getBinaryParser();
				if (parser.isBinary(bytes, path)) {
					IBinaryFile binFile = parser.getBinary(bytes, path);
					if (binFile != null) {
						return binFile;
					}
				}
			} catch (IOException e) {
			} catch (CoreException e) {
			}
		}
		return null;
	}

	public String[] getSourceFiles(Executable executable, IProgressMonitor monitor) {

		IBinaryFile bin = createBinaryFile(executable);
		if (bin != null) {
			ISymbolReader symbolreader = (ISymbolReader) bin.getAdapter(ISymbolReader.class);
			if (symbolreader != null) {
				return symbolreader.getSourceFiles(monitor);
			}

		}
		return new String[0];
	}

	public int getPriority(Executable executable) {
		return ISourceFilesProvider.NORMAL_PRIORITY;
	}

}
