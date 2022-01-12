#!/bin/sh
echo `locate libstdc++.a | sort -r | head -1 | sed "s/libstdc++.a$//"`
