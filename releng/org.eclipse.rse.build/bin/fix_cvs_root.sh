#!/bin/sh
echo ""
echo "Replace /cvsroot/dsdp -> /cvsroot/tools in all CVS/Root files below current directory"
echo ""
if [ "$1" != "-go" ]; then
  echo "use -go to actually perform the operation."
  exit 0
fi
for file in `find . -name Root -print | grep 'CVS/Root$'` ; do
  echo $file :
  if [ -f "$file.new" ]; then rm -rf "$file.new"; fi
  sed -e 's,/cvsroot/dsdp,/cvsroot/tools,g' "$file" > "$file.new"
  mv -f "$file.new" "$file"
done
