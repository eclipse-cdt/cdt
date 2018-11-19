#!/bin/bash

find  . -type f ! -name "change_to_eplv2.sh" \
  -exec sed -i -E \
    -e 's@Eclipse Public License v1.0@Eclipse Public License 2.0@g' \
    -e 's@^([[:blank:]]*[^[:blank:]])([[:blank:]]*)(All rights reserved. )@\1\n\1\2@' \
    -e 's@All rights reserved. @@' \
    -e 's@([[:blank:]]*[^[:blank:]])*([[:blank:]]*)http://www.eclipse.org/legal/epl-v10.html@\1\2https://www.eclipse.org/legal/epl-2.0/\n\1\n\1\2SPDX-License-Identifier: EPL-2.0@' \
    -e 's@http://www.eclipse.org/legal/epl-v10.html\&quot;\&gt;http://www.eclipse.org/legal/epl-v10.html\&lt;/a\&gt;@https://www.eclipse.org/legal/epl-2.0\&quot;\&gt;https://www.eclipse.org/legal/epl-v20.html\&lt;/a\&gt;/\n\nSPDX-License-Identifier: EPL-2.0@' \
    -e 's@http://www.eclipse.org/legal/epl-v10.html@https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0@' \
    {} +

# find core/org.eclipse.cdt.ui/src/org/eclipse/cdt/internal/ui/refactoring/extractlocalvariable/ExtractLocalVariableRefactoring.java -type f ! -name "change_to_eplv2.sh" \
#   -exec sed -i -E \
#     {} +

