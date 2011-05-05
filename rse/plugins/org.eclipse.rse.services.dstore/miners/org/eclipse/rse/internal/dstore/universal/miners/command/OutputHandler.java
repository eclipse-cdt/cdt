/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [243699] [dstore] Loop in OutputHandler
 * David McKnight     (IBM)   [249715] [dstore][shells] Unix shell does not echo command
 * David McKnight   (IBM)        - [282919] [dstore] server shutdown results in exception in shell io reading
 * David McKnight (IBM) - [286671] Dstore shell service interprets &lt; and &gt; sequences
 * David McKnight     (IBM)   [287305] [dstore] Need to set proper uid for commands when using SecuredThread and single server for multiple clients[
 * Peter Wang         (IBM)   [299422] [dstore] OutputHandler.readLines() not compatible with servers that return max 1024bytes available to be read
 * David McKnight   (IBM)     [302996] [dstore] null checks and performance issue with shell output
 * David McKnight   (IBM)     [309338] [dstore] z/OS USS - invocation of 'env' shell command returns inconsistently organized output
 * David McKnight   (IBM)     [312415] [dstore] shell service interprets &lt; and &gt; sequences - handle old client/new server case
 * David McKnight   (IBM)     [341366] [dstore][shells] codepage IBM-1141 has faulty display of \ character
 * David McKnight   (IBM)     [343421] [dstore] Man page was not displayed properly in the shell
 *******************************************************************************/

package org.eclipse.rse.internal.dstore.universal.miners.command;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.dstore.core.model.Handler;
import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;

/**
 * The OutputHandler class is used to listen to a particular output or error stream, 
 * interpret that information and create DataElements for it for use on the client.
 */
/**
 * The OutputHandler class is used to listen to a particular output or error
 * stream, interpret that information and create DataElements for it for use on
 * the client.
 */
public class OutputHandler extends Handler {


	private DataInputStream _reader;
	private boolean _isStdError;

	private boolean _isTerminal;

	private CommandMinerThread _commandThread;

	private boolean _isShell;


	private static int MAX_OFFSET = 10000;

	private boolean _endOfStream = false;

	private List _encodings;


	public OutputHandler(DataInputStream reader, String qualifier,
			boolean isTerminal, boolean isStdError, boolean isShell,
			CommandMinerThread commandThread) {
		_reader = reader;
		_isStdError = isStdError;
		_isTerminal = isTerminal;
		_commandThread = commandThread;
		_isShell = isShell;

		_encodings = new ArrayList();
		String system = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		
		// use special encoding first if it exists
		String specialEncoding = System.getProperty(IDataStoreSystemProperties.DSTORE_STDIN_ENCODING);
		if (specialEncoding != null) {
			_encodings.add(specialEncoding);
		}
		
		if (system.startsWith("z")) { //$NON-NLS-1$
			_encodings.add("IBM-1047"); //$NON-NLS-1$
			/*
			 * _encodings.add("Cp1047"); _encodings.add("Cp037");
			 * _encodings.add("UTF8");
			 */
		} else {
			_encodings.add(System.getProperty("file.encoding")); //$NON-NLS-1$
		}

	}



	public void handle() {
		String[] lines = readLines();
		if (lines != null) {
						
			/*
			 * if (lines.length == 0) { _reader. }
			 *  // don't do anything unless we require output if (_newCommand &&
			 * !_isTerminal) { doPrompt(); } } else
			 */
			for (int i = 0; i < lines.length; i++) {
				// first make sure it's not a multiline line
				String ln = lines[i];
				if (ln.indexOf('\n') > 0){
					String[] lns = ln.split("\n"); //$NON-NLS-1$
					for (int j = 0; j < lns.length; j++){
						String line = convertSpecialCharacters(lns[j]);
						_commandThread.interpretLine(line, _isStdError);
					}
				}
				else {
					String line = convertSpecialCharacters(ln);
					_commandThread.interpretLine(line, _isStdError);
				}
			}

			if (!_isTerminal){
				doPrompt();
			}

			_commandThread.refreshStatus();
		} else {
			finish();
		}
	}
	
	private String convertSpecialCharacters(String input){
		if (_commandThread._supportsCharConversion){
		   // needed to ensure xml characters aren't converted in xml layer			
			StringBuffer output = new StringBuffer();

			for (int idx = 0; idx < input.length(); idx++)
			{
				char currChar = input.charAt(idx);
				switch (currChar)
				{
				case '&' :
					output.append("&#38;"); //$NON-NLS-1$
					break;
				case ';' :
					output.append("&#59;"); //$NON-NLS-1$
					break;
				case '\b': // special case for backspace control char
					int len = output.length()-1;
					if (len>=0) output.deleteCharAt(len);
					break;
				default :
					output.append(currChar);
					break;
				}
			}
			return output.toString();
		}
		else {
			return input;
		}
	}

	private void doPrompt() {
		try {
			if ((_reader.available() == 0) && !_isStdError && _isShell) {
				if (!_isTerminal) {
					try {
						Thread.sleep(200);
						if (_reader.available() == 0) {
							// create fake prompt
							String cwd = _commandThread.getCWD();
							_commandThread.createPrompt(cwd + '>', cwd);							
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (IOException e) {
			_commandThread._dataStore.trace(e);
		}
	}

	private int checkAvailable(){
		return checkAvailable(100);
	}
	
	private int checkAvailable(int time) {
		try
		{
			int available = _reader.available();

			// if there's none, wait a bit and return true to continue
			if (available <= 0){
				sleep(time);
				available = _reader.available();
			}
			return available;
		}
		catch (Exception e)
		{			
		}
		return 0;
	}

	private String[] readLines() {
		if (_endOfStream) {
			return null;
		}
		String[] output = null;

		try {

			// find out how many bytes are available to be read
			int available = checkAvailable();

			int lookahead = 0;

			// re-determine available if none available now
			if (available == 0) {
				try {
					lookahead = _reader.read();
				}
				catch  (IOException e){
					// pipe closed
					return null;
				}
				if (lookahead == -1) {
					return null;
				} else {
					available = _reader.available() + 1;
				}
			}

			byte[] readBytes = new byte[available];

			// read the available bytes
			int numRead = 0;
			if (lookahead > 0) {
				readBytes[0] = (byte) lookahead;
				numRead = _reader.read(readBytes, 1, available - 1) + 1;
			} else {
				numRead = _reader.read(readBytes, 0, available);
			}

			// if we've reached end of stream, quit
			if (numRead == -1) {
				return null;
			}

			if (readBytes[numRead - 1] == -1) {
				_endOfStream = true;
			}

			// use various encodings as a precaution
			// note that the first encoding will be the encoding that we were
			// given
			int encodingIndex = 0;

			while (encodingIndex < _encodings.size()) {
				String encoding = (String) (_encodings.get(encodingIndex));

				// get the output using the encoding
				try {
					String fullOutput = new String(readBytes, 0, numRead,
							encoding);
					
					// if output is not null, we assume the encoding was correct
					// and process the output
					if (fullOutput != null) {
						// tokenize the output so that we can get each line of
						// output
						// the delimiters are therefore set to "\n\r"
						StringTokenizer tokenizer = new StringTokenizer(
								fullOutput, "\n\r"); //$NON-NLS-1$
						int numTokens = tokenizer.countTokens();
						if (numTokens == 0){
							output = new String[1];
							output[0] = fullOutput;
							return output;
						}
						
						output = new String[numTokens];
						int index = 0;
						while (tokenizer.hasMoreTokens()) {
							output[index] = tokenizer.nextToken();
							
						
							index++;
						}

						String lastLine = output[index - 1];

						boolean endLine = fullOutput.endsWith("\n") || fullOutput.endsWith("\r") || fullOutput.endsWith(">");
						
						if (!_endOfStream && !endLine)
						{
							// our last line may be cut off		
							byte[] lastBytes = new byte[MAX_OFFSET];
							
							int lastIndex = 0;
							available = checkAvailable();
					
							if (available == 0){
								try {
									lookahead = _reader.read();
								}
								catch  (IOException e){
									// pipe closed
									// allow to fall through
								}
								if (lookahead == -1) {
									// allow to fall through
								} else {
									available = _reader.available() + 1;
								}
							}
				
							if (available > 0)
							{
								while (!_endOfStream && lastIndex < MAX_OFFSET)
								{
									
									if (available == 0)
									{
										String suffix = new String(lastBytes, 0, lastIndex, encoding);
										output[index - 1] = lastLine + suffix.substring(0, suffix.length() - 2);
										return output;
									}
									int c = _reader.read();
									if (c == -1)
									{
										_endOfStream = true;
										String suffix = new String(lastBytes, 0, lastIndex, encoding);
										output[index - 1] = lastLine + suffix.substring(0, suffix.length() - 2);
										return output;
									}
									else
									{
										lastBytes[lastIndex] = (byte)c;
										
										String osname = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
										char lf = '\r';
										char nl = '\n';
										
										// in ebcdic, the following chars are used
										if (osname.startsWith("z")){ //$NON-NLS-1$
											lf = '\25';
											nl = '\15';
										}
										
										if (lastBytes[lastIndex] == lf || lastBytes[lastIndex] == nl){
											// we've hit the end of line;
											String suffix = new String(lastBytes, 0, lastIndex + 1, encoding);
											output[index - 1] = lastLine + suffix.substring(0, suffix.length() - 1);
											return output;
										}
									
										lastIndex++;
										available = checkAvailable();
									}
								
								}
							}
							
						}

						return output;
					}
				} catch (Exception e) {
					_commandThread._dataStore.trace(e);
				}
			}
		} catch (Exception e) {
			_commandThread._dataStore.trace(e);
		}
		return output;
	}
	public synchronized void waitForInput() {
		try {
			Thread.sleep(100);
		} catch (Exception e) {

		}
	}
}