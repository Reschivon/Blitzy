
import Layout.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    static class MismatchedWhitespaceException extends IOException {}
    /**
     * Returns indent size, adjusting for spaces (4) or tabs
     * @param s the string
     * @return -1 for empty line, else indents (adjusted for space and tabs)
     */
    static int indents(String s) throws MismatchedWhitespaceException {
        char whitespaceChar = 0;

        int i;
        for(i = 0; i < s.length(); i++) {
            var currChar = s.charAt(i);
            if(!Character.isWhitespace(currChar))
                break;

            if(whitespaceChar == 0)
                whitespaceChar = currChar;

            if(currChar != whitespaceChar)
                throw new MismatchedWhitespaceException();
        }

        if(i == s.length())
            return -1; // empty line

        if(whitespaceChar == ' ')
            return i / 4;
        return i;
    }

    static int indexOfSpecifierStart(StringBuilder buf) throws ParseException {
        int nesting = 0;
        int i = buf.length() - 1;
        while (i >= 0) {
            if(buf.charAt(i) == ']')
                nesting++;
            if(buf.charAt(i) == '[')
                nesting--;
            if(nesting == 0 && buf.charAt(i) == ' ')
                break;

            if(nesting < 0 || nesting > 1)
                throw new ParseException("Malformed brackets on specifier parameters");

            i--;
        }

        if(nesting != 0)
            throw new ParseException("Malformed brackets on specifier parameters");

        return i;
    }

    static String extractSpecifierName(String specifier) throws ParseException {
        int chopIndex;
        if(specifier.indexOf('[') == -1)
            chopIndex = specifier.indexOf(':');
        else
            chopIndex = specifier.indexOf('[');

        var specifierName = specifier.substring(0, chopIndex);

        if(specifierName.trim().isEmpty())
            throw new ParseException("Empty Specifier found (or one with only" +
                                        " arguments and no name)");

        return specifierName;
    }

    static ArrayList<Pair<String, Float>> extractSpecifierArgs(String specifier) throws ParseException {
        ArrayList<Pair<String, Float>> ret = new ArrayList<>();

        boolean hasArgs = specifier.contains("]");
        if(!hasArgs)
            return new ArrayList<>();

        // get just the brackets part
        int nameEnd = Math.min(specifier.indexOf(':'), specifier.indexOf('['));
        String args = specifier.substring(nameEnd, specifier.length()-1);

        // split each matching bracket into its own thing
        String[] argSplit = args.split("[\\[\\]]");
        for(var singleArg : argSplit) {
            if(singleArg.trim().isEmpty())
                continue;

            singleArg = singleArg.trim();
            var argDisassembled = singleArg.split(" ");
            if(argDisassembled.length != 2)
                throw new ParseException("Argument \"" + singleArg + "\" should " +
                                        "be formatted [<name> <val>], but was not");

            try {
                ret.add(Pair.of(argDisassembled[0], Float.valueOf(argDisassembled[1])));
            } catch (NumberFormatException e) {
                throw new ParseException("Value of argument is not a valid float");
            }
        }

        return ret;
    }

    private static record ParsedSpecifier(String name, ArrayList<Pair<String, Float>> args){
        @Override
        public String toString() {
            return "ParsedSpecifier{" +
                    "name='" + name + '\'' +
                    ", args=" + args +
                    '}';
        }
    }

    /**
     * Returns a list of objects, which can wither be parsedSpecifier or String
     * parsedSpecifier represents a specifier and its arguments, and String is
     * simple text
     */
    ArrayList<Object> splitBySpecifier(String line) throws ParseException{
        ArrayList<Object> tokens = new ArrayList<>();
        StringBuilder buf = new StringBuilder();

        int nesting = 0;
        for(var character : line.toCharArray()) {
            buf.append(character);

            // Ohh, a possible specifier! Let's look back into the buffer and see
            if(character == ':' && nesting == 0) {
                if (buf.isEmpty())
                    throw new ParseException("Standalone colon without layout specifier name");

                // extract specifier name
                int spaceIndex = Math.max(0, indexOfSpecifierStart(buf));
                if(buf.length() == 0)
                    throw new ParseException("Colon encountered without specifier");

                String specifierWithParams = buf.substring(spaceIndex).trim();
                String specifierName = extractSpecifierName(specifierWithParams);
                if(validLayoutSpecifier(specifierName)) {
                    var args = extractSpecifierArgs(specifierWithParams);

                    // dump regular text portion of buffer
                    buf.setLength(spaceIndex);
                    var trimmedBuffer = buf.toString().trim();
                    if(!trimmedBuffer.isEmpty())
                        tokens.add(trimmedBuffer);
                    buf.setLength(0);

                    // dump specifier data
                    tokens.add(new ParsedSpecifier(specifierName, args));
                }
            }

            if(character == '[')
                nesting++;
            if(character == ']')
                nesting--;

            if(nesting < 0)
                throw new ParseException("Brackets overclosed");
        }

        var trimmedBuffer = buf.toString().trim();
        if(!trimmedBuffer.isEmpty())
            tokens.add(trimmedBuffer);


        if(nesting != 0)
            throw new ParseException("Ended line with unclosed brackets");

        return tokens;
    }

    public boolean validLayoutSpecifier(String s) {
        for(var possibleName : LayoutBox.BoxType.values())
            if(possibleName.name().equalsIgnoreCase(s))
                return true;
        return false;
    }

    public static LayoutBox boxFromString(ParsedSpecifier specifier) throws ParseException {
        return switch (specifier.name()) {
            case "column" -> new ColumnBox(specifier.args());
            case "row" -> new RowBox(specifier.args());
            case "justify" -> new JustifyBox(specifier.args());
            default -> throw new ParseException("Bad specifier name: " + specifier.name());
        };
    }

    static String trimBackSlash(String s) {
        if(s.length() > 0 && s.charAt(s.length()-1) == '\\')
            return s.substring(0, s.length()-1);
        return s;
    }

    private final LayoutBox root = new ColumnBox(null);

    public void parse(String filename) throws IOException {
        var reader = new BufferedReader(new FileReader(filename));

        var currBox = root;
        int nesting = 0;

        mainParse: while (true) {
            // handle backslashes
            StringBuilder currLine = new StringBuilder();
            String lookForward;
            do{
                lookForward = reader.readLine();
                if(lookForward == null)
                    break mainParse;

                if(currLine.isEmpty())
                    // beginning of line, keep indentation for later processing
                    currLine.append(" ").append(trimBackSlash(lookForward));
                else
                    // remove indentation
                    currLine.append(" ").append(trimBackSlash(lookForward).trim());

            } while (lookForward.trim().endsWith("\\"));

            var tokens = splitBySpecifier(currLine.toString());

            if(tokens.size() == 0)
                continue;

            // indent check
            int currLineIndents = indents(currLine.toString());

            if (currLineIndents > nesting)
                throw new ParseException("Extra indents found");

            if (currLineIndents < nesting) {
                // go back up the tree until we reach the right fork
                int difference = nesting - currLineIndents;
                while (difference-- > 0) {
                    currBox = currBox.getParent();
                    nesting--;
                }
            }

            // first layout specifier
            if(tokens.get(0) instanceof ParsedSpecifier) {
                // actually add the specifiers
                for (Object tok : tokens) {
                    if (tok instanceof ParsedSpecifier) {
                        currBox = currBox.addChild(boxFromString((ParsedSpecifier) tok));
                        nesting++;
                    } else {
                        // add text on the same line as specifier
                        // no nesting, this is leaf node
                        currBox.addChild(new TextBox((String) tok));
                    }
                }

            }else{
                // add text that does not share line with specifier
                String text = ((String) tokens.get(0)).trim();
                currBox.addChild(new TextBox(text));
            }

        }

        reader.close();
    }


    public static void rubbify(String inPath, String outPath) {
        var parser = new Parser();
        try {
            parser.parse(inPath);
        }catch (ParseException e) {
            System.out.println("Parsing error, " + e.message);
            return;
        }catch (IOException e) {
            System.out.println("IO excpetion");
            return;
        }

        parser.root.printTree();

        var generator = new Generator();
        generator.addPage();

        float rightPad = 30, leftPad = 50;
        float topPad = 40, bottomPad = 80;

        parser.root.render(generator,
                leftPad,
                generator.pageHeight() - topPad,
                generator.pageWidth() - leftPad - rightPad,
                generator.pageHeight() - topPad - bottomPad);

        try {
            generator.save(outPath);
        } catch (IOException e) {
            System.out.println("SHIT!");
        }
    }

    public static void main(String[] args) {
        String inFile = "syntax_sample";
        String outFile = "output.pdf";

        Parser.rubbify(inFile, outFile);
    }
}
