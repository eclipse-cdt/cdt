package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
<pre>
`OUTPUT ==>'
     `( OUT-OF-BAND-RECORD )* [ RESULT-RECORD ] "(gdb)" NL'

`RESULT-RECORD ==>'
     ` [ TOKEN ] "^" RESULT-CLASS ( "," RESULT )* NL'

`OUT-OF-BAND-RECORD ==>'
     `ASYNC-RECORD | STREAM-RECORD'

`ASYNC-RECORD ==>'
     `EXEC-ASYNC-OUTPUT | STATUS-ASYNC-OUTPUT | NOTIFY-ASYNC-OUTPUT'

`EXEC-ASYNC-OUTPUT ==>'
     `[ TOKEN ] "*" ASYNC-OUTPUT'

`STATUS-ASYNC-OUTPUT ==>'
     `[ TOKEN ] "+" ASYNC-OUTPUT'

`NOTIFY-ASYNC-OUTPUT ==>'
     `[ TOKEN ] "=" ASYNC-OUTPUT'

`ASYNC-OUTPUT ==>'
     `ASYNC-CLASS ( "," RESULT )* NL'

`RESULT-CLASS ==>'
     `"done" | "running" | "connected" | "error" | "exit"'

`ASYNC-CLASS ==>'
     `"stopped" | OTHERS' (where OTHERS will be added depending on the
     needs--this is still in development).

`RESULT ==>'
     ` VARIABLE "=" VALUE'

`VARIABLE ==>'
     ` STRING '

`VALUE ==>'
     ` CONST | TUPLE | LIST '

`CONST ==>'
     `C-STRING'

`TUPLE ==>'
     ` "{}" | "{" RESULT ( "," RESULT )* "}" '

`LIST ==>'
     ` "[]" | "[" VALUE ( "," VALUE )* "]" | "[" RESULT ( "," RESULT )*
     "]" '

`STREAM-RECORD ==>'
     `CONSOLE-STREAM-OUTPUT | TARGET-STREAM-OUTPUT | LOG-STREAM-OUTPUT'

`CONSOLE-STREAM-OUTPUT ==>'
     `"~" C-STRING'

`TARGET-STREAM-OUTPUT ==>'
     `"@" C-STRING'

`LOG-STREAM-OUTPUT ==>'
     `"&" C-STRING'

`NL ==>'
     `CR | CR-LF'

`TOKEN ==>'
     _any sequence of digits_.

`C-STRING ==>'
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
			String token = st.nextToken();

			// Fetch the Token/Id
			if (Character.isDigit(token.charAt(0))) {
				int i = 1;
				while (Character.isDigit(token.charAt(i)) && i < token.length()) {
					i++;
				}
				String numbers = token.substring(i);
				try {
					id = Integer.parseInt(numbers);
				} catch(NumberFormatException e) {
				}
				// Consume the token.
				token = token.substring(i);
			}

			// Process ResultRecord | Out-Of-Band Records
			if (token.charAt(0) == '^') {
					rr = processMIResultRecord(token.substring(1), id);
			//} else if(token.startsWith(MIOutput.terminator)) {
			//  break;
			} else {
					MIOOBRecord band = processMIOOBRecord(token.substring(1), id);
					if (band != null) {
						oobs.add(band);
					}
			}
		}
		MIOOBRecord[] bands = (MIOOBRecord[])oobs.toArray(new MIOOBRecord[oobs.size()]);
		mi.setMIOOBRecords(bands);
		mi.setMIResultRecord(rr);
		return mi;
	}

	MIResultRecord processMIResultRecord(String buffer, int id) {
		MIResultRecord rr = new MIResultRecord();
		rr.setToken(id);
		if (buffer.startsWith(MIResultRecord.DONE)) {
			rr.setResultClass(MIResultRecord.DONE);
			buffer = buffer.substring(MIResultRecord.DONE.length());
		} else if (buffer.startsWith(MIResultRecord.ERROR)) {
			rr.setResultClass(MIResultRecord.ERROR);
			buffer = buffer.substring(MIResultRecord.ERROR.length());
		} else if (buffer.startsWith(MIResultRecord.EXIT)) {
			rr.setResultClass(MIResultRecord.EXIT);
			buffer = buffer.substring(MIResultRecord.EXIT.length());
		} else if (buffer.startsWith(MIResultRecord.RUNNING)) {
			rr.setResultClass(MIResultRecord.RUNNING);
			buffer = buffer.substring(MIResultRecord.RUNNING.length());
		} else if (buffer.startsWith(MIResultRecord.CONNECTED)) {
			rr.setResultClass(MIResultRecord.CONNECTED);
			buffer = buffer.substring(MIResultRecord.CONNECTED.length());
		} else {
			// FIXME:
			// Error throw an exception?
		}

		// Results are separated by commas.
		if (buffer.charAt(0) == ',') {
			String s = buffer.substring(1);
			MIResult[] res = processMIResults(s);
			rr.setMIResults(res);
		}
		return rr;
	}

	/**
     */
	MIOOBRecord processMIOOBRecord(String buffer, int id) {
		MIOOBRecord oob = null;
		char c = buffer.charAt(0);
		if (c == '*' || c == '+' || c == '=') {
			MIAsyncRecord async = null;
			switch (c) {
				case '*':
					async = new MIExecAsyncOutput();
				break;

				case '+':
					async = new MIStatusAsyncOutput();
				break;

				case '=':
					async = new MINotifyAsyncOutput();
				break;
			}
			async.setToken(id);
			// Extract the Async-Class
			int i = buffer.indexOf(',');
			if (i != -1) {
				String asyncClass = buffer.substring(1, i);
				async.setAsyncClass(asyncClass);
				// Consume the async-class and the comma
				buffer = buffer.substring(i + 1);
			}
			MIResult[] res = processMIResults(buffer);
			async.setMIResults(res);
			oob = async;
		} else if (c == '~' || c == '@' || c == '&') {
			MIStreamRecord stream = null;
			switch (c) {
				case '~':
					stream = new MIConsoleStreamOutput();
				break;

				case '@':
					stream = new MITargetStreamOutput();
				break;

				case '&':
					stream = new MILogStreamOutput();
				break;
			}
			stream.setCString(translateCString(buffer.substring(1)));
			oob = stream;
		}
		return oob;
	}

	
	/**
	 * Assuming that the usual leading comma was consume.
	 */
	MIResult[] processMIResults(String buffer) {
		List aList = new ArrayList();
		StringBuffer sb = new StringBuffer(buffer);
		MIResult result = processMIResult(sb);
		if (result != null) {
			aList.add(result);
		}
		while (sb.length() > 0 && sb.charAt(0) == ',') {
			sb.deleteCharAt(0);
			result = processMIResult(sb);
			if (result != null) {
				aList.add(result);
			}
		}
		return (MIResult[])aList.toArray(new MIResult[aList.size()]);
	}

	MIResult processMIResult(StringBuffer sb) {
		MIResult result = new MIResult();
		String buffer = sb.toString();
		int equal;
		if (Character.isLetter(buffer.charAt(0)) &&
		    (equal = buffer.indexOf('=')) != -1) {
			String variable = buffer.substring(0, equal);
			result.setVariable(variable);
			sb.delete(0, equal + 1);
			MIValue value = processMIValue(sb);
			result.setMIValue(value);
		} else {
			result.setVariable(buffer);
			sb.setLength(0);
		}
		return result;
	}

	MIValue processMIValue(StringBuffer sb) {
		MIValue value = null;
		if (sb.charAt(0) == '{') {
			sb.deleteCharAt(0);
			value = processMITuple(sb);
		} else if (sb.charAt(0) == '[') {
			sb.deleteCharAt(0);
			value = processMIList(sb);
		} else if (sb.charAt(0) == '"') {
			MIConst cnst = new MIConst();
			cnst.setCString(translateCString(sb));
			value = cnst;
		}
		return value;
	}
	
	MIValue processMITuple(StringBuffer sb) {
		MITuple tuple = new MITuple();
		List aList = new ArrayList(1);
		// Catch closing '}'
		while (sb.length() > 0 && sb.charAt(0) != '}') {
			MIResult res = processMIResult(sb);
			if (res != null) {
				aList.add(res);
			}
		}
		MIResult[] results = (MIResult[])aList.toArray(new MIResult[aList.size()]);
		tuple.setMIResults(results);
		return tuple;
	}
	
	MIValue processMIList(StringBuffer sb) {
		// catch closing ']'
		return new MIList();
	}

	String translateCString(String str) {
		return translateCString(new StringBuffer(str));
	}

	// FIXME: TODO
	// FIXME: Side effect in the loop
	// FIXME: Deal with <repeat>
	String translateCString(StringBuffer sb) {
		boolean escape = false;
		boolean termination = false;
		if (sb.charAt(0) == '"') {
			sb.deleteCharAt(0);
		}
		int i = 0;
		for (i = 0; i < sb.length() || termination; i++) {
			switch (sb.charAt(i)) {
				case '\\':
					if (escape) {
						sb.setCharAt(i, '\\');
						sb.deleteCharAt(i - 1);
						escape = false;
					} else {
						escape = true;
					}
					break;

				case '"':
					if (escape) {
						sb.setCharAt(i, '"');
						sb.deleteCharAt(i - 1);
						escape = false;
					} else {
						termination = true;
					}
				break;

				default:
					escape = false;
			}
		}
		return sb.substring(0, i);
	}
}
