package org.eclipse.cdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


/**
 * Bundled state of a launched process including the threads linking the process in/output
 * to console documents.
 */
public class ProcessClosure {
		
	/**
	 * Thread which continuously reads from a input stream and pushes the read data
	 * to an output stream which is immediately flushed afterwards.
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
			fOutputStream= out;
			fInputStream= in;
			setDaemon(true);
			lineSeparator =	(String) System.getProperty("line.separator");
		}
		
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(fInputStream));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fOutputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					line += lineSeparator;
					char[] array = line.toCharArray();
					writer.write(array, 0, array.length);
					writer.flush();
				}
			} catch (IOException x) {
				// ignore
			} finally {
				try {
					fInputStream.close();
				} catch (IOException e) {
					// ignore
				}
				try {
					fOutputStream.close();
				} catch (IOException e) {
					// ignore
				}
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
	}
		
	protected static int fCounter= 0;
	
	protected Process fProcess;

	protected OutputStream fOutput;
	protected OutputStream fError;
	
	protected ReaderThread fOutputReader;
	protected ReaderThread fErrorReader;	
	
	/**
	 * Creates a process closure and connects the launched process with
	 * a console document.
	 * @param outputStream prcess stdout is written to this stream. Can be <code>null</code>, if
	 *        not interested in reading the output
	 * @param errorStream prcess stderr is written to this stream. Can be <code>null</code>, if
	 *        not interested in reading the output	 
	 */
	public ProcessClosure(Process process, OutputStream outputStream, OutputStream errorStream) {
		fProcess= process;		
		fOutput= outputStream;
		fError= errorStream;
	}
		
	/**
	 * Live links the launched process with the configured in/out streams using
	 * reader threads.
	 */
	public void runNonBlocking() {
		ThreadGroup group= new ThreadGroup("CBuilder" + fCounter++);
		
		InputStream stdin= fProcess.getInputStream();
		InputStream stderr= fProcess.getErrorStream();
		
		fOutputReader= new ReaderThread(group, "OutputReader", stdin, fOutput);
		fErrorReader= new ReaderThread(group, "ErrorReader", stderr, fError);
				
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
		
		// it seems that thread termination and stream closing is working without
		// any help
		fProcess= null;
		fOutputReader= null;
		fErrorReader= null;
	}
	
	
	public boolean isAlive() {
		if (fProcess != null) {
			if (fOutputReader.isAlive() || fErrorReader.isAlive()) {
				return true;
			} else {
				fProcess= null;
				fOutputReader= null;
				fErrorReader= null;	
			}
		}
		return false;
	}
		
	/**
	 * Forces the termination the launched process
	 */
	public void terminate() {
		if (fProcess != null) {
			fProcess.destroy();
			fProcess= null;
		}
	}
}
