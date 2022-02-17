package Layout;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.ArrayList;

public class Generator {
    PDDocument document = new PDDocument();
    PDPage currPage = null;

    public void save(String path) throws IOException {
        document.save(path);
    }

    public void addPage() {
        currPage = new PDPage();
        document.addPage(currPage);
    }

    public float pageWidth() {
        return currPage.getMediaBox().getWidth();
    }

    public float pageHeight() {
        return currPage.getMediaBox().getHeight();
    }

   
    public Rectangle drawText(ArrayList<ArrayList<Token>> paragraph, float x, float y){

        try(var contentStream = new PDPageContentStream(
                document, currPage,
                PDPageContentStream.AppendMode.APPEND,
                true, true
        )) {
            contentStream.beginText();

            float yDistance = 0;
            float maxXDistance = 0;
            for(var line : paragraph) {

                // figure out the baseline of next line before drawing
                float maxHeight = 0;
                for(var token : line) {
                    float lineHeight = token.getHeight() ;
                    if (lineHeight > maxHeight)
                        maxHeight = lineHeight;
                }
                yDistance += maxHeight * Token.SPACING_RATIO;

                float xDistance = 0;
                for(var token : line) {
                    token.render(contentStream, x + xDistance, y - yDistance);
                    xDistance += token.getWidth();
                }
                if(xDistance > maxXDistance)
                    maxXDistance = xDistance;
            }

            contentStream.endText();

            // return height
            return new Rectangle(0, 0, maxXDistance, yDistance);
        } catch (IOException e) {
            System.out.println("Rendering failed");
        }

        return new Rectangle(0, 0, 0, 0);

    }

}
