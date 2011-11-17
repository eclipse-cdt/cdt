/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.parser;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.internal.core.parser.CodeReaderLRUCache;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This is the CodeReaderBuffer used to cache CodeReaders for the ICodeReaderFactory
 * when working with saved copies (primarily SavedCodeReaderFactory).
 *
 * @author dsteffle
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class CodeReaderCache implements ICodeReaderCache {
	/**
	 * The string used to identify this CodeReaderCache.  Mainly used for preferences.
	 */
	public static final String CODE_READER_BUFFER = CCorePlugin.PLUGIN_ID + ".codeReaderCache"; //$NON-NLS-1$

	/**
	 * The default size of the cache in MB.
	 */
	public static final int DEFAULT_CACHE_SIZE_IN_MB = 64;

	/**
	 * A String value of the default size of the cache.
	 */
	public static final String DEFAULT_CACHE_SIZE_IN_MB_STRING = String.valueOf(DEFAULT_CACHE_SIZE_IN_MB);
	private static final int MB_TO_KB_FACTOR = 1024;
	private CodeReaderLRUCache cache = null; // the actual cache
	private final IResourceChangeListener listener = new UpdateCodeReaderCacheListener(this);

	private class UpdateCodeReaderCacheListener implements IResourceChangeListener {
		ICodeReaderCache c = null;

		/**
		 * Create the UpdateCodeReaderCacheListener used to dispatch events to remove CodeReaders
		 * from the cache when a resource is changed and detected in Eclipse.
		 * @param cache
		 */
		public UpdateCodeReaderCacheListener(ICodeReaderCache cache) {
			this.c = cache;
		}

		private class RemoveCacheJob extends Job {
			private static final String REMOVE_CACHE = "Remove Cache"; //$NON-NLS-1$
			ICodeReaderCache cache1 = null;
			IResourceChangeEvent event = null;

			/**
			 * Create a RemoveCacheJob used to run as a separate thread to remove a CodeReader from the cache.
			 */
			public RemoveCacheJob(ICodeReaderCache cache, IResourceChangeEvent event) {
				super(REMOVE_CACHE);
				this.cache1 = cache;
				this.event = event;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (event.getSource() instanceof IWorkspace && event.getDelta() != null) {
					removeKeys(event.getDelta().getAffectedChildren());
				}
				event = null;
				return Status.OK_STATUS;
			}

            private void removeKeys(IResourceDelta[] deltas) {
                for (IResourceDelta delta : deltas) {
                    if (delta.getResource().getType() == IResource.PROJECT || delta.getResource().getType() == IResource.FOLDER) {
                        removeKeys(delta.getAffectedChildren());
                    } else if (delta.getResource() instanceof IFile && ((IFile)delta.getResource()).getLocation() != null) {
                        removeKey(((IFile)delta.getResource()).getLocation().toOSString());
                    }
                }
            }

            private void removeKey(String key) {
                if (key != null && cache1 != null)
                    cache1.remove(key);
            }

		}

		/**
		 * Identifies when a resource was chaned and schedules a new RemoveCacheJob.
		 */
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (c instanceof CodeReaderCache)
				new RemoveCacheJob(c, event).schedule();
		}
	}

	/**
	 * Creates a CodeReaderCache and sets the size of the CodeReaderCache in MB.  Creating a new
	 * CodeReaderCache also adds an UpdateCodeReaderCacheListener to the workspace so that when
	 * a resource is changed then the CodeReader for that resource is removed from this cache.
	 *
	 * @param size initial size of the CodeReaderCache in terms of MB
	 */
	public CodeReaderCache(int size) {
		cache = new CodeReaderLRUCache(size * MB_TO_KB_FACTOR);
	}

	@Override
	protected void finalize() throws Throwable {
		flush();
		super.finalize();
	}

	/**
	 * Get a CodeReader from the cache.  The key is the char[] filename of the CodeReader to retrieve.
	 * @param key the path of the CodeReader to retrieve
	 */
	@Override
	public synchronized CodeReader get(String key) {
		CodeReader result = null;
		if (cache.getSpaceLimit() > 0)
			result= cache.get(key);

		if (result != null)
			return result;

		// for efficiency: check File.exists before ParserUtil#createReader()
		// bug 100947 fix: don't want to attempt to create a code reader if there is no file for the key
		final File jfile = new File(key);
		if (!(jfile.exists()))
			return null;

		try {
			IResource file = ParserUtil.getResourceForFilename(key);
			if (file instanceof IFile) {
				key= InternalParserUtil.normalizePath(key, (IFile) file);
				result=  InternalParserUtil.createWorkspaceFileReader(key, (IFile) file, cache);
			}
			result= InternalParserUtil.createExternalFileReader(key, cache);
			if (cache.getSpaceLimit() > 0)
				put(result);

			return result;
		} catch (CoreException ce) {
		} catch (IOException e) {
		} catch (IllegalStateException e) {
		}
		return null;
	}

	/**
	 * @throws IOException
	 * @throws CoreException
	 * @since 5.1
	 */
	@Override
	public CodeReader get(String key, IIndexFileLocation ifl) throws CoreException, IOException {
		CodeReader result = null;
		if (cache.getSpaceLimit() > 0)
			result= cache.get(key);

		if (result != null)
			return result;

		result= InternalParserUtil.createCodeReader(ifl, cache);
		if (cache.getSpaceLimit() > 0)
			put(result);

		return result;
	}

	/**
	 * Put a CodeReader into the Cache.
	 * @param key
	 * @param value
	 * @return
	 */
	private synchronized CodeReader put(CodeReader value) {
		if (value==null) return null;
		if (cache.isEmpty()) {
			if (ResourcesPlugin.getWorkspace() != null)
				ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
		}
		return cache.put(String.valueOf(value.filename), value);
	}

	/**
	 * Sets the max cache size of this cache in terms of MB.
	 * @param size
	 */
	public void setCacheSize(int size) {
		cache.setSpaceLimit(size * MB_TO_KB_FACTOR);
	}


	/**
	 * Removes the CodeReader from the cache corresponding to the path specified by the key and
	 * returns the CodeReader that was removed.  If no CodeReader is removed then null is returned.
	 * @param key
	 */
	@Override
	public synchronized CodeReader remove(String key) {
		CodeReader removed= cache.remove(key);
		if (cache.isEmpty()) {
			if (ResourcesPlugin.getWorkspace() != null)
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		}
		return removed;
	}

	/**
	 * Returns the current size of the cache.  For the CodeReaderCache this is in MB.
	 */
	@Override
	public int getCurrentSpace() {
		return cache.getCurrentSpace();
	}

	@Override
	public void flush() {
		cache.flush();
		if (ResourcesPlugin.getWorkspace() != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
	}

}
