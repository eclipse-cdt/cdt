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

package org.eclipse.rse.services.files.ftp;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FTPLinuxDirectoryListingParser implements IFTPDirectoryListingParser 
{

	public FTPHostFile getFTPHostFile(String line, String parentPath) 
	{
		// Note this assumes that text is always formatted the same way
		if (line == null) return null;
		String[] tokens = line.split("\\s+", 9);
		if (tokens.length < 9) return null;
		String name = tokens[8];
		boolean isDirectory = line.charAt(0) == 'd';	
		long length = 0;
		long lastMod = 0;
		if (tokens.length > 4)
		{
			try
			{
				length = Long.parseLong(tokens[tokens.length - 5]);
			}
			catch (NumberFormatException e) {}
			
			try
			{
				int i = tokens.length - 4;
				int j = tokens.length - 3;
				int k = tokens.length - 2;
				String time = "";
				String year = "";
				if (tokens[k].indexOf(":") == -1)
				{
					time = "11:59 PM";
					year = tokens[k];
				}
				else
				{
					String[] parts = tokens[k].split(":");
					int hours = Integer.parseInt(parts[0]);
					boolean morning = hours < 12; // assumes midnight is 00:00
					if (morning) 
					{
						if (hours == 0)
						{
							hours = 12;
						}
					}
					time = hours + ":" + parts[1] + (morning? " AM" : " PM");
					year = "" + (Calendar.getInstance().get(Calendar.YEAR));
				}
				
				String date = tokens[i] + " " + tokens[j] + ", " + year + " " + time;
				lastMod = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, new Locale("EN", "US")).parse(date).getTime();
			}
			catch (Exception e) {}
		}
		return new FTPHostFile(parentPath, name, isDirectory, false, lastMod, length);
	}

}