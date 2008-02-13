#include <stdio.h>

int main() {
    printf("Running Generic App\n");
    
    const char *path = "/tmp/dsftest.txt";
    const char *mode = "a";
    FILE* fd = fopen(path, mode);
    fprintf(fd, "Running Generic App\n");
    fclose(fd);
    
    
    return 0;
}

