/********************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David McKnight   (IBM)        - [276194] cannot open file with '...' in pathname
 * David McKnight   (IBM)        - [332275] "#" in local file name is changed to "#035" after copy
 ********************************************************************************/

package org.eclipse.rse.core.model;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;

/**
 * This is a utility class used in the construction of file names.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SystemEscapeCharHelper {

	private static char ESCAPE_CHAR = '#';
	
	private char changedChars[];
	private int escapeStringLength;

	/**
	 * Constructor.
	 * @param chars array of chars to be escaped.
	 */
	public SystemEscapeCharHelper (char[] chars)
	{
		changedChars = new char[chars.length+1];

		for (int i = 0; i < chars.length; i++)
		{
			changedChars[i]=chars[i];
		}

		// add the escape character itself so that it itself can be escaped
		changedChars[chars.length]=ESCAPE_CHAR;

		escapeStringLength = 4;

	}

	public String getStringForFileName(String name)
	{
		// first do a validation before escaping
		IStatus status = ResourcesPlugin.getWorkspace().validatePath(name,IResource.FILE);
		if (status.getCode() == IStatus.OK){ // if it's valid, don't bother escaping
			return name;
		}
		
		String fileName = name;

		int i = 0;

		while (i < fileName.length())
		{			
			char currentChar = fileName.charAt(i);
			for (int j = 0; j < changedChars.length; j++)
			{				
				if (currentChar == changedChars[j])
				{
					if ((fileName.length()-1) >= i)
					{
						fileName = fileName.substring(0, i) + escapeString(changedChars[j]) + fileName.substring(i+1);
					}
					else
					{
						fileName = fileName.substring(0, i) + escapeString(changedChars[j]);
					}
					i = i + escapeStringLength-1;
				}
				else if (currentChar == '.'){ // special case for bug 276194
					if (fileName.length() > i + 1){
						char nextChar = fileName.charAt(i + 1);
						if (nextChar == '.' || nextChar == File.separatorChar){
							fileName = fileName.substring(0, i) + escapeString(currentChar) + fileName.substring(i+1);
						}
					}
				}
			}
         i++;
		}

		return fileName;

	}

	public String getStringFromFileName(String fileName)
	{
		String name = fileName;

		int i = 0;
		while (i < name.length())
		{
			if (name.charAt(i) == ESCAPE_CHAR)
			{
				if ((name.length()-2) >= i)
				{
					name = name.substring(0, i) + originalString(name.substring(i+1, i+escapeStringLength)) + name.substring(i+escapeStringLength);
				}
				else
				{
					name = name.substring(0, i) + originalString(name.substring(i+1));
				}
			}
			i++;
		}

		return name;
	}

	private String escapeString(char c)
	{
	/*	for (int i = 0; i < changedChars.length; i++)
		{
			if (changedChars[i]== c)
			{
				return ""+ESCAPE_CHAR+i;
			}
		}
		return ""+c;
	*/

      int intValue = c;
      String returnStr=""+ESCAPE_CHAR; //$NON-NLS-1$

      if (intValue < 10)
         returnStr = returnStr+"00"; //$NON-NLS-1$
      else if (intValue < 100)
         returnStr = returnStr+"0"; //$NON-NLS-1$

     return returnStr + intValue;

	}

	private String originalString(String s)
	{
	 //	return ""+changedChars[Integer.parseInt(s)];

      char c = (char)Integer.parseInt(s);

      return ""+c; //$NON-NLS-1$
	}



/* TEST HARNESS */
/* public static void main(String[] args)
   {
    try {
      char [] charArray = new char[1];

      charArray[0]='\'';
      SystemEscapeCharHelper helper = new 
SystemEscapeCharHelper(charArray);

      System.out.println(">>>>>start>>>>>");


      String[] strings = {"'hello_world'", "'", "'abc'", "bca", "ca'_'b"};
      for (int i = 0; i < strings.length; i++)
      {
      String escaped = helper.getStringForFileName(strings[i]);

      System.out.println("escaped:"+escaped+":");

      String unescaped = helper.getStringFromFileName(escaped);

      System.out.println("unescaped:"+unescaped+":");
      System.out.println("***");
      }
      System.out.println("*****end*****");


   } catch  (Exception e){
      System.out.println(""+e.toString());

//      try{
//      System.in.read();
//      }catch(Exception ex)
//      {
//            }

   }
//      try{
//         System.in.read();
//      }catch(Exception e)
//      {}
}*/
}