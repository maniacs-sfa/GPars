package org.gparallelizer.actors.pooledActors;

import org.gparallelizer.actors.ActorMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import static java.lang.Math.*;

/**
 * Buffers messages for the next continuation of an event-driven actor, handles timeouts and no-param continuations.
 *
 * @author Vaclav Pech
 * Date: May 22, 2009
 */
@SuppressWarnings({"InstanceVariableOfConcreteClass"})
public final class MessageHolder {
    private final int numberOfExpectedMessages;
    private int currentSize = 0;
    private final ActorMessage[] messages;
    private boolean timeout = false;

    /**
     * Creates a new instance.
     * @param numberOfExpectedMessages The number of messages expected by the next continuation. If zero, the buffer
     * will still wait for a message, but return an empty message list from the getMessages() method.
     */
    MessageHolder(final int numberOfExpectedMessages) {
        this.numberOfExpectedMessages = max(1, numberOfExpectedMessages);  //the numberOfExpectedMessages field cannot be zero
        messages = new ActorMessage[this.numberOfExpectedMessages];
    }

    /**
     * Retrieves the current number of messages in the buffer.
     * @return The curent buffer size
     */
    public int getCurrentSize() { return currentSize; }

    /**
     * Indicates, whether a timeout message is held in the buffer
     * @return True, if a timeout event has been detected.
     */
    public boolean isTimeout() { return timeout; }

    /**
     * Indicates whether the buffer contains all the messages required for the next continuation.
     * @return True, if the next continuation can start.
     */
    public boolean isReady() {
        return timeout || getCurrentSize() == numberOfExpectedMessages;
    }

    /**
     * Adds a new message to the buffer.
     * @param message The message to add.
     */
    public void addMessage(final ActorMessage message) {
        if (isReady()) throw new IllegalStateException("The MessageHolder cannot accept new messages when ready");
        messages[currentSize] = message;
        currentSize++;
        if (message.getPayLoad().equals(ActorException.TIMEOUT)) timeout = true;
    }

    /**
     * Retrieves messages for the next continuation once the MessageHolder is ready.
     * @return The messages to pass to the next continuation.
     */
    public List<ActorMessage> getMessages() {
        if (!isReady()) throw new IllegalStateException("Cannot build messages before being in the ready state");
        return Collections.unmodifiableList(Arrays.asList(messages));
    }

    /**
     * Dumps so far stored messages. Useful on timeout to restore the already delivered messages
     * to the afterStop() handler in the PooledActor's sweepQueue() method..
     * @return The messages stored so far.
     */
    List<ActorMessage> dumpMessages() {
        return Collections.unmodifiableList(Arrays.asList(messages));
    }
}