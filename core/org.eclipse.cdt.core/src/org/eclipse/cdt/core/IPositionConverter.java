/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core;

import org.eclipse.jface.text.Position;

/**
 * Allows for converting character ranges of files previously stored on disk to the 
 * range where the characters are found in the current version of the file. The 
 * current version can be the content of a dirty editor, or if there is none, the
 * latest verison of the file as stored on disk.
 * 
 * As long as the underlying text of the character range has not been modified the
 * converted range will have the same underlying text. Insertions at the beginning
 * or the end of the text do not added to the converted range. 
 * 
 * An insertion inside the underlying text will increase the length of the converted
 * range, a deletion of one of the characters will decrease it.
 * 
 * An deletion followed by an insertion without saving the file inbetween, will cancel
 * the deletion as much as possible.
 */

public interface IPositionConverter {
    /** 
     * Converts an actual character range to the range where the underlying text 
     * was originally found.
     * @param actualPosition a range as found in the current text buffer for the file.
     * @return a range suitable for the version of the file for which the converter
     * was obtained.
     */
    Position actualToHistoric(Position actualPosition);

    /** 
     * Converts a historic character range to the range where the underlying text 
     * currently can be found.
     * @param historicPosition a range as found in the version of the file for which
     * the converter was obtained.
     * @return a range suitable for the current text buffer of the file.
     * 
     */
    Position historicToActual(Position historicPosition);
}
