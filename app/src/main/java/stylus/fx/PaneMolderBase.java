package stylus.fx;


import javafx.scene.Node;
import javafx.scene.layout.Pane;

@SuppressWarnings({"unchecked", "unused"})
public interface PaneMolderBase<P extends Pane, Self>
    extends RegionMolderBase<P, Self>
{
    default Self child(Molder<? extends Node> molder) {
        object().getChildren().add(molder.object());
        return (Self)this;
    }

    default Self bareChild(Node node) {
        object().getChildren().add(node);
        return (Self)this;
    }
}
