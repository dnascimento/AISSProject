#include "util.h"
/*Optimized version Offset =2*/
#define  OFFSET 0

char fpgaIP[] = {'1','9','2','.','1','6','8','.','0','.','1','4','\0'} ;        /* Server IP address XXX.XXX.XXX.XXX */
WSADATA wsaData;															    /* Structure for WinSock setup communication */
int fd ;																		/* socket file descriptor*/ 
struct sockaddr_in fpgaAddr;												 /* Structure for Socket address */
unsigned char buf[EBUFSIZE];




void DieWithError(char *errorMessage)
{
    fprintf(stderr,"%s: %d\n", errorMessage, WSAGetLastError());
	
    exit(1);
}




int EthCypherUDP(FILE* fp_r,char *file_name,int size, char mode)
{
    int i,iResult,data_len,bsent,brecv,total_bsent,first;
    WSADATA wsaData;
    SOCKET RecvSocket = INVALID_SOCKET,SendSocket = INVALID_SOCKET;
    struct sockaddr_in SendAddr,RecvAddr;
	struct sockaddr_in SenderAddr;
    unsigned short Port = 27015;
    int BufLen = EPLENGTH;
	 int SenderAddrSize = sizeof (SenderAddr);
	 int max_padding;
	unsigned char block_s[EPLENGTH],block_r[EPLENGTH+2];
	FILE *fp_w;

	fp_w = fopen(file_name,"wb");
   //----------------------
    // Initialize Winsock
    iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
    if (iResult != NO_ERROR) {
        printf("WSAStartup failed with error: %d\n", iResult);
        return -1;
    }

    //---------------------------------------------
    // Create a socket for sending data
    SendSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    if (SendSocket == INVALID_SOCKET) {
        printf("socket failed with error: %ld\n", WSAGetLastError());
        WSACleanup();
        return -1;
    }
    SendAddr.sin_family = AF_INET;
    SendAddr.sin_port = htons(FPGA_PORT);
    SendAddr.sin_addr.s_addr = inet_addr(fpgaIP);


    //-----------------------------------------------
    // Bind the socket to any address and the specified port.
    RecvAddr.sin_family = AF_INET;
    RecvAddr.sin_port = htons(Port);
    RecvAddr.sin_addr.s_addr = htonl(INADDR_ANY);

    iResult = bind(SendSocket, (SOCKADDR *) & RecvAddr, sizeof (RecvAddr));
    if (iResult != 0) {
        printf("bind failed with error %d\n", WSAGetLastError());
        return -1;
    }
	i = size;
	
	first =1;
	max_padding = mode =='d'? 32 : 0; // + 32 Full0block padding

	while((data_len=fread(block_s+4,1,MAX_DATA+max_padding,fp_r)) !=0) 
	{
		if(mode == 'c')
		{
			data_len += InsPadding(block_s+4,data_len);
			block_s[1] = first == 1? 0x06 : 0x02; // Encryption mode
		}
		else if(mode == 'd')
			block_s[1] = first == 1 ?  0x07 : 0x03; //Decryption mode
		else
		{
			printf("\nUsage: Demo -com -mode src_file dest_file \n \n__-com parameters__ \n\t -e: Ethernet \n\t -u: USB \n\n__-mode parameters__ \n\t -c: cypher mode\n\t -d: decryption mode \n");
			exit(-1);
		}	
				
		block_s[0] = 0x0A;
		block_s[2] = 0xFF;
		block_s[3] = 0xFF;

		data_len += 4;
		total_bsent = 0;
		first =0;
		
	 do {

		iResult = sendto(SendSocket,&block_s[total_bsent],data_len-total_bsent, 0, (SOCKADDR *) & SendAddr, sizeof (SendAddr));
		
		if (iResult == SOCKET_ERROR) {
			printf("sendto failed with error: %d\n", WSAGetLastError());
			closesocket(SendSocket);
			WSACleanup();
			return -1;
		}
		bsent = iResult;

		brecv=0;
		while(brecv<bsent-4)  
		{
		
		 iResult = recvfrom(SendSocket,block_r,sizeof(block_r), 0, (SOCKADDR *) & RecvAddr, &SenderAddrSize);
		 iResult = recvfrom(SendSocket,block_r,sizeof(block_r), 0, (SOCKADDR *) & RecvAddr, &SenderAddrSize);
		iResult -=OFFSET; /** WARNING !!!!!!!!!!!!!!!!**/

		if(iResult == SOCKET_ERROR) 
			{	
				printf("recvfrom failed with error %d\n", WSAGetLastError());
				return -1;
			}
		 if(mode == 'c')
			 fwrite(&block_r[OFFSET],1,iResult,fp_w);  /** WARNING !!!!!!!!!!!!!!!!**/
		 else if(mode=='d')
			fwrite(&block_r[OFFSET],1,iResult-RemPadding(&block_r[OFFSET],iResult),fp_w);  /** WARNING !!!!!!!!!!!!!!!!**/
		 brecv += iResult;
		 
		}
		 
		 total_bsent += bsent;
		 }while(bsent<data_len);	
	}
	//---------------------------------------------
    // When the application is finished sending, close the socket
	fclose(fp_w);
    printf("Finished receiving. Closing socket.\n");
    iResult = closesocket(SendSocket);
    if (iResult == SOCKET_ERROR) {
        printf("closesocket failed ith error: %d\n", WSAGetLastError());
        WSACleanup();
        return -1;
    }

    //---------------------------------------------
    // Clean up and quit.
    printf("Exiting.\n");
    WSACleanup();
	return 0;

}

