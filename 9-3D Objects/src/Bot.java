import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.game.GameObject;

/**
    The Bot game object is a small static bot with a turret
    that turns to face the player.
*/
public class Bot extends GameObject {

    private static final float TURN_SPEED = .0005f;
    private static final long DECISION_TIME = 2000;

    protected MovingTransform3D mainTransform;
    protected MovingTransform3D turretTransform;
    protected long timeUntilDecision;
    protected Vector3D lastPlayerLocation;

    public Bot(PolygonGroup polygonGroup) {
        super(polygonGroup);
        mainTransform = polygonGroup.getTransform();
        PolygonGroup turret = polygonGroup.getGroup("turret");
        if (turret != null) {
            turretTransform = turret.getTransform();
        }
        else {
            System.out.println("No turret defined!");
        }
        lastPlayerLocation = new Vector3D();
    }

    public void notifyVisible(boolean visible) {
        if (!isDestroyed()) {
            if (visible) {
                setState(STATE_ACTIVE);
            }
            else {
                setState(STATE_IDLE);
            }
        }
    }

    public void update(GameObject player, long elapsedTime) {
        if (turretTransform == null || isIdle()) {
            return;
        }

        Vector3D playerLocation = player.getLocation();
        if (playerLocation.equals(lastPlayerLocation)) {
            timeUntilDecision = DECISION_TIME;
        }
        else {
            timeUntilDecision-=elapsedTime;
            if (timeUntilDecision <= 0 ||
                !turretTransform.isTurningY())
            {
                float x = player.getX() - getX();
                float z = player.getZ() - getZ();
                turretTransform.turnYTo(x, z,
                    -mainTransform.getAngleY(), TURN_SPEED);
                lastPlayerLocation.setTo(playerLocation);
                timeUntilDecision = DECISION_TIME;
            }
        }
        super.update(player, elapsedTime);
    }
}
