/*******************************************************************************
 * Copyright (c) 2007, 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.output;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class CLIInfoLineInfo extends MIInfo {

	private int lineNumber;
	private BigInteger startAddress;
	private BigInteger endAddress;
	private String startLocation;
	private String endLocation;
	private String fileName;

	public CLIInfoLineInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public BigInteger getStartAddress() {
		return startAddress;
	}

	public BigInteger getEndAddress() {
		return endAddress;
	}

	public String getStartLocation() {
		return startLocation;
	}

	public String getEndLocation() {
		return endLocation;
	}

	public String getFileName() {
		return fileName;
	}

	protected void parse() {
		List aList = new ArrayList();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in finding the current thread
					parseLineInfo(str.trim(), aList);
				}
			}
		}

	}

	protected void parseLineInfo(String str, List aList) {
		if (str.endsWith(".")) //$NON-NLS-1$
			str = str.substring(0, str.length()-1);
		String[] strbits = str.split("\\s"); //$NON-NLS-1$
		for (int i = 0; i < strbits.length; i++) {
			if (strbits[i].equals("Line")) //$NON-NLS-1$
			{
				lineNumber = Integer.parseInt(strbits[i+1]);
			}
			else
			if (strbits[i].equals("starts")) //$NON-NLS-1$
			{
				
				startAddress = new BigInteger(strbits[i+3].substring(2), 16);
				startLocation = strbits[i+4];
			}
			else
			if (strbits[i].equals("ends")) //$NON-NLS-1$
			{
				endAddress = new BigInteger(strbits[i+2].substring(2), 16);
				endLocation = i+3<strbits.length? strbits[i+3]: null;
			}
		}
		strbits = str.split("\""); //$NON-NLS-1$
		if (strbits.length>0) fileName = strbits[1];
	}

}
