/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *  David McKnight  (IBM)  - [202822] updating cleanup
 *  David McKnight   (IBM)        - [196624] dstore miner IDs should be String constants rather than dynamic lookup
 *  Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 *  David McKnight     (IBM)   [224906] [dstore] changes for getting properties and doing exit due to single-process capability
 *  David McKnight     (IBM)   [250203] [dstore][shells]%var% is substituted to null in Unix shell
 *******************************************************************************/

package org.eclipse.rse.internal.dstore.universal.miners.command;



import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.dstore.core.miners.MinerThread;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.rse.dstore.universal.miners.CommandMiner;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.internal.dstore.universal.miners.command.patterns.ParsedOutput;
import org.eclipse.rse.internal.dstore.universal.miners.command.patterns.Patterns;



/**
 * CommandMinerThread is used for running and handling io for shell commands
 * in a thread.
 */
public class CommandMinerThread extends MinerThread
{
	private DataElement _status;
	private String _invocation;

	private DataInputStream _stdInput;
	private DataInputStream _stdError;
	
	
	private BufferedWriter _stdOutput;

	private Patterns _patterns;

	private Process _theProcess;

	private DataElement _subject;
	private String _cwdStr;
	private OutputHandler _stdOutputHandler; 
	private OutputHandler _stdErrorHandler;
	private boolean _isShell;
	private boolean _isDone;
	private boolean _isWindows;
	private boolean _isTTY;
	private boolean _didInitialCWDQuery = false;
	
	private CommandMiner.CommandMinerDescriptors _descriptors;
	
	// default
	private String PSEUDO_TERMINAL;

	private DataElement _lastPrompt;

	public CommandMinerThread(DataElement theElement, String invocation, DataElement status, Patterns thePatterns, CommandMiner.CommandMinerDescriptors descriptors)
	{ 
		super(theElement.getDataStore());
		_isShell = false;
		_isDone = false;
		_status = status;
		_descriptors = descriptors;
		
		_subject = theElement;
		String theOS = System.getProperty("os.name"); //$NON-NLS-1$
		
		_invocation = invocation.trim();
		_patterns = thePatterns;
		_patterns.refresh(_invocation);
		
	
		boolean isZ = theOS.toLowerCase().startsWith("z");//$NON-NLS-1$
		
		if (isZ)
		{
		  System.setProperty("dstore.stdin.encoding","Cp037"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	    	
		_isWindows = theOS.toLowerCase().startsWith("win"); //$NON-NLS-1$
		if (!_isWindows)
		{
			PSEUDO_TERMINAL = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + File.separatorChar + "rseterm" + File.separatorChar + "rseterm"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		try
		{
			String userHome = null;
			if (_dataStore.getClient() != null){
				userHome = _dataStore.getClient().getProperty("user.home");//$NON-NLS-1$
			}
			else {	
				userHome = System.getProperty("user.home");//$NON-NLS-1$
			}
			
			_cwdStr = theElement.getSource();
			if (_cwdStr == null || _cwdStr.length() == 0)
			{
				_cwdStr = userHome;
			}

			File theDirectory = new File(_cwdStr);
			if (!theDirectory.isDirectory())
				theDirectory = theDirectory.getParentFile();
			try
			{
				_cwdStr = theDirectory.getAbsolutePath();
			}
			catch (Exception e)
			{
				_cwdStr = userHome; 
			}
			_status.setAttribute(DE.A_SOURCE, _cwdStr);

			
			boolean didLogin = false;

			String theShell = null;
			if (!_isWindows)
			{
				File psuedoTerminal = new File(PSEUDO_TERMINAL);
				if (psuedoTerminal.exists())
				{
					_isTTY = true;
					PSEUDO_TERMINAL = psuedoTerminal.getAbsolutePath();
				}
				else
				{
					_isTTY = false;
				}
				_patterns.setIsTerminal(_isTTY);
				
				String property = "SHELL="; //$NON-NLS-1$
				
				String[] env = getEnvironment(_subject);
				boolean isBash = false;
				boolean isBashonZ = false;
				boolean isSHonZ = false;
				
				for (int i = 0; i < env.length; i++)
				{
					String var = env[i];
					if (var.startsWith(property))
					{
						theShell = var.substring(property.length(), var.length());
						if (theShell.endsWith("bash"))//$NON-NLS-1$
						{
							if (isZ)
							{
								isBashonZ = true;
							}
							else
							{
								isBash = true;
								// no bash!
								theShell = "sh"; //$NON-NLS-1$
								
							}
						}
						else if (theShell.endsWith("sh") && isZ)//$NON-NLS-1$
						{
							isSHonZ = true;
						}
					}
					// In a single-process server, both user.home and HOME don't represent
					// each client home directory.
					if (_dataStore.getClient() != null)
					{
					    if (var.startsWith("HOME")) //$NON-NLS-1$
					    {					    	
						    env[i] = "HOME=" + _dataStore.getClient().getProperty("user.home"); //$NON-NLS-1$ //$NON-NLS-2$
					    }
					}
				}
			
				
				if (theShell == null)
				{
					if (_invocation.equals(">")) //$NON-NLS-1$
					{
						_invocation = "sh"; //$NON-NLS-1$
						_isShell = true;
						if (isZ)
							isSHonZ = true;
					}
					if (_isTTY)
					{
						if (isSHonZ)
						{
							String args[] = new String[3];				
							args[0] = PSEUDO_TERMINAL;
							args[1] = _invocation;
							args[2] = "-L"; //$NON-NLS-1$
							
							try {
								_theProcess = Runtime.getRuntime().exec(args, env, theDirectory);
							}
							catch (Exception e) {
								_theProcess = Runtime.getRuntime().exec(_invocation, env, theDirectory);
							}
							didLogin = true;
						}
						else
						{
							String args[] = new String[2];				
							args[0] = PSEUDO_TERMINAL;
							args[1] = _invocation;
							_theProcess = Runtime.getRuntime().exec(args, env, theDirectory);
						}
					}
					else
					{
						_theProcess = Runtime.getRuntime().exec(_invocation, env, theDirectory);
					}
				}
				else
				{
					if (_invocation.equals(">")) //$NON-NLS-1$
					{					
						_invocation = theShell;
				
						_isShell = true;
					
						if (_isTTY)
						{
						    String args[] = null;
						    if (isBashonZ)
						    {
						    	args = new String[5];
								args[0] = PSEUDO_TERMINAL;
								args[1] = "-w"; //$NON-NLS-1$
								args[2] = "256"; //$NON-NLS-1$
								args[3] = _invocation;
								args[4] = "--login"; //$NON-NLS-1$
								didLogin = true;
						    }						    
						    else if (isBash)
						    {
						    	args = new String[5];
								args[0] = PSEUDO_TERMINAL;
								args[1] = "-w"; //$NON-NLS-1$
								args[2] = "256"; //$NON-NLS-1$
								args[3] = _invocation;
								args[4] = "-l";								 //$NON-NLS-1$
								didLogin = true;
						    }						    						    
						    else if (isSHonZ)
						    {
						    	args = new String[5];
								args[0] = PSEUDO_TERMINAL;
								args[1] = "-w"; //$NON-NLS-1$
								args[2] = "256"; //$NON-NLS-1$
								args[3] = _invocation;
								args[4] = "-L"; //$NON-NLS-1$
								didLogin = true;
						    }
						    else
						    {
						        args = new String[4];
								args[0] = PSEUDO_TERMINAL;
								args[1] = "-w"; //$NON-NLS-1$
								args[2] = "256"; //$NON-NLS-1$
								args[3] = _invocation;
						    }
						    
							try {
								_theProcess = Runtime.getRuntime().exec(args, env, theDirectory);
							}
							catch (Exception e) {
								_theProcess = Runtime.getRuntime().exec(_invocation, env, theDirectory);
							}		
					
						}
						else
						{
							if (isBashonZ)
							{
								_theProcess = Runtime.getRuntime().exec(_invocation + " --login", env, theDirectory); //$NON-NLS-1$
								didLogin = true;
							}
							else if (isBash)
							{
								_theProcess = Runtime.getRuntime().exec(_invocation + " -l", env, theDirectory);								 //$NON-NLS-1$
								didLogin = true;
							}
							else if (isSHonZ)
							{
								_theProcess = Runtime.getRuntime().exec(_invocation + " -L", env, theDirectory); //$NON-NLS-1$
								didLogin = true;
							}
							else
							{
								_theProcess = Runtime.getRuntime().exec(_invocation, env, theDirectory);
							}
						}
					}
					else
					{
						_isTTY = false;
			
						
						
						//String[] inv = parseArgs(_invocation);
						if (_isTTY)
						{
							String args[] = new String[4];
							args[0] = PSEUDO_TERMINAL;
							args[1] = theShell;
							args[2] = "-c"; //$NON-NLS-1$
							args[3] = _invocation;
					
							_theProcess = Runtime.getRuntime().exec(args, env, theDirectory);
						}
						else
						{
	
							String args[] = new String[3];
							args[0] = theShell;
							args[1] = "-c"; //$NON-NLS-1$
							args[2] = _invocation;
		

							_theProcess = Runtime.getRuntime().exec(args, env, theDirectory);
						}
					}
				}
			}
			else // windows
			{
				if ((theOS.indexOf("95") >= 0) || (theOS.indexOf("98") >= 0) || (theOS.indexOf("ME") >= 0)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				{
					theShell = "start"; //$NON-NLS-1$
				}
				else
				{
					theShell = "cmd"; //$NON-NLS-1$
				}
				if (_invocation.equals(">")) //$NON-NLS-1$
				{
					_invocation = theShell;
					_isShell = true;
				}
				
			
				String args[] = new String[3];
				args[0]= theShell;
				if (theShell.equals("start")) //$NON-NLS-1$
				{
					args[1] = "/B "; //$NON-NLS-1$
				}
				else
				{
					args[1] = "/C "; //$NON-NLS-1$
				}
				args[2] = _invocation;
		
				String[] env = getEnvironment(_subject);

				if (_invocation.equals(theShell))
				{
					
					_theProcess = Runtime.getRuntime().exec(_invocation, env, theDirectory);
				}
				else
				{
					_theProcess = Runtime.getRuntime().exec(args, env, theDirectory);
				}
	
			}

			
			
			OutputStream output = _theProcess.getOutputStream();
			_stdInput = new DataInputStream(_theProcess.getInputStream());
			_stdError = new DataInputStream(_theProcess.getErrorStream());
	
			
			String specialEncoding = System.getProperty("dstore.stdin.encoding"); //$NON-NLS-1$
			
			if (specialEncoding != null)
			{		
			    /*
				_stdInput = new BufferedReader(new InputStreamReader(_theProcess.getInputStream(), specialEncoding));
				_stdError = new BufferedReader(new InputStreamReader(_theProcess.getErrorStream(), specialEncoding));
				*/
				try
				{
					_stdOutput = new BufferedWriter(new OutputStreamWriter(output, specialEncoding));
				}
				catch (UnsupportedEncodingException e)
				{
					_stdOutput = new BufferedWriter(new OutputStreamWriter(output));
				}
			}
			else
			{
				//_stdInput = new BufferedReader(new InputStreamReader(_theProcess.getInputStream()));
				//_stdError = new BufferedReader(new InputStreamReader(_theProcess.getErrorStream()));
				_stdOutput = new BufferedWriter(new OutputStreamWriter(output));
			}
			
			createObject("command", _invocation); //$NON-NLS-1$
			createObject("stdout", ""); //$NON-NLS-1$ //$NON-NLS-2$
						
			status.setAttribute(DE.A_NAME, "progress"); //$NON-NLS-1$
			_dataStore.update(status);
			_dataStore.disconnectObjects(status);
			
			_stdOutputHandler = new OutputHandler(_stdInput, null, _isWindows || _isTTY, false, _isShell, this);
			_stdOutputHandler.setWaitTime(100);
			_stdOutputHandler.setDataStore(_dataStore);
			_stdOutputHandler.start();
				
			_stdErrorHandler = new OutputHandler(_stdError, null, _isWindows || _isTTY, true, _isShell, this);
			_stdErrorHandler.setWaitTime(100);
			_stdOutputHandler.setDataStore(_dataStore);
			_stdErrorHandler.start();
			
			if (didLogin && !userHome.equals(_cwdStr))
			{
				// need to CD to the correct directory
				final String cdCmd = "cd " + _cwdStr; //$NON-NLS-1$
				Thread cdThread = new Thread(
						new Runnable()
						{
							public void run()
							{				
								// wait a second so the profile can complete startup
								try
								{
									sleep(1000);
								}
								catch (Exception e)
								{
									
								}
								sendInput(cdCmd);
							}
						});
				cdThread.start();

			}			
			else if (_isShell && !_isWindows && !_isTTY)
			{				
				createPrompt(_cwdStr +">", _cwdStr); //$NON-NLS-1$
				refreshStatus();
			}

		}
		catch (IOException e) 
		{
			_theProcess = null;
			e.printStackTrace();
			createObject("command", e.getMessage()); //$NON-NLS-1$
			status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
			return;
		} 

	}
	
	
//	private String[] parseArgs(String full)
//	{
//		StringBuffer result = new StringBuffer();
//		char[] chars = full.toCharArray();
//		boolean inQuotes = false;
//		boolean escaping = false;
//		for (int i = 0; i < chars.length; i++)
//		{
//			char c = chars[i];
//			if (c == '\"')
//			{
//				inQuotes = !inQuotes;
//			}
//			else
//			{
//				if (c == '\\')
//				{
//					escaping = true;
//				}
//				else
//				{
//					if (c == ' ')
//					{
//						if (!inQuotes && !escaping)
//						{
//							c = ',';
//						}
//						escaping = false;
//					}
//				}
//				result.append(c);	
//			}
//			
//		}
//		return result.toString().split(",");
//	}
	
	public Process getProcess()
	{
		return _theProcess;
	}

	public String getCWD()
	{
		return _cwdStr;
	}
	
	public void queryCWD()
	{
		BufferedWriter writer = _stdOutput;
		try
		{
			// hidden command
			writer.write("echo '<'PWD=$PWD"); //$NON-NLS-1$
			writer.newLine(); 
			writer.flush();
		}
		catch (Exception e)
		{			
		}
		_didInitialCWDQuery = true;
		
	}




	public void sendInput(String input)
	{
		if (!_isDone)
		{

//			byte[] intoout = input.getBytes();
			input.getBytes();

			try
			{
				BufferedWriter writer = _stdOutput;
			    // pty executable handles the break now
				if (input.equals("#break") && !_isTTY) //$NON-NLS-1$
				{
					return;
				}
				else if (input.equals("#enter")) //$NON-NLS-1$
				{
					writer.newLine();
					writer.flush();
				    return;
				}
			
				if (_isShell)
				{
					if (_lastPrompt != null)
					{
					    if (!_isTTY)
					    {
					        String promptText = _lastPrompt.getName();
					        if (promptText.endsWith(">")) //$NON-NLS-1$
					        {
					            _lastPrompt.setAttribute(DE.A_NAME, promptText + input);
					            _dataStore.refresh(_lastPrompt);
					        }
					        
						else
						{
//						    String cwd = getCWD();
//						    String line = cwd + ">" + input;
						    //createObject("prompt", line);
						    //createPrompt(line, cwd);
						}
					    }
					}

					_patterns.update(input);
				}

				if (!_isWindows && !_isTTY)
				{
					createObject("input", input); //$NON-NLS-1$
				}

				writer.write(input);
				writer.newLine();
				writer.flush();
				

				if (!_isWindows && (input.startsWith("cd ") || input.equals("cd"))) //$NON-NLS-1$ //$NON-NLS-2$
				{
					queryCWD();
				}
				else if (!_didInitialCWDQuery)
				{
					queryCWD();
				}
				if (!_isWindows && !_isTTY)
				{
					// special case for pattern interpretting
					// if cwd is not set, then files aren't resolved
					// create mock prompt to ensure that they do get resolved
					if (input.startsWith("cd ") || input.equals("cd")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						writer.write("echo $PWD'>'"); //$NON-NLS-1$
						writer.newLine(); 
						writer.flush();

						// sleep to allow reader to interpret before going on
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
						}
					}
				}
			}
			catch (IOException e)
			{
			    cleanupThread();
			}
		}
	}
	private String[] getEnvironment(DataElement theSubject)
	{
		//Grab the system environment:
		DataElement envMiner = _dataStore.findMinerInformation(IUniversalDataStoreConstants.UNIVERSAL_ENVIRONMENT_MINER_ID);
		DataElement systemEnv = _dataStore.find(envMiner, DE.A_NAME, "System Environment", 1); //$NON-NLS-1$
		//Walk up until we find an element with an inhabits relationship.
		DataElement theProject = theSubject;
		List projectEnvReference = null;
		while (theProject != null && !theProject.getValue().equals("Data")) //$NON-NLS-1$
		{
			projectEnvReference = theProject.getAssociated("inhabits"); //$NON-NLS-1$
			if (projectEnvReference.size() > 0)
				break;
			theProject = theProject.getParent();
		}
		DataElement projectEnv = null;
		if (projectEnvReference != null && (projectEnvReference.size() > 0))
			projectEnv = (DataElement) projectEnvReference.get(0);
		

		String[] theEnv = mergeEnvironments(systemEnv, projectEnv);
		return theEnv;
	}
	
	private String[] mergeEnvironments(DataElement systemEnv, DataElement projectEnv)
	{

		List prjVars = null;
		List sysVars = null;
		//Fill the ArrayLists with the environment variables
		if (systemEnv != null)
			sysVars = systemEnv.getNestedData();
		if (projectEnv != null)
			prjVars = projectEnv.getNestedData();
		//If one or both of the ArrayLists are null, exit early:
		if ((sysVars == null) || (sysVars.size() == 0))
			return listToArray(prjVars);
		if ((prjVars == null) || (prjVars.size() == 0))
			return listToArray(sysVars);
		//If we get here, then we have both system and project variables...to make merging the 2 lists easier, we'll
		//use a Hashtable (Variable Names are the keys, Variables Values are the values):
		Hashtable varTable = new Hashtable();
	
		//First fill the varTable with the sysVars
		varTable.putAll(mapVars(sysVars));

		//Now for every project variable, check to see if it already exists, and if the value contains other variables:
		for (int i = 0; i < prjVars.size(); i++)
		{
			DataElement envElement = (DataElement) prjVars.get(i);
			if (!envElement.getType().equals("Environment Variable")) //$NON-NLS-1$
				continue;
			String theVariable = envElement.getValue();
			String theKey = getKey(theVariable);
			String theValue = getValue(theVariable);
			theValue = calculateValue(theValue, varTable);
			varTable.put(theKey, theValue);
		}
		

		if (_isTTY)
		{
			varTable.put("PS1","$PWD/>"); //$NON-NLS-1$ //$NON-NLS-2$
			varTable.put("COLUMNS","256"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		

		/*  DKM: for some reason this isn't getting applied properly here
		 * but it works via export
		 * */
		String theOS = System.getProperty("os.name"); //$NON-NLS-1$
		if (theOS.toLowerCase().startsWith("os")) //$NON-NLS-1$
		{
			varTable.put("QIBM_JAVA_STDIO_CONVERT","Y"); //$NON-NLS-1$ //$NON-NLS-2$
    		varTable.put("QIBM_USE_DESCRIPTOR_STDIO","I"); //$NON-NLS-1$ //$NON-NLS-2$
    		varTable.put("PASE_STDIO_ISATTY","N"); //$NON-NLS-1$ //$NON-NLS-2$
    		varTable.put("TERMINAL_TYPE","REMOTE"); //$NON-NLS-1$ //$NON-NLS-2$
    		varTable.put("STDIO_ISATTY","Y"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
	
		
		return tableToArray(varTable);
	} //This method is responsible for replacing variable references with their values.
	//We support 3 methods of referencing a variable (assume we are referencing a variable called FOO):
	// 1. $FOO     - common to most shells (must be followed by a non-alphanumeric or nothing...in other words, we
	//               always construct the longest name after the $)
	// 2. ${FOO}   - used when you want do something like ${FOO}bar, since $FOObar means a variable named FOObar not 
	//               the value of FOO followed by "bar". 
	// 3. %FOO%    - Windows command interpreter
	private String calculateValue(String value, Hashtable theTable)
	{
		value = value.replaceAll("}","\n}"); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuffer theValue = new StringBuffer(value);
		try
		{
			int index = 0;
			char c;
			while (index < theValue.length())
			{
				c = theValue.charAt(index);
				if (c == '{')
				{
					index++;
					c = theValue.charAt(index);
					// skip everything til end quote
					while (index < theValue.length() && c != '}')
					{
						index++;
						c = theValue.charAt(index);
					}
				}
				//If the current char is a $, then look for a { or just match alphanumerics
				else if (c == '$' && !_isWindows)
				{
					int nextIndex = index + 1;
					if (nextIndex < theValue.length())
					{
						c = theValue.charAt(nextIndex);
						//If there is a { then we just look for the closing }, and replace the span with the variable value
						if (c == '{')
						{
							int next = theValue.toString().indexOf("}", nextIndex); //$NON-NLS-1$
							if (next > 0)
							{
								String replacementValue = findValue(theValue.substring(nextIndex + 1, next), theTable, true);
								theValue.replace(index, next + 1, replacementValue);
								index += replacementValue.length() - 1;
							}
						} //If there is no { then we just keep matching alphanumerics to construct the longest possible variable name
						else
						{
							if (Character.isJavaIdentifierStart(c))
							{
								while (nextIndex + 1 < theValue.length() && (Character.isJavaIdentifierPart(c)))
								{
									nextIndex++;
									c = theValue.charAt(nextIndex);							
								}
				
								String v = theValue.substring(index + 1, nextIndex);
								String replacementValue = findValue(v, theTable, true);
								theValue.replace(index, nextIndex, replacementValue);
								index += replacementValue.length() - 1;
							}
						}
					}
				} //If the current char is a %, then simply look for a matching %
				else if (c == '%' && _isWindows)
				{
					int next = theValue.toString().indexOf("%", index + 1); //$NON-NLS-1$
					if (next > 0)
					{
						String replacementValue = findValue(theValue.substring(index + 1, next), theTable, false);
						theValue.replace(index, next + 1, replacementValue);
						index += replacementValue.length() - 1;
					}
				}
				else if (c == '"')
				{
					index++;
					c = theValue.charAt(index);
					// skip everything til end quote
					while (index < theValue.length() && c != '"')
					{
						index++;
						c = theValue.charAt(index);
					}
	
				}
				
				index++;
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return theValue.toString();
	}
	private String findValue(String key, Hashtable theTable, boolean caseSensitive)
	{
		Object theValue = null;
		if (caseSensitive)
			theValue = theTable.get(key);
		else
		{
			String matchString = key.toUpperCase();
			for (Enumeration e = theTable.keys(); e.hasMoreElements();)
			{
				String theKey = (String) e.nextElement();
				if (matchString.equals(theKey.toUpperCase()))
					theValue =  theTable.get(theKey);
			}
		}
		if (theValue == null)
			return ""; //$NON-NLS-1$
		return (String) theValue;
	}
	private String getKey(String var)
	{
		int index = var.indexOf("="); //$NON-NLS-1$
		if (index < 0)
			return var;
		return var.substring(0, index);
	}
	private String getValue(String var)
	{
		var = var.replaceAll("}","\n}"); //$NON-NLS-1$ //$NON-NLS-2$
		int index = var.indexOf("=") + 1; //$NON-NLS-1$
		int varLength = var.length();
		if ((index < 1) || (index == var.length()))
			return ""; //$NON-NLS-1$
		return var.substring(index, varLength);
	}
	private Hashtable mapVars(List theVars)
	{
		Hashtable theTable = new Hashtable();
		int theSize = theVars.size();
		for (int i = 0; i < theSize; i++)
		{
			String theVar = ((DataElement) theVars.get(i)).getValue();
			theTable.put(getKey(theVar), getValue(theVar));
		}
		return theTable;
	}
	private String[] listToArray(List theList)
	{
		if (theList == null)
			theList = new ArrayList();
		int theSize = theList.size();
		String theArray[] = new String[theSize];
		for (int i = 0; i < theSize; i++)
			theArray[i] = ((DataElement) theList.get(i)).getValue();
		return theArray;
	}
	private String[] tableToArray(Hashtable theTable)
	{
		if (theTable == null)
			theTable = new Hashtable();
		int theSize = theTable.size();
		String theArray[] = new String[theSize];
		int i = 0;
		for (Enumeration e = theTable.keys(); e.hasMoreElements();)
		{
			String theKey = (String) e.nextElement();
			String theValue = (String) theTable.get(theKey);
			theArray[i++] = theKey + "=" + theValue; //$NON-NLS-1$
		}
		return theArray;
	}
	public boolean doThreadedWork()
	{
	
		if (((_stdOutputHandler == null) || _stdOutputHandler.isFinished()) && ((_stdErrorHandler == null) || _stdErrorHandler.isFinished()))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	public void initializeThread()
	{
	}
	
	public void sendExit()
	{
	    if (_isShell)
	    {
	        sendInput("exit"); //$NON-NLS-1$
	        
	        // in case exit doesn't end it
	        try
	        {
	        	Thread.sleep(1000);
	        }
	        catch (Exception e)
	        {
	        	
	        }
	        if (_stdOutputHandler.isAlive())
	        {
	        	_theProcess.destroy();
	        }
	    }
	}
	

	
	public void cleanupThread()
	{
		
		_isDone = true;
		try
		{

		

	    /*
		if (_isShell)
		{
			sendInput("#exit");
		}*/

			if (_theProcess != null)
			{
				int exitcode;
				try
				{
					if (_isCancelled)
					{
						_theProcess.destroy();
					}
					else
					{
						exitcode = _theProcess.exitValue();
						createObject("prompt", "> Shell Completed (exit code = " + exitcode + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
				catch (IllegalThreadStateException e)
				{ //e.printStackTrace();
					exitcode = -1;
					_theProcess.destroy();
				}
				_theProcess = null;
			}
			

			_stdOutputHandler.finish();
			_stdErrorHandler.finish();
			_stdInput.close();
			_stdError.close();
			
			_status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
			_dataStore.refresh(_status);
		
		// disconnecting all
	
		_dataStore.disconnectObjects(_status);
		

		// clean up the associated environment
		List projectEnvReference = _subject.getAssociated("inhabits"); //$NON-NLS-1$

		if (projectEnvReference != null)
		{
			DataElement env = (DataElement)projectEnvReference.get(0);
			DataElement envParent = env.getParent();			
			_dataStore.deleteObject(envParent, env);
			_dataStore.refresh(envParent);
		}
		_dataStore.disconnectObject(_subject); //bug 70420
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}				
	}
	
	
	public void interpretLine(String line, boolean stdError)
	{
		int maxLine = 100;
		int num = line.length();
		String[] lines = new String[num/maxLine+1];
		if(lines.length>1)
		{
			int beg=0;
			int end=maxLine;
			for(int i=0;i<lines.length;i++)
			{
				//try/catch put in for testing purposes
				//try
				//{
					if(end>line.length())
					{
						lines[i]=line.substring(beg);
					}
					else
					{
						lines[i]=line.substring(beg,end);
					}
					beg=end;
					end=end+maxLine;
				//}
				//catch(Exception e)
				//{
				//	createObject(_descriptors._stdout, "<<EXCEPTION>> line:= " + num + " beg : " + beg + " end = " + end);
				//	return;
				//}
			}		
		}
		else
		{
			lines[0]=line;
		}
		
		for(int i=0;i<lines.length;i++)
		{	
			line=lines[i];
			
			// for prompting
			if (line.startsWith("<PWD")) //$NON-NLS-1$
			{
				// special processing
				String statement = line.substring(1);
				String pair[] = statement.split("="); //$NON-NLS-1$
				String value = pair[1];
				_status.setAttribute(DE.A_SOURCE, value);
			
				return;
			}
			if (line.indexOf("echo '<'PWD=$PWD") > 0) //$NON-NLS-1$
			{
				// ignore this line
			}
			else
			{
				ParsedOutput parsedMsg = null;
			
				try
				{
					parsedMsg = _patterns.matchLine(removeWhitespace(line));
	 			}
				catch (Throwable e) 
				{
					e.printStackTrace();
				}
				if (parsedMsg == null)
				{
					if (stdError)
					{
						createObject(_descriptors._stderr, line);
					}
					else
					{
						createObject(_descriptors._stdout, line);
					}
				}
				else
				{		    		    		    		    
					try
					{	    			    
						String fileName = parsedMsg.file;  
						if (parsedMsg.type.equals("prompt")) //$NON-NLS-1$
						{
							int tildaIndex = fileName.indexOf("~"); //$NON-NLS-1$
							if (tildaIndex == 0)
							{
								String userHome = null;
								
								if (_dataStore.getClient() != null){
									userHome = _dataStore.getClient().getProperty("user.home"); //$NON-NLS-1$
								}
								else {
									userHome = System.getProperty("user.home"); //$NON-NLS-1$
								}
								
								fileName = userHome + fileName.substring(1);
							}
							
							
							File promptFile = new File(fileName);
							if (promptFile.exists())
							{
								createPrompt(line, fileName);
							}
							else
							{
								createObject(_descriptors._stdout, line);
							}
						}
						else if (parsedMsg.type.equals("file")) //$NON-NLS-1$
						{
							createObject(parsedMsg.type, line, fileName, null);				    
						}
						else
						{
							createObject(parsedMsg.type, line, fileName, new Integer(parsedMsg.line));
						}
					}
					catch (NumberFormatException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		// moving this to do refresh after serious of lines interpretted
		//refreshStatus();
	}
	
	public void refreshStatus() 
	{
		_dataStore.refresh(_status);
	}
	
	public void createPrompt(String line, String fileName)
	{
		// prevent duplicate prompts    
		DataElement object = null;
		int size = _status.getNestedSize();
		if (size > 0)
		{
			DataElement lastObject = _status.get(size - 1);
			if (!lastObject.getType().equals("prompt")) //$NON-NLS-1$
			{
			    line = line.replaceAll("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
			    fileName = fileName.replaceAll("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
				object = createObject("prompt", line, fileName, null); //$NON-NLS-1$
			
				_lastPrompt = object;
				_cwdStr = object.getSource();
				_status.setAttribute(DE.A_SOURCE, fileName);
			}
		}
	}

	public String removeWhitespace(String theLine)
	{
		StringBuffer strippedLine = new StringBuffer();
		boolean inWhitespace = true;
		char curChar;
		for (int i = 0; i < theLine.length(); i++)
		{
			curChar = theLine.charAt(i);
			if (curChar == '\t')
			{
				if (!inWhitespace)
				{
					strippedLine.append(' ');
					inWhitespace = true;
				}
			}
			else if (curChar == ' ')
			{
				if (!inWhitespace)
				{
					strippedLine.append(' ');
					inWhitespace = true;
				}
			}
			else
			{
				strippedLine.append(curChar);
				inWhitespace = false;
			}
		}
		return strippedLine.toString();
	}

	/************************************************************************************************
						   private void createObject (String,String)
						   Create a simple object with no source information
	*************************************************************************************************/
	public DataElement createObject(String type, String text)
	{
	    DataElement newObj = null;
	    DataElement descriptorType = _descriptors.getDescriptorFor(type);
	    if (descriptorType != null)
	    {
	        newObj = _dataStore.createObject(_status, descriptorType, text, ""); //$NON-NLS-1$
	    }
	    else
	    {
	        newObj = _dataStore.createObject(_status, type, text, ""); //$NON-NLS-1$
	    }
		return newObj;
	}

	public DataElement createObject(DataElement type, String text)
	{
		return _dataStore.createObject(_status, type, text, ""); //$NON-NLS-1$
	}
	
	/************************************************************************************************
						   private void createObject (String,String,String,Integer,Integer)
						  
						   Create an object that can contain file information as well as line an column.            
						   Note: currently our editors do not support jumping to a column, so neither
						   do we here.
	*************************************************************************************************/
	private DataElement createObject(String type, String text, String file, Integer line)
	{
	    DataElement descriptorType = null;
		if (file != null && file.length() > 0)
		{
		    boolean foundFile = false;
			String expectedPath = null;
			File aFile = new File(file);
			if (type.equals("prompt")) //$NON-NLS-1$
			 {
			    descriptorType = _descriptors._prompt;
				expectedPath = file;
				_cwdStr = file.replaceAll("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
			 }
			 else if (aFile.exists())
			 {
			   expectedPath = aFile.getAbsolutePath();
			   file = expectedPath;
			   if (aFile.isDirectory() && type.equals("file")) //$NON-NLS-1$
			   {
			       type = "directory"; //$NON-NLS-1$
			   }
			   foundFile = true;
			 }
			 else if (_cwdStr.endsWith("/")) //$NON-NLS-1$
			 {
			     if (file.equals("/")) //$NON-NLS-1$
			     {
			         expectedPath = _cwdStr;
			     }
			     else
			     {
			         expectedPath = _cwdStr + file;
			     }
			 }
			 else 
			 {
			     expectedPath = _cwdStr + "/" + file; //$NON-NLS-1$
			   }
		
			if (!foundFile)
			{
			
				File qfile = new File(expectedPath);
				if (!qfile.exists())
				{
					expectedPath = file;
					qfile = new File(expectedPath);
					if (qfile.exists())
					{
						if (qfile.isDirectory() && type.equals("file")) //$NON-NLS-1$
						{
							type = "directory"; //$NON-NLS-1$
						}
					}
					else
					{
						File cwdFile = new File(_cwdStr);
						String cwdParent = cwdFile.getAbsolutePath();
						if (cwdFile.getParent() != null)
						{
							cwdParent = cwdFile.getParentFile().getAbsolutePath();
						}
	
						if (cwdParent.endsWith("/")) //$NON-NLS-1$
						{
							expectedPath = cwdParent + file;
						}
						else
						{
							expectedPath = cwdParent + "/" + file; //$NON-NLS-1$
						}
	
						qfile = new File(expectedPath);
						if (qfile.exists())
						{
							if (qfile.isDirectory() && type.equals("file")) //$NON-NLS-1$
							{
								type = "directory"; //$NON-NLS-1$
							}
							file = expectedPath;
						}
						else
						{
						    // no match, so can't be a file
						    if (type.equals("file")) //$NON-NLS-1$
						    {
						        type = "stdout"; //$NON-NLS-1$
						        descriptorType = _descriptors._stdout;
						    }
						    else if (type.equals("error")) //$NON-NLS-1$
						    {
						        type = "stderr"; //$NON-NLS-1$
						        descriptorType = _descriptors._stderr;
						    }
						    else
						    {
						        type = "stdout"; //$NON-NLS-1$
						        descriptorType = _descriptors._stdout;
						    }
						}
					}
				}
				else
				{
					if (qfile.isDirectory() && type.equals("file")) //$NON-NLS-1$
					{
						type = "directory"; //$NON-NLS-1$
						expectedPath = expectedPath.replaceAll("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					file = expectedPath;
				}
			}
		
			
			DataElement obj = null;
			if (line == null || (line.intValue() == 1))
			{
			    if (descriptorType != null)
			    {
			        obj = _dataStore.createObject(_status, descriptorType, text, file); 
			    }
			    else
			    {
			        obj = _dataStore.createObject(_status, type, text, file);
			    }
			}
			else
			{
			    if (descriptorType != null)
			    {
			        obj = _dataStore.createObject(_status, descriptorType, text, file);
			    }
			    else
			    {
			        obj = _dataStore.createObject(_status, type, text, file);
			    }
			    obj.setAttribute(DE.A_SOURCE, obj.getSource() + ':' + line.toString());				
			}

			return obj;
		}
		else
		{
		    
			return createObject(type, text);
		}
	}
}
