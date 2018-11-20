/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class Checksums {
	private static final String KEY_ALGORITHM = "//algorithm//"; //$NON-NLS-1$
	private static final String DEFAULT_ALGORITHM = "MD5"; //$NON-NLS-1$

	/**
	 * Returns the default algorithm used to compute checksums.
	 * @throws NoSuchAlgorithmException
	 * @since 4.0
	 */
	public static MessageDigest getDefaultAlgorithm() throws NoSuchAlgorithmException {
		return MessageDigest.getInstance(DEFAULT_ALGORITHM);
	}

	/**
	 * Retrieves the algorithm for computing checksums from the persisted map.
	 * @throws NoSuchAlgorithmException
	 * @since 4.0
	 */
	public static MessageDigest getAlgorithm(Map<?, ?> persistedMap) throws NoSuchAlgorithmException {
		Object obj = persistedMap.get(KEY_ALGORITHM);
		String alg = obj instanceof String ? (String) obj : DEFAULT_ALGORITHM;
		return MessageDigest.getInstance(alg);
	}

	/**
	 * Stores the algorithm in a map.
	 * @since 4.0
	 */
	public static void putAlgorithm(Map<String, Object> mapToPersist, MessageDigest md) {
		mapToPersist.put(KEY_ALGORITHM, md.getAlgorithm());
	}

	/**
	 * Computes the checksum for a given file.
	 */
	public static byte[] computeChecksum(MessageDigest md, File file) throws IOException {
		md.reset();
		FileInputStream fi = new FileInputStream(file);
		try {
			int read;
			byte[] buf = new byte[1024 * 64];
			while ((read = fi.read(buf)) >= 0) {
				md.update(buf, 0, read);
			}
			return md.digest();
		} finally {
			fi.close();
		}
	}

	/**
	 * Retrieves a checksum for a file from the persisted map. May return <code>null</code>.
	 * @since 4.0
	 */
	public static byte[] getChecksum(Map<?, ?> persistedMap, IFile file) {
		IPath prjRel = file.getProjectRelativePath();
		Object checksum = persistedMap.get(prjRel.toString());
		if (checksum instanceof byte[])
			return (byte[]) checksum;
		return null;
	}

	/**
	 * Stores a checksum in a map.
	 * @since 4.0
	 */
	public static void putChecksum(Map<String, Object> mapToPersist, IFile file, byte[] checksum) {
		IPath prjRel = file.getProjectRelativePath();
		mapToPersist.put(prjRel.toString(), checksum);
	}

	/**
	 * Creates a map to persist checksums for a project.
	 * @throws OperationCanceledException
	 * @since 4.0
	 */
	public static Map<String, Object> createChecksumMap(IFile[] tus, MessageDigest md, IProgressMonitor pm)
			throws OperationCanceledException {
		Map<String, Object> result = new HashMap<>();
		putAlgorithm(result, md);
		pm.beginTask(Messages.Checksums_taskComputeChecksums, tus.length);
		for (IFile file : tus) {
			if (pm.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (file != null) {
				IPath location = file.getLocation();
				if (location != null) {
					File f = location.toFile();
					if (f.isFile()) {
						try {
							byte[] checksum = computeChecksum(md, f);
							putChecksum(result, file, checksum);
						} catch (IOException e) {
							CCorePlugin.log(e);
						}
					}
				}
			}
			pm.worked(1);
		}
		pm.done();
		return result;
	}
}
