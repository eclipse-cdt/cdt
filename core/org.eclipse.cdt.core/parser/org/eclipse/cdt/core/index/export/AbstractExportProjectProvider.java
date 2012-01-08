/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index.export;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.export.CLIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;


/**
 * An IExportProjectProvider implementation intended to be sub-classed by clients. It
 * provides convenience methods for obtaining options and their parameters from the
 * command-line.
 * 
 * @see ExternalExportProjectProvider for usage scenarios
 */
public abstract class AbstractExportProjectProvider implements IExportProjectProvider {
	public static final IProgressMonitor NPM= new NullProgressMonitor();
	
	private Map<String, List<String>> arguments;
	private String[] appArguments;
	
	public AbstractExportProjectProvider() {}
	
	/**
	 * @return the application arguments
	 */
	protected String[] getApplicationArguments() {
		return appArguments.clone();
	}
	
	/*
	 * @see org.eclipse.cdt.core.index.export.IExportProjectProvider#setApplicationArguments(java.lang.String[])
	 */
	@Override
	public void setApplicationArguments(String[] arguments) {
		this.appArguments= arguments.clone();
		this.arguments= Collections.unmodifiableMap(CLIUtil.parseToMap(arguments));
	}

	/**
	 * Returns a mapping from string option to parameter string list
	 * <br>
	 * For example, if -option p1 p2 p3 appears on the command line, then
	 * the mapping option=>[p1,p2,p3] will be present in the map
	 * @return a mapping from string option to parameter string list
	 */
	protected Map<String,List<String>> getParsedArgs() {
		return arguments;
	}
	
	/**
	 * Gets an option's single parameter, or throws a CoreException should the option
	 * not be present, or if it does not have exactly one parameter
	 * @param option
	 * @return an option's single parameter
	 * @throws CoreException should the specified option
	 * not be present, or if it does not have exactly one parameter
	 */
	public String getSingleString(String option) throws CoreException {
		return CLIUtil.getArg(arguments, option, 1).get(0);
	}
	
	/**
	 * @param option
	 * @return the list of parameters given with this option
	 */
	public List<String> getParameters(String option) {
		return arguments.get(option); 
	}
	
	/**
	 * Returns whether the specified option appears in the application arguments
	 * @param option the option to check for
	 * @return whether the specified option appears in the application arguments
	 */
	public boolean isPresent(String option) {
		return arguments.containsKey(option);
	}
	
	/**
	 * Returns a list of strings representing the parameters to the specified option. If the number
	 * of parameters does not match the expected number, an command-line error message is shown to the
	 * user.
	 * @param option
	 * @param expected the number of parameters expected
	 * @throws CoreException
	 */
	public List<String> getParameters(String option, int expected) throws CoreException {
		return CLIUtil.getArg(arguments, option, expected);
	}
	
	/**
	 * Produces an error in the application
	 * @param message an error message suitable for the user
	 * @return does not return
	 * @throws CoreException Throws a CoreException with an ERROR status
	 */
	public IStatus fail(String message) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, message));
	}
}
