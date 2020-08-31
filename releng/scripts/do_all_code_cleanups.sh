#!/bin/bash

set -e

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
${DIR}/do_format_code.sh
${DIR}/do_remove_trailing_whitespace.sh
${DIR}/do_add_all_file_types_to_gitattributes.sh
${DIR}/do_project_settings.sh
${DIR}/do_rebuild_natives.sh
