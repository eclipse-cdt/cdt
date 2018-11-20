/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
package org.eclipse.cdt.internal.ui.text.contentassist;

import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.swt.graphics.Image;

/**
 * @author aniefer
 */
public class CProposalContextInformation implements IContextInformation, IContextInformationExtension {
	/** The name of the context */
	private String fContextDisplayString;
	/** The information to be displayed */
	private String fInformationDisplayString;
	/** The position to display the information */
	private int fInformationPosition;
	/** The image to be displayed */
	private Image fImage;

	/**
	 * The information display string usually just contains a comma-separated
	 * list of (function or template) parameters.
	 * Optionally, it can contain a prefix before and a suffix after the
	 * parameter list (so that e.g. it displays a function's full signature,
	 * including name and return value).
	 * In such a case, fHasPrefixSuffix is true, and fParamlistStartIndex
	 * and fParamlistEndIndex denote the indices that bound the parameter list
	 * portion of the information display string.
	 */
	private boolean fHasPrefixSuffix;
	private int fParamlistStartIndex;
	private int fParamlistEndIndex;

	public void setHasPrefixSuffix(int paramlistStartIndex, int paramlistEndIndex) {
		fHasPrefixSuffix = true;
		fParamlistStartIndex = paramlistStartIndex;
		fParamlistEndIndex = paramlistEndIndex;
	}

	public boolean hasPrefixSuffix() {
		return fHasPrefixSuffix;
	}

	public int getArglistStartIndex() {
		return fParamlistStartIndex;
	}

	public int getArglistEndIndex() {
		return fParamlistEndIndex;
	}

	/**
	 * Creates a new context information without an image.
	 *
	 * @param contextDisplayString the string to be used when presenting the context
	 * @param informationDisplayString the string to be displayed when presenting the context information
	 */
	public CProposalContextInformation(String contextDisplayString, String informationDisplayString) {
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
	public CProposalContextInformation(Image image, String contextDisplayString, String informationDisplayString) {
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
