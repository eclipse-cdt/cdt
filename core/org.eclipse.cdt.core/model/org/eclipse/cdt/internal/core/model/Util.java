package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class Util {

	private Util() {
	}

	public static StringBuffer getContent(IFile file) throws IOException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(file.getContents(true));
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
		try {
			char [] b = getInputStreamAsCharArray(stream, -1, null);
			return new StringBuffer(b.length).append(b);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Returns the given input stream's contents as a character array.
	 * If a length is specified (ie. if length != -1), only length chars
	 * are returned. Otherwise all chars in the stream are returned.
	 * Note this doesn't close the stream.
	 * @throws IOException if a problem occured reading the stream.
	 */
	public static char[] getInputStreamAsCharArray(InputStream stream, int length, String encoding)
		throws IOException {
		InputStreamReader reader = null;
		reader = encoding == null
			? new InputStreamReader(stream)
			: new InputStreamReader(stream, encoding);
		char[] contents;
		if (length == -1) {
			contents = new char[0];
			int contentsLength = 0;
			int charsRead = -1;
			do {
				int available = stream.available();

				// resize contents if needed
				if (contentsLength + available > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new char[contentsLength + available],
						0,
						contentsLength);
				}

				// read as many chars as possible
				charsRead = reader.read(contents, contentsLength, available);

				if (charsRead > 0) {
					// remember length of contents
					contentsLength += charsRead;
				}
			} while (charsRead > 0);

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new char[contentsLength],
					0,
					contentsLength);
			}
		} else {
			contents = new char[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize = reader.read(contents, len, length - len);
			}
			// See PR 1FMS89U
		// Now we need to resize in case the default encoding used more than one byte for each
		// character
			if (len != length)
				System.arraycopy(contents, 0, (contents = new char[len]), 0, len);
		}

		return contents;
	}

	public static void save (StringBuffer buffer, IFile file) throws CoreException {
		byte[] bytes = buffer.toString().getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		// use a platform operation to update the resource contents
		boolean force = true;
		file.setContents(stream, force, true, null); // record history
	}
}
