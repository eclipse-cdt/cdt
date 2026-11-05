// Calls RaceTarget() in a tight loop, so that a breakpoint there is hit
// almost at once after every resume.
volatile int raceCounter = 0;

void RaceTarget()
{
	raceCounter++;
}

int main(int argc, char *argv[])
{
	for (;;) {
		RaceTarget();
	}
	return 0;
}
