package Layout;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class RowBox extends LayoutBox{
    public RowBox(ArrayList<Pair<String, Float>> args) {
        super(args);
        type = BoxType.ROW;
    }

    @Override
    public Rectangle renderInternal(Generator generator, float x, float y, float width, float height) {
        // TreePrint.println("Row Box rendering at " + x + " " + y );
        //TreePrint.indent();

        float widthPerChild = width / children.size();
        float maxHeight = 0;

        for(int i = 0; i < children.size(); i++) {
            var child = children.get(i);
            var bound = child.render(generator, x + i * widthPerChild, y, widthPerChild, height);

            if(bound.height > maxHeight)
                maxHeight = bound.height;
        }

        //TreePrint.unindent();

        return new Rectangle(x, y, width, maxHeight);
    }

    @Override
    public Rectangle getMinDimension(float width, float height) {
        float minWidth = 0;
        float maxHeight = 0;

        for(var child : children) {
            var dim = child.getMinDimension(width, height);
            minWidth += dim.width;
            if(dim.height > maxHeight)
                maxHeight = dim.height;
        }

        return new Rectangle(0, 0, minWidth, maxHeight);
    }
}
