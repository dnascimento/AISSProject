#include <stdio.h>
#include <stdlib.h>
#include "protocol.h"

char init(u32 mode);
char  update(u8 * data_in, u32 size, u8 * data_out,u32 * size_out);
char  doFinal(u8 * data_in, u32 size,u8 * data_out,u32 *size_out);
