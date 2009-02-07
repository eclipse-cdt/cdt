/*******************************************************************************
 * Copyright (c) 2008 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemResourceVariant;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.ThreeWayResourceComparator;
import org.eclipse.team.core.variants.ThreeWaySynchronizer;

public class RSEResourceVariantComparator extends ThreeWayResourceComparator {
	public RSEResourceVariantComparator(ThreeWaySynchronizer synchronizer){
		super(synchronizer);
	}

	public boolean compare(IResource local, IResourceVariant remote) {
		return super.compare(local, remote) && equalSize(local, remote);
	}

	/**
	 * Return if the size of local and remote file are the same.
	 * @param local
	 * @param remote
	 * @return
	 */
	private boolean equalSize(IResource local, IResourceVariant remote){
		long localSize = local.getLocation().toFile().length();
		long remoteSize = 0;
		if(remote instanceof FileSystemResourceVariant){
			remoteSize = ((FileSystemResourceVariant)remote).getFile().length();
		}
		return localSize == remoteSize;
	}
}
