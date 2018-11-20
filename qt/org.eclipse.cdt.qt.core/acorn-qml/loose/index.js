/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
(function (mod) {
	if (typeof exports == "object" && typeof module == "object") // CommonJS
		return mod(require("./inject.js"), require("acorn"), require("acorn/dist/acorn_loose"));
	if (typeof define == "function" && define.amd) // AMD
		return define(["./inject.js", "acorn", "acorn/dist/acorn_loose"], mod);
	mod(acornQMLLooseInjector, acorn, acorn); // Plain browser env
})(function (acornQMLLooseInjector, acorn, acorn_loose) {
	"use strict";

	acornQMLLooseInjector.inject(acorn);
});