package org.eclipse.cdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.model.BufferChangedEvent;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.IBufferChangedListener;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;

/**
  * @see IBuffer
  * This class is similar to the JDT Buffer class.
  */
public class Buffer implements IBuffer {
	protected IFile file;
	protected int flags;
	protected char[] contents;
	protected ArrayList changeListeners;
	protected IOpenable owner;
	protected int gapStart= -1;
	protected int gapEnd= -1;

	protected Object lock= new Object();

	protected static final int F_HAS_UNSAVED_CHANGES= 1;
	protected static final int F_IS_READ_ONLY= 2;
	protected static final int F_IS_CLOSED= 4;
	/**
	 * Creates a new buffer on an underlying resource.
	 */
	protected Buffer(IFile file, IOpenable owner, boolean readOnly) {
		this.file = file;
		this.owner = owner;
		if (file == null) {
			setReadOnly(readOnly);
		}
	}

	/**
	 * @see IBuffer
	 */
	public void addBufferChangedListener(IBufferChangedListener listener) {
		if (this.changeListeners == null) {
			this.changeListeners = new ArrayList(5);
		}
		if (!this.changeListeners.contains(listener)) {
			this.changeListeners.add(listener);
		}
	}
	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#append(char)
	 */
	public void append(char[] text) {
		if (!isReadOnly()) {
			if (text == null || text.length == 0) {
				return;
			}
			int length = getLength();
			moveAndResizeGap(length, text.length);
			System.arraycopy(text, 0, this.contents, length, text.length);
			this.gapStart += text.length;
			this.flags |= F_HAS_UNSAVED_CHANGES;
			notifyChanged(new BufferChangedEvent(this, length, 0, new String(text)));
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#append(java.lang.String)
	 */
	public void append(String text) {
		if (text == null) {
			return;
		}
		this.append(text.toCharArray());
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#close()
	 */
	public void close() {
		BufferChangedEvent event = null;
		synchronized (this.lock) {
			if (isClosed())
				return;
			event = new BufferChangedEvent(this, 0, 0, null);
			this.contents = null;
			this.flags |= F_IS_CLOSED;
		}
		notifyChanged(event); // notify outside of synchronized block
		this.changeListeners = null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#getChar(int)
	 */
	public char getChar(int position) {
		synchronized (this.lock) {
			if (position < this.gapStart) {
				return this.contents[position];
			}
			int gapLength = this.gapEnd - this.gapStart;
			return this.contents[position + gapLength];
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#getCharacters()
	 */
	public char[] getCharacters() {
		if (this.contents == null) return null;
		synchronized (this.lock) {
			if (this.gapStart < 0) {
				return this.contents;
			}
			int length = this.contents.length;
			char[] newContents = new char[length - this.gapEnd + this.gapStart];
			System.arraycopy(this.contents, 0, newContents, 0, this.gapStart);
			System.arraycopy(this.contents, this.gapEnd, newContents, this.gapStart, length - this.gapEnd);
			return newContents;
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#getContents()
	 */
	public String getContents() {
		if (this.contents == null) return null;
		return new String(this.getCharacters());
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#getLength()
	 */
	public int getLength() {
		synchronized (this.lock) {
			int length = this.gapEnd - this.gapStart;
			return (this.contents.length - length);
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#getOwner()
	 */
	public IOpenable getOwner() {
		return this.owner;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#getText(int, int)
	 */
	public String getText(int offset, int length) {
		if (this.contents == null)
			return ""; //$NON-NLS-1$
		synchronized (this.lock) {
			if (offset + length < this.gapStart)
				return new String(this.contents, offset, length);
			if (this.gapStart < offset) {
				int gapLength = this.gapEnd - this.gapStart;
				return new String(this.contents, offset + gapLength, length);
			}
			StringBuffer buf = new StringBuffer();
			buf.append(this.contents, offset, this.gapStart - offset);
			buf.append(this.contents, this.gapEnd, offset + length - this.gapStart);
			return buf.toString();
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#getUnderlyingResource()
	 */
	public IResource getUnderlyingResource() {
		return this.file;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#hasUnsavedChanges()
	 */
	public boolean hasUnsavedChanges() {
		return (this.flags & F_HAS_UNSAVED_CHANGES) != 0;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#isClosed()
	 */
	public boolean isClosed() {
		return (this.flags & F_IS_CLOSED) != 0;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#isReadOnly()
	 */
	public boolean isReadOnly() {
		if (this.file == null) {
			return (this.flags & F_IS_READ_ONLY) != 0;
		}
		return this.file.isReadOnly();
	}

	/**
	 * Notify the listeners that this buffer has changed.
	 * To avoid deadlock, this should not be called in a synchronized block.
	 */
	protected void notifyChanged(final BufferChangedEvent event) {
		if (this.changeListeners != null) {
			for (int i = 0, size = this.changeListeners.size(); i < size; ++i) {
				final IBufferChangedListener listener = (IBufferChangedListener) this.changeListeners.get(i);
				Platform.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
						Util.log(exception, "Exception occurred in listener of buffer change notification", ICLogConstants.CDT); //$NON-NLS-1$
					}
					public void run() throws Exception {
						listener.bufferChanged(event);
					}
				});
			}
		}
	}
	/**
	 * @see IBuffer
	 */
	public void removeBufferChangedListener(IBufferChangedListener listener) {
		if (this.changeListeners != null) {
			this.changeListeners.remove(listener);
			if (this.changeListeners.size() == 0) {
				this.changeListeners = null;
			}
		}
	}
	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#replace(int, int, char)
	 */
	public void replace(int position, int length, char[] text) {
		if (!isReadOnly()) {
			int textLength = text == null ? 0 : text.length;
			synchronized (this.lock) {
				// move gap
				moveAndResizeGap(position + length, textLength - length);

				// overwrite
				int min = Math.min(textLength, length);
				if (min > 0) {
					System.arraycopy(text, 0, this.contents, position, min);
				}
				if (length > textLength) {
					// enlarge the gap
					this.gapStart -= length - textLength;
				} else if (textLength > length) {
					// shrink gap
					this.gapStart += textLength - length;
					System.arraycopy(text, 0, this.contents, position, textLength);
				}
			}
			this.flags |= F_HAS_UNSAVED_CHANGES;
			String string = null;
			if (textLength > 0) {
				string = new String(text);
			}
			notifyChanged(new BufferChangedEvent(this, position, length, string));			
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#replace(int, int, java.lang.String)
	 */
	public void replace(int position, int length, String text) {
		this.replace(position, length, text == null ? null : text.toCharArray());
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#save(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	public void save(IProgressMonitor progress, boolean force)
		throws CModelException {
			// determine if saving is required 
			if (isReadOnly() || this.file == null) {
				return;
			}
			synchronized (this.lock) {
				if (!hasUnsavedChanges())
					return;
			
				// use a platform operation to update the resource contents
				try {
					String encoding = null;
					try {
						encoding = this.file.getCharset();
					}
					catch (CoreException ce) {
						// use no encoding
					}
					String contents = this.getContents();
					if (contents == null) return;
					byte[] bytes = encoding == null 
						? contents.getBytes() 
					    : contents.getBytes(encoding);
					ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

					if (this.file.exists()) {
						this.file.setContents(
							stream, 
							force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
							null);
					} else {
						this.file.create(stream, force, null);
					}	
				}  catch (IOException e) {
					throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
				}
				catch (CoreException e) {
					throw new CModelException(e);
				}

				// the resource no longer has unsaved changes
				this.flags &= ~ (F_HAS_UNSAVED_CHANGES);
			}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#setContents(char)
	 */
	public void setContents(char[] newContents) {
		// allow special case for first initialization 
		// after creation by buffer factory
		if (this.contents == null) {
			this.contents = newContents;
			this.flags &= ~ (F_HAS_UNSAVED_CHANGES);
			return;
		}
	
		if (!isReadOnly()) {
			String string = null;
			if (newContents != null) {
				string = new String(newContents);
			}
			BufferChangedEvent event = new BufferChangedEvent(this, 0, this.getLength(), string);
			synchronized (this.lock) {
				this.contents = newContents;
				this.flags |= F_HAS_UNSAVED_CHANGES;
				this.gapStart = -1;
				this.gapEnd = -1;
			}
			notifyChanged(event);
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBuffer#setContents(java.lang.String)
	 */
	public void setContents(String newContents) {
		this.setContents(newContents.toCharArray());
	}
	
	/**
	 * Moves the gap to location and adjust its size to the
	 * anticipated change size. The size represents the expected 
	 * range of the gap that will be filled after the gap has been moved.
	 * Thus the gap is resized to actual size + the specified size and
	 * moved to the given position.
	 */
	protected void moveAndResizeGap(int position, int size) {
		char[] content = null;
		int oldSize = this.gapEnd - this.gapStart;
		if (size < 0) {
			if (oldSize > 0) {
				content = new char[this.contents.length - oldSize];
				System.arraycopy(this.contents, 0, content, 0, this.gapStart);
				System.arraycopy(this.contents, this.gapEnd, content, this.gapStart, content.length - this.gapStart);
				this.contents = content;
			}
			this.gapStart = this.gapEnd = position;
			return;
		}
		content = new char[this.contents.length + (size - oldSize)];
		int newGapStart = position;
		int newGapEnd = newGapStart + size;
		if (oldSize == 0) {
			System.arraycopy(this.contents, 0, content, 0, newGapStart);
			System.arraycopy(this.contents, newGapStart, content, newGapEnd, content.length - newGapEnd);
		} else
			if (newGapStart < this.gapStart) {
				int delta = this.gapStart - newGapStart;
				System.arraycopy(this.contents, 0, content, 0, newGapStart);
				System.arraycopy(this.contents, newGapStart, content, newGapEnd, delta);
				System.arraycopy(this.contents, this.gapEnd, content, newGapEnd + delta, this.contents.length - this.gapEnd);
			} else {
				int delta = newGapStart - this.gapStart;
				System.arraycopy(this.contents, 0, content, 0, this.gapStart);
				System.arraycopy(this.contents, this.gapEnd, content, this.gapStart, delta);
				System.arraycopy(this.contents, this.gapEnd + delta, content, newGapEnd, content.length - newGapEnd);
			}
		this.contents = content;
		this.gapStart = newGapStart;
		this.gapEnd = newGapEnd;
	}
	
	/**
	 * Sets this <code>Buffer</code> to be read only.
	 */
	protected void setReadOnly(boolean readOnly) {
		if (readOnly) {
			this.flags |= F_IS_READ_ONLY;
		} else {
			this.flags &= ~(F_IS_READ_ONLY);
		}
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Owner: " + ((CElement)this.owner).toString()); //$NON-NLS-1$
		buffer.append("\nHas unsaved changes: " + this.hasUnsavedChanges()); //$NON-NLS-1$
		buffer.append("\nIs readonly: " + this.isReadOnly()); //$NON-NLS-1$
		buffer.append("\nIs closed: " + this.isClosed()); //$NON-NLS-1$
		buffer.append("\nContents:\n"); //$NON-NLS-1$
		char[] contents = this.getCharacters();
		if (contents == null) {
			buffer.append("<null>"); //$NON-NLS-1$
		} else {
			int length = contents.length;
			for (int i = 0; i < length; i++) {
				char car = contents[i];
				switch (car) {
					case '\n': 
						buffer.append("\\n\n"); //$NON-NLS-1$
						break;
					case '\r':
						if (i < length-1 && this.contents[i+1] == '\n') {
							buffer.append("\\r\\n\n"); //$NON-NLS-1$
							i++;
						} else {
							buffer.append("\\r\n"); //$NON-NLS-1$
						}
						break;
					default:
						buffer.append(car);
						break;
				}
			}
		}
		return buffer.toString();
	}
}
