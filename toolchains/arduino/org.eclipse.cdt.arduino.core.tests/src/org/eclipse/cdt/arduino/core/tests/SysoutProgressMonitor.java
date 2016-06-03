package org.eclipse.cdt.arduino.core.tests;

import org.eclipse.core.runtime.NullProgressMonitor;

public class SysoutProgressMonitor extends NullProgressMonitor {
	
	@Override
	public void beginTask(String name, int totalWork) {
		if (name.length() > 0) {
			System.out.println(name);
			System.out.flush();
		}
	}
	
	@Override
	public void subTask(String name) {
		if (name.length() > 0) {
			System.out.println(name);
			System.out.flush();
		}
	}

	@Override
	public void setTaskName(String name) {
		if (name.length() > 0) {
			System.out.println(name);
			System.out.flush();
		}
	}
	
}
