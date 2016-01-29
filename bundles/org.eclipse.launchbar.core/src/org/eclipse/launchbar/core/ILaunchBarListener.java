/*******************************************************************************
 * Copyright (c) Jan 29, 2016 QNX Software Systems. All Rights Reserved.
 *
 * You must obtain a written license from and pay applicable license fees to QNX
 * Software Systems before you may reproduce, modify or distribute this software,
 * or any work that includes all or part of this software.   Free development
 * licenses are available for evaluation and non-commercial purposes.  For more
 * information visit [http://licensing.qnx.com] or email licensing@qnx.com.
 *
 * This file may contain contributions from others.  Please review this entire
 * file for other proprietary rights or license notices, as well as the QNX
 * Development Suite License Guide at [http://licensing.qnx.com/license-guide/]
 * for other information.
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * Listener that can be installed on launch bar to listen for state changes.
 * {@link ILaunchBarManager#addListener(ILaunchBarListener)}.
 *
 * <code>
 *    Activator.getService(ILaunchBarManager.class).addListener(new ILaunchBarListener(){
 *             public void activeLaunchTargetChanged(ILaunchTarget target) {
 *                     // do something
 *             }
 *    });
 * </code>
 */
public interface ILaunchBarListener {
	default void activeLaunchDescriptorChanged(ILaunchDescriptor descriptor) {
	}

	default void activeLaunchModeChanged(ILaunchMode mode) {
	}

	default void activeLaunchTargetChanged(ILaunchTarget target) {
	}

	default void launchTargetsChanged() {
	}
}