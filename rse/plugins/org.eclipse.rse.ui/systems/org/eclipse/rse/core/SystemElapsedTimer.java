/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core;
import java.io.PrintWriter;

/**
 * A utility helper class to help when making timings of potentially
 * long operations, such as remote system calls.
 */
public class SystemElapsedTimer
{



	private long startTime;
	private long endTime;

	public SystemElapsedTimer()
	{
		setStartTime();
	}
	public void setStartTime()
	{
		startTime = System.currentTimeMillis();
	}
	public SystemElapsedTimer setEndTime()
	{
		endTime = System.currentTimeMillis();
		return this;
	}
	public long getElapsedTime()
	{
		return (endTime - startTime);
	}
	public String toString()
	{
		long deltaMillis = getElapsedTime();
		// deltaMillis = n + s*1000 + m*60*1000 + h*60*60*1000;
		long millis       = (deltaMillis) %1000;
		long deltaSeconds = (deltaMillis) /1000;
		long deltaMinutes = deltaSeconds  / 60;
		long hours  = (int)(deltaMillis   / (60 * 60 * 1000));
		long minutes= (int)(deltaMinutes - (hours*60) );
		long seconds= (int)(deltaSeconds - (hours*60*60) - (minutes*60));
		String result = "Elapsed time: " + hours   + " hours, " +
										   minutes + " minutes, " +
										   seconds + " seconds, " +
										   millis  + " milliseconds";
		return result;
	}
	public void writeElapsedTime(PrintWriter writer, String header)
	{
		writer.println(header);
		writer.println(toString());
		writer.flush();
	}
	// the following methods are for testing purposes only
	public static void main(String args[])
	{
		SystemElapsedTimer me = new SystemElapsedTimer();
		me.setET(5, 4, 3, 100);
		System.out.println(me);
		me.setET(25, 14, 53, 999);
		System.out.println(me);
		me.setET(25, 0, 53, 0);
		System.out.println(me);
		me.setET(0, 0, 13, 0);
		System.out.println(me);
	}
	public void setET(long h, long m, long s, long n)
	{
		long et = n + s*1000 + m*60*1000 + h*60*60*1000;
		setET(et);
	}
	public void setET(long givenET)
	{
		startTime = 0L;
		endTime = givenET;
	}
}