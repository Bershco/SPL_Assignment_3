#ifndef AKE
#define AKE
#include "ConnectionHandler.h"
#include "StompClient.h"
#include <vector>

using string = std::string;


class AverageKeyboardEnjoyer {
    public:
    AverageKeyboardEnjoyer();
    void Run();

    string buildFrameConnect(string , string);
    string buildFrameDisconnect();
    string buildFrameSubscribe(string);
    string buildFrameUnsubscribe(string);
    std::vector<string> buildFramesSend(string);
    void generateSummary();
    int idOf(string);
    string nameOf(int);
    int addToList(string);



    ConnectionHandler ch;
    bool connectedProperly;
    std::vector<std::pair<string,int>> gameName_to_Id;
    int sub_id_counter;


};
#endif