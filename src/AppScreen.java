import javafx.scene.Node;

public abstract class AppScreen {
    private Node viewNode = null;

    public Node getViewNode() {
        return this.viewNode;
    }
}
