package Layout;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.ArrayList;

class Token{
    String text="";
    ArrayList<Token> children = new ArrayList<>();

    Point offset;

    private final PDFont font;
    private double fontSize;

    boolean bold = false;
    boolean italic = false;

    private final Rectangle baseBounds;


    final static float SPACING_RATIO = 1.1f;

    static record Point(float x, float y){
        public Point translate(float x, float y) {
            return new Point(this.x + x, this.y + y);
        }
    };
    static record ParsedModifierText(String modifier, ArrayList<String> arguments){};

    public Token(String s, int fontSize) {
        this(s, new Point(0, 0), fontSize, false, false);
    }
    public Token(String s, Point offset, double fontSize,
                 boolean bold, boolean italic)  {
        this.fontSize = fontSize;
        this.offset = offset;
        this.bold = bold;
        this.italic = italic;


        if(hasModifiers(s)) {
            var parsedModifier = parseModifiers(s);

            System.out.println(parsedModifier);
            switch (parsedModifier.modifier) {

               case "bold" -> children.add(new Token(
                       parsedModifier.arguments.get(0),
                       new Point(0, 0),
                       fontSize, true, italic));

                case "italic" -> children.add(new Token(
                        parsedModifier.arguments.get(0),
                        new Point(0, 0),
                        fontSize, bold, true));

                case "large" -> children.add(new Token(
                        parsedModifier.arguments.get(0),
                        new Point(0, 0),
                        SPACING_RATIO * fontSize, bold, italic));

                case "sum" -> {
                    var summator = new Token(
                            "E",
                            new Point(0, 0),
                            fontSize * 1.4, bold, italic
                    );
                    float verticalAlign = -1f * summator.getHeight() * (1.4f - 1) / 4f;
                    summator.offset = summator.offset.translate(0, verticalAlign);

                    var top = new Token(
                            parsedModifier.arguments.get(0),
                            new Point(0, verticalAlign),
                            fontSize * 0.7f, bold, italic
                    );
                    top.offset = top.offset.translate(summator.getWidth()/2f - top.getWidth()/2f, summator.getHeight() * 0.8f);

                    var bottom = new Token(
                            parsedModifier.arguments.get(1),
                            new Point(0, verticalAlign), // set this after knowing height
                            fontSize * 0.7f, bold, italic
                    );
                    bottom.offset = bottom.offset.translate(summator.getWidth()/2f - bottom.getWidth()/3f, -bottom.getHeight() * 0.8f);

                    children.add(summator);
                    children.add(top);
                    children.add(bottom);
                }

                default -> text = s;
            }

        }else{
            // regular text
            text = s;
        }

        font = getFont(bold, italic);

        float textWidth;
        try {
            textWidth = font.getStringWidth(text) / 1000f * (int)this.fontSize;
        }catch (IOException e) {
            textWidth=0;
        }
        baseBounds = new Rectangle(offset.x, offset.y,
                textWidth,
                font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * (int)this.fontSize);

        // draw children and buff include their rectangles
        for(var child : children)
            baseBounds.combineWith(child.baseBounds);

    }

    private boolean hasModifiers(String s) {
        return s.contains("[");
    }
    /**
     * something like "fraction[3][2]" would return
     * pair("frac", ["3", "2"])
     * @return
     */
    private static ParsedModifierText parseModifiers(String s) {

        ArrayList<String> arguments = new ArrayList<>();
        String modifier;

        StringBuilder buf = new StringBuilder();
        int nesting = 0;
        for(int i = s.length()-1; i >= 0; i--) {
            var currChar = s.charAt(i);

            if(currChar == ']')
                nesting++;
            if(currChar == '[')
                nesting--;
            if(currChar == '[' && nesting == 0) {
                arguments.add(buf.delete(0,1).reverse().toString());
                buf.setLength(0);
            }

            if(nesting > 0)
                buf.append(currChar);
        }

        if(s.contains("["))
            modifier = s.substring(0, s.indexOf('['));
        else
            modifier = s;

        return new ParsedModifierText(modifier, arguments);
    }

    public Rectangle render(PDPageContentStream contentStream,
                       float x, float y) throws IOException {
        try {
            // draw own text
            contentStream.setFont(font, (int)fontSize);
            contentStream.setTextMatrix(Matrix.getTranslateInstance(x + offset.x, y + offset.y));
            contentStream.showText(text);

            TreePrint.indent();;
            for(var child : children) {
                child.render(contentStream, x + offset.x, y + offset.y);
            }
            TreePrint.unindent();;

            baseBounds.x = x;
            baseBounds.y = y;
            return baseBounds;

        } catch (IOException e) {
            System.out.println("well FUCK");
        }

        return null;
    }

    private static PDFont getFont(boolean bold, boolean italic) {
        if(bold && italic)
            return PDType1Font.TIMES_BOLD_ITALIC;
        if(bold)
            return PDType1Font.TIMES_BOLD;
        if(italic)
            return PDType1Font.TIMES_ITALIC;

        return PDType1Font.TIMES_ROMAN;
    }

    public float getWidth() {
        return baseBounds.width;
    }

    public float getHeight() {
        return baseBounds.height;
    }
}
