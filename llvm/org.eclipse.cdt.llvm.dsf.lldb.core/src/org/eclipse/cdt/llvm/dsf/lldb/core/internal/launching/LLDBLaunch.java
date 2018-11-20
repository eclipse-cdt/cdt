/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBDebugPreferenceConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBLaunchConfigurationConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.ILLDBConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.LLDBCorePlugin;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.LLDBTrait;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * LLDB specific launch. It mostly deals with setting up the paths correctly.
 */
public class LLDBLaunch extends GdbLaunch {

	private static final String XCODE_HINT = "(Xcode 7.3.1)"; //$NON-NLS-1$
	private static final IntegerTuple LLDB_MINIMUM_REVISION = new IntegerTuple(350, 0, 21, 9);
	private static final IntegerTuple LLDB_MINIMUM_VERSION = new IntegerTuple(3, 8, 0);
	private static final Pattern LLDB_VERSION_PATTERN = Pattern.compile("lldb\\s*version\\s*(\\d+)\\.(\\d+)\\.(\\d+).*", //$NON-NLS-1$
			Pattern.DOTALL); //;
	private static final Pattern LLDB_REVISION_PATTERN = Pattern.compile("lldb-(\\d+)\\.(\\d+)\\.(\\d+)(\\.(\\d)+)?.*", //$NON-NLS-1$
			Pattern.DOTALL);

	private IntegerTuple fLldbVersion;
	private IntegerTuple fLldbRevision;
	private Set<LLDBTrait> fTraits;

	/**
	 * Constructs a launch.
	 *
	 * @param launchConfiguration
	 *            the launch configuration
	 * @param mode
	 *            the launch mode, i.e., debug, profile, etc.
	 * @param locator
	 */
	public LLDBLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
	}

	/*
	 * TODO: GdbLaunch.getGDBPath() and setGDBPath() should reference each other
	 * in the javadoc to make sure extenders override both.
	 */
	@Override
	public IPath getGDBPath() {
		String lldbPath = getAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME);
		if (lldbPath != null) {
			return new Path(lldbPath);
		}

		return getLLDBPath(getLaunchConfiguration());
	}

	@Override
	public void setGDBPath(String path) {
		setAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, path);
	}

	/**
	 * Get the LLDB path based on a launch configuration.
	 *
	 * @param configuration
	 *            the launch configuration.
	 * @return the LLDB path
	 */
	public static IPath getLLDBPath(ILaunchConfiguration configuration) {
		String defaultLLdbCommand = getDefaultLLDBPath();

		IPath retVal = new Path(defaultLLdbCommand);
		try {
			String lldbPath = configuration.getAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
					defaultLLdbCommand);
			lldbPath = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(lldbPath,
					false);
			retVal = new Path(lldbPath);
		} catch (CoreException e) {
			LLDBCorePlugin.getDefault().getLog().log(e.getStatus());
		}
		return retVal;
	}

	@Override
	protected String getDefaultGDBPath() {
		return getDefaultLLDBPath();
	}

	@Override
	public String getGDBVersion() throws CoreException {
		String gdbVersion = super.getGDBVersion();
		computeLLDBVersions();
		if (fLldbRevision != null) {
			if (fLldbRevision.compareTo(LLDB_MINIMUM_REVISION) < 0) {
				throw new DebugException(LLDBCorePlugin.createStatus(MessageFormat.format(
						Messages.LLDBLaunch_minimum_version_error, fLldbRevision, LLDB_MINIMUM_REVISION, XCODE_HINT)));
			}
		} else if (fLldbVersion != null) {
			if (fLldbVersion.compareTo(LLDB_MINIMUM_VERSION) < 0) {
				throw new DebugException(LLDBCorePlugin.createStatus(MessageFormat
						.format(Messages.LLDBLaunch_minimum_version_error, fLldbVersion, LLDB_MINIMUM_VERSION, ""))); //$NON-NLS-1$
			}
		}

		return gdbVersion;
	}

	private void computeLLDBVersions() throws CoreException {
		if (fLldbRevision != null || fLldbVersion != null) {
			return;
		}

		// LLDB-MI always outputs the GDB version so try LLDB (non-MI)
		// FIXME: There should be a better way to get the lldb version number
		// from lldb-mi
		IPath lldbMiPath = getGDBPath();
		String lastSegment = lldbMiPath.lastSegment();
		if (lastSegment.contains(ILLDBConstants.LLDB_MI_EXECUTABLE_NAME)) {
			lastSegment = lastSegment.replace(ILLDBConstants.LLDB_MI_EXECUTABLE_NAME,
					ILLDBConstants.LLDB_EXECUTABLE_NAME);
		}
		lldbMiPath = lldbMiPath.removeLastSegments(1).append(lastSegment);

		String cmd = lldbMiPath + " --version"; //$NON-NLS-1$

		// Parse cmd to properly handle spaces and such things (bug 458499)
		String[] args = CommandLineUtil.argumentsToArray(cmd);

		Process process = null;
		Job timeoutJob = null;
		try {
			process = ProcessFactory.getFactory().exec(args, getLaunchEnvironment());

			// Start a timeout job to make sure we don't get stuck waiting for
			// an answer from a gdb that is hanging
			// Bug 376203
			final Process finalProc = process;
			timeoutJob = new Job("LLDB version timeout job") { //$NON-NLS-1$
				{
					setSystem(true);
				}

				@Override
				protected IStatus run(IProgressMonitor arg) {
					// Took too long. Kill the lldb process and
					// let things clean up.
					finalProc.destroy();
					return Status.OK_STATUS;
				}
			};
			timeoutJob.schedule(10000);

			String streamOutput = readStream(process.getInputStream());

			fLldbVersion = getLLDBVersionFromText(streamOutput);
			fLldbRevision = getLLDBRevisionFromText(streamOutput);
			if (fLldbVersion == null && fLldbRevision == null) {
				if (!streamOutput.isEmpty()) {
					// We got some output but couldn't parse it. Make that
					// output visible to the user in the error dialog.
					Exception detailedException = new Exception("Unexpected output format: \n\n" + streamOutput); //$NON-NLS-1$
					throw new DebugException(LLDBCorePlugin.createStatus(
							"Could not determine LLDB version using command: " + StringUtil.join(args, " "), //$NON-NLS-1$ //$NON-NLS-2$
							detailedException));
				}
			}
			computeTraits();
		} catch (IOException e) {
			// Since we can't use lldb-mi for version checking, we try to use
			// the lldb executable but it's possible that it's not there at all
			// and that shouldn't prevent users to start debugging with lldb-mi.
			// So here we log instead of throwing an exception and stopping the
			// launch.
			LLDBCorePlugin.log(new DebugException(new Status(IStatus.ERROR, LLDBCorePlugin.PLUGIN_ID,
					DebugException.REQUEST_FAILED, "Error with command: " + StringUtil.join(args, " "), e))); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			// If we get here we are obviously not stuck reading the stream so
			// we can cancel the timeout job.
			// Note that it may already have executed, but that is not a
			// problem.
			if (timeoutJob != null) {
				timeoutJob.cancel();
			}

			if (process != null) {
				process.destroy();
			}
		}
	}

	private void computeTraits() {
		if (fTraits == null) {
			fTraits = new HashSet<>();

			// Here are some LLDB/Xcode version mappings
			// 360.1.65 => Xcode 8.1.0
			// 360.1.70 => Xcode 8.2.1, 8.2.0
			// 370.0.37 => Xcode 8.3.0
			// 370.0.40 => Xcode 8.3.1
			// 902.0.79.7 => Xcode 9.4.1
			// 1000.11.37.1 => Xcode 10.0
			//
			// Note that a LLDB built from source on macOS can report the same
			// Apple-style version even for different LLDB/Clang-style version
			// For example, 3.9.1 and 4.0.0 both report 360.99.0, how
			// inconvenient! But this will only affect people building it from
			// source, not LLDB included in Xcode.

			if (fLldbVersion != null && fLldbVersion.compareTo(new IntegerTuple(4, 0, 0)) < 0
					|| fLldbRevision != null && fLldbRevision.compareTo(new IntegerTuple(370, 0, 37)) < 0) {
				fTraits.add(LLDBTrait.BROKEN_BREAKPOINT_INSERT_FULL_PATH_LLVM_BUG_28709);
			}

			if (fLldbVersion != null && fLldbVersion.compareTo(new IntegerTuple(8, 0, 0)) < 0
					|| fLldbRevision != null) {
				fTraits.add(LLDBTrait.MISSING_GDB_SET_BREAKPOINT_PENDING);
			}
		}
	}

	/**
	 * Read from the specified stream and return what was read.
	 *
	 * @param stream
	 *            The input stream to be used to read the data. This method will
	 *            close the stream.
	 * @return The data read from the stream
	 * @throws IOException
	 *             If an IOException happens when reading the stream
	 */
	private static String readStream(InputStream stream) throws IOException {
		StringBuilder cmdOutput = new StringBuilder(200);
		try {
			Reader r = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(r);

			String line;
			while ((line = reader.readLine()) != null) {
				cmdOutput.append(line);
				cmdOutput.append('\n');
			}
			return cmdOutput.toString();
		} finally {
			// Cleanup to avoid leaking pipes
			// Bug 345164
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static class IntegerTuple implements Comparable<IntegerTuple> {
		private Integer[] fIntegers;

		private IntegerTuple(Integer... integers) {
			fIntegers = integers;
		}

		@Override
		public int compareTo(IntegerTuple o) {
			for (int i = 0; i < fIntegers.length; i++) {
				if (i >= o.fIntegers.length) {
					// All numbers are the same up to now but the other tuple
					// has less
					return 1;
				}

				int compareTo = fIntegers[i].compareTo(o.fIntegers[i]);
				if (compareTo != 0) {
					return compareTo;
				}
			}

			// All numbers are the same up to now but this tuple has less than
			// the other
			if (fIntegers.length < o.fIntegers.length) {
				return -1;
			}

			return 0;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < fIntegers.length; i++) {
				sb.append(fIntegers[i]);
				if (i != fIntegers.length - 1) {
					sb.append("."); //$NON-NLS-1$
				}
			}
			return sb.toString();
		}
	}

	/**
	 * This depends on the SVN revision, for example 350.0.21.9
	 *
	 * @param versionOutput
	 *            output text from "lldb --version" command .
	 * @return String representation of revision of lldb such as "350.0.21.9" on
	 *         success; null otherwise.
	 */
	private static IntegerTuple getLLDBRevisionFromText(String versionOutput) {
		// These are the LLDB version patterns I have seen up to now
		// Apple Xcode 7.3.1: lldb-350.0.21.9
		// LLVM build: lldb-360.99.0

		Matcher matcher = LLDB_REVISION_PATTERN.matcher(versionOutput);
		if (!matcher.matches()) {
			return null;
		}

		try {
			Integer major = Integer.valueOf(matcher.group(1));
			Integer minor = Integer.valueOf(matcher.group(2));
			Integer micro = Integer.valueOf(matcher.group(3));
			String patchGroup = matcher.group(5);
			if (patchGroup != null) {
				Integer patch = Integer.valueOf(patchGroup);
				return new IntegerTuple(major, minor, micro, patch);
			} else {
				return new IntegerTuple(major, minor, micro);
			}
		} catch (NumberFormatException e) {
			LLDBCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Returns Clang-style/LLVM version, for example 3.9.0
	 *
	 * @param versionOutput
	 *            output text from "lldb --version" command .
	 * @return String representation of version of lldb such as "3.9.0" on
	 *         success; null otherwise.
	 */
	private static IntegerTuple getLLDBVersionFromText(String versionOutput) {
		// These are the LLDB version patterns I have seen up to now
		// Ubuntu 14.04: lldb version 3.6.0 ( revision )
		// Ubuntu 14.04: lldb version 3.8.0 ( revision )

		Matcher matcher = LLDB_VERSION_PATTERN.matcher(versionOutput);
		if (!matcher.find()) {
			return null;
		}

		try {
			Integer major = Integer.valueOf(matcher.group(1));
			Integer minor = Integer.valueOf(matcher.group(2));
			Integer micro = Integer.valueOf(matcher.group(3));
			IntegerTuple version = new IntegerTuple(major, minor, micro);
			return version;
		} catch (NumberFormatException e) {
			LLDBCorePlugin.log(e);
		}
		return null;
	}

	private static String getDefaultLLDBPath() {
		return Platform.getPreferencesService().getString(LLDBCorePlugin.PLUGIN_ID,
				ILLDBDebugPreferenceConstants.PREF_DEFAULT_LLDB_COMMAND,
				ILLDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT, null);
	}

	@Override
	public String getGDBInitFile() throws CoreException {
		// Not supported by LLDB-MI right now. There is also no MI command in
		// GDB to source a file. We should look into adding this in GDB first.
		return null;
	}

	/**
	 * Returns whether or not the LLDB use by this launch has the given trait.
	 *
	 * @param trait
	 *            the trait to check
	 * @return if the launch has this trait for the LLDB, false otherwise
	 */
	public boolean hasTrait(LLDBTrait trait) {
		return fTraits.contains(trait);
	}
}
