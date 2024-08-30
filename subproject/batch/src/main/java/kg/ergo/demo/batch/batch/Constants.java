package kg.ergo.demo.batch.batch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    public static final String GEN_KEY = "gen-";
    public static final String HYPHEN_KEY = "-";
    public static final String EMPTY_STRING = "";
    public static final String TRANSACTION_KEY = "transactionId";
    public static final int ARCHIVE_RETRY_TIME = 3;
    public static final int ARCHIVE_CHUNK_SIZE = 500;
    public static final String ARCHIVE_FILE_NAME = "metering/hub-metering-archive.csv";
    protected static final String[] ARCHIVE_HEADER_NAMES_ARRAY = {"resourceInstanceCRN", "planId", "region",
            "metricsStartTime", "metricsEndTime", "measure", "quantity"};
}
