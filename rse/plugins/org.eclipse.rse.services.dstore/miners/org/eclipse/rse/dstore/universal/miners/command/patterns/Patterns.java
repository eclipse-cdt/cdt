/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.dstore.universal.miners.command.patterns;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreAttributes;

/**
 * This class is used for interpretting standard error and standard output.  
 */
public class Patterns
{

	private ArrayList _theCommands;
	private DataStore _dataStore;

	private String _currentCommand = null;
	private List _currentCommandPatterns = null;
	
	//private String _previousCommand = null;
	private long _timeStamp = 0;
	private File _thePatternsFile;
	private boolean _isTerminal = false;
	private String _pluginPath;

	private static String MINERS_PACKAGE = "org.eclipse.rse.services.dstore";
	private static String PATTERNS_FILE = "patterns.dat";

	// HACK - too late in cycle to deal with version properly - for now this has to be fixed
	private String _version = "8.0.0";

	public Patterns(DataStore ds)
	{
		_dataStore = ds;
		_theCommands = new ArrayList();
		_currentCommandPatterns = new ArrayList();
		_pluginPath = ds.getAttribute(DataStoreAttributes.A_ROOT_PATH);
		parsePatternsFile();
	}
	
	public Patterns(DataStore ds, String pluginsPath)
	{
		_dataStore = ds;
		_theCommands = new ArrayList();
		_currentCommandPatterns = new ArrayList();
		_pluginPath = pluginsPath;
		parsePatternsFile();
	}
	
	public void setIsTerminal(boolean isTerminal)
	{
		_isTerminal = isTerminal;
	}

	private String cleanCmdString(String theCommand)
	{
		String result = theCommand;

		// for multi commands
		int semiIndex = result.indexOf(";");
		if (semiIndex > 0)
		{ 
			result = result.substring(0, semiIndex);
		}
		
		// for qualified commands
		int spaceIndex = result.indexOf(" ");
		if (spaceIndex > -1)
		{
			int slashIndex = result.lastIndexOf("/", spaceIndex);
			if ((slashIndex > 0))
			{
				result = result.substring(slashIndex + 1, result.length());
			}
		}
		else
		{
			int slashIndex = result.lastIndexOf("/");
			if ((slashIndex > 0))
			{
				result = result.substring(slashIndex + 1, result.length());
			}
		}
		
		return result;
	}
	
	public void refresh(String theCommand)
	{
	//	_previousCommand = _currentCommand;
		_currentCommand = cleanCmdString(theCommand);
		_currentCommandPatterns.clear();
		parsePatternsFile();
	}

	public void update(String theCommand)
	{
	//	_previousCommand = _currentCommand;
		_currentCommand = cleanCmdString(theCommand);
		_currentCommandPatterns.clear();
		
		// don't reparse patterns file	
	}

	private File getPatternsFile()
	{
		if (_thePatternsFile == null)
		{

			File thePatternsFile = new File(_pluginPath + "/" + MINERS_PACKAGE + "/" + PATTERNS_FILE);
			if (!thePatternsFile.exists())
			{
				thePatternsFile = new File(_pluginPath + "/" + PATTERNS_FILE);
				if (!thePatternsFile.exists())
				{
					thePatternsFile = new File(_pluginPath + "/" + MINERS_PACKAGE + "_" + _version + "/" + PATTERNS_FILE);

					if (!thePatternsFile.exists())
					{
						File parentFile = new File(_pluginPath);
						if (parentFile.exists())
						{
							// now we're really desparate!
							// search for a file that looks like it
							File[] files = parentFile.listFiles();
							for (int i = 0; i < files.length && !thePatternsFile.exists(); i++)
							{
								File c = files[i];
								
								if (c.getName().startsWith(MINERS_PACKAGE))
								{
									thePatternsFile = c;
								}
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
		if (_dataStore == null)
			return;

		//Check the timestamp of the patterns.dat file to make sure we need to read it.
		File thePatternsFile = getPatternsFile();

		long newTimeStamp = 0;
		if (!thePatternsFile.exists() || ((newTimeStamp = thePatternsFile.lastModified()) == _timeStamp))
			return;

		_timeStamp = newTimeStamp;

		//If we get here, we are actually going to read\parse the file. 
		try
		{		    
			readPatternsFile(thePatternsFile, DE.ENCODING_UTF_8);
		}
		catch (Exception e)
		{
			_dataStore.trace(e);
			_dataStore.trace("failed to load patterns.dat with UTF-8.  Trying with native encoding");

			try
			{
				readPatternsFile(thePatternsFile, null);
			}
			catch (Exception ex)
			{
				_dataStore.trace(ex);
			}
		}
	}

	private void readPatternsFile(File thePatternsFile, String encoding) throws Exception
	{
		FileInputStream fileStream = new FileInputStream(thePatternsFile);
		InputStreamReader inReader = null;
		if (encoding == null)
		{
			inReader = new InputStreamReader(fileStream);
		}
		else
		{
			inReader = new InputStreamReader(fileStream, encoding);
		}
		BufferedReader reader = new BufferedReader(inReader);

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
				String cmdStr = curLine.substring(colon + 1, curLine.length()).trim();
			
				Pattern thePattern = Pattern.compile(cmdStr);
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
	
	private List getCurrentCommandPatterns()
	{
	    
	    if (_currentCommandPatterns.size() == 0)
	    {
		    int commands = _theCommands.size();
			 
			for (int i = 0; i < commands; i++)
			{
				CommandPattern curCommand = (CommandPattern) _theCommands.get(i);
								
				if (curCommand.matchCommand(_currentCommand))
				{
				    _currentCommandPatterns.add(curCommand);
				}
			}
	    }
		return _currentCommandPatterns;
	}
	

	public ParsedOutput matchLine(String theLine)
	{

		CommandPattern curCommand;
		ParsedOutput matchedOutput = null; 

		List cmdPatterns = getCurrentCommandPatterns();
		for (int i = 0; i < cmdPatterns.size(); i++)
		{
			curCommand = (CommandPattern) cmdPatterns.get(i);
						
			if (curCommand.matchCommand(_currentCommand))
			{
				if (_currentCommand.equals("ls") && _isTerminal && !curCommand.getPattern().equals(".*"))
				{
				}
				else
				{
					matchedOutput = curCommand.matchLine(theLine);											
				}
			}	

			if (matchedOutput != null)
			{
				return matchedOutput;
			}
		}

		return null;
	}
}