package kg.ergo.demo.batch.batch;

import java.util.UUID;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;

/**
 * Intercepts each and every new job executions.
 * jobInstanceId from JobExecution will act as a tractionId and will set in
 * ThreadContext before job execution starts for logging purpose
 * ThreadContext will be cleared after the job execution ends
 * (See - https://logging.apache.org/log4j/2.x/manual/thread-context.html)
 */
@Slf4j
public class MeteringLogListener implements JobExecutionListener {
    private String jobName;

    public MeteringLogListener(String jobName) {
        this.jobName = jobName;
    }

    /**
     * @param jobExecution
     * @Description: setting transaction id in ThreadContext for logs
     */
    @Override
    public void beforeJob(@NonNull JobExecution jobExecution) {

        if (jobExecution.getStatus() == BatchStatus.STARTED) {
            log.info("Job execution starts : {} " + jobName);
        }

        String transactionId = Constants.GEN_KEY
                + UUID.randomUUID().toString().replace(Constants.HYPHEN_KEY, Constants.EMPTY_STRING);
        ThreadContext.put(Constants.TRANSACTION_KEY, transactionId);
    }

    /**
     * clears ThreadContext once jobExecution completes
     */
    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        log.info("Job execution completed : {}" + jobName);
        ThreadContext.clearAll();
    }

}
