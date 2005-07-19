package org.eclipse.cdt.debug.mi.core.command;

/**
 * TODO: Make this an enum type.
 * MI Version constants.
 */
public class MIVersion {
	
	public static final String MI1 = "mi1"; //$NON-NLS-1$
	public static final String MI2 = "mi2"; //$NON-NLS-1$
	public static final String MI3 = "mi3"; //$NON-NLS-1$
	
	public static int compare(String v1, String v2) {
		return v1.compareToIgnoreCase(v2);
	}
	
	public static boolean equals(String v1, String v2) {
		return v1.equalsIgnoreCase(v2);
	}
}
