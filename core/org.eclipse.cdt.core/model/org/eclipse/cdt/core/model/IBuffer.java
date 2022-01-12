/*******************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A buffer contains the text contents of a resource. It is not language-specific.
 * The contents may be in the process of being edited, differing from the actual contents of the
 * underlying resource. A buffer has an owner, which is an
 * {@code IOpenable}. If a buffer does not have an underlying resource,
 * saving the buffer has no effect. Buffers can be read-only.
 * <p>
 * This interface is similar to the JDT IBuffer interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IBuffer {
	/**
	 * Adds the given listener for changes to this buffer.
	 * Has no effect if an identical listener is already registered or if the buffer is closed.
	 *
	 * @param listener the listener of buffer changes
	 */
	public void addBufferChangedListener(IBufferChangedListener listener);

	/**
	 * Appends the given character array to the contents of the buffer.
	 * This buffer will now have unsaved changes.
	 * Any client can append to the contents of the buffer, not just the owner of the buffer.
	 * Reports a buffer changed event.
	 * <p>
	 * Has no effect if this buffer is read-only.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param text the given character array to append to contents of the buffer
	 */
	public void append(char[] text);

	/**
	 * Appends the given string to the contents of the buffer.
	 * This buffer will now have unsaved changes.
	 * Any client can append to the contents of the buffer, not just the owner of the buffer.
	 * Reports a buffer changed event.
	 * <p>
	 * Has no effect if this buffer is read-only.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param text the {@code String} to append to the contents of the buffer
	 */
	public void append(String text);

	/**
	 * Closes the buffer. Any unsaved changes are lost. Reports a buffer changed event
	 * with a 0 offset and a 0 length. When this event is fired, the buffer should already
	 * be closed.
	 * <p>
	 * Further operations on the buffer are not allowed, except for close.  If an
	 * attempt is made to close an already closed buffer, the second attempt has no effect.
	 */
	public void close();

	/**
	 * Returns the character at the given position in this buffer.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param position a zero-based source offset in this buffer
	 * @return the character at the given position in this buffer
	 */
	public char getChar(int position);

	/**
	 * Returns the contents of this buffer as a character array, or {@code null} if
	 * the buffer has not been initialized.
	 * <p>
	 * Callers should make no assumption about whether the returned character array
	 * is or is not the genuine article or a copy. In other words, if the client
	 * wishes to change this array, they should make a copy. Likewise, if the
	 * client wishes to hang on to the array in its current state, they should
	 * make a copy.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @return the characters contained in this buffer
	 */
	public char[] getCharacters();

	/**
	 * Returns the contents of this buffer as a {@code String}. Like all strings,
	 * the result is an immutable value object., It can also answer {@code null} if
	 * the buffer has not been initialized.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @return the contents of this buffer as a {@code String}
	 */
	public String getContents();

	/**
	 * Returns number of characters stored in this buffer.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @return the number of characters in this buffer
	 */
	public int getLength();

	/**
	 * Returns the resource element owning of this buffer.
	 *
	 * @return the resource element owning this buffer
	 */
	public IOpenable getOwner();

	/**
	 * Returns the given range of text in this buffer.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param offset the  zero-based starting offset
	 * @param length the number of characters to retrieve
	 * @return the given range of text in this buffer
	 */
	public String getText(int offset, int length);

	/**
	 * Returns the underlying resource for which this buffer was opened,
	 * or {@code null} if this buffer was not opened on a resource.
	 *
	 * @return the underlying resource for this buffer, or {@code null}
	 *  if none.
	 */
	public IResource getUnderlyingResource();

	/**
	 * Returns whether this buffer has been modified since it
	 * was opened or since it was last saved.
	 * If a buffer does not have an underlying resource, this method always
	 * returns {@code true}.
	 *
	 * @return a {@code boolean} indicating presence of unsaved changes (in
	 *   the absence of any underlying resource, it will always return {@code true}).
	 */
	public boolean hasUnsavedChanges();

	/**
	 * Returns whether this buffer has been closed.
	 *
	 * @return a {@code boolean} indicating whether this buffer is closed.
	 */
	public boolean isClosed();

	/**
	 * Returns whether this buffer is read-only.
	 *
	 * @return a {@code boolean} indicating whether this buffer is read-only
	 */
	public boolean isReadOnly();

	/**
	 * Removes the given listener from this buffer.
	 * Has no affect if an identical listener is not registered or if the buffer is closed.
	 *
	 * @param listener the listener
	 */
	public void removeBufferChangedListener(IBufferChangedListener listener);

	/**
	 * Replaces the given range of characters in this buffer with the given text.
	 * {@code position} and {@code position + length} must be in the range [0, getLength()].
	 * {@code length} must not be negative.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param position the zero-based starting position of the affected text range in this buffer
	 * @param length the length of the affected text range in this buffer
	 * @param text the replacing text as a character array
	 */
	public void replace(int position, int length, char[] text);

	/**
	 * Replaces the given range of characters in this buffer with the given text.
	 * {@code position} and {@code position + length} must be in the range [0, getLength()].
	 * {@code length} must not be negative.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param position the zero-based starting position of the affected text range in this buffer
	 * @param length the length of the affected text range in this buffer
	 * @param text the replacing text as a {@code String}
	 */
	public void replace(int position, int length, String text);

	/**
	 * Saves the contents of this buffer to its underlying resource. If
	 * successful, this buffer will have no unsaved changes.
	 * The buffer is left open. Saving a buffer with no unsaved
	 * changes has no effect - the underlying resource is not changed.
	 * If the buffer does not have an underlying resource or is read-only, this
	 * has no effect.
	 * <p>
	 * The {@code force} parameter controls how this method deals with
	 * cases where the workbench is not completely in sync with the local file system.
	 * If {@code false} is specified, this method will only attempt
	 * to overwrite a corresponding file in the local file system provided
	 * it is in sync with the workbench. This option ensures there is no
	 * unintended data loss; it is the recommended setting.
	 * However, if {@code true} is specified, an attempt will be made
	 * to write a corresponding file in the local file system,
	 * overwriting any existing one if need be.
	 * In either case, if this method succeeds, the resource will be marked
	 * as being local (even if it wasn't before).
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param progress the progress monitor to notify
	 * @param force a {@code boolean} flag indicating how to deal with resource
	 *   inconsistencies.
	 *
	 * @exception CModelException if an error occurs writing the buffer 	to
	 * the underlying resource
	 *
	 * @see org.eclipse.core.resources.IFile#setContents(java.io.InputStream, boolean, boolean, IProgressMonitor)
	 */
	public void save(IProgressMonitor progress, boolean force) throws CModelException;

	/**
	 * Sets the contents of this buffer to the given character array.
	 * This buffer will now have unsaved changes.
	 * Any client can set the contents of the buffer, not just the owner of the buffer.
	 * Reports a buffer changed event.
	 * <p>
	 * Equivalent to {@code replace(0, getLength(), contents)}.
	 * <p>
	 * Has no effect if this buffer is read-only.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param contents the new contents of this buffer as a character array
	 */
	public void setContents(char[] contents);

	/**
	 * Sets the contents of this buffer to the given {@code String}.
	 * This buffer will now have unsaved changes.
	 * Any client can set the contents of the buffer, not just the owner of the buffer.
	 * Reports a buffer changed event.
	 * <p>
	 * Equivalent to {@code replace(0, getLength(), contents)}.
	 * <p>
	 * Has no effect if this buffer is read-only.
	 * <p>
	 * A {@code RuntimeException} might be thrown if the buffer is closed.
	 *
	 * @param contents the new contents of this buffer as a {@code String}
	 */
	public void setContents(String contents);
}
