package client.core;

import client.networking.Client;
import client.networking.Client1;
import client.networking.FakeClient;
import client.networking.SocketClient;

public class ClientFactory {
    private Client client;

    public Client getClient() {
        if(client == null) {
            client = new Client1();
        }
        return client;
    }

}
