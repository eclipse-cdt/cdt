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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.rse.services.local.Activator;
import org.osgi.framework.Bundle;

public class Patterns
{

	private ArrayList _theCommands;
	private String _currentCommand;
	private String _pluginsPath;
	private String _version;
	private long _timeStamp = 0;
	private File _thePatternsFile;
	private static String PATTERNS_PACKAGE = "org.eclipse.rse.services.local";
	private static String PATTERNS_FILE = "patterns.dat";

	public Patterns()
	{
		_theCommands = new ArrayList();
		parsePatternsFile();
	}

	protected String getPatternsFilePath(Bundle bundle)
	{
		URL pluginsURL = bundle.getEntry("/");
		String path = null;
		try
		{
			path = Platform.resolve(pluginsURL).getPath();
			File systemsPluginDir = new File(path);
			path = systemsPluginDir.getParentFile().getAbsolutePath();
		}
		catch (IOException e)
		{
		}
		return path;
	}

	private String getPatternsFilePath()
	{
		if (_pluginsPath == null)
		{
			Bundle bundle = Activator.getDefault().getBundle();
			_pluginsPath = getPatternsFilePath(bundle);
			String version = (String)(bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION));
			_version = (new PluginVersionIdentifier(version)).toString();
		}
		return _pluginsPath;
	}

	public void refresh(String theCommand)
	{
		_currentCommand = theCommand;
		parsePatternsFile();
	}

	public void update(String theCommand)
	{
		_currentCommand = theCommand;
	}

	private File getPatternsFile()
	{
		if (_thePatternsFile == null)
		{
			String pluginDir = getPatternsFilePath();
		
			
			File thePatternsFile = new File(pluginDir + "/" + PATTERNS_PACKAGE + "/" + PATTERNS_FILE);
			if (!thePatternsFile.exists())
			{
				thePatternsFile = new File(pluginDir + "/" + PATTERNS_PACKAGE + "_" + _version + "/" + PATTERNS_FILE);

				if (!thePatternsFile.exists())
				{
					File parentFile = new File(pluginDir);
					if (parentFile.exists())
					{
						// now we're really desparate!
						// search for a file that looks like it
						File[] files = parentFile.listFiles();
						for (int i = 0; i < files.length && !thePatternsFile.exists(); i++)
						{
							File c = files[i];
							if (c.getName().startsWith(PATTERNS_PACKAGE))
							{
								thePatternsFile = c;
							}
						}

					}
				}
			}
			_thePatternsFile = thePatternsFile;
		}
		return _thePatternsFile;
	}
	private void parsePatternsFile()
	{
		File thePatternsFile = getPatternsFile();

		long newTimeStamp = 0;
		if (!thePatternsFile.exists() || ((newTimeStamp = thePatternsFile.lastModified()) == _timeStamp))
			return;

		_timeStamp = newTimeStamp;

		//If we get here, we are actually going to read\parse the file. 
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(thePatternsFile));
			_theCommands.clear();

			String curLine;
			CommandPattern curCommand = null;

			//Main Loop that reads each line.
			while ((curLine = reader.readLine()) != null)
			{
				curLine = curLine.trim();
				//Skip the current line if it is empty or starts with a #
				if ((curLine.length() == 0) || (curLine.charAt(0) == '#'))
					continue;

				//Check if this line is the start of a new command section
				if (curLine.startsWith("command"))
				{
					int colon = curLine.indexOf(":");
					//Check that there is something after the colon
					if (colon == (curLine.length() - 1))
						continue;
						
					Pattern thePattern = Pattern.compile(curLine.substring(colon + 1, curLine.length()).trim());
					curCommand = new CommandPattern(thePattern);
					_theCommands.add(curCommand);
				}

				//If we get here, the line must be an output pattern 
				else
				{
					int firstSpace = curLine.indexOf(" ");
					int patternWord = curLine.indexOf("pattern");
					int firstEquals = curLine.indexOf("=");
					if ((firstEquals == -1) || (firstEquals == (curLine.length() - 1)))
						continue;
					String objType = curLine.substring(0, firstSpace);
					String matchOrder = curLine.substring(firstSpace + 1, patternWord).trim();
					String patternString = curLine.substring(firstEquals + 1, curLine.length());
					Pattern thePattern = Pattern.compile(patternString.trim());

					if (curCommand != null)
						curCommand.addOutputPattern(new OutputPattern(objType, matchOrder, thePattern));
				}
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
			return;
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
			return;
		}
	}

	public ParsedOutput matchLine(String theLine)
	{
		CommandPattern curCommand;
		ParsedOutput matchedOutput = null;
		int commands = _theCommands.size();

		if (_currentCommand != null)
		{
			for (int i = 0; i < commands; i++)
			{
				curCommand = (CommandPattern) _theCommands.get(i);
				if (curCommand.matchCommand(_currentCommand))
					matchedOutput = curCommand.matchLine(theLine);
				if (matchedOutput != null)
					return matchedOutput;
			}
		}
		return null;
	}
}