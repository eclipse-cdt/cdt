/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.Iterator;
import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;
import org.eclipse.cdt.debug.mi.core.command.MIExecArguments;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetEnvironment;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;

/**
 */
public class RuntimeOptions implements ICDIRuntimeOptions {

	Session session;
	
	public RuntimeOptions(Session s) {
		session = s;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions#setArguments(String)
	 */
	public void setArguments(String[] args) throws CDIException {
		if (args == null || args.length == 0) {
			return;
		}
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecArguments arguments =  factory.createMIExecArguments(args);
		try {
			mi.postCommand(arguments);
			MIInfo info = arguments.getMIInfo();
			if (info == null) {
				throw new CDIException("Unable to set arguments: target is not responding");
			}
		} catch (MIException e) {
			throw new CDIException("Unable to set arguments: " + e.getMessage());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions#setEnvironment(Properties)
	 */
	public void setEnvironment(Properties props) throws CDIException {
		if (props == null) {
			return;
		}
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		Iterator iterator = props.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String value = props.getProperty(key);
			String params[] = null;
			if (value == null || value.length() == 0) {
				params = new String[] {key}; 
			} else {
				params = new String[] {key, value}; 
			}
			MIGDBSetEnvironment set =  factory.createMIGDBSetEnvironment(params);
			try {
				mi.postCommand(set);
				MIInfo info = set.getMIInfo();
				if (info == null) {
					throw new CDIException("Unable to set environment: target is not responding");
				}
			} catch (MIException e) {
				throw new CDIException("Unable to set environment: " + e.getMessage());
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions#setWorkingDirectory(String)
	 */
	public void setWorkingDirectory(String wd) throws CDIException {
		if (wd == null || wd.length() == 0) {
			return;
		}
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIEnvironmentCD cd =  factory.createMIEnvironmentCD(wd);
		try {
			mi.postCommand(cd);
			MIInfo info = cd.getMIInfo();
			if (info == null) {
				throw new CDIException("Unable to set working directory: target is not responding");
			}
		} catch (MIException e) {
			throw new CDIException("Unable to set working directory: " + e.getMessage());
		}
	}

}
