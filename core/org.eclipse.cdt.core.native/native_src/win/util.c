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
#include <tchar.h>

static bool spawner = false;
static bool spawnerDetails = false;
static bool spawnerStarter = false;
static bool readReport = false;

void enableTraceFor(const TraceKind_t traceKind) {
    switch (traceKind) {
    case CDT_TRACE_SPAWNER:
        spawner = true;
        break;
    case CDT_TRACE_SPAWNER_DETAILS:
        spawnerDetails = true;
        break;
    case CDT_TRACE_SPAWNER_STARTER:
        spawnerStarter = true;
        break;
    case CDT_TRACE_SPAWNER_READ_REPORT:
        readReport = true;
        break;
    default:
        cdtTrace(L"%S: Invalid trace kind supplied: %d\n", __func__, traceKind);
        break;
    }
}

bool isTraceEnabled(const TraceKind_t traceKind) {
    switch (traceKind) {
    case CDT_TRACE_SPAWNER:
        return spawner;
    case CDT_TRACE_SPAWNER_DETAILS:
        return spawnerDetails;
    case CDT_TRACE_SPAWNER_STARTER:
        return spawnerStarter;
    case CDT_TRACE_SPAWNER_READ_REPORT:
        return readReport;
    default:
        cdtTrace(L"%S: Invalid trace kind supplied: %d\n", __func__, traceKind);
        return false;
    }
}

void cdtTrace(const wchar_t *fmt, ...) {
    va_list ap;
    wchar_t *buffer = NULL;
    int size = 0;

    va_start(ap, fmt);

    do {
        // Free previous buffer
        free(buffer);

        // Allocate a slightly larger buffer
        size += 256;
        buffer = (wchar_t *)malloc(size * sizeof(wchar_t));

        if (!buffer) {
            // malloc failed
            OutputDebugStringW(L"Failed to allocate buffer to format message into.\n");
            va_end(ap);
            return;
        }
    } while (-1 == vswprintf(buffer, size, fmt, ap) && errno == ERANGE);
    va_end(ap);

    // Send the output
    OutputDebugStringW(buffer);

    // Clean up
    free(buffer);
}

int copyTo(wchar_t *target, const wchar_t *source, int cpyLength, int availSpace) {
    bool bSlash = false;
    int i = 0, j = 0;

    enum { QUOTATION_DO, QUOTATION_DONE, QUOTATION_NONE } nQuotationMode = QUOTATION_DO;

    if (availSpace <= cpyLength) { // = to reserve space for final '\0'
        return -1;
    }

    if ((_T('\"') == *source) && (_T('\"') == *(source + cpyLength - 1))) {
        nQuotationMode = QUOTATION_DONE;
    } else if (wcschr(source, _T(' '))) {
        // Needs to be quoted
        nQuotationMode = QUOTATION_DO;
        if (target) {
            *target = _T('\"');
        }
        ++j;
    } else {
        // No reason to quote term because it doesn't have embedded spaces
        nQuotationMode = QUOTATION_NONE;
    }

    for (; i < cpyLength; ++i, ++j) {
        if (source[i] == _T('\\')) {
            bSlash = true;
        } else {
            // Don't escape embracing quotation marks
            if ((source[i] == _T('\"')) &&
                !((nQuotationMode == QUOTATION_DONE) && ((i == 0) || (i == (cpyLength - 1))))) {
                if (!bSlash) { // If still not escaped
                    if (j == availSpace) {
                        return -1;
                    }
                    target[j] = _T('\\');
                    ++j;
                }
            }
            bSlash = false;
        }

        if (j == availSpace) {
            return -1;
        }
        if (target) {
            target[j] = source[i];
        }
    }

    if (nQuotationMode == QUOTATION_DO) {
        if (j == availSpace) {
            return -1;
        }
        if (target) {
            target[j] = _T('\"');
        }
        ++j;
    }

    return j;
}
