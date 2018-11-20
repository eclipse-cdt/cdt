/*******************************************************************************
 * Copyright (c) 2002, 2015 QNX Software Systems and others.
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

	final boolean console;
	final String slave;
	final PTYInputStream in;
	final PTYOutputStream out;

	/**
	 * NOTE: Field is accessed by the native layer. Do not refactor!
	 */
	int master;

	private static boolean hasPTY;
	private static boolean isWinPTY;
	private static boolean isConsoleModeSupported;
	private static boolean setTerminalSizeErrorAlreadyLogged;

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
	 * Identical to {@link PTY#PTY(boolean) PTY(Mode.CONSOLE)}.
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
		this(mode == Mode.CONSOLE);
	}

	/**
	 * Create pseudo terminal.
	 *
	 * <p>
	 * The provided flag indicates whether the pseudo terminal is used with the interactive
	 * Eclipse console:
	 * <ul>
	 * <li>If <code>true</code> the terminal is configured with no echo and stderr is
	 * redirected to a pipe instead of the PTY. This mode is not supported on windows</li>
	 * <li>If <code>false</code> the terminal is configured with echo and stderr is
	 * connected to the PTY. This mode is best suited for use with a proper terminal emulation.
	 * Note that this mode might not be supported on all platforms.
	 * Known platforms which support this mode are:
	 * <code>linux-x86</code>, <code>linux-x86_64</code>, <code>solaris-sparc</code>, <code>macosx</code>.
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param console  whether terminal is used with Eclipse console
	 * @throws IOException  if the PTY could not be created
	 * @deprecated Use {@link #PTY(Mode)} instead
	 * @since 5.2
	 */
	@Deprecated
	public PTY(boolean console) throws IOException {
		this.console = console;
		if (console && !isConsoleModeSupported) {
			throw new IOException(Messages.Util_exception_cannotCreatePty);
		}
		slave = hasPTY ? openMaster(console) : null;

		if (slave == null) {
			throw new IOException(Messages.Util_exception_cannotCreatePty);
		}

		in = new PTYInputStream(new MasterFD());
		out = new PTYOutputStream(new MasterFD(), !isWinPTY);
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
		if (isWinPTY)
			throw new IOException("Slave name is not valid"); //$NON-NLS-1$
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
		return console;
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
			change_window_size(master, width, height);
		} catch (UnsatisfiedLinkError ule) {
			if (!setTerminalSizeErrorAlreadyLogged) {
				setTerminalSizeErrorAlreadyLogged = true;
				CNativePlugin.log(Messages.Util_exception_cannotSetTerminalSize, ule);
			}
		}
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.6
	 */
	public int exec_pty(Spawner spawner, String[] cmdarray, String[] envp, String dir, int[] chan) throws IOException {
		if (isWinPTY) {
			return exec2(cmdarray, envp, dir, chan, slave, master, console);
		} else {
			return spawner.exec2(cmdarray, envp, dir, chan, slave, master, console);
		}
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.6
	 */
	public int waitFor(Spawner spawner, int pid) {
		if (isWinPTY) {
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
	native int exec2(String[] cmdarray, String[] envp, String dir, int[] chan, String slaveName, int masterFD,
			boolean console) throws IOException;

	/**
	 * Native method to wait for process to terminate (winpty only).
	 */
	native int waitFor(int masterFD, int processID);

	static {
		try {
			System.loadLibrary("pty"); //$NON-NLS-1$
			hasPTY = true;
			isWinPTY = Platform.OS_WIN32.equals(Platform.getOS());
			// on windows console mode is not supported except for experimental use
			// to enable it, set system property org.eclipse.cdt.core.winpty_console_mode=true
			isConsoleModeSupported = !isWinPTY || Boolean.getBoolean("org.eclipse.cdt.core.winpty_console_mode"); //$NON-NLS-1$
		} catch (SecurityException e) {
			// Comment out it worries the users too much
			//CCorePlugin.log(e);
		} catch (UnsatisfiedLinkError e) {
			// Comment out it worries the users too much
			//CCorePlugin.log(e);
		}
	}

}
