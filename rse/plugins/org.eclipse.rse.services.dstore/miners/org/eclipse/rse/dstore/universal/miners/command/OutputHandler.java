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

package org.eclipse.rse.dstore.universal.miners.command;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.dstore.core.model.Handler;

/**
 * The OutputHandler class is used to listen to a particular output or error stream, 
 * interpret that information and create DataElements for it for use on the client.
 */
public class OutputHandler extends Handler
{

	private DataInputStream _reader;
	private boolean _isStdError;
	private boolean _isTerminal;

	private CommandMinerThread _commandThread;
	private boolean _isShell;
//	private static int MAX_OFFSET = 10000;
	private boolean _endOfStream = false;
	
	
	private List _encodings;


	public OutputHandler(DataInputStream reader, String qualifier, boolean isTerminal, boolean isStdError, boolean isShell, CommandMinerThread commandThread)
	{
		_reader = reader;
		_isStdError = isStdError;
		_isTerminal = isTerminal;
		_commandThread = commandThread;
		_isShell = isShell;
		_encodings = new ArrayList();
		String system = System.getProperty("os.name").toLowerCase();
		
		if (system.startsWith("z"))
		{
			_encodings.add("IBM-1047");
			/*
		    _encodings.add("Cp1047");
		    _encodings.add("Cp037");
		    _encodings.add("UTF8");
		    */
		}
		else
		{
		String specialEncoding = System.getProperty("dstore.stdin.encoding");
		if (specialEncoding != null)
		{
		    _encodings.add(specialEncoding);		    
		}
		_encodings.add(System.getProperty("file.encoding"));
		}
	
	}

	public void newCommand()
	{
	}

	public void handle()
	{
	    String[] lines = readLines();
		if (lines != null)
		{
		
		    /*
			if (lines.length == 0)
			{
				_reader.
			}
				
				// don't do anything unless we require output	
				if (_newCommand && !_isTerminal)
				{
					doPrompt();
				}
			}
			else
			*/
		    for (int i = 0; i < lines.length; i++)
			{
		        String line = lines[i];
				_commandThread.interpretLine(line, _isStdError);
			}
			if (!_isTerminal)
				doPrompt();		
		}
		else
		{
			finish();
		}
	}

	private void doPrompt()
	{
		try
		{
			if ((_reader.available() == 0) && !_isStdError && _isShell)
			{
				if (!_isTerminal)
				{
					try
					{
						Thread.sleep(500);
						if (_reader.available() == 0)
						{
							// create fake prompt 					 
							_commandThread.createPrompt(_commandThread.getCWD() + '>', _commandThread.getCWD());
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public synchronized void waitForInput()
	{
		try
		{
			Thread.sleep(100);
		}
		catch (Exception e)
		{
			
		}
	}
	
	
	private String[] readLines()
	{
		if (_endOfStream)
		{
			return null;
		}
	    String[] output = null;
	  
	  try 
	  {
	   
	   // find out how many bytes are available to be read
	   int available = _reader.available();
	   int lookahead = 0;
	   
	   // if there's none, wait a bit and return true to continue
	   if (available <= 0) 
	   {
	    sleep(100);
		available = _reader.available();

			if (available == 0)
			{
				lookahead = _reader.read();
				if (lookahead == -1)
				{
					return null;
				}
				else
				{
					available = _reader.available() + 1;
				}
			}
	   }
	   
	   byte[] readBytes = new byte[available];

	   // read the available bytes
	   int numRead = 0;
	   if (lookahead > 0)
	   {
		   readBytes[0] = (byte)lookahead;
		   numRead = _reader.read(readBytes, 1, available - 1) + 1;
	   }
	   else
	   {
		   numRead = _reader.read(readBytes, 0, available);
	   }
	   
	   // if we've reached end of stream, quit
	   if (numRead == -1) 
	   {
	    return null;
	   }
	 

	   if (readBytes[numRead - 1]== -1)
	   {
		   _endOfStream = true;
	   }
	   
	   // use various encodings as a precaution
	   // note that the first encoding will be the encoding that we were given
	   int encodingIndex = 0;
	   
	   while (encodingIndex < _encodings.size())
	   {
	    String encoding = (String)(_encodings.get(encodingIndex));
	    
	    // get the output using the encoding
	    try
	    {
	    String fullOutput = new String(readBytes, 0, numRead, encoding);
	    
	    // if output is not null, we assume the encoding was correct and process the output
	    if (fullOutput != null /*&& fullOutput.length() == numRead*/) 
	    {   
	     // tokenize the output so that we can get each line of output
	     // the delimiters are therefore set to "\n\r"
	     StringTokenizer tokenizer = new StringTokenizer(fullOutput, "\n\r");
	     int numTokens = tokenizer.countTokens();
	     output = new String[numTokens];
	     int index = 0;
	     while (tokenizer.hasMoreTokens()) 
	     {	     
	      output[index] = tokenizer.nextToken();
	      index++;
	     }
		 
		 
	     return output;
	    }
	    }
	    catch (Exception e)
	    {	        
	    }
	   }
	  }
	  catch (Exception e)
	  {
	      
	  }
	  return output;
	}
/*
	private String readLine()
	{

		
		int ch;
		boolean done = false;
		int byteArrayOffset = 0;
		while (!done && !isFinished() && (byteArrayOffset < MAX_OFFSET))
		{
			try
			{
				//synchronized (_reader)
				{
					
					if (byteArrayOffset > 0 && (_reader.available() == 0))
					{
						try
						{
							Thread.sleep(_waitIncrement);
						}
						catch (InterruptedException e)
						{
						}
						if (_reader.available() == 0)
						{
							_isWaiting = true;
							done = true;
							//System.out.println("return nothiong");
							//return "";
						}
						
					}
				
					ch = _reader.read();
					
					_isWaiting = false;
					switch (ch)
					{
						case -1 :
						case 65535 :
							if (byteArrayOffset == 0) //End of Reader 
							{
							    return null;
							}
							done = true;
							break;

						case 10 : // new line
						case 13 : // carriage return
							done = true; //Newline
							break;

						case 27:
							break;
							
						case 9 :
						    

						    // DKM - test - can we preserve tabs?
						    _byteArray[byteArrayOffset++] = (byte)ch;
						    
							//theLine.append("     "); //Tab
							break;
							
						default :
							char tch = (char) ch;
							if (!Character.isISOControl(tch))
							{
								//System.out.println("char="+tch);
								_byteArray[byteArrayOffset++] = (byte)ch;
							}
							else
							{
								//System.out.println("ignoring:"+ch);
								// ignore next char too
								if (_reader.available() > 0)
									_reader.read();
							}
							break;
					}
					
					
					//Check to see if the BufferedReader is still ready which means there are more characters 
					//in the Buffer...If not, then we assume it is waiting for input.
					if (_reader.available() == 0)
					{
						//wait to make sure 					
						try
						{
							Thread.sleep(_waitIncrement);
						}
						catch (InterruptedException e)
						{
						}
						if (_reader.available() == 0)
						{
							_isWaiting = true;
							done = true;
						}
					}
				}
			}
			catch (IOException e)
			{
				return null;
			}
		}
		
		String lineObject = null;
		if (byteArrayOffset > 0)
		{
			
				int encodingIndex = 0;
				//printEncodedLines(_byteArray, 0, byteArrayOffset);
			
								
				while (lineObject == null && encodingIndex < _encodings.size())
				{
					lineObject = getEncodedLine(_byteArray, 0, byteArrayOffset, (String) _encodings.get(encodingIndex));
					encodingIndex++;
				}
				
				if (lineObject == null)
				{
				    lineObject = new String(_byteArray, 0, byteArrayOffset);
				}
		}
		else
		{
			lineObject ="";
		}


		return lineObject;
	}
	*/
}