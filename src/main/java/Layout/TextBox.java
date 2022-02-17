package Layout;

import java.util.ArrayList;

public class TextBox extends LayoutBox {
    ArrayList<Token> text;

    ArrayList<ArrayList<Token>> cachedWrappedText;
    float cachedParagraphWidth;
    
    public TextBox(String text) throws ParseException {
        super(null);
        this.text = tokenize(text);
        type = BoxType.TEXT;
    }

    @Override
    public Rectangle renderInternal(Generator generator, float x, float y, float width, float height) {
        // exactly equal, no elipson
        if(cachedParagraphWidth != width) {
            cachedWrappedText = wrapText(text, width);
        }

        return generator.drawText(cachedWrappedText, x, y);
    }

    @Override
    public Rectangle getMinDimension(float width, float height) {
        cachedWrappedText = wrapText(text, width);
        cachedParagraphWidth = width;

        float pheight = 0;
        float pWidth = 0;
        for(var line : cachedWrappedText) {
            float maxHeight = 0;
            float sumWidth = 0;
            for(var token : line) {
                float lineHeight = token.getHeight();
                if (lineHeight > maxHeight)
                    maxHeight = lineHeight;
                sumWidth += token.getWidth();
            }
            pheight += maxHeight * Token.SPACING_RATIO;
            if(sumWidth > pWidth)
                pWidth = sumWidth;
        }

        return new Rectangle(0, 0, pWidth, pheight);
    }


    public ArrayList<Token> tokenize(String s) throws ParseException {
        int defaultFontSize = 16;
        ArrayList<Token> ret = new ArrayList<>();

        int nesting = 0;
        var buf = new StringBuilder();
        for(int i = 0; i < s.length(); i++) {
            var currChar = s.charAt(i);

            if(currChar == '[')
                nesting++;
            if(currChar == ']')
                nesting--;

            if(nesting < 0)
                throw new ParseException("bracket overclosed in text");

            if(currChar == ' ' && nesting == 0) {
                ret.add(new Token(buf.toString(), defaultFontSize));
                buf.setLength(0);
                ret.add(new Token(" ", defaultFontSize));
            }else {
                buf.append(currChar);
            }

        }

        if(!buf.isEmpty())
            ret.add(new Token(buf.toString(), defaultFontSize));

        return ret;
    }

    public ArrayList<ArrayList<Token>> wrapText(ArrayList<Token> tokens, double maxWidth)  {
        maxWidth -= 20;

        ArrayList<ArrayList<Token>> lines = new ArrayList<>();
        lines.add(new ArrayList<>());

        var lastLine = lines.get(0);
        double totalWidth = 0;
        for(var token : tokens) {
            float tokenWidth = token.getWidth();
            if(totalWidth + tokenWidth >= maxWidth) {
                lines.add(new ArrayList<>());
                lastLine = lines.get(lines.size()-1);
                totalWidth = 0;
            }

            totalWidth += tokenWidth;
            lastLine.add(token);
        }

        return lines;
    }


    @Override
    public String toString() {
        return super.toString() + ": " + text;
    }

}
