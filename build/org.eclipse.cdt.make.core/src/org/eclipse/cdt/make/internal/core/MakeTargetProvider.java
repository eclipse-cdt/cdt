/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.IMakeTargetProvider;
import org.eclipse.core.resources.IContainer;

public class MakeTargetProvider implements IMakeTargetProvider {

	public MakeTargetProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.IMakeTargetProvider#getTargets()
	 */
	public IMakeTarget[] getTargets() {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.IMakeTargetProvider#getTargets(org.eclipse.core.resources.IContainer)
	 */
	public IMakeTarget[] getTargets(IContainer container) {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.IMakeTargetProvider#getBuilderInfo(org.eclipse.cdt.make.core.IMakeTarget)
	 */
	public IMakeBuilderInfo getBuilderInfo(IMakeTarget target) {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.IMakeTargetProvider#addListener(org.eclipse.cdt.make.core.IMakeTargetListener)
	 */
	public void addListener(IMakeTargetListener listener) {
		// dinglis-TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.IMakeTargetProvider#removeListener(org.eclipse.cdt.make.core.IMakeTargetListener)
	 */
	public void removeListener(IMakeTargetListener listener) {
		// dinglis-TODO Auto-generated method stub

	}

}
