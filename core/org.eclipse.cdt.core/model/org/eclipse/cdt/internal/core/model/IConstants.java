package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * This interface defines constants for use by the builder / compiler interface.
 */
public interface IConstants {

	/*
	 * Modifiers
	 */
	int AccPublic = 0x0001;
	int AccPrivate = 0x0002;
	int AccProtected = 0x0004;
	int AccStatic = 0x0008;
	int AccExtern = 0x0010;
	int AccInline = 0x0020;
	int AccVolatile = 0x0040;
	int AccRegister = 0x0080;
	int AccExplicit = 0x0100;
	int AccExport = 0x0200;
	int AccAbstract = 0x0400;
	int AccMutable = 0x0800;

	/*
	 * Other VM flags.
	 */
	int AccAuto = 0x0020;

	/**
	 * Extra flags for types and members.
	 */
	int AccVirtual = 0x20000;
	int AccTypename = 0x100000;
}
