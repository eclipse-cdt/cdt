/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */package org.eclipse.cdt.debug.core;

public interface ICDebuggerInfo {
	String getID();
	String getName();
	String[] getSupportedPlatforms();
}
