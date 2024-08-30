package kg.ergo.demo.batch.batch;

public enum ServiceCodes {

    DPXMTR0001, // Successfully consumed CAMS metrics
    DPXMTR0002, // Successfully consumed Broker metrics
    DPXMTR0003, // Error collecting CAMS metrics
    DPXMTR0004, // Error collecting Broker metrics
    DPXMTR0005, // Error converting Resource Metrics object to json string

    DPXMTR0006, // Unable to submit BSS metrics with error code

    DPXMTR0007, // Error executing metering cronjob
    DPXMTR0008, // Successfully submitted BSS metrics
    DPXMTR0009, // Successfully converted Resource Metrics object to json string

    DPXMTR0010, // BSS metric submission was successful
    DPXMTR0011, // BSS metric submission failed with schema validation error
    DPXMTR0012, // BSS metric submission failed with Authentication error
    DPXMTR0013, // BSS metric submission failed with Authorization error
    DPXMTR0014, // BSS metric submission failed with plan/instance not found error
    DPXMTR0015, // BSS metric submission failed with large payload error
    DPXMTR0016, // BSS metric submission failed with conflict error
    DPXMTR0017, // BSS metric submission failed with internal server error
    DPXMTR0018 // Plan configurations could not be loaded

}
