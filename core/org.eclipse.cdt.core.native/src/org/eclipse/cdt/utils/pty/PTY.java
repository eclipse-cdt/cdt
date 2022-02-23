/*******************************************************************************
 * Copyright (c) 2002, 2021 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems, Inc. - bug 248071
 *     Martin Oberhuber (Wind River) - [303083] Split out the Spawner
 *******************************************************************************/
package org.eclipse.cdt.utils.pty;

import java.io.IOException;

import org.eclipse.cdt.internal.core.natives.CNativePlugin;
import org.eclipse.cdt.internal.core.natives.Messages;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.cdt.utils.spawner.Spawner.IChannel;
import org.eclipse.core.runtime.Platform;

/**
 * PTY - pseudo terminal support.
 */
public class PTY {

	/**
	 * The pty modes.
	 * @since 5.6
	 */
	public enum Mode {
		/** This mode is for use with an Eclipse console. */
		CONSOLE,
		/** This mode is for use with a terminal emulator. */
		TERMINAL
	}

	final Mode mode;
	/**
	 * Unused in conPTY.
	 * Created, but never read in winPTY.
	 * Important for Posix PTY.
	 */
	final String slave;
	final PTYInputStream in;
	final PTYOutputStream out;

	/**
	 * NOTE: Field is accessed by the native layer. Do not refactor!
	 * This field is NOT used by ConPTY layer.
	 */
	int master;

	private static boolean hasPTY;

	private enum IS_CONPTY {
		CONPTY_UNKNOWN, CONPTY_YES, CONPTY_NO;
	}

	/**
	 * We don't know if we have conpty until we try - so this starts
	 * null and will be true or false once it is determined.
	 */
	private static IS_CONPTY isConPTY = IS_CONPTY.CONPTY_UNKNOWN;
	private static boolean isWinPTY;
	private static boolean isConsoleModeSupported;
	private static boolean setTerminalSizeErrorAlreadyLogged;
	private ConPTY conPTY;

	/**
	 * The master fd is used on two streams. We need to wrap the fd
	 * so that when stream.close() is called the other stream is disabled.
	 */
	public class MasterFD {

		public int getFD() {
			return master;
		}

		void setFD(int fd) {
			master = fd;
		}
	}

	/**
	 * @return whether PTY support for console mode is available on this platform
	 */
	@Deprecated
	public static boolean isSupported() {
		return isSupported(Mode.CONSOLE);
	}

	/**
	 * @return whether PTY support for given mode is available on this platform
	 * @since 5.6
	 */
	public static boolean isSupported(Mode mode) {
		return hasPTY && (isConsoleModeSupported || mode == Mode.TERMINAL);
	}

	/**
	 * Create PTY for use with Eclipse console.
	 * Identical to <code>PTY(Mode.CONSOLE)</code>.
	 */
	public PTY() throws IOException {
		this(Mode.CONSOLE);
	}

	/**
	 * Create PTY for given mode.
	 *
	 * <p>
	 * The provided mode indicates whether the pseudo terminal is used with the interactive
	 * Eclipse console or a terminal emulation:
	 * <ul>
	 * <li><code>CONSOLE</code> - the terminal is configured with no echo and stderr is
	 * redirected to a pipe instead of the PTY. This mode is not supported on windows</li>
	 * <li><code>TERMINAL</code> - the terminal is configured with echo and stderr is
	 * connected to the PTY. This mode is best suited for use with a proper terminal emulation.
	 * Note that this mode might not be supported on all platforms.
	 * Known platforms which support this mode are:
	 * <code>linux-x86</code>, <code>linux-x86_64</code>, <code>solaris-sparc</code>, <code>macosx</code>.
	 * </li>
	 * </ul>
	 * </p>
	 * @param mode  the desired mode of operation
	 * @throws IOException  if the PTY could not be created
	 * @since 5.6
	 */
	public PTY(Mode mode) throws IOException {
		this.mode = mode;
		if (isConsole() && !isConsoleModeSupported) {
			throw new IOException(Messages.Util_exception_cannotCreatePty);
		}
		PTYInputStream inInit = null;
		PTYOutputStream outInit = null;
		String slaveInit = null;
		if (isConPTY != IS_CONPTY.CONPTY_NO) {
			try {
				conPTY = new ConPTY();
				isConPTY = IS_CONPTY.CONPTY_YES;
				slaveInit = "conpty"; //$NON-NLS-1$
				inInit = new ConPTYInputStream(conPTY);
				outInit = new ConPTYOutputStream(conPTY);
			} catch (NoClassDefFoundError e) {
				isConPTY = IS_CONPTY.CONPTY_NO;
				CNativePlugin.log(Messages.PTY_NoClassDefFoundError, e);
			} catch (Throwable e) {
				isConPTY = IS_CONPTY.CONPTY_NO;
				CNativePlugin.log(Messages.PTY_FailedToStartConPTY, e);
			}
		}

		// fall through in exception case as well as normal non-conPTY
		if (isConPTY == IS_CONPTY.CONPTY_NO) {

			slaveInit = hasPTY ? openMaster(isConsole()) : null;

			if (slaveInit == null) {
				throw new IOException(Messages.Util_exception_cannotCreatePty);
			}

			inInit = new PTYInputStream(new MasterFD());
			outInit = new PTYOutputStream(new MasterFD(), !isWinPTY);
		}
		slave = slaveInit;
		in = inInit;
		out = outInit;
	}

	/**
	 * Test whether the slave name can be used as a tty device by external processes (e.g. gdb).
	 * If the slave name is not valid an IOException is thrown.
	 * @throws IOException  if the slave name is not valid
	 * @since 5.6
	 */
	public void validateSlaveName() throws IOException {
		// on windows the slave name is just an internal identifier
		// and does not represent a real device
		if (isWinPTY) {
			throw new IOException("Slave name is not valid"); //$NON-NLS-1$
		}
	}

	public String getSlaveName() {
		return slave;
	}

	public MasterFD getMasterFD() {
		return new MasterFD();
	}

	/**
	 * @return whether this pseudo terminal is for use with the Eclipse console.
	 *
	 * @since 5.2
	 */
	public final boolean isConsole() {
		return mode == Mode.CONSOLE;
	}

	public PTYOutputStream getOutputStream() {
		return out;
	}

	public PTYInputStream getInputStream() {
		return in;
	}

	/**
	 * Change terminal window size to given width and height.
	 * <p>
	 * This should only be used when the pseudo terminal is configured
	 * for use with a terminal emulation, i.e. when {@link #isConsole()}
	 * returns <code>false</code>.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> This method may not be supported on all platforms.
	 * Known platforms which support this method are:
	 * <code>linux-x86</code>, <code>linux-x86_64</code>, <code>solaris-sparc</code>, <code>macosx</code>.
	 * </p>
	 *
	 * @since 5.2
	 */
	public final void setTerminalSize(int width, int height) {
		try {
			if (isConPTY == IS_CONPTY.CONPTY_YES) {
				conPTY.setTerminalSize(width, height);
			} else {
				change_window_size(master, width, height);
			}
		} catch (UnsatisfiedLinkError | IOException e) {
			if (!setTerminalSizeErrorAlreadyLogged) {
				setTerminalSizeErrorAlreadyLogged = true;
				CNativePlugin.log(Messages.Util_exception_cannotSetTerminalSize, e);
			}

		}
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.6
	 */
	public int exec_pty(Spawner spawner, String[] cmdarray, String[] envp, String dir, IChannel[] chan)
			throws IOException {
		if (isConPTY == IS_CONPTY.CONPTY_YES) {
			return conPTY.exec(cmdarray, envp, dir);
		} else if (isWinPTY) {
			return exec2(cmdarray, envp, dir, chan, slave, master, isConsole());
		} else {
			return spawner.exec2(cmdarray, envp, dir, chan, slave, master, isConsole());
		}
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.6
	 */
	public int waitFor(Spawner spawner, int pid) {
		if (isConPTY == IS_CONPTY.CONPTY_YES) {
			return conPTY.waitFor();
		} else if (isWinPTY) {
			return waitFor(master, pid);
		} else {
			return spawner.waitFor(pid);
		}
	}

	native String openMaster(boolean console);

	native int change_window_size(int fdm, int width, int height);

	/**
	 * Native method when executing with a terminal emulation (winpty only).
	 */
	native int exec2(String[] cmdarray, String[] envp, String dir, IChannel[] chan, String slaveName, int masterFD,
			boolean console) throws IOException;

	/**
	 * Native method to wait for process to terminate (winpty only).
	 */
	native int waitFor(int masterFD, int processID);

	static {
		try {
			boolean isWindows = Platform.OS_WIN32.equals(Platform.getOS());
			if (!isWindows) {
				isConPTY = IS_CONPTY.CONPTY_NO;
			}
			// Disable ConPTY if the user needs to
			boolean conPtyEnabled = Boolean
					.parseBoolean(System.getProperty("org.eclipse.cdt.core.conpty_enabled", "true")); //$NON-NLS-1$ //$NON-NLS-2$
			if (!conPtyEnabled) {
				isConPTY = IS_CONPTY.CONPTY_NO;
			}

			isWinPTY = isWindows;
			if (isWindows) {
				// When we used to build with VC++ we used DelayLoadDLLs (See Gerrit 167674 and Bug 521515) so that the winpty
				// could be found. When we ported to mingw we didn't port across this feature because it was simpler to just
				// manually load winpty first.
				System.loadLibrary("winpty"); //$NON-NLS-1$
			}
			System.loadLibrary("pty"); //$NON-NLS-1$
			hasPTY = true;
			// on windows console mode is not supported except for experimental use
			// to enable it, set system property org.eclipse.cdt.core.winpty_console_mode=true
			isConsoleModeSupported = !isWinPTY || Boolean.getBoolean("org.eclipse.cdt.core.winpty_console_mode"); //$NON-NLS-1$
		} catch (SecurityException | UnsatisfiedLinkError e) {
			CNativePlugin.log(
					"Failed to load the PTY library. This may indicate a configuration problem, but can be ignored if no further problems are observed.", //$NON-NLS-1$
					e);
		}
	}

}
