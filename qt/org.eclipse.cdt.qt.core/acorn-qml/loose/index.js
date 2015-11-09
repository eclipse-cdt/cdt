/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
'use strict';

// This will only be visible globally if we are in a browser environment
var acornQMLLoose;

(function (mod) {
	if (typeof exports == "object" && typeof module == "object") // CommonJS
		return module.exports = mod(require("./inject.js"), require("acorn"), require("acorn/dist/acorn_loose"));
	if (typeof define == "function" && define.amd) // AMD
		return define(["./inject.js", "acorn", "acorn/dist/acorn_loose"], mod);
	acornQMLLoose = mod(injectQMLLoose, acorn, acorn); // Plain browser env
})(function (injectQMLLoose, acorn, acorn_loose) {
	return injectQMLLoose(acorn);
})