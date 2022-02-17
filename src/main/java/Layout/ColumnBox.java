package Layout;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class ColumnBox extends LayoutBox{
    public ColumnBox(ArrayList<Pair<String, Float>> args) {
        super(args);
        type = BoxType.COLUMN;
    }

    @Override
    public Rectangle renderInternal(Generator generator, float x, float y, float width, float height) {
        // TreePrint.println("Column Box rendering at " + x + " " + y );
        //TreePrint.indent();

        float currY = y;
        float remainingHeight = height;

        for(var child : children) {
            var bound = child.render(generator, x, currY, width, remainingHeight);
            currY -= bound.height;
            remainingHeight -= bound.height;
        }

        //TreePrint.unindent();

        return new Rectangle(x, y, width, height - remainingHeight);
    }

    @Override
    public Rectangle getMinDimension(float width, float height) {
        float maxWidth = 0;
        float totalHeight = 0;

        for(var child : children) {
            var dim = child.getMinDimension(width, height);
            if(dim.width > maxWidth)
                maxWidth = dim.width;
            totalHeight += dim.height;
        }

        return new Rectangle(0, 0, maxWidth, totalHeight);
    }
}
