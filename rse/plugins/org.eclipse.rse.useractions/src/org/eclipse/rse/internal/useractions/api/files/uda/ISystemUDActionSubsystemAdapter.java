/*********************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - [222470] initial contribution.
 *********************************************************************************/
package org.eclipse.rse.internal.useractions.api.files.uda;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionSubsystem;


/**
 * User-defined Action Subsystem Adapter Interface.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public interface ISystemUDActionSubsystemAdapter {

	public SystemUDActionSubsystem getSystemUDActionSubsystem(ISubSystemConfiguration ssc);

}
