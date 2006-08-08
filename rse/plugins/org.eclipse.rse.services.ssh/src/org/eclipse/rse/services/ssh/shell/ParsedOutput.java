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
 * Martin Oberhuber (WindRiver) - adapted from services.local
 ********************************************************************************/

package org.eclipse.rse.services.ssh.shell;

//This is just a convenience object for storing information parsed out of a line of output.
public class ParsedOutput
{


 public String  type;
 public String  text;
 public String  file;
 public int     line;
 public int     col;

 public ParsedOutput (String theType, String theText, String theFile, int theLine, int theColumn)
 {
  type = theType;
  text = theText; 
  file = theFile;
  line = theLine; 
  col  = theColumn;
 }
}