package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 */
public class MIParser {

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
				token = token.substring(i);
			}

			if (token.charAt(0) == '^') {
					rr = processMIResultRecord(token.substring(1), id);
			} else {
					MIOOBRecord br = processMIOOBRecord(token.substring(1), id);
					if (br != null) {
						oobs.add(br);
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
		if (buffer.startsWith("done")) {
			rr.setResultClass("done");
		} else if (buffer.startsWith("error")) {
			rr.setResultClass("error");
		} else if (buffer.startsWith("exit")) {
			rr.setResultClass("exit");
		} else if (buffer.startsWith("running")) {
			rr.setResultClass("running");
		} else if (buffer.startsWith("connected")) {
			rr.setResultClass("connected");
		} else {
			// FIXME:
			// Error throw an exception?
		}
		int i = buffer.indexOf( ',' );
		if (i != -1) {
			String s = buffer.substring(i + 1);
			MIResult[] res = processMIResults(s);
			rr.setResults(res);
		}
		return rr;
	}

	MIOOBRecord processMIOOBRecord(String buffer, int id) {
		return null;
	}

	MIResult[] processMIResults(String buffer) {
		return new MIResult[0];
	}
}
