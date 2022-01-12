#include <stdio.h>

int main() {
    printf("Running SpecialTestApp\n");

    const char *path = "/tmp/dsftest.txt";
    const char *mode = "a";
    FILE* fd = fopen(path, mode);
    fprintf(fd, "Running SpecialTestApp\n");
    fclose(fd);
    
    
    return 0;
}

