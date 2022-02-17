package Layout;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public abstract class LayoutBox {

    public enum BoxType {
        COLUMN, ROW, JUSTIFY, TEXT,
    }

    public BoxType type;

    protected ArrayList<LayoutBox> children = new ArrayList<>();
    protected LayoutBox parent = null;

    float marginLeft = 0;
    float marginRight = 0;
    float marginTop = 0;
    float marginBottom = 0;

    ArrayList<Pair<String, Float>> args;
    public LayoutBox(ArrayList<Pair<String, Float>> args) {
        type = BoxType.COLUMN;

        this.args = args;

        if (args != null) {
            // parse args
            for (var arg : args) {
                if (arg.getLeft().equals("left"))
                    marginLeft = arg.getRight();
                if (arg.getLeft().equals("right"))
                    marginRight = arg.getRight();
                if (arg.getLeft().equals("top"))
                    marginTop = arg.getRight();
                if (arg.getLeft().equals("bottom"))
                    marginBottom = arg.getRight();
            }
        }
    }

    public LayoutBox addChild(LayoutBox child) {
        child.parent = this;
        children.add(child);
        return child;
    }

    public LayoutBox getParent() {
        return parent;
    }

//    public double getHeight(double maxWidth);
//    public double getWidth(double maxheight);


    public final Rectangle render(Generator generator, float x, float y, float width, float height) {

        float innerWidth = width - marginLeft - marginRight;
        float innerHeight = height - marginTop - marginBottom;

        var internalBounds = renderInternal(generator,
                x + marginLeft,
                y - marginTop,
                innerWidth,
                innerHeight);

        return new Rectangle(
                internalBounds.x - marginLeft,
                internalBounds.y + marginTop,
                internalBounds.width + marginLeft + marginRight,
                internalBounds.height + marginTop + marginBottom
            );
    }

    public abstract Rectangle renderInternal(Generator generator, float x, float y, float width, float height);

    public abstract Rectangle getMinDimension(float width, float height);

    public void printTree() {
        TreePrint.println(this.toString());
        TreePrint.indent();
        for(var child : children)
            child.printTree();
        TreePrint.unindent();
    }

    public String toString(){
        return type + " " + args;
    }
}
