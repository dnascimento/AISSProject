#ifndef ETHCOM_H
#define ETHCOM_H
#define MAX_DATA 1024
#define EPLENGTH (MAX_DATA+32 +4 ) // +32 to accomodate 1 full-block padding
#define EBUFSIZE (EPLENGTH+1000)
#define FPGA_PORT 12120


int EthCypherUDP(FILE* fp_r,char *file_name,int size, char mode);
int EthCypherTCP(FILE* fp_r,char *file_name,int size, char mode);

#endif
