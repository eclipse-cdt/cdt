/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.dstore.security.util;
/**
 * This class provides static methods for some of the
 * very used IString operations
 */

public class StringModifier
{
   // change all occurrences of oldPat to newPat
   public  static String change(String in, String oldPat, String newPat)
   {
	  if (oldPat.length() == 0)
		 return in;
	  if (oldPat.length() == 1 && newPat.length() == 1)
		 return in.replace(oldPat.charAt(0), newPat.charAt(0));

	  int lastIndex = 0;
	  int newIndex = 0;
	  StringBuffer newString = new StringBuffer();
	  for(;;)
	  {
		 newIndex = in.indexOf(oldPat, lastIndex);
		 if (newIndex != -1)
		 {
			newString.append(in.substring(lastIndex, newIndex) + newPat);
			lastIndex = newIndex + oldPat.length();
		 }
		 else
		 {
			newString.append(in.substring(lastIndex));
			break;
		 }
	  }
	  return newString.toString();
   }   
  }