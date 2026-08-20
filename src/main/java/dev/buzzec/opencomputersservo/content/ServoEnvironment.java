package dev.buzzec.opencomputersservo.content;

import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;

public interface ServoEnvironment extends Environment {
    ServoComponent servoComponent();

    @Override
    default Node node() {
        return servoComponent().node();
    }

    @Override
    default void onConnect(Node node) {
    }

    @Override
    default void onDisconnect(Node node) {
    }

    @Override
    default void onMessage(Message message) {
    }
}
