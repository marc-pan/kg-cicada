package kg.ergo.demo.batch.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EntityScan(basePackages = {"kg.ergo.demo.batch.batch"})
@Slf4j
public class BatchApplication implements CommandLineRunner, ExitCodeGenerator {
    private int exitCode; // initialized with 0

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("archiveUsageJob")
    private Job archiveUsageJob;

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Job jobToExecute = null;

        log.info("Argument passed in the job - archiveJob ");
        jobToExecute = archiveUsageJob;
        log.info(String.format("Executing job %s ", jobToExecute.getName()));

        /*
         * Call jobLauncher with the batch job to be executed.
         * archival job is initiated with parameters month, year
         * It archives data of the given month, uploads it to a designated storage
         * location
         * and then deletes the records from DB.
         * currently we archive hub_usage records that are 2 months old from the current
         * time.
         */
        JobExecution jobExecution = null;
        try {
            jobExecution = jobLauncher.run(archiveUsageJob, Utils.getCurrentDateJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            log.error("Job execution failed with error " + e.getMessage());
            log.error("{} :Job {} Execution failed {}", ServiceCodes.DPXMTR0007, jobToExecute.getName(),
                    e.getMessage());

            exitCode = 1;
        }

        if (null != jobExecution && jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info(String.format(" %s -  Job Execution was successful : %s", jobToExecute.getName(),
                    jobExecution.getStatus().toString()));
            exitCode = 0;
        } else {
            log.error("{} :Job {} Execution failed {}", ServiceCodes.DPXMTR0007, jobToExecute.getName(),
                    jobExecution.getStatus().toString());
            exitCode = 1;
        }
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }
}
