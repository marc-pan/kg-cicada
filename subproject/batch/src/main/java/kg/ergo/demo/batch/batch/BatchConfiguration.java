package kg.ergo.demo.batch.batch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * batch configuration for usage metrics
 */
@Configuration
@EnableRetry
public class BatchConfiguration {
    @Autowired
    private UsageRepository usageRepository;

    @Bean
    public JobExecutionListener meteringLogListener(String jobName) {
        return new MeteringLogListener(jobName);
    }

    @Bean
    @JobScope
    public ItemReader<HubUsage> archiveReader(@Value("#{jobParameters['year']}") Integer year,
            @Value("#{jobParameters['month']}") Integer month) {
        RepositoryItemReader<HubUsage> reader = new RepositoryItemReader<>();
        reader.setRepository(usageRepository);
        reader.setMethodName("findSubmittedMetricsInSpecifiedMonth");
        Map<String, Sort.Direction> sorts = Map.of("resourceInstanceCRN", Direction.ASC);
        reader.setSort(sorts);
        List<Integer> arguments = Arrays.asList(year, month);
        reader.setArguments(arguments);
        return reader;
    }

    @Bean
    public FlatFileItemWriter<HubUsage> archiveWriter() {
        return new FlatFileItemWriterBuilder<HubUsage>()
                .name("write-usage-csv")
                .delimited().delimiter(",")
                .names(Constants.ARCHIVE_HEADER_NAMES_ARRAY)
                .resource(new FileSystemResource(Constants.ARCHIVE_FILE_NAME))
                .headerCallback(new ArchiveFileHeaderCallback())
                .build();
    }

    @Bean
    public UpdateArchiveStatus updateArchiveStatus() {
        return new UpdateArchiveStatus();
    }

    @Bean
    public CompositeItemWriter<HubUsage> compositeArchiveWriters() {
        CompositeItemWriter<HubUsage> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(archiveWriter(), updateArchiveStatus()));
        return compositeItemWriter;
    }

    @Bean
    @JobScope
    public Step archiveUsageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            FlatFileItemWriter<HubUsage> writer, @Value("#{jobParameters['year']}") Integer year,
            @Value("#{jobParameters['month']}") Integer month) {
        return new StepBuilder("archiveUsageStep", jobRepository)
                .<HubUsage, HubUsage> chunk(Constants.ARCHIVE_CHUNK_SIZE, transactionManager)
                .reader(archiveReader(year, month))
                .writer(compositeArchiveWriters())
                .faultTolerant()
                // retry for everything
                .retry(Exception.class)
                .retryLimit(Constants.ARCHIVE_RETRY_TIME)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Job archiveUsageJob(JobRepository jobRepository,
            Step archiveUsageStep, Step uploadUsageStep, Step cleanupUsageStep) {
        return new JobBuilder("archiveUsageJob", jobRepository)
                .listener(meteringLogListener("archiveUsageJob"))
                .incrementer(new RunIdIncrementer())
                .start(archiveUsageStep)
                .next(uploadUsageStep)
                .next(cleanupUsageStep)
                .build();
    }
}
