#ifndef AKE
#define AKE
#include "ConnectionHandler.h"
#include "StompClient.h"
#include <vector>
#include <unordered_map>
#include <queue>
#include <utility>

using string = std::string;
class Event;


class AverageMemeEnjoyer {
    public:
    AverageMemeEnjoyer();
    void RunKeyboard();
    void RunSocket();

    string buildFrameConnect(string , string);
    string buildFrameDisconnect();
    string buildFrameSubscribe(string);
    string buildFrameUnsubscribe(string);
    std::vector<string> buildFramesSend(string);
    void generateSummary(string,string,string);
    int idOf(string);
    string nameOf(int);
    int addToList(string);
    void addReceipt(string);
    string getMsgFromReceipt(int);
    std::queue<std::pair<string,string>> order_lex(std::map<string,string>&);
    ConnectionHandler& getCH();
    void decodeFrameConnected();
    void decodeFrameReceipt(string);
    void decodeFrameMessage(string);
    void decodeFrameError(string);

    
    std::unordered_map<string,std::queue<Event>> eventByUser; //TODO implement the insertion of the maps to this queue
    ConnectionHandler ch;
    std::map<int,string> sub_id_to_game_name;
    int sub_id_counter;
    std::map<int,string> receipt_id_to_msg;
    
    bool closeSocketThread;
};
#endif