/*******************************************************************************
 * Copyright (c) 2020 Torbjörn Svensson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Torbjörn Svensson - initial API and implementation
 *******************************************************************************/

#include "util.h"

#include <stdio.h>

bool isTraceEnabled(const TraceKind_t traceKind) {
    static bool initialized = false;
    static bool monitor = false;
    static bool monitorDetails = false;
    static bool readReport = false;

    if (!initialized) {
        monitor = _wgetenv(L"TRACE_ORG_ECLIPSE_CDT_MONITOR") != NULL;
        monitorDetails = _wgetenv(L"TRACE_ORG_ECLIPSE_CDT_MONITORDETAILS") != NULL;
        readReport = _wgetenv(L"TRACE_ORG_ECLIPSE_CDT_READREPORT") != NULL;

        initialized = true;
    }

    switch (traceKind) {
    case CDT_TRACE_MONITOR:
        return monitor;
    case CDT_TRACE_MONITOR_DETAILS:
        return monitorDetails;
    case CDT_TRACE_READ_REPORT:
        return readReport;
    default:
        cdtTrace(L"Invalid trace kind supplied: %d\n", traceKind);
        return false;
    }
}

void cdtTrace(const wchar_t *fmt, ...) {
    va_list ap;
    wchar_t *buffer = NULL;
    int size = 0;
    int required = 0;

    va_start(ap, fmt);

    do {
        // Free previous buffer
        free(buffer);

        // Allocate a slightly larger buffer
        size += 256;
        buffer = (wchar_t *)malloc(size * sizeof(wchar_t));

        if (buffer) {
            // Try to format the string
            required = vswprintf(buffer, size, fmt, ap);
        } else {
            // malloc failed
            OutputDebugStringW(L"Failed to allocate buffer to format message into.\n");
            break;
        }
    } while (size <= required);

    // Clean up
    free(buffer);
    va_end(ap);
}
