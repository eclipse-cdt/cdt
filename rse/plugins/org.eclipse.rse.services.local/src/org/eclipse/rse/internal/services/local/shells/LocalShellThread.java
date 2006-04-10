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
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.eclipse.core.runtime.Platform;

/**
 * The LocalCommandThread class is used for running and interacting with a
 * local command shell.
 */
public class LocalShellThread extends Thread
{

	private volatile Thread _commandThread;
	protected boolean _isCancelled;

	private String _cwd;
	private String _invocation;
	private String[] _envVars;
	private String PSEUDO_TERMINAL;


	private boolean _isShell;
	private boolean _isDone;

	private Process _theProcess;


	private boolean _isTTY = false;

	private boolean _isWindows;
	private String  _encoding;
	
	private BufferedReader _stdInput;
	private BufferedReader _stdError;
	
	/**
	 * consturtor for local command shell monitor
	 * @param fileSubSystem associated local file subsystem
	 * @param cwd initial working directory
	 * @param invocation launch shell command
	 * @param encoding
	 * @param patterns patterns file for output interpretation
	 * @param envVars user and system environment variables to launch shell with
	 */
	public LocalShellThread(String cwd, String invocation, String encoding, String[] envVars)
	{
		super();
		_encoding = encoding;
		_isCancelled = false;
		_cwd = cwd;
		_invocation = invocation;
		
		// if pty exists for this client
		// then the rse.pty property will have been set
		// by the contributor of the pty exectuable
		// on linux client this is a likely scenario
		PSEUDO_TERMINAL = System.getProperty("rse.pty");
		//PSEUDO_TERMINAL ="platform:/base/plugins/com.ibm.etools.power.universal.server/server.components/intellinux/pty/pty";
		try
		{
			PSEUDO_TERMINAL = Platform.resolve(new URL(PSEUDO_TERMINAL)).getPath();
		}
		catch (Exception e)
		{	
		}
	
		_envVars = envVars;
		init();
	}


	public boolean isShell()
	{
		return _isShell;
	}

	public boolean isWindows()
	{
		return _isWindows;
	}

	public boolean isDone()
	{
		return _isDone || _isCancelled;
	}

	public String getInvocation()
	{
		return _invocation;
	}

	public String getCWD()
	{
		return _cwd;
	}

	public void setCWD(String cwd)
	{
		_cwd = cwd;
	}

	private void init()
	{
		try
		{
			File theDirectory = new File(_cwd);
			if (!theDirectory.isDirectory())
				theDirectory = theDirectory.getParentFile();
			String theOS = System.getProperty("os.name");
			_isWindows = theOS.toLowerCase().startsWith("win");
			_isTTY = PSEUDO_TERMINAL != null && (new File(PSEUDO_TERMINAL).exists());
			
			String theShell = null;

			if (!_isWindows)
			{
				String[] envVars = getEnvironmentVariables(false);
				if (theShell == null)
				{
				    if (_isTTY)
				    {
				        if (_invocation.equals(">"))
						{
							_invocation = "sh";
							_isShell = true;
						}
				     
						String args[] = new String[2];
						args[0] = PSEUDO_TERMINAL;
						args[1] = _invocation;
					    			
						_theProcess = Runtime.getRuntime().exec(args, envVars, theDirectory);
				    }
				    else
				    {
						if (_invocation.equals(">"))
						{
							_invocation = "sh";
							_isShell = true;
						}
						String args[] = new String[1];
						args[0] = _invocation;
					    			
						_theProcess = Runtime.getRuntime().exec(args[0], envVars, theDirectory);
				    }
				}
				else
				{
				    if (_isTTY)
				    {
				        if (_invocation.equals(">"))
						{
							_invocation = theShell;
							_isShell = true;
						}
			
						String args[] = new String[4];
						args[0] = PSEUDO_TERMINAL;
						args[1] = theShell;
						args[2] = "-c";
						args[3] = _invocation;
	
						_theProcess = Runtime.getRuntime().exec(args, envVars, theDirectory);
				    }
				    else
				    {
					    if (_invocation.equals(">"))
						{
							_invocation = theShell;
							_isShell = true;
						}
	
						String args[] = new String[3];
						args[0] = theShell;
						args[1] = "-c";
						args[2] = _invocation;
	
						_theProcess = Runtime.getRuntime().exec(args, envVars, theDirectory);
				    }
				    
				}
			}
			else
			{
				String[] envVars = getEnvironmentVariables(true);
				if ((theOS.indexOf("95") >= 0) || (theOS.indexOf("98") >= 0) || (theOS.indexOf("ME") >= 0))
				{
					theShell = "start";
				}
				else
				{
					theShell = "cmd";
				}

				if (_invocation.equals(">"))
				{
					_invocation = theShell;
					_isShell = true;
				}

				if (theShell.equals("start"))
				{
					theShell += " /B ";
				}
				else
				{
					theShell += " /C ";
				}

				_theProcess = Runtime.getRuntime().exec(theShell + _invocation, envVars, theDirectory);
			}

			// determine the windows encoding
			if (_encoding == null || _encoding.length() == 0)
			{
				try
				{
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os);
					_encoding = osw.getEncoding();
					osw.close();
					os.close();
				}
				catch (Exception x)
				{
				}
				if (_encoding == null)
				{		
					if (_encoding == null || _encoding.length() == 0)
					{
						_encoding = System.getProperty("file.encoding");
					}
				}
			}

			if (_encoding.equals("Cp1252") && !theOS.startsWith("Windows XP"))
			{
				_stdInput = new BufferedReader(new InputStreamReader(_theProcess.getInputStream(), "Cp850"));
				_stdError = new BufferedReader(new InputStreamReader(_theProcess.getErrorStream(), "Cp850"));
			}
			else
			{
				_stdInput = new BufferedReader(new InputStreamReader(_theProcess.getInputStream()));
				_stdError = new BufferedReader(new InputStreamReader(_theProcess.getErrorStream()));
			}

		}
		catch (IOException e)
		{
			_theProcess = null;
			e.printStackTrace();
			return;
		}


	}
	
	public BufferedReader getOutputStream()
	{
		return _stdInput;
	}
	
	public BufferedReader getErrorStream()
	{
		return _stdError;
	}
	
	

	public synchronized void stopThread()
	{
		if (_commandThread != null)
		{
			_isCancelled = true;

			try
			{
				_commandThread = null;
			}
			catch (Exception e)
			{
				System.out.println(e);
			}

		}
		notify();
	}

	public void sendInput(String input)
	{
		if (!_isDone)
		{
			byte[] intoout = input.getBytes();
			OutputStream output = _theProcess.getOutputStream();

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

			try
			{

				writer.write(input);
				writer.write('\n');
				writer.flush();

				if (!_isWindows && !_isTTY)
				{
					// special case for pattern interpretting
					// if cwd is not set, then files aren't resolved
					// create mock prompt to ensure that they do get resolved
					if (input.startsWith("cd ") || input.equals("cd"))
					{
						writer.write("echo $PWD'>'");
						writer.write('\n');
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
				System.out.println(e);
			}

		}
	}

	public void run()
	{
		Thread thisThread = Thread.currentThread();
		_commandThread = thisThread;


		while (_commandThread != null && _commandThread == thisThread && _commandThread.isAlive() && !_isCancelled)
		{
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{
				System.out.println(e);
			}

			//This function is where the Threads do real work, and return false when finished
			if (!doThreadedWork())
			{
				try
				{
					_commandThread = null;
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
			}
			else
			{
			}
		}

		//This function lets derived classes cleanup or whatever
		cleanupThread();
	}

	public boolean doThreadedWork()
	{
		if (_stdInput == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public void cleanupThread()
	{
		if (_isShell)
		{
			sendInput("exit");
		}

		_isDone = true;
		try 
		{
			_stdInput.close();
			_stdError.close();

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
					}
				}
				catch (IllegalThreadStateException e)
				{
					//e.printStackTrace();
					exitcode = -1;
					_theProcess.destroy();
				}
				_theProcess = null;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	
	public String getPathEnvironmentVariable()
	{
		String[] vars = _envVars;
		if (vars != null)
		{
		
			for (int i = 0; i < vars.length; i++)
			{
				String var = vars[i].toUpperCase();
				if (var.startsWith("PATH="))
				{
					return var;
				}
			}
		
		}
		return null;
	}

	/**
	 * Retrieve the system environment variables and append the user defined
	 * environment variables to create the String array that can be passed to 
	 * Runtime.exec().  We need to retrieve the system env vars because the 
	 * env vars passed to Runtime.exec() prevent the system ones from being
	 * inherited.
	 */
	private String[] getEnvironmentVariables(boolean windows)
	{
		if (_isTTY)
		{
			String[] newEnv = new String[_envVars.length + 1];
			for (int i = 0; i < _envVars.length; i++)
				newEnv[i] = _envVars[i];
			newEnv[_envVars.length] = "PS1=$PWD/>";
			_envVars = newEnv;
		}
		return _envVars;
	}
		

}