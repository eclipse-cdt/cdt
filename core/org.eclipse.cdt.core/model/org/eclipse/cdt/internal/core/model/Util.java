package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001. All Rights Reserved.
 */
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants.DebugLogConstant;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Util implements ICLogConstants {
	public static boolean VERBOSE_PARSER = false;
	public static boolean VERBOSE_SCANNER = false;
	public static boolean VERBOSE_MODEL = false;

	private Util() {
	}

	public static StringBuffer getContent(IFile file) throws IOException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(file.getContents(true));
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
		try {
			char[] b = getInputStreamAsCharArray(stream, -1, null);
			return new StringBuffer(b.length).append(b);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Returns the given input stream's contents as a character array. If a
	 * length is specified (ie. if length != -1), only length chars are
	 * returned. Otherwise all chars in the stream are returned. Note this
	 * doesn't close the stream.
	 * 
	 * @throws IOException
	 *             if a problem occured reading the stream.
	 */
	public static char[] getInputStreamAsCharArray(InputStream stream,
			int length, String encoding) throws IOException {
		InputStreamReader reader = null;
		reader = encoding == null
				? new InputStreamReader(stream)
				: new InputStreamReader(stream, encoding);
		char[] contents;
		if (length == -1) {
			contents = new char[0];
			int contentsLength = 0;
			int charsRead = -1;
			do {
				int available = stream.available();
				// resize contents if needed
				if (contentsLength + available > contents.length) {
					System.arraycopy(contents, 0,
							contents = new char[contentsLength + available], 0,
							contentsLength);
				}
				// read as many chars as possible
				charsRead = reader.read(contents, contentsLength, available);
				if (charsRead > 0) {
					// remember length of contents
					contentsLength += charsRead;
				}
			} while (charsRead > 0);
			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(contents, 0,
						contents = new char[contentsLength], 0, contentsLength);
			}
		} else {
			contents = new char[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the
				// actual read size.
				len += readSize;
				readSize = reader.read(contents, len, length - len);
			}
			// See PR 1FMS89U
			// Now we need to resize in case the default encoding used more
			// than one byte for each
			// character
			if (len != length)
				System.arraycopy(contents, 0, (contents = new char[len]), 0,
						len);
		}
		return contents;
	}

	public static void save(StringBuffer buffer, IFile file)
			throws CoreException {
		byte[] bytes = buffer.toString().getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		// use a platform operation to update the resource contents
		boolean force = true;
		file.setContents(stream, force, true, null); // record history
	}

	/**
	 * Returns the given file's contents as a character array.
	 */
	public static char[] getResourceContentsAsCharArray(IFile file)
			throws CModelException {
		return getResourceContentsAsCharArray(file, null);
	}

	public static char[] getResourceContentsAsCharArray(IFile file,
			String encoding) throws CModelException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(file.getContents(true));
		} catch (CoreException e) {
			throw new CModelException(e,
					ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST);
		}
		try {
			return Util.getInputStreamAsCharArray(stream, -1, encoding);
		} catch (IOException e) {
			throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}

	/*
	 * Add a log entry
	 */
	public static void log(Throwable e, String message, LogConst logType) {
		IStatus status = new Status(IStatus.ERROR, CCorePlugin.getDefault()
				.getDescriptor().getUniqueIdentifier(), IStatus.ERROR, message,
				e);
		Util.log(status, logType);
	}

	public static void log(IStatus status, LogConst logType) {
		if (logType.equals(ICLogConstants.PDE)) {
			CCorePlugin.getDefault().getLog().log(status);
		} else if (logType.equals(ICLogConstants.CDT)) {
			CCorePlugin.getDefault().cdtLog.log(status);
		}
	}

	public static void log(String message, LogConst logType) {
		IStatus status = new Status(IStatus.INFO, CCorePlugin.getDefault()
				.getDescriptor().getUniqueIdentifier(), IStatus.INFO, message,
				null);
		Util.log(status, logType);
	}

	public static void debugLog(String message, DebugLogConstant client) {
		Util.debugLog(message, client, true);
	}

	public static void debugLog(String message, DebugLogConstant client,
			boolean addTimeStamp) {
		if (CCorePlugin.getDefault() == null)
			return;
		if (CCorePlugin.getDefault().isDebugging() && isActive(client)) {
			// Time stamp
			if (addTimeStamp)
				message = MessageFormat.format("[{0}] {1}", new Object[]{ //$NON-NLS-1$
						new Long(System.currentTimeMillis()), message}); //$NON-NLS-1$
			while (message.length() > 100) {
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.out.println(partial + "\\"); //$NON-NLS-1$
			}
			if (message.endsWith("\n")) { //$NON-NLS-1$
				System.err.print(message);
			} else {
				System.out.println(message);
			}
		}
	}

	/**
	 * @param client
	 * @return
	 */
	public static boolean isActive(DebugLogConstant client) {
		if (client.equals(IDebugLogConstants.PARSER)) {
			return VERBOSE_PARSER;
		} else if (client.equals(IDebugLogConstants.SCANNER))
			return VERBOSE_SCANNER;
		else if (client.equals(IDebugLogConstants.MODEL)) {
			return VERBOSE_MODEL;
		}
		return false;
	}

	public static void setDebugging(boolean value) {
		CCorePlugin.getDefault().setDebugging(value);
	}

	/**
	 * Combines two hash codes to make a new one.
	 */
	public static int combineHashCodes(int hashCode1, int hashCode2) {
		return hashCode1 * 17 + hashCode2;
	}

	/**
	 * Compares two arrays using equals() on the elements. Either or both
	 * arrays may be null. Returns true if both are null. Returns false if only
	 * one is null. If both are arrays, returns true iff they have the same
	 * length and all elements compare true with equals.
	 */
	public static boolean equalArraysOrNull(Object[] a, Object[] b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		int len = a.length;
		if (len != b.length)
			return false;
		for (int i = 0; i < len; ++i) {
			if (a[i] == null) {
				if (b[i] != null)
					return false;
			} else {
				if (!a[i].equals(b[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Compares two arrays using equals() on the elements. Either or both
	 * arrays may be null. Returns true if both are null. Returns false if only
	 * one is null. If both are arrays, returns true iff they have the same
	 * length and all elements are equal.
	 */
	public static boolean equalArraysOrNull(int[] a, int[] b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		int len = a.length;
		if (len != b.length)
			return false;
		for (int i = 0; i < len; ++i) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}

	/**
	 * Compares two objects using equals(). Either or both array may be null.
	 * Returns true if both are null. Returns false if only one is null.
	 * Otherwise, return the result of comparing with equals().
	 */
	public static boolean equalOrNull(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

	/*
	 * Returns whether the given resource path matches one of the exclusion
	 * patterns.
	 * 
	 * @see IClasspathEntry#getExclusionPatterns
	 */
	public final static boolean isExcluded(IPath resourcePath,
			char[][] exclusionPatterns) {
		if (exclusionPatterns == null)
			return false;
		char[] path = resourcePath.toString().toCharArray();
		for (int i = 0, length = exclusionPatterns.length; i < length; i++)
			if (pathMatch(exclusionPatterns[i], path, true, '/'))
				return true;
		return false;
	}

	/*
	 * Returns whether the given resource matches one of the exclusion
	 * patterns.
	 * 
	 * @see IClasspathEntry#getExclusionPatterns
	 */
	public final static boolean isExcluded(IResource resource,
			char[][] exclusionPatterns) {
		IPath path = resource.getFullPath();
		// ensure that folders are only excluded if all of their children are
		// excluded
		if (resource.getType() == IResource.FOLDER)
			path = path.append("*"); //$NON-NLS-1$
		return isExcluded(path, exclusionPatterns);
	}

	/**
	 * Answers true if the pattern matches the given name, false otherwise.
	 * This char[] pattern matching accepts wild-cards '*' and '?'.
	 * 
	 * When not case sensitive, the pattern is assumed to already be
	 * lowercased, the name will be lowercased character per character as
	 * comparing. If name is null, the answer is false. If pattern is null, the
	 * answer is true if name is not null. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 *  pattern = { '?', 'b', '*' }
	 *  name = { 'a', 'b', 'c' , 'd' }
	 *  isCaseSensitive = true
	 *  result =&gt; true
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *  pattern = { '?', 'b', '?' }
	 *  name = { 'a', 'b', 'c' , 'd' }
	 *  isCaseSensitive = true
	 *  result =&gt; false
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *  pattern = { 'b', '*' }
	 *  name = { 'a', 'b', 'c' , 'd' }
	 *  isCaseSensitive = true
	 *  result =&gt; false
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param pattern
	 *            the given pattern
	 * @param name
	 *            the given name
	 * @param isCaseSensitive
	 *            flag to know whether or not the matching should be case
	 *            sensitive
	 * @return true if the pattern matches the given name, false otherwise
	 */
	public static final boolean match(char[] pattern, char[] name,
			boolean isCaseSensitive) {
		if (name == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		return match(pattern, 0, pattern.length, name, 0, name.length,
				isCaseSensitive);
	}

	/**
	 * Answers true if the a sub-pattern matches the subpart of the given name,
	 * false otherwise. char[] pattern matching, accepting wild-cards '*' and
	 * '?'. Can match only subset of name/pattern. end positions are
	 * non-inclusive. The subpattern is defined by the patternStart and
	 * pattternEnd positions. When not case sensitive, the pattern is assumed
	 * to already be lowercased, the name will be lowercased character per
	 * character as comparing. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 *  pattern = { '?', 'b', '*' }
	 *  patternStart = 1
	 *  patternEnd = 3
	 *  name = { 'a', 'b', 'c' , 'd' }
	 *  nameStart = 1
	 *  nameEnd = 4
	 *  isCaseSensitive = true
	 *  result =&gt; true
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *  pattern = { '?', 'b', '*' }
	 *  patternStart = 1
	 *  patternEnd = 2
	 *  name = { 'a', 'b', 'c' , 'd' }
	 *  nameStart = 1
	 *  nameEnd = 2
	 *  isCaseSensitive = true
	 *  result =&gt; false
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param pattern
	 *            the given pattern
	 * @param patternStart
	 *            the given pattern start
	 * @param patternEnd
	 *            the given pattern end
	 * @param name
	 *            the given name
	 * @param nameStart
	 *            the given name start
	 * @param nameEnd
	 *            the given name end
	 * @param isCaseSensitive
	 *            flag to know if the matching should be case sensitive
	 * @return true if the a sub-pattern matches the subpart of the given name,
	 *         false otherwise
	 */
	public static final boolean match(char[] pattern, int patternStart,
			int patternEnd, char[] name, int nameStart, int nameEnd,
			boolean isCaseSensitive) {
		if (name == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		int iPattern = patternStart;
		int iName = nameStart;
		if (patternEnd < 0)
			patternEnd = pattern.length;
		if (nameEnd < 0)
			nameEnd = name.length;
		/* check first segment */
		char patternChar = 0;
		while ((iPattern < patternEnd)
				&& (patternChar = pattern[iPattern]) != '*') {
			if (iName == nameEnd)
				return false;
			if (patternChar != (isCaseSensitive ? name[iName] : Character
					.toLowerCase(name[iName]))
					&& patternChar != '?') {
				return false;
			}
			iName++;
			iPattern++;
		}
		/* check sequence of star+segment */
		int segmentStart;
		if (patternChar == '*') {
			segmentStart = ++iPattern; // skip star
		} else {
			segmentStart = 0; // force iName check
		}
		int prefixStart = iName;
		checkSegment : while (iName < nameEnd) {
			if (iPattern == patternEnd) {
				iPattern = segmentStart; // mismatch - restart current segment
				iName = ++prefixStart;
				continue checkSegment;
			}
			/* segment is ending */
			if ((patternChar = pattern[iPattern]) == '*') {
				segmentStart = ++iPattern; // skip start
				if (segmentStart == patternEnd) {
					return true;
				}
				prefixStart = iName;
				continue checkSegment;
			}
			/* check current name character */
			if ((isCaseSensitive ? name[iName] : Character
					.toLowerCase(name[iName])) != patternChar
					&& patternChar != '?') {
				iPattern = segmentStart; // mismatch - restart current segment
				iName = ++prefixStart;
				continue checkSegment;
			}
			iName++;
			iPattern++;
		}
		return (segmentStart == patternEnd)
				|| (iName == nameEnd && iPattern == patternEnd)
				|| (iPattern == patternEnd - 1 && pattern[iPattern] == '*');
	}

	/**
	 * Answers true if the pattern matches the filepath using the
	 * pathSepatator, false otherwise.
	 * 
	 * Path char[] pattern matching, accepting wild-cards '**', '*' and '?'
	 * (using Ant directory tasks conventions, also see
	 * "http://jakarta.apache.org/ant/manual/dirtasks.html#defaultexcludes").
	 * Path pattern matching is enhancing regular pattern matching in
	 * supporting extra rule where '**' represent any folder combination.
	 * Special rule: - foo\ is equivalent to foo\** When not case sensitive,
	 * the pattern is assumed to already be lowercased, the name will be
	 * lowercased character per character as comparing.
	 * 
	 * @param pattern
	 *            the given pattern
	 * @param filepath
	 *            the given path
	 * @param isCaseSensitive
	 *            to find out whether or not the matching should be case
	 *            sensitive
	 * @param pathSeparator
	 *            the given path separator
	 * @return true if the pattern matches the filepath using the
	 *         pathSepatator, false otherwise
	 */
	public static final boolean pathMatch(char[] pattern, char[] filepath,
			boolean isCaseSensitive, char pathSeparator) {
		if (filepath == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		// offsets inside pattern
		int pSegmentStart = pattern[0] == pathSeparator ? 1 : 0;
		int pLength = pattern.length;
		int pSegmentEnd = indexOf(pathSeparator, pattern, pSegmentStart + 1);
		if (pSegmentEnd < 0)
			pSegmentEnd = pLength;
		// special case: pattern foo\ is equivalent to foo\**
		boolean freeTrailingDoubleStar = pattern[pLength - 1] == pathSeparator;
		// offsets inside filepath
		int fSegmentStart, fLength = filepath.length;
		if (filepath[0] != pathSeparator) {
			fSegmentStart = 0;
		} else {
			fSegmentStart = 1;
		}
		if (fSegmentStart != pSegmentStart) {
			return false; // both must start with a separator or none.
		}
		int fSegmentEnd = indexOf(pathSeparator, filepath, fSegmentStart + 1);
		if (fSegmentEnd < 0)
			fSegmentEnd = fLength;
		// first segments
		while (pSegmentStart < pLength
				&& !(pSegmentEnd == pLength && freeTrailingDoubleStar || (pSegmentEnd == pSegmentStart + 2
						&& pattern[pSegmentStart] == '*' && pattern[pSegmentStart + 1] == '*'))) {
			if (fSegmentStart >= fLength)
				return false;
			if (!match(pattern, pSegmentStart, pSegmentEnd, filepath,
					fSegmentStart, fSegmentEnd, isCaseSensitive)) {
				return false;
			}
			// jump to next segment
			pSegmentEnd = indexOf(pathSeparator, pattern,
					pSegmentStart = pSegmentEnd + 1);
			// skip separator
			if (pSegmentEnd < 0)
				pSegmentEnd = pLength;
			fSegmentEnd = indexOf(pathSeparator, filepath,
					fSegmentStart = fSegmentEnd + 1);
			// skip separator
			if (fSegmentEnd < 0)
				fSegmentEnd = fLength;
		}
		/* check sequence of doubleStar+segment */
		int pSegmentRestart;
		if ((pSegmentStart >= pLength && freeTrailingDoubleStar)
				|| (pSegmentEnd == pSegmentStart + 2
						&& pattern[pSegmentStart] == '*' && pattern[pSegmentStart + 1] == '*')) {
			pSegmentEnd = indexOf(pathSeparator, pattern,
					pSegmentStart = pSegmentEnd + 1);
			// skip separator
			if (pSegmentEnd < 0)
				pSegmentEnd = pLength;
			pSegmentRestart = pSegmentStart;
		} else {
			if (pSegmentStart >= pLength)
				return fSegmentStart >= fLength; // true if filepath is done
			// too.
			pSegmentRestart = 0; // force fSegmentStart check
		}
		int fSegmentRestart = fSegmentStart;
		checkSegment : while (fSegmentStart < fLength) {
			if (pSegmentStart >= pLength) {
				if (freeTrailingDoubleStar)
					return true;
				// mismatch - restart current path segment
				pSegmentEnd = indexOf(pathSeparator, pattern,
						pSegmentStart = pSegmentRestart);
				if (pSegmentEnd < 0)
					pSegmentEnd = pLength;
				fSegmentRestart = indexOf(pathSeparator, filepath,
						fSegmentRestart + 1);
				// skip separator
				if (fSegmentRestart < 0) {
					fSegmentRestart = fLength;
				} else {
					fSegmentRestart++;
				}
				fSegmentEnd = indexOf(pathSeparator, filepath,
						fSegmentStart = fSegmentRestart);
				if (fSegmentEnd < 0)
					fSegmentEnd = fLength;
				continue checkSegment;
			}
			/* path segment is ending */
			if (pSegmentEnd == pSegmentStart + 2
					&& pattern[pSegmentStart] == '*'
					&& pattern[pSegmentStart + 1] == '*') {
				pSegmentEnd = indexOf(pathSeparator, pattern,
						pSegmentStart = pSegmentEnd + 1);
				// skip separator
				if (pSegmentEnd < 0)
					pSegmentEnd = pLength;
				pSegmentRestart = pSegmentStart;
				fSegmentRestart = fSegmentStart;
				if (pSegmentStart >= pLength)
					return true;
				continue checkSegment;
			}
			/* chech current path segment */
			if (!match(pattern, pSegmentStart, pSegmentEnd, filepath,
					fSegmentStart, fSegmentEnd, isCaseSensitive)) {
				// mismatch - restart current path segment
				pSegmentEnd = indexOf(pathSeparator, pattern,
						pSegmentStart = pSegmentRestart);
				if (pSegmentEnd < 0)
					pSegmentEnd = pLength;
				fSegmentRestart = indexOf(pathSeparator, filepath,
						fSegmentRestart + 1);
				// skip separator
				if (fSegmentRestart < 0) {
					fSegmentRestart = fLength;
				} else {
					fSegmentRestart++;
				}
				fSegmentEnd = indexOf(pathSeparator, filepath,
						fSegmentStart = fSegmentRestart);
				if (fSegmentEnd < 0)
					fSegmentEnd = fLength;
				continue checkSegment;
			}
			// jump to next segment
			pSegmentEnd = indexOf(pathSeparator, pattern,
					pSegmentStart = pSegmentEnd + 1);
			// skip separator
			if (pSegmentEnd < 0)
				pSegmentEnd = pLength;
			fSegmentEnd = indexOf(pathSeparator, filepath,
					fSegmentStart = fSegmentEnd + 1);
			// skip separator
			if (fSegmentEnd < 0)
				fSegmentEnd = fLength;
		}
		return (pSegmentRestart >= pSegmentEnd)
				|| (fSegmentStart >= fLength && pSegmentStart >= pLength)
				|| (pSegmentStart == pLength - 2
						&& pattern[pSegmentStart] == '*' && pattern[pSegmentStart + 1] == '*')
				|| (pSegmentStart == pLength && freeTrailingDoubleStar);
	}

	/**
	 * Answers the first index in the array for which the corresponding
	 * character is equal to toBeFound. Answers -1 if no occurrence of this
	 * character is found. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 *  toBeFound = 'c'
	 *  array = { ' a', 'b', 'c', 'd' }
	 *  result =&gt; 2
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *  toBeFound = 'e'
	 *  array = { ' a', 'b', 'c', 'd' }
	 *  result =&gt; -1
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param toBeFound
	 *            the character to search
	 * @param array
	 *            the array to be searched
	 * @return the first index in the array for which the corresponding
	 *         character is equal to toBeFound, -1 otherwise
	 * @throws NullPointerException
	 *             if array is null
	 */
	public static final int indexOf(char toBeFound, char[] array) {
		for (int i = 0; i < array.length; i++)
			if (toBeFound == array[i])
				return i;
		return -1;
	}

	/**
	 * Answers the first index in the array for which the corresponding
	 * character is equal to toBeFound starting the search at index start.
	 * Answers -1 if no occurrence of this character is found. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 *  toBeFound = 'c'
	 *  array = { ' a', 'b', 'c', 'd' }
	 *  start = 2
	 *  result =&gt; 2
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *  toBeFound = 'c'
	 *  array = { ' a', 'b', 'c', 'd' }
	 *  start = 3
	 *  result =&gt; -1
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *  toBeFound = 'e'
	 *  array = { ' a', 'b', 'c', 'd' }
	 *  start = 1
	 *  result =&gt; -1
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param toBeFound
	 *            the character to search
	 * @param array
	 *            the array to be searched
	 * @param start
	 *            the starting index
	 * @return the first index in the array for which the corresponding
	 *         character is equal to toBeFound, -1 otherwise
	 * @throws NullPointerException
	 *             if array is null
	 * @throws ArrayIndexOutOfBoundsException
	 *             if start is lower than 0
	 */
	public static final int indexOf(char toBeFound, char[] array, int start) {
		for (int i = start; i < array.length; i++)
			if (toBeFound == array[i])
				return i;
		return -1;
	}
}
