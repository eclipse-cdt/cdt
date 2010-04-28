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
 * Last modified by: $Author$
 */
package org.eclipse.cdt.codan.ui;

import java.net.URL;

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
	
	protected String getProblemMessage(){
		String message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		return message;
	}

	/**
	 * return true if provider can provide details for given marker (previously set by setMarker)
	 * @param id - id of the problem
	 * @return 
	 */
	public abstract boolean isApplicable(String id);

	/**
	 * URL to external help for the problem, would be displayed as label, and as action
	 * will go to given URL is not null. If label is null (getHelpLabel) URL would be used as label.
	 */
	public URL getHelpURL() {
		return null;
	}

	/**
	 * Label text to use to navigate to a help. Would be shown as hyperlink. If helpURL is not
	 * null would open a browser with given URL.
	 */
	public String getHelpLabel() {
		return null;
	}

	/**
	 * Return help context id. Only one getHelpURL or getHelpContextId can be used.
	 * In case if help context id is not null hyperlink would open context help page.
	 */
	public String getHelpContextId() {
		return null;
	}
}
