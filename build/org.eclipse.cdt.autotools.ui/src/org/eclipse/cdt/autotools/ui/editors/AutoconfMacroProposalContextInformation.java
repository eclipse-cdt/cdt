/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on May 6, 2004
 */
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.swt.graphics.Image;

/**
 * @author aniefer
 */
public class AutoconfMacroProposalContextInformation implements IContextInformation, IContextInformationExtension {
	/** The name of the context */
	private String fContextDisplayString;
	/** The information to be displayed */
	private String fInformationDisplayString;
	/** The position to display the information */
	private int fInformationPosition;
	/** The image to be displayed */
	private Image fImage;

	/**
	 * Creates a new context information without an image.
	 *
	 * @param contextDisplayString the string to be used when presenting the context
	 * @param informationDisplayString the string to be displayed when presenting the context information
	 */
	public AutoconfMacroProposalContextInformation(String contextDisplayString, String informationDisplayString) {
		this(null, contextDisplayString, informationDisplayString);
	}

	/**
	 * Creates a new context information with an image.
	 *
	 * @param image the image to display when presenting the context information
	 * @param contextDisplayString the string to be used when presenting the context
	 * @param informationDisplayString the string to be displayed when presenting the context information,
	 *		may not be <code>null</code>
	 */
	public AutoconfMacroProposalContextInformation(Image image, String contextDisplayString,
			String informationDisplayString) {
		//Assert.isNotNull(informationDisplayString);
		fImage = image;
		fContextDisplayString = contextDisplayString;
		fInformationDisplayString = informationDisplayString;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof IContextInformation) {
			IContextInformation contextInformation = (IContextInformation) object;
			boolean equals = fInformationDisplayString
					.equalsIgnoreCase(contextInformation.getInformationDisplayString());
			if (fContextDisplayString != null)
				equals = equals && fContextDisplayString.equalsIgnoreCase(contextInformation.getContextDisplayString());
			return equals;
		}
		return false;
	}

	@Override
	public int hashCode() {
		String combined = fInformationDisplayString.toLowerCase().concat(fContextDisplayString.toLowerCase());
		return combined.hashCode();
	}

	@Override
	public String getInformationDisplayString() {
		return fInformationDisplayString;
	}

	@Override
	public Image getImage() {
		return fImage;
	}

	@Override
	public String getContextDisplayString() {
		if (fContextDisplayString != null)
			return fContextDisplayString;
		return fInformationDisplayString;
	}

	@Override
	public int getContextInformationPosition() {
		return fInformationPosition;
	}

	public void setContextInformationPosition(int pos) {
		fInformationPosition = pos;
	}
}
