#include <iostream>
#include "ConnectionHandler.h"
#include <thread>
#include <utility>
using string = std::string;


class StompClient {
    public:
    static string buildFrameConnect(string , string);
    static string buildFrameDisconnect();
    static string buildFrameSubscribe(string);
    static string buildFrameUnsubscribe(string);
    static string buildFrameSend();
    static void generateSummary();
    static int idOf(string);
    static string nameOf(int);
    static int addToList(string);

    private:
    static std::vector<std::pair<string,int>> gameName_to_Id;
    static int sub_id_counter;
};