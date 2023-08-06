/*******************************************************************************
 * Copyright (c) 2007, 2023 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     John Dallaway - support both IArchive and IBinary as input (#413)
 *     John Dallaway - provide hex dump when no GNU tool factory (#416)
 *     John Dallaway - rework to use a FileDocumentProvider (#425)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.commons.io.HexDump;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * A readonly editor to view binary files. This default implementation displays the GNU objdump output of the
 * binary as plain text. If no objdump output can be obtained, a hex dump is displayed.
 */
public class DefaultBinaryFileEditor extends TextEditor {

	/**
	 * A file document provider for binary files.
	 */
	public static class BinaryFileDocumentProvider extends FileDocumentProvider {

		private static final String CONTENT_TRUNCATED_MESSAGE_FORMAT = "\n--- {0} ---\n"; //$NON-NLS-1$

		private InputStream getInputStream(Consumer<OutputStream> writer) throws IOException {
			final AtomicReference<IOException> writerException = new AtomicReference<>();
			final PipedInputStream pipedInputStream = new PipedInputStream();
			final FilterInputStream filterInputStream = new FilterInputStream(pipedInputStream) {
				@Override
				public void close() throws IOException {
					try {
						final IOException exception = writerException.get();
						if (exception != null) {
							// propagate pipe writer exception to pipe reader
							throw new IOException(exception.getMessage(), exception);
						}
					} finally {
						super.close();
					}
				}
			};
			final OutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
			new Thread(() -> {
				try (pipedOutputStream) {
					writer.accept(pipedOutputStream);
				} catch (UncheckedIOException e) {
					writerException.set(e.getCause());
				} catch (IOException e) {
					writerException.set(e);
				}
			}).start();
			return filterInputStream;
		}

		private void writeObjdump(Objdump objdump, OutputStream outputStream) {
			try (InputStream objdumpStream = objdump.getInputStream()) {
				int offset = 0;
				while (true) {
					// read objdump content via 4 KiB buffer
					final byte[] buffer = objdumpStream.readNBytes(4096);
					if (0 == buffer.length) { // end of file stream
						break;
					}
					// limit to 16 MiB objdump content
					if (offset >= 0x1000000) {
						// append a message for user
						String message = "\n" + MessageFormat.format(CONTENT_TRUNCATED_MESSAGE_FORMAT, //$NON-NLS-1$
								CEditorMessages.DefaultBinaryFileEditor_TruncateMessage) + objdump.toString();
						outputStream.write(message.getBytes());
						break;
					}
					outputStream.write(buffer);
					offset += buffer.length;
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private void writeHexDump(IPath filePath, OutputStream outputStream) {
			final int BYTES_PER_LINE = 16; // hard-coded in HexDump class - do not modify
			try (InputStream fileStream = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
				int offset = 0;
				while (true) {
					// read data for 256 complete lines of hex dump output (4 KiB buffer)
					final byte[] buffer = fileStream.readNBytes(BYTES_PER_LINE * 256);
					if (0 == buffer.length) { // end of file stream
						break;
					}
					// limit to 16 MiB binary file content
					if (offset >= 0x1000000) {
						// append a message for user
						String message = MessageFormat.format(CONTENT_TRUNCATED_MESSAGE_FORMAT,
								CEditorMessages.DefaultBinaryFileEditor_TruncateHexDumpMessage);
						outputStream.write(message.getBytes());
						break;
					}
					HexDump.dump(buffer, offset, outputStream, 0);
					offset += buffer.length;
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private InputStream getBinaryFileContent(IBinaryFile binaryFile) throws CoreException {
			try {
				IPath filePath = binaryFile.getPath();
				IGnuToolFactory factory = binaryFile.getBinaryParser().getAdapter(IGnuToolFactory.class);
				if (factory != null) {
					Objdump objdump = factory.getObjdump(filePath);
					if (objdump != null) {
						// use output from objdump tool
						return getInputStream(stream -> writeObjdump(objdump, stream));
					}
				}
				// fall back to a hex dump if objdump tool not available
				return getInputStream(stream -> writeHexDump(filePath, stream));
			} catch (IOException e) {
				String message = (e.getMessage() != null ? e.getMessage() : ""); //$NON-NLS-1$
				throw new CoreException(Status.error(message, e));
			}
		}

		@Override
		protected boolean setDocumentContent(IDocument document, IEditorInput editorInput, String encoding)
				throws CoreException {
			if (editorInput instanceof IFileEditorInput fileEditorInput) {
				IFile file = fileEditorInput.getFile();
				ICElement cElement = CoreModel.getDefault().create(file);
				if (cElement != null) {
					IBinaryFile binaryFile = cElement.getAdapter(IBinaryFile.class);
					if (binaryFile != null) {
						setDocumentContent(document, getBinaryFileContent(binaryFile), encoding);
						return true;
					}
				}
			}
			return super.setDocumentContent(document, editorInput, encoding);
		}

		/*
		 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#isModifiable(java.lang.Object)
		 */
		@Override
		public boolean isModifiable(Object element) {
			return false;
		}

		/*
		 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#isReadOnly(java.lang.Object)
		 */
		@Override
		public boolean isReadOnly(Object element) {
			return true;
		}

	}

	public DefaultBinaryFileEditor() {
		super();
		setDocumentProvider(new BinaryFileDocumentProvider());
	}

}
