/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.core.index;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * Represents a provider for IQMakeEnv which is used to specify an environment for qmake run.
 * This class needs to be registered via org.eclipse.cdt.qt.core.qmakeEnvProvider extension point.
 */
public interface IQMakeEnvProvider {

	/**
	 * Creates a QMake environment for a specific IController (aka a project configuration).
	 *
	 * @param controller the controller
	 * @return the IQMakeEnv instance that is used for qmake run;
	 *         or null if this provider cannot create IQMakeEnv instance for the specified IController.
	 */
	IQMakeEnv createEnv(IController controller);

	/**
	 * Represents a project configuration and provides a control over the environment.
	 *
	 * This class is not meant to be implemented.
	 */
	public interface IController {

		/**
		 * Returns a project configuration for which a QMake environment should be supplied.
		 *
		 * @return the project configuration
		 */
		ICConfigurationDescription getConfiguration();

		/**
		 * Request the controller to schedule a new qmake run to retrieve new QMake information.
		 * This method should be called when there is any change in IQMakeEnv that might affect resulting IQMakeEnvInfo.
		 */
		void scheduleUpdate();

	}

}
