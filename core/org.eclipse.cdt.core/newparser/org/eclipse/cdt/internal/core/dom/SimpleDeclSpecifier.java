package org.eclipse.cdt.internal.core.dom;

/**
 * @author dschaefe
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SimpleDeclSpecifier extends DeclSpecifier {
	public static final int t_char = 0;
	public static final int t_wchar_t = 1;
	public static final int t_bool = 2;
	public static final int t_short = 3;
	public static final int t_int = 4;
	public static final int t_long = 5;
	public static final int t_signed = 6;
	public static final int t_unsigned = 7;
	public static final int t_float = 8;
	public static final int t_double = 9;
	public static final int t_void = 10;
	public static final int t_auto = 11;
	public static final int t_register = 12;
	public static final int t_static = 13;
	public static final int t_extern = 14;
	public static final int t_mutable = 15; 
	public static final int t_inline = 16;
	public static final int t_virtual = 17;
	public static final int t_explicit = 18;
	public static final int t_const = 19;
	public static final int t_volatile = 20;
	public static final int t_friend = 21;
	public static final int t_typedef = 22;

	private int type;

	public SimpleDeclSpecifier(int t) {
		type = t;
	}
}
