/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
<pre>
`OUTPUT :'
     `( OUT-OF-BAND-RECORD )* [ RESULT-RECORD ] "(gdb)" NL'

`RESULT-RECORD :'
     ` [ TOKEN ] "^" RESULT-CLASS ( "," RESULT )* NL'

`OUT-OF-BAND-RECORD :'
     `ASYNC-RECORD | STREAM-RECORD'

`ASYNC-RECORD :'
     `EXEC-ASYNC-OUTPUT | STATUS-ASYNC-OUTPUT | NOTIFY-ASYNC-OUTPUT'

`EXEC-ASYNC-OUTPUT :'
     `[ TOKEN ] "*" ASYNC-OUTPUT'

`STATUS-ASYNC-OUTPUT :'
     `[ TOKEN ] "+" ASYNC-OUTPUT'

`NOTIFY-ASYNC-OUTPUT :'
     `[ TOKEN ] "=" ASYNC-OUTPUT'

`ASYNC-OUTPUT :'
     `ASYNC-CLASS ( "," RESULT )* NL'

`RESULT-CLASS :'
     `"done" | "running" | "connected" | "error" | "exit"'

`ASYNC-CLASS :'
     `"stopped" | OTHERS' (where OTHERS will be added depending on the
     needs--this is still in development).

`RESULT :'
     ` VARIABLE "=" VALUE'

`VARIABLE :'
     ` STRING '

`VALUE :'
     ` CONST | TUPLE | LIST '

`CONST :'
     `C-STRING'

`TUPLE :'
     ` "{}" | "{" RESULT ( "," RESULT )* "}" '

`LIST :'
     ` "[]" | "[" VALUE ( "," VALUE )* "]" | "[" RESULT ( "," RESULT )*
     "]" '

`STREAM-RECORD :'
     `CONSOLE-STREAM-OUTPUT | TARGET-STREAM-OUTPUT | LOG-STREAM-OUTPUT'

`CONSOLE-STREAM-OUTPUT :'
     `"~" C-STRING'

`TARGET-STREAM-OUTPUT :'
     `"@" C-STRING'

`LOG-STREAM-OUTPUT :'
     `"&" C-STRING'

`NL :'
     `CR | CR-LF'

`TOKEN :'
     _any sequence of digits_.

`C-STRING :'
     `""" SEVEN-BIT-ISO-C-STRING-CONTENT """'
</pre>
 */
public class MIParser {

	public String primaryPrompt = "(gdb)"; //$NON-NLS-1$
	public String secondaryPrompt = ">"; //$NON-NLS-1$

	/**
	 * Point of entry to create an AST for MI.
	 *
	 * @param buffer Output from MI Channel.
	 * @return MIOutput
	 * @see MIOutput
	 */
	public MIOutput parse(String buffer) {
		MIOutput mi = new MIOutput();
		MIResultRecord rr = null;
		List oobs = new ArrayList(1);
		int id = -1;

		StringTokenizer st = new StringTokenizer(buffer, "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			StringBuffer token = new StringBuffer(st.nextToken());

			// Fetch the Token/Id
			if (token.length() > 0 && Character.isDigit(token.charAt(0))) {
				int i = 1;
				while (i < token.length() && Character.isDigit(token.charAt(i))) {
					i++;
				}
				String numbers = token.substring(0, i);
				try {
					id = Integer.parseInt(numbers);
				} catch (NumberFormatException e) {
				}
				// Consume the token.
				token.delete(0, i);
			}

			// ResultRecord ||| Out-Of-Band Records
			if (token.length() > 0) {
				if (token.charAt(0) == '^') {
					token.deleteCharAt(0);
					rr = processMIResultRecord(token, id);
				} else if (token.toString().startsWith(primaryPrompt)) {
					//break; // Do nothing.
				} else {
					MIOOBRecord band = processMIOOBRecord(token, id);
					if (band != null) {
						oobs.add(band);
					}
				}
			}
		}
		MIOOBRecord[] bands = (MIOOBRecord[]) oobs.toArray(new MIOOBRecord[oobs.size()]);
		mi.setMIOOBRecords(bands);
		mi.setMIResultRecord(rr);
		return mi;
	}

	/**
	 * Assuming '^' was deleted from the Result Record.
	 */
	private MIResultRecord processMIResultRecord(StringBuffer buffer, int id) {
		MIResultRecord rr = new MIResultRecord();
		rr.setToken(id);
		if (buffer.toString().startsWith(MIResultRecord.DONE)) {
			rr.setResultClass(MIResultRecord.DONE);
			buffer.delete(0, MIResultRecord.DONE.length());
		} else if (buffer.toString().startsWith(MIResultRecord.ERROR)) {
			rr.setResultClass(MIResultRecord.ERROR);
			buffer.delete(0, MIResultRecord.ERROR.length());
		} else if (buffer.toString().startsWith(MIResultRecord.EXIT)) {
			rr.setResultClass(MIResultRecord.EXIT);
			buffer.delete(0, MIResultRecord.EXIT.length());
		} else if (buffer.toString().startsWith(MIResultRecord.RUNNING)) {
			rr.setResultClass(MIResultRecord.RUNNING);
			buffer.delete(0, MIResultRecord.RUNNING.length());
		} else if (buffer.toString().startsWith(MIResultRecord.CONNECTED)) {
			rr.setResultClass(MIResultRecord.CONNECTED);
			buffer.delete(0, MIResultRecord.CONNECTED.length());
		} else {
			// FIXME:
			// Error throw an exception?
		}

		// Results are separated by commas.
		if (buffer.length() > 0 && buffer.charAt(0) == ',') {
			buffer.deleteCharAt(0);
			MIResult[] res = processMIResults(new FSB(buffer));
			rr.setMIResults(res);
		}
		return rr;
	}

	/**
	 * Find OutOfBand Records depending on the starting token.
	 */
	private MIOOBRecord processMIOOBRecord(StringBuffer buffer, int id) {
		MIOOBRecord oob = null;
		char c = buffer.charAt(0);
		if (c == '*' || c == '+' || c == '=') {
			// Consume the first char
			buffer.deleteCharAt(0);
			MIAsyncRecord async = null;
			switch (c) {
				case '*' :
					async = new MIExecAsyncOutput();
					break;

				case '+' :
					async = new MIStatusAsyncOutput();
					break;

				case '=' :
					async = new MINotifyAsyncOutput();
					break;
			}
			async.setToken(id);
			// Extract the Async-Class
			int i = buffer.toString().indexOf(',');
			if (i != -1) {
				String asyncClass = buffer.substring(0, i);
				async.setAsyncClass(asyncClass);
				// Consume the async-class and the comma
				buffer.delete(0, i + 1);
			} else {
				async.setAsyncClass(buffer.toString().trim());
				buffer.setLength(0);
			}
			MIResult[] res = processMIResults(new FSB(buffer));
			async.setMIResults(res);
			oob = async;
		} else if (c == '~' || c == '@' || c == '&') {
			// Consume the first char
			buffer.deleteCharAt(0);
			MIStreamRecord stream = null;
			switch (c) {
				case '~' :
					stream = new MIConsoleStreamOutput();
					break;

				case '@' :
					stream = new MITargetStreamOutput();
					break;

				case '&' :
					stream = new MILogStreamOutput();
					break;
			}
			// translateCString() assumes that the leading " is deleted
			if (buffer.length() > 0 && buffer.charAt(0) == '"') {
				buffer.deleteCharAt(0);
			}
			stream.setCString(translateCString(new FSB(buffer)));
			oob = stream;
		} else {
			// Badly format MI line, just pass it to the user as target stream
			MIStreamRecord stream = new MITargetStreamOutput();
			stream.setCString(buffer.toString() + "\n"); //$NON-NLS-1$
			oob = stream;
		}
		return oob;
	}

	/**
	 * Assuming that the usual leading comma was consumed.
	 * Extract the MI Result comma seperated responses.
	 */
	private MIResult[] processMIResults(FSB buffer) {
		List aList = new ArrayList();
		MIResult result = processMIResult(buffer);
		if (result != null) {
			aList.add(result);
		}
		while (buffer.length() > 0 && buffer.charAt(0) == ',') {
			buffer.deleteCharAt(0);
			result = processMIResult(buffer);
			if (result != null) {
				aList.add(result);
			}
		}
		return (MIResult[]) aList.toArray(new MIResult[aList.size()]);
	}

	/**
	 * Construct the MIResult.  Characters will be consume/delete
	 * moving forward constructing the AST.
	 */
	private MIResult processMIResult(FSB buffer) {
		MIResult result = new MIResult();
		int equal;
		if (buffer.length() > 0 && Character.isLetter(buffer.charAt(0)) && (equal = buffer.indexOf('=')) != -1) {
			String variable = buffer.substring(0, equal);
			result.setVariable(variable);
			buffer.delete(0, equal + 1);
			MIValue value = processMIValue(buffer);
			result.setMIValue(value);
		} else {
			result.setVariable(buffer.toString());
			result.setMIValue(new MIConst()); // Empty string:???
			buffer.setLength(0);
		}
		return result;
	}

	/**
	 * Find a MIValue implementation or return null.
	 */
	private MIValue processMIValue(FSB buffer) {
		MIValue value = null;
		if (buffer.length() > 0) {
			if (buffer.charAt(0) == '{') {
				buffer.deleteCharAt(0);
				value = processMITuple(buffer);
			} else if (buffer.charAt(0) == '[') {
				buffer.deleteCharAt(0);
				value = processMIList(buffer);
			} else if (buffer.charAt(0) == '"') {
				buffer.deleteCharAt(0);
				MIConst cnst = new MIConst();
				cnst.setCString(translateCString(buffer));
				value = cnst;
			}
		}
		return value;
	}

	/**
	 * Assuming the starting '{' was deleted form the StringBuffer,
	 * go to the closing '}' consuming/deleting all the characters.
	 * This is usually call by processMIvalue();
	 */
	private MIValue processMITuple(FSB buffer) {
		MITuple tuple = new MITuple();
		MIResult[] results = null;
		// Catch closing '}'
		while (buffer.length() > 0 && buffer.charAt(0) != '}') {
			results = processMIResults(buffer);
		}
		if (buffer.length() > 0 && buffer.charAt(0) == '}') {
			buffer.deleteCharAt(0);
		}
		if (results == null) {
			results = new MIResult[0];
		}
		tuple.setMIResults(results);
		return tuple;
	}

	/**
	 * Assuming the leading '[' was deleted, find the closing
	 * ']' consuming/delete chars from the StringBuffer.
	 */
	private MIValue processMIList(FSB buffer) {
		MIList list = new MIList();
		List valueList = new ArrayList();
		List resultList = new ArrayList();
		// catch closing ']'
		while (buffer.length() > 0 && buffer.charAt(0) != ']') {
			// Try for the MIValue first
			MIValue value = processMIValue(buffer);
			if (value != null) {
				valueList.add(value);
			} else {
				MIResult result = processMIResult(buffer);
				if (result != null) {
					resultList.add(result);
				}
			}
			if (buffer.length() > 0 && buffer.charAt(0) == ',') {
				buffer.deleteCharAt(0);
			}
		}
		if (buffer.length() > 0 && buffer.charAt(0) == ']') {
			buffer.deleteCharAt(0);
		}
		MIValue[] values = (MIValue[]) valueList.toArray(new MIValue[valueList.size()]);
		MIResult[] res = (MIResult[]) resultList.toArray(new MIResult[resultList.size()]);
		list.setMIValues(values);
		list.setMIResults(res);
		return list;
	}

	/*
	 * MI C-String rather MICOnst values are enclose in double quotes
	 * and any double quotes or backslash in the string are escaped.
	 * Assuming the starting double quote was removed.
	 * This method will stop at the closing double quote remove the extra
	 * backslach escaping and return the string __without__ the enclosing double quotes
	 * The orignal StringBuffer will move forward.
	 */
	private String translateCString(FSB buffer) {
		boolean escape = false;
		boolean closingQuotes = false;

		StringBuffer sb = new StringBuffer();

		int index = 0;
		for (; index < buffer.length() && !closingQuotes; index++) {
			char c = buffer.charAt(index);
			if (c == '\\') {
				if (escape) {
					sb.append(c);
					escape = false;
				} else {
					escape = true;
				}
			} else if (c == '"') {
				if (escape) {
					sb.append(c);
					escape = false;
				} else {
					// Bail out.
					closingQuotes = true;
				}
			} else {
				if (escape) {
					sb.append('\\');
				}
				sb.append(c);
				escape = false;
			}
		}
		buffer.delete(0, index);
		return sb.toString();
	}

	/** 
	 * Fast String Buffer class. MIParser does a lot
	 * of deleting off the front of a string, that's clearly
	 * an order N operation for StringBuffer which makes 
	 * the MIParser an order N^2 operation. There are "issues"
	 * with this for large arrays. Use of FSB rather than String
	 * Buffer makes MIParser N rather than N^2 because FSB can 
	 * delete from the front in constant time.
	 */
	public class FSB {
		StringBuffer buf;
		int pos;
		boolean shared;

		public FSB(StringBuffer buf) {
			this.buf = buf;
			pos = 0;
			shared = false;
		}

		public FSB(FSB fbuf) {
			pos = fbuf.pos;
			buf = fbuf.buf;
			shared = true;
		}

		public int length() {
			int res = buf.length() - pos;
			if (res < 0)
				return 0;

			return res;
		}

		public char charAt(int index) {
			return buf.charAt(index + pos);
		}

		private void resolveCopy() {
			if (shared) {
				buf = new StringBuffer(buf.toString());
				shared = false;
			}
		}

		public FSB deleteCharAt(int index) {
			if (index == 0) {
				pos++;
			} else {
				resolveCopy();
				buf = buf.deleteCharAt(pos + index);
			}

			return this;
		}

		public FSB delete(int start, int end) {
			if (start == 0) {
				pos = pos + end - start;
			} else {
				resolveCopy();
				buf.delete(start + pos, end + pos);
			}

			return this;
		}

		public void setLength(int a) {
			if (a == 0)
				pos = buf.length();
			else {
				// panic! fortunately we don't do this.
			}
		}

		public String substring(int start, int end) {
			return buf.substring(start + pos, end + pos);
		}

		public String toString() {
			return buf.substring(pos, buf.length());
		}

		int indexOf(char c) {
			int len = buf.length();
			for (int i = pos; i < len; i++) {
				if (buf.charAt(i) == c)
					return i - pos;
			}

			return -1;
		}

		boolean startsWith(String s) {
			int len = Math.min(s.length(), length());
			if (len < s.length())
				return false;

			for (int i = 0; i < len; i++) {
				if (s.charAt(i) != buf.charAt(pos + i))
					return false;
			}

			return true;
		}
	}
}
