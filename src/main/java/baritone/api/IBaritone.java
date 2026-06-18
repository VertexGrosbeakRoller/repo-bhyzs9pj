package baritone.api;

import baritone.pathing.PathingBehavior;
import baritone.pathing.MineProcess;
import baritone.pathing.FollowProcess;
import baritone.pathing.GoToProcess;

public interface IBaritone {
    Settings getSettings();
    PathingBehavior getPathingBehavior();
    MineProcess getMineProcess();
    FollowProcess getFollowProcess();
    GoToProcess getGoToProcess();
}
