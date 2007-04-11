package org.eclipse.rse.internal.useractions.ui.uda.util;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.StringTokenizer;
import java.util.Vector;

public class MatchStr {
	private String matchStr;
	private Vector mStrTokens;
	private boolean splatAtEnd;

	public MatchStr(String p_matchStr) {
		matchStr = p_matchStr;
		StringTokenizer st = new StringTokenizer(matchStr, "*"); //$NON-NLS-1$
		int n = st.countTokens();
		mStrTokens = new Vector();
		for (int i = 0; i < n; i++) {
			mStrTokens.addElement(st.nextToken());
		}
		splatAtEnd = matchStr.charAt(matchStr.length() - 1) == '*';
	}

	public boolean isMatch(String str) {
		if (str.indexOf("*") == 0) //$NON-NLS-1$
		{
			str = str.substring(1);
		}
		if (mStrTokens.size() == 0) {
			return true;
		}
		if (!str.startsWith((String) mStrTokens.elementAt(0))) {
			return false;
		}
		if (!splatAtEnd) {
			if (mStrTokens.size() == 1) {
				return str.equals(mStrTokens.elementAt(0));
			} else if (!str.endsWith((String) mStrTokens.elementAt(mStrTokens.size() - 1))) {
				return false;
			}
		}
		int i = ((String) mStrTokens.elementAt(0)).length();
		int j = 1;
		while (i < str.length() && j < mStrTokens.size()) {
			String tempStr = str.substring(i);
			if (tempStr.startsWith((String) mStrTokens.elementAt(j))) {
				i = i + tempStr.length();
				j++;
			}
			i++;
		}
		return mStrTokens.size() == j;
	}
	/*   public static void main(String[] args)
	 {
	 MatchStr matchStr = new MatchStr("AB*");

	 if (matchStr.isMatch("ABaasBdddd"))
	 {
	 System.out.println("true");
	 }
	 else
	 {
	 System.out.println("false");      
	 }
	 
	 matchStr = new MatchStr("*");
	 boolean one = matchStr.isMatch("lskda");
	 boolean two = matchStr.isMatch("");	
	 boolean three = matchStr.isMatch("1");		
	 
	 
	 matchStr = new MatchStr("abc");
	 boolean four = matchStr.isMatch("abcdabc");			
	 boolean five = matchStr.isMatch("abc");	
	 
	 
	 matchStr = new MatchStr("abc*");
	 boolean six = matchStr.isMatch("abcdabc");			
	 boolean seven = matchStr.isMatch("abc");	
	 

	 try {
	 System.in.read();
	 } catch  (Exception e)
	 {
	 }
	 }*/
}
