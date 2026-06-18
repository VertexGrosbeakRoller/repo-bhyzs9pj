package baritone.api;

public class Settings {
    public boolean allowSprint = true;
    public boolean allowJump = true;
    public boolean allowParkour = true;
    public boolean allowPlace = true;
    public boolean allowBreak = true;
    public boolean allowWaterBucketFall = true;
    public boolean allowDiagonalDescend = true;
    public boolean allowDiagonalAscend = true;
    public boolean allowOvershootDiagonalDescend = true;
    public boolean allowDownward = true;
    public boolean freeLook = true;
    public boolean antiCheatCompatibility = true;
    public double costHeuristic = 3.563;
    public int pathingMaxChunkBorderFetch = 50;
    public boolean renderPath = true;
    public boolean renderGoal = true;
    public int primaryColor = 0xFFFF0000;
    public int goalColor = 0xFF00FF00;
    public int nextColor = 0xFF0000FF;
    public double followRadius = 3.0;
    public boolean chatDebug = false;
    public boolean chatControl = true;
    public String chatPrefix = "#";
    public float blockReachDistance = 4.5f;
    public int planAhead = 2;
    public int ticksBetweenRecalc = 20;
    public boolean walkWhileBreaking = true;
    public boolean allowWalkOnBottomSlab = true;
}
