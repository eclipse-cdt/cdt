package org.eclipse.cdt.qt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.launchbar.core.ILaunchDescriptor;

public interface IQtLaunchDescriptor extends ILaunchDescriptor {

	IProject getProject();

}
