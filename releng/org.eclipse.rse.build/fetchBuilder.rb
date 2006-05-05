#!/usr/bin/ruby
STDERR.print("Which tag do you want to fetch? (default is HEAD): ")
answer = readline().strip
tag = answer.empty? ? "HEAD" : answer

command = "cvs "
command += "-d :pserver:anonymous:none@dev.eclipse.org:/cvsroot/dsdp "
command += "checkout "
command += "-r #{tag} "
command += "-d builder "
command += "org.eclipse.tm.rse/releng/org.eclipse.rse.build "

system(command)

puts()
puts("Builder has been fetched and is in the builder subdirectory")
