package client;

import shared.Message;

public interface MessageListener {
    void onMessage(Message msg);
}