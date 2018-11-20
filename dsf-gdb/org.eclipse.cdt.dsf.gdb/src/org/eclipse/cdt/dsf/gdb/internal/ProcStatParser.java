/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 396268)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marc Dumais
 * TODO: extend to more than the tick counters.
 * @see also http://www.linuxhowtos.org/System/procstat.htm
 */
public class ProcStatParser {

	private ProcStatCounters cpuCoreCounters;
	private ProcStatCounters cpuCoreCountersOld;

	public ProcStatParser() {

	}

	/**
	 * Read and parse the /proc/stat file given as param
	 * @param fileName
	 */
	public void parseStatFile(String fileName) throws FileNotFoundException, NumberFormatException {
		cpuCoreCountersOld = cpuCoreCounters;

		File statFile = new File(fileName);
		if (!statFile.exists()) {
			throw new FileNotFoundException();
		}

		cpuCoreCounters = new ProcStatCounters();
		BufferedReader reader = null;
		try {
			String coreId;
			Reader r = new InputStreamReader(new FileInputStream(statFile));
			reader = new BufferedReader(r);
			String line;
			// ex: "cpu0 2048635 3195 385292 66149962 895977 22 36130 0 0 0"
			// note: we intentionally do not catch the "cpu" (without a core number) line.
			Pattern patternCpu = Pattern.compile("^(cpu[0-9]+)(.*)$"); //$NON-NLS-1$

			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// catch "cpu" lines from /proc/stat
				Matcher matcherCpu = patternCpu.matcher(line);
				if (matcherCpu.find()) {
					Vector<Integer> ticks = new Vector<>();
					coreId = matcherCpu.group(1);
					// extract the counters for current cpu line
					for (String tick : matcherCpu.group(2).trim().split("\\s+")) { //$NON-NLS-1$
						ticks.add(Integer.parseInt(tick));
					}

					cpuCoreCounters.addTickCounters(coreId, ticks.toArray(new Integer[ticks.size()]));
				}
			}

		} catch (IOException e) {
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				/* Don't care */}
			reader = null;
		}

	}

	/**
	 * @return a Map of the computed CPU/core loads.  The load of individual
	 * CPUs/cores can be found with keys "cpuN", where N is the CPU/core
	 * number, starting with 0, as found in /proc/stat .
	 *
	 */
	public ProcStatCoreLoads getCpuLoad() {
		return cpuCoreCounters.computeLoads(cpuCoreCountersOld);
	}

}
