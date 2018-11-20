/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core;

import org.eclipse.jface.text.IRegion;

/**
 * Allows for converting character ranges of files previously stored on disk to the
 * range where the characters are found in the current version of the file. The
 * current version can be the content of a dirty editor, or if there is none, the
 * latest verison of the file as stored on disk.
 *
 * As long as the underlying text of the character range has not been modified the
 * converted range will have the same underlying text. Insertions at the beginning
 * or the end of the text are not added to the converted range.
 *
 * An insertion inside the underlying text will increase the length of the converted
 * range, a deletion of one of the characters will decrease it.
 *
 * An deletion followed by an insertion without saving the file inbetween, will cancel
 * the deletion as far as possible.
 *
 * @since 4.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */

public interface IPositionConverter {
	/**
	 * Converts an actual character range to the range where the underlying text
	 * was originally found.
	 * @param actualPosition a range as found in the current text buffer for the file.
	 * @return a range suitable for the version of the file for which the converter
	 * was obtained.
	 */
	IRegion actualToHistoric(IRegion actualPosition);

	/**
	 * Converts a historic character range to the range where the underlying text
	 * currently can be found.
	 * @param historicPosition a range as found in the version of the file for which
	 * the converter was obtained.
	 * @return a range suitable for the current text buffer of the file.
	 */
	IRegion historicToActual(IRegion historicPosition);
}
