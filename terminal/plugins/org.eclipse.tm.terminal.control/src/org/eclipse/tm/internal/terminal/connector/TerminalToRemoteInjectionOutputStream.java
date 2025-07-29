/*******************************************************************************
 * Copyright (c) 2008, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.connector;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noreference This class is not intended to be referenced by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *     This class used to be package-protected. It is public only for access by the Unit Tests.
 */
public class TerminalToRemoteInjectionOutputStream extends FilterOutputStream {
	/**
	 * This class handles bytes written to the {@link TerminalToRemoteInjectionOutputStream}.
	 */
	static abstract public class Interceptor {
		protected OutputStream fOriginal;

		/**
		 * @param original the injection into the original stream begins
		 * @throws IOException
		 */
		public void begin(OutputStream original) throws IOException {
			fOriginal = original;
		}

		/**
		 * @param b a byte was written to the {@link TerminalToRemoteInjectionOutputStream}.
		 * @throws IOException
		 */
		public void write(int b) throws IOException {
		}

		/**
		 * @param b bytes written to the {@link TerminalToRemoteInjectionOutputStream}.
		 * @param off   the start offset in the data.
		 * @param len   the number of bytes to write.
		 * @throws IOException
		 */
		public void write(byte[] b, int off, int len) throws IOException {
		}

		/**
		 * The injection into the normal stream ends.
		 * @throws IOException
		 */
		public void close() throws IOException {
		}

		public void flush() {
		}
	}

	static public class BufferInterceptor extends Interceptor {
		private final ByteArrayOutputStream fBuffer = new ByteArrayOutputStream();

		@Override
		public void close() throws IOException {
			fOriginal.write(fBuffer.toByteArray());
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			fBuffer.write(b, off, len);
		}

		@Override
		public void write(int b) throws IOException {
			fBuffer.write(b);
		}
	}

	private class TerminalFilterOutputStream extends OutputStream {
		final private Object fLock = TerminalToRemoteInjectionOutputStream.this;

		@Override
		public void close() throws IOException {
			synchronized (fLock) {
				if (fInjection == this) {
					flush();
					ungrabOutput();
				}
			}
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			synchronized (fLock) {
				checkStream();
				out.write(b, off, len);
			}
		}

		@Override
		public void write(byte[] b) throws IOException {
			synchronized (fLock) {
				checkStream();
				out.write(b);
			}
		}

		@Override
		public void flush() throws IOException {
			synchronized (fLock) {
				checkStream();
				out.flush();
			}
		}

		@Override
		public void write(int b) throws IOException {
			synchronized (fLock) {
				checkStream();
				out.write(b);
			}
		}

		private void checkStream() throws IOException {
			if (fInjection != this)
				throw new IOException("Stream is closed"); //$NON-NLS-1$
		}
	}

	private Interceptor fInterceptor;
	private TerminalFilterOutputStream fInjection;

	public TerminalToRemoteInjectionOutputStream(OutputStream out) {
		super(out);
	}

	synchronized protected void ungrabOutput() throws IOException {
		if (fInterceptor != null) {
			fInterceptor.close();
			fInterceptor = null;
			fInjection = null;
		}
	}

	/**
	 * There can only be one injection stream active at a time. You must call close on the
	 * returned output stream to end the injection.
	 * @param interceptor This is used handle bytes sent while the injection stream is active.
	 * @return a output stream that can be used to write to the decorated stream.
	 * @throws IOException
	 */
	public synchronized OutputStream grabOutput(Interceptor interceptor) throws IOException {
		if (fInjection != null) {
			throw new IOException("Buffer in use"); //$NON-NLS-1$
		}
		fInterceptor = interceptor;
		fInterceptor.begin(out);
		fInjection = new TerminalFilterOutputStream();
		return fInjection;
	}

	/** See {@link #grabOutput(TerminalToRemoteInjectionOutputStream.Interceptor)}.
	 * @return injection output stream
	 * @throws IOException
	 */
	public synchronized OutputStream grabOutput() throws IOException {
		return grabOutput(new BufferInterceptor());
	}

	@Override
	synchronized public void close() throws IOException {
		if (fInjection != null) {
			fInjection.close();
		}
		super.close();
	}

	@Override
	synchronized public void flush() throws IOException {
		if (fInterceptor != null)
			fInterceptor.flush();
		out.flush();
	}

	@Override
	synchronized public void write(byte[] b, int off, int len) throws IOException {
		if (fInterceptor != null)
			fInterceptor.write(b, off, len);
		else
			out.write(b, off, len);
	}

	@Override
	synchronized public void write(int b) throws IOException {
		if (fInterceptor != null)
			fInterceptor.write(b);
		else
			out.write(b);
	}
}
