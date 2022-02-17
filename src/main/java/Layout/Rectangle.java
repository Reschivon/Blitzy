package Layout;

public class Rectangle {
    float x, y, width, height;

    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    void combineWith(Rectangle o) {
        float bottomSup = (o.y - o.height) - (y - height);
        float topSup = o.y - y;
        float leftSup = o.x - x;
        float rightSup = o.x + o.width - (x + width);

        if(bottomSup < 0) {
            height -= bottomSup;
        }

        if(topSup > 0) {
            height += topSup;
            y += topSup;
        }

        if(leftSup < 0) {
            width -= leftSup;
            x += leftSup;
        }

        if(rightSup > 0) {
            width += rightSup;
        }
    }
}
