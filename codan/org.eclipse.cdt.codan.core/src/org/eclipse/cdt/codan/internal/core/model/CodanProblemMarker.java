/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Instance of a problem. Intermediate representation before problem become a
 * marker
 * 
 * @since 1.1
 */
public class CodanProblemMarker implements ICodanProblemMarker {
	private static final String PROBLEM_ARGS = "args"; //$NON-NLS-1$
	private IProblemLocation loc;
	private IProblem problem;
	private Object args[];

	/**
	 * @param problem
	 * @param loc
	 * @param args
	 */
	public CodanProblemMarker(IProblem problem, IProblemLocation loc,
			Object[] args) {
		this.problem = problem;
		this.loc = loc;
		this.args = args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.ICodanProblemMarker#getLocation()
	 */
	public IProblemLocation getLocation() {
		return loc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.ICodanProblemMarker#getProblem()
	 */
	public IProblem getProblem() {
		return problem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.ICodanProblemMarker#getResource()
	 */
	public IResource getResource() {
		return loc.getFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.ICodanProblemMarker#createMarker()
	 */
	public IMarker createMarker() throws CoreException {
		IResource file = loc.getFile();
		int lineNumber = loc.getLineNumber();
		int severity = problem.getSeverity().intValue();
		String message = createMessage();
		IMarker marker = file.createMarker(problem.getMarkerType());
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		marker.setAttribute(IMarker.PROBLEM, problem.getId());
		marker.setAttribute(IMarker.CHAR_END, loc.getEndingChar());
		marker.setAttribute(IMarker.CHAR_START, loc.getStartingChar());
		marker.setAttribute("org.eclipse.cdt.core.problem", 42); //$NON-NLS-1$
		String propArgs = serializeArgs(args);
		marker.setAttribute(PROBLEM_ARGS, propArgs);
		return marker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.ICodanProblemMarker#createMessage()
	 */
	public String createMessage() {
		String messagePattern = problem.getMessagePattern();
		String message = problem.getId();
		if (messagePattern == null) {
			if (args != null && args.length > 0 && args[0] instanceof String)
				message = (String) args[0];
		} else {
			message = MessageFormat.format(messagePattern, args);
		}
		return message;
	}

	/**
	 * @param args2
	 * @return
	 */
	private String serializeArgs(Object[] args) {
		if (args != null) {
			Properties prop = new Properties();
			prop.put("len", String.valueOf(args.length)); //$NON-NLS-1$
			for (int i = 0; i < args.length; i++) {
				Object object = args[i];
				if (object != null)
					prop.put("a" + i, object.toString()); //$NON-NLS-1$
			}
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try {
				prop.store(bout, null);
			} catch (IOException e) {
				// nope
			}
			return bout.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Return the argument of a problem that checker passed to "reportProblem"
	 * method
	 * 
	 * @param marker - problem marker
	 * @param index - index of the argument 0 based
	 * @return problem argument at index, can be null if not set. Can throw AUBE
	 *         if out of bounds.
	 */
	public static String getProblemArgument(IMarker marker, int index) {
		String[] args = getProblemArguments(marker);
		return args[index];
	}

	/**
	 * Return the arguments of a problem that checker passed to "reportProblem"
	 * method
	 * 
	 * @param marker - problem marker
	 * @return problem arguments, can not be null. Can be 0 sized array.
	 */
	public static String[] getProblemArguments(IMarker marker) {
		String attrs = marker.getAttribute(PROBLEM_ARGS, ""); //$NON-NLS-1$
		Properties prop = new Properties();
		ByteArrayInputStream bin = new ByteArrayInputStream(attrs.getBytes());
		try {
			prop.load(bin);
		} catch (IOException e) {
			// not happening
		}
		String len = prop.getProperty("len", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		int length = Integer.valueOf(len);
		String args[] = new String[length];
		for (int i = 0; i < length; i++) {
			args[i] = prop.getProperty("a" + i); //$NON-NLS-1$
		}
		return args;
	}

	/**
	 * Return problemId from marker
	 * 
	 * @param marker
	 * @return codan problemId
	 */
	public static String getProblemId(IMarker marker) {
		try {
			return (String) marker.getAttribute(IMarker.PROBLEM);
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * @param marker
	 * @return problem message
	 */
	public static String getMessage(IMarker marker) {
		try {
			return (String) marker.getAttribute(IMarker.MESSAGE);
		} catch (CoreException e) {
			return null;
		}
	}
}
