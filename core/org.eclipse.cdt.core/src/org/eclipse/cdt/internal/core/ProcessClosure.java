package org.eclipse.cdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Bundled state of a launched process including the threads linking the process
 * in/output to console documents.
 */
public class ProcessClosure {

	/**
	 * Thread which continuously reads from a input stream and pushes the read
	 * data to an output stream which is immediately flushed afterwards.
	 */
	protected static class ReaderThread extends Thread {

		private InputStream fInputStream;
		private OutputStream fOutputStream;
		private boolean fFinished = false;
		private String lineSeparator;
		/*
		 * outputStream can be null
		 */
		public ReaderThread(ThreadGroup group, String name, InputStream in, OutputStream out) {
			super(group, name);
			fOutputStream = out;
			fInputStream = in;
			setDaemon(true);
			lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		}

		public void run() {
			try {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(fInputStream));
					String line;
					while ((line = reader.readLine()) != null) {
						line += lineSeparator;
						fOutputStream.write(line.getBytes());
					}
				} catch (IOException x) {
					// ignore
				} finally {
					try {
						//					writer.flush();
						fOutputStream.flush();
					} catch (IOException e) {
						// ignore
					}
					try {
						fInputStream.close();
					} catch (IOException e) {
						// ignore
					}
				}
			} finally {
				complete();
			}
		}

		public synchronized boolean finished() {
			return fFinished;
		}

		public synchronized void waitFor() {
			while (!fFinished) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}

		public synchronized void complete() {
			fFinished = true;
			notify();
		}

		public void close() {
			try {
				fOutputStream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	protected static int fCounter = 0;

	protected Process fProcess;

	protected OutputStream fOutput;
	protected OutputStream fError;

	protected ReaderThread fOutputReader;
	protected ReaderThread fErrorReader;

	/**
	 * Creates a process closure and connects the launched process with a
	 * console document.
	 * 
	 * @param outputStream
	 *            prcess stdout is written to this stream. Can be
	 *            <code>null</code>, if not interested in reading the output
	 * @param errorStream
	 *            prcess stderr is written to this stream. Can be
	 *            <code>null</code>, if not interested in reading the output
	 */
	public ProcessClosure(Process process, OutputStream outputStream, OutputStream errorStream) {
		fProcess = process;
		fOutput = outputStream;
		fError = errorStream;
	}

	/**
	 * Live links the launched process with the configured in/out streams using
	 * reader threads.
	 */
	public void runNonBlocking() {
		ThreadGroup group = new ThreadGroup("CBuilder" + fCounter++); //$NON-NLS-1$

		InputStream stdin = fProcess.getInputStream();
		InputStream stderr = fProcess.getErrorStream();

		fOutputReader = new ReaderThread(group, "OutputReader", stdin, fOutput); //$NON-NLS-1$
		fErrorReader = new ReaderThread(group, "ErrorReader", stderr, fError); //$NON-NLS-1$

		fOutputReader.start();
		fErrorReader.start();
	}

	public void runBlocking() {
		runNonBlocking();

		boolean finished = false;
		while (!finished) {
			try {
				fProcess.waitFor();
			} catch (InterruptedException e) {
				//System.err.println("Closure exception " +e);
			}
			try {
				fProcess.exitValue();
				finished = true;
			} catch (IllegalThreadStateException e) {
				//System.err.println("Closure exception " +e);
			}
		}

		// @@@FIXME: Windows 2000 is screwed; double-check using output threads
		if (!fOutputReader.finished()) {
			fOutputReader.waitFor();
		}

		if (!fErrorReader.finished()) {
			fErrorReader.waitFor();
		}

		fOutputReader.close();
		fErrorReader.close();
		// it seems that thread termination and stream closing is working
		// without
		// any help
		fProcess = null;
		fOutputReader = null;
		fErrorReader = null;
	}

	public boolean isAlive() {
		if (fProcess != null) {
			if (fOutputReader.isAlive() || fErrorReader.isAlive()) {
				return true;
			}
			fProcess = null;
			fOutputReader.close();
			fErrorReader.close();
			fOutputReader = null;
			fErrorReader = null;
		}
		return false;
	}

	/**
	 * Forces the termination the launched process
	 */
	public void terminate() {
		if (fProcess != null) {
			fProcess.destroy();
			fProcess = null;
		}
		if (!fOutputReader.finished()) {
			fOutputReader.waitFor();
		}
		if (!fErrorReader.finished()) {
			fErrorReader.waitFor();
		}
		fOutputReader.close();
		fErrorReader.close();
		fOutputReader = null;
		fErrorReader = null;
	}
}