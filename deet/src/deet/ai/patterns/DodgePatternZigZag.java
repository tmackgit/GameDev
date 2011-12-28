package deet.ai.patterns;

import java.util.*;

import deet.bsp2D.BSPTree;
import deet.math3D.Vector3D;
import deet.object.GameObject;
import deet.util.MoreMath;

/**
    An "dodge" pattern that makes the bot zig perpindicular to
    the player, the zag back to the starting location.
*/
public class DodgePatternZigZag extends AIPattern {

    private float dodgeDist;

    public DodgePatternZigZag(BSPTree tree) {
        this(tree, 200);
    }

    public DodgePatternZigZag(BSPTree tree, float dodgeDist) {
        super(tree);
        this.dodgeDist = dodgeDist;
    }


    public Iterator find(GameObject bot, GameObject player) {

        // create the vector to the dodge location
        Vector3D zig = new Vector3D(bot.getLocation());
        zig.subtract(player.getLocation());
        zig.normalize();
        zig.multiply(dodgeDist);
        zig.rotateY((float)Math.PI/2);

        // 50% chance - dodge one way or the other
        if (MoreMath.chance(.5f)) {
            zig.multiply(-1);
        }

        // convert vector to absolute location
        zig.add(bot.getLocation());
        calcFloorHeight(zig, bot.getFloorHeight());

        Vector3D zag = new Vector3D(bot.getLocation());
        calcFloorHeight(zag, bot.getFloorHeight());

        List path = new ArrayList();
        path.add(zig);
        path.add(zag);

        return path.iterator();
    }
}

