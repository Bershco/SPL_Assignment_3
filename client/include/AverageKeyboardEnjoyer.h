#ifndef AKE
#define AKE
#include "ConnectionHandler.h"
#include "StompClient.h"
#include <vector>
#include <unordered_map>
#include <queue>

using string = std::string;
class Event;


class AverageKeyboardEnjoyer {
    public:
    AverageKeyboardEnjoyer();
    void Run();

    string buildFrameConnect(string , string);
    string buildFrameDisconnect();
    string buildFrameSubscribe(string);
    string buildFrameUnsubscribe(string);
    std::vector<string> buildFramesSend(string);
    void generateSummary(string,string,string);
    int idOf(string);
    string nameOf(int);
    int addToList(string);


    std::unordered_map<string,std::queue<Event>> eventByUser; //TODO implement the insertion of the maps to this queue
    ConnectionHandler ch;
    bool connectedProperly;
    std::vector<std::pair<string,int>> gameName_to_Id;
    int sub_id_counter;


};

struct compareLexicographically {
    bool operator()(const std::pair<string,string>& a, const std::pair<string,string>& b) const {
        return a.first < b.first;
    }
};
#endif