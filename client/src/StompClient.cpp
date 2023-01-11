#include "../include/StompClient.h"
#include "../include/AverageKeyboardEnjoyer.h"
#include "AverageKeyboardEnjoyer.cpp"
#include <thread>
#include <stdlib.h>

int main(int argc, char *argv[]) {

	AverageKeyboardEnjoyer ake;
	//AverageSocketEnjoyer ase();
	std::thread inputThread(&AverageKeyboardEnjoyer::Run, &ake);
	//std::thread outputThread(&AverageSocketEnjoyer::Run, &ase);
	if (inputThread.joinable())
		inputThread.join();
	//if (outputThread.joinable())
		//outputThread.join();
	return 0;
}