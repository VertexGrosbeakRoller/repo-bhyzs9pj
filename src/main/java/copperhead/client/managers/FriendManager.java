package copperhead.client.managers;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static final List<String> friends = new ArrayList<>();

    public static boolean isFriend(String name) {
        return friends.stream().anyMatch(f -> f.equalsIgnoreCase(name));
    }

    public static void addFriend(String name) {
        if (!isFriend(name)) {
            friends.add(name);
        }
    }

    public static void removeFriend(String name) {
        friends.removeIf(f -> f.equalsIgnoreCase(name));
    }

    public static List<String> getFriends() {
        return friends;
    }
}
