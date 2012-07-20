package org.eclipse.cdt.autotools.core;

import org.eclipse.core.runtime.CoreException;


/**
 * @since 1.2
 */
public interface IAutotoolsOption {
	public final static int CATEGORY = 0;
	public final static int BIN = 1;
	public final static int STRING = 2;
	public final static int INTERNAL = 3;
	public final static int MULTIARG = 4;
	public final static int TOOL = 5;
	public final static int FLAG = 6;
	public final static int FLAGVALUE = 7;
	public int getType();
	public boolean canUpdate();
	public void setValue(String value) throws CoreException;
	public String getValue();
}
