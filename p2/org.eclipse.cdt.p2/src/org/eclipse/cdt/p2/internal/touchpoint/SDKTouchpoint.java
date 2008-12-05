/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.p2.internal.touchpoint;

import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;
import org.eclipse.equinox.internal.provisional.p2.engine.Touchpoint;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
import org.eclipse.equinox.internal.provisional.p2.metadata.TouchpointType;
import org.osgi.framework.Version;

/**
 * @author DSchaefe
 *
 */
public class SDKTouchpoint extends Touchpoint {

	public static final TouchpointType TOUCHPOINT_TYPE = MetadataFactory.createTouchpointType(SDKTouchpoint.class.getName(), new Version("1"));
	
	@Override
	public ProvisioningAction getAction(String actionId) {
		if (CollectAction.ACTION_NAME.equals(actionId))
			return new CollectAction();
		else if (UninstallAction.ACTION_NAME.equals(actionId))
			return new UninstallAction();
		else
			return null;
	}

	@Override
	public TouchpointType getTouchpointType() {
		return TOUCHPOINT_TYPE;
	}

}
