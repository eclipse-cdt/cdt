/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
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

		StringTokenizer st = new StringTokenizer(buffer, "\n");
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

			// Process ResultRecord | Out-Of-Band Records
			if (token.length() > 0) {
				if (token.charAt(0) == '^') {
					token.deleteCharAt(0);
					rr = processMIResultRecord(token, id);
				} else if(token.toString().startsWith(MIOutput.terminator)) {
					break;
				} else {
					MIOOBRecord band = processMIOOBRecord(token, id);
					if (band != null) {
						oobs.add(band);
					}
				}
			}
		}
		MIOOBRecord[] bands =
			(MIOOBRecord[]) oobs.toArray(new MIOOBRecord[oobs.size()]);
		mi.setMIOOBRecords(bands);
		mi.setMIResultRecord(rr);
		return mi;
	}

	/**
	 * Assuming '^' was deleted.
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
			MIResult[] res = processMIResults(buffer);
			rr.setMIResults(res);
		}
		return rr;
	}

	/**
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
			}
			MIResult[] res = processMIResults(buffer);
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
			stream.setCString(translateCString(buffer));
			oob = stream;
		}
		return oob;
	}

	/**
	 * Assuming that the usual leading comma was consume.
	 */
	private MIResult[] processMIResults(StringBuffer buffer) {
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
	 * has moving forward constructing the AST.
	 */
	private MIResult processMIResult(StringBuffer buffer) {
		MIResult result = new MIResult();
		int equal;
		if (buffer.length() > 0 && Character.isLetter(buffer.charAt(0))
			&& (equal = buffer.toString().indexOf('=')) != -1) {
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
	private MIValue processMIValue(StringBuffer buffer) {
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
	private MIValue processMITuple(StringBuffer buffer) {
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
	private MIValue processMIList(StringBuffer buffer) {
		MIList list = new MIList();
		List valueList = new ArrayList();
		List resultList = new ArrayList();
		// catch closing ']'
		while (buffer.length() > 0 && buffer.charAt(0) != ']') {
			// Try for the MIValue first
			MIValue value = processMIValue(buffer);
			if (value != null) {
				valueList.add(value);
			} else  {
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
		MIValue[] values = (MIValue[])valueList.toArray(new MIValue[valueList.size()]);
		MIResult[] res = (MIResult[])resultList.toArray(new MIResult[resultList.size()]);
		list.setMIValues(values);
		list.setMIResults(res);
		return list;
	}

	private String translateCString(StringBuffer buffer) {
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
					sb.append(c);;
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
}
