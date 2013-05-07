#ifndef PROTOCOL_H
#define PROTOCOL_H


#include <string.h>
#include "com.h"

#define RESET_CODE 		0
#define INIT_CODE  		1
#define UPDATE_CODE		2
#define DOFINAL_CODE 	3
#define DATA_CONT_CODE 	5
#define DATA_COMP_CODE  6
#define STATUS_CODE		7
#define STATUS_OK_CODE		8
#define ERROR_CODE 		-1


#define AES_BLOCK_SIZE 32 /* 256 bit AES */
#define MIN_BLOCK_NUM  20 /* 640 byte  : minimum length of outgoing packet */
#define MAX_BLOCK_NUM  43//44 /* 1408 byte : maximum length of outgoing packet*/

#define MIN_PACKET_DATA (MIN_BLOCK_NUM*AES_BLOCK_SIZE)
#define MAX_PACKET_DATA (MAX_BLOCK_NUM*AES_BLOCK_SIZE) /* 1514 - 6 (packet_t header) - 42 (ip/udp header) */
#define MAX_PACKET_RAW_DATA (45*AES_BLOCK_SIZE + PACKET_HEADER_SIZE)
#define MAX_DATA_IN  (MAX_PACKET_DATA*50)
#define MAX_DATA_OUT (MAX_DATA_IN + AES_BLOCK_SIZE)

#define ENDIANNESS LITTLE_ENDIAN

#define PACKET_HEADER_SIZE 6


#define KEY_SIZE 	32
#define IV_SIZE 	32
#define VERSION_SIZE 4
#define MODE_SIZE 	4
#define INIT_SIZE  (KEY_SIZE+IV_SIZE+VERSION_SIZE+MODE_SIZE)


#define EBC_FLAG 	 0x00000000
#define CBC_FLAG 	 0x00000200
#define FIRST_FLAG 	 0x00000400
#define DECRYPT_FLAG 0x00000100
#define ENCRYPTION_MASK 0x00000100
#define ENCRYPT_FLAG 0x00000000
#define ROUNDS_10	 0x0A
#define ROUNDS_12	 0x0C
#define ROUNDS_14	 0x0D




/** @brief Structure of the packet at the application level */
typedef union packet_t_
{

	struct{
		/** @brief Packet type*/
			char code_op[2];
		/** @brief Packet data size*/
            u8 size[4];
		/** @brief Packet data as byte array (extra 2 packets for random CBC and padding) */
			u8 data[MAX_PACKET_RAW_DATA - PACKET_HEADER_SIZE];
		} /** @brief Packet structured by fields*/	 field;


	struct{
		/** @brief Packet type*/
			char code_op[2];
		/** @brief Packet data size*/
			u8 size[4];
		/** @brief Application version*/
			u8 version[4];
		/** @brief Cyphering mode*/
			u8 mode[4];
		/** @brief CBC initialization vector*/
			u8 IV[IV_SIZE];
		/** @brief AES key */
			u8 Key[KEY_SIZE];
		} param;

	/** @brief Packet raw data  (byte array), header + data + extra 2 packets (random CBC and padding)*/
	//u8 raw[MAX_PACKET_DATA + PACKET_HEADER_SIZE + AES_BLOCK_SIZE*2];
	u8 raw[MAX_PACKET_RAW_DATA];
} packet_t;



/** @brief Initialization function

	This function initiates a new transfer from the PC to the board, indicating the
	necessary parameters to control the FPGA processing.

	@param version Application version number identifier
	@param mode AES Core processing mode
	@param IV Initialization Vector for AES CBC mode

	@return Returning code from the acknowledge packet
*/
char init(u32 mode);


/** @brief Update function

	This function updates the processing core with a data block to be cyphered.
	However, calling this function does not imply that a transfer will occur
	since the function employs internal buffering.

	@param data_in Input data
	@param size Input data length
	@param data_out Output data buffer
	@param rbytes Number of bytes correctly received (return parameter)
	@param fin_code Final code_op that is to be sent with the last packet

	@return Returning code from the last acknowledge + reply packet
*/

char  update_int(u8 * data_in, u32 size, u8 * data_out,u32 * rbytes, char fin_code);


/** @brief Update internal function

	This function is redirected to the complete Update function.
	It calls the Update function with fin_code = UPDATE_CODE, bypassing
	all the other parameters.

	@param data_in Input data
	@param size Input data length
	@param data_out Output data buffer
	@param size_out Output data length (return parameter)

	@return Returning code from the last acknowledge + reply packet
*/
char  update(u8 * data_in, u32 size, u8 * data_out,u32 * size_out);


/** @brief Dofinal function with no input data

	This function is the last one to be called when cyphering a given
	file or stream of data. It will first send the remaining buffered
	data, and then ask for the cyphered data (padded if needed).

	@param data_out Output data buffer
	@param size_out Output data length (return parameter)

	@return Returning code from the last acknowledge + reply packet
*/
//char  doFinal(u8 * data_out,u32 *size_out);


/** @brief Dofinal function with a last chunk of input data

	This function is the last one to be called when cyphering a given
	file or stream of data. It will first update the last block of input
	data and then will call the other version of the Dofinal function,
	with no input data.

	@param data_in Input data
	@param size Input data length
	@param data_out Output data buffer
	@param size_out Output data length (return parameter)

	@return Returning code from the last acknowledge + reply packet
*/
char  doFinal(u8 * data_in, u32 size,u8 * data_out,u32 *size_out);


/** @brief Get Data from a formatted packet

	This function will read the header info of the packet and write
	the packet data into the buffer and the valid number of bytes
	received into the size variable. Finally, it returns the code
	indicating the type of the packet.

	@param p packet as byte array
	@param data buffer
	@param size buffer length

	@return  Code of the packet
*/
char get_packet_data(packet_t * p, u8 * data, u32 *size);


/** @brief Create formatted packet for the initialization

	This function will generate the header info of the initialization packet
	and write all the necessary parameters to the packet data field.


	@param p 		Output packet
	@param version 	Application version
	@param mode 	AES cyphering mode
	@param Key 		AES Key
	@param Key 		AES Initialization Vector for CBC mode of operation
	@param code 	Packet coded type

	@return  none.
*/
void form_packet_init(packet_t * p,u32 mode, char code);


/** @brief Create formatted packet for a data transfer

	This function will generate the header info of a data packet, namely
	the size and type of the packet.

	@param p 		Output packet
	@param data 	Data as byte array
	@param size 	Number of bytes to be sent
	@param code 	Packet coded type

	@return  none.
*/
void  form_packet(packet_t * p, u8 * data, u32 size, char  code);

void  form_packet_header(packet_t * p, u32 size,char code);

u16 parse_packet(packet_t * p);

void reset( );

 #endif

