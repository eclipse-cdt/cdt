/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIBreakInsert;
import org.eclipse.cdt.debug.mi.core.command.MITargetAttach;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;

/**
 * GDB/MI Plugin.
 */
public class MIPlugin extends Plugin {

	//The shared instance.
	private static MIPlugin plugin;

	/**
	 * The constructor.
	 */
	public MIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the singleton.
	 */
	public static MIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Create a MI Session.
	 */
	public MISession createMISession(InputStream in, OutputStream out) {
		return new MISession(in, out);
	}

	public ICDISession createCSession(String program) throws IOException {
		String[]args = new String[]{"gdb", "-q", "-i", "mi", program};
		Process gdb = Runtime.getRuntime().exec(args);
		MISession session = createMISession(gdb.getInputStream(), gdb.getOutputStream());
		/*
		try {
			CommandFactory factory = session.getCommandFactory();
			MIBreakInsert bkpt= factory.createMIBreakInsert(true, false, null, 0, "routine");
			session.postCommand(bkpt);
			MIInfo info = bkpt.getMIInfo();
			if (info == null) {
				throw new IOException("Timedout");
			}
		} catch (MIException e) {
			throw new IOException("Failed to attach");
		}
		*/
		return new CSession(session);
	}

	public ICDISession createCSession(String program, String core) throws IOException {
		String[]args = new String[]{"gdb", "--quiet", "-i", "mi", program, core};
		Process gdb = Runtime.getRuntime().exec(args);
		MISession session = createMISession(gdb.getInputStream(), gdb.getOutputStream());
		return new CSession(session);
	}

	public ICDISession createCSession(String program, int pid) throws IOException {
		String[]args = new String[]{"gdb", "--quiet", "-i", "mi", program};
		Process gdb = Runtime.getRuntime().exec(args);
		MISession session = createMISession(gdb.getInputStream(), gdb.getOutputStream());
		try {
			CommandFactory factory = session.getCommandFactory();
			MITargetAttach attach = factory.createMITargetAttach(pid);
			session.postCommand(attach);
			MIInfo info = attach.getMIInfo();
			if (info == null) {
				throw new IOException("Timedout");
			}
		} catch (MIException e) {
			throw new IOException("Failed to attach");
		}
		return new CSession(session);
	}
	
	public static void debugLog(String message) {
	//	if ( getDefault().isDebugging() ) {
	//		getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
			if (message.endsWith("\n")) {
				System.err.print(message);
			} else {
				System.err.println(message);
			}
	//	}
	}
}
