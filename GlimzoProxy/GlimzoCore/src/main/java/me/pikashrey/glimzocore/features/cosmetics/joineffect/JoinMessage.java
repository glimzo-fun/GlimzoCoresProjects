package me.pikashrey.glimzocore.features.cosmetics.joineffect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JoinMessage {

    // Default list - replaced at runtime by messages.yml if present
    private static final List<String> messages = new CopyOnWriteArrayList<>(Arrays.asList(
            "landed here from mars",
            "landed here from moon",
            "entered the grill",
            "escaped the Jail",
            "arrived after great session in beauty parlour",
            "is in a rage",
            "landed to dominate",
            "stopped cheating now",
            "didnt stop cheating yet",
            "is another AI model",
            "is another AI agent",
            "took a flight in Emirates airlines to Arrive here"
    ));

    /** Returns the current (possibly config-updated) message list. */
    public static List<String> getAll() {
        return Collections.unmodifiableList(messages);
    }

    public static void updateFromConfig(List<String> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
    }

    // Legacy static field kept for source compatibility with any code referencing JoinMessage.ALL
    @Deprecated
    public static final List<String> ALL = messages;

    private JoinMessage() {}
}
