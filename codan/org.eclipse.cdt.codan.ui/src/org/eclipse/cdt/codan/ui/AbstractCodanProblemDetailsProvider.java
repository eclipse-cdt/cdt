/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.core.resources.IMarker;

/**
 * Abstract class that provides stubs for problems details.
 * This class intended to be extended by the users of codanProblemDetails extension point.
 * One instance of this class would exists at runtime. To query for results, framework 
 * would synchronize on this class object, set setMarker then call other getStyled* methods
 * to obtain data.
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

	/**
	 * Get marker associated with this provider
	 * @return
	 */
	public IMarker getMarker() {
		return marker;
	}

	/**
	 * Convenience method to return marker message
	 * @return
	 */
	protected String getProblemMessage() {
		String message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		return message;
	}

	/**
	 * Convenience method to return codan problem id
	 * @return
	 */
	protected String getProblemId() {
		String id = marker.getAttribute(IMarker.PROBLEM, (String) null);
		return id;
	}

	/**
	 * return true if provider can provide details for given marker (previously set by setMarker)
	 * @param id - id of the problem
	 * @return true if details are available for given marker
	 */
	public abstract boolean isApplicable(String id);

	/**
	 * Return styled problem message. This text would be used in Link widget.
	 * String can include <a> tags to which would be
	 * visible as hyperlinks and newline characters (\n). Default message if
	 * marker message plus location. Ampersand (&) should be escape because
	 * it is interpreted as mnemonic for control navigation (can use espaceForLink method). <br>
	 * This method intended to be overriden by the client.
	 */
	public String getStyledProblemMessage() {
		String message = escapeForLink(getProblemMessage());
		String href = getLocationHRef();
		String link = href.replaceFirst("^file:", ""); //$NON-NLS-1$ //$NON-NLS-2$
		link = link.replaceFirst("#(\\d+)$", ":$1");  //$NON-NLS-1$//$NON-NLS-2$
		return "<a href=\"" + href + "\">" + link + "</a>\n" + message; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
	protected String getLocationHRef() {
		return CodanEditorUtility.getLocationHRef(marker);
	}
	/**
	 * Return styled problem description. This text would be used in Link widget.
	 * String can include <a> tags to which would be
	 * visible as hyperlinks and newline characters (\n).
	 * Ampersand (&) should be escape because
	 * it is interpreted as mnemonic for control navigation (can use espaceForLink method).
	 * 
	 * Default implementation return desciption of codan problem. <br>
	 * This method intended to be overriden by the client.
	 * 
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
		return text.replaceAll("&", "&&"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
