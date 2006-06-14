/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;


public class CDTLogWriter {
	protected File logFile = null;
	protected Writer log = null;
	protected boolean newSession = true;
	
	protected static final String SESSION = "*** SESSION";//$NON-NLS-1$
	protected static final String ENTRY = "ENTRY";//$NON-NLS-1$
	protected static final String SUBENTRY = "SUBENTRY";//$NON-NLS-1$
	protected static final String MESSAGE = "MESSAGE";//$NON-NLS-1$
	protected static final String STACK = "STACK";//$NON-NLS-1$

	protected static final String LINE_SEPARATOR;
	protected static final String TAB_STRING = "\t";//$NON-NLS-1$
	protected static final long MAXLOG_SIZE = 10000000;
	static {
		String s = System.getProperty("line.separator");//$NON-NLS-1$
		LINE_SEPARATOR = s == null ? "\n" : s;//$NON-NLS-1$
	}
	/**
	 * 
	 */
	public CDTLogWriter(File log) {
		this.logFile = log;
		if(log.length() > MAXLOG_SIZE){
		  log.delete();
		  this.logFile = CCorePlugin.getDefault().getStateLocation().append(".log").toFile(); //$NON-NLS-1$
		}
		openLogFile();
	}
	
	protected void closeLogFile() throws IOException {
		try {
			if (log != null) {
				log.flush();
				log.close();
			}
		} finally {
			log = null;
		}
	}
	
	protected void openLogFile() {
		try {
			log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile.getAbsolutePath(), true), "UTF-8"));//$NON-NLS-1$
			if (newSession) {
				writeHeader();
				newSession = false;
			}
		} catch (IOException e) {
			// there was a problem opening the log file so log to the console
			//log = logForStream(System.err);
		}
	}
	protected void writeHeader() throws IOException {
		write(SESSION);
		writeSpace();
		String date = getDate();
		write(date);
		writeSpace();
		for (int i=SESSION.length()+date.length(); i<78; i++) {
			write("-");//$NON-NLS-1$
		}
		writeln();
	}
	
	protected String getDate() {
		try {
			DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SS"); //$NON-NLS-1$
			return formatter.format(new Date());
		} catch (Exception e) {
			// If there were problems writing out the date, ignore and
			// continue since that shouldn't stop us from losing the rest
			// of the information
		}
		return Long.toString(System.currentTimeMillis());
	}
	protected Writer logForStream(OutputStream output) {
		try {
			return new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));//$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			return new BufferedWriter(new OutputStreamWriter(output));
		}
	}
	/**
	 * Writes the given string to the log, followed by the line terminator string.
	 */
	protected void writeln(String s) throws IOException {
		write(s);
		writeln();
	}
	/**
	 * Shuts down the log.
	 */
	public synchronized void shutdown() {
		try {
			if (logFile != null) {
				closeLogFile();
				logFile = null;
			} else {
				if (log != null) {
					Writer old = log;
					log = null;
					old.flush();
					old.close();
				}
			}
		} catch (Exception e) {
			//we've shutdown the log, so not much else we can do!
			e.printStackTrace();
		}
	}

	protected void write(Throwable throwable) throws IOException {
		if (throwable == null)
			return;
		write(STACK);
		writeSpace();
		boolean isCoreException = throwable instanceof CoreException;
		if (isCoreException)
			writeln("1");//$NON-NLS-1$
		else
			writeln("0");//$NON-NLS-1$
		throwable.printStackTrace(new PrintWriter(log));
		if (isCoreException) {
		 CoreException e = (CoreException) throwable;
		 write(e.getStatus(), 0);
		}
	}

	public synchronized void log(IStatus status){
		try {
			this.write(status, 0);
		} catch (IOException e) {
		}
	}
	protected void write(IStatus status, int depth) throws IOException {
		if (depth == 0) {
			write(ENTRY);
		} else {
			write(SUBENTRY);
			writeSpace();
			write(Integer.toString(depth));
		}
		writeSpace();
		write(status.getPlugin());
		writeSpace();
		write(Integer.toString(status.getSeverity()));
		writeSpace();
		write(Integer.toString(status.getCode()));
		writeSpace();
		write(getDate());
		writeln();

		write(MESSAGE);
		writeSpace();
		writeln(status.getMessage());

		//Took out the stack dump - too much space
		//write(status.getException());

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				write(children[i], depth+1);
			}
		}
	}

	protected void writeln() throws IOException {
		write(LINE_SEPARATOR);
	}
	protected void write(String message) throws IOException {
		if (message != null)
			log.write(message);
	}
	protected void writeSpace() throws IOException {
		write(" ");//$NON-NLS-1$
	}
	
	public synchronized void flushLog(){
		try {
			log.flush();
		} catch (IOException e) {}
	}

}
