#!/usr/bin/perl

# First arg is version number
# The rest are the log files

foreach (<STDIN>)
{
    if (/<!--new..>/)
    {
	$version = shift;
	$date = `date`;
	chomp $date;
	print "  <li>$version - $date</li>\n";
	print "  <ul>\n";
	foreach (@ARGV)
	{
	    $plugin = $_;
	    $plugin =~ s/\.html//;
	    print "    <li><a href=\"logs/$version/$_\">$plugin</a></li>\n"
	}
	print "  </ul>\n";
    }
    print $_;
}
