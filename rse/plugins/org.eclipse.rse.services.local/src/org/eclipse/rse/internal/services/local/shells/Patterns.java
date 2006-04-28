/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.internal.services.local.shells;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.eclipse.rse.services.local.Activator;
import org.osgi.framework.Bundle;

public class Patterns {
	private ArrayList _theCommands;
	private String _currentCommand;

	public Patterns() {
		_theCommands = new ArrayList();
		parsePatterns();
	}

	private void parsePatterns() {
		Bundle bundle = Activator.getDefault().getBundle();
		URL patterns = bundle.getEntry("/patterns.dat");
		if (patterns != null) {
			try {
				InputStream in = patterns.openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				_theCommands.clear();
				String curLine;
				CommandPattern curCommand = null;
				while ((curLine = reader.readLine()) != null) {
					curLine = curLine.trim();
					// Skip the current line if it is empty or starts with a #
					if ((curLine.length() == 0) || (curLine.charAt(0) == '#')) {
						continue;
					}
					// Check if this line is the start of a new command section
					if (curLine.startsWith("command")) {
						int colon = curLine.indexOf(":");
						// Check that there is something after the colon
						if (colon == (curLine.length() - 1)) {
							continue;
						}
						Pattern thePattern = Pattern.compile(curLine.substring(colon + 1, curLine.length()).trim());
						curCommand = new CommandPattern(thePattern);
						_theCommands.add(curCommand);
					}
					// If we get here, the line must be an output pattern
					else {
						int firstSpace = curLine.indexOf(" ");
						int patternWord = curLine.indexOf("pattern");
						int firstEquals = curLine.indexOf("=");
						if ((firstEquals == -1) || (firstEquals == (curLine.length() - 1))) {
							continue;
						}
						String objType = curLine.substring(0, firstSpace);
						String matchOrder = curLine.substring(firstSpace + 1, patternWord).trim();
						String patternString = curLine.substring(firstEquals + 1, curLine.length());
						Pattern thePattern = Pattern.compile(patternString.trim());
						if (curCommand != null) {
							curCommand.addOutputPattern(new OutputPattern(objType, matchOrder, thePattern));
						}
					}
				}
				in.close();
			} catch (IOException e) {
				Activator.getDefault().logException(e);
			}
		}
	}

	public void refresh(String theCommand) {
		_currentCommand = theCommand;
		parsePatterns();
	}

	public void update(String theCommand) {
		_currentCommand = theCommand;
	}

	public ParsedOutput matchLine(String theLine) {
		CommandPattern curCommand;
		ParsedOutput matchedOutput = null;
		int commands = _theCommands.size();
		if (_currentCommand != null) {
			for (int i = 0; i < commands; i++) {
				curCommand = (CommandPattern) _theCommands.get(i);
				if (curCommand.matchCommand(_currentCommand)) {
					matchedOutput = curCommand.matchLine(theLine);
				}
				if (matchedOutput != null) {
					return matchedOutput;
				}
			}
		}
		return null;
	}
}