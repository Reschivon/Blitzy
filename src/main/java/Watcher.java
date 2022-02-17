import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

public class Watcher {
    public static void main(String[] args) throws IOException {
        String inFile = "syntax_sample";
        String outFile = "output.pdf";

        Path path = Path.of(inFile);

        System.out.println("Watching " + path.toAbsolutePath() + " for changes");

        try (var watchService = FileSystems.getDefault().newWatchService();
             var scanner = new Scanner(System.in)) {

            path.toAbsolutePath().getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                var watchKey = watchService.take();

                for (var event : watchKey.pollEvents()) {
                    //we only register "ENTRY_MODIFY" so the context is always a Path.
                    final Path changed = (Path) event.context();

                    if (changed.endsWith(inFile)) {
                        System.out.println("File changed " + changed);
                        Parser.rubbify(inFile, outFile);
                        break;
                    }
                }

                // reset the key
                watchKey.reset();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
