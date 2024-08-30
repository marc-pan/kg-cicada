package kg.ergo.demo.batch.batch;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArchiveFileHeaderCallback implements FlatFileHeaderCallback {

    /**
     * @param writer
     * @throws IOException
     * @Description: Write the header for the CSV file
     */
    @Override
    public void writeHeader(@NonNull Writer writer) throws IOException {
        String archiveFileHeader = StringUtils.join(Constants.ARCHIVE_HEADER_NAMES_ARRAY, ",");
        writer.write(archiveFileHeader);
        log.info("writing metrics into a csv file");
    }

}
