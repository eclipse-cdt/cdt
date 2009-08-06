/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Fix 161844 - regex matching backslashes
 * Martin Oberhuber (Wind River) - Fix 162781 - normalize without replaceAll()
 * Martin Oberhuber (Wind River) - Use pre-compiled regex Pattern
 * Martin Oberhuber (Wind River) - Fix 154874 - handle files with space or $ in the name
 * Martin Oberhuber (Wind River) - Fix 183991 - handle windows C:/ paths for FTP
 * Martin Oberhuber (Wind River) - [246710] Fix quoting backslashes in UNIX shells
 * Martin Oberhuber (Wind River) - [164110] Fix quoting single-quotes in UNIX shells
 * Martin Oberhuber (Wind River) - [285945] Fix quoting ! and " characters
 ********************************************************************************/

package org.eclipse.rse.services.clientserver;

import java.util.regex.Pattern;

import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;

public class PathUtility
{
	//Regex pattern: / or \\
	private static Pattern badSlashPatternWin=Pattern.compile("/|\\\\\\\\"); //$NON-NLS-1$

	/**
	 * Normalize a path name that is supposed to be Windows style.
	 * Replaces / characters by \ and remove duplicate \ characters.
	 *
	 * @param path a path to normalize
	 * @return a normalized path.
	 */
	public static String normalizeWindows(String path)
	{
		if (path == null || path.length() < 2) {
			return path;
		}
		//FIXME Windows UNC Paths should probably be considered.
		boolean endsWithSlash = (path.endsWith("\\") || path.endsWith("/")); //$NON-NLS-1$ //$NON-NLS-2$
		if (badSlashPatternWin.matcher(path).find()) {
			//Replace /->\, then replace \\->\
			StringBuffer buf = new StringBuffer(path.length());
			boolean foundBackslash=false;
			for (int i=0; i<path.length(); i++) {
				char c = path.charAt(i);
				if (c=='/') {
					c='\\';
				}
				if (c=='\\') {
					if (!foundBackslash) {
						foundBackslash=true;
						buf.append(c);
					}
				} else {
					foundBackslash=false;
					buf.append(c);
				}
			}
			if (endsWithSlash && buf.length()!=3) {
				buf.deleteCharAt(buf.length()-1);
			}
			path = buf.toString();
		} else if (endsWithSlash && path.length()!=3) {
			//remove trailing slash only
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	//Regex pattern: \ or //
	private static Pattern badSlashPatternUnix=Pattern.compile("\\\\|//"); //$NON-NLS-1$

	/**
	 * Normalize a path name that is supposed to be UNIX style.
	 * Replaces \ characters by / and remove duplicate / characters.
	 *
	 * @deprecated this should not be used since \ is a valid part of
	 *     UNIX file names. Also, a better normalizer would also consider
	 *     swquences like a/../b and a/./b -- Try to work without this
	 *     method.
	 * @param path a path to normalize
	 * @return a normalized path.
	 */
	public static String normalizeUnix(String path)
	{
		if (path == null || path.length() < 2) {
			return path;
		}
		boolean endsWithSlash = (path.endsWith("\\") || path.endsWith("/")); //$NON-NLS-1$ //$NON-NLS-2$
		if (badSlashPatternUnix.matcher(path).find()) {
			//Replace \->/, then replace //->/
			StringBuffer buf = new StringBuffer(path.length());
			boolean foundSlash=false;
			for (int i=0; i<path.length(); i++) {
				char c = path.charAt(i);
				if (c=='\\') {
					c='/';
				}
				if (c=='/') {
					if (!foundSlash) {
						foundSlash=true;
						buf.append(c);
					}
				} else {
					foundSlash=false;
					buf.append(c);
				}
			}
			if (endsWithSlash && buf.length()!=1) {
				buf.deleteCharAt(buf.length()-1);
			}
			path = buf.toString();
		} else if (endsWithSlash && path.length()!=1) {
			//remove trailing slash only
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	public static String normalizeVirtualWindows(String path)
	{
		if (path == null || path.length() < 2) return path;
		AbsoluteVirtualPath avp = new AbsoluteVirtualPath(path);
		String realPart = avp.getContainingArchiveString();
		if (ArchiveHandlerManager.isVirtual(realPart))
			realPart = normalizeVirtualWindows(realPart);
		else realPart = normalizeWindows(realPart);
		return realPart + ArchiveHandlerManager.VIRTUAL_SEPARATOR + avp.getVirtualPart();
	}

	public static String normalizeVirtualUnix(String path)
	{
		if (path == null || path.length() < 2) return path;
		AbsoluteVirtualPath avp = new AbsoluteVirtualPath(path);
		String realPart = avp.getContainingArchiveString();
		if (ArchiveHandlerManager.isVirtual(realPart))
			realPart = normalizeVirtualUnix(realPart);
		else realPart = normalizeUnix(realPart);
		return realPart + ArchiveHandlerManager.VIRTUAL_SEPARATOR + avp.getVirtualPart();
	}

	public static String normalizeUnknown(String path)
	{
		if (path == null || path.length() < 2) return path;
		if (path.charAt(1) == ':')
			if (path.indexOf(ArchiveHandlerManager.VIRTUAL_CANONICAL_SEPARATOR) == -1)
				return normalizeWindows(path);
			else return normalizeVirtualWindows(path);
		else if (path.charAt(0) == '/')
			if (path.indexOf(ArchiveHandlerManager.VIRTUAL_CANONICAL_SEPARATOR) == -1)
				return normalizeUnix(path);
			else return normalizeVirtualUnix(path);
		else return path;
	}

	/**
	 * Given a path name, try to guess what separator is used.
	 * Should only be used for absolute path names, but tries to compute
	 * a good fallback in case of relative path names as well.
	 * @param path a path to check
	 * @return a separator String for the given path: either "/" or "\\"
	 */
	public static String getSeparator(String path)
	{
		if (path!=null && path.length()>0) {
			//The most common case, first
			switch(path.charAt(0)) {
			case '/': return "/"; //UNIX absolute //$NON-NLS-1$
			case '\\': return "\\"; //UNC //$NON-NLS-1$
			}
			if (path.length()>2 && path.charAt(1)==':') {
				switch(path.charAt(2)) {
				case '\\': return "\\"; //Windows absolute //$NON-NLS-1$
				case '/': return "/"; //Windows absolute with / //$NON-NLS-1$
				}
			}
			//We have some relative path. Should never come here,
			//but try to guess anyways. Note that ':' and '\\' are
			//valid parts of UNIX filenames, so check for / first.
			//TODO check if it is a good idea to put an assert in here
			//or even throw an (unchecked) exception.
			if (path.indexOf('/')>0) {
				//Slash is a path illegal character on Windows -> must be UNIX
				return "/"; //$NON-NLS-1$
			} else if (path.indexOf('\\')>0) {
				//Not a single / but got \\ -> Likely Windows but not sure
				return "\\"; //$NON-NLS-1$
			} else if (path.length()==2 && path.charAt(1)==':') {
				//Windows drive letter only
				return "\\"; //$NON-NLS-1$
			}
		}
		// Path contains no /, no \\ and is not a drive only --> Fallback
		return "/"; //$NON-NLS-1$
	}

	/**
	 * Quotes a string such that it can be used in a remote UNIX shell.
	 *
	 * This has been tested with sh, bash and tcsh shells.
	 * On Windows, special characters likes quotes and dollar sign. and
	 * - most importantly - the backslash will not be quoted correctly.
	 *
	 * Newline is only quoted correctly in tcsh. But since this is mainly
	 * intended for file names, it should work OK in almost every case.
	 *
	 * @param s String to be quoted
	 * @return quoted string, or original if no quoting was necessary.
	 */
	public static String enQuoteUnix(String s) {
		if(fValidShellPattern.matcher(s).matches()) {
			return s;
		} else {
			StringBuffer buf = new StringBuffer(s.length()+16);
			buf.append('"');
			for(int i=0; i<s.length(); i++) {
				char c=s.charAt(i);
				switch(c) {
				case '$':
				case '\\':
				case '\'':
				case '`':
				case '"':
					//Need to treat specially to work in both bash and tcsh:
					//close the quote, insert quoted $, reopen the quote
					buf.append('"');
					buf.append('\\');
					buf.append(c);
					buf.append('"');
					break;
				case '\n':
				case '!':
					//just quote it. The newline will work in tcsh only -
					//bash replaces it by the empty string. But newlines
					//in filenames are an academic issue, hopefully.
					buf.append('\\');
					buf.append(c);
					break;
				default:
					buf.append(c);
				}
			}
			buf.append('"');
			return buf.toString();
		}
	}
	private static Pattern fValidShellPattern = Pattern.compile("[a-zA-Z0-9._/]*"); //$NON-NLS-1$

}