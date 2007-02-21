package org.eclipse.cdt.ui.newui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Simple IStatus implementation to avoid using internal classes
 */
public class CDTStatusInfo implements IStatus {
	private String text;
	private int code;

	public CDTStatusInfo() {this(OK, null); }
	public CDTStatusInfo(int _code, String _text) {
		text= _text;
		code= _code;
	}		
	public IStatus[] getChildren() { return new IStatus[0];} 
	public int getCode() { return code; } 
	public Throwable getException() { return null; }
	public String getMessage() { return text; }
	public String getPlugin() { return CUIPlugin.PLUGIN_ID; }
	public int getSeverity() { return code; }
	public boolean isMultiStatus() { return false; }
	public boolean isOK() { return (code == OK); }
	public boolean matches(int mask) { return (code & mask) != 0; }
}
