/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.util.Iterator;
import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;
import org.eclipse.cdt.debug.mi.core.command.MIExecArguments;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetEnvironment;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;

/**
 */
public class RuntimeOptions extends CObject implements ICDIRuntimeOptions {

	Target target;
	
	public RuntimeOptions(Target t) {
		super(t);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions#setArguments(String)
	 */
	public void setArguments(String[] args) throws CDIException {
		Target target = (Target)getTarget();
		if (args == null || args.length == 0) {
			return;
		}
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecArguments arguments =  factory.createMIExecArguments(args);
		try {
			mi.postCommand(arguments);
			MIInfo info = arguments.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.RuntimeOptions.Unable_to_set_args_target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new CDIException(CdiResources.getString("cdi.RuntimeOptions.Unable_to_set_args") + e.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions#setEnvironment(Properties)
	 */
	public void setEnvironment(Properties props) throws CDIException {
		Target target = (Target)getTarget();
		if (props == null) {
			return;
		}
		MISession mi = target.getMISession();
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
					throw new CDIException(CdiResources.getString("cdi.RuntimeOptions.Unable_to_set_args_target_not_responding")); //$NON-NLS-1$
				}
			} catch (MIException e) {
				throw new CDIException(CdiResources.getString("cdi.RuntimeOptions.Unable_to_set_environment") + e.getMessage()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions#setWorkingDirectory(String)
	 */
	public void setWorkingDirectory(String wd) throws CDIException {
		Target target = (Target)getTarget();
		if (wd == null || wd.length() == 0) {
			return;
		}
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIEnvironmentCD cd =  factory.createMIEnvironmentCD(wd);
		try {
			mi.postCommand(cd);
			MIInfo info = cd.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.RuntimeOptions.Unable_to_set_args_target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new CDIException(CdiResources.getString("cdi.RuntimeOptions.Unable_to_set_working_dir") + e.getMessage()); //$NON-NLS-1$
		}
	}

}
