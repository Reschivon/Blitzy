package Layout;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

/**
 * Centers the shit horizontally
 */
public class JustifyBox extends LayoutBox{
    public JustifyBox(ArrayList<Pair<String, Float>> args) {
        super(args);
        type = BoxType.JUSTIFY;
    }

    @Override
    public Rectangle renderInternal(Generator generator, float x, float y, float width, float height) {
        return null;
    }

    @Override
    public Rectangle getMinDimension(float width, float height) {
        return null;
    }
}
