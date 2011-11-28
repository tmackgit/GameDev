import java.util.*;

public class BreadthFirstSearchTest {

    public static class Node {
        List neighbors;
        Node pathParent;
        String name;

        public Node(String name) {
            this.name = name;
            neighbors = new LinkedList();
        }

        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        Node nodeF = new Node("F");
        Node nodeG = new Node("G");
        Node nodeH = new Node("H");

        nodeA.neighbors.add(nodeC);
        nodeA.neighbors.add(nodeD);
        nodeA.neighbors.add(nodeE);

        nodeB.neighbors.add(nodeE);

        nodeC.neighbors.add(nodeA);
        nodeC.neighbors.add(nodeD);
        nodeC.neighbors.add(nodeF);

        nodeD.neighbors.add(nodeA);
        nodeD.neighbors.add(nodeC);

        nodeE.neighbors.add(nodeA);
        nodeE.neighbors.add(nodeB);
        nodeE.neighbors.add(nodeG);

        nodeF.neighbors.add(nodeC);
        nodeF.neighbors.add(nodeH);

        nodeG.neighbors.add(nodeE);

        nodeH.neighbors.add(nodeC);
        nodeH.neighbors.add(nodeF);

        BreadthFirstSearchTest bfs = new BreadthFirstSearchTest();
        System.out.println("From A to B: " +
            bfs.search(nodeA, nodeB));
        System.out.println("From C to B: " +
            bfs.search(nodeC, nodeB));
        System.out.println("From G to H: " +
            bfs.search(nodeG, nodeH));
        System.out.println("From A to G: " +
            bfs.search(nodeH, nodeG));
        System.out.println("From A to unknown: " +
            bfs.search(nodeA, new Node("unknown")));
    }

    /**
        Construct the path, not including the start node.
    */
    protected List constructPath(Node node) {
        LinkedList path = new LinkedList();
        while (node.pathParent != null) {
            path.addFirst(node);
            node = node.pathParent;
        }
        return path;
    }

    public List search(Node startNode, Node goalNode) {
        // list of visited nodes
        LinkedList closedList = new LinkedList();

        // list of nodes to visit (sorted)
        LinkedList openList = new LinkedList();
        openList.add(startNode);
        startNode.pathParent = null;

        while (!openList.isEmpty()) {
            Node node = (Node)openList.removeFirst();
            if (node == goalNode) {
                // path found!
                return constructPath(goalNode);
            }
            else {
                closedList.add(node);

                Iterator i = node.neighbors.iterator();
                while (i.hasNext()) {
                    Node neighborNode = (Node)i.next();
                    if (!closedList.contains(neighborNode) &&
                        !openList.contains(neighborNode))
                    {
                        neighborNode.pathParent = node;
                        openList.add(neighborNode);
                    }
                }
            }
        }

        // no path found
        return null;
    }

}
