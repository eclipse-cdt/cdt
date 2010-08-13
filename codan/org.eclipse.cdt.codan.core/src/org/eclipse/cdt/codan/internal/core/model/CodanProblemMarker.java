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
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
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

	public Object[] getArgs() {
		return args;
	}

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
		marker.setAttribute(ID, problem.getId());
		marker.setAttribute(IMarker.CHAR_END, loc.getEndingChar());
		marker.setAttribute(IMarker.CHAR_START, loc.getStartingChar());
		marker.setAttribute("org.eclipse.cdt.core.problem", 42); //$NON-NLS-1$
		String propArgs = serializeArgs(args);
		marker.setAttribute(PROBLEM_ARGS, propArgs);
		IProblemCategory[] cats = CodanProblemCategory.findProblemCategories(
				getProfile(file).getRoot(), problem.getId());
		String cat = cats.length > 0 ? cats[0].getId() : ""; //$NON-NLS-1$
		marker.setAttribute(CATEGORY, cat);
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
	private static String serializeArgs(Object[] args) {
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
			return (String) marker.getAttribute(ICodanProblemMarker.ID);
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * @param marker
	 * @return problem message
	 */
	public static String getMessage(IMarker marker) {
		return marker.getAttribute(IMarker.MESSAGE, (String) null);
	}

	/**
	 * @param marker
	 * @return codan severity
	 */
	public static CodanSeverity getSeverity(IMarker marker) {
		int sev = marker.getAttribute(IMarker.SEVERITY, 0);
		return CodanSeverity.valueOf(sev);
	}

	/**
	 * Attempt to restore CodamProblemMaker from the resource marker
	 * 
	 * @param marker
	 * @return new instanceof of ICodanProblemMarker or null if marker is not
	 *         codan marker
	 */
	public static ICodanProblemMarker createCodanProblemMarkerFromResourceMarker(
			IMarker marker) {
		CodanProblem problem = getProblem(marker);
		if (problem == null)
			return null;
		CodanProblemLocation loc = getLocation(marker);
		return new CodanProblemMarker(problem, loc, getProblemArguments(marker));
	}

	/**
	 * @param marker
	 * @return
	 */
	public static CodanProblem getProblem(IMarker marker) {
		String id = getProblemId(marker);
		if (id == null)
			return null;
		IResource resource = marker.getResource();
		IProblemProfile profile = getProfile(resource);
		CodanProblem problem = (CodanProblem) ((CodanProblem) profile.findProblem(id)).clone();
		CodanSeverity sev = getSeverity(marker);
		problem.setSeverity(sev);
		return problem;
	}

	/**
	 * @param resource
	 * @return
	 */
	public static IProblemProfile getProfile(IResource resource) {
		IProblemProfile profile = CheckersRegistry.getInstance()
				.getResourceProfile(resource);
		return profile;
	}

	/**
	 * @param marker
	 * @return location object using marker attributes
	 */
	public static CodanProblemLocation getLocation(IMarker marker) {
		int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
		int charend = marker.getAttribute(IMarker.CHAR_END, -1);
		int charstart = marker.getAttribute(IMarker.CHAR_START, -1);
		CodanProblemLocation loc = new CodanProblemLocation(
				marker.getResource(), charstart, charend, line);
		return loc;
	}

	/**
	 * @param marker
	 * @param res
	 * @throws CoreException
	 */
	public static void setProblemArguments(IMarker marker, String[] args)
			throws CoreException {
		String propArgs = serializeArgs(args);
		marker.setAttribute(PROBLEM_ARGS, propArgs);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(args);
		result = prime * result + ((loc == null) ? 0 : loc.hashCode());
		result = prime * result + problem.getId().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ICodanProblemMarker))
			return false;
		CodanProblemMarker other = (CodanProblemMarker) obj;
		if (!Arrays.equals(args, other.args))
			return false;
		if (!loc.equals(other.loc))
			return false;
		if (!problem.getId().equals(other.problem.getId()))
			return false;
		return true;
	}
}
