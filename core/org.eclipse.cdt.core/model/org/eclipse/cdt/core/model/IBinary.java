package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Represents a Binary file, for example an ELF excutable.
 * An ELF parser will inspect the binary.
 */
public interface IBinary extends ICFile {
	/**
	 * Return whether the file was compiling with debug symbols.
	 */
	public boolean hasDebug();

	public boolean isExecutable();

	public boolean isObject();

	public boolean isSharedLib();

	public String [] getNeededSharedLibs();

	public String getSoname();

	public String getCPU();

	public long getText();

	public long getData();

	public long getBSS();
}
