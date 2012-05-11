/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses the GDB "tdump" command printout, as returned by GDB, to make it a
 * bit more human-friendly.
 * <p>
 * See bottom of this file for a raw example of what is
 * returned by "tdump"
 * @since 4.0
 */
public class CLITraceDumpInfo extends MIInfo {

	// Here is what this pattern looks-for - the first line of the tdump printout:
	//~"Data collected at tracepoint 2, trace frame 555:\n"
	private static final Pattern RESULT_PATTERN_TPINFO = Pattern.compile(
			"Data collected at tracepoint (\\d+), trace frame (\\d+)",  Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	// raw output of command
	private String fOutput = null;
	private String fParsedOutput = null;

	// tdump parsed info
	private String fTracepointNum = null;
	private String fTraceFrameNumber = null;

	// keep the tdump header in parsed result or not - by default we keep
	private static final boolean KEEP_HEADER = true;

	/**
	 * Constructor.
	 * @param out the output of the tdump printout
	 */
	public CLITraceDumpInfo(MIOutput out) {
		super(out);
		parse(KEEP_HEADER);
	}
	
	/**
	 * Alternative constructor.  Use this one to have control if the tdump
	 * header is kept or not in the result.  
	 * @param out the output of the tdump printout
	 * @param keepHeader keep the tdump header in result or not
	 */
	public CLITraceDumpInfo(MIOutput out, boolean keepHeader) {
		super(out);
		parse(keepHeader);
	}

	
	/**
	 * Do a quick parse of the tdump printout.  The tdump command printout is 
	 * split in short pieces (records), each one wrapped like this:
	 * <p>
	 * ~"eax            0x10"
	 * <p>
	 * Also, tabs and newlines are represented by symbols: \n and \t . 
	 * <p> 
	 * In this method, we strip the wrapping off each record and translate the 
	 * symbols to their value.   The resulting string is not parsed further.
	 * <p>
	 * See an example of a tdump printout at the end of this file.
	 */
	private void parse(boolean keepHeader) {
		final Pattern RESULT_PATTERN_UNWRAPRECORD = Pattern.compile("~\"(.*)\"",  Pattern.CANON_EQ); //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		String unwrapped;
		if (isDone()) {
			MIOutput out = getMIOutput();
			// save raw output of command
			fOutput = out.toString();
			
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (MIOOBRecord oob : oobs) {
				if (oob instanceof MIConsoleStreamOutput) {
					Matcher matcher = RESULT_PATTERN_UNWRAPRECORD.matcher(oob.toString());
					if (matcher.find()) {
						unwrapped = matcher.group(1);
						// accumulate the records into a buffer
						buf.append(unwrapped);
					}
				}
			}
			// convert buffer into string
			fParsedOutput = buf.toString();
			// extract the tracepoint and frame numbers
			Matcher matcher = RESULT_PATTERN_TPINFO.matcher(fParsedOutput);	
			if (matcher.find()) {
				fTracepointNum = matcher.group(1).trim();
				fTraceFrameNumber = matcher.group(2).trim();
			}

			// command result has the substrings "\n" and "\t" in it. 
			// replace them by their actual meaning (real newline and tab)
			fParsedOutput = fParsedOutput.replaceAll("\\\\n", "\n");   //$NON-NLS-1$ //$NON-NLS-2$
			fParsedOutput = fParsedOutput.replaceAll("\\\\t+", "\t");   //$NON-NLS-1$ //$NON-NLS-2$

			// Optionaly remove the header line from tdump printout 
			if(!keepHeader) {
				fParsedOutput = fParsedOutput.replaceFirst("Data collected at tracepoint \\d+, trace frame \\d+:\\n", "");   //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}


	/**
	 * @return the raw output of tdump.
	 */
	public String getOutput() {
		return fOutput;
	}

	/**
	 * @return a String containing the semi-parsed
	 * register and local variables, as listed by
	 * tdump.
	 */
	public String getContent() {
		return fParsedOutput;
	}
	
	/**
	 * @return the tracepoint number
	 */
	public String getTracepointNumber() {
		return fTracepointNum;
	}


	/**
	 * @return the trace's frame number
	 */
	public String getFrameNumber() {
		return fTraceFrameNumber;
	}

	/**
	 * @return the timestamp of the tracepoint frame
	 */
	public String getTimestamp() {
		// Timestamp not yet available in printout of command
		// "tdump" -> revisit when it is.
		return null;
	}
}


/*  
 * Example of raw output from command tdump:

&"tdump\n"
~"Data collected at tracepoint 2, trace frame 555:\n"
~"eax            0x10"
~"\t16"
~"\n"
~"ecx            0x0"
~"\t0"
~"\n"
~"edx            0x0"
~"\t0"
~"\n"
~"ebx            0x11"
~"\t17"
~"\n"
~"esp            0xbfec14c0"
~"\t0xbfec14c0\n"
~"ebp            0xbfec1508"
~"\t0xbfec1508\n"
~"esi            0x0"
~"\t0"
~"\n"
~"edi            0x0"
~"\t0"
~"\n"
~"eip            0x8048520"
~"\t0x8048520 <factorial1(unsigned long long)+45>\n"
~"eflags         0x216"
~"\t[ PF AF IF ]"
~"\n"
~"cs             0x73"
~"\t115"
~"\n"
~"ss             0x7b"
~"\t123"
~"\n"
~"ds             0x7b"
~"\t123"
~"\n"
~"es             0x7b"
~"\t123"
~"\n"
~"fs             0x0"
~"\t0"
~"\n"
~"gs             0x33"
~"\t51"
~"\n"
~"st0            0"
~"\t(raw 0x00000000000000000000)\n"
~"st1            0"
~"\t(raw 0x00000000000000000000)\n"
~"st2            0"
~"\t(raw 0x00000000000000000000)\n"
~"st3            0"
~"\t(raw 0x00000000000000000000)\n"
~"st4            0"
~"\t(raw 0x00000000000000000000)\n"
~"st5            0"
~"\t(raw 0x00000000000000000000)\n"
~"st6            0"
~"\t(raw 0x00000000000000000000)\n"
~"st7            0"
~"\t(raw 0x00000000000000000000)\n"
~"fctrl          0x37f"
~"\t895"
~"\n"
~"fstat          0x0"
~"\t0"
~"\n"
~"ftag           0xffff"
~"\t65535"
~"\n"
~"fiseg          0x73"
~"\t115"
~"\n"
~"fioff          0x718d9c"
~"\t7441820"
~"\n"
~"foseg          0x7b"
~"\t123"
~"\n"
~"fooff          0x754928"
~"\t7686440"
~"\n"
~"fop            0x599"
~"\t1433"
~"\n"
~"xmm0           {v4_float = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_double = {0x0"
~", 0x0"
~"}"
~", v16_int8 = {0x0"
~" <repeats 16 times>}"
~", v8_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v4_int32 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_int64 = {0x0"
~", 0x0"
~"}"
~", uint128 = 0x00000000000000000000000000000000"
~"}"
~"\n"
~"xmm1           {v4_float = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_double = {0x0"
~", 0x0"
~"}"
~", v16_int8 = {0x0"
~" <repeats 16 times>}"
~", v8_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v4_int32 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_int64 = {0x0"
~", 0x0"
~"}"
~", uint128 = 0x00000000000000000000000000000000"
~"}"
~"\n"
~"xmm2           {v4_float = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_double = {0x0"
~", 0x0"
~"}"
~", v16_int8 = {0x0"
~" <repeats 16 times>}"
~", v8_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v4_int32 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_int64 = {0x0"
~", 0x0"
~"}"
~", uint128 = 0x00000000000000000000000000000000"
~"}"
~"\n"
~"xmm3           {v4_float = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_double = {0x0"
~", 0x0"
~"}"
~", v16_int8 = {0x0"
~" <repeats 16 times>}"
~", v8_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v4_int32 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_int64 = {0x0"
~", 0x0"
~"}"
~", uint128 = 0x00000000000000000000000000000000"
~"}"
~"\n"
~"xmm4           {v4_float = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_double = {0x0"
~", 0x0"
~"}"
~", v16_int8 = {0x0"
~" <repeats 16 times>}"
~", v8_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v4_int32 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_int64 = {0x0"
~", 0x0"
~"}"
~", uint128 = 0x00000000000000000000000000000000"
~"}"
~"\n"
~"xmm5           {v4_float = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_double = {0x0"
~", 0x0"
~"}"
~", v16_int8 = {0x0"
~" <repeats 16 times>}"
~", v8_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v4_int32 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_int64 = {0x0"
~", 0x0"
~"}"
~", uint128 = 0x00000000000000000000000000000000"
~"}"
~"\n"
~"xmm6           {v4_float = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_double = {0x0"
~", 0x0"
~"}"
~", v16_int8 = {0x0"
~" <repeats 16 times>}"
~", v8_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v4_int32 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_int64 = {0x0"
~", 0x0"
~"}"
~", uint128 = 0x00000000000000000000000000000000"
~"}"
~"\n"
~"xmm7           {v4_float = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_double = {0x0"
~", 0x0"
~"}"
~", v16_int8 = {0x0"
~" <repeats 16 times>}"
~", v8_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v4_int32 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v2_int64 = {0x0"
~", 0x0"
~"}"
~", uint128 = 0x00000000000000000000000000000000"
~"}"
~"\n"
~"mxcsr          0x1f80"
~"\t[ IM DM ZM OM UM PM ]"
~"\n"
~"mm0            {uint64 = 0x0"
~", v2_int32 = {0x0"
~", 0x0"
~"}"
~", v4_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v8_int8 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~"}"
~"\n"
~"mm1            {uint64 = 0x0"
~", v2_int32 = {0x0"
~", 0x0"
~"}"
~", v4_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v8_int8 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~"}"
~"\n"
~"mm2            {uint64 = 0x0"
~", v2_int32 = {0x0"
~", 0x0"
~"}"
~", v4_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v8_int8 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~"}"
~"\n"
~"mm3            {uint64 = 0x0"
~", v2_int32 = {0x0"
~", 0x0"
~"}"
~", v4_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v8_int8 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~"}"
~"\n"
~"mm4            {uint64 = 0x0"
~", v2_int32 = {0x0"
~", 0x0"
~"}"
~", v4_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v8_int8 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~"}"
~"\n"
~"mm5            {uint64 = 0x0"
~", v2_int32 = {0x0"
~", 0x0"
~"}"
~", v4_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v8_int8 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~"}"
~"\n"
~"mm6            {uint64 = 0x0"
~", v2_int32 = {0x0"
~", 0x0"
~"}"
~", v4_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v8_int8 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~"}"
~"\n"
~"mm7            {uint64 = 0x0"
~", v2_int32 = {0x0"
~", 0x0"
~"}"
~", v4_int16 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~", v8_int8 = {0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~", 0x0"
~"}"
~"}"
~"\n"
~"a = 16"
~"\n"
27^done
 */
