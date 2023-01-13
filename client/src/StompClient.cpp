#include "../include/StompClient.h"
#include "../include/AverageMemeEnjoyer.h"
#include "AverageMemeEnjoyer.cpp"
#include <thread>
#include <stdlib.h>

int main(int argc, char *argv[]) {

	AverageMemeEnjoyer Gaben;
	std::thread inputThread(&AverageMemeEnjoyer::RunKeyboard, &Gaben);
	std::thread outputThread(&AverageMemeEnjoyer::RunSocket, &Gaben);
	if (inputThread.joinable())
		inputThread.join();
	if (outputThread.joinable())
		outputThread.join();
	return 0;
}