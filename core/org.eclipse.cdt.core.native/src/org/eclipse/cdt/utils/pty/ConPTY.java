/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.utils.pty;

import static com.sun.jna.platform.win32.WinBase.CREATE_UNICODE_ENVIRONMENT;
import static com.sun.jna.platform.win32.WinBase.EXTENDED_STARTUPINFO_PRESENT;
import static com.sun.jna.platform.win32.WinBase.INFINITE;
import static com.sun.jna.platform.win32.WinBase.STARTF_USESTDHANDLES;
import static com.sun.jna.platform.win32.WinBase.WAIT_OBJECT_0;
import static com.sun.jna.platform.win32.WinError.S_OK;
import static com.sun.jna.platform.win32.WinNT.PROCESS_QUERY_INFORMATION;
import static com.sun.jna.platform.win32.WinNT.SYNCHRONIZE;
import static org.eclipse.cdt.utils.pty.ConPTYKernel32.PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.cdt.utils.WindowsArgumentQuoter;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.PVOID;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.IntByReference;

/**
 * A JNA implementation for ConPTY to provide a Windows native (as opposed to WinPTY)
 * implementation of a PTY.
 *
 * This class should be accessed/created via the PTY class which will use ConPTY when it
 * is available.
 *
 * @noreference This class is not intended to be referenced by clients.
 */
public class ConPTY {

	private Handles handles = new Handles();

	/**
	 * The handles that need to be closed when the PTY is done
	 */
	private static class Handles {
		private HANDLEByReference pseudoConsole;
		private ConPTYKernel32.STARTUPINFOEX startupInfo;
		private Memory threadAttributeListMemory;
		private WinBase.PROCESS_INFORMATION processInformation;
		private HANDLEByReference pipeOut;
		private HANDLEByReference pipeIn;

		/** Saved for convenience to make it easier to identify/find the process in process explorer */
		public int pid;
	}

	/**
	 * Create a new Windows Pseudo Console (ConPTY) that an application can be attached to.
	 */
	public ConPTY() throws IOException {
		handles.pseudoConsole = new HANDLEByReference();
		handles.pipeIn = new HANDLEByReference();
		handles.pipeOut = new HANDLEByReference();

		var phPipePTYIn = new WinNT.HANDLEByReference();
		var phPipePTYOut = new WinNT.HANDLEByReference();

		boolean res;
		res = ConPTYKernel32.INSTANCE.CreatePipe(phPipePTYIn, handles.pipeOut, null, 0);
		checkErr(res, "CreatePipe"); //$NON-NLS-1$

		res = ConPTYKernel32.INSTANCE.CreatePipe(handles.pipeIn, phPipePTYOut, null, 0);
		checkErr(res, "CreatePipe"); //$NON-NLS-1$

		// The console will be resized later with ResizePseudoConsole, start with the old classic size!
		var consoleSize = new ConPTYKernel32.COORD_ByValue();
		consoleSize.X = (short) 80;
		consoleSize.Y = (short) 24;

		var hr = ConPTYKernel32.INSTANCE.CreatePseudoConsole(consoleSize, phPipePTYIn.getValue(),
				phPipePTYOut.getValue(), new WinDef.DWORD(0), handles.pseudoConsole);
		checkErr(hr, "CreatePseudoConsole"); //$NON-NLS-1$

		res = ConPTYKernel32.INSTANCE.CloseHandle(phPipePTYOut.getValue());
		checkErr(res, "CloseHandle"); //$NON-NLS-1$
		res = ConPTYKernel32.INSTANCE.CloseHandle(phPipePTYIn.getValue());
		checkErr(res, "CloseHandle"); //$NON-NLS-1$
	}

	/**
	 * Executes the given process in the PTY
	 *
	 * @param cmdarray Command and arguments that will be quotes using standard Windows rules to make a
	 * command line. See {@link WindowsArgumentQuoter}
	 * @param envp
	 * @param dir
	 * @return the PID
	 * @throws IOException
	 */
	public int exec(String[] cmdarray, String[] envp, String dir) throws IOException {
		String quoted = WindowsArgumentQuoter.quoteArgv(cmdarray, false);
		handles.startupInfo = new ConPTYKernel32.STARTUPINFOEX();
		handles.threadAttributeListMemory = PrepareStartupInformation(handles.startupInfo, handles.pseudoConsole);
		handles.processInformation = new PROCESS_INFORMATION();

		var status = ConPTYKernel32.INSTANCE.CreateProcess(null, quoted, null, null, false,
				new DWORD(CREATE_UNICODE_ENVIRONMENT | EXTENDED_STARTUPINFO_PRESENT), toByteArray(envp), dir,
				handles.startupInfo, handles.processInformation);
		checkErr(status, "CreateProcess"); //$NON-NLS-1$
		return getPID();
	}

	/**
	 * Convert envp to a byte array, encoding UTF_16LE. Remember to pass CREATE_UNICODE_ENVIRONMENT
	 * to CreateProcess
	 */
	public static Memory toByteArray(String[] envp) throws IOException {
		if (envp == null) {
			return null;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (String string : envp) {
			bos.write(string.getBytes(StandardCharsets.UTF_16LE));
			// Terminate each variable with two zero bytes
			bos.write(0);
			bos.write(0);
		}
		// Terminate the whole block with two additional zero bytes
		bos.write(0);
		bos.write(0);
		byte[] byteArray = bos.toByteArray();
		Memory memory = new Memory(byteArray.length);
		memory.write(0, byteArray, 0, byteArray.length);
		return memory;

	}

	public int getPID() {
		handles.pid = handles.processInformation.dwProcessId.intValue();
		return handles.pid;
	}

	private static Memory PrepareStartupInformation(ConPTYKernel32.STARTUPINFOEX pStartupInfo, HANDLEByReference phPC)
			throws IOException {
		pStartupInfo.StartupInfo.cb = new DWORD(pStartupInfo.size());

		pStartupInfo.StartupInfo.hStdOutput = new HANDLE();
		pStartupInfo.StartupInfo.hStdError = new HANDLE();
		pStartupInfo.StartupInfo.hStdInput = new HANDLE();
		pStartupInfo.StartupInfo.dwFlags = STARTF_USESTDHANDLES;

		boolean res;

		var attrListSize = new ConPTYKernel32.SIZE_TByReference();
		res = ConPTYKernel32.INSTANCE.InitializeProcThreadAttributeList(Pointer.NULL, new DWORD(1), new DWORD(0),
				attrListSize);
		Kernel32.INSTANCE.SetLastError(0);
		var memory = new Memory(attrListSize.getValue().longValue());

		res = ConPTYKernel32.INSTANCE.InitializeProcThreadAttributeList(memory, new DWORD(1), new DWORD(0),
				attrListSize);
		checkErr(res, "InitializeProcThreadAttributeList"); //$NON-NLS-1$

		var dwPROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE = new DWORD_PTR(PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE);

		res = ConPTYKernel32.INSTANCE.UpdateProcThreadAttribute(memory, new DWORD(0),
				dwPROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE, new PVOID(phPC.getValue().getPointer()),
				new SIZE_T(Native.POINTER_SIZE), null, null);
		checkErr(res, "UpdateProcThreadAttribute"); //$NON-NLS-1$

		pStartupInfo.lpAttributeList = memory.share(0);
		return memory;
	}

	/**
	 * Implements the contract of {@link Process#waitFor()}. This is used by {@link PTY#waitFor(org.eclipse.cdt.utils.spawner.Spawner, int)},
	 * but in the Spawner case the PID is passed around unnecessarily. This method therefore waits for the process it created only,
	 * like how Process#waitFor() behaves.
	 *
	 * @see Process#waitFor()
	 * @see PTY#waitFor(org.eclipse.cdt.utils.spawner.Spawner, int)
	 */
	public int waitFor() {
		try {
			int what = 0;
			HANDLE hProc;

			hProc = Kernel32.INSTANCE.OpenProcess(SYNCHRONIZE | PROCESS_QUERY_INFORMATION, false, getPID());
			checkErr(hProc, "OpenProcess"); //$NON-NLS-1$

			what = Kernel32.INSTANCE.WaitForSingleObject(hProc, INFINITE);

			IntByReference exit_code = new IntByReference(0);
			if (what == WAIT_OBJECT_0) {
				Kernel32.INSTANCE.GetExitCodeProcess(hProc, exit_code);
			}

			boolean closeHandle = Kernel32.INSTANCE.CloseHandle(hProc);
			checkErr(closeHandle, "CloseHandle"); //$NON-NLS-1$
			return exit_code.getValue();
		} catch (IOException e) {
			// Returning -1 is the equivalent of what was done
			// in error handling in the JNI versions of waitFor
			return -1;
		}
	}

	/**
	 * Closes the entire PTY session. This will have the side effect of closing all the IO
	 * channels at once. The process will also be terminated when the console is closed if
	 * it isn't closed already. If the console's host (conhost) is closed then the process
	 * won't be automatically terminated. This happens if conhost crashes and the behaviour
	 * with winpty is the same in that case.
	 */
	public synchronized void close() throws IOException {
		if (handles == null) {
			return;
		}
		boolean res;

		res = ConPTYKernel32.INSTANCE.CloseHandle(handles.processInformation.hThread);
		checkErr(res, "CloseHandle processInformation.hThread"); //$NON-NLS-1$

		res = ConPTYKernel32.INSTANCE.CloseHandle(handles.processInformation.hProcess);
		checkErr(res, "CloseHandle processInformation.hProcess"); //$NON-NLS-1$

		ConPTYKernel32.INSTANCE.DeleteProcThreadAttributeList(handles.startupInfo.lpAttributeList);
		handles.threadAttributeListMemory.clear();

		ConPTYKernel32.INSTANCE.ClosePseudoConsole(handles.pseudoConsole.getValue());

		res = ConPTYKernel32.INSTANCE.CancelIoEx(handles.pipeIn.getValue(), Pointer.NULL);
		int err = Native.getLastError();
		if (err != WinError.ERROR_NOT_FOUND) {
			checkErr(res, "CancelIoEx"); //$NON-NLS-1$
		}

		res = ConPTYKernel32.INSTANCE.CloseHandle(handles.pipeOut.getValue());
		checkErr(res, "CloseHandle pipeOut"); //$NON-NLS-1$

		res = ConPTYKernel32.INSTANCE.CloseHandle(handles.pipeIn.getValue());
		checkErr(res, "CloseHandle pipeIn"); //$NON-NLS-1$

		handles = null;
	}

	/**
	 * Implements contract of {@link InputStream#read(byte[])}
	 * @see InputStream#read(byte[])
	 */
	public int read(byte[] buf) throws IOException {
		if (handles == null) {
			throw new IOException("ConPTY is closed."); //$NON-NLS-1$
		}

		var pipe = handles.pipeIn;

		IntByReference dwBytesRead = new IntByReference(0);
		boolean fRead = false;

		fRead = Kernel32.INSTANCE.ReadFile(pipe.getValue(), buf, buf.length, dwBytesRead, null);
		checkErr(fRead, "ReadFile"); //$NON-NLS-1$
		int value = dwBytesRead.getValue();
		if (value == 0) {
			// We are at EOF because we are doing Synchronous and non-overlapped operation
			// Implementation note: I don't know how to get this with terminal programs, so
			// I have not seen this happen in development.
			return -1;
		}
		return value;
	}

	/**
	 * Implements the contract of {@link OutputStream#write(byte[])}
	 * @see OutputStream#write(byte[])
	 */
	public void write(byte[] buf) throws IOException {
		if (handles == null) {
			throw new IOException("ConPTY is closed."); //$NON-NLS-1$
		}
		IntByReference dwBytesWritten = new IntByReference(0);
		boolean fWritten = false;
		fWritten = Kernel32.INSTANCE.WriteFile(handles.pipeOut.getValue(), buf, buf.length, dwBytesWritten, null);
		checkErr(fWritten, "WriteFile"); //$NON-NLS-1$
	}

	/**
	 * Implements the contract of {@link PTY#setTerminalSize(int, int)}, but throws exceptions
	 * that PTY logs.
	 * @see PTY#setTerminalSize(int, int)
	 */
	public void setTerminalSize(int width, int height) throws IOException {
		if (handles == null) {
			throw new IOException("ConPTY is closed."); //$NON-NLS-1$
		}

		var consoleSize = new ConPTYKernel32.COORD_ByValue();
		consoleSize.X = (short) width;
		consoleSize.Y = (short) height;

		HRESULT result = ConPTYKernel32.INSTANCE.ResizePseudoConsole(handles.pseudoConsole.getValue(), consoleSize);
		checkErr(result, "ResizePseudoConsole"); //$NON-NLS-1$

	}

	/**
	 * Throw an IOException if hr is not S_OK.
	 */
	private static void checkErr(WinNT.HRESULT hr, String method) throws IOException {
		if (!S_OK.equals(hr)) {
			String msg = Kernel32Util.getLastErrorMessage();
			throw new IOException(String.format("%s: %s", method, msg)); //$NON-NLS-1$
		}
	}

	/**
	 * Throw an IOException if status is false.
	 */
	private static void checkErr(boolean status, String method) throws IOException {
		if (!status) {
			int lastError = Native.getLastError();
			String msg = Kernel32Util.formatMessage(lastError);
			throw new IOException(String.format("%s: %s: %s", method, lastError, msg)); //$NON-NLS-1$
		}
	}

	/**
	 * Throw an IOException if handle is null.
	 */
	private static void checkErr(HANDLE handle, String method) throws IOException {
		if (handle == null) {
			String msg = Kernel32Util.getLastErrorMessage();
			throw new IOException(String.format("%s: %s", method, msg)); //$NON-NLS-1$
		}
	}
}
