import java.util.*;

import com.brackeen.javagamebook.ai.*;
import com.brackeen.javagamebook.ai.pattern.*;
import com.brackeen.javagamebook.bsp2D.*;
import com.brackeen.javagamebook.game.*;
import com.brackeen.javagamebook.math3D.*;
import com.brackeen.javagamebook.path.*;
import com.brackeen.javagamebook.shooter3D.*;

public class EvolutionTest extends PathFindingTest {

    private EvolutionGenePool genePool;

    public static void main(String[] args) {
        new EvolutionTest(args, "../images/sample3.map").run();
    }

    public EvolutionTest(String[] args, String defaultMap) {
        super(args, defaultMap);
    }

    public void stop() {
        super.stop();

        // print information about the "brains" in the gene pool.
        System.out.println(genePool);
    }

    protected void createGameObjects(List mapObjects) {

        drawInstructions = false;
        MessageQueue queue = MessageQueue.getInstance();
        addOverlay(queue);
        addOverlay(new HeadsUpDisplay(
            (Player)gameObjectManager.getPlayer()));
        queue.setDebug(true);
        queue.add("Use the mouse/arrow keys to move.");
        queue.add("Press Esc to exit.");

        genePool = new EvolutionGenePool(bspTree);

        Iterator i= mapObjects.iterator();
        while (i.hasNext()) {
            PolygonGroup group = (PolygonGroup)i.next();
            String filename = group.getFilename();
            if (filename != null && filename.endsWith("bot.obj")) {

                EvolutionBot bot = new EvolutionBot(group,
                    collisionDetection, genePool,
                    botProjectileModel);
                bot.setRegenerating(true);
                gameObjectManager.add(bot);
            }
            else {
                // static object
                gameObjectManager.add(new GameObject(group));
            }
        }
    }

}
