package org.eclipse.cdt.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


public class TagFlags {

	public final static int T_UNKNOWN = 0;
	public final static int T_CLASS = 1;
	public final static int T_MACRO = 2;
	public final static int T_ENUMERATOR = 3;
	public final static int T_FUNCTION = 4;
	public final static int T_ENUM = 5;
	public final static int T_MEMBER = 6;
	public final static int T_NAMESPACE = 7;
	public final static int T_PROTOTYPE = 8;
	public final static int T_STRUCT = 9;
	public final static int T_TYPEDEF = 10;
	public final static int T_UNION = 11;
	public final static int T_VARIABLE = 12;
	public final static int T_EXTERNVAR = 13;

	public final static int T_PRIVATE = 14;
	public final static int T_PROTECTED = 15;
	public final static int T_PUBLIC = 16;
	public final static int T_FRIEND = 17;
	public final static int T_VIRTUAL = 18;
	public final static int T_ABSTRACT = 19;

	private TagFlags() {
	}

	public static boolean isUnknown(int k) {
		return k == T_UNKNOWN;
	}

	public static boolean isClass(int k) {
		return k == T_CLASS;
	}

	public static boolean isMacro(int k) {
		return k == T_MACRO;
	}

	public static boolean isEnumerator(int k) {
		return k == T_ENUMERATOR;
	}

	public static boolean isFunction(int k) {
		return k == T_FUNCTION;
	}

	public static boolean isEnum(int k) {
		return k == T_ENUM;
	}

	public static boolean isMember(int k) {
		return k == T_MEMBER;
	}

	public static boolean isNamespace(int k) {
		return k == T_NAMESPACE;
	}

	public static boolean isPrototype(int k) {
		return k == T_PROTOTYPE;
	}

	public static boolean isStruct(int k) {
		return k == T_STRUCT;
	}

	public static boolean isTypedef(int k) {
		return k == T_TYPEDEF;
	}

	public static boolean isUnion(int k) {
		return k == T_UNION;
	}

	public static boolean isVariable(int k) {
		return k == T_VARIABLE;
	}

	public static boolean isExternVar(int k) {
		return k == T_EXTERNVAR;
	}

	public static int value(String flag) {
		if (flag == null) {
			return T_UNKNOWN;
		} else if (flag.equals("class")) {
			return T_CLASS;
		} else if (flag.equals("macro")) {
			return T_MACRO;
		} else if (flag.equals("enumerator")) {
			return T_ENUMERATOR;
		} else if (flag.equals("function")) {
			return T_FUNCTION;
		} else if (flag.equals("enum")) {
			return T_ENUM;
		} else if (flag.equals("member")) {
			return T_MEMBER;
		} else if (flag.equals("namespace")) {
			return T_NAMESPACE;
		} else if (flag.equals("prototype")) {
			return T_PROTOTYPE;
		} else if (flag.equals("struct")) {
			return T_STRUCT;
		} else if (flag.equals("typedef")) {
			return T_TYPEDEF;
		} else if (flag.equals("union")) {
			return T_UNION;
		} else if (flag.equals("variable")) {
			return T_VARIABLE;
		} else if (flag.equals("externvar")) {
			return T_EXTERNVAR;
		} else if (flag.equals("public")) {
			return T_PUBLIC;
		} else if (flag.equals("private")) {
			return T_PRIVATE;
		} else if (flag.equals("protected")) {
			return T_PROTECTED;
		} else if (flag.equals("virtual")) {
			return T_VIRTUAL;
		} else if (flag.equals("abstract")) {
			return T_ABSTRACT;
		} else if (flag.equals("friend")) {
			return T_FRIEND;
		}
		return T_UNKNOWN;
	}

	public static String value(int flag) {
		if (flag == T_CLASS) {
			return "class";
		} else if (flag == T_MACRO) {
			return "macro";
		} else if (flag == T_ENUMERATOR) {
			return "enumerator";
		} else if (flag == T_FUNCTION) {
			return "function";
		} else if (flag == T_ENUM) {
			return "enum";
		} else if (flag == T_MEMBER) {
			return "member";
		} else if (flag == T_NAMESPACE) {
			return "namespace";
		} else if (flag == T_PROTOTYPE) {
			return "prototype";
		} else if (flag == T_STRUCT) {
			return "struct";
		} else if (flag == T_TYPEDEF) {
			return "typedef";
		} else if (flag == T_UNION) {
			return "union";
		} else if (flag == T_VARIABLE) {
			return "variable";
		} else if (flag == T_EXTERNVAR) {
			return "externvar";
		} else if (flag == T_PUBLIC) {
			return "public";
		} else if (flag == T_PRIVATE) {
			return "private";
		} else if (flag == T_PROTECTED) {
			return "protected";
		} else if (flag == T_VIRTUAL) {
			return "virtual";
		} else if (flag == T_ABSTRACT) {
			return "abstract";
		} else if (flag == T_FRIEND) {
			return "friend";
		}
		return null;
	}
}
