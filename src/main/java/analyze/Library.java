package analyze;

import java.util.List;

public record Library(
        List<LibraryFile> libraries
) {
    public record LibraryFile(
            String fileName,
            String content
    ) {
    }

}