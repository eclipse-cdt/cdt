/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core.make;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.core.runtime.CoreException;

public class MakeProject implements ICOwner {

	public void configure(ICDescriptor cproject) throws CoreException {
		cproject.remove(CCorePlugin.BUILDER_MODEL_ID);
		ICExtensionReference ext = cproject.create(CCorePlugin.BUILDER_MODEL_ID, CCorePlugin.PLUGIN_ID + ".makeBuilder");
		ext.setExtensionData("command", "make");
	}

	public void update(ICDescriptor cproject, String extensionID) throws CoreException {
		if ( extensionID.equals(CCorePlugin.BUILDER_MODEL_ID ) ) {
			ICExtensionReference ext = cproject.create(CCorePlugin.BUILDER_MODEL_ID, CCorePlugin.PLUGIN_ID + ".makeBuilder");
			ext.setExtensionData("command", "make");
		}
	}
}
