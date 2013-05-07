#ifndef COM_H
#define COM_H

#define FPGA_PORT 12120


typedef struct sockaddr SOCKADDR;
typedef unsigned char	u8;
typedef unsigned short	u16;
typedef unsigned int	u32;

int  COMinit(); 

int sendPacket(u8 *data, u32 size);
int recvPacket(u8 *buf, u32 size);

#endif

