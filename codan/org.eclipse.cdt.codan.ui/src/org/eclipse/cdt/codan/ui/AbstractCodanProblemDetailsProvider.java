/*******************************************************************************
 * $QNXLicenseC:
 * Copyright 2008, QNX Software Systems. All Rights Reserved.
 * 
 * You must obtain a written license from and pay applicable license fees to QNX 
 * Software Systems before you may reproduce, modify or distribute this software, 
 * or any work that includes all or part of this software.   Free development 
 * licenses are available for evaluation and non-commercial purposes.  For more 
 * information visit http://licensing.qnx.com or email licensing@qnx.com.
 *  
 * This file may contain contributions from others.  Please review this entire 
 * file for other proprietary rights or license notices, as well as the QNX 
 * Development Suite License Guide at http://licensing.qnx.com/license-guide/ 
 * for other information.
 * $
 *******************************************************************************/
/*
 * Created by: Elena Laskavaia
 * Created on: 2010-04-28
 * Last modified by: $Author: elaskavaia $
 */
package org.eclipse.cdt.codan.ui;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.core.resources.IMarker;

/**
 * Abstract class that provides stubs for problems details
 */
public abstract class AbstractCodanProblemDetailsProvider {
	protected IMarker marker;

	public AbstractCodanProblemDetailsProvider() {
	}

	/**
	 * sets the marker, called from framework to initialize provider
	 */
	public void setMarker(IMarker marker) {
		this.marker = marker;
	}

	public IMarker getMarker() {
		return marker;
	}

	protected String getProblemMessage() {
		String message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		return message;
	}

	protected String getProblemId() {
		String id = marker.getAttribute(IMarker.PROBLEM, (String) null); //$NON-NLS-1$
		return id;
	}

	/**
	 * return true if provider can provide details for given marker (previously set by setMarker)
	 * @param id - id of the problem
	 * @return 
	 */
	public abstract boolean isApplicable(String id);

	/**
	 * Return styled problem message. String can include <a> tags to which would be
	 * visible as hyperlinks and newline characters (\n). Default message if
	 * marker message plus location.
	 */
	public String getStyledProblemMessage() {
		String message = escapeForLink(getProblemMessage());
		String loc = marker.getResource().getFullPath().toOSString();
		String loc2 = marker.getAttribute(IMarker.LOCATION, ""); //$NON-NLS-1$
		if (loc2.length()>0)
			loc=loc2;
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
		return message + "\n" + loc + ":" + line; //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Return styled problem description. String can include <a> tags to which would be
	 * visible as hyperlinks and newline characters (\n)
	 */
	public String getStyledProblemDescription() {
		String id = getProblemId();
		if (id == null)
			return ""; //$NON-NLS-1$
		IProblem problem = CodanRuntime.getInstance().getChechersRegistry().getDefaultProfile().findProblem(id);
		return escapeForLink(problem.getDescription());
	}

	/**
	 * Method to escape characters which are interpreted by Link swt control,
	 * such as & (mnemonic)
	 */
	protected String escapeForLink(String text) {
		return text.replaceAll("&", "&&"); //$NON-NLS-2$
	}
}
