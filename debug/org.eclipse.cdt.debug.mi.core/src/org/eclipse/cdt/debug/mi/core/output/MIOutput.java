package org.eclipse.cdt.debug.mi.core.output;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MIOutput {

	public static final String terminator = "(gdb)\n";

	public boolean interpret() {
		return false;
	}

	public String getToken() {
		return "";
	}
}
