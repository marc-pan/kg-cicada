package kg.ergo.demo.batch.batch;

import java.time.LocalDate;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    /**
     * @return JobParameters
     * @Description: Retrieve job parameters based on the current date and return
     *               the year and month for the month that
     *               is two months prior to the current month.
     */
    public static JobParameters getCurrentDateJobParameters() {
        LocalDate now = LocalDate.now();
        int monthToArchive = now.getMonthValue() - 2;
        int year = now.getYear();

        // If current month is march , monthToArchive will be 3 - 2 = 1 (take backup of
        // jan month)
        // If current month is feb , monthToArchive will be 2 - 2 = 0 (take backup of
        // dec of prev year)
        // If current month is Jan , monthToArchive will be 1 - 2 = -1 (take backup of
        // Nov of prev year)
        if (monthToArchive == 0) {
            monthToArchive = 12;
            year--;
        } else if (monthToArchive == -1) {
            monthToArchive = 11;
            year--;
        }

        log.info("Taking backup for the month: " + monthToArchive);
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addJobParameter("year", new JobParameter<>(year, Integer.class))
                .addJobParameter("month", new JobParameter<>(monthToArchive, Integer.class))
                .toJobParameters();
    }

}
