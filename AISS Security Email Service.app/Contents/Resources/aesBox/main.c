#include "util.h"
//#include "counter.h"
#include "com.h"
#include "protocol.h"
//#pragma comment( lib, "wsock32.lib" )     // This will link to wsock32.lib

#define LINEMAX 10
#define SHOW 1
#define HIDE 0
#define USB_MODE 'u'
#define ETH_MODE 'e'

#define ENC_MODE 'c'
#define DEC_MODE 'd'

#define N 2000
#define INC 20

#define MODE FILE
packet_t packet;

int main(int argc, char*argv[])
{
	printf("%d",MAX_DATA_OUT);
	char src_file[100],c;
	char dest_file[100];

	FILE * fp_r,*fp_w;
	int sz;
	char com;

	u32 mode;
	u32 version=1;
	u32 data_len,n;
	u32 inc = 20;

	u8 *Key;
	u32 origkey[8];
	u8 IV[32]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	u8 tv[16]={0x33,0x22,0x11,0x00,0x77,0x66,0x55,0x44,0xBB,0xAA,0x99,0x88,0xFF,0xEE,0xDD,0xCC};
	u8 *data_in;
	u8 buffer_in[MAX_DATA_IN];
    u8 empty_buffer[0];
	u8 buffer_out[MAX_DATA_OUT];
	u32 size_out,i,m,j,size_empty;
    
    size_empty = 0;
	size_out = 0;
	version = 1;
	mode = ROUNDS_10 | CBC_FLAG |FIRST_FLAG| ENCRYPT_FLAG;

		if( argc<3 )
		{
			printf("\nUsage: Demo -mode src_file dest_file \n \n__-mode parameters__ \n\t -c: cypher mode\n\t -d: decryption mode \n");
			return -1;
		}

		if(argv[1][0]='-')
		{
			if(argv[1][1]=='c')
				mode = ROUNDS_10 | CBC_FLAG |FIRST_FLAG| ENCRYPT_FLAG;
			else if(argv[1][1]=='d')
				mode = ROUNDS_10 | CBC_FLAG |FIRST_FLAG | DECRYPT_FLAG;
			else
			{
				printf("\nUsage: Demo -mode src_file dest_file \n \n__-mode parameters__ \n\t -c: cypher mode\n\t -d: decryption mode \n");
				return -1;
			}
		}


		size_out = 0;
		version = 1;

		strcpy(src_file,argv[2]);
		strcpy(dest_file,argv[3]);



		if(	(fp_r = fopen(src_file,"rb")) !=NULL)
		{
				fseek(fp_r, 0L, SEEK_END);
				sz = ftell(fp_r);
				printf("Size %d\n",sz);
				fseek(fp_r, 0L, SEEK_SET);
		}
		else
		{
			printf("\n File %s not found \n",src_file);
			return -1;
		}

		fp_w = fopen(dest_file,"wb");

					/* Initialization*/
					printf("Init mode: %d \n",mode);
					char res = init( mode);
					printf("Init result: %c \n",res);

					//StartCounter();
					while( (data_len = fread(buffer_in,1,MAX_DATA_IN,fp_r))>0)
					{
						if(data_len == ERROR_CODE)
						{
							printf("\n Error : fread() \n");
							return -1;
						}

						if(update(buffer_in,data_len,buffer_out,&n)== ERROR_CODE)
						{
							printf("\n Error : Update() \n");
							return -1;
						}

						fwrite(buffer_out,1,n,fp_w);
					}
					if(doFinal(empty_buffer,size_empty,buffer_out,&n) == ERROR_CODE)
						{
							printf("\n Error : Update() \n");
							return -1;
						}
					if(n>0)
						fwrite(buffer_out,1,n,fp_w);

			


					fclose(fp_r);
					fclose(fp_w);



}
